/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server.handler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.util.IncludeExclude;
import org.eclipse.jetty.util.IncludeExcludeSet;
import org.eclipse.jetty.util.InetAddressSet;
import org.eclipse.jetty.util.component.DumpableCollection;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class InetAccessHandler
extends HandlerWrapper {
    private static final Logger LOG = Log.getLogger(InetAccessHandler.class);
    private final IncludeExcludeSet<String, InetAddress> _addrs = new IncludeExcludeSet(InetAddressSet.class);
    private final IncludeExclude<String> _names = new IncludeExclude();

    public void clear() {
        this._addrs.clear();
        this._names.clear();
    }

    public void include(String pattern) {
        this._addrs.include(pattern);
    }

    public void include(String ... patterns) {
        this._addrs.include((String[])patterns);
    }

    public void exclude(String pattern) {
        this._addrs.exclude(pattern);
    }

    public void exclude(String ... patterns) {
        this._addrs.exclude((String[])patterns);
    }

    public void includeConnector(String name) {
        this._names.include(name);
    }

    public void excludeConnector(String name) {
        this._names.exclude(name);
    }

    public void includeConnectors(String ... names) {
        this._names.include((T[])names);
    }

    public void excludeConnectors(String ... names) {
        this._names.exclude((T[])names);
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        InetSocketAddress address;
        EndPoint endp;
        HttpChannel channel = baseRequest.getHttpChannel();
        if (channel != null && (endp = channel.getEndPoint()) != null && (address = endp.getRemoteAddress()) != null && !this.isAllowed(address.getAddress(), baseRequest, request)) {
            response.sendError(403);
            baseRequest.setHandled(true);
            return;
        }
        this.getHandler().handle(target, baseRequest, request, response);
    }

    protected boolean isAllowed(InetAddress addr, Request baseRequest, HttpServletRequest request) {
        String name = baseRequest.getHttpChannel().getConnector().getName();
        boolean filterAppliesToConnector = this._names.test(name);
        boolean allowedByAddr = this._addrs.test(addr);
        if (LOG.isDebugEnabled()) {
            LOG.debug("name = {}/{} addr={}/{} appliesToConnector={} allowedByAddr={}", name, this._names, addr, this._addrs, filterAppliesToConnector, allowedByAddr);
        }
        if (!filterAppliesToConnector) {
            return true;
        }
        return allowedByAddr;
    }

    @Override
    public void dump(Appendable out, String indent) throws IOException {
        this.dumpObjects(out, indent, new DumpableCollection("included", this._addrs.getIncluded()), new DumpableCollection("excluded", this._addrs.getExcluded()), new DumpableCollection("includedConnector", this._names.getIncluded()), new DumpableCollection("excludedConnector", this._names.getExcluded()));
    }
}

