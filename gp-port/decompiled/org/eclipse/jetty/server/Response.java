/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.IllegalSelectorException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.function.Supplier;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.ServletResponseWrapper;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;
import org.eclipse.jetty.http.CookieCompliance;
import org.eclipse.jetty.http.DateGenerator;
import org.eclipse.jetty.http.HttpContent;
import org.eclipse.jetty.http.HttpCookie;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpGenerator;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpHeaderValue;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http.MetaData;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.http.PreEncodedHttpField;
import org.eclipse.jetty.io.RuntimeIOException;
import org.eclipse.jetty.server.EncodingHttpWriter;
import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.HttpOutput;
import org.eclipse.jetty.server.Iso88591HttpWriter;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.ResponseWriter;
import org.eclipse.jetty.server.Utf8HttpWriter;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.util.AtomicBiInteger;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class Response
implements HttpServletResponse {
    private static final Logger LOG = Log.getLogger(Response.class);
    private static final int __MIN_BUFFER_SIZE = 1;
    private static final HttpField __EXPIRES_01JAN1970 = new PreEncodedHttpField(HttpHeader.EXPIRES, DateGenerator.__01Jan1970);
    public static final int NO_CONTENT_LENGTH = -1;
    public static final int USE_KNOWN_CONTENT_LENGTH = -2;
    public static final String SET_INCLUDE_HEADER_PREFIX = "org.eclipse.jetty.server.include.";
    private final HttpChannel _channel;
    private final HttpFields _fields = new HttpFields();
    private final AtomicBiInteger _errorSentAndIncludes = new AtomicBiInteger();
    private final HttpOutput _out;
    private int _status = 200;
    private String _reason;
    private Locale _locale;
    private MimeTypes.Type _mimeType;
    private String _characterEncoding;
    private EncodingFrom _encodingFrom = EncodingFrom.NOT_SET;
    private String _contentType;
    private OutputType _outputType = OutputType.NONE;
    private ResponseWriter _writer;
    private long _contentLength = -1L;
    private Supplier<HttpFields> _trailers;
    private static final EnumSet<EncodingFrom> __localeOverride = EnumSet.of(EncodingFrom.NOT_SET, EncodingFrom.INFERRED);
    private static final EnumSet<EncodingFrom> __explicitCharset = EnumSet.of(EncodingFrom.SET_LOCALE, EncodingFrom.SET_CHARACTER_ENCODING);

    public Response(HttpChannel channel, HttpOutput out) {
        this._channel = channel;
        this._out = out;
    }

    public HttpChannel getHttpChannel() {
        return this._channel;
    }

    protected void recycle() {
        this._fields.clear();
        this._errorSentAndIncludes.set(0L);
        this._out.recycle();
        this._status = 200;
        this._reason = null;
        this._locale = null;
        this._mimeType = null;
        this._characterEncoding = null;
        this._encodingFrom = EncodingFrom.NOT_SET;
        this._contentType = null;
        this._outputType = OutputType.NONE;
        this._contentLength = -1L;
        this._trailers = null;
    }

    public HttpOutput getHttpOutput() {
        return this._out;
    }

    public void reopen() {
        this.setErrorSent(false);
        this._out.reopen();
    }

    public void errorClose() {
        this.setErrorSent(true);
        this._out.softClose();
    }

    private boolean isMutable() {
        return this._errorSentAndIncludes.get() == 0L;
    }

    private void setErrorSent(boolean errorSent) {
        this._errorSentAndIncludes.getAndSetHi(errorSent ? 1 : 0);
    }

    public boolean isIncluding() {
        return this._errorSentAndIncludes.getLo() > 0;
    }

    public void include() {
        this._errorSentAndIncludes.add(0, 1);
    }

    public void included() {
        this._errorSentAndIncludes.add(0, -1);
        if (this._outputType == OutputType.WRITER) {
            this._writer.reopen();
        }
        this._out.reopen();
    }

    public void addCookie(HttpCookie cookie) {
        if (StringUtil.isBlank(cookie.getName())) {
            throw new IllegalArgumentException("Cookie.name cannot be blank/null");
        }
        this._fields.add(new HttpCookie.SetCookieHttpField(this.checkSameSite(cookie), this.getHttpChannel().getHttpConfiguration().getResponseCookieCompliance()));
        this._fields.put(__EXPIRES_01JAN1970);
    }

    private HttpCookie checkSameSite(HttpCookie cookie) {
        if (cookie == null || cookie.getSameSite() != null) {
            return cookie;
        }
        HttpCookie.SameSite contextDefault = HttpCookie.getSameSiteDefault(this._channel.getRequest().getServletContext());
        if (contextDefault == null) {
            return cookie;
        }
        return new HttpCookie(cookie.getName(), cookie.getValue(), cookie.getDomain(), cookie.getPath(), cookie.getMaxAge(), cookie.isHttpOnly(), cookie.isSecure(), cookie.getComment(), cookie.getVersion(), contextDefault);
    }

    public void replaceCookie(HttpCookie cookie) {
        ListIterator<HttpField> i = this._fields.listIterator();
        while (i.hasNext()) {
            HttpField field = i.next();
            if (field.getHeader() != HttpHeader.SET_COOKIE) continue;
            CookieCompliance compliance = this.getHttpChannel().getHttpConfiguration().getResponseCookieCompliance();
            HttpCookie oldCookie = field instanceof HttpCookie.SetCookieHttpField ? ((HttpCookie.SetCookieHttpField)field).getHttpCookie() : new HttpCookie(field.getValue());
            if (!cookie.getName().equals(oldCookie.getName()) || (cookie.getDomain() != null ? !cookie.getDomain().equalsIgnoreCase(oldCookie.getDomain()) : oldCookie.getDomain() != null) || (cookie.getPath() == null ? oldCookie.getPath() != null : !cookie.getPath().equals(oldCookie.getPath()))) continue;
            i.set(new HttpCookie.SetCookieHttpField(this.checkSameSite(cookie), compliance));
            return;
        }
        this.addCookie(cookie);
    }

    @Override
    public void addCookie(Cookie cookie) {
        if (this.isMutable()) {
            if (StringUtil.isBlank(cookie.getName())) {
                throw new IllegalArgumentException("Cookie.name cannot be blank/null");
            }
            String comment = cookie.getComment();
            boolean httpOnly = cookie.isHttpOnly() || HttpCookie.isHttpOnlyInComment(comment);
            HttpCookie.SameSite sameSite = HttpCookie.getSameSiteFromComment(comment);
            comment = HttpCookie.getCommentWithoutAttributes(comment);
            this.addCookie(new HttpCookie(cookie.getName(), cookie.getValue(), cookie.getDomain(), cookie.getPath(), cookie.getMaxAge(), httpOnly, cookie.getSecure(), comment, cookie.getVersion(), sameSite));
        }
    }

    @Override
    public boolean containsHeader(String name) {
        return this._fields.containsKey(name);
    }

    @Override
    public String encodeURL(String url) {
        int prefix;
        String sessionURLPrefix;
        Request request = this._channel.getRequest();
        SessionHandler sessionManager = request.getSessionHandler();
        if (sessionManager == null) {
            return url;
        }
        HttpURI uri = null;
        if (sessionManager.isCheckingRemoteSessionIdEncoding() && URIUtil.hasScheme(url)) {
            uri = new HttpURI(url);
            String path = uri.getPath();
            path = path == null ? "" : path;
            int port = uri.getPort();
            if (port < 0) {
                int n = port = HttpScheme.HTTPS.asString().equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
            }
            if (!request.getServerName().equalsIgnoreCase(uri.getHost())) {
                return url;
            }
            if (request.getServerPort() != port) {
                return url;
            }
            if (!path.startsWith(request.getContextPath())) {
                return url;
            }
        }
        if ((sessionURLPrefix = sessionManager.getSessionIdPathParameterNamePrefix()) == null) {
            return url;
        }
        if (url == null) {
            return null;
        }
        if (sessionManager.isUsingCookies() && request.isRequestedSessionIdFromCookie() || !sessionManager.isUsingURLs()) {
            int prefix2 = url.indexOf(sessionURLPrefix);
            if (prefix2 != -1) {
                int suffix = url.indexOf("?", prefix2);
                if (suffix < 0) {
                    suffix = url.indexOf("#", prefix2);
                }
                if (suffix <= prefix2) {
                    return url.substring(0, prefix2);
                }
                return url.substring(0, prefix2) + url.substring(suffix);
            }
            return url;
        }
        HttpSession session = request.getSession(false);
        if (session == null) {
            return url;
        }
        if (!sessionManager.isValid(session)) {
            return url;
        }
        String id = sessionManager.getExtendedId(session);
        if (uri == null) {
            uri = new HttpURI(url);
        }
        if ((prefix = url.indexOf(sessionURLPrefix)) != -1) {
            int suffix = url.indexOf("?", prefix);
            if (suffix < 0) {
                suffix = url.indexOf("#", prefix);
            }
            if (suffix <= prefix) {
                return url.substring(0, prefix + sessionURLPrefix.length()) + id;
            }
            return url.substring(0, prefix + sessionURLPrefix.length()) + id + url.substring(suffix);
        }
        int suffix = url.indexOf(63);
        if (suffix < 0) {
            suffix = url.indexOf(35);
        }
        if (suffix < 0) {
            return url + ((HttpScheme.HTTPS.is(uri.getScheme()) || HttpScheme.HTTP.is(uri.getScheme())) && uri.getPath() == null ? "/" : "") + sessionURLPrefix + id;
        }
        return url.substring(0, suffix) + ((HttpScheme.HTTPS.is(uri.getScheme()) || HttpScheme.HTTP.is(uri.getScheme())) && uri.getPath() == null ? "/" : "") + sessionURLPrefix + id + url.substring(suffix);
    }

    @Override
    public String encodeRedirectURL(String url) {
        return this.encodeURL(url);
    }

    @Override
    @Deprecated
    public String encodeUrl(String url) {
        return this.encodeURL(url);
    }

    @Override
    @Deprecated
    public String encodeRedirectUrl(String url) {
        return this.encodeRedirectURL(url);
    }

    @Override
    public void sendError(int sc) throws IOException {
        this.sendError(sc, null);
    }

    @Override
    public void sendError(int code, String message) throws IOException {
        if (this.isIncluding()) {
            return;
        }
        switch (code) {
            case -1: {
                this._channel.abort(new IOException(message));
                break;
            }
            case 102: {
                this.sendProcessing();
                break;
            }
            default: {
                this._channel.getState().sendError(code, message);
            }
        }
    }

    public void sendProcessing() throws IOException {
        if (this._channel.isExpecting102Processing() && !this.isCommitted()) {
            this._channel.sendResponse(HttpGenerator.PROGRESS_102_INFO, null, true);
        }
    }

    public void sendRedirect(int code, String location) throws IOException {
        this.sendRedirect(code, location, false);
    }

    public void sendRedirect(String location, boolean consumeAll) throws IOException {
        this.sendRedirect(this.getHttpChannel().getRequest().getHttpVersion().getVersion() < HttpVersion.HTTP_1_1.getVersion() ? 302 : 303, location, consumeAll);
    }

    public void sendRedirect(int code, String location, boolean consumeAll) throws IOException {
        if (consumeAll) {
            this.getHttpChannel().ensureConsumeAllOrNotPersistent();
        }
        if (!HttpStatus.isRedirection(code)) {
            throw new IllegalArgumentException("Not a 3xx redirect code");
        }
        if (!this.isMutable()) {
            return;
        }
        if (location == null) {
            throw new IllegalArgumentException();
        }
        if (!URIUtil.hasScheme(location)) {
            StringBuilder buf;
            StringBuilder stringBuilder = buf = this._channel.getHttpConfiguration().isRelativeRedirectAllowed() ? new StringBuilder() : this._channel.getRequest().getRootURL();
            if (location.startsWith("/")) {
                location = URIUtil.canonicalEncodedPath(location);
            } else {
                String path = this._channel.getRequest().getRequestURI();
                String parent = path.endsWith("/") ? path : URIUtil.parentPath(path);
                location = URIUtil.canonicalEncodedPath(URIUtil.addEncodedPaths(parent, location));
                if (location != null && !location.startsWith("/")) {
                    buf.append('/');
                }
            }
            if (location == null) {
                throw new IllegalStateException("path cannot be above root");
            }
            buf.append(location);
            location = buf.toString();
        }
        this.resetBuffer();
        this.setHeader(HttpHeader.LOCATION, location);
        this.setStatus(code);
        this.closeOutput();
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        this.sendRedirect(302, location);
    }

    @Override
    public void setDateHeader(String name, long date) {
        if (this.isMutable()) {
            this._fields.putDateField(name, date);
        }
    }

    @Override
    public void addDateHeader(String name, long date) {
        if (this.isMutable()) {
            this._fields.addDateField(name, date);
        }
    }

    public void setHeader(HttpHeader name, String value) {
        if (this.isMutable()) {
            if (HttpHeader.CONTENT_TYPE == name) {
                this.setContentType(value);
            } else {
                this._fields.put(name, value);
                if (HttpHeader.CONTENT_LENGTH == name) {
                    this._contentLength = value == null ? -1L : Long.parseLong(value);
                }
            }
        }
    }

    @Override
    public void setHeader(String name, String value) {
        long biInt = this._errorSentAndIncludes.get();
        if (biInt != 0L) {
            boolean including;
            boolean errorSent = AtomicBiInteger.getHi(biInt) != 0;
            boolean bl = including = AtomicBiInteger.getLo(biInt) > 0;
            if (!errorSent && including && name.startsWith(SET_INCLUDE_HEADER_PREFIX)) {
                name = name.substring(SET_INCLUDE_HEADER_PREFIX.length());
            } else {
                return;
            }
        }
        if (HttpHeader.CONTENT_TYPE.is(name)) {
            this.setContentType(value);
        } else {
            this._fields.put(name, value);
            if (HttpHeader.CONTENT_LENGTH.is(name)) {
                this._contentLength = value == null ? -1L : Long.parseLong(value);
            }
        }
    }

    @Override
    public Collection<String> getHeaderNames() {
        return this._fields.getFieldNamesCollection();
    }

    @Override
    public String getHeader(String name) {
        return this._fields.get(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        List<String> i = this._fields.getValuesList(name);
        if (i == null) {
            return Collections.emptyList();
        }
        return i;
    }

    @Override
    public void addHeader(String name, String value) {
        long biInt = this._errorSentAndIncludes.get();
        if (biInt != 0L) {
            boolean including;
            boolean errorSent = AtomicBiInteger.getHi(biInt) != 0;
            boolean bl = including = AtomicBiInteger.getLo(biInt) > 0;
            if (!errorSent && including && name.startsWith(SET_INCLUDE_HEADER_PREFIX)) {
                name = name.substring(SET_INCLUDE_HEADER_PREFIX.length());
            } else {
                return;
            }
        }
        if (HttpHeader.CONTENT_TYPE.is(name)) {
            this.setContentType(value);
            return;
        }
        if (HttpHeader.CONTENT_LENGTH.is(name)) {
            this.setHeader(name, value);
            return;
        }
        this._fields.add(name, value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        if (this.isMutable()) {
            this._fields.putLongField(name, (long)value);
            if (HttpHeader.CONTENT_LENGTH.is(name)) {
                this._contentLength = value;
            }
        }
    }

    @Override
    public void addIntHeader(String name, int value) {
        if (this.isMutable()) {
            this._fields.add(name, Integer.toString(value));
            if (HttpHeader.CONTENT_LENGTH.is(name)) {
                this._contentLength = value;
            }
        }
    }

    @Override
    public void setStatus(int sc) {
        if (sc <= 0) {
            throw new IllegalArgumentException();
        }
        if (this.isMutable()) {
            if (this._status != sc) {
                this._reason = null;
            }
            this._status = sc;
        }
    }

    @Override
    @Deprecated
    public void setStatus(int sc, String sm) {
        this.setStatusWithReason(sc, sm);
    }

    public void setStatusWithReason(int sc, String sm) {
        if (sc <= 0) {
            throw new IllegalArgumentException();
        }
        if (this.isMutable()) {
            this._status = sc;
            this._reason = sm;
        }
    }

    @Override
    public String getCharacterEncoding() {
        if (this._characterEncoding == null) {
            String encoding = MimeTypes.getCharsetAssumedFromContentType(this._contentType);
            if (encoding != null) {
                return encoding;
            }
            encoding = MimeTypes.getCharsetInferredFromContentType(this._contentType);
            if (encoding != null) {
                return encoding;
            }
            return "iso-8859-1";
        }
        return this._characterEncoding;
    }

    @Override
    public String getContentType() {
        return this._contentType;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (this._outputType == OutputType.WRITER) {
            throw new IllegalStateException("WRITER");
        }
        this._outputType = OutputType.STREAM;
        return this._out;
    }

    public boolean isWriting() {
        return this._outputType == OutputType.WRITER;
    }

    public boolean isStreaming() {
        return this._outputType == OutputType.STREAM;
    }

    public boolean isWritingOrStreaming() {
        return this.isWriting() || this.isStreaming();
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (this._outputType == OutputType.STREAM) {
            throw new IllegalStateException("STREAM");
        }
        if (this._outputType == OutputType.NONE) {
            String encoding = this._characterEncoding;
            if (encoding == null) {
                if (this._mimeType != null && this._mimeType.isCharsetAssumed()) {
                    encoding = this._mimeType.getCharsetString();
                } else {
                    encoding = MimeTypes.getCharsetAssumedFromContentType(this._contentType);
                    if (encoding == null) {
                        encoding = MimeTypes.getCharsetInferredFromContentType(this._contentType);
                        if (encoding == null) {
                            encoding = "iso-8859-1";
                        }
                        this.setCharacterEncoding(encoding, EncodingFrom.INFERRED);
                    }
                }
            }
            Locale locale = this.getLocale();
            if (this._writer != null && this._writer.isFor(locale, encoding)) {
                this._writer.reopen();
            } else {
                this._writer = "iso-8859-1".equalsIgnoreCase(encoding) ? new ResponseWriter(new Iso88591HttpWriter(this._out), locale, encoding) : ("utf-8".equalsIgnoreCase(encoding) ? new ResponseWriter(new Utf8HttpWriter(this._out), locale, encoding) : new ResponseWriter(new EncodingHttpWriter(this._out, encoding), locale, encoding));
            }
            this._outputType = OutputType.WRITER;
        }
        return this._writer;
    }

    @Override
    public void setContentLength(int len) {
        if (this.isCommitted() || !this.isMutable()) {
            return;
        }
        if (len > 0) {
            long written = this._out.getWritten();
            if (written > (long)len) {
                throw new IllegalArgumentException("setContentLength(" + len + ") when already written " + written);
            }
            this._contentLength = len;
            this._fields.putLongField(HttpHeader.CONTENT_LENGTH, (long)len);
            if (this.isAllContentWritten(written)) {
                try {
                    this.closeOutput();
                }
                catch (IOException e) {
                    throw new RuntimeIOException(e);
                }
            }
        } else if (len == 0) {
            long written = this._out.getWritten();
            if (written > 0L) {
                throw new IllegalArgumentException("setContentLength(0) when already written " + written);
            }
            this._contentLength = len;
            this._fields.put(HttpHeader.CONTENT_LENGTH, "0");
        } else {
            this._contentLength = len;
            this._fields.remove(HttpHeader.CONTENT_LENGTH);
        }
    }

    public long getContentLength() {
        return this._contentLength;
    }

    public boolean isAllContentWritten(long written) {
        return this._contentLength >= 0L && written >= this._contentLength;
    }

    public boolean isContentComplete(long written) {
        return this._contentLength < 0L || written >= this._contentLength;
    }

    public void closeOutput() throws IOException {
        if (this._outputType == OutputType.WRITER) {
            this._writer.close();
        } else {
            this._out.close();
        }
    }

    @Deprecated
    public void completeOutput() throws IOException {
        this.closeOutput();
    }

    public void completeOutput(Callback callback) {
        if (this._outputType == OutputType.WRITER) {
            this._writer.complete(callback);
        } else {
            this._out.complete(callback);
        }
    }

    public long getLongContentLength() {
        return this._contentLength;
    }

    public void setLongContentLength(long len) {
        if (this.isCommitted() || !this.isMutable()) {
            return;
        }
        this._contentLength = len;
        this._fields.putLongField(HttpHeader.CONTENT_LENGTH.toString(), len);
    }

    @Override
    public void setContentLengthLong(long length) {
        this.setLongContentLength(length);
    }

    @Override
    public void setCharacterEncoding(String encoding) {
        this.setCharacterEncoding(encoding, EncodingFrom.SET_CHARACTER_ENCODING);
    }

    private void setCharacterEncoding(String encoding, EncodingFrom from) {
        if (!this.isMutable() || this.isWriting()) {
            return;
        }
        if (this._outputType != OutputType.WRITER && !this.isCommitted()) {
            if (encoding == null) {
                this._encodingFrom = EncodingFrom.NOT_SET;
                if (this._characterEncoding != null) {
                    this._characterEncoding = null;
                    if (this._mimeType != null) {
                        this._mimeType = this._mimeType.getBaseType();
                        this._contentType = this._mimeType.asString();
                        this._fields.put(this._mimeType.getContentTypeField());
                    } else if (this._contentType != null) {
                        this._contentType = MimeTypes.getContentTypeWithoutCharset(this._contentType);
                        this._fields.put(HttpHeader.CONTENT_TYPE, this._contentType);
                    }
                }
            } else {
                this._encodingFrom = from;
                String string = this._characterEncoding = HttpGenerator.__STRICT ? encoding : StringUtil.normalizeCharset(encoding);
                if (this._mimeType != null) {
                    this._contentType = this._mimeType.getBaseType().asString() + ";charset=" + this._characterEncoding;
                    this._mimeType = MimeTypes.CACHE.get(this._contentType);
                    if (this._mimeType == null || HttpGenerator.__STRICT) {
                        this._fields.put(HttpHeader.CONTENT_TYPE, this._contentType);
                    } else {
                        this._fields.put(this._mimeType.getContentTypeField());
                    }
                } else if (this._contentType != null) {
                    this._contentType = MimeTypes.getContentTypeWithoutCharset(this._contentType) + ";charset=" + this._characterEncoding;
                    this._fields.put(HttpHeader.CONTENT_TYPE, this._contentType);
                }
            }
        }
    }

    @Override
    public void setContentType(String contentType) {
        if (this.isCommitted() || !this.isMutable()) {
            return;
        }
        if (contentType == null) {
            if (this.isWriting() && this._characterEncoding != null) {
                throw new IllegalSelectorException();
            }
            if (this._locale == null) {
                this._characterEncoding = null;
            }
            this._mimeType = null;
            this._contentType = null;
            this._fields.remove(HttpHeader.CONTENT_TYPE);
        } else {
            this._contentType = contentType;
            this._mimeType = MimeTypes.CACHE.get(contentType);
            String charset = this._mimeType != null && this._mimeType.getCharset() != null && !this._mimeType.isCharsetAssumed() ? this._mimeType.getCharsetString() : MimeTypes.getCharsetFromContentType(contentType);
            if (charset == null) {
                switch (this._encodingFrom) {
                    case NOT_SET: {
                        break;
                    }
                    case INFERRED: 
                    case SET_CONTENT_TYPE: {
                        if (this.isWriting()) {
                            this._mimeType = null;
                            this._contentType = this._contentType + ";charset=" + this._characterEncoding;
                            break;
                        }
                        this._encodingFrom = EncodingFrom.NOT_SET;
                        this._characterEncoding = null;
                        break;
                    }
                    case SET_LOCALE: 
                    case SET_CHARACTER_ENCODING: {
                        this._contentType = contentType + ";charset=" + this._characterEncoding;
                        this._mimeType = null;
                    }
                }
            } else if (this.isWriting() && !charset.equalsIgnoreCase(this._characterEncoding)) {
                this._mimeType = null;
                this._contentType = MimeTypes.getContentTypeWithoutCharset(this._contentType);
                if (this._characterEncoding != null) {
                    this._contentType = this._contentType + ";charset=" + this._characterEncoding;
                }
            } else {
                this._characterEncoding = charset;
                this._encodingFrom = EncodingFrom.SET_CONTENT_TYPE;
            }
            if (HttpGenerator.__STRICT || this._mimeType == null) {
                this._fields.put(HttpHeader.CONTENT_TYPE, this._contentType);
            } else {
                this._contentType = this._mimeType.asString();
                this._fields.put(this._mimeType.getContentTypeField());
            }
        }
    }

    @Override
    public void setBufferSize(int size) {
        if (this.isCommitted()) {
            throw new IllegalStateException("cannot set buffer size after response is in committed state");
        }
        if (this.getContentCount() > 0L) {
            throw new IllegalStateException("cannot set buffer size after response has " + this.getContentCount() + " bytes already written");
        }
        if (size < 1) {
            size = 1;
        }
        this._out.setBufferSize(size);
    }

    @Override
    public int getBufferSize() {
        return this._out.getBufferSize();
    }

    @Override
    public void flushBuffer() throws IOException {
        if (!this._out.isClosed()) {
            this._out.flush();
        }
    }

    @Override
    public void reset() {
        HttpCookie c;
        SessionHandler sh;
        this._status = 200;
        this._reason = null;
        this._out.resetBuffer();
        this._outputType = OutputType.NONE;
        this._contentLength = -1L;
        this._contentType = null;
        this._mimeType = null;
        this._characterEncoding = null;
        this._encodingFrom = EncodingFrom.NOT_SET;
        this._trailers = null;
        this._fields.clear();
        for (String value : this._channel.getRequest().getHttpFields().getCSV(HttpHeader.CONNECTION, false)) {
            HttpHeaderValue cb = HttpHeaderValue.CACHE.get(value);
            if (cb == null) continue;
            switch (cb) {
                case CLOSE: {
                    this._fields.put(HttpHeader.CONNECTION, HttpHeaderValue.CLOSE.toString());
                    break;
                }
                case KEEP_ALIVE: {
                    if (!HttpVersion.HTTP_1_0.is(this._channel.getRequest().getProtocol())) break;
                    this._fields.put(HttpHeader.CONNECTION, HttpHeaderValue.KEEP_ALIVE.toString());
                    break;
                }
                case TE: {
                    this._fields.put(HttpHeader.CONNECTION, HttpHeaderValue.TE.toString());
                    break;
                }
            }
        }
        Request request = this.getHttpChannel().getRequest();
        HttpSession session = request.getSession(false);
        if (session != null && session.isNew() && (sh = request.getSessionHandler()) != null && (c = sh.getSessionCookie(session, request.getContextPath(), request.isSecure())) != null) {
            this.addCookie(c);
        }
    }

    public void resetContent() {
        this._out.resetBuffer();
        this._outputType = OutputType.NONE;
        this._contentLength = -1L;
        this._contentType = null;
        this._mimeType = null;
        this._characterEncoding = null;
        this._encodingFrom = EncodingFrom.NOT_SET;
        Iterator<HttpField> i = this.getHttpFields().iterator();
        block3: while (i.hasNext()) {
            HttpField field = i.next();
            if (field.getHeader() == null) continue;
            switch (field.getHeader()) {
                case CONTENT_TYPE: 
                case CONTENT_LENGTH: 
                case CONTENT_ENCODING: 
                case CONTENT_LANGUAGE: 
                case CONTENT_RANGE: 
                case CONTENT_MD5: 
                case CONTENT_LOCATION: 
                case TRANSFER_ENCODING: 
                case CACHE_CONTROL: 
                case LAST_MODIFIED: 
                case EXPIRES: 
                case ETAG: 
                case DATE: 
                case VARY: {
                    i.remove();
                    continue block3;
                }
            }
        }
    }

    public void resetForForward() {
        this.resetBuffer();
        this._outputType = OutputType.NONE;
    }

    @Override
    public void resetBuffer() {
        this._out.resetBuffer();
        this._out.reopen();
    }

    public void setTrailers(Supplier<HttpFields> trailers) {
        this._trailers = trailers;
    }

    public Supplier<HttpFields> getTrailers() {
        return this._trailers;
    }

    protected MetaData.Response newResponseMetaData() {
        MetaData.Response info = new MetaData.Response(this._channel.getRequest().getHttpVersion(), this.getStatus(), this.getReason(), this._fields, this.getLongContentLength());
        info.setTrailerSupplier(this.getTrailers());
        return info;
    }

    public MetaData.Response getCommittedMetaData() {
        MetaData.Response meta = this._channel.getCommittedMetaData();
        if (meta == null) {
            return this.newResponseMetaData();
        }
        return meta;
    }

    @Override
    public boolean isCommitted() {
        if (this._channel.isSendError()) {
            return true;
        }
        return this._channel.isCommitted();
    }

    @Override
    public void setLocale(Locale locale) {
        if (locale == null || this.isCommitted() || !this.isMutable()) {
            return;
        }
        this._locale = locale;
        this._fields.put(HttpHeader.CONTENT_LANGUAGE, StringUtil.replace(locale.toString(), '_', '-'));
        if (this._outputType != OutputType.NONE) {
            return;
        }
        if (this._channel.getRequest().getContext() == null) {
            return;
        }
        String charset = this._channel.getRequest().getContext().getContextHandler().getLocaleEncoding(locale);
        if (charset != null && charset.length() > 0 && __localeOverride.contains((Object)this._encodingFrom)) {
            this.setCharacterEncoding(charset, EncodingFrom.SET_LOCALE);
        }
    }

    @Override
    public Locale getLocale() {
        if (this._locale == null) {
            return Locale.getDefault();
        }
        return this._locale;
    }

    @Override
    public int getStatus() {
        return this._status;
    }

    public String getReason() {
        return this._reason;
    }

    public HttpFields getHttpFields() {
        return this._fields;
    }

    public long getContentCount() {
        return this._out.getWritten();
    }

    public String toString() {
        return String.format("%s %d %s%n%s", new Object[]{this._channel.getRequest().getHttpVersion(), this._status, this._reason == null ? "" : this._reason, this._fields});
    }

    public void putHeaders(HttpContent content, long contentLength, boolean etag) {
        HttpField et;
        HttpField ce;
        HttpField lm = content.getLastModified();
        if (lm != null) {
            this._fields.put(lm);
        }
        if (contentLength == -2L) {
            this._fields.put(content.getContentLength());
            this._contentLength = content.getContentLengthValue();
        } else if (contentLength > -1L) {
            this._fields.putLongField(HttpHeader.CONTENT_LENGTH, contentLength);
            this._contentLength = contentLength;
        }
        HttpField ct = content.getContentType();
        if (ct != null) {
            if (this._characterEncoding != null && content.getCharacterEncoding() == null && content.getContentTypeValue() != null && __explicitCharset.contains((Object)this._encodingFrom)) {
                this.setContentType(MimeTypes.getContentTypeWithoutCharset(content.getContentTypeValue()));
            } else {
                this._fields.put(ct);
                this._contentType = ct.getValue();
                this._characterEncoding = content.getCharacterEncoding();
                this._mimeType = content.getMimeType();
            }
        }
        if ((ce = content.getContentEncoding()) != null) {
            this._fields.put(ce);
        }
        if (etag && (et = content.getETag()) != null) {
            this._fields.put(et);
        }
    }

    public static void putHeaders(HttpServletResponse response, HttpContent content, long contentLength, boolean etag) {
        String et;
        String ce;
        String ct;
        long lml = content.getResource().lastModified();
        if (lml >= 0L) {
            response.setDateHeader(HttpHeader.LAST_MODIFIED.asString(), lml);
        }
        if (contentLength == -2L) {
            contentLength = content.getContentLengthValue();
        }
        if (contentLength > -1L) {
            if (contentLength < Integer.MAX_VALUE) {
                response.setContentLength((int)contentLength);
            } else {
                response.setHeader(HttpHeader.CONTENT_LENGTH.asString(), Long.toString(contentLength));
            }
        }
        if ((ct = content.getContentTypeValue()) != null && response.getContentType() == null) {
            response.setContentType(ct);
        }
        if ((ce = content.getContentEncodingValue()) != null) {
            response.setHeader(HttpHeader.CONTENT_ENCODING.asString(), ce);
        }
        if (etag && (et = content.getETagValue()) != null) {
            response.setHeader(HttpHeader.ETAG.asString(), et);
        }
    }

    public static HttpServletResponse unwrap(ServletResponse servletResponse) {
        if (servletResponse instanceof HttpServletResponseWrapper) {
            return (HttpServletResponseWrapper)servletResponse;
        }
        if (servletResponse instanceof ServletResponseWrapper) {
            return Response.unwrap(((ServletResponseWrapper)servletResponse).getResponse());
        }
        return (HttpServletResponse)servletResponse;
    }

    private static enum EncodingFrom {
        NOT_SET,
        INFERRED,
        SET_LOCALE,
        SET_CONTENT_TYPE,
        SET_CHARACTER_ENCODING;

    }

    public static enum OutputType {
        NONE,
        STREAM,
        WRITER;

    }
}

