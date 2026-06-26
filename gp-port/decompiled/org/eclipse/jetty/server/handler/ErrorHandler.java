/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.http.QuotedQualityCSV;
import org.eclipse.jetty.io.ByteBufferOutputStream;
import org.eclipse.jetty.server.Dispatcher;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.BufferUtil;
import org.eclipse.jetty.util.QuotedStringTokenizer;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class ErrorHandler
extends AbstractHandler {
    private static final Logger LOG = Log.getLogger(ErrorHandler.class);
    public static final String ERROR_PAGE = "org.eclipse.jetty.server.error_page";
    public static final String ERROR_CONTEXT = "org.eclipse.jetty.server.error_context";
    boolean _showServlet = true;
    boolean _showStacks = true;
    boolean _disableStacks = false;
    boolean _showMessageInTitle = true;
    String _cacheControl = "must-revalidate,no-cache,no-store";

    public boolean errorPageForMethod(String method) {
        switch (method) {
            case "GET": 
            case "POST": 
            case "HEAD": {
                return true;
            }
        }
        return false;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        this.doError(target, baseRequest, request, response);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     */
    @Override
    public void doError(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        block11: {
            String cacheControl = this.getCacheControl();
            if (cacheControl != null) {
                response.setHeader(HttpHeader.CACHE_CONTROL.asString(), cacheControl);
            }
            String errorPage = this instanceof ErrorPageMapper ? ((ErrorPageMapper)((Object)this)).getErrorPage(request) : null;
            ContextHandler.Context context = baseRequest.getErrorContext();
            Dispatcher errorDispatcher = errorPage != null && context != null ? (Dispatcher)context.getRequestDispatcher(errorPage) : null;
            try {
                String message;
                block10: {
                    if (errorDispatcher != null) {
                        try {
                            errorDispatcher.error(request, response);
                            return;
                        }
                        catch (ServletException e) {
                            LOG.debug(e);
                            if (!response.isCommitted()) break block10;
                            baseRequest.setHandled(true);
                            return;
                        }
                    }
                }
                if ((message = (String)request.getAttribute("javax.servlet.error.message")) == null) {
                    message = baseRequest.getResponse().getReason();
                }
                this.generateAcceptableResponse(baseRequest, request, response, response.getStatus(), message);
                break block11;
                {
                    catch (Throwable throwable) {
                        throw throwable;
                    }
                }
            }
            finally {
                baseRequest.setHandled(true);
            }
        }
    }

    protected void generateAcceptableResponse(Request baseRequest, HttpServletRequest request, HttpServletResponse response, int code, String message) throws IOException {
        List<String> acceptable = baseRequest.getHttpFields().getQualityCSV(HttpHeader.ACCEPT, QuotedQualityCSV.MOST_SPECIFIC_MIME_ORDERING);
        if (acceptable.isEmpty() && !baseRequest.getHttpFields().contains(HttpHeader.ACCEPT)) {
            this.generateAcceptableResponse(baseRequest, request, response, code, message, MimeTypes.Type.TEXT_HTML.asString());
        } else {
            for (String mimeType : acceptable) {
                this.generateAcceptableResponse(baseRequest, request, response, code, message, mimeType);
                if (!response.isCommitted() && !baseRequest.getResponse().isWritingOrStreaming()) continue;
                break;
            }
        }
    }

    @Deprecated
    protected Writer getAcceptableWriter(Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<String> acceptable = baseRequest.getHttpFields().getQualityCSV(HttpHeader.ACCEPT_CHARSET);
        if (acceptable.isEmpty()) {
            response.setCharacterEncoding(StandardCharsets.ISO_8859_1.name());
            return response.getWriter();
        }
        for (String charset : acceptable) {
            try {
                if ("*".equals(charset)) {
                    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                } else {
                    response.setCharacterEncoding(Charset.forName(charset).name());
                }
                return response.getWriter();
            }
            catch (Exception e) {
                LOG.ignore(e);
            }
        }
        return null;
    }

    protected void generateAcceptableResponse(Request baseRequest, HttpServletRequest request, HttpServletResponse response, int code, String message, String contentType) throws IOException {
        MimeTypes.Type type;
        Charset charset = null;
        List<String> acceptable = baseRequest.getHttpFields().getQualityCSV(HttpHeader.ACCEPT_CHARSET);
        if (!acceptable.isEmpty()) {
            for (String name : acceptable) {
                if ("*".equals(name)) {
                    charset = StandardCharsets.UTF_8;
                    break;
                }
                try {
                    charset = Charset.forName(name);
                }
                catch (Exception e) {
                    LOG.ignore(e);
                }
            }
            if (charset == null) {
                return;
            }
        }
        switch (contentType) {
            case "text/html": 
            case "text/*": 
            case "*/*": {
                type = MimeTypes.Type.TEXT_HTML;
                if (charset != null) break;
                charset = StandardCharsets.ISO_8859_1;
                break;
            }
            case "text/json": 
            case "application/json": {
                type = MimeTypes.Type.TEXT_JSON;
                if (charset != null) break;
                charset = StandardCharsets.UTF_8;
                break;
            }
            case "text/plain": {
                type = MimeTypes.Type.TEXT_PLAIN;
                if (charset != null) break;
                charset = StandardCharsets.ISO_8859_1;
                break;
            }
            default: {
                return;
            }
        }
        while (true) {
            try {
                ByteBuffer buffer = baseRequest.getResponse().getHttpOutput().getBuffer();
                ByteBufferOutputStream out = new ByteBufferOutputStream(buffer);
                PrintWriter writer = new PrintWriter(new OutputStreamWriter((OutputStream)out, charset));
                switch (type) {
                    case TEXT_HTML: {
                        response.setContentType(MimeTypes.Type.TEXT_HTML.asString());
                        response.setCharacterEncoding(charset.name());
                        this.handleErrorPage(request, writer, code, message);
                        break;
                    }
                    case TEXT_JSON: {
                        response.setContentType(contentType);
                        this.writeErrorJson(request, writer, code, message);
                        break;
                    }
                    case TEXT_PLAIN: {
                        response.setContentType(MimeTypes.Type.TEXT_PLAIN.asString());
                        response.setCharacterEncoding(charset.name());
                        this.writeErrorPlain(request, writer, code, message);
                        break;
                    }
                    default: {
                        throw new IllegalStateException();
                    }
                }
                writer.flush();
            }
            catch (BufferOverflowException e) {
                LOG.warn("Error page too large: {} {} {}", code, message, request);
                if (LOG.isDebugEnabled()) {
                    LOG.warn(e);
                }
                baseRequest.getResponse().resetContent();
                if (this._disableStacks) break;
                LOG.info("Disabling showsStacks for " + this, new Object[0]);
                this._disableStacks = true;
                continue;
            }
            break;
        }
        baseRequest.getHttpChannel().sendResponseAndComplete();
    }

    protected void handleErrorPage(HttpServletRequest request, Writer writer, int code, String message) throws IOException {
        this.writeErrorPage(request, writer, code, message, this._showStacks);
    }

    protected void writeErrorPage(HttpServletRequest request, Writer writer, int code, String message, boolean showStacks) throws IOException {
        if (message == null) {
            message = HttpStatus.getMessage(code);
        }
        writer.write("<html>\n<head>\n");
        this.writeErrorPageHead(request, writer, code, message);
        writer.write("</head>\n<body>");
        this.writeErrorPageBody(request, writer, code, message, showStacks);
        writer.write("\n</body>\n</html>\n");
    }

    protected void writeErrorPageHead(HttpServletRequest request, Writer writer, int code, String message) throws IOException {
        writer.write("<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\"/>\n");
        writer.write("<title>Error ");
        String status = Integer.toString(code);
        writer.write(status);
        if (message != null && !message.equals(status)) {
            writer.write(32);
            writer.write(StringUtil.sanitizeXmlString(message));
        }
        writer.write("</title>\n");
    }

    protected void writeErrorPageBody(HttpServletRequest request, Writer writer, int code, String message, boolean showStacks) throws IOException {
        String uri = request.getRequestURI();
        this.writeErrorPageMessage(request, writer, code, message, uri);
        if (showStacks && !this._disableStacks) {
            this.writeErrorPageStacks(request, writer);
        }
        Request.getBaseRequest(request).getHttpChannel().getHttpConfiguration().writePoweredBy(writer, "<hr>", "<hr/>\n");
    }

    protected void writeErrorPageMessage(HttpServletRequest request, Writer writer, int code, String message, String uri) throws IOException {
        writer.write("<h2>HTTP ERROR ");
        String status = Integer.toString(code);
        writer.write(status);
        if (message != null && !message.equals(status)) {
            writer.write(32);
            writer.write(StringUtil.sanitizeXmlString(message));
        }
        writer.write("</h2>\n");
        writer.write("<table>\n");
        this.htmlRow(writer, "URI", uri);
        this.htmlRow(writer, "STATUS", status);
        this.htmlRow(writer, "MESSAGE", message);
        if (this.isShowServlet()) {
            this.htmlRow(writer, "SERVLET", request.getAttribute("javax.servlet.error.servlet_name"));
        }
        for (Throwable cause = (Throwable)request.getAttribute("javax.servlet.error.exception"); cause != null; cause = cause.getCause()) {
            this.htmlRow(writer, "CAUSED BY", cause);
        }
        writer.write("</table>\n");
    }

    private void htmlRow(Writer writer, String tag, Object value) throws IOException {
        writer.write("<tr><th>");
        writer.write(tag);
        writer.write(":</th><td>");
        if (value == null) {
            writer.write("-");
        } else {
            writer.write(StringUtil.sanitizeXmlString(value.toString()));
        }
        writer.write("</td></tr>\n");
    }

    private void writeErrorPlain(HttpServletRequest request, PrintWriter writer, int code, String message) {
        writer.write("HTTP ERROR ");
        writer.write(Integer.toString(code));
        writer.write(32);
        writer.write(StringUtil.sanitizeXmlString(message));
        writer.write("\n");
        writer.printf("URI: %s%n", request.getRequestURI());
        writer.printf("STATUS: %s%n", code);
        writer.printf("MESSAGE: %s%n", message);
        if (this.isShowServlet()) {
            writer.printf("SERVLET: %s%n", request.getAttribute("javax.servlet.error.servlet_name"));
        }
        for (Throwable cause = (Throwable)request.getAttribute("javax.servlet.error.exception"); cause != null; cause = cause.getCause()) {
            writer.printf("CAUSED BY %s%n", cause);
            if (!this.isShowStacks() || this._disableStacks) continue;
            cause.printStackTrace(writer);
        }
    }

    private void writeErrorJson(HttpServletRequest request, PrintWriter writer, int code, String message) {
        Throwable cause = (Throwable)request.getAttribute("javax.servlet.error.exception");
        Object servlet = request.getAttribute("javax.servlet.error.servlet_name");
        HashMap<String, String> json = new HashMap<String, String>();
        json.put("url", request.getRequestURI());
        json.put("status", Integer.toString(code));
        json.put("message", message);
        if (this.isShowServlet() && servlet != null) {
            json.put("servlet", servlet.toString());
        }
        int c = 0;
        while (cause != null) {
            json.put("cause" + c++, cause.toString());
            cause = cause.getCause();
        }
        writer.append(json.entrySet().stream().map(e -> QuotedStringTokenizer.quote((String)e.getKey()) + ":" + QuotedStringTokenizer.quote(StringUtil.sanitizeXmlString((String)e.getValue()))).collect(Collectors.joining(",\n", "{\n", "\n}")));
    }

    protected void writeErrorPageStacks(HttpServletRequest request, Writer writer) throws IOException {
        Throwable th = (Throwable)request.getAttribute("javax.servlet.error.exception");
        if (th != null) {
            writer.write("<h3>Caused by:</h3><pre>");
            try (StringWriter sw = new StringWriter();
                 PrintWriter pw = new PrintWriter(sw);){
                th.printStackTrace(pw);
                pw.flush();
                this.write(writer, sw.getBuffer().toString());
            }
            writer.write("</pre>\n");
        }
    }

    public ByteBuffer badMessageError(int status, String reason, HttpFields fields) {
        if (reason == null) {
            reason = HttpStatus.getMessage(status);
        }
        fields.put(HttpHeader.CONTENT_TYPE, MimeTypes.Type.TEXT_HTML_8859_1.asString());
        return BufferUtil.toBuffer("<h1>Bad Message " + status + "</h1><pre>reason: " + reason + "</pre>");
    }

    public String getCacheControl() {
        return this._cacheControl;
    }

    public void setCacheControl(String cacheControl) {
        this._cacheControl = cacheControl;
    }

    public boolean isShowServlet() {
        return this._showServlet;
    }

    public void setShowServlet(boolean showServlet) {
        this._showServlet = showServlet;
    }

    public boolean isShowStacks() {
        return this._showStacks;
    }

    public void setShowStacks(boolean showStacks) {
        this._showStacks = showStacks;
    }

    public void setShowMessageInTitle(boolean showMessageInTitle) {
        this._showMessageInTitle = showMessageInTitle;
    }

    public boolean getShowMessageInTitle() {
        return this._showMessageInTitle;
    }

    protected void write(Writer writer, String string) throws IOException {
        if (string == null) {
            return;
        }
        writer.write(StringUtil.sanitizeXmlString(string));
    }

    public static ErrorHandler getErrorHandler(Server server, ContextHandler context) {
        ErrorHandler errorHandler = null;
        if (context != null) {
            errorHandler = context.getErrorHandler();
        }
        if (errorHandler == null && server != null) {
            errorHandler = server.getBean(ErrorHandler.class);
        }
        return errorHandler;
    }

    public static interface ErrorPageMapper {
        public String getErrorPage(HttpServletRequest var1);
    }
}

