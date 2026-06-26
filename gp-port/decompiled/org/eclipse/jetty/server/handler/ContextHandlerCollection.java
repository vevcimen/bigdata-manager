/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server.handler;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HandlerContainer;
import org.eclipse.jetty.server.HttpChannelState;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.util.ArrayTernaryTrie;
import org.eclipse.jetty.util.ArrayUtil;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.Trie;
import org.eclipse.jetty.util.annotation.ManagedObject;
import org.eclipse.jetty.util.annotation.ManagedOperation;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.thread.SerializedExecutor;

@ManagedObject(value="Context Handler Collection")
public class ContextHandlerCollection
extends HandlerCollection {
    private static final Logger LOG = Log.getLogger(ContextHandlerCollection.class);
    private final SerializedExecutor _serializedExecutor = new SerializedExecutor();
    @Deprecated
    private Class<? extends ContextHandler> _contextClass = ContextHandler.class;

    public ContextHandlerCollection() {
        super(true, new Handler[0]);
    }

    public ContextHandlerCollection(ContextHandler ... contexts) {
        super(true, new Handler[0]);
        this.setHandlers(contexts);
    }

    @ManagedOperation(value="Update the mapping of context path to context")
    public void mapContexts() {
        this._serializedExecutor.execute(() -> {
            HandlerCollection.Handlers handlers;
            while ((handlers = (HandlerCollection.Handlers)this._handlers.get()) != null && !this.updateHandlers(handlers, this.newHandlers(handlers.getHandlers()))) {
            }
        });
    }

    @Override
    protected HandlerCollection.Handlers newHandlers(Handler[] handlers) {
        Mapping mapping;
        if (handlers == null || handlers.length == 0) {
            return null;
        }
        HashMap<String, Branch[]> path2Branches = new HashMap<String, Branch[]>();
        for (Handler handler : handlers) {
            Branch branch = new Branch(handler);
            for (String contextPath : branch.getContextPaths()) {
                Branch[] branches = (Branch[])path2Branches.get(contextPath);
                path2Branches.put(contextPath, ArrayUtil.addToArray(branches, branch, Branch.class));
            }
        }
        for (Map.Entry entry : path2Branches.entrySet()) {
            Branch[] branches = (Branch[])entry.getValue();
            Branch[] sorted = new Branch[branches.length];
            int i = 0;
            for (Branch branch : branches) {
                if (!branch.hasVirtualHost()) continue;
                sorted[i++] = branch;
            }
            for (Branch branch : branches) {
                if (branch.hasVirtualHost()) continue;
                sorted[i++] = branch;
            }
            entry.setValue(sorted);
        }
        int capacity = 512;
        block5: while (true) {
            mapping = new Mapping(handlers, capacity);
            for (Map.Entry entry : path2Branches.entrySet()) {
                if (mapping._pathBranches.put(((String)entry.getKey()).substring(1), entry)) continue;
                capacity += 512;
                continue block5;
            }
            break;
        }
        if (LOG.isDebugEnabled()) {
            for (String ctx : mapping._pathBranches.keySet()) {
                LOG.debug("{}->{}", ctx, Arrays.asList((Branch[])((Map.Entry)mapping._pathBranches.get(ctx)).getValue()));
            }
        }
        Iterator<Object> iterator = path2Branches.values().iterator();
        while (iterator.hasNext()) {
            Branch[] branches;
            for (Branch branch : branches = (Branch[])iterator.next()) {
                for (ContextHandler context : branch.getContextHandlers()) {
                    mapping._contextBranches.put(context, branch.getHandler());
                }
            }
        }
        return mapping;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        ContextHandler context;
        Mapping mapping = (Mapping)this._handlers.get();
        if (mapping == null) {
            return;
        }
        Handler[] handlers = mapping.getHandlers();
        if (handlers == null || handlers.length == 0) {
            return;
        }
        if (handlers.length == 1) {
            handlers[0].handle(target, baseRequest, request, response);
            return;
        }
        HttpChannelState async = baseRequest.getHttpChannelState();
        if (async.isAsync() && (context = async.getContextHandler()) != null) {
            Handler branch = (Handler)mapping._contextBranches.get(context);
            if (branch == null) {
                context.handle(target, baseRequest, request, response);
            } else {
                branch.handle(target, baseRequest, request, response);
            }
            return;
        }
        if (target.startsWith("/")) {
            Map.Entry branches;
            Trie pathBranches = mapping._pathBranches;
            if (pathBranches == null) {
                return;
            }
            int limit = target.length() - 1;
            while (limit >= 0 && (branches = (Map.Entry)pathBranches.getBest(target, 1, limit)) != null) {
                int l = ((String)branches.getKey()).length();
                if (l == 1 || target.length() == l || target.charAt(l) == '/') {
                    for (Branch branch : (Branch[])branches.getValue()) {
                        branch.getHandler().handle(target, baseRequest, request, response);
                        if (!baseRequest.isHandled()) continue;
                        return;
                    }
                }
                limit = l - 2;
            }
        } else {
            for (Handler handler : handlers) {
                handler.handle(target, baseRequest, request, response);
                if (!baseRequest.isHandled()) continue;
                return;
            }
        }
    }

    @Deprecated
    public ContextHandler addContext(String contextPath, String resourceBase) {
        try {
            ContextHandler context = this._contextClass.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
            context.setContextPath(contextPath);
            context.setResourceBase(resourceBase);
            this.addHandler(context);
            return context;
        }
        catch (Exception e) {
            LOG.debug(e);
            throw new Error(e);
        }
    }

    public void deployHandler(final Handler handler, final Callback callback) {
        if (handler.getServer() != this.getServer()) {
            handler.setServer(this.getServer());
        }
        this._serializedExecutor.execute(new SerializedExecutor.ErrorHandlingTask(){

            @Override
            public void run() {
                ContextHandlerCollection.this.addHandler(handler);
                callback.succeeded();
            }

            @Override
            public void accept(Throwable throwable) {
                callback.failed(throwable);
            }
        });
    }

    public void undeployHandler(final Handler handler, final Callback callback) {
        this._serializedExecutor.execute(new SerializedExecutor.ErrorHandlingTask(){

            @Override
            public void run() {
                ContextHandlerCollection.this.removeHandler(handler);
                callback.succeeded();
            }

            @Override
            public void accept(Throwable throwable) {
                callback.failed(throwable);
            }
        });
    }

    @Deprecated
    public Class<?> getContextClass() {
        return this._contextClass;
    }

    @Deprecated
    public void setContextClass(Class<? extends ContextHandler> contextClass) {
        if (contextClass == null || !ContextHandler.class.isAssignableFrom(contextClass)) {
            throw new IllegalArgumentException();
        }
        this._contextClass = contextClass;
    }

    private static class Mapping
    extends HandlerCollection.Handlers {
        private final Map<ContextHandler, Handler> _contextBranches = new HashMap<ContextHandler, Handler>();
        private final Trie<Map.Entry<String, Branch[]>> _pathBranches;

        private Mapping(Handler[] handlers, int capacity) {
            super(handlers);
            this._pathBranches = new ArrayTernaryTrie<Map.Entry<String, Branch[]>>(false, capacity);
        }
    }

    private static final class Branch {
        private final Handler _handler;
        private final ContextHandler[] _contexts;

        Branch(Handler handler) {
            this._handler = handler;
            if (handler instanceof ContextHandler) {
                this._contexts = new ContextHandler[]{(ContextHandler)handler};
            } else if (handler instanceof HandlerContainer) {
                Handler[] contexts = ((HandlerContainer)((Object)handler)).getChildHandlersByClass(ContextHandler.class);
                this._contexts = new ContextHandler[contexts.length];
                System.arraycopy(contexts, 0, this._contexts, 0, contexts.length);
            } else {
                this._contexts = new ContextHandler[0];
            }
        }

        Set<String> getContextPaths() {
            HashSet<String> set = new HashSet<String>();
            for (ContextHandler context : this._contexts) {
                set.add(context.getContextPath());
            }
            return set;
        }

        boolean hasVirtualHost() {
            for (ContextHandler context : this._contexts) {
                if (context.getVirtualHosts() == null || context.getVirtualHosts().length <= 0) continue;
                return true;
            }
            return false;
        }

        ContextHandler[] getContextHandlers() {
            return this._contexts;
        }

        Handler getHandler() {
            return this._handler;
        }

        public String toString() {
            return String.format("{%s,%s}", this._handler, Arrays.asList(this._contexts));
        }
    }
}

