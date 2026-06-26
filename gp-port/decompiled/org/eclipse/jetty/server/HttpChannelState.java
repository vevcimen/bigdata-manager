/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.servlet.AsyncListener;
import javax.servlet.ServletContext;
import javax.servlet.ServletResponse;
import javax.servlet.UnavailableException;
import org.eclipse.jetty.http.BadMessageException;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.io.QuietException;
import org.eclipse.jetty.server.AsyncContextEvent;
import org.eclipse.jetty.server.AsyncContextState;
import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.thread.Scheduler;

public class HttpChannelState {
    private static final Logger LOG = Log.getLogger(HttpChannelState.class);
    private static final long DEFAULT_TIMEOUT = Long.getLong("org.eclipse.jetty.server.HttpChannelState.DEFAULT_TIMEOUT", 30000L);
    private final HttpChannel _channel;
    private List<AsyncListener> _asyncListeners;
    private State _state = State.IDLE;
    private RequestState _requestState = RequestState.BLOCKING;
    private OutputState _outputState = OutputState.OPEN;
    private InputState _inputState = InputState.IDLE;
    private boolean _initial = true;
    private boolean _sendError;
    private boolean _asyncWritePossible;
    private long _timeoutMs = DEFAULT_TIMEOUT;
    private AsyncContextEvent _event;

    protected HttpChannelState(HttpChannel channel) {
        this._channel = channel;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public State getState() {
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            return this._state;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addListener(AsyncListener listener) {
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            if (this._asyncListeners == null) {
                this._asyncListeners = new ArrayList<AsyncListener>();
            }
            this._asyncListeners.add(listener);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean hasListener(AsyncListener listener) {
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            if (this._asyncListeners == null) {
                return false;
            }
            for (AsyncListener l : this._asyncListeners) {
                if (l == listener) {
                    return true;
                }
                if (!(l instanceof AsyncContextState.WrappedAsyncListener) || ((AsyncContextState.WrappedAsyncListener)l).getListener() != listener) continue;
                return true;
            }
            return false;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean isSendError() {
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            return this._sendError;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setTimeout(long ms) {
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            this._timeoutMs = ms;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public long getTimeout() {
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            return this._timeoutMs;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public AsyncContextEvent getAsyncContextEvent() {
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            return this._event;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String toString() {
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            return this.toStringLocked();
        }
    }

    private String toStringLocked() {
        return String.format("%s@%x{%s}", this.getClass().getSimpleName(), this.hashCode(), this.getStatusStringLocked());
    }

    private String getStatusStringLocked() {
        return String.format("s=%s rs=%s os=%s is=%s awp=%b se=%b i=%b al=%d", new Object[]{this._state, this._requestState, this._outputState, this._inputState, this._asyncWritePossible, this._sendError, this._initial, this._asyncListeners == null ? 0 : this._asyncListeners.size()});
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String getStatusString() {
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            return this.getStatusStringLocked();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean commitResponse() {
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            switch (this._outputState) {
                case OPEN: {
                    this._outputState = OutputState.COMMITTED;
                    return true;
                }
            }
            return false;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean partialResponse() {
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            switch (this._outputState) {
                case COMMITTED: {
                    this._outputState = OutputState.OPEN;
                    return true;
                }
            }
            return false;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean completeResponse() {
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            switch (this._outputState) {
                case OPEN: 
                case COMMITTED: {
                    this._outputState = OutputState.COMPLETED;
                    return true;
                }
            }
            return false;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean isResponseCommitted() {
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            switch (this._outputState) {
                case OPEN: {
                    return false;
                }
            }
            return true;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean isResponseCompleted() {
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            return this._outputState == OutputState.COMPLETED;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean abortResponse() {
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            switch (this._outputState) {
                case ABORTED: {
                    return false;
                }
                case OPEN: {
                    this._channel.getResponse().setStatus(500);
                    this._outputState = OutputState.ABORTED;
                    return true;
                }
            }
            this._outputState = OutputState.ABORTED;
            return true;
        }
    }

    public Action handling() {
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("handling {}", this.toStringLocked());
            }
            switch (this._state) {
                case IDLE: {
                    if (this._requestState != RequestState.BLOCKING) {
                        throw new IllegalStateException(this.getStatusStringLocked());
                    }
                    this._initial = true;
                    this._state = State.HANDLING;
                    return Action.DISPATCH;
                }
                case WOKEN: {
                    if (this._event != null && this._event.getThrowable() != null && !this._sendError) {
                        this._state = State.HANDLING;
                        return Action.ASYNC_ERROR;
                    }
                    Action action = this.nextAction(true);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("nextAction(true) {} {}", new Object[]{action, this.toStringLocked()});
                    }
                    return action;
                }
            }
            throw new IllegalStateException(this.getStatusStringLocked());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected Action unhandle() {
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("unhandle {}", this.toStringLocked());
            }
            if (this._state != State.HANDLING) {
                throw new IllegalStateException(this.getStatusStringLocked());
            }
            this._initial = false;
            Action action = this.nextAction(false);
            if (LOG.isDebugEnabled()) {
                LOG.debug("nextAction(false) {} {}", new Object[]{action, this.toStringLocked()});
            }
            return action;
        }
    }

    private Action nextAction(boolean handling) {
        this._state = State.HANDLING;
        if (this._sendError) {
            switch (this._requestState) {
                case BLOCKING: 
                case ASYNC: 
                case COMPLETE: 
                case DISPATCH: 
                case COMPLETING: {
                    this._requestState = RequestState.BLOCKING;
                    this._sendError = false;
                    return Action.SEND_ERROR;
                }
            }
        }
        switch (this._requestState) {
            case BLOCKING: {
                if (handling) {
                    throw new IllegalStateException(this.getStatusStringLocked());
                }
                this._requestState = RequestState.COMPLETING;
                return Action.COMPLETE;
            }
            case ASYNC: {
                switch (this._inputState) {
                    case POSSIBLE: {
                        this._inputState = InputState.PRODUCING;
                        return Action.READ_PRODUCE;
                    }
                    case READY: {
                        this._inputState = InputState.IDLE;
                        return Action.READ_CALLBACK;
                    }
                    case REGISTER: 
                    case PRODUCING: {
                        this._inputState = InputState.REGISTERED;
                        return Action.READ_REGISTER;
                    }
                    case IDLE: 
                    case REGISTERED: {
                        break;
                    }
                    default: {
                        throw new IllegalStateException(this.getStatusStringLocked());
                    }
                }
                if (this._asyncWritePossible) {
                    this._asyncWritePossible = false;
                    return Action.WRITE_CALLBACK;
                }
                Scheduler scheduler = this._channel.getScheduler();
                if (scheduler != null && this._timeoutMs > 0L && !this._event.hasTimeoutTask()) {
                    this._event.setTimeoutTask(scheduler.schedule(this._event, this._timeoutMs, TimeUnit.MILLISECONDS));
                }
                this._state = State.WAITING;
                return Action.WAIT;
            }
            case DISPATCH: {
                this._requestState = RequestState.BLOCKING;
                return Action.ASYNC_DISPATCH;
            }
            case EXPIRE: {
                this._requestState = RequestState.EXPIRING;
                return Action.ASYNC_TIMEOUT;
            }
            case EXPIRING: {
                if (handling) {
                    throw new IllegalStateException(this.getStatusStringLocked());
                }
                this.sendError(500, "AsyncContext timeout");
                this._requestState = RequestState.BLOCKING;
                this._sendError = false;
                return Action.SEND_ERROR;
            }
            case COMPLETE: {
                this._requestState = RequestState.COMPLETING;
                return Action.COMPLETE;
            }
            case COMPLETING: {
                this._state = State.WAITING;
                return Action.WAIT;
            }
            case COMPLETED: {
                this._state = State.IDLE;
                return Action.TERMINATED;
            }
        }
        throw new IllegalStateException(this.getStatusStringLocked());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void startAsync(final AsyncContextEvent event) {
        List<AsyncListener> lastAsyncListeners;
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("startAsync {}", this.toStringLocked());
            }
            if (this._state != State.HANDLING || this._requestState != RequestState.BLOCKING) {
                throw new IllegalStateException(this.getStatusStringLocked());
            }
            this._requestState = RequestState.ASYNC;
            this._event = event;
            lastAsyncListeners = this._asyncListeners;
            this._asyncListeners = null;
        }
        if (lastAsyncListeners != null) {
            Runnable callback = new Runnable(){

                @Override
                public void run() {
                    for (AsyncListener listener : lastAsyncListeners) {
                        try {
                            listener.onStartAsync(event);
                        }
                        catch (Throwable e) {
                            LOG.warn(e);
                        }
                    }
                }

                public String toString() {
                    return "startAsync";
                }
            };
            this.runInContext(event, callback);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void dispatch(ServletContext context, String path) {
        AsyncContextEvent event;
        boolean dispatch = false;
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("dispatch {} -> {}", this.toStringLocked(), path);
            }
            switch (this._requestState) {
                case ASYNC: 
                case EXPIRING: {
                    break;
                }
                default: {
                    throw new IllegalStateException(this.getStatusStringLocked());
                }
            }
            if (context != null) {
                this._event.setDispatchContext(context);
            }
            if (path != null) {
                this._event.setDispatchPath(path);
            }
            if (this._requestState == RequestState.ASYNC && this._state == State.WAITING) {
                this._state = State.WOKEN;
                dispatch = true;
            }
            this._requestState = RequestState.DISPATCH;
            event = this._event;
        }
        this.cancelTimeout(event);
        if (dispatch) {
            this.scheduleDispatch();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void timeout() {
        boolean dispatch = false;
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Timeout {}", this.toStringLocked());
            }
            if (this._requestState != RequestState.ASYNC) {
                return;
            }
            this._requestState = RequestState.EXPIRE;
            if (this._state == State.WAITING) {
                this._state = State.WOKEN;
                dispatch = true;
            }
        }
        if (dispatch) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Dispatch after async timeout {}", this);
            }
            this.scheduleDispatch();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void onTimeout() {
        List<AsyncListener> listeners;
        AsyncContextEvent event;
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("onTimeout {}", this.toStringLocked());
            }
            if (this._requestState != RequestState.EXPIRING || this._state != State.HANDLING) {
                throw new IllegalStateException(this.toStringLocked());
            }
            event = this._event;
            listeners = this._asyncListeners;
        }
        if (listeners != null) {
            Runnable task = new Runnable(){

                @Override
                public void run() {
                    for (AsyncListener listener : listeners) {
                        try {
                            listener.onTimeout(event);
                        }
                        catch (Throwable x) {
                            LOG.warn(x + " while invoking onTimeout listener " + listener, new Object[0]);
                            LOG.debug(x);
                        }
                    }
                }

                public String toString() {
                    return "onTimeout";
                }
            };
            this.runInContext(event, task);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void complete() {
        AsyncContextEvent event;
        boolean handle = false;
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("complete {}", this.toStringLocked());
            }
            event = this._event;
            switch (this._requestState) {
                case ASYNC: 
                case EXPIRING: {
                    this._requestState = this._sendError ? RequestState.BLOCKING : RequestState.COMPLETE;
                    break;
                }
                case COMPLETE: {
                    return;
                }
                default: {
                    throw new IllegalStateException(this.getStatusStringLocked());
                }
            }
            if (this._state == State.WAITING) {
                handle = true;
                this._state = State.WOKEN;
            }
        }
        this.cancelTimeout(event);
        if (handle) {
            this.runInContext(event, this._channel);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void asyncError(Throwable failure) {
        AsyncContextEvent event = null;
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("asyncError " + this.toStringLocked(), failure);
            }
            if (this._state == State.WAITING && this._requestState == RequestState.ASYNC) {
                this._state = State.WOKEN;
                this._event.addThrowable(failure);
                event = this._event;
            } else {
                if (!(failure instanceof QuietException)) {
                    LOG.warn(failure.toString(), new Object[0]);
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug(failure);
                }
            }
        }
        if (event != null) {
            this.cancelTimeout(event);
            this.runInContext(event, this._channel);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void onError(Throwable th) {
        List<AsyncListener> asyncListeners;
        AsyncContextEvent asyncEvent;
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("thrownException " + this.getStatusStringLocked(), th);
            }
            if (this._state != State.HANDLING) {
                throw new IllegalStateException(this.getStatusStringLocked());
            }
            if (this._sendError) {
                LOG.warn("unhandled due to prior sendError", th);
                return;
            }
            switch (this._requestState) {
                case BLOCKING: {
                    this.sendError(th);
                    return;
                }
                case ASYNC: 
                case COMPLETE: 
                case DISPATCH: {
                    if (this._asyncListeners == null || this._asyncListeners.isEmpty()) {
                        this.sendError(th);
                        return;
                    }
                    asyncEvent = this._event;
                    asyncEvent.addThrowable(th);
                    asyncListeners = this._asyncListeners;
                    break;
                }
                default: {
                    LOG.warn("unhandled in state " + (Object)((Object)this._requestState), new IllegalStateException(th));
                    return;
                }
            }
        }
        this.runInContext(asyncEvent, () -> {
            for (AsyncListener listener : asyncListeners) {
                try {
                    listener.onError(asyncEvent);
                }
                catch (Throwable x) {
                    LOG.warn(x + " while invoking onError listener " + listener, new Object[0]);
                    LOG.debug(x);
                }
            }
        });
        httpChannelState = this;
        synchronized (httpChannelState) {
            if (this._requestState == RequestState.ASYNC && !this._sendError) {
                this.sendError(th);
            } else if (this._requestState != RequestState.COMPLETE) {
                LOG.warn("unhandled in state " + (Object)((Object)this._requestState), new IllegalStateException(th));
            }
        }
    }

    private void sendError(Throwable th) {
        String message;
        int code;
        Request request = this._channel.getRequest();
        Throwable cause = this._channel.unwrap(th, BadMessageException.class, UnavailableException.class);
        if (cause == null) {
            code = 500;
            message = th.toString();
        } else if (cause instanceof BadMessageException) {
            BadMessageException bme = (BadMessageException)cause;
            code = bme.getCode();
            message = bme.getReason();
        } else if (cause instanceof UnavailableException) {
            message = cause.toString();
            code = ((UnavailableException)cause).isPermanent() ? 404 : 503;
        } else {
            code = 500;
            message = null;
        }
        this.sendError(code, message);
        request.setAttribute("javax.servlet.error.exception", th);
        request.setAttribute("javax.servlet.error.exception_type", th.getClass());
        this._requestState = RequestState.BLOCKING;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void sendError(int code, String message) {
        Request request = this._channel.getRequest();
        Response response = this._channel.getResponse();
        if (message == null) {
            message = HttpStatus.getMessage(code);
        }
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            Throwable cause;
            if (LOG.isDebugEnabled()) {
                LOG.debug("sendError {}", this.toStringLocked());
            }
            if (this._outputState != OutputState.OPEN) {
                throw new IllegalStateException(this._outputState.toString());
            }
            switch (this._state) {
                case WOKEN: 
                case HANDLING: 
                case WAITING: {
                    break;
                }
                default: {
                    throw new IllegalStateException(this.getStatusStringLocked());
                }
            }
            response.setStatus(code);
            response.errorClose();
            request.setAttribute("org.eclipse.jetty.server.error_context", request.getErrorContext());
            request.setAttribute("javax.servlet.error.request_uri", request.getRequestURI());
            request.setAttribute("javax.servlet.error.servlet_name", request.getServletName());
            request.setAttribute("javax.servlet.error.status_code", code);
            request.setAttribute("javax.servlet.error.message", message);
            this._sendError = true;
            if (this._event != null && (cause = (Throwable)request.getAttribute("javax.servlet.error.exception")) != null) {
                this._event.addThrowable(cause);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void completing() {
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("completing {}", this.toStringLocked());
            }
            switch (this._requestState) {
                case COMPLETED: {
                    throw new IllegalStateException(this.getStatusStringLocked());
                }
            }
            this._requestState = RequestState.COMPLETING;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void completed(Throwable failure) {
        AsyncContextEvent event;
        List<AsyncListener> aListeners;
        boolean handle = false;
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("completed {}", this.toStringLocked());
            }
            if (this._requestState != RequestState.COMPLETING) {
                throw new IllegalStateException(this.getStatusStringLocked());
            }
            if (this._event == null) {
                this._requestState = RequestState.COMPLETED;
                aListeners = null;
                event = null;
                if (this._state == State.WAITING) {
                    this._state = State.WOKEN;
                    handle = true;
                }
            } else {
                aListeners = this._asyncListeners;
                event = this._event;
            }
        }
        this._channel.getResponse().getHttpOutput().completed(failure);
        if (event != null) {
            this.cancelTimeout(event);
            if (aListeners != null) {
                this.runInContext(event, () -> {
                    for (AsyncListener listener : aListeners) {
                        try {
                            listener.onComplete(event);
                        }
                        catch (Throwable e) {
                            LOG.warn(e + " while invoking onComplete listener " + listener, new Object[0]);
                            LOG.debug(e);
                        }
                    }
                });
            }
            event.completed();
            httpChannelState = this;
            synchronized (httpChannelState) {
                this._requestState = RequestState.COMPLETED;
                if (this._state == State.WAITING) {
                    this._state = State.WOKEN;
                    handle = true;
                }
            }
        }
        if (handle) {
            this._channel.handle();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void recycle() {
        this.cancelTimeout();
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("recycle {}", this.toStringLocked());
            }
            switch (this._state) {
                case HANDLING: {
                    throw new IllegalStateException(this.getStatusStringLocked());
                }
                case UPGRADED: {
                    return;
                }
            }
            this._asyncListeners = null;
            this._state = State.IDLE;
            this._requestState = RequestState.BLOCKING;
            this._outputState = OutputState.OPEN;
            this._initial = true;
            this._inputState = InputState.IDLE;
            this._asyncWritePossible = false;
            this._timeoutMs = DEFAULT_TIMEOUT;
            this._event = null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void upgrade() {
        this.cancelTimeout();
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("upgrade {}", this.toStringLocked());
            }
            switch (this._state) {
                case IDLE: {
                    break;
                }
                default: {
                    throw new IllegalStateException(this.getStatusStringLocked());
                }
            }
            this._asyncListeners = null;
            this._state = State.UPGRADED;
            this._requestState = RequestState.BLOCKING;
            this._initial = true;
            this._inputState = InputState.IDLE;
            this._asyncWritePossible = false;
            this._timeoutMs = DEFAULT_TIMEOUT;
            this._event = null;
        }
    }

    protected void scheduleDispatch() {
        this._channel.execute(this._channel);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void cancelTimeout() {
        AsyncContextEvent event;
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            event = this._event;
        }
        this.cancelTimeout(event);
    }

    protected void cancelTimeout(AsyncContextEvent event) {
        if (event != null) {
            event.cancelTimeoutTask();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean isIdle() {
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            return this._state == State.IDLE;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean isExpired() {
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            return this._requestState == RequestState.EXPIRE || this._requestState == RequestState.EXPIRING;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean isInitial() {
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            return this._initial;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean isSuspended() {
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            return this._state == State.WAITING || this._state == State.HANDLING && this._requestState == RequestState.ASYNC;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    boolean isCompleted() {
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            return this._requestState == RequestState.COMPLETED;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean isAsyncStarted() {
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            if (this._state == State.HANDLING) {
                return this._requestState != RequestState.BLOCKING;
            }
            return this._requestState == RequestState.ASYNC || this._requestState == RequestState.EXPIRING;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean isAsync() {
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            return !this._initial || this._requestState != RequestState.BLOCKING;
        }
    }

    public Request getBaseRequest() {
        return this._channel.getRequest();
    }

    public HttpChannel getHttpChannel() {
        return this._channel;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public ContextHandler getContextHandler() {
        AsyncContextEvent event;
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            event = this._event;
        }
        return this.getContextHandler(event);
    }

    ContextHandler getContextHandler(AsyncContextEvent event) {
        ContextHandler.Context context;
        if (event != null && (context = (ContextHandler.Context)event.getServletContext()) != null) {
            return context.getContextHandler();
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public ServletResponse getServletResponse() {
        AsyncContextEvent event;
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            event = this._event;
        }
        return this.getServletResponse(event);
    }

    public ServletResponse getServletResponse(AsyncContextEvent event) {
        if (event != null && event.getSuppliedResponse() != null) {
            return event.getSuppliedResponse();
        }
        return this._channel.getResponse();
    }

    void runInContext(AsyncContextEvent event, Runnable runnable) {
        ContextHandler contextHandler = this.getContextHandler(event);
        if (contextHandler == null) {
            runnable.run();
        } else {
            contextHandler.handle(this._channel.getRequest(), runnable);
        }
    }

    public Object getAttribute(String name) {
        return this._channel.getRequest().getAttribute(name);
    }

    public void removeAttribute(String name) {
        this._channel.getRequest().removeAttribute(name);
    }

    public void setAttribute(String name, Object attribute) {
        this._channel.getRequest().setAttribute(name, attribute);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void onReadUnready() {
        boolean interested = false;
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("onReadUnready {}", this.toStringLocked());
            }
            switch (this._inputState) {
                case READY: 
                case IDLE: {
                    if (this._state == State.WAITING) {
                        interested = true;
                        this._inputState = InputState.REGISTERED;
                        break;
                    }
                    this._inputState = InputState.REGISTER;
                    break;
                }
            }
        }
        if (interested) {
            this._channel.onAsyncWaitForContent();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean onContentAdded() {
        boolean woken = false;
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("onContentAdded {}", this.toStringLocked());
            }
            switch (this._inputState) {
                case READY: 
                case IDLE: {
                    break;
                }
                case PRODUCING: {
                    this._inputState = InputState.READY;
                    break;
                }
                case REGISTER: 
                case REGISTERED: {
                    this._inputState = InputState.READY;
                    if (this._state != State.WAITING) break;
                    woken = true;
                    this._state = State.WOKEN;
                    break;
                }
                case POSSIBLE: {
                    throw new IllegalStateException(this.toStringLocked());
                }
            }
        }
        return woken;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean onReadReady() {
        boolean woken = false;
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("onReadReady {}", this.toStringLocked());
            }
            switch (this._inputState) {
                case IDLE: {
                    this._inputState = InputState.READY;
                    if (this._state != State.WAITING) break;
                    woken = true;
                    this._state = State.WOKEN;
                    break;
                }
                default: {
                    throw new IllegalStateException(this.toStringLocked());
                }
            }
        }
        return woken;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean onReadPossible() {
        boolean woken = false;
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("onReadPossible {}", this.toStringLocked());
            }
            switch (this._inputState) {
                case REGISTERED: {
                    this._inputState = InputState.POSSIBLE;
                    if (this._state != State.WAITING) break;
                    woken = true;
                    this._state = State.WOKEN;
                    break;
                }
                default: {
                    throw new IllegalStateException(this.toStringLocked());
                }
            }
        }
        return woken;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean onReadEof() {
        boolean woken = false;
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("onEof {}", this.toStringLocked());
            }
            this._inputState = InputState.READY;
            if (this._state == State.WAITING) {
                woken = true;
                this._state = State.WOKEN;
            }
        }
        return woken;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean onWritePossible() {
        boolean wake = false;
        HttpChannelState httpChannelState = this;
        synchronized (httpChannelState) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("onWritePossible {}", this.toStringLocked());
            }
            this._asyncWritePossible = true;
            if (this._state == State.WAITING) {
                this._state = State.WOKEN;
                wake = true;
            }
        }
        return wake;
    }

    public static enum Action {
        DISPATCH,
        ASYNC_DISPATCH,
        SEND_ERROR,
        ASYNC_ERROR,
        ASYNC_TIMEOUT,
        WRITE_CALLBACK,
        READ_REGISTER,
        READ_PRODUCE,
        READ_CALLBACK,
        COMPLETE,
        TERMINATED,
        WAIT;

    }

    private static enum OutputState {
        OPEN,
        COMMITTED,
        COMPLETED,
        ABORTED;

    }

    private static enum InputState {
        IDLE,
        REGISTER,
        REGISTERED,
        POSSIBLE,
        PRODUCING,
        READY;

    }

    private static enum RequestState {
        BLOCKING,
        ASYNC,
        DISPATCH,
        EXPIRE,
        EXPIRING,
        COMPLETE,
        COMPLETING,
        COMPLETED;

    }

    public static enum State {
        IDLE,
        HANDLING,
        WAITING,
        WOKEN,
        UPGRADED;

    }
}

