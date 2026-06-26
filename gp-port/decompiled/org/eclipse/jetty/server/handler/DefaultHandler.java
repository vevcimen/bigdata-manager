/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server.handler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.resource.Resource;

public class DefaultHandler
extends AbstractHandler {
    private static final Logger LOG = Log.getLogger(DefaultHandler.class);
    final long _faviconModified = System.currentTimeMillis() / 1000L * 1000L;
    final byte[] _favicon;
    boolean _serveIcon = true;
    boolean _showContexts = true;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public DefaultHandler() {
        byte[] favbytes = null;
        try {
            URL fav = this.getClass().getClassLoader().getResource("org/eclipse/jetty/favicon.ico");
            if (fav != null) {
                Resource r = Resource.newResource(fav);
                favbytes = IO.readBytes(r.getInputStream());
            }
        }
        catch (Exception e) {
            LOG.warn(e);
        }
        finally {
            this._favicon = favbytes;
        }
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (response.isCommitted() || baseRequest.isHandled()) {
            return;
        }
        baseRequest.setHandled(true);
        String method = request.getMethod();
        if (this._serveIcon && this._favicon != null && HttpMethod.GET.is(method) && target.equals("/favicon.ico")) {
            if (request.getDateHeader(HttpHeader.IF_MODIFIED_SINCE.toString()) == this._faviconModified) {
                response.setStatus(304);
            } else {
                response.setStatus(200);
                response.setContentType("image/x-icon");
                response.setContentLength(this._favicon.length);
                response.setDateHeader(HttpHeader.LAST_MODIFIED.toString(), this._faviconModified);
                response.setHeader(HttpHeader.CACHE_CONTROL.toString(), "max-age=360000,public");
                response.getOutputStream().write(this._favicon);
            }
            return;
        }
        if (!(this._showContexts && HttpMethod.GET.is(method) && request.getRequestURI().equals("/"))) {
            response.sendError(404);
            return;
        }
        response.setStatus(404);
        response.setContentType(MimeTypes.Type.TEXT_HTML_UTF_8.toString());
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter((OutputStream)outputStream, StandardCharsets.UTF_8);){
            writer.append("<!DOCTYPE html>\n");
            writer.append("<html lang=\"en\">\n<head>\n");
            writer.append("<title>Error 404 - Not Found</title>\n");
            writer.append("<meta charset=\"utf-8\">\n");
            writer.append("<style>body { font-family: sans-serif; } table, td { border: 1px solid #333; } td, th { padding: 5px; } thead, tfoot { background-color: #333; color: #fff; } </style>\n");
            writer.append("</head>\n<body>\n");
            writer.append("<h2>Error 404 - Not Found.</h2>\n");
            writer.append("<p>No context on this server matched or handled this request.</p>\n");
            writer.append("<p>Contexts known to this server are:</p>\n");
            Server server = this.getServer();
            Handler[] handlers = server == null ? null : server.getChildHandlersByClass(ContextHandler.class);
            writer.append("<table class=\"contexts\"><thead><tr>");
            writer.append("<th>Context Path</th>");
            writer.append("<th>Display Name</th>");
            writer.append("<th>Status</th>");
            writer.append("<th>LifeCycle</th>");
            writer.append("</tr></thead><tbody>\n");
            for (int i = 0; handlers != null && i < handlers.length; ++i) {
                writer.append("<tr><td>");
                ContextHandler context = (ContextHandler)handlers[i];
                String contextPath = context.getContextPath();
                String href = URIUtil.encodePath(contextPath);
                if (contextPath.length() > 1 && !contextPath.endsWith("/")) {
                    href = href + '/';
                }
                if (context.isRunning()) {
                    writer.append("<a href=\"").append(href).append("\">");
                }
                writer.append(StringUtil.replace(contextPath, "%", "&#37;"));
                if (context.isRunning()) {
                    writer.append("</a>");
                }
                writer.append("</td><td>");
                if (StringUtil.isNotBlank(context.getDisplayName())) {
                    writer.append(StringUtil.sanitizeXmlString(context.getDisplayName()));
                }
                writer.append("&nbsp;</td><td>");
                if (context.isAvailable()) {
                    writer.append("Available");
                } else {
                    writer.append("<em>Not</em> Available");
                }
                writer.append("</td><td>");
                writer.append(context.getState());
                writer.append("</td></tr>\n");
            }
            writer.append("</tbody></table><hr/>\n");
            writer.append("<a href=\"https://eclipse.org/jetty\"><img alt=\"icon\" src=\"/favicon.ico\"/></a>&nbsp;");
            writer.append("<a href=\"https://eclipse.org/jetty\">Powered by Eclipse Jetty:// Server</a><hr/>\n");
            writer.append("</body>\n</html>\n");
            writer.flush();
            byte[] content = outputStream.toByteArray();
            response.setContentLength(content.length);
            try (ServletOutputStream out = response.getOutputStream();){
                out.write(content);
            }
        }
    }

    public boolean getServeIcon() {
        return this._serveIcon;
    }

    public void setServeIcon(boolean serveIcon) {
        this._serveIcon = serveIcon;
    }

    public boolean getShowContexts() {
        return this._showContexts;
    }

    public void setShowContexts(boolean show) {
        this._showContexts = show;
    }
}

