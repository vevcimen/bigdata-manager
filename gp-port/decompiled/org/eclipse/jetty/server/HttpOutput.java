/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritePendingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.ResourceBundle;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import org.eclipse.jetty.http.HttpContent;
import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.io.EofException;
import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.HttpChannelState;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.util.BufferUtil;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.IteratingCallback;
import org.eclipse.jetty.util.SharedBlockingCallback;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.thread.Invocable;

public class HttpOutput
extends ServletOutputStream
implements Runnable {
    private static final String LSTRING_FILE = "javax.servlet.LocalStrings";
    private static ResourceBundle lStrings = ResourceBundle.getBundle("javax.servlet.LocalStrings");
    private static Logger LOG = Log.getLogger(HttpOutput.class);
    private static final ThreadLocal<CharsetEncoder> _encoder = new ThreadLocal();
    private final HttpChannel _channel;
    private final HttpChannelState _channelState;
    private final SharedBlockingCallback _writeBlocker;
    private ApiState _apiState = ApiState.BLOCKING;
    private State _state = State.OPEN;
    private boolean _softClose = false;
    private Interceptor _interceptor;
    private long _written;
    private long _flushed;
    private long _firstByteTimeStamp = -1L;
    private ByteBuffer _aggregate;
    private int _bufferSize;
    private int _commitSize;
    private WriteListener _writeListener;
    private volatile Throwable _onError;
    private Callback _closedCallback;

    public HttpOutput(HttpChannel channel) {
        this._channel = channel;
        this._channelState = channel.getState();
        this._interceptor = channel;
        this._writeBlocker = new WriteBlocker(channel);
        HttpConfiguration config = channel.getHttpConfiguration();
        this._bufferSize = config.getOutputBufferSize();
        this._commitSize = config.getOutputAggregationSize();
        if (this._commitSize > this._bufferSize) {
            LOG.warn("OutputAggregationSize {} exceeds bufferSize {}", this._commitSize, this._bufferSize);
            this._commitSize = this._bufferSize;
        }
    }

    public HttpChannel getHttpChannel() {
        return this._channel;
    }

    public Interceptor getInterceptor() {
        return this._interceptor;
    }

    public void setInterceptor(Interceptor interceptor) {
        this._interceptor = interceptor;
    }

    public boolean isWritten() {
        return this._written > 0L;
    }

    public long getWritten() {
        return this._written;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void reopen() {
        HttpChannelState httpChannelState = this._channelState;
        synchronized (httpChannelState) {
            this._softClose = false;
        }
    }

    protected SharedBlockingCallback.Blocker acquireWriteBlockingCallback() throws IOException {
        return this._writeBlocker.acquire();
    }

    private void channelWrite(ByteBuffer content, boolean complete) throws IOException {
        try (SharedBlockingCallback.Blocker blocker = this._writeBlocker.acquire();){
            this.channelWrite(content, complete, blocker);
            blocker.block();
        }
    }

    private void channelWrite(ByteBuffer content, boolean last, Callback callback) {
        if (this._firstByteTimeStamp == -1L) {
            long minDataRate = this.getHttpChannel().getHttpConfiguration().getMinResponseDataRate();
            this._firstByteTimeStamp = minDataRate > 0L ? System.nanoTime() : Long.MAX_VALUE;
        }
        this._interceptor.write(content, last, callback);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void onWriteComplete(boolean last, Throwable failure) {
        String state = null;
        boolean wake = false;
        Callback closedCallback = null;
        ByteBuffer closeContent = null;
        HttpChannelState httpChannelState = this._channelState;
        synchronized (httpChannelState) {
            if (LOG.isDebugEnabled()) {
                state = this.stateString();
            }
            if (last || failure != null) {
                this._state = State.CLOSED;
                closedCallback = this._closedCallback;
                this._closedCallback = null;
                this.releaseBuffer(failure);
                wake = this.updateApiState(failure);
            } else if (this._state == State.CLOSE) {
                this._state = State.CLOSING;
                closeContent = BufferUtil.hasContent(this._aggregate) ? this._aggregate : BufferUtil.EMPTY_BUFFER;
            } else {
                wake = this.updateApiState(null);
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("onWriteComplete({},{}) {}->{} c={} cb={} w={}", last, failure, state, this.stateString(), BufferUtil.toDetailString(closeContent), closedCallback, wake);
        }
        try {
            if (failure != null) {
                this._channel.abort(failure);
            }
            if (closedCallback != null) {
                if (failure == null) {
                    closedCallback.succeeded();
                } else {
                    closedCallback.failed(failure);
                }
            } else if (closeContent != null) {
                this.channelWrite(closeContent, true, new WriteCompleteCB());
            }
        }
        finally {
            if (wake) {
                this._channel.execute(this._channel);
            }
        }
    }

    private boolean updateApiState(Throwable failure) {
        boolean wake = false;
        switch (this._apiState) {
            case BLOCKED: {
                this._apiState = ApiState.BLOCKING;
                break;
            }
            case PENDING: {
                this._apiState = ApiState.ASYNC;
                if (failure == null) break;
                this._onError = failure;
                wake = this._channelState.onWritePossible();
                break;
            }
            case UNREADY: {
                this._apiState = ApiState.READY;
                if (failure != null) {
                    this._onError = failure;
                }
                wake = this._channelState.onWritePossible();
                break;
            }
            default: {
                if (this._state == State.CLOSED) break;
                throw new IllegalStateException(this.stateString());
            }
        }
        return wake;
    }

    private int maximizeAggregateSpace() {
        if (this._aggregate == null) {
            return this.getBufferSize();
        }
        BufferUtil.compact(this._aggregate);
        return BufferUtil.space(this._aggregate);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void softClose() {
        HttpChannelState httpChannelState = this._channelState;
        synchronized (httpChannelState) {
            this._softClose = true;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void complete(Callback callback) {
        boolean succeeded = false;
        Throwable error = null;
        ByteBuffer content = null;
        HttpChannelState httpChannelState = this._channelState;
        synchronized (httpChannelState) {
            switch (this._apiState) {
                case UNREADY: {
                    error = new CancellationException("Completed whilst write unready");
                    break;
                }
                case PENDING: {
                    if (this._channel.getResponse().isContentComplete(this._written)) break;
                    error = new CancellationException("Completed whilst write pending");
                    break;
                }
                case BLOCKED: {
                    error = new CancellationException("Completed whilst write blocked");
                    break;
                }
            }
            if (error != null) {
                this._channel.abort(error);
                this._writeBlocker.fail(error);
                this._state = State.CLOSED;
            } else {
                block7 : switch (this._state) {
                    case CLOSED: {
                        succeeded = true;
                        break;
                    }
                    case CLOSE: 
                    case CLOSING: {
                        this._closedCallback = Callback.combine(this._closedCallback, callback);
                        break;
                    }
                    case OPEN: {
                        if (this._onError != null) {
                            error = this._onError;
                            break;
                        }
                        this._closedCallback = Callback.combine(this._closedCallback, callback);
                        switch (this._apiState) {
                            case BLOCKING: {
                                this._apiState = ApiState.BLOCKED;
                                this._state = State.CLOSING;
                                content = BufferUtil.hasContent(this._aggregate) ? this._aggregate : BufferUtil.EMPTY_BUFFER;
                                break block7;
                            }
                            case ASYNC: 
                            case READY: {
                                this._apiState = ApiState.PENDING;
                                this._state = State.CLOSING;
                                content = BufferUtil.hasContent(this._aggregate) ? this._aggregate : BufferUtil.EMPTY_BUFFER;
                                break block7;
                            }
                            case PENDING: 
                            case UNREADY: {
                                this._softClose = true;
                                this._state = State.CLOSE;
                                break block7;
                            }
                        }
                        throw new IllegalStateException();
                    }
                }
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("complete({}) {} s={} e={}, c={}", callback, this.stateString(), succeeded, error, BufferUtil.toDetailString(content));
        }
        if (succeeded) {
            callback.succeeded();
            return;
        }
        if (error != null) {
            callback.failed(error);
            return;
        }
        if (content != null) {
            this.channelWrite(content, true, new WriteCompleteCB());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void completed(Throwable failure) {
        HttpChannelState httpChannelState = this._channelState;
        synchronized (httpChannelState) {
            this._state = State.CLOSED;
            this.releaseBuffer(failure);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void close() throws IOException {
        SharedBlockingCallback.Blocker b;
        ByteBuffer content = null;
        SharedBlockingCallback.Blocker blocker = null;
        HttpChannelState httpChannelState = this._channelState;
        synchronized (httpChannelState) {
            if (this._onError != null) {
                if (this._onError instanceof IOException) {
                    throw (IOException)this._onError;
                }
                if (this._onError instanceof RuntimeException) {
                    throw (RuntimeException)this._onError;
                }
                if (this._onError instanceof Error) {
                    throw (Error)this._onError;
                }
                throw new IOException(this._onError);
            }
            block7 : switch (this._state) {
                case CLOSED: {
                    break;
                }
                case CLOSE: 
                case CLOSING: {
                    switch (this._apiState) {
                        case BLOCKED: 
                        case BLOCKING: {
                            blocker = this._writeBlocker.acquire();
                            this._closedCallback = Callback.combine(this._closedCallback, blocker);
                            break block7;
                        }
                    }
                    break;
                }
                case OPEN: {
                    switch (this._apiState) {
                        case BLOCKING: {
                            this._apiState = ApiState.BLOCKED;
                            this._state = State.CLOSING;
                            blocker = this._writeBlocker.acquire();
                            content = BufferUtil.hasContent(this._aggregate) ? this._aggregate : BufferUtil.EMPTY_BUFFER;
                            break block7;
                        }
                        case BLOCKED: {
                            this._softClose = true;
                            this._state = State.CLOSE;
                            blocker = this._writeBlocker.acquire();
                            this._closedCallback = Callback.combine(this._closedCallback, blocker);
                            break block7;
                        }
                        case ASYNC: 
                        case READY: {
                            this._apiState = ApiState.PENDING;
                            this._state = State.CLOSING;
                            content = BufferUtil.hasContent(this._aggregate) ? this._aggregate : BufferUtil.EMPTY_BUFFER;
                            break block7;
                        }
                        case PENDING: 
                        case UNREADY: {
                            this._softClose = true;
                            this._state = State.CLOSE;
                        }
                    }
                }
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("close() {} c={} b={}", this.stateString(), BufferUtil.toDetailString(content), blocker);
        }
        if (content == null) {
            if (blocker == null) {
                return;
            }
            b = blocker;
            try {
                b.block();
            }
            finally {
                if (b != null) {
                    b.close();
                }
            }
        }
        if (blocker == null) {
            this.channelWrite(content, true, new WriteCompleteCB());
        } else {
            try {
                b = blocker;
                try {
                    this.channelWrite(content, true, blocker);
                    b.block();
                    this.onWriteComplete(true, null);
                }
                finally {
                    if (b != null) {
                        b.close();
                    }
                }
            }
            catch (Throwable t) {
                this.onWriteComplete(true, t);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public ByteBuffer getBuffer() {
        HttpChannelState httpChannelState = this._channelState;
        synchronized (httpChannelState) {
            return this.acquireBuffer();
        }
    }

    private ByteBuffer acquireBuffer() {
        if (this._aggregate == null) {
            this._aggregate = this._channel.getByteBufferPool().acquire(this.getBufferSize(), this._interceptor.isOptimizedForDirectBuffers());
        }
        return this._aggregate;
    }

    private void releaseBuffer(Throwable failure) {
        if (this._aggregate != null) {
            ByteBufferPool bufferPool = this._channel.getConnector().getByteBufferPool();
            if (failure == null) {
                bufferPool.release(this._aggregate);
            } else {
                bufferPool.remove(this._aggregate);
            }
            this._aggregate = null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean isClosed() {
        HttpChannelState httpChannelState = this._channelState;
        synchronized (httpChannelState) {
            return this._softClose || this._state != State.OPEN;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean isAsync() {
        HttpChannelState httpChannelState = this._channelState;
        synchronized (httpChannelState) {
            switch (this._apiState) {
                case PENDING: 
                case UNREADY: 
                case ASYNC: 
                case READY: {
                    return true;
                }
            }
            return false;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void flush() throws IOException {
        ByteBuffer content = null;
        HttpChannelState httpChannelState = this._channelState;
        synchronized (httpChannelState) {
            switch (this._state) {
                case CLOSED: 
                case CLOSING: {
                    return;
                }
            }
            switch (this._apiState) {
                case BLOCKING: {
                    this._apiState = ApiState.BLOCKED;
                    content = BufferUtil.hasContent(this._aggregate) ? this._aggregate : BufferUtil.EMPTY_BUFFER;
                    break;
                }
                case PENDING: 
                case ASYNC: {
                    throw new IllegalStateException("isReady() not called: " + this.stateString());
                }
                case READY: {
                    this._apiState = ApiState.PENDING;
                    break;
                }
                case UNREADY: {
                    throw new WritePendingException();
                }
                default: {
                    throw new IllegalStateException(this.stateString());
                }
            }
        }
        if (content == null) {
            new AsyncFlush(false).iterate();
        } else {
            try {
                this.channelWrite(content, false);
                this.onWriteComplete(false, null);
            }
            catch (Throwable t) {
                this.onWriteComplete(false, t);
                throw t;
            }
        }
    }

    private void checkWritable() throws EofException {
        if (this._softClose) {
            throw new EofException("Closed");
        }
        switch (this._state) {
            case CLOSED: 
            case CLOSING: {
                throw new EofException("Closed");
            }
        }
        if (this._onError != null) {
            throw new EofException(this._onError);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        boolean async;
        boolean aggregate;
        boolean last;
        if (LOG.isDebugEnabled()) {
            LOG.debug("write(array {})", BufferUtil.toDetailString(ByteBuffer.wrap(b, off, len)));
        }
        HttpChannelState httpChannelState = this._channelState;
        synchronized (httpChannelState) {
            boolean flush;
            this.checkWritable();
            long written = this._written + (long)len;
            int space = this.maximizeAggregateSpace();
            last = this._channel.getResponse().isAllContentWritten(written);
            aggregate = len <= this._commitSize && (!last || BufferUtil.hasContent(this._aggregate) && len <= space);
            boolean bl = flush = last || !aggregate || len >= space;
            if (last && this._state == State.OPEN) {
                this._state = State.CLOSING;
            }
            switch (this._apiState) {
                case BLOCKING: {
                    this._apiState = flush ? ApiState.BLOCKED : ApiState.BLOCKING;
                    async = false;
                    break;
                }
                case ASYNC: {
                    throw new IllegalStateException("isReady() not called: " + this.stateString());
                }
                case READY: {
                    async = true;
                    this._apiState = flush ? ApiState.PENDING : ApiState.ASYNC;
                    break;
                }
                case PENDING: 
                case UNREADY: {
                    throw new WritePendingException();
                }
                default: {
                    throw new IllegalStateException(this.stateString());
                }
            }
            this._written = written;
            if (aggregate) {
                this.acquireBuffer();
                int filled = BufferUtil.fill(this._aggregate, b, off, len);
                if (!flush) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("write(array) {} aggregated !flush {}", this.stateString(), BufferUtil.toDetailString(this._aggregate));
                    }
                    return;
                }
                off += filled;
                len -= filled;
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("write(array) {} last={} agg={} flush=true async={}, len={} {}", this.stateString(), last, aggregate, async, len, BufferUtil.toDetailString(this._aggregate));
        }
        if (async) {
            new AsyncWrite(b, off, len, last).iterate();
            return;
        }
        try {
            if (BufferUtil.hasContent(this._aggregate)) {
                this.channelWrite(this._aggregate, last && len == 0);
                if (len > 0 && !last && len <= this._commitSize && len <= this.maximizeAggregateSpace()) {
                    BufferUtil.append(this._aggregate, b, off, len);
                    this.onWriteComplete(false, null);
                    return;
                }
            }
            if (len > 0) {
                ByteBuffer view = ByteBuffer.wrap(b, off, len);
                while (len > this.getBufferSize()) {
                    int p = view.position();
                    int l = p + this.getBufferSize();
                    view.limit(l);
                    this.channelWrite(view, false);
                    view.limit(p + len);
                    view.position(l);
                    len -= this.getBufferSize();
                }
                this.channelWrite(view, last);
            } else if (last) {
                this.channelWrite(BufferUtil.EMPTY_BUFFER, true);
            }
            this.onWriteComplete(last, null);
        }
        catch (Throwable t) {
            this.onWriteComplete(last, t);
            throw t;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void write(ByteBuffer buffer) throws IOException {
        boolean async;
        boolean flush;
        boolean last;
        int len = BufferUtil.length(buffer);
        HttpChannelState httpChannelState = this._channelState;
        synchronized (httpChannelState) {
            this.checkWritable();
            long written = this._written + (long)len;
            last = this._channel.getResponse().isAllContentWritten(this._written);
            boolean bl = flush = last || len > 0 || BufferUtil.hasContent(this._aggregate);
            if (last && this._state == State.OPEN) {
                this._state = State.CLOSING;
            }
            switch (this._apiState) {
                case BLOCKING: {
                    async = false;
                    this._apiState = flush ? ApiState.BLOCKED : ApiState.BLOCKING;
                    break;
                }
                case ASYNC: {
                    throw new IllegalStateException("isReady() not called: " + this.stateString());
                }
                case READY: {
                    async = true;
                    this._apiState = flush ? ApiState.PENDING : ApiState.ASYNC;
                    break;
                }
                case PENDING: 
                case UNREADY: {
                    throw new WritePendingException();
                }
                default: {
                    throw new IllegalStateException(this.stateString());
                }
            }
            this._written = written;
        }
        if (!flush) {
            return;
        }
        if (async) {
            new AsyncWrite(buffer, last).iterate();
        } else {
            try {
                if (BufferUtil.hasContent(this._aggregate)) {
                    this.channelWrite(this._aggregate, last && len == 0);
                }
                if (len > 0) {
                    this.channelWrite(buffer, last);
                } else if (last) {
                    this.channelWrite(BufferUtil.EMPTY_BUFFER, true);
                }
                this.onWriteComplete(last, null);
            }
            catch (Throwable t) {
                this.onWriteComplete(last, t);
                throw t;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void write(int b) throws IOException {
        boolean flush;
        boolean last;
        boolean async = false;
        HttpChannelState httpChannelState = this._channelState;
        synchronized (httpChannelState) {
            this.checkWritable();
            long written = this._written + 1L;
            int space = this.maximizeAggregateSpace();
            last = this._channel.getResponse().isAllContentWritten(written);
            boolean bl = flush = last || space == 1;
            if (last && this._state == State.OPEN) {
                this._state = State.CLOSING;
            }
            switch (this._apiState) {
                case BLOCKING: {
                    this._apiState = flush ? ApiState.BLOCKED : ApiState.BLOCKING;
                    break;
                }
                case ASYNC: {
                    throw new IllegalStateException("isReady() not called: " + this.stateString());
                }
                case READY: {
                    async = true;
                    this._apiState = flush ? ApiState.PENDING : ApiState.ASYNC;
                    break;
                }
                case PENDING: 
                case UNREADY: {
                    throw new WritePendingException();
                }
                default: {
                    throw new IllegalStateException(this.stateString());
                }
            }
            this._written = written;
            this.acquireBuffer();
            BufferUtil.append(this._aggregate, (byte)b);
        }
        if (!flush) {
            return;
        }
        if (async) {
            new AsyncFlush(last).iterate();
        } else {
            try {
                this.channelWrite(this._aggregate, last);
                this.onWriteComplete(last, null);
            }
            catch (Throwable t) {
                this.onWriteComplete(last, t);
                throw t;
            }
        }
    }

    @Override
    public void print(String s2) throws IOException {
        this.print(s2, false);
    }

    @Override
    public void println(String s2) throws IOException {
        this.print(s2, true);
    }

    private void print(String s2, boolean eoln) throws IOException {
        if (this.isClosed()) {
            throw new IOException("Closed");
        }
        String charset = this._channel.getResponse().getCharacterEncoding();
        CharsetEncoder encoder = _encoder.get();
        if (encoder == null || !encoder.charset().name().equalsIgnoreCase(charset)) {
            encoder = Charset.forName(charset).newEncoder();
            encoder.onMalformedInput(CodingErrorAction.REPLACE);
            encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
            _encoder.set(encoder);
        } else {
            encoder.reset();
        }
        CharBuffer in = CharBuffer.wrap(s2);
        CharBuffer crlf = eoln ? CharBuffer.wrap("\r\n") : null;
        ByteBuffer out = this.getHttpChannel().getByteBufferPool().acquire((int)(1.0f + (float)(s2.length() + 2) * encoder.averageBytesPerChar()), false);
        BufferUtil.flipToFill(out);
        while (true) {
            CoderResult result;
            if (in.hasRemaining()) {
                result = encoder.encode(in, out, crlf == null);
                if (result.isUnderflow()) {
                    if (crlf != null) continue;
                    break;
                }
            } else {
                if (crlf == null || !crlf.hasRemaining()) break;
                result = encoder.encode(crlf, out, true);
                if (result.isUnderflow()) {
                    if (encoder.flush(out).isUnderflow()) break;
                    result.throwException();
                    break;
                }
            }
            if (result.isOverflow()) {
                BufferUtil.flipToFlush(out, 0);
                ByteBuffer bigger = BufferUtil.ensureCapacity(out, out.capacity() + s2.length() + 2);
                this.getHttpChannel().getByteBufferPool().release(out);
                BufferUtil.flipToFill(bigger);
                out = bigger;
                continue;
            }
            result.throwException();
        }
        BufferUtil.flipToFlush(out, 0);
        this.write(out.array(), out.arrayOffset(), out.remaining());
        this.getHttpChannel().getByteBufferPool().release(out);
    }

    @Override
    public void println(boolean b) throws IOException {
        this.println(lStrings.getString(b ? "value.true" : "value.false"));
    }

    @Override
    public void println(char c) throws IOException {
        this.println(String.valueOf(c));
    }

    @Override
    public void println(int i) throws IOException {
        this.println(String.valueOf(i));
    }

    @Override
    public void println(long l) throws IOException {
        this.println(String.valueOf(l));
    }

    @Override
    public void println(float f) throws IOException {
        this.println(String.valueOf(f));
    }

    @Override
    public void println(double d) throws IOException {
        this.println(String.valueOf(d));
    }

    public void sendContent(ByteBuffer content) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("sendContent({})", BufferUtil.toDetailString(content));
        }
        this._written += (long)content.remaining();
        this.channelWrite(content, true);
    }

    public void sendContent(InputStream in) throws IOException {
        try (SharedBlockingCallback.Blocker blocker = this._writeBlocker.acquire();){
            new InputStreamWritingCB(in, blocker).iterate();
            blocker.block();
        }
    }

    public void sendContent(ReadableByteChannel in) throws IOException {
        try (SharedBlockingCallback.Blocker blocker = this._writeBlocker.acquire();){
            new ReadableByteChannelWritingCB(in, blocker).iterate();
            blocker.block();
        }
    }

    public void sendContent(HttpContent content) throws IOException {
        try (SharedBlockingCallback.Blocker blocker = this._writeBlocker.acquire();){
            this.sendContent(content, (Callback)blocker);
            blocker.block();
        }
    }

    public void sendContent(ByteBuffer content, Callback callback) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("sendContent(buffer={},{})", BufferUtil.toDetailString(content), callback);
        }
        if (this.prepareSendContent(content.remaining(), callback)) {
            this.channelWrite(content, true, new Callback.Nested(callback){

                @Override
                public void succeeded() {
                    HttpOutput.this.onWriteComplete(true, null);
                    super.succeeded();
                }

                @Override
                public void failed(Throwable x) {
                    HttpOutput.this.onWriteComplete(true, x);
                    super.failed(x);
                }
            });
        }
    }

    public void sendContent(InputStream in, Callback callback) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("sendContent(stream={},{})", in, callback);
        }
        if (this.prepareSendContent(0, callback)) {
            new InputStreamWritingCB(in, callback).iterate();
        }
    }

    public void sendContent(ReadableByteChannel in, Callback callback) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("sendContent(channel={},{})", in, callback);
        }
        if (this.prepareSendContent(0, callback)) {
            new ReadableByteChannelWritingCB(in, callback).iterate();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean prepareSendContent(int len, Callback callback) {
        HttpChannelState httpChannelState = this._channelState;
        synchronized (httpChannelState) {
            if (BufferUtil.hasContent(this._aggregate)) {
                callback.failed(new IOException("cannot sendContent() after write()"));
                return false;
            }
            if (this._channel.isCommitted()) {
                callback.failed(new IOException("cannot sendContent(), output already committed"));
                return false;
            }
            switch (this._state) {
                case CLOSED: 
                case CLOSING: {
                    callback.failed(new EofException("Closed"));
                    return false;
                }
            }
            this._state = State.CLOSING;
            if (this._onError != null) {
                callback.failed(this._onError);
                return false;
            }
            if (this._apiState != ApiState.BLOCKING) {
                throw new IllegalStateException(this.stateString());
            }
            this._apiState = ApiState.PENDING;
            if (len > 0) {
                this._written += (long)len;
            }
            return true;
        }
    }

    public void sendContent(HttpContent httpContent, Callback callback) {
        ByteBuffer buffer;
        if (LOG.isDebugEnabled()) {
            LOG.debug("sendContent(http={},{})", httpContent, callback);
        }
        ByteBuffer byteBuffer = buffer = this._channel.useDirectBuffers() ? httpContent.getDirectBuffer() : null;
        if (buffer == null) {
            buffer = httpContent.getIndirectBuffer();
        }
        if (buffer != null) {
            this.sendContent(buffer, callback);
            return;
        }
        ReadableByteChannel rbc = null;
        try {
            rbc = httpContent.getReadableByteChannel();
        }
        catch (Throwable x) {
            LOG.debug(x);
        }
        if (rbc != null) {
            this.sendContent(rbc, callback);
            return;
        }
        InputStream in = null;
        try {
            in = httpContent.getInputStream();
        }
        catch (Throwable x) {
            LOG.debug(x);
        }
        if (in != null) {
            this.sendContent(in, callback);
            return;
        }
        IllegalArgumentException cause = new IllegalArgumentException("unknown content for " + httpContent);
        this._channel.abort(cause);
        callback.failed(cause);
    }

    public int getBufferSize() {
        return this._bufferSize;
    }

    public void setBufferSize(int size) {
        this._bufferSize = size;
        this._commitSize = size;
    }

    public void onFlushed(long bytes) throws IOException {
        if (this._firstByteTimeStamp == -1L || this._firstByteTimeStamp == Long.MAX_VALUE) {
            return;
        }
        long minDataRate = this.getHttpChannel().getHttpConfiguration().getMinResponseDataRate();
        this._flushed += bytes;
        long elapsed = System.nanoTime() - this._firstByteTimeStamp;
        long minFlushed = minDataRate * TimeUnit.NANOSECONDS.toMillis(elapsed) / TimeUnit.SECONDS.toMillis(1L);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Flushed bytes min/actual {}/{}", minFlushed, this._flushed);
        }
        if (this._flushed < minFlushed) {
            IOException ioe = new IOException(String.format("Response content data rate < %d B/s", minDataRate));
            this._channel.abort(ioe);
            throw ioe;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void recycle() {
        HttpChannelState httpChannelState = this._channelState;
        synchronized (httpChannelState) {
            this._state = State.OPEN;
            this._apiState = ApiState.BLOCKING;
            this._softClose = true;
            this._interceptor = this._channel;
            HttpConfiguration config = this._channel.getHttpConfiguration();
            this._bufferSize = config.getOutputBufferSize();
            this._commitSize = config.getOutputAggregationSize();
            if (this._commitSize > this._bufferSize) {
                this._commitSize = this._bufferSize;
            }
            this.releaseBuffer(null);
            this._written = 0L;
            this._writeListener = null;
            this._onError = null;
            this._firstByteTimeStamp = -1L;
            this._flushed = 0L;
            this._closedCallback = null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void resetBuffer() {
        HttpChannelState httpChannelState = this._channelState;
        synchronized (httpChannelState) {
            this._interceptor.resetBuffer();
            if (BufferUtil.hasContent(this._aggregate)) {
                BufferUtil.clear(this._aggregate);
            }
            this._written = 0L;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setWriteListener(WriteListener writeListener) {
        boolean wake;
        if (!this._channel.getState().isAsync()) {
            throw new IllegalStateException("!ASYNC: " + this.stateString());
        }
        HttpChannelState httpChannelState = this._channelState;
        synchronized (httpChannelState) {
            if (this._apiState != ApiState.BLOCKING) {
                throw new IllegalStateException("!OPEN" + this.stateString());
            }
            this._apiState = ApiState.READY;
            this._writeListener = writeListener;
            wake = this._channel.getState().onWritePossible();
        }
        if (wake) {
            this._channel.execute(this._channel);
        }
    }

    @Override
    public boolean isReady() {
        HttpChannelState httpChannelState = this._channelState;
        synchronized (httpChannelState) {
            switch (this._apiState) {
                case BLOCKING: 
                case READY: {
                    return true;
                }
                case ASYNC: {
                    this._apiState = ApiState.READY;
                    return true;
                }
                case PENDING: {
                    this._apiState = ApiState.UNREADY;
                    return false;
                }
                case BLOCKED: 
                case UNREADY: {
                    return false;
                }
            }
            throw new IllegalStateException(this.stateString());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void run() {
        Throwable error = null;
        HttpChannelState httpChannelState = this._channelState;
        synchronized (httpChannelState) {
            if (this._onError != null) {
                error = this._onError;
                this._onError = null;
            }
        }
        try {
            if (error == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("onWritePossible", new Object[0]);
                }
                this._writeListener.onWritePossible();
                return;
            }
        }
        catch (Throwable t) {
            error = t;
        }
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("onError", error);
            }
            this._writeListener.onError(error);
        }
        catch (Throwable t) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(t);
            }
        }
        finally {
            IO.close(this);
        }
    }

    private String stateString() {
        return String.format("s=%s,api=%s,sc=%b,e=%s", new Object[]{this._state, this._apiState, this._softClose, this._onError});
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String toString() {
        HttpChannelState httpChannelState = this._channelState;
        synchronized (httpChannelState) {
            return String.format("%s@%x{%s}", this.getClass().getSimpleName(), this.hashCode(), this.stateString());
        }
    }

    private class WriteCompleteCB
    implements Callback {
        private WriteCompleteCB() {
        }

        @Override
        public void succeeded() {
            HttpOutput.this.onWriteComplete(true, null);
        }

        @Override
        public void failed(Throwable x) {
            HttpOutput.this.onWriteComplete(true, x);
        }
    }

    private static class WriteBlocker
    extends SharedBlockingCallback {
        private final HttpChannel _channel;

        private WriteBlocker(HttpChannel channel) {
            this._channel = channel;
        }

        @Override
        protected long getIdleTimeout() {
            long blockingTimeout = this._channel.getHttpConfiguration().getBlockingTimeout();
            if (blockingTimeout == 0L) {
                return this._channel.getIdleTimeout();
            }
            return blockingTimeout;
        }
    }

    private class ReadableByteChannelWritingCB
    extends NestedChannelWriteCB {
        private final ReadableByteChannel _in;
        private final ByteBuffer _buffer;
        private boolean _eof;
        private boolean _closed;

        ReadableByteChannelWritingCB(ReadableByteChannel in, Callback callback) {
            super(callback, true);
            this._in = in;
            this._buffer = HttpOutput.this._channel.getByteBufferPool().acquire(HttpOutput.this.getBufferSize(), HttpOutput.this._channel.useDirectBuffers());
        }

        @Override
        protected IteratingCallback.Action process() throws Exception {
            if (this._eof) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("EOF of {}", this);
                }
                if (!this._closed) {
                    this._closed = true;
                    HttpOutput.this._channel.getByteBufferPool().release(this._buffer);
                    IO.close(this._in);
                }
                return IteratingCallback.Action.SUCCEEDED;
            }
            BufferUtil.clearToFill(this._buffer);
            while (this._buffer.hasRemaining() && !this._eof) {
                this._eof = this._in.read(this._buffer) < 0;
            }
            BufferUtil.flipToFlush(this._buffer, 0);
            HttpOutput.this._written += this._buffer.remaining();
            HttpOutput.this.channelWrite(this._buffer, this._eof, this);
            return IteratingCallback.Action.SCHEDULED;
        }

        @Override
        public void onCompleteFailure(Throwable x) {
            HttpOutput.this._channel.getByteBufferPool().release(this._buffer);
            IO.close(this._in);
            super.onCompleteFailure(x);
        }
    }

    private class InputStreamWritingCB
    extends NestedChannelWriteCB {
        private final InputStream _in;
        private final ByteBuffer _buffer;
        private boolean _eof;
        private boolean _closed;

        InputStreamWritingCB(InputStream in, Callback callback) {
            super(callback, true);
            this._in = in;
            this._buffer = HttpOutput.this._channel.getByteBufferPool().acquire(HttpOutput.this.getBufferSize(), false);
        }

        @Override
        protected IteratingCallback.Action process() throws Exception {
            if (this._eof) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("EOF of {}", this);
                }
                if (!this._closed) {
                    this._closed = true;
                    HttpOutput.this._channel.getByteBufferPool().release(this._buffer);
                    IO.close(this._in);
                }
                return IteratingCallback.Action.SUCCEEDED;
            }
            int len = 0;
            while (len < this._buffer.capacity() && !this._eof) {
                int r = this._in.read(this._buffer.array(), this._buffer.arrayOffset() + len, this._buffer.capacity() - len);
                if (r < 0) {
                    this._eof = true;
                    continue;
                }
                len += r;
            }
            this._buffer.position(0);
            this._buffer.limit(len);
            HttpOutput.this._written += len;
            HttpOutput.this.channelWrite(this._buffer, this._eof, this);
            return IteratingCallback.Action.SCHEDULED;
        }

        @Override
        public void onCompleteFailure(Throwable x) {
            try {
                HttpOutput.this._channel.getByteBufferPool().release(this._buffer);
            }
            finally {
                super.onCompleteFailure(x);
            }
        }
    }

    private class AsyncWrite
    extends ChannelWriteCB {
        private final ByteBuffer _buffer;
        private final ByteBuffer _slice;
        private final int _len;
        private boolean _completed;

        AsyncWrite(byte[] b, int off, int len, boolean last) {
            super(last);
            this._buffer = ByteBuffer.wrap(b, off, len);
            this._len = len;
            this._slice = this._len < HttpOutput.this.getBufferSize() ? null : this._buffer.duplicate();
        }

        AsyncWrite(ByteBuffer buffer, boolean last) {
            super(last);
            this._buffer = buffer;
            this._len = buffer.remaining();
            this._slice = this._buffer.isDirect() || this._len < HttpOutput.this.getBufferSize() ? null : this._buffer.duplicate();
        }

        @Override
        protected IteratingCallback.Action process() throws Exception {
            if (BufferUtil.hasContent(HttpOutput.this._aggregate)) {
                this._completed = this._len == 0;
                HttpOutput.this.channelWrite(HttpOutput.this._aggregate, this._last && this._completed, this);
                return IteratingCallback.Action.SCHEDULED;
            }
            if (!this._last && HttpOutput.this._aggregate != null && this._len < HttpOutput.this.maximizeAggregateSpace() && this._len < HttpOutput.this._commitSize) {
                int position = BufferUtil.flipToFill(HttpOutput.this._aggregate);
                BufferUtil.put(this._buffer, HttpOutput.this._aggregate);
                BufferUtil.flipToFlush(HttpOutput.this._aggregate, position);
                return IteratingCallback.Action.SUCCEEDED;
            }
            if (this._buffer.hasRemaining()) {
                if (this._slice == null) {
                    this._completed = true;
                    HttpOutput.this.channelWrite(this._buffer, this._last, this);
                    return IteratingCallback.Action.SCHEDULED;
                }
                int p = this._buffer.position();
                int l = Math.min(HttpOutput.this.getBufferSize(), this._buffer.remaining());
                int pl = p + l;
                this._slice.limit(pl);
                this._buffer.position(pl);
                this._slice.position(p);
                this._completed = !this._buffer.hasRemaining();
                HttpOutput.this.channelWrite(this._slice, this._last && this._completed, this);
                return IteratingCallback.Action.SCHEDULED;
            }
            if (this._last && !this._completed) {
                this._completed = true;
                HttpOutput.this.channelWrite(BufferUtil.EMPTY_BUFFER, true, this);
                return IteratingCallback.Action.SCHEDULED;
            }
            if (LOG.isDebugEnabled() && this._completed) {
                LOG.debug("EOF of {}", this);
            }
            return IteratingCallback.Action.SUCCEEDED;
        }
    }

    private class AsyncFlush
    extends ChannelWriteCB {
        volatile boolean _flushed;

        AsyncFlush(boolean last) {
            super(last);
        }

        @Override
        protected IteratingCallback.Action process() throws Exception {
            if (BufferUtil.hasContent(HttpOutput.this._aggregate)) {
                this._flushed = true;
                HttpOutput.this.channelWrite(HttpOutput.this._aggregate, false, this);
                return IteratingCallback.Action.SCHEDULED;
            }
            if (!this._flushed) {
                this._flushed = true;
                HttpOutput.this.channelWrite(BufferUtil.EMPTY_BUFFER, false, this);
                return IteratingCallback.Action.SCHEDULED;
            }
            return IteratingCallback.Action.SUCCEEDED;
        }
    }

    private abstract class NestedChannelWriteCB
    extends ChannelWriteCB {
        final Callback _callback;

        NestedChannelWriteCB(Callback callback, boolean last) {
            super(last);
            this._callback = callback;
        }

        @Override
        protected void onCompleteSuccess() {
            try {
                super.onCompleteSuccess();
            }
            finally {
                this._callback.succeeded();
            }
        }

        @Override
        public void onCompleteFailure(Throwable e) {
            try {
                super.onCompleteFailure(e);
            }
            catch (Throwable t) {
                if (t != e) {
                    e.addSuppressed(t);
                }
            }
            finally {
                this._callback.failed(e);
            }
        }
    }

    private abstract class ChannelWriteCB
    extends IteratingCallback {
        final boolean _last;

        ChannelWriteCB(boolean last) {
            this._last = last;
        }

        @Override
        public Invocable.InvocationType getInvocationType() {
            return Invocable.InvocationType.NON_BLOCKING;
        }

        @Override
        protected void onCompleteSuccess() {
            HttpOutput.this.onWriteComplete(this._last, null);
        }

        @Override
        public void onCompleteFailure(Throwable e) {
            HttpOutput.this.onWriteComplete(this._last, e);
        }
    }

    public static interface Interceptor {
        public void write(ByteBuffer var1, boolean var2, Callback var3);

        public Interceptor getNextInterceptor();

        public boolean isOptimizedForDirectBuffers();

        default public void resetBuffer() throws IllegalStateException {
            Interceptor next = this.getNextInterceptor();
            if (next != null) {
                next.resetBuffer();
            }
        }
    }

    static enum ApiState {
        BLOCKING,
        BLOCKED,
        ASYNC,
        READY,
        PENDING,
        UNREADY;

    }

    static enum State {
        OPEN,
        CLOSE,
        CLOSING,
        CLOSED;

    }
}

