/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server.handler.gzip;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.ListIterator;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.CompressedContentFormat;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.http.PreEncodedHttpField;
import org.eclipse.jetty.http.pathmap.PathSpecSet;
import org.eclipse.jetty.server.HttpOutput;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.server.handler.gzip.GzipFactory;
import org.eclipse.jetty.server.handler.gzip.GzipHttpInputInterceptor;
import org.eclipse.jetty.server.handler.gzip.GzipHttpOutputInterceptor;
import org.eclipse.jetty.util.IncludeExclude;
import org.eclipse.jetty.util.RegexSet;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.compression.DeflaterPool;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class GzipHandler
extends HandlerWrapper
implements GzipFactory {
    public static final String GZIP_HANDLER_ETAGS = "o.e.j.s.h.gzip.GzipHandler.etag";
    public static final String GZIP = "gzip";
    public static final String DEFLATE = "deflate";
    public static final int DEFAULT_MIN_GZIP_SIZE = 32;
    public static final int BREAK_EVEN_GZIP_SIZE = 23;
    private static final Logger LOG = Log.getLogger(GzipHandler.class);
    private static final HttpField X_CE_GZIP = new PreEncodedHttpField("X-Content-Encoding", "gzip");
    private static final Pattern COMMA_GZIP = Pattern.compile(".*, *gzip");
    private int _poolCapacity = -1;
    private DeflaterPool _deflaterPool = null;
    private int _minGzipSize = 32;
    private int _compressionLevel = -1;
    @Deprecated
    private boolean _checkGzExists = false;
    private boolean _syncFlush = false;
    private int _inflateBufferSize = -1;
    private EnumSet<DispatcherType> _dispatchers = EnumSet.of(DispatcherType.REQUEST);
    private final IncludeExclude<String> _agentPatterns = new IncludeExclude(RegexSet.class);
    private final IncludeExclude<String> _methods = new IncludeExclude();
    private final IncludeExclude<String> _paths = new IncludeExclude(PathSpecSet.class);
    private final IncludeExclude<String> _mimeTypes = new IncludeExclude();
    private HttpField _vary;

    public GzipHandler() {
        this._methods.include(HttpMethod.GET.asString());
        for (String type : MimeTypes.getKnownMimeTypes()) {
            if ("image/svg+xml".equals(type)) {
                this._paths.exclude("*.svgz");
                continue;
            }
            if (!type.startsWith("image/") && !type.startsWith("audio/") && !type.startsWith("video/")) continue;
            this._mimeTypes.exclude(type);
        }
        this._mimeTypes.exclude("application/compress");
        this._mimeTypes.exclude("application/zip");
        this._mimeTypes.exclude("application/gzip");
        this._mimeTypes.exclude("application/bzip2");
        this._mimeTypes.exclude("application/brotli");
        this._mimeTypes.exclude("application/x-xz");
        this._mimeTypes.exclude("application/x-rar-compressed");
        if (LOG.isDebugEnabled()) {
            LOG.debug("{} mime types {}", this, this._mimeTypes);
        }
        this._agentPatterns.exclude(".*MSIE 6.0.*");
    }

    public void addExcludedAgentPatterns(String ... patterns) {
        this._agentPatterns.exclude((T[])patterns);
    }

    public void addExcludedMethods(String ... methods) {
        for (String m3 : methods) {
            this._methods.exclude(m3);
        }
    }

    public EnumSet<DispatcherType> getDispatcherTypes() {
        return this._dispatchers;
    }

    public void setDispatcherTypes(EnumSet<DispatcherType> dispatchers) {
        this._dispatchers = dispatchers;
    }

    public void setDispatcherTypes(DispatcherType ... dispatchers) {
        this._dispatchers = EnumSet.copyOf(Arrays.asList(dispatchers));
    }

    public void addExcludedMimeTypes(String ... types) {
        for (String t : types) {
            this._mimeTypes.exclude((T[])StringUtil.csvSplit(t));
        }
    }

    public void addExcludedPaths(String ... pathspecs) {
        for (String p : pathspecs) {
            this._paths.exclude((T[])StringUtil.csvSplit(p));
        }
    }

    public void addIncludedAgentPatterns(String ... patterns) {
        this._agentPatterns.include((T[])patterns);
    }

    public void addIncludedMethods(String ... methods) {
        for (String m3 : methods) {
            this._methods.include(m3);
        }
    }

    public boolean isSyncFlush() {
        return this._syncFlush;
    }

    public void setSyncFlush(boolean syncFlush) {
        this._syncFlush = syncFlush;
    }

    public void addIncludedMimeTypes(String ... types) {
        for (String t : types) {
            this._mimeTypes.include((T[])StringUtil.csvSplit(t));
        }
    }

    public void addIncludedPaths(String ... pathspecs) {
        for (String p : pathspecs) {
            this._paths.include((T[])StringUtil.csvSplit(p));
        }
    }

    @Override
    protected void doStart() throws Exception {
        this._deflaterPool = this.newDeflaterPool(this._poolCapacity);
        this.addBean(this._deflaterPool);
        this._vary = this._agentPatterns.size() > 0 ? GzipHttpOutputInterceptor.VARY_ACCEPT_ENCODING_USER_AGENT : GzipHttpOutputInterceptor.VARY_ACCEPT_ENCODING;
        super.doStart();
    }

    @Deprecated
    public boolean getCheckGzExists() {
        return this._checkGzExists;
    }

    public int getCompressionLevel() {
        return this._compressionLevel;
    }

    @Override
    public Deflater getDeflater(Request request, long contentLength) {
        HttpFields httpFields = request.getHttpFields();
        String ua = httpFields.get(HttpHeader.USER_AGENT);
        if (ua != null && !this.isAgentGzipable(ua)) {
            LOG.debug("{} excluded user agent {}", this, request);
            return null;
        }
        if (contentLength >= 0L && contentLength < (long)this._minGzipSize) {
            LOG.debug("{} excluded minGzipSize {}", this, request);
            return null;
        }
        if (!httpFields.contains(HttpHeader.ACCEPT_ENCODING, GZIP)) {
            LOG.debug("{} excluded not gzip accept {}", this, request);
            return null;
        }
        return (Deflater)this._deflaterPool.acquire();
    }

    public String[] getExcludedAgentPatterns() {
        Set<String> excluded = this._agentPatterns.getExcluded();
        return excluded.toArray(new String[0]);
    }

    public String[] getExcludedMethods() {
        Set<String> excluded = this._methods.getExcluded();
        return excluded.toArray(new String[0]);
    }

    public String[] getExcludedMimeTypes() {
        Set<String> excluded = this._mimeTypes.getExcluded();
        return excluded.toArray(new String[0]);
    }

    public String[] getExcludedPaths() {
        Set<String> excluded = this._paths.getExcluded();
        return excluded.toArray(new String[0]);
    }

    public String[] getIncludedAgentPatterns() {
        Set<String> includes = this._agentPatterns.getIncluded();
        return includes.toArray(new String[0]);
    }

    public String[] getIncludedMethods() {
        Set<String> includes = this._methods.getIncluded();
        return includes.toArray(new String[0]);
    }

    public String[] getIncludedMimeTypes() {
        Set<String> includes = this._mimeTypes.getIncluded();
        return includes.toArray(new String[0]);
    }

    public String[] getIncludedPaths() {
        Set<String> includes = this._paths.getIncluded();
        return includes.toArray(new String[0]);
    }

    @Deprecated
    public String[] getMethods() {
        return this.getIncludedMethods();
    }

    public int getMinGzipSize() {
        return this._minGzipSize;
    }

    protected HttpField getVaryField() {
        return this._vary;
    }

    public int getInflateBufferSize() {
        return this._inflateBufferSize;
    }

    public void setInflateBufferSize(int size) {
        this._inflateBufferSize = size;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        File gz;
        String realpath;
        String mimeType;
        ServletContext context = baseRequest.getServletContext();
        String path = context == null ? baseRequest.getRequestURI() : URIUtil.addPaths(baseRequest.getServletPath(), baseRequest.getPathInfo());
        LOG.debug("{} handle {} in {}", this, baseRequest, context);
        if (!this._dispatchers.contains((Object)baseRequest.getDispatcherType())) {
            LOG.debug("{} excluded by dispatcherType {}", new Object[]{this, baseRequest.getDispatcherType()});
            this._handler.handle(target, baseRequest, request, response);
            return;
        }
        if (this._inflateBufferSize > 0) {
            boolean inflate = false;
            ListIterator<HttpField> i = baseRequest.getHttpFields().listIterator();
            while (i.hasNext()) {
                HttpField field = i.next();
                if (field.getHeader() != HttpHeader.CONTENT_ENCODING) continue;
                if (field.getValue().equalsIgnoreCase(GZIP)) {
                    i.set(X_CE_GZIP);
                    inflate = true;
                    break;
                }
                if (!COMMA_GZIP.matcher(field.getValue()).matches()) continue;
                String v = field.getValue();
                v = v.substring(0, v.lastIndexOf(44));
                i.set(new HttpField(HttpHeader.CONTENT_ENCODING, v));
                i.add(X_CE_GZIP);
                inflate = true;
                break;
            }
            if (inflate) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("{} inflate {}", this, request);
                }
                baseRequest.getHttpInput().addInterceptor(new GzipHttpInputInterceptor(baseRequest.getHttpChannel().getByteBufferPool(), this._inflateBufferSize));
                baseRequest.getHttpFields().computeField(HttpHeader.CONTENT_LENGTH, (header, fields) -> {
                    if (fields == null) {
                        return null;
                    }
                    String length = fields.stream().map(HttpField::getValue).findAny().orElse("0");
                    return new HttpField("X-Content-Length", length);
                });
            }
        }
        HttpOutput out = baseRequest.getResponse().getHttpOutput();
        for (HttpOutput.Interceptor interceptor = out.getInterceptor(); interceptor != null; interceptor = interceptor.getNextInterceptor()) {
            if (!(interceptor instanceof GzipHttpOutputInterceptor)) continue;
            LOG.debug("{} already intercepting {}", this, request);
            this._handler.handle(target, baseRequest, request, response);
            return;
        }
        if (!StringUtil.isEmpty(CompressedContentFormat.ETAG_SEPARATOR)) {
            ListIterator<HttpField> fields2 = baseRequest.getHttpFields().listIterator();
            while (fields2.hasNext()) {
                String etags;
                String etagsNoSuffix;
                HttpField field = fields2.next();
                if (field.getHeader() != HttpHeader.IF_NONE_MATCH && field.getHeader() != HttpHeader.IF_MATCH || (etagsNoSuffix = CompressedContentFormat.GZIP.stripSuffixes(etags = field.getValue())).equals(etags)) continue;
                fields2.set(new HttpField(field.getHeader(), etagsNoSuffix));
                baseRequest.setAttribute(GZIP_HANDLER_ETAGS, etags);
            }
        }
        if (!this._methods.test(baseRequest.getMethod())) {
            LOG.debug("{} excluded by method {}", this, request);
            this._handler.handle(target, baseRequest, request, response);
            return;
        }
        if (!this.isPathGzipable(path)) {
            LOG.debug("{} excluded by path {}", this, request);
            this._handler.handle(target, baseRequest, request, response);
            return;
        }
        String string = mimeType = context == null ? MimeTypes.getDefaultMimeByExtension(path) : context.getMimeType(path);
        if (mimeType != null && !this.isMimeTypeGzipable(mimeType = MimeTypes.getContentTypeWithoutCharset(mimeType))) {
            LOG.debug("{} excluded by path suffix mime type {}", this, request);
            this._handler.handle(target, baseRequest, request, response);
            return;
        }
        if (this._checkGzExists && context != null && (realpath = request.getServletContext().getRealPath(path)) != null && (gz = new File(realpath + ".gz")).exists()) {
            LOG.debug("{} gzip exists {}", this, request);
            this._handler.handle(target, baseRequest, request, response);
            return;
        }
        HttpOutput.Interceptor origInterceptor = out.getInterceptor();
        try {
            out.setInterceptor(new GzipHttpOutputInterceptor(this, this.getVaryField(), baseRequest.getHttpChannel(), origInterceptor, this.isSyncFlush()));
            if (this._handler != null) {
                this._handler.handle(target, baseRequest, request, response);
            }
        }
        finally {
            if (!baseRequest.isHandled() && !baseRequest.isAsyncStarted()) {
                out.setInterceptor(origInterceptor);
            }
        }
    }

    protected boolean isAgentGzipable(String ua) {
        if (ua == null) {
            return false;
        }
        return this._agentPatterns.test(ua);
    }

    @Override
    public boolean isMimeTypeGzipable(String mimetype) {
        return this._mimeTypes.test(mimetype);
    }

    protected boolean isPathGzipable(String requestURI) {
        if (requestURI == null) {
            return true;
        }
        return this._paths.test(requestURI);
    }

    @Override
    public void recycle(Deflater deflater) {
        this._deflaterPool.release(deflater);
    }

    @Deprecated
    public void setCheckGzExists(boolean checkGzExists) {
        this._checkGzExists = checkGzExists;
    }

    public void setCompressionLevel(int compressionLevel) {
        if (this.isStarted()) {
            throw new IllegalStateException(this.getState());
        }
        this._compressionLevel = compressionLevel;
    }

    public void setExcludedAgentPatterns(String ... patterns) {
        this._agentPatterns.getExcluded().clear();
        this.addExcludedAgentPatterns(patterns);
    }

    public void setExcludedMethods(String ... methods) {
        this._methods.getExcluded().clear();
        this._methods.exclude((T[])methods);
    }

    public void setExcludedMimeTypes(String ... types) {
        this._mimeTypes.getExcluded().clear();
        this._mimeTypes.exclude((T[])types);
    }

    public void setExcludedPaths(String ... pathspecs) {
        this._paths.getExcluded().clear();
        this._paths.exclude((T[])pathspecs);
    }

    public void setIncludedAgentPatterns(String ... patterns) {
        this._agentPatterns.getIncluded().clear();
        this.addIncludedAgentPatterns(patterns);
    }

    public void setIncludedMethods(String ... methods) {
        this._methods.getIncluded().clear();
        this._methods.include((T[])methods);
    }

    public void setIncludedMimeTypes(String ... types) {
        this._mimeTypes.getIncluded().clear();
        this._mimeTypes.include((T[])types);
    }

    public void setIncludedPaths(String ... pathspecs) {
        this._paths.getIncluded().clear();
        this._paths.include((T[])pathspecs);
    }

    public void setMinGzipSize(int minGzipSize) {
        if (minGzipSize < 23) {
            LOG.warn("minGzipSize of {} is inefficient for short content, break even is size {}", minGzipSize, 23);
        }
        this._minGzipSize = Math.max(0, minGzipSize);
    }

    public void setIncludedMethodList(String csvMethods) {
        this.setIncludedMethods(StringUtil.csvSplit(csvMethods));
    }

    public String getIncludedMethodList() {
        return String.join((CharSequence)",", this.getIncludedMethods());
    }

    public void setExcludedMethodList(String csvMethods) {
        this.setExcludedMethods(StringUtil.csvSplit(csvMethods));
    }

    public String getExcludedMethodList() {
        return String.join((CharSequence)",", this.getExcludedMethods());
    }

    public int getDeflaterPoolCapacity() {
        return this._poolCapacity;
    }

    public void setDeflaterPoolCapacity(int capacity) {
        if (this.isStarted()) {
            throw new IllegalStateException(this.getState());
        }
        this._poolCapacity = capacity;
    }

    protected DeflaterPool newDeflaterPool(int capacity) {
        return new DeflaterPool(capacity, this.getCompressionLevel(), true);
    }

    @Override
    public String toString() {
        return String.format("%s@%x{%s,min=%s,inflate=%s}", this.getClass().getSimpleName(), this.hashCode(), this.getState(), this._minGzipSize, this._inflateBufferSize);
    }
}

