/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server;

import java.util.Set;
import org.eclipse.jetty.server.AsyncAttributes;
import org.eclipse.jetty.util.Attributes;
import org.eclipse.jetty.util.AttributesMap;

public class ServletAttributes
implements Attributes {
    private final Attributes _attributes = new AttributesMap();
    private AsyncAttributes _asyncAttributes;

    public void setAsyncAttributes(String requestURI, String contextPath, String servletPath, String pathInfo, String queryString) {
        this._asyncAttributes = new AsyncAttributes(this._attributes, requestURI, contextPath, servletPath, pathInfo, queryString);
    }

    private Attributes getAttributes() {
        return this._asyncAttributes == null ? this._attributes : this._asyncAttributes;
    }

    @Override
    public void removeAttribute(String name) {
        this.getAttributes().removeAttribute(name);
    }

    @Override
    public void setAttribute(String name, Object attribute) {
        this.getAttributes().setAttribute(name, attribute);
    }

    @Override
    public Object getAttribute(String name) {
        return this.getAttributes().getAttribute(name);
    }

    @Override
    public Set<String> getAttributeNameSet() {
        return this.getAttributes().getAttributeNameSet();
    }

    @Override
    public void clearAttributes() {
        this.getAttributes().clearAttributes();
        this._asyncAttributes = null;
    }
}

