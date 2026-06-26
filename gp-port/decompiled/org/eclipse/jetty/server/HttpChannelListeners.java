/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server;

import java.nio.ByteBuffer;
import java.util.Collection;
import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class HttpChannelListeners
implements HttpChannel.Listener {
    static final Logger LOG = Log.getLogger(HttpChannel.class);
    public static HttpChannel.Listener NOOP = new HttpChannel.Listener(){};
    private final NotifyRequest onRequestBegin;
    private final NotifyRequest onBeforeDispatch;
    private final NotifyFailure onDispatchFailure;
    private final NotifyRequest onAfterDispatch;
    private final NotifyContent onRequestContent;
    private final NotifyRequest onRequestContentEnd;
    private final NotifyRequest onRequestTrailers;
    private final NotifyRequest onRequestEnd;
    private final NotifyFailure onRequestFailure;
    private final NotifyRequest onResponseBegin;
    private final NotifyRequest onResponseCommit;
    private final NotifyContent onResponseContent;
    private final NotifyRequest onResponseEnd;
    private final NotifyFailure onResponseFailure;
    private final NotifyRequest onComplete;

    public HttpChannelListeners(Collection<HttpChannel.Listener> listeners) {
        try {
            NotifyRequest onRequestBegin = NotifyRequest.NOOP;
            NotifyRequest onBeforeDispatch = NotifyRequest.NOOP;
            NotifyFailure onDispatchFailure = NotifyFailure.NOOP;
            NotifyRequest onAfterDispatch = NotifyRequest.NOOP;
            NotifyContent onRequestContent = NotifyContent.NOOP;
            NotifyRequest onRequestContentEnd = NotifyRequest.NOOP;
            NotifyRequest onRequestTrailers = NotifyRequest.NOOP;
            NotifyRequest onRequestEnd = NotifyRequest.NOOP;
            NotifyFailure onRequestFailure = NotifyFailure.NOOP;
            NotifyRequest onResponseBegin = NotifyRequest.NOOP;
            NotifyRequest onResponseCommit = NotifyRequest.NOOP;
            NotifyContent onResponseContent = NotifyContent.NOOP;
            NotifyRequest onResponseEnd = NotifyRequest.NOOP;
            NotifyFailure onResponseFailure = NotifyFailure.NOOP;
            NotifyRequest onComplete = NotifyRequest.NOOP;
            for (HttpChannel.Listener listener : listeners) {
                if (!listener.getClass().getMethod("onRequestBegin", Request.class).isDefault()) {
                    onRequestBegin = HttpChannelListeners.combine(onRequestBegin, listener::onRequestBegin);
                }
                if (!listener.getClass().getMethod("onBeforeDispatch", Request.class).isDefault()) {
                    onBeforeDispatch = HttpChannelListeners.combine(onBeforeDispatch, listener::onBeforeDispatch);
                }
                if (!listener.getClass().getMethod("onDispatchFailure", Request.class, Throwable.class).isDefault()) {
                    onDispatchFailure = HttpChannelListeners.combine(onDispatchFailure, listener::onDispatchFailure);
                }
                if (!listener.getClass().getMethod("onAfterDispatch", Request.class).isDefault()) {
                    onAfterDispatch = HttpChannelListeners.combine(onAfterDispatch, listener::onAfterDispatch);
                }
                if (!listener.getClass().getMethod("onRequestContent", Request.class, ByteBuffer.class).isDefault()) {
                    onRequestContent = HttpChannelListeners.combine(onRequestContent, listener::onRequestContent);
                }
                if (!listener.getClass().getMethod("onRequestContentEnd", Request.class).isDefault()) {
                    onRequestContentEnd = HttpChannelListeners.combine(onRequestContentEnd, listener::onRequestContentEnd);
                }
                if (!listener.getClass().getMethod("onRequestTrailers", Request.class).isDefault()) {
                    onRequestTrailers = HttpChannelListeners.combine(onRequestTrailers, listener::onRequestTrailers);
                }
                if (!listener.getClass().getMethod("onRequestEnd", Request.class).isDefault()) {
                    onRequestEnd = HttpChannelListeners.combine(onRequestEnd, listener::onRequestEnd);
                }
                if (!listener.getClass().getMethod("onRequestFailure", Request.class, Throwable.class).isDefault()) {
                    onRequestFailure = HttpChannelListeners.combine(onRequestFailure, listener::onRequestFailure);
                }
                if (!listener.getClass().getMethod("onResponseBegin", Request.class).isDefault()) {
                    onResponseBegin = HttpChannelListeners.combine(onResponseBegin, listener::onResponseBegin);
                }
                if (!listener.getClass().getMethod("onResponseCommit", Request.class).isDefault()) {
                    onResponseCommit = HttpChannelListeners.combine(onResponseCommit, listener::onResponseCommit);
                }
                if (!listener.getClass().getMethod("onResponseContent", Request.class, ByteBuffer.class).isDefault()) {
                    onResponseContent = HttpChannelListeners.combine(onResponseContent, listener::onResponseContent);
                }
                if (!listener.getClass().getMethod("onResponseEnd", Request.class).isDefault()) {
                    onResponseEnd = HttpChannelListeners.combine(onResponseEnd, listener::onResponseEnd);
                }
                if (!listener.getClass().getMethod("onResponseFailure", Request.class, Throwable.class).isDefault()) {
                    onResponseFailure = HttpChannelListeners.combine(onResponseFailure, listener::onResponseFailure);
                }
                if (listener.getClass().getMethod("onComplete", Request.class).isDefault()) continue;
                onComplete = HttpChannelListeners.combine(onComplete, listener::onComplete);
            }
            this.onRequestBegin = onRequestBegin;
            this.onBeforeDispatch = onBeforeDispatch;
            this.onDispatchFailure = onDispatchFailure;
            this.onAfterDispatch = onAfterDispatch;
            this.onRequestContent = onRequestContent;
            this.onRequestContentEnd = onRequestContentEnd;
            this.onRequestTrailers = onRequestTrailers;
            this.onRequestEnd = onRequestEnd;
            this.onRequestFailure = onRequestFailure;
            this.onResponseBegin = onResponseBegin;
            this.onResponseCommit = onResponseCommit;
            this.onResponseContent = onResponseContent;
            this.onResponseEnd = onResponseEnd;
            this.onResponseFailure = onResponseFailure;
            this.onComplete = onComplete;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onRequestBegin(Request request) {
        this.onRequestBegin.onRequest(request);
    }

    @Override
    public void onBeforeDispatch(Request request) {
        this.onBeforeDispatch.onRequest(request);
    }

    @Override
    public void onDispatchFailure(Request request, Throwable failure) {
        this.onDispatchFailure.onFailure(request, failure);
    }

    @Override
    public void onAfterDispatch(Request request) {
        this.onAfterDispatch.onRequest(request);
    }

    @Override
    public void onRequestContent(Request request, ByteBuffer content) {
        this.onRequestContent.onContent(request, content);
    }

    @Override
    public void onRequestContentEnd(Request request) {
        this.onRequestContentEnd.onRequest(request);
    }

    @Override
    public void onRequestTrailers(Request request) {
        this.onRequestTrailers.onRequest(request);
    }

    @Override
    public void onRequestEnd(Request request) {
        this.onRequestEnd.onRequest(request);
    }

    @Override
    public void onRequestFailure(Request request, Throwable failure) {
        this.onRequestFailure.onFailure(request, failure);
    }

    @Override
    public void onResponseBegin(Request request) {
        this.onResponseBegin.onRequest(request);
    }

    @Override
    public void onResponseCommit(Request request) {
        this.onResponseCommit.onRequest(request);
    }

    @Override
    public void onResponseContent(Request request, ByteBuffer content) {
        this.onResponseContent.onContent(request, content);
    }

    @Override
    public void onResponseEnd(Request request) {
        this.onResponseEnd.onRequest(request);
    }

    @Override
    public void onResponseFailure(Request request, Throwable failure) {
        this.onResponseFailure.onFailure(request, failure);
    }

    @Override
    public void onComplete(Request request) {
        this.onComplete.onRequest(request);
    }

    private static NotifyRequest combine(NotifyRequest first, NotifyRequest second) {
        if (first == NotifyRequest.NOOP) {
            return second;
        }
        if (second == NotifyRequest.NOOP) {
            return first;
        }
        return request -> {
            first.onRequest(request);
            second.onRequest(request);
        };
    }

    private static NotifyFailure combine(NotifyFailure first, NotifyFailure second) {
        if (first == NotifyFailure.NOOP) {
            return second;
        }
        if (second == NotifyFailure.NOOP) {
            return first;
        }
        return (request, throwable) -> {
            first.onFailure(request, throwable);
            second.onFailure(request, throwable);
        };
    }

    private static NotifyContent combine(NotifyContent first, NotifyContent second) {
        if (first == NotifyContent.NOOP) {
            return (request, content) -> second.onContent(request, content.slice());
        }
        if (second == NotifyContent.NOOP) {
            return (request, content) -> first.onContent(request, content.slice());
        }
        return (request, content) -> {
            content = content.slice();
            first.onContent(request, content);
            second.onContent(request, content);
        };
    }

    private static interface NotifyContent {
        public static final NotifyContent NOOP = (request, content) -> {};

        public void onContent(Request var1, ByteBuffer var2);
    }

    private static interface NotifyFailure {
        public static final NotifyFailure NOOP = (request, failure) -> {};

        public void onFailure(Request var1, Throwable var2);
    }

    private static interface NotifyRequest {
        public static final NotifyRequest NOOP = request -> {};

        public void onRequest(Request var1);
    }
}

