/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server;

import java.io.IOException;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.HttpChannelState;
import org.eclipse.jetty.server.handler.ContextHandler;

public class AsyncContextState
implements AsyncContext {
    private final HttpChannel _channel;
    volatile HttpChannelState _state;

    public AsyncContextState(HttpChannelState state) {
        this._state = state;
        this._channel = this._state.getHttpChannel();
    }

    public HttpChannel getHttpChannel() {
        return this._channel;
    }

    HttpChannelState state() {
        HttpChannelState state = this._state;
        if (state == null) {
            throw new IllegalStateException("AsyncContext completed and/or Request lifecycle recycled");
        }
        return state;
    }

    @Override
    public void addListener(AsyncListener listener, ServletRequest request, ServletResponse response) {
        WrappedAsyncListener wrap = new WrappedAsyncListener(listener, request, response);
        this.state().addListener(wrap);
    }

    @Override
    public void addListener(AsyncListener listener) {
        this.state().addListener(listener);
    }

    @Override
    public void complete() {
        this.state().complete();
    }

    @Override
    public <T extends AsyncListener> T createListener(Class<T> clazz) throws ServletException {
        ContextHandler contextHandler = this.state().getContextHandler();
        if (contextHandler != null) {
            return (T)((AsyncListener)contextHandler.getServletContext().createListener(clazz));
        }
        try {
            return (T)((AsyncListener)clazz.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]));
        }
        catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    public void dispatch() {
        this.state().dispatch(null, null);
    }

    @Override
    public void dispatch(String path) {
        this.state().dispatch(null, path);
    }

    @Override
    public void dispatch(ServletContext context, String path) {
        this.state().dispatch(context, path);
    }

    @Override
    public ServletRequest getRequest() {
        return this.state().getAsyncContextEvent().getSuppliedRequest();
    }

    @Override
    public ServletResponse getResponse() {
        return this.state().getAsyncContextEvent().getSuppliedResponse();
    }

    @Override
    public long getTimeout() {
        return this.state().getTimeout();
    }

    @Override
    public boolean hasOriginalRequestAndResponse() {
        HttpChannel channel = this.state().getHttpChannel();
        return channel.getRequest() == this.getRequest() && channel.getResponse() == this.getResponse();
    }

    @Override
    public void setTimeout(long arg0) {
        this.state().setTimeout(arg0);
    }

    @Override
    public void start(final Runnable task) {
        final HttpChannel channel = this.state().getHttpChannel();
        channel.execute(new Runnable(){

            @Override
            public void run() {
                ContextHandler.Context context = AsyncContextState.this.state().getAsyncContextEvent().getContext();
                if (context == null) {
                    task.run();
                } else {
                    context.getContextHandler().handle(channel.getRequest(), task);
                }
            }
        });
    }

    public void reset() {
        this._state = null;
    }

    public HttpChannelState getHttpChannelState() {
        return this.state();
    }

    public static class WrappedAsyncListener
    implements AsyncListener {
        private final AsyncListener _listener;
        private final ServletRequest _request;
        private final ServletResponse _response;

        public WrappedAsyncListener(AsyncListener listener, ServletRequest request, ServletResponse response) {
            this._listener = listener;
            this._request = request;
            this._response = response;
        }

        public AsyncListener getListener() {
            return this._listener;
        }

        @Override
        public void onTimeout(AsyncEvent event) throws IOException {
            this._listener.onTimeout(new AsyncEvent(event.getAsyncContext(), this._request, this._response, event.getThrowable()));
        }

        @Override
        public void onStartAsync(AsyncEvent event) throws IOException {
            this._listener.onStartAsync(new AsyncEvent(event.getAsyncContext(), this._request, this._response, event.getThrowable()));
        }

        @Override
        public void onError(AsyncEvent event) throws IOException {
            this._listener.onError(new AsyncEvent(event.getAsyncContext(), this._request, this._response, event.getThrowable()));
        }

        @Override
        public void onComplete(AsyncEvent event) throws IOException {
            this._listener.onComplete(new AsyncEvent(event.getAsyncContext(), this._request, this._response, event.getThrowable()));
        }
    }
}

