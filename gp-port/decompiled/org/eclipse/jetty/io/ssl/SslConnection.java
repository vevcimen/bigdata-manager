/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.io.ssl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.ToIntFunction;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;
import org.eclipse.jetty.io.AbstractConnection;
import org.eclipse.jetty.io.AbstractEndPoint;
import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.io.WriteFlusher;
import org.eclipse.jetty.io.ssl.SslHandshakeListener;
import org.eclipse.jetty.util.BufferUtil;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.thread.Invocable;

public class SslConnection
extends AbstractConnection
implements Connection.UpgradeTo {
    private static final Logger LOG = Log.getLogger(SslConnection.class);
    private static final String TLS_1_3 = "TLSv1.3";
    private final List<SslHandshakeListener> handshakeListeners = new ArrayList<SslHandshakeListener>();
    private final ByteBufferPool _bufferPool;
    private final SSLEngine _sslEngine;
    private final DecryptedEndPoint _decryptedEndPoint;
    private ByteBuffer _decryptedInput;
    private ByteBuffer _encryptedInput;
    private ByteBuffer _encryptedOutput;
    private final boolean _encryptedDirectBuffers;
    private final boolean _decryptedDirectBuffers;
    private boolean _renegotiationAllowed;
    private int _renegotiationLimit = -1;
    private boolean _closedOutbound;
    private boolean _requireCloseMessage;
    private FlushState _flushState = FlushState.IDLE;
    private FillState _fillState = FillState.IDLE;
    private AtomicReference<HandshakeState> _handshake = new AtomicReference<HandshakeState>(HandshakeState.INITIAL);
    private boolean _underflown;
    private final Runnable _runFillable = new RunnableTask("runFillable"){

        @Override
        public void run() {
            SslConnection.this._decryptedEndPoint.getFillInterest().fillable();
        }

        @Override
        public Invocable.InvocationType getInvocationType() {
            return SslConnection.this._decryptedEndPoint.getFillInterest().getCallbackInvocationType();
        }
    };
    private final Callback _sslReadCallback = new Callback(){

        @Override
        public void succeeded() {
            SslConnection.this.onFillable();
        }

        @Override
        public void failed(Throwable x) {
            SslConnection.this.onFillInterestedFailed(x);
        }

        @Override
        public Invocable.InvocationType getInvocationType() {
            return SslConnection.this.getDecryptedEndPoint().getFillInterest().getCallbackInvocationType();
        }

        public String toString() {
            return String.format("SSLC.NBReadCB@%x{%s}", SslConnection.this.hashCode(), SslConnection.this);
        }
    };

    public SslConnection(ByteBufferPool byteBufferPool, Executor executor, EndPoint endPoint, SSLEngine sslEngine) {
        this(byteBufferPool, executor, endPoint, sslEngine, false, false);
    }

    public SslConnection(ByteBufferPool byteBufferPool, Executor executor, EndPoint endPoint, SSLEngine sslEngine, boolean useDirectBuffersForEncryption, boolean useDirectBuffersForDecryption) {
        super(endPoint, executor);
        this._bufferPool = byteBufferPool;
        this._sslEngine = sslEngine;
        this._decryptedEndPoint = this.newDecryptedEndPoint();
        this._encryptedDirectBuffers = useDirectBuffersForEncryption;
        this._decryptedDirectBuffers = useDirectBuffersForDecryption;
    }

    public void addHandshakeListener(SslHandshakeListener listener) {
        this.handshakeListeners.add(listener);
    }

    public boolean removeHandshakeListener(SslHandshakeListener listener) {
        return this.handshakeListeners.remove(listener);
    }

    protected DecryptedEndPoint newDecryptedEndPoint() {
        return new DecryptedEndPoint();
    }

    public SSLEngine getSSLEngine() {
        return this._sslEngine;
    }

    public DecryptedEndPoint getDecryptedEndPoint() {
        return this._decryptedEndPoint;
    }

    public boolean isRenegotiationAllowed() {
        return this._renegotiationAllowed;
    }

    public void setRenegotiationAllowed(boolean renegotiationAllowed) {
        this._renegotiationAllowed = renegotiationAllowed;
    }

    public int getRenegotiationLimit() {
        return this._renegotiationLimit;
    }

    public void setRenegotiationLimit(int renegotiationLimit) {
        this._renegotiationLimit = renegotiationLimit;
    }

    @Deprecated
    public boolean isAllowMissingCloseMessage() {
        return !this.isRequireCloseMessage();
    }

    @Deprecated
    public void setAllowMissingCloseMessage(boolean allowMissingCloseMessage) {
        this.setRequireCloseMessage(!allowMissingCloseMessage);
    }

    public boolean isRequireCloseMessage() {
        return this._requireCloseMessage;
    }

    public void setRequireCloseMessage(boolean requireCloseMessage) {
        this._requireCloseMessage = requireCloseMessage;
    }

    private boolean isHandshakeInitial() {
        return this._handshake.get() == HandshakeState.INITIAL;
    }

    private boolean isHandshakeSucceeded() {
        return this._handshake.get() == HandshakeState.SUCCEEDED;
    }

    private boolean isHandshakeComplete() {
        HandshakeState state = this._handshake.get();
        return state == HandshakeState.SUCCEEDED || state == HandshakeState.FAILED;
    }

    private int getApplicationBufferSize() {
        return this.getBufferSize(SSLSession::getApplicationBufferSize);
    }

    private int getPacketBufferSize() {
        return this.getBufferSize(SSLSession::getPacketBufferSize);
    }

    private int getBufferSize(ToIntFunction<SSLSession> bufferSizeFn) {
        SSLSession hsSession = this._sslEngine.getHandshakeSession();
        SSLSession session = this._sslEngine.getSession();
        int size = bufferSizeFn.applyAsInt(session);
        if (hsSession == null || hsSession == session) {
            return size;
        }
        int hsSize = bufferSizeFn.applyAsInt(hsSession);
        return Math.max(hsSize, size);
    }

    private void acquireEncryptedInput() {
        if (this._encryptedInput == null) {
            this._encryptedInput = this._bufferPool.acquire(this.getPacketBufferSize(), this._encryptedDirectBuffers);
        }
    }

    private void acquireEncryptedOutput() {
        if (this._encryptedOutput == null) {
            this._encryptedOutput = this._bufferPool.acquire(this.getPacketBufferSize(), this._encryptedDirectBuffers);
        }
    }

    @Override
    public void onUpgradeTo(ByteBuffer buffer) {
        this.acquireEncryptedInput();
        BufferUtil.append(this._encryptedInput, buffer);
    }

    @Override
    public void onOpen() {
        super.onOpen();
        this.getDecryptedEndPoint().getConnection().onOpen();
    }

    @Override
    public void onClose() {
        this._decryptedEndPoint.getConnection().onClose();
        super.onClose();
    }

    @Override
    public void close() {
        this.getDecryptedEndPoint().getConnection().close();
    }

    @Override
    public boolean onIdleExpired() {
        return this.getDecryptedEndPoint().getConnection().onIdleExpired();
    }

    @Override
    public void onFillable() {
        if (LOG.isDebugEnabled()) {
            LOG.debug(">c.onFillable {}", this);
        }
        if (this._decryptedEndPoint.isInputShutdown()) {
            this._decryptedEndPoint.close();
        }
        this._decryptedEndPoint.onFillable();
        if (LOG.isDebugEnabled()) {
            LOG.debug("<c.onFillable {}", this);
        }
    }

    @Override
    public void onFillInterestedFailed(Throwable cause) {
        this._decryptedEndPoint.onFillableFail(cause == null ? new IOException() : cause);
    }

    protected SSLEngineResult wrap(SSLEngine sslEngine, ByteBuffer[] input, ByteBuffer output) throws SSLException {
        return sslEngine.wrap(input, output);
    }

    protected SSLEngineResult unwrap(SSLEngine sslEngine, ByteBuffer input, ByteBuffer output) throws SSLException {
        return sslEngine.unwrap(input, output);
    }

    @Override
    public String toConnectionString() {
        ByteBuffer b = this._encryptedInput;
        int ei = b == null ? -1 : b.remaining();
        b = this._encryptedOutput;
        int eo = b == null ? -1 : b.remaining();
        b = this._decryptedInput;
        int di = b == null ? -1 : b.remaining();
        Connection connection = this._decryptedEndPoint.getConnection();
        return String.format("%s@%x{%s,eio=%d/%d,di=%d,fill=%s,flush=%s}~>%s=>%s", new Object[]{this.getClass().getSimpleName(), this.hashCode(), this._sslEngine.getHandshakeStatus(), ei, eo, di, this._fillState, this._flushState, this._decryptedEndPoint.toEndPointString(), connection instanceof AbstractConnection ? ((AbstractConnection)connection).toConnectionString() : connection});
    }

    private void releaseEncryptedInputBuffer() {
        if (this._encryptedInput != null && !this._encryptedInput.hasRemaining()) {
            this._bufferPool.release(this._encryptedInput);
            this._encryptedInput = null;
        }
    }

    protected void releaseDecryptedInputBuffer() {
        if (this._decryptedInput != null && !this._decryptedInput.hasRemaining()) {
            this._bufferPool.release(this._decryptedInput);
            this._decryptedInput = null;
        }
    }

    private void releaseEncryptedOutputBuffer() {
        if (!Thread.holdsLock(this._decryptedEndPoint)) {
            throw new IllegalStateException();
        }
        if (this._encryptedOutput != null && !this._encryptedOutput.hasRemaining()) {
            this._bufferPool.release(this._encryptedOutput);
            this._encryptedOutput = null;
        }
    }

    protected int networkFill(ByteBuffer input) throws IOException {
        return this.getEndPoint().fill(input);
    }

    protected boolean networkFlush(ByteBuffer output) throws IOException {
        return this.getEndPoint().flush(output);
    }

    static /* synthetic */ void access$800(SslConnection x0) {
        x0.acquireEncryptedInput();
    }

    static /* synthetic */ ByteBuffer access$602(SslConnection x0, ByteBuffer x1) {
        x0._decryptedInput = x1;
        return x0._decryptedInput;
    }

    static /* synthetic */ boolean access$900(SslConnection x0) {
        return x0._decryptedDirectBuffers;
    }

    static /* synthetic */ ByteBufferPool access$1000(SslConnection x0) {
        return x0._bufferPool;
    }

    static /* synthetic */ boolean access$1200(SslConnection x0) {
        return x0.isHandshakeInitial();
    }

    static /* synthetic */ boolean access$1502(SslConnection x0, boolean x1) {
        x0._underflown = x1;
        return x0._underflown;
    }

    static /* synthetic */ Executor access$1600(SslConnection x0) {
        return x0.getExecutor();
    }

    static /* synthetic */ void access$1700(SslConnection x0) {
        x0.releaseEncryptedInputBuffer();
    }

    static /* synthetic */ Executor access$1800(SslConnection x0) {
        return x0.getExecutor();
    }

    static /* synthetic */ int access$2400(SslConnection x0) {
        return x0.getPacketBufferSize();
    }

    static /* synthetic */ void access$2500(SslConnection x0) {
        x0.acquireEncryptedOutput();
    }

    public class DecryptedEndPoint
    extends AbstractEndPoint {
        private final Callback _incompleteWriteCallback;
        private Throwable _failure;

        public DecryptedEndPoint() {
            super(null);
            this._incompleteWriteCallback = new IncompleteWriteCallback();
            super.setIdleTimeout(-1L);
        }

        @Override
        public long getIdleTimeout() {
            return SslConnection.this.getEndPoint().getIdleTimeout();
        }

        @Override
        public void setIdleTimeout(long idleTimeout) {
            SslConnection.this.getEndPoint().setIdleTimeout(idleTimeout);
        }

        @Override
        public boolean isOpen() {
            return SslConnection.this.getEndPoint().isOpen();
        }

        @Override
        public InetSocketAddress getLocalAddress() {
            return SslConnection.this.getEndPoint().getLocalAddress();
        }

        @Override
        public InetSocketAddress getRemoteAddress() {
            return SslConnection.this.getEndPoint().getRemoteAddress();
        }

        @Override
        public WriteFlusher getWriteFlusher() {
            return super.getWriteFlusher();
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        protected void onFillable() {
            block10: {
                try {
                    boolean waitingForFill;
                    DecryptedEndPoint decryptedEndPoint = SslConnection.this._decryptedEndPoint;
                    synchronized (decryptedEndPoint) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("onFillable {}", SslConnection.this);
                        }
                        SslConnection.this._fillState = FillState.IDLE;
                        waitingForFill = SslConnection.this._flushState == FlushState.WAIT_FOR_FILL;
                    }
                    this.getFillInterest().fillable();
                    if (!waitingForFill) break block10;
                    decryptedEndPoint = SslConnection.this._decryptedEndPoint;
                    synchronized (decryptedEndPoint) {
                        waitingForFill = SslConnection.this._flushState == FlushState.WAIT_FOR_FILL;
                    }
                    if (waitingForFill) {
                        this.fill(BufferUtil.EMPTY_BUFFER);
                    }
                }
                catch (Throwable e) {
                    this.close(e);
                }
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        protected void onFillableFail(Throwable failure) {
            boolean fail = false;
            DecryptedEndPoint decryptedEndPoint = SslConnection.this._decryptedEndPoint;
            synchronized (decryptedEndPoint) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("onFillableFail {}", SslConnection.this, failure);
                }
                SslConnection.this._fillState = FillState.IDLE;
                if (SslConnection.this._flushState == FlushState.WAIT_FOR_FILL) {
                    SslConnection.this._flushState = FlushState.IDLE;
                    fail = true;
                }
            }
            this.getFillInterest().onFail(failure);
            if (fail && !this.getWriteFlusher().onFail(failure)) {
                this.close(failure);
            }
        }

        @Override
        public void setConnection(Connection connection) {
            if (connection instanceof AbstractConnection) {
                AbstractConnection c = (AbstractConnection)connection;
                int appBufferSize = SslConnection.this.getApplicationBufferSize();
                if (c.getInputBufferSize() < appBufferSize) {
                    c.setInputBufferSize(appBufferSize);
                }
            }
            super.setConnection(connection);
        }

        public SslConnection getSslConnection() {
            return SslConnection.this;
        }

        /*
         * Exception decompiling
         */
        @Override
        public int fill(ByteBuffer buffer) throws IOException {
            /*
             * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
             * 
             * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [0[TRYBLOCK]], but top level block is 26[MONITOR]
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.processEndingBlocks(Op04StructuredStatement.java:435)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:484)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
             *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
             *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
             *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
             *     at org.benf.cfr.reader.entities.ClassFile.analyseInnerClassesPass1(ClassFile.java:923)
             *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1035)
             *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
             *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
             *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
             *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
             *     at org.benf.cfr.reader.Main.main(Main.java:54)
             */
            throw new IllegalStateException("Decompilation failed");
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        protected void needsFillInterest() {
            try {
                boolean fillable;
                ByteBuffer write = null;
                boolean interest = false;
                DecryptedEndPoint decryptedEndPoint = SslConnection.this._decryptedEndPoint;
                synchronized (decryptedEndPoint) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(">needFillInterest s={}/{} uf={} ei={} di={} {}", new Object[]{SslConnection.this._flushState, SslConnection.this._fillState, SslConnection.this._underflown, BufferUtil.toDetailString(SslConnection.this._encryptedInput), BufferUtil.toDetailString(SslConnection.this._decryptedInput), SslConnection.this});
                    }
                    if (SslConnection.this._fillState != FillState.IDLE) {
                        return;
                    }
                    fillable = BufferUtil.hasContent(SslConnection.this._decryptedInput) || BufferUtil.hasContent(SslConnection.this._encryptedInput) && !SslConnection.this._underflown;
                    SSLEngineResult.HandshakeStatus status = SslConnection.this._sslEngine.getHandshakeStatus();
                    switch (status) {
                        case NEED_TASK: {
                            fillable = true;
                            break;
                        }
                        case NEED_UNWRAP: 
                        case NOT_HANDSHAKING: {
                            if (fillable) break;
                            interest = true;
                            SslConnection.this._fillState = FillState.INTERESTED;
                            if (SslConnection.this._flushState != FlushState.IDLE || !BufferUtil.hasContent(SslConnection.this._encryptedOutput)) break;
                            SslConnection.this._flushState = FlushState.WRITING;
                            write = SslConnection.this._encryptedOutput;
                            break;
                        }
                        case NEED_WRAP: {
                            if (fillable) break;
                            SslConnection.this._fillState = FillState.WAIT_FOR_FLUSH;
                            if (SslConnection.this._flushState != FlushState.IDLE) break;
                            SslConnection.this._flushState = FlushState.WRITING;
                            write = BufferUtil.hasContent(SslConnection.this._encryptedOutput) ? SslConnection.this._encryptedOutput : BufferUtil.EMPTY_BUFFER;
                            break;
                        }
                        default: {
                            throw new IllegalStateException("Unexpected HandshakeStatus " + (Object)((Object)status));
                        }
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("<needFillInterest s={}/{} f={} i={} w={}", new Object[]{SslConnection.this._flushState, SslConnection.this._fillState, fillable, interest, BufferUtil.toDetailString(write)});
                    }
                }
                if (write != null) {
                    SslConnection.this.getEndPoint().write(this._incompleteWriteCallback, write);
                } else if (fillable) {
                    SslConnection.this.getExecutor().execute(SslConnection.this._runFillable);
                } else if (interest) {
                    this.ensureFillInterested();
                }
            }
            catch (Throwable x) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(SslConnection.this.toString(), x);
                }
                this.close(x);
                throw x;
            }
        }

        private void handshakeSucceeded() throws SSLException {
            if (SslConnection.this._handshake.compareAndSet(HandshakeState.HANDSHAKE, HandshakeState.SUCCEEDED)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("handshake succeeded {} {} {}/{}", SslConnection.this, SslConnection.this._sslEngine.getUseClientMode() ? "client" : "resumed server", SslConnection.this._sslEngine.getSession().getProtocol(), SslConnection.this._sslEngine.getSession().getCipherSuite());
                }
                this.notifyHandshakeSucceeded(SslConnection.this._sslEngine);
            } else if (SslConnection.this.isHandshakeSucceeded() && SslConnection.this._renegotiationLimit > 0) {
                SslConnection.this._renegotiationLimit--;
            }
        }

        private Throwable handshakeFailed(Throwable failure) {
            if (SslConnection.this._handshake.compareAndSet(HandshakeState.HANDSHAKE, HandshakeState.FAILED)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("handshake failed {} {}", SslConnection.this, failure);
                }
                if (!(failure instanceof SSLHandshakeException)) {
                    failure = new SSLHandshakeException(failure.getMessage()).initCause(failure);
                }
                this.notifyHandshakeFailed(SslConnection.this._sslEngine, failure);
            }
            return failure;
        }

        private void terminateInput() {
            try {
                SslConnection.this._sslEngine.closeInbound();
            }
            catch (Throwable x) {
                LOG.ignore(x);
            }
        }

        private Throwable closeInbound() throws SSLException {
            SSLEngineResult.HandshakeStatus handshakeStatus = SslConnection.this._sslEngine.getHandshakeStatus();
            try {
                SslConnection.this._sslEngine.closeInbound();
                return null;
            }
            catch (SSLException x) {
                if (handshakeStatus == SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING && SslConnection.this.isRequireCloseMessage()) {
                    throw x;
                }
                LOG.ignore(x);
                return x;
            }
            catch (Throwable x) {
                LOG.ignore(x);
                return x;
            }
        }

        /*
         * Exception decompiling
         */
        @Override
        public boolean flush(ByteBuffer ... appOuts) throws IOException {
            /*
             * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
             * 
             * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [0[TRYBLOCK]], but top level block is 24[MONITOR]
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.processEndingBlocks(Op04StructuredStatement.java:435)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:484)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
             *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
             *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
             *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
             *     at org.benf.cfr.reader.entities.ClassFile.analyseInnerClassesPass1(ClassFile.java:923)
             *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1035)
             *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
             *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
             *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
             *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
             *     at org.benf.cfr.reader.Main.main(Main.java:54)
             */
            throw new IllegalStateException("Decompilation failed");
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        protected void onIncompleteFlush() {
            try {
                boolean fillInterest = false;
                ByteBuffer write = null;
                DecryptedEndPoint decryptedEndPoint = SslConnection.this._decryptedEndPoint;
                synchronized (decryptedEndPoint) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(">onIncompleteFlush {} {}", SslConnection.this, BufferUtil.toDetailString(SslConnection.this._encryptedOutput));
                    }
                    if (SslConnection.this._flushState != FlushState.IDLE) {
                        return;
                    }
                    block11: while (true) {
                        SSLEngineResult.HandshakeStatus status = SslConnection.this._sslEngine.getHandshakeStatus();
                        switch (status) {
                            case NOT_HANDSHAKING: 
                            case NEED_TASK: 
                            case NEED_WRAP: {
                                write = BufferUtil.hasContent(SslConnection.this._encryptedOutput) ? SslConnection.this._encryptedOutput : BufferUtil.EMPTY_BUFFER;
                                SslConnection.this._flushState = FlushState.WRITING;
                                break block11;
                            }
                            case NEED_UNWRAP: {
                                if (BufferUtil.hasContent(SslConnection.this._encryptedOutput)) {
                                    write = SslConnection.this._encryptedOutput;
                                    SslConnection.this._flushState = FlushState.WRITING;
                                    break block11;
                                }
                                if (SslConnection.this._fillState != FillState.IDLE) {
                                    SslConnection.this._flushState = FlushState.WAIT_FOR_FILL;
                                    break block11;
                                }
                                try {
                                    int filled = this.fill(BufferUtil.EMPTY_BUFFER);
                                    if (SslConnection.this._sslEngine.getHandshakeStatus() != status) continue block11;
                                    if (filled < 0) {
                                        throw new IOException("Broken pipe");
                                    }
                                }
                                catch (IOException e) {
                                    LOG.debug(e);
                                    this.close(e);
                                    write = BufferUtil.EMPTY_BUFFER;
                                    SslConnection.this._flushState = FlushState.WRITING;
                                    break block11;
                                }
                                fillInterest = true;
                                SslConnection.this._fillState = FillState.INTERESTED;
                                SslConnection.this._flushState = FlushState.WAIT_FOR_FILL;
                                break block11;
                            }
                            default: {
                                throw new IllegalStateException("Unexpected HandshakeStatus " + (Object)((Object)status));
                            }
                        }
                        break;
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("<onIncompleteFlush s={}/{} fi={} w={}", new Object[]{SslConnection.this._flushState, SslConnection.this._fillState, fillInterest, BufferUtil.toDetailString(write)});
                    }
                }
                if (write != null) {
                    SslConnection.this.getEndPoint().write(this._incompleteWriteCallback, write);
                } else if (fillInterest) {
                    this.ensureFillInterested();
                }
            }
            catch (Throwable x) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(SslConnection.this.toString(), x);
                }
                this.close(x);
                throw x;
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void doShutdownOutput() {
            EndPoint endPoint = SslConnection.this.getEndPoint();
            try {
                boolean close;
                boolean flush = false;
                DecryptedEndPoint decryptedEndPoint = SslConnection.this._decryptedEndPoint;
                synchronized (decryptedEndPoint) {
                    boolean ishut = endPoint.isInputShutdown();
                    boolean oshut = endPoint.isOutputShutdown();
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("shutdownOutput: {} oshut={}, ishut={}", SslConnection.this, oshut, ishut);
                    }
                    this.closeOutbound();
                    if (!SslConnection.this._closedOutbound) {
                        SslConnection.this._closedOutbound = true;
                        flush = !oshut;
                    }
                    close = ishut;
                }
                if (flush && !this.flush(BufferUtil.EMPTY_BUFFER) && !close) {
                    ByteBuffer write = null;
                    DecryptedEndPoint decryptedEndPoint2 = SslConnection.this._decryptedEndPoint;
                    synchronized (decryptedEndPoint2) {
                        if (BufferUtil.hasContent(SslConnection.this._encryptedOutput)) {
                            write = SslConnection.this._encryptedOutput;
                            SslConnection.this._flushState = FlushState.WRITING;
                        }
                    }
                    if (write != null) {
                        endPoint.write(Callback.from(() -> {
                            DecryptedEndPoint decryptedEndPoint = SslConnection.this._decryptedEndPoint;
                            synchronized (decryptedEndPoint) {
                                SslConnection.this._flushState = FlushState.IDLE;
                                SslConnection.this.releaseEncryptedOutputBuffer();
                            }
                        }, t -> endPoint.close()), write);
                    }
                }
                if (close) {
                    endPoint.close();
                } else {
                    this.ensureFillInterested();
                }
            }
            catch (Throwable x) {
                LOG.ignore(x);
                endPoint.close();
            }
        }

        private void closeOutbound() {
            block2: {
                try {
                    SslConnection.this._sslEngine.closeOutbound();
                }
                catch (Throwable x) {
                    if (!LOG.isDebugEnabled()) break block2;
                    LOG.debug(x);
                }
            }
        }

        private void ensureFillInterested() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("ensureFillInterested {}", SslConnection.this);
            }
            SslConnection.this.tryFillInterested(SslConnection.this._sslReadCallback);
        }

        @Override
        public boolean isOutputShutdown() {
            return this.isOutboundDone() || SslConnection.this.getEndPoint().isOutputShutdown();
        }

        private boolean isOutboundDone() {
            try {
                return SslConnection.this._sslEngine.isOutboundDone();
            }
            catch (Throwable x) {
                LOG.ignore(x);
                return true;
            }
        }

        @Override
        public void doClose() {
            this.doShutdownOutput();
            SslConnection.this.getEndPoint().close();
            super.doClose();
        }

        @Override
        public Object getTransport() {
            return SslConnection.this.getEndPoint();
        }

        @Override
        public boolean isInputShutdown() {
            return BufferUtil.isEmpty(SslConnection.this._decryptedInput) && (SslConnection.this.getEndPoint().isInputShutdown() || this.isInboundDone());
        }

        private boolean isInboundDone() {
            try {
                return SslConnection.this._sslEngine.isInboundDone();
            }
            catch (Throwable x) {
                LOG.ignore(x);
                return true;
            }
        }

        private void notifyHandshakeSucceeded(SSLEngine sslEngine) throws SSLException {
            SslHandshakeListener.Event event = null;
            for (SslHandshakeListener listener : SslConnection.this.handshakeListeners) {
                if (event == null) {
                    event = new SslHandshakeListener.Event(sslEngine);
                }
                try {
                    listener.handshakeSucceeded(event);
                }
                catch (SSLException x) {
                    throw x;
                }
                catch (Throwable x) {
                    LOG.info("Exception while notifying listener " + listener, x);
                }
            }
        }

        private void notifyHandshakeFailed(SSLEngine sslEngine, Throwable failure) {
            SslHandshakeListener.Event event = null;
            for (SslHandshakeListener listener : SslConnection.this.handshakeListeners) {
                if (event == null) {
                    event = new SslHandshakeListener.Event(sslEngine);
                }
                try {
                    listener.handshakeFailed(event, failure);
                }
                catch (Throwable x) {
                    LOG.info("Exception while notifying listener " + listener, x);
                }
            }
        }

        private boolean isRenegotiating() {
            if (!SslConnection.this.isHandshakeComplete()) {
                return false;
            }
            if (this.isTLS13()) {
                return false;
            }
            return SslConnection.this._sslEngine.getHandshakeStatus() != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING;
        }

        private boolean allowRenegotiate() {
            if (!SslConnection.this.isRenegotiationAllowed()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Renegotiation denied {}", SslConnection.this);
                }
                this.terminateInput();
                return false;
            }
            if (SslConnection.this.getRenegotiationLimit() == 0) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Renegotiation limit exceeded {}", SslConnection.this);
                }
                this.terminateInput();
                return false;
            }
            return true;
        }

        private boolean isTLS13() {
            String protocol = SslConnection.this._sslEngine.getSession().getProtocol();
            return SslConnection.TLS_1_3.equals(protocol);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private Throwable handleException(Throwable x, String context) {
            DecryptedEndPoint decryptedEndPoint = SslConnection.this._decryptedEndPoint;
            synchronized (decryptedEndPoint) {
                if (this._failure == null) {
                    this._failure = x;
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(this + " stored " + context + " exception", x);
                    }
                } else if (x != this._failure) {
                    this._failure.addSuppressed(x);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(this + " suppressed " + context + " exception", x);
                    }
                }
                return this._failure;
            }
        }

        private void rethrow(Throwable x) throws IOException {
            if (x instanceof RuntimeException) {
                throw (RuntimeException)x;
            }
            if (x instanceof Error) {
                throw (Error)x;
            }
            if (x instanceof IOException) {
                throw (IOException)x;
            }
            throw new IOException(x);
        }

        @Override
        public String toString() {
            return super.toEndPointString();
        }

        private /* synthetic */ void lambda$fill$1() {
            SslConnection.this._decryptedEndPoint.getWriteFlusher().completeWrite();
        }

        private /* synthetic */ void lambda$fill$0(Throwable failure) {
            SslConnection.this._decryptedEndPoint.getWriteFlusher().onFail(failure);
        }

        private final class IncompleteWriteCallback
        implements Callback,
        Invocable {
            private IncompleteWriteCallback() {
            }

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void succeeded() {
                boolean fillable;
                boolean interested;
                DecryptedEndPoint decryptedEndPoint = SslConnection.this._decryptedEndPoint;
                synchronized (decryptedEndPoint) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("IncompleteWriteCB succeeded {}", SslConnection.this);
                    }
                    SslConnection.this.releaseEncryptedOutputBuffer();
                    SslConnection.this._flushState = FlushState.IDLE;
                    interested = SslConnection.this._fillState == FillState.INTERESTED;
                    boolean bl = fillable = SslConnection.this._fillState == FillState.WAIT_FOR_FLUSH;
                    if (fillable) {
                        SslConnection.this._fillState = FillState.IDLE;
                    }
                }
                if (interested) {
                    DecryptedEndPoint.this.ensureFillInterested();
                } else if (fillable) {
                    SslConnection.this._decryptedEndPoint.getFillInterest().fillable();
                }
                SslConnection.this._decryptedEndPoint.getWriteFlusher().completeWrite();
            }

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void failed(Throwable x) {
                boolean failFillInterest;
                DecryptedEndPoint decryptedEndPoint = SslConnection.this._decryptedEndPoint;
                synchronized (decryptedEndPoint) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("IncompleteWriteCB failed {}", SslConnection.this, x);
                    }
                    BufferUtil.clear(SslConnection.this._encryptedOutput);
                    SslConnection.this.releaseEncryptedOutputBuffer();
                    SslConnection.this._flushState = FlushState.IDLE;
                    boolean bl = failFillInterest = SslConnection.this._fillState == FillState.WAIT_FOR_FLUSH || SslConnection.this._fillState == FillState.INTERESTED;
                    if (failFillInterest) {
                        SslConnection.this._fillState = FillState.IDLE;
                    }
                }
                SslConnection.this.getExecutor().execute(() -> {
                    if (failFillInterest) {
                        SslConnection.this._decryptedEndPoint.getFillInterest().onFail(x);
                    }
                    SslConnection.this._decryptedEndPoint.getWriteFlusher().onFail(x);
                });
            }

            @Override
            public Invocable.InvocationType getInvocationType() {
                return SslConnection.this._decryptedEndPoint.getWriteFlusher().getCallbackInvocationType();
            }

            public String toString() {
                return String.format("SSL@%h.DEP.writeCallback", SslConnection.this);
            }
        }
    }

    private abstract class RunnableTask
    implements Runnable,
    Invocable {
        private final String _operation;

        protected RunnableTask(String op) {
            this._operation = op;
        }

        public String toString() {
            return String.format("SSL:%s:%s:%s", new Object[]{SslConnection.this, this._operation, this.getInvocationType()});
        }
    }

    private static enum FlushState {
        IDLE,
        WRITING,
        WAIT_FOR_FILL;

    }

    private static enum FillState {
        IDLE,
        INTERESTED,
        WAIT_FOR_FLUSH;

    }

    private static enum HandshakeState {
        INITIAL,
        HANDSHAKE,
        SUCCEEDED,
        FAILED;

    }
}

