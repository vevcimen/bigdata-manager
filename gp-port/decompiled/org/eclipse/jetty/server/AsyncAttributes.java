/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.jetty.util.Attributes;

class AsyncAttributes
extends Attributes.Wrapper {
    public static final String __ASYNC_PREFIX = "javax.servlet.async.";
    private String _requestURI;
    private String _contextPath;
    private String _servletPath;
    private String _pathInfo;
    private String _queryString;

    public AsyncAttributes(Attributes attributes, String requestUri, String contextPath, String servletPath, String pathInfo, String queryString) {
        super(attributes);
        this._requestURI = requestUri;
        this._contextPath = contextPath;
        this._servletPath = servletPath;
        this._pathInfo = pathInfo;
        this._queryString = queryString;
    }

    @Override
    public Object getAttribute(String key) {
        switch (key) {
            case "javax.servlet.async.request_uri": {
                return this._requestURI;
            }
            case "javax.servlet.async.context_path": {
                return this._contextPath;
            }
            case "javax.servlet.async.servlet_path": {
                return this._servletPath;
            }
            case "javax.servlet.async.path_info": {
                return this._pathInfo;
            }
            case "javax.servlet.async.query_string": {
                return this._queryString;
            }
        }
        return super.getAttribute(key);
    }

    @Override
    public Set<String> getAttributeNameSet() {
        HashSet<String> set = new HashSet<String>();
        super.getAttributeNameSet().stream().filter(name -> !name.startsWith(__ASYNC_PREFIX)).forEach(set::add);
        if (this._requestURI != null) {
            set.add("javax.servlet.async.request_uri");
        }
        if (this._contextPath != null) {
            set.add("javax.servlet.async.context_path");
        }
        if (this._servletPath != null) {
            set.add("javax.servlet.async.servlet_path");
        }
        if (this._pathInfo != null) {
            set.add("javax.servlet.async.path_info");
        }
        if (this._queryString != null) {
            set.add("javax.servlet.async.query_string");
        }
        return set;
    }

    @Override
    public void setAttribute(String key, Object value) {
        switch (key) {
            case "javax.servlet.async.request_uri": {
                this._requestURI = (String)value;
                break;
            }
            case "javax.servlet.async.context_path": {
                this._contextPath = (String)value;
                break;
            }
            case "javax.servlet.async.servlet_path": {
                this._servletPath = (String)value;
                break;
            }
            case "javax.servlet.async.path_info": {
                this._pathInfo = (String)value;
                break;
            }
            case "javax.servlet.async.query_string": {
                this._queryString = (String)value;
                break;
            }
            default: {
                super.setAttribute(key, value);
            }
        }
    }

    @Override
    public void clearAttributes() {
        this._requestURI = null;
        this._contextPath = null;
        this._servletPath = null;
        this._pathInfo = null;
        this._queryString = null;
        super.clearAttributes();
    }

    public static void applyAsyncAttributes(Attributes attributes, String requestURI, String contextPath, String servletPath, String pathInfo, String queryString) {
        if (requestURI != null) {
            attributes.setAttribute("javax.servlet.async.request_uri", requestURI);
        }
        if (contextPath != null) {
            attributes.setAttribute("javax.servlet.async.context_path", contextPath);
        }
        if (servletPath != null) {
            attributes.setAttribute("javax.servlet.async.servlet_path", servletPath);
        }
        if (pathInfo != null) {
            attributes.setAttribute("javax.servlet.async.path_info", pathInfo);
        }
        if (queryString != null) {
            attributes.setAttribute("javax.servlet.async.query_string", queryString);
        }
    }
}

