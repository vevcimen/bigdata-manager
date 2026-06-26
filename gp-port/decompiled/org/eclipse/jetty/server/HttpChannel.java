/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.DispatcherType;
import javax.servlet.ServletException;
import org.eclipse.jetty.http.BadMessageException;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpGenerator;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpHeaderValue;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http.MetaData;
import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.io.ChannelEndPoint;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.io.QuietException;
import org.eclipse.jetty.server.AbstractConnector;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpChannelState;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnection;
import org.eclipse.jetty.server.HttpInput;
import org.eclipse.jetty.server.HttpOutput;
import org.eclipse.jetty.server.HttpTransport;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.RequestLogCollection;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.util.BufferUtil;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.SharedBlockingCallback;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.thread.Scheduler;

public class HttpChannel
implements Runnable,
HttpOutput.Interceptor {
    public static Listener NOOP_LISTENER = new Listener(){};
    private static final Logger LOG = Log.getLogger(HttpChannel.class);
    private final AtomicLong _requests = new AtomicLong();
    private final Connector _connector;
    private final Executor _executor;
    private final HttpConfiguration _configuration;
    private final EndPoint _endPoint;
    private final HttpTransport _transport;
    private final HttpChannelState _state;
    private final Request _request;
    private final Response _response;
    private final Listener _combinedListener;
    @Deprecated
    private final List<Listener> _transientListeners = new ArrayList<Listener>();
    private HttpFields _trailers;
    private final Supplier<HttpFields> _trailerSupplier = () -> this._trailers;
    private MetaData.Response _committedMetaData;
    private RequestLog _requestLog;
    private long _oldIdleTimeout;
    private long _written;

    public HttpChannel(Connector connector, HttpConfiguration configuration, EndPoint endPoint, HttpTransport transport) {
        this._connector = connector;
        this._configuration = configuration;
        this._endPoint = endPoint;
        this._transport = transport;
        this._state = new HttpChannelState(this);
        this._request = new Request(this, this.newHttpInput(this._state));
        this._response = new Response(this, this.newHttpOutput());
        this._executor = connector.getServer().getThreadPool();
        this._requestLog = connector.getServer().getRequestLog();
        Listener listener = this._combinedListener = connector instanceof AbstractConnector ? ((AbstractConnector)connector).getHttpChannelListeners() : NOOP_LISTENER;
        if (LOG.isDebugEnabled()) {
            LOG.debug("new {} -> {},{},{}", this, this._endPoint, this._endPoint == null ? null : this._endPoint.getConnection(), this._state);
        }
    }

    public boolean isSendError() {
        return this._state.isSendError();
    }

    protected HttpInput newHttpInput(HttpChannelState state) {
        return new HttpInput(state);
    }

    protected HttpOutput newHttpOutput() {
        return new HttpOutput(this);
    }

    public HttpChannelState getState() {
        return this._state;
    }

    @Deprecated
    public boolean addListener(Listener listener) {
        return this._transientListeners.add(listener);
    }

    @Deprecated
    public boolean removeListener(Listener listener) {
        return this._transientListeners.remove(listener);
    }

    @Deprecated
    public List<Listener> getTransientListeners() {
        return this._transientListeners;
    }

    public long getBytesWritten() {
        return this._written;
    }

    public long getRequests() {
        return this._requests.get();
    }

    public Connector getConnector() {
        return this._connector;
    }

    public HttpTransport getHttpTransport() {
        return this._transport;
    }

    public RequestLog getRequestLog() {
        return this._requestLog;
    }

    public void setRequestLog(RequestLog requestLog) {
        this._requestLog = requestLog;
    }

    public void addRequestLog(RequestLog requestLog) {
        if (this._requestLog == null) {
            this._requestLog = requestLog;
        } else if (this._requestLog instanceof RequestLogCollection) {
            ((RequestLogCollection)this._requestLog).add(requestLog);
        } else {
            this._requestLog = new RequestLogCollection(this._requestLog, requestLog);
        }
    }

    public MetaData.Response getCommittedMetaData() {
        return this._committedMetaData;
    }

    public long getIdleTimeout() {
        return this._endPoint.getIdleTimeout();
    }

    public void setIdleTimeout(long timeoutMs) {
        this._endPoint.setIdleTimeout(timeoutMs);
    }

    public ByteBufferPool getByteBufferPool() {
        return this._connector.getByteBufferPool();
    }

    public HttpConfiguration getHttpConfiguration() {
        return this._configuration;
    }

    @Override
    public boolean isOptimizedForDirectBuffers() {
        return this.getHttpTransport().isOptimizedForDirectBuffers();
    }

    public Server getServer() {
        return this._connector.getServer();
    }

    public Request getRequest() {
        return this._request;
    }

    public Response getResponse() {
        return this._response;
    }

    public Connection getConnection() {
        return this._endPoint.getConnection();
    }

    public EndPoint getEndPoint() {
        return this._endPoint;
    }

    public InetSocketAddress getLocalAddress() {
        return this._endPoint.getLocalAddress();
    }

    public InetSocketAddress getRemoteAddress() {
        return this._endPoint.getRemoteAddress();
    }

    public void continue100(int available) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void recycle() {
        this._request.recycle();
        this._response.recycle();
        this._committedMetaData = null;
        this._requestLog = this._connector == null ? null : this._connector.getServer().getRequestLog();
        this._written = 0L;
        this._trailers = null;
        this._oldIdleTimeout = 0L;
        this._transientListeners.clear();
    }

    public void onAsyncWaitForContent() {
    }

    public void onBlockWaitForContent() {
    }

    public void onBlockWaitForContentFailure(Throwable failure) {
        this.getRequest().getHttpInput().failed(failure);
    }

    @Override
    public void run() {
        this.handle();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean handle() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("handle {} {} ", this._request.getHttpURI(), this);
        }
        HttpChannelState.Action action = this._state.handling();
        block24: while (!this.getServer().isStopped()) {
            try {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("action {} {}", new Object[]{action, this});
                }
                switch (action) {
                    case TERMINATED: {
                        this.onCompleted();
                        break block24;
                    }
                    case WAIT: {
                        break block24;
                    }
                    case DISPATCH: {
                        if (!this._request.hasMetaData()) {
                            throw new IllegalStateException("state=" + this._state);
                        }
                        this.dispatch(DispatcherType.REQUEST, () -> {
                            for (HttpConfiguration.Customizer customizer : this._configuration.getCustomizers()) {
                                customizer.customize(this.getConnector(), this._configuration, this._request);
                                if (!this._request.isHandled()) continue;
                                return;
                            }
                            this.getServer().handle(this);
                        });
                        break;
                    }
                    case ASYNC_DISPATCH: {
                        this.dispatch(DispatcherType.ASYNC, () -> this.getServer().handleAsync(this));
                        break;
                    }
                    case ASYNC_TIMEOUT: {
                        this._state.onTimeout();
                        break;
                    }
                    case SEND_ERROR: {
                        try {
                            this._response.resetContent();
                            Integer code = (Integer)this._request.getAttribute("javax.servlet.error.status_code");
                            if (code == null) {
                                code = 500;
                            }
                            this._response.setStatus(code);
                            this.ensureConsumeAllOrNotPersistent();
                            ContextHandler.Context context = (ContextHandler.Context)this._request.getAttribute("org.eclipse.jetty.server.error_context");
                            ErrorHandler errorHandler = ErrorHandler.getErrorHandler(this.getServer(), context == null ? null : context.getContextHandler());
                            if (HttpStatus.hasNoBody(this._response.getStatus()) || errorHandler == null || !errorHandler.errorPageForMethod(this._request.getMethod())) {
                                this.sendResponseAndComplete();
                            } else {
                                this.dispatch(DispatcherType.ERROR, () -> {
                                    errorHandler.handle(null, this._request, this._request, this._response);
                                    this._request.setHandled(true);
                                });
                            }
                            break;
                        }
                        catch (Throwable x) {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Could not perform ERROR dispatch, aborting", x);
                            }
                            if (this._state.isResponseCommitted()) {
                                this.abort(x);
                            } else {
                                try {
                                    this._response.resetContent();
                                    this.sendResponseAndComplete();
                                }
                                catch (Throwable t) {
                                    if (x != t) {
                                        x.addSuppressed(t);
                                    }
                                    this.abort(x);
                                }
                            }
                            break;
                        }
                        finally {
                            this._request.removeAttribute("org.eclipse.jetty.server.error_context");
                        }
                    }
                    case ASYNC_ERROR: {
                        throw this._state.getAsyncContextEvent().getThrowable();
                    }
                    case READ_REGISTER: {
                        this.onAsyncWaitForContent();
                        break;
                    }
                    case READ_PRODUCE: {
                        this._request.getHttpInput().asyncReadProduce();
                        break;
                    }
                    case READ_CALLBACK: {
                        ContextHandler handler = this._state.getContextHandler();
                        if (handler != null) {
                            handler.handle(this._request, this._request.getHttpInput());
                            break;
                        }
                        this._request.getHttpInput().run();
                        break;
                    }
                    case WRITE_CALLBACK: {
                        ContextHandler handler = this._state.getContextHandler();
                        if (handler != null) {
                            handler.handle(this._request, this._response.getHttpOutput());
                            break;
                        }
                        this._response.getHttpOutput().run();
                        break;
                    }
                    case COMPLETE: {
                        if (!this._response.isCommitted()) {
                            if (!this._request.isHandled() && !this._response.getHttpOutput().isClosed()) {
                                this._response.sendError(404);
                                break;
                            }
                            if (this._response.getStatus() >= 200) {
                                this.ensureConsumeAllOrNotPersistent();
                            }
                        }
                        if (!this._request.isHead() && this._response.getStatus() != 304 && !this._response.isContentComplete(this._response.getHttpOutput().getWritten())) {
                            if (this.isCommitted()) {
                                this.abort(new IOException("insufficient content written"));
                            } else {
                                this._response.sendError(500, "insufficient content written");
                                break;
                            }
                        }
                        this._response.completeOutput(Callback.from(() -> this._state.completed(null), this._state::completed));
                        break;
                    }
                    default: {
                        throw new IllegalStateException(this.toString());
                    }
                }
            }
            catch (Throwable failure) {
                if ("org.eclipse.jetty.continuation.ContinuationThrowable".equals(failure.getClass().getName())) {
                    LOG.ignore(failure);
                }
                this.handleException(failure);
            }
            action = this._state.unhandle();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("!handle {} {}", new Object[]{action, this});
        }
        boolean suspended = action == HttpChannelState.Action.WAIT;
        return !suspended;
    }

    public void ensureConsumeAllOrNotPersistent() {
        switch (this._request.getHttpVersion()) {
            case HTTP_1_0: {
                if (this._request.getHttpInput().consumeAll()) {
                    return;
                }
                this._response.getHttpFields().computeField(HttpHeader.CONNECTION, (h2, fields) -> {
                    if (fields == null || fields.isEmpty()) {
                        return null;
                    }
                    String v = fields.stream().flatMap(field -> Stream.of(field.getValues()).filter(s2 -> !HttpHeaderValue.KEEP_ALIVE.is((String)s2))).collect(Collectors.joining(", "));
                    if (StringUtil.isEmpty(v)) {
                        return null;
                    }
                    return new HttpField(HttpHeader.CONNECTION, v);
                });
                break;
            }
            case HTTP_1_1: {
                if (this._request.getHttpInput().consumeAll()) {
                    return;
                }
                this._response.getHttpFields().computeField(HttpHeader.CONNECTION, (h2, fields) -> {
                    if (fields == null || fields.isEmpty()) {
                        return HttpConnection.CONNECTION_CLOSE;
                    }
                    if (fields.stream().anyMatch(f -> f.contains(HttpHeaderValue.CLOSE.asString()))) {
                        HttpField f2;
                        if (fields.size() == 1 && HttpConnection.CONNECTION_CLOSE.equals(f2 = (HttpField)fields.get(0))) {
                            return f2;
                        }
                        return new HttpField(HttpHeader.CONNECTION, fields.stream().flatMap(field -> Stream.of(field.getValues()).filter(s2 -> !HttpHeaderValue.KEEP_ALIVE.is((String)s2))).collect(Collectors.joining(", ")));
                    }
                    return new HttpField(HttpHeader.CONNECTION, Stream.concat(fields.stream().flatMap(field -> Stream.of(field.getValues()).filter(s2 -> !HttpHeaderValue.KEEP_ALIVE.is((String)s2))), Stream.of(HttpHeaderValue.CLOSE.asString())).collect(Collectors.joining(", ")));
                });
                break;
            }
        }
    }

    private void dispatch(DispatcherType type, Dispatchable dispatchable) throws IOException, ServletException {
        try {
            this._request.setHandled(false);
            this._response.reopen();
            this._request.setDispatcherType(type);
            this._combinedListener.onBeforeDispatch(this._request);
            dispatchable.dispatch();
        }
        catch (Throwable x) {
            this._combinedListener.onDispatchFailure(this._request, x);
            throw x;
        }
        finally {
            this._combinedListener.onAfterDispatch(this._request);
            this._request.setDispatcherType(null);
        }
    }

    protected void handleException(Throwable failure) {
        Throwable quiet = this.unwrap(failure, QuietException.class);
        Throwable noStack = this.unwrap(failure, BadMessageException.class, IOException.class, TimeoutException.class);
        if (quiet != null || !this.getServer().isRunning()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(this._request.getRequestURI(), failure);
            }
        } else if (noStack != null) {
            if (LOG.isDebugEnabled()) {
                LOG.warn("handleException " + this._request.getRequestURI(), failure);
            } else {
                LOG.warn("handleException {} {}", this._request.getRequestURI(), noStack.toString());
            }
        } else {
            LOG.warn(this._request.getRequestURI(), failure);
        }
        if (this.isCommitted()) {
            this.abort(failure);
        } else {
            this._state.onError(failure);
        }
    }

    protected Throwable unwrap(Throwable failure, Class<?> ... targets) {
        while (failure != null) {
            for (Class<?> x : targets) {
                if (!x.isInstance(failure)) continue;
                return failure;
            }
            failure = failure.getCause();
        }
        return null;
    }

    public void sendResponseAndComplete() {
        try {
            this._request.setHandled(true);
            this._state.completing();
            this.sendResponse(null, this._response.getHttpOutput().getBuffer(), true, Callback.from(() -> this._state.completed(null), this._state::completed));
        }
        catch (Throwable x) {
            this.abort(x);
        }
    }

    public boolean isExpecting100Continue() {
        return false;
    }

    public boolean isExpecting102Processing() {
        return false;
    }

    public String toString() {
        long timeStamp = this._request.getTimeStamp();
        return String.format("%s@%x{s=%s,r=%s,c=%b/%b,a=%s,uri=%s,age=%d}", new Object[]{this.getClass().getSimpleName(), this.hashCode(), this._state, this._requests, this.isRequestCompleted(), this.isResponseCompleted(), this._state.getState(), this._request.getHttpURI(), timeStamp == 0L ? 0L : System.currentTimeMillis() - timeStamp});
    }

    public void onRequest(MetaData.Request request) {
        this._requests.incrementAndGet();
        this._request.setTimeStamp(System.currentTimeMillis());
        HttpFields fields = this._response.getHttpFields();
        if (this._configuration.getSendDateHeader() && !fields.contains(HttpHeader.DATE)) {
            fields.put(this._connector.getServer().getDateField());
        }
        long idleTO = this._configuration.getIdleTimeout();
        this._oldIdleTimeout = this.getIdleTimeout();
        if (idleTO >= 0L && this._oldIdleTimeout != idleTO) {
            this.setIdleTimeout(idleTO);
        }
        request.setTrailerSupplier(this._trailerSupplier);
        this._request.setMetaData(request);
        this._request.setSecure(HttpScheme.HTTPS.is(request.getURI().getScheme()));
        this._combinedListener.onRequestBegin(this._request);
        if (LOG.isDebugEnabled()) {
            LOG.debug("REQUEST for {} on {}{}{} {} {}{}{}", new Object[]{request.getURIString(), this, System.lineSeparator(), request.getMethod(), request.getURIString(), request.getHttpVersion(), System.lineSeparator(), request.getFields()});
        }
    }

    public boolean onContent(HttpInput.Content content) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("onContent {} {}", this, content);
        }
        this._combinedListener.onRequestContent(this._request, content.getByteBuffer());
        return this._request.getHttpInput().addContent(content);
    }

    public boolean onContentComplete() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("onContentComplete {}", this);
        }
        this._combinedListener.onRequestContentEnd(this._request);
        return false;
    }

    public void onTrailers(HttpFields trailers) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("onTrailers {} {}", this, trailers);
        }
        this._trailers = trailers;
        this._combinedListener.onRequestTrailers(this._request);
    }

    public boolean onRequestComplete() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("onRequestComplete {}", this);
        }
        boolean result = this._request.getHttpInput().eof();
        this._combinedListener.onRequestEnd(this._request);
        return result;
    }

    public void onCompleted() {
        long idleTO;
        if (LOG.isDebugEnabled()) {
            LOG.debug("onCompleted for {} written={}", this.getRequest().getRequestURI(), this.getBytesWritten());
        }
        if (this._requestLog != null) {
            this._requestLog.log(this._request, this._response);
        }
        if ((idleTO = this._configuration.getIdleTimeout()) >= 0L && this.getIdleTimeout() != this._oldIdleTimeout) {
            this.setIdleTimeout(this._oldIdleTimeout);
        }
        this._request.onCompleted();
        this._combinedListener.onComplete(this._request);
        this._transport.onCompleted();
    }

    public boolean onEarlyEOF() {
        return this._request.getHttpInput().earlyEOF();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void onBadMessage(BadMessageException failure) {
        HttpChannelState.Action action;
        int status = failure.getCode();
        String reason = failure.getReason();
        if (status < 400 || status > 599) {
            failure = new BadMessageException(400, reason, failure);
        }
        this._combinedListener.onRequestFailure(this._request, failure);
        try {
            action = this._state.handling();
        }
        catch (Throwable e) {
            this.abort(e);
            throw failure;
        }
        try {
            if (action == HttpChannelState.Action.DISPATCH) {
                ByteBuffer content = null;
                HttpFields fields = new HttpFields();
                ErrorHandler handler = this.getServer().getBean(ErrorHandler.class);
                if (handler != null) {
                    content = handler.badMessageError(status, reason, fields);
                }
                this.sendResponse(new MetaData.Response(HttpVersion.HTTP_1_1, status, reason, fields, BufferUtil.length(content)), content, true);
            }
        }
        catch (IOException e) {
            LOG.debug(e);
        }
        finally {
            try {
                this.onCompleted();
            }
            catch (Throwable e) {
                LOG.debug(e);
                this.abort(e);
            }
        }
    }

    public boolean sendResponse(MetaData.Response info, ByteBuffer content, boolean complete, Callback callback) {
        boolean committing = this._state.commitResponse();
        if (LOG.isDebugEnabled()) {
            LOG.debug("sendResponse info={} content={} complete={} committing={} callback={}", info, BufferUtil.toDetailString(content), complete, committing, callback);
        }
        if (committing) {
            if (info == null) {
                info = this._response.newResponseMetaData();
            }
            this.commit(info);
            this._combinedListener.onResponseBegin(this._request);
            this._request.onResponseCommit();
            int status = info.getStatus();
            SendCallback committed = status < 200 && status >= 100 ? new Send100Callback(callback) : new SendCallback(callback, content, true, complete);
            this._transport.send(info, this._request.isHead(), content, complete, committed);
        } else if (info == null) {
            this._transport.send(null, this._request.isHead(), content, complete, new SendCallback(callback, content, false, complete));
        } else {
            callback.failed(new IllegalStateException("committed"));
        }
        return committing;
    }

    public boolean sendResponse(MetaData.Response info, ByteBuffer content, boolean complete) throws IOException {
        SharedBlockingCallback.Blocker blocker = this._response.getHttpOutput().acquireWriteBlockingCallback();
        try {
            boolean committing = this.sendResponse(info, content, complete, blocker);
            blocker.block();
            boolean bl = committing;
            if (blocker != null) {
                blocker.close();
            }
            return bl;
        }
        catch (Throwable throwable) {
            try {
                if (blocker != null) {
                    try {
                        blocker.close();
                    }
                    catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
            catch (Throwable failure) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(failure);
                }
                this.abort(failure);
                throw failure;
            }
        }
    }

    protected void commit(MetaData.Response info) {
        this._committedMetaData = info;
        if (LOG.isDebugEnabled()) {
            LOG.debug("COMMIT for {} on {}{}{} {} {}{}{}", new Object[]{this.getRequest().getRequestURI(), this, System.lineSeparator(), info.getStatus(), info.getReason(), info.getHttpVersion(), System.lineSeparator(), info.getFields()});
        }
    }

    public boolean isCommitted() {
        return this._state.isResponseCommitted();
    }

    public boolean isRequestCompleted() {
        return this._state.isCompleted();
    }

    public boolean isResponseCompleted() {
        return this._state.isResponseCompleted();
    }

    public boolean isPersistent() {
        return this._endPoint.isOpen();
    }

    @Override
    public void write(ByteBuffer content, boolean complete, Callback callback) {
        this.sendResponse(null, content, complete, callback);
    }

    @Override
    public void resetBuffer() {
        if (this.isCommitted()) {
            throw new IllegalStateException("Committed");
        }
    }

    @Override
    public HttpOutput.Interceptor getNextInterceptor() {
        return null;
    }

    protected void execute(Runnable task) {
        this._executor.execute(task);
    }

    public Scheduler getScheduler() {
        return this._connector.getScheduler();
    }

    public boolean useDirectBuffers() {
        return this.getEndPoint() instanceof ChannelEndPoint;
    }

    public void abort(Throwable failure) {
        if (this._state.abortResponse()) {
            this._combinedListener.onResponseFailure(this._request, failure);
            this._transport.abort(failure);
        }
    }

    private void notifyEvent1(Function<Listener, Consumer<Request>> function, Request request) {
        for (Listener listener : this._transientListeners) {
            try {
                function.apply(listener).accept(request);
            }
            catch (Throwable x) {
                LOG.debug("Failure invoking listener " + listener, x);
            }
        }
    }

    private void notifyEvent2(Function<Listener, BiConsumer<Request, ByteBuffer>> function, Request request, ByteBuffer content) {
        for (Listener listener : this._transientListeners) {
            ByteBuffer view = content.slice();
            try {
                function.apply(listener).accept(request, view);
            }
            catch (Throwable x) {
                LOG.debug("Failure invoking listener " + listener, x);
            }
        }
    }

    private void notifyEvent2(Function<Listener, BiConsumer<Request, Throwable>> function, Request request, Throwable failure) {
        for (Listener listener : this._transientListeners) {
            try {
                function.apply(listener).accept(request, failure);
            }
            catch (Throwable x) {
                LOG.debug("Failure invoking listener " + listener, x);
            }
        }
    }

    @Deprecated
    public static class TransientListeners
    implements Listener {
        @Override
        public void onRequestBegin(Request request) {
            request.getHttpChannel().notifyEvent1(listener -> listener::onRequestBegin, request);
        }

        @Override
        public void onBeforeDispatch(Request request) {
            request.getHttpChannel().notifyEvent1(listener -> listener::onBeforeDispatch, request);
        }

        @Override
        public void onDispatchFailure(Request request, Throwable failure) {
            request.getHttpChannel().notifyEvent2(listener -> listener::onDispatchFailure, request, failure);
        }

        @Override
        public void onAfterDispatch(Request request) {
            request.getHttpChannel().notifyEvent1(listener -> listener::onAfterDispatch, request);
        }

        @Override
        public void onRequestContent(Request request, ByteBuffer content) {
            request.getHttpChannel().notifyEvent2(listener -> listener::onRequestContent, request, content);
        }

        @Override
        public void onRequestContentEnd(Request request) {
            request.getHttpChannel().notifyEvent1(listener -> listener::onRequestContentEnd, request);
        }

        @Override
        public void onRequestTrailers(Request request) {
            request.getHttpChannel().notifyEvent1(listener -> listener::onRequestTrailers, request);
        }

        @Override
        public void onRequestEnd(Request request) {
            request.getHttpChannel().notifyEvent1(listener -> listener::onRequestEnd, request);
        }

        @Override
        public void onRequestFailure(Request request, Throwable failure) {
            request.getHttpChannel().notifyEvent2(listener -> listener::onRequestFailure, request, failure);
        }

        @Override
        public void onResponseBegin(Request request) {
            request.getHttpChannel().notifyEvent1(listener -> listener::onResponseBegin, request);
        }

        @Override
        public void onResponseCommit(Request request) {
            request.getHttpChannel().notifyEvent1(listener -> listener::onResponseCommit, request);
        }

        @Override
        public void onResponseContent(Request request, ByteBuffer content) {
            request.getHttpChannel().notifyEvent2(listener -> listener::onResponseContent, request, content);
        }

        @Override
        public void onResponseEnd(Request request) {
            request.getHttpChannel().notifyEvent1(listener -> listener::onResponseEnd, request);
        }

        @Override
        public void onResponseFailure(Request request, Throwable failure) {
            request.getHttpChannel().notifyEvent2(listener -> listener::onResponseFailure, request, failure);
        }

        @Override
        public void onComplete(Request request) {
            request.getHttpChannel().notifyEvent1(listener -> listener::onComplete, request);
        }
    }

    private class Send100Callback
    extends SendCallback {
        private Send100Callback(Callback callback) {
            super(callback, null, false, false);
        }

        @Override
        public void succeeded() {
            if (HttpChannel.this._state.partialResponse()) {
                super.succeeded();
            } else {
                super.failed(new IllegalStateException());
            }
        }
    }

    private class SendCallback
    extends Callback.Nested {
        private final ByteBuffer _content;
        private final int _length;
        private final boolean _commit;
        private final boolean _complete;

        private SendCallback(Callback callback, ByteBuffer content, boolean commit, boolean complete) {
            super(callback);
            this._content = content == null ? BufferUtil.EMPTY_BUFFER : content.slice();
            this._length = this._content.remaining();
            this._commit = commit;
            this._complete = complete;
        }

        @Override
        public void succeeded() {
            HttpChannel.this._written += this._length;
            if (this._commit) {
                HttpChannel.this._combinedListener.onResponseCommit(HttpChannel.this._request);
            }
            if (this._length > 0) {
                HttpChannel.this._combinedListener.onResponseContent(HttpChannel.this._request, this._content);
            }
            if (this._complete && HttpChannel.this._state.completeResponse()) {
                HttpChannel.this._combinedListener.onResponseEnd(HttpChannel.this._request);
            }
            super.succeeded();
        }

        @Override
        public void failed(final Throwable x) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Commit failed", x);
            }
            if (x instanceof BadMessageException) {
                HttpChannel.this._transport.send(HttpGenerator.RESPONSE_500_INFO, false, null, true, new Callback.Nested(this){

                    @Override
                    public void succeeded() {
                        HttpChannel.this._response.getHttpOutput().completed(null);
                        super.failed(x);
                    }

                    @Override
                    public void failed(Throwable th) {
                        HttpChannel.this.abort(x);
                        super.failed(x);
                    }
                });
            } else {
                HttpChannel.this.abort(x);
                super.failed(x);
            }
        }
    }

    public static interface Listener
    extends EventListener {
        default public void onRequestBegin(Request request) {
        }

        default public void onBeforeDispatch(Request request) {
        }

        default public void onDispatchFailure(Request request, Throwable failure) {
        }

        default public void onAfterDispatch(Request request) {
        }

        default public void onRequestContent(Request request, ByteBuffer content) {
        }

        default public void onRequestContentEnd(Request request) {
        }

        default public void onRequestTrailers(Request request) {
        }

        default public void onRequestEnd(Request request) {
        }

        default public void onRequestFailure(Request request, Throwable failure) {
        }

        default public void onResponseBegin(Request request) {
        }

        default public void onResponseCommit(Request request) {
        }

        default public void onResponseContent(Request request, ByteBuffer content) {
        }

        default public void onResponseEnd(Request request) {
        }

        default public void onResponseFailure(Request request, Throwable failure) {
        }

        default public void onComplete(Request request) {
        }
    }

    static interface Dispatchable {
        public void dispatch() throws IOException, ServletException;
    }
}

