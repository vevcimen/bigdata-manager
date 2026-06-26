/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server.handler;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HandlerContainer;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandlerContainer;
import org.eclipse.jetty.util.ArrayUtil;
import org.eclipse.jetty.util.MultiException;
import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.annotation.ManagedObject;

@ManagedObject(value="Handler of multiple handlers")
public class HandlerCollection
extends AbstractHandlerContainer {
    private final boolean _mutableWhenRunning;
    protected final AtomicReference<Handlers> _handlers = new AtomicReference();

    public HandlerCollection() {
        this(false, new Handler[0]);
    }

    public HandlerCollection(Handler ... handlers) {
        this(false, handlers);
    }

    public HandlerCollection(boolean mutableWhenRunning, Handler ... handlers) {
        this._mutableWhenRunning = mutableWhenRunning;
        if (handlers.length > 0) {
            this.setHandlers(handlers);
        }
    }

    @Override
    @ManagedAttribute(value="Wrapped handlers", readonly=true)
    public Handler[] getHandlers() {
        Handlers handlers = this._handlers.get();
        return handlers == null ? null : handlers._handlers;
    }

    public void setHandlers(Handler[] handlers) {
        if (!this._mutableWhenRunning && this.isStarted()) {
            throw new IllegalStateException("STARTED");
        }
        while (!this.updateHandlers(this._handlers.get(), this.newHandlers(handlers))) {
        }
    }

    protected Handlers newHandlers(Handler[] handlers) {
        if (handlers == null || handlers.length == 0) {
            return null;
        }
        return new Handlers(handlers);
    }

    protected boolean updateHandlers(Handlers old, Handlers handlers) {
        if (handlers != null) {
            for (Handler handler : handlers._handlers) {
                if (handler != this && (!(handler instanceof HandlerContainer) || !Arrays.asList(((HandlerContainer)((Object)handler)).getChildHandlers()).contains(this))) continue;
                throw new IllegalStateException("setHandler loop");
            }
            for (Handler handler : handlers._handlers) {
                if (handler.getServer() == this.getServer()) continue;
                handler.setServer(this.getServer());
            }
        }
        if (this._handlers.compareAndSet(old, handlers)) {
            Object[] oldBeans = old == null ? null : old._handlers;
            Object[] newBeans = handlers == null ? null : handlers._handlers;
            this.updateBeans(oldBeans, newBeans);
            return true;
        }
        return false;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (this.isStarted()) {
            Handlers handlers = this._handlers.get();
            if (handlers == null) {
                return;
            }
            MultiException mex = null;
            for (Handler handler : handlers._handlers) {
                try {
                    handler.handle(target, baseRequest, request, response);
                }
                catch (IOException | RuntimeException e) {
                    throw e;
                }
                catch (Exception e) {
                    if (mex == null) {
                        mex = new MultiException();
                    }
                    mex.add(e);
                }
            }
            if (mex != null) {
                if (mex.size() == 1) {
                    throw new ServletException(mex.getThrowable(0));
                }
                throw new ServletException(mex);
            }
        }
    }

    public void addHandler(Handler handler) {
        Handlers handlers;
        Handlers old;
        while (!this.updateHandlers(old, handlers = this.newHandlers(ArrayUtil.addToArray((old = this._handlers.get()) == null ? null : ArrayUtil.removeFromArray(old._handlers, handler), handler, Handler.class)))) {
        }
    }

    public void prependHandler(Handler handler) {
        Handlers handlers;
        Handlers old;
        while (!this.updateHandlers(old, handlers = this.newHandlers(ArrayUtil.prependToArray(handler, (old = this._handlers.get()) == null ? null : old._handlers, Handler.class)))) {
        }
    }

    public void removeHandler(Handler handler) {
        Handlers handlers;
        Handlers old;
        while ((old = this._handlers.get()) != null && old._handlers.length != 0 && !this.updateHandlers(old, handlers = this.newHandlers(ArrayUtil.removeFromArray(old._handlers, handler)))) {
        }
    }

    @Override
    protected void expandChildren(List<Handler> list, Class<?> byClass) {
        Handler[] handlers = this.getHandlers();
        if (handlers != null) {
            for (Handler h2 : handlers) {
                this.expandHandler(h2, list, byClass);
            }
        }
    }

    @Override
    public void destroy() {
        if (!this.isStopped()) {
            throw new IllegalStateException("!STOPPED");
        }
        Handler[] children = this.getChildHandlers();
        this.setHandlers(null);
        for (Handler child : children) {
            child.destroy();
        }
        super.destroy();
    }

    protected static class Handlers {
        private final Handler[] _handlers;

        protected Handlers(Handler[] handlers) {
            this._handlers = handlers;
        }

        public Handler[] getHandlers() {
            return this._handlers;
        }
    }
}

