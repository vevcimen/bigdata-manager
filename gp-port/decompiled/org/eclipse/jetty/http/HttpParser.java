/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.http;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import org.eclipse.jetty.http.BadMessageException;
import org.eclipse.jetty.http.HostPortHttpField;
import org.eclipse.jetty.http.HttpCompliance;
import org.eclipse.jetty.http.HttpComplianceSection;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpHeaderValue;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpTokens;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http.PreEncodedHttpField;
import org.eclipse.jetty.http.QuotedCSV;
import org.eclipse.jetty.util.ArrayTernaryTrie;
import org.eclipse.jetty.util.ArrayTrie;
import org.eclipse.jetty.util.BufferUtil;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.Trie;
import org.eclipse.jetty.util.Utf8StringBuilder;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class HttpParser {
    public static final Logger LOG = Log.getLogger(HttpParser.class);
    @Deprecated
    public static final String __STRICT = "org.eclipse.jetty.http.HttpParser.STRICT";
    public static final int INITIAL_URI_LENGTH = 256;
    private static final int MAX_CHUNK_LENGTH = 0x7FFFFEF;
    public static final Trie<HttpField> CACHE = new ArrayTrie<HttpField>(2048);
    private static final Trie<HttpField> NO_CACHE = Trie.empty(true);
    private static final EnumSet<State> __idleStates = EnumSet.of(State.START, State.END, State.CLOSE, State.CLOSED);
    private static final EnumSet<State> __completeStates = EnumSet.of(State.END, State.CLOSE, State.CLOSED);
    private final boolean debug = LOG.isDebugEnabled();
    private final HttpHandler _handler;
    private final RequestHandler _requestHandler;
    private final ResponseHandler _responseHandler;
    private final ComplianceHandler _complianceHandler;
    private final int _maxHeaderBytes;
    private final HttpCompliance _compliance;
    private final EnumSet<HttpComplianceSection> _compliances;
    private final Utf8StringBuilder _uri = new Utf8StringBuilder(256);
    private HttpField _field;
    private HttpHeader _header;
    private String _headerString;
    private String _valueString;
    private int _responseStatus;
    private int _headerBytes;
    private boolean _host;
    private boolean _headerComplete;
    private volatile State _state = State.START;
    private volatile FieldState _fieldState = FieldState.FIELD;
    private volatile boolean _eof;
    private HttpMethod _method;
    private String _methodString;
    private HttpVersion _version;
    private HttpTokens.EndOfContent _endOfContent;
    private boolean _hasContentLength;
    private boolean _hasTransferEncoding;
    private long _contentLength = -1L;
    private long _contentPosition;
    private int _chunkLength;
    private int _chunkPosition;
    private boolean _headResponse;
    private boolean _cr;
    private ByteBuffer _contentChunk;
    private Trie<HttpField> _fieldCache;
    private int _length;
    private final StringBuilder _string = new StringBuilder();

    private static HttpCompliance compliance() {
        boolean strict = Boolean.getBoolean(__STRICT);
        if (strict) {
            LOG.warn("Deprecated property used: org.eclipse.jetty.http.HttpParser.STRICT", new Object[0]);
            return HttpCompliance.LEGACY;
        }
        return HttpCompliance.RFC7230;
    }

    public HttpParser(RequestHandler handler) {
        this(handler, -1, HttpParser.compliance());
    }

    public HttpParser(ResponseHandler handler) {
        this(handler, -1, HttpParser.compliance());
    }

    public HttpParser(RequestHandler handler, int maxHeaderBytes) {
        this(handler, maxHeaderBytes, HttpParser.compliance());
    }

    public HttpParser(ResponseHandler handler, int maxHeaderBytes) {
        this(handler, maxHeaderBytes, HttpParser.compliance());
    }

    @Deprecated
    public HttpParser(RequestHandler handler, int maxHeaderBytes, boolean strict) {
        this(handler, maxHeaderBytes, strict ? HttpCompliance.LEGACY : HttpParser.compliance());
    }

    @Deprecated
    public HttpParser(ResponseHandler handler, int maxHeaderBytes, boolean strict) {
        this(handler, maxHeaderBytes, strict ? HttpCompliance.LEGACY : HttpParser.compliance());
    }

    public HttpParser(RequestHandler handler, HttpCompliance compliance) {
        this(handler, -1, compliance);
    }

    public HttpParser(RequestHandler handler, int maxHeaderBytes, HttpCompliance compliance) {
        this(handler, null, maxHeaderBytes, compliance == null ? HttpParser.compliance() : compliance);
    }

    public HttpParser(ResponseHandler handler, int maxHeaderBytes, HttpCompliance compliance) {
        this(null, handler, maxHeaderBytes, compliance == null ? HttpParser.compliance() : compliance);
    }

    private HttpParser(RequestHandler requestHandler, ResponseHandler responseHandler, int maxHeaderBytes, HttpCompliance compliance) {
        this._handler = requestHandler != null ? requestHandler : responseHandler;
        this._requestHandler = requestHandler;
        this._responseHandler = responseHandler;
        this._maxHeaderBytes = maxHeaderBytes;
        this._compliance = compliance;
        this._compliances = compliance.sections();
        this._complianceHandler = (ComplianceHandler)(this._handler instanceof ComplianceHandler ? this._handler : null);
    }

    public HttpHandler getHandler() {
        return this._handler;
    }

    public HttpCompliance getHttpCompliance() {
        return this._compliance;
    }

    protected boolean complianceViolation(HttpComplianceSection violation) {
        return this.complianceViolation(violation, null);
    }

    protected boolean complianceViolation(HttpComplianceSection violation, String reason) {
        if (this._compliances.contains((Object)violation)) {
            return true;
        }
        if (reason == null) {
            reason = violation.description;
        }
        if (this._complianceHandler != null) {
            this._complianceHandler.onComplianceViolation(this._compliance, violation, reason);
        }
        return false;
    }

    protected void handleViolation(HttpComplianceSection section, String reason) {
        if (this._complianceHandler != null) {
            this._complianceHandler.onComplianceViolation(this._compliance, section, reason);
        }
    }

    protected String caseInsensitiveHeader(String orig, String normative) {
        if (this._compliances.contains((Object)HttpComplianceSection.FIELD_NAME_CASE_INSENSITIVE)) {
            return normative;
        }
        if (!orig.equals(normative)) {
            this.handleViolation(HttpComplianceSection.FIELD_NAME_CASE_INSENSITIVE, orig);
        }
        return orig;
    }

    public long getContentLength() {
        return this._contentLength;
    }

    public long getContentRead() {
        return this._contentPosition;
    }

    public int getHeaderLength() {
        return this._headerBytes;
    }

    public void setHeadResponse(boolean head) {
        this._headResponse = head;
    }

    protected void setResponseStatus(int status) {
        this._responseStatus = status;
    }

    public State getState() {
        return this._state;
    }

    public boolean inContentState() {
        return this._state.ordinal() >= State.CONTENT.ordinal() && this._state.ordinal() < State.END.ordinal();
    }

    public boolean inHeaderState() {
        return this._state.ordinal() < State.CONTENT.ordinal();
    }

    public boolean isChunking() {
        return this._endOfContent == HttpTokens.EndOfContent.CHUNKED_CONTENT;
    }

    public boolean isStart() {
        return this.isState(State.START);
    }

    public boolean isClose() {
        return this.isState(State.CLOSE);
    }

    public boolean isClosed() {
        return this.isState(State.CLOSED);
    }

    public boolean isIdle() {
        return __idleStates.contains((Object)this._state);
    }

    public boolean isComplete() {
        return __completeStates.contains((Object)this._state);
    }

    public boolean isState(State state) {
        return this._state == state;
    }

    private HttpTokens.Token next(ByteBuffer buffer) {
        byte ch = buffer.get();
        HttpTokens.Token t = HttpTokens.TOKENS[0xFF & ch];
        switch (t.getType()) {
            case CNTL: {
                throw new IllegalCharacterException(this._state, t, buffer);
            }
            case LF: {
                this._cr = false;
                break;
            }
            case CR: {
                if (this._cr) {
                    throw new BadMessageException("Bad EOL");
                }
                this._cr = true;
                if (buffer.hasRemaining()) {
                    if (this._maxHeaderBytes > 0 && (this._state == State.HEADER || this._state == State.TRAILER)) {
                        ++this._headerBytes;
                    }
                    return this.next(buffer);
                }
                return null;
            }
            case ALPHA: 
            case DIGIT: 
            case TCHAR: 
            case VCHAR: 
            case HTAB: 
            case SPACE: 
            case OTEXT: 
            case COLON: {
                if (!this._cr) break;
                throw new BadMessageException("Bad EOL");
            }
        }
        return t;
    }

    private boolean quickStart(ByteBuffer buffer) {
        HttpTokens.Token t;
        if (this._requestHandler != null) {
            this._method = HttpMethod.lookAheadGet(buffer);
            if (this._method != null) {
                this._methodString = this._method.asString();
                buffer.position(buffer.position() + this._methodString.length() + 1);
                this.setState(State.SPACE1);
                return false;
            }
        } else if (this._responseHandler != null) {
            this._version = HttpVersion.lookAheadGet(buffer);
            if (this._version != null) {
                buffer.position(buffer.position() + this._version.asString().length() + 1);
                this.setState(State.SPACE1);
                return false;
            }
        }
        while (this._state == State.START && buffer.hasRemaining() && (t = this.next(buffer)) != null) {
            switch (t.getType()) {
                case ALPHA: 
                case DIGIT: 
                case TCHAR: 
                case VCHAR: {
                    this._string.setLength(0);
                    this._string.append(t.getChar());
                    this.setState(this._requestHandler != null ? State.METHOD : State.RESPONSE_VERSION);
                    return false;
                }
                case HTAB: 
                case SPACE: 
                case OTEXT: {
                    throw new IllegalCharacterException(this._state, t, buffer);
                }
            }
            if (this._maxHeaderBytes <= 0 || ++this._headerBytes <= this._maxHeaderBytes) continue;
            LOG.warn("padding is too large >" + this._maxHeaderBytes, new Object[0]);
            throw new BadMessageException(400);
        }
        return false;
    }

    private void setString(String s2) {
        this._string.setLength(0);
        this._string.append(s2);
        this._length = s2.length();
    }

    private String takeString() {
        this._string.setLength(this._length);
        String s2 = this._string.toString();
        this._string.setLength(0);
        this._length = -1;
        return s2;
    }

    private boolean handleHeaderContentMessage() {
        boolean handleHeader = this._handler.headerComplete();
        this._headerComplete = true;
        if (handleHeader) {
            return true;
        }
        this.setState(State.CONTENT_END);
        return this.handleContentMessage();
    }

    private boolean handleContentMessage() {
        boolean handleContent = this._handler.contentComplete();
        if (handleContent) {
            return true;
        }
        this.setState(State.END);
        return this._handler.messageComplete();
    }

    private boolean parseLine(ByteBuffer buffer) {
        HttpTokens.Token t;
        boolean handle = false;
        block47: while (this._state.ordinal() < State.HEADER.ordinal() && buffer.hasRemaining() && !handle && (t = this.next(buffer)) != null) {
            if (this._maxHeaderBytes > 0 && ++this._headerBytes > this._maxHeaderBytes) {
                if (this._state == State.URI) {
                    LOG.warn("URI is too large >" + this._maxHeaderBytes, new Object[0]);
                    throw new BadMessageException(414);
                }
                if (this._requestHandler != null) {
                    LOG.warn("request is too large >" + this._maxHeaderBytes, new Object[0]);
                } else {
                    LOG.warn("response is too large >" + this._maxHeaderBytes, new Object[0]);
                }
                throw new BadMessageException(431);
            }
            block0 : switch (this._state) {
                case METHOD: {
                    switch (t.getType()) {
                        case SPACE: {
                            HttpMethod method;
                            this._length = this._string.length();
                            this._methodString = this.takeString();
                            if (this._compliances.contains((Object)HttpComplianceSection.METHOD_CASE_SENSITIVE)) {
                                method = HttpMethod.CACHE.get(this._methodString);
                                if (method != null) {
                                    this._methodString = method.asString();
                                }
                            } else {
                                method = HttpMethod.INSENSITIVE_CACHE.get(this._methodString);
                                if (method != null) {
                                    if (!method.asString().equals(this._methodString)) {
                                        this.handleViolation(HttpComplianceSection.METHOD_CASE_SENSITIVE, this._methodString);
                                    }
                                    this._methodString = method.asString();
                                }
                            }
                            this.setState(State.SPACE1);
                            break block0;
                        }
                        case LF: {
                            throw new BadMessageException("No URI");
                        }
                        case ALPHA: 
                        case DIGIT: 
                        case TCHAR: {
                            this._string.append(t.getChar());
                            break block0;
                        }
                    }
                    throw new IllegalCharacterException(this._state, t, buffer);
                }
                case RESPONSE_VERSION: {
                    Object version;
                    switch (t.getType()) {
                        case SPACE: {
                            this._length = this._string.length();
                            version = this.takeString();
                            this._version = HttpVersion.CACHE.get((String)version);
                            this.checkVersion();
                            this.setState(State.SPACE1);
                            break block0;
                        }
                        case ALPHA: 
                        case DIGIT: 
                        case TCHAR: 
                        case VCHAR: 
                        case COLON: {
                            this._string.append(t.getChar());
                            break block0;
                        }
                    }
                    throw new IllegalCharacterException(this._state, t, buffer);
                }
                case SPACE1: {
                    switch (t.getType()) {
                        case SPACE: {
                            break block0;
                        }
                        case ALPHA: 
                        case DIGIT: 
                        case TCHAR: 
                        case VCHAR: 
                        case COLON: {
                            if (this._responseHandler != null) {
                                if (t.getType() != HttpTokens.Type.DIGIT) {
                                    throw new IllegalCharacterException(this._state, t, buffer);
                                }
                                this.setState(State.STATUS);
                                this.setResponseStatus(t.getByte() - 48);
                                break block0;
                            }
                            this._uri.reset();
                            this.setState(State.URI);
                            if (buffer.hasArray()) {
                                int i;
                                byte[] array = buffer.array();
                                int p = buffer.arrayOffset() + buffer.position();
                                int l = buffer.arrayOffset() + buffer.limit();
                                for (i = p; i < l && array[i] > 32; ++i) {
                                }
                                int len = i - p;
                                this._headerBytes += len;
                                if (this._maxHeaderBytes > 0 && ++this._headerBytes > this._maxHeaderBytes) {
                                    LOG.warn("URI is too large >" + this._maxHeaderBytes, new Object[0]);
                                    throw new BadMessageException(414);
                                }
                                this._uri.append(array, p - 1, len + 1);
                                buffer.position(i - buffer.arrayOffset());
                                break block0;
                            }
                            this._uri.append(t.getByte());
                            break block0;
                        }
                    }
                    throw new BadMessageException(400, this._requestHandler != null ? "No URI" : "No Status");
                }
                case STATUS: {
                    switch (t.getType()) {
                        case SPACE: {
                            this.setState(State.SPACE2);
                            break block0;
                        }
                        case DIGIT: {
                            this._responseStatus = this._responseStatus * 10 + (t.getByte() - 48);
                            if (this._responseStatus < 1000) continue block47;
                            throw new BadMessageException("Bad status");
                        }
                        case LF: {
                            this.setState(State.HEADER);
                            this._responseHandler.startResponse(this._version, this._responseStatus, null);
                            break block0;
                        }
                    }
                    throw new IllegalCharacterException(this._state, t, buffer);
                }
                case URI: {
                    switch (t.getType()) {
                        case SPACE: {
                            this.setState(State.SPACE2);
                            break block0;
                        }
                        case LF: {
                            if (this.complianceViolation(HttpComplianceSection.NO_HTTP_0_9, "No request version")) {
                                throw new BadMessageException(505, "HTTP/0.9 not supported");
                            }
                            this._requestHandler.startRequest(this._methodString, this._uri.toString(), HttpVersion.HTTP_0_9);
                            this.setState(State.CONTENT);
                            this._endOfContent = HttpTokens.EndOfContent.NO_CONTENT;
                            BufferUtil.clear(buffer);
                            handle = this.handleHeaderContentMessage();
                            break block0;
                        }
                        case ALPHA: 
                        case DIGIT: 
                        case TCHAR: 
                        case VCHAR: 
                        case OTEXT: 
                        case COLON: {
                            this._uri.append(t.getByte());
                            break block0;
                        }
                    }
                    throw new IllegalCharacterException(this._state, t, buffer);
                }
                case SPACE2: {
                    Object version;
                    switch (t.getType()) {
                        case SPACE: {
                            break block0;
                        }
                        case ALPHA: 
                        case DIGIT: 
                        case TCHAR: 
                        case VCHAR: 
                        case COLON: {
                            int pos;
                            this._string.setLength(0);
                            this._string.append(t.getChar());
                            if (this._responseHandler != null) {
                                this._length = 1;
                                this.setState(State.REASON);
                                break block0;
                            }
                            this.setState(State.REQUEST_VERSION);
                            version = buffer.position() > 0 && buffer.hasArray() ? HttpVersion.lookAheadGet(buffer.array(), buffer.arrayOffset() + buffer.position() - 1, buffer.arrayOffset() + buffer.limit()) : HttpVersion.CACHE.getBest(buffer, 0, buffer.remaining());
                            if (version == null || (pos = buffer.position() + ((HttpVersion)((Object)version)).asString().length() - 1) >= buffer.limit()) continue block47;
                            byte n = buffer.get(pos);
                            if (n == 13) {
                                this._cr = true;
                                this._version = version;
                                this.checkVersion();
                                this._string.setLength(0);
                                buffer.position(pos + 1);
                                break block0;
                            }
                            if (n != 10) continue block47;
                            this._version = version;
                            this.checkVersion();
                            this._string.setLength(0);
                            buffer.position(pos);
                            break block0;
                        }
                        case LF: {
                            if (this._responseHandler != null) {
                                this.setState(State.HEADER);
                                this._responseHandler.startResponse(this._version, this._responseStatus, null);
                                break block0;
                            }
                            if (this.complianceViolation(HttpComplianceSection.NO_HTTP_0_9, "No request version")) {
                                throw new BadMessageException("HTTP/0.9 not supported");
                            }
                            this._requestHandler.startRequest(this._methodString, this._uri.toString(), HttpVersion.HTTP_0_9);
                            this.setState(State.CONTENT);
                            this._endOfContent = HttpTokens.EndOfContent.NO_CONTENT;
                            BufferUtil.clear(buffer);
                            handle = this.handleHeaderContentMessage();
                            break block0;
                        }
                        default: {
                            throw new IllegalCharacterException(this._state, t, buffer);
                        }
                    }
                }
                case REQUEST_VERSION: {
                    switch (t.getType()) {
                        case LF: {
                            if (this._version == null) {
                                this._length = this._string.length();
                                this._version = HttpVersion.CACHE.get(this.takeString());
                            }
                            this.checkVersion();
                            this.setState(State.HEADER);
                            this._requestHandler.startRequest(this._methodString, this._uri.toString(), this._version);
                            continue block47;
                        }
                        case ALPHA: 
                        case DIGIT: 
                        case TCHAR: 
                        case VCHAR: 
                        case COLON: {
                            this._string.append(t.getChar());
                            break block0;
                        }
                    }
                    throw new IllegalCharacterException(this._state, t, buffer);
                }
                case REASON: {
                    switch (t.getType()) {
                        case LF: {
                            String reason = this.takeString();
                            this.setState(State.HEADER);
                            this._responseHandler.startResponse(this._version, this._responseStatus, reason);
                            continue block47;
                        }
                        case ALPHA: 
                        case DIGIT: 
                        case TCHAR: 
                        case VCHAR: 
                        case OTEXT: 
                        case COLON: {
                            this._string.append(t.getChar());
                            this._length = this._string.length();
                            break block0;
                        }
                        case HTAB: 
                        case SPACE: {
                            this._string.append(t.getChar());
                            break block0;
                        }
                    }
                    throw new IllegalCharacterException(this._state, t, buffer);
                }
                default: {
                    throw new IllegalStateException(this._state.toString());
                }
            }
        }
        return handle;
    }

    private void checkVersion() {
        if (this._version == null) {
            throw new BadMessageException(505, "Unknown Version");
        }
        if (this._version.getVersion() < 10 || this._version.getVersion() > 20) {
            throw new BadMessageException(505, "Unsupported Version");
        }
    }

    private void parsedHeader() {
        if (this._headerString != null || this._valueString != null) {
            if (this._header != null) {
                boolean addToFieldCache = false;
                switch (this._header) {
                    case CONTENT_LENGTH: {
                        if (this._hasTransferEncoding && this.complianceViolation(HttpComplianceSection.TRANSFER_ENCODING_WITH_CONTENT_LENGTH)) {
                            throw new BadMessageException(400, "Transfer-Encoding and Content-Length");
                        }
                        if (this._hasContentLength) {
                            if (this.complianceViolation(HttpComplianceSection.MULTIPLE_CONTENT_LENGTHS)) {
                                throw new BadMessageException(400, HttpComplianceSection.MULTIPLE_CONTENT_LENGTHS.description);
                            }
                            if (this.convertContentLength(this._valueString) != this._contentLength) {
                                throw new BadMessageException(400, HttpComplianceSection.MULTIPLE_CONTENT_LENGTHS.description);
                            }
                        }
                        this._hasContentLength = true;
                        if (this._endOfContent == HttpTokens.EndOfContent.CHUNKED_CONTENT) break;
                        this._contentLength = this.convertContentLength(this._valueString);
                        if (this._contentLength <= 0L) {
                            this._endOfContent = HttpTokens.EndOfContent.NO_CONTENT;
                            break;
                        }
                        this._endOfContent = HttpTokens.EndOfContent.CONTENT_LENGTH;
                        break;
                    }
                    case TRANSFER_ENCODING: {
                        this._hasTransferEncoding = true;
                        if (this._hasContentLength && this.complianceViolation(HttpComplianceSection.TRANSFER_ENCODING_WITH_CONTENT_LENGTH)) {
                            throw new BadMessageException(400, "Transfer-Encoding and Content-Length");
                        }
                        if (this._endOfContent == HttpTokens.EndOfContent.CHUNKED_CONTENT) {
                            throw new BadMessageException(400, "Bad Transfer-Encoding, chunked not last");
                        }
                        if (HttpHeaderValue.CHUNKED.is(this._valueString)) {
                            this._endOfContent = HttpTokens.EndOfContent.CHUNKED_CONTENT;
                            this._contentLength = -1L;
                            break;
                        }
                        List<String> values = new QuotedCSV(this._valueString).getValues();
                        int chunked = -1;
                        int len = values.size();
                        for (int i = 0; i < len; ++i) {
                            if (HttpHeaderValue.CHUNKED.is(values.get(i))) {
                                if (chunked != -1) {
                                    throw new BadMessageException(400, "Bad Transfer-Encoding, multiple chunked tokens");
                                }
                                chunked = i;
                                this._endOfContent = HttpTokens.EndOfContent.CHUNKED_CONTENT;
                                this._contentLength = -1L;
                                continue;
                            }
                            if (this._endOfContent != HttpTokens.EndOfContent.CHUNKED_CONTENT) continue;
                            throw new BadMessageException(400, "Bad Transfer-Encoding, chunked not last");
                        }
                        break;
                    }
                    case HOST: {
                        this._host = true;
                        if (this._field instanceof HostPortHttpField || this._valueString == null || this._valueString.isEmpty()) break;
                        this._field = new HostPortHttpField(this._header, this._compliances.contains((Object)HttpComplianceSection.FIELD_NAME_CASE_INSENSITIVE) ? this._header.asString() : this._headerString, this._valueString);
                        addToFieldCache = true;
                        break;
                    }
                    case CONNECTION: {
                        if (this._field == null) {
                            this._field = new HttpField(this._header, this.caseInsensitiveHeader(this._headerString, this._header.asString()), this._valueString);
                        }
                        if (this._handler.getHeaderCacheSize() <= 0 || !this._field.contains(HttpHeaderValue.CLOSE.asString())) break;
                        this._fieldCache = NO_CACHE;
                        break;
                    }
                    case AUTHORIZATION: 
                    case ACCEPT: 
                    case ACCEPT_CHARSET: 
                    case ACCEPT_ENCODING: 
                    case ACCEPT_LANGUAGE: 
                    case COOKIE: 
                    case CACHE_CONTROL: 
                    case USER_AGENT: {
                        addToFieldCache = this._field == null;
                        break;
                    }
                }
                if (addToFieldCache && this._header != null && this._valueString != null) {
                    if (this._fieldCache == null) {
                        Trie<Object> trie = this._fieldCache = this._handler.getHeaderCacheSize() > 0 && this._version != null && this._version == HttpVersion.HTTP_1_1 ? new ArrayTernaryTrie(this._handler.getHeaderCacheSize()) : NO_CACHE;
                    }
                    if (!this._fieldCache.isFull()) {
                        if (this._field == null) {
                            this._field = new HttpField(this._header, this.caseInsensitiveHeader(this._headerString, this._header.asString()), this._valueString);
                        }
                        this._fieldCache.put(this._field);
                    }
                }
            }
            this._handler.parsedHeader(this._field != null ? this._field : new HttpField(this._header, this._headerString, this._valueString));
        }
        this._valueString = null;
        this._headerString = null;
        this._header = null;
        this._field = null;
    }

    private void parsedTrailer() {
        if (this._headerString != null || this._valueString != null) {
            this._handler.parsedTrailer(this._field != null ? this._field : new HttpField(this._header, this._headerString, this._valueString));
        }
        this._valueString = null;
        this._headerString = null;
        this._header = null;
        this._field = null;
    }

    private long convertContentLength(String valueString) {
        try {
            return Long.parseLong(valueString);
        }
        catch (NumberFormatException e) {
            LOG.ignore(e);
            throw new BadMessageException(400, "Invalid Content-Length Value", e);
        }
    }

    protected boolean parseFields(ByteBuffer buffer) {
        HttpTokens.Token t;
        block37: while ((this._state == State.HEADER || this._state == State.TRAILER) && buffer.hasRemaining() && (t = this.next(buffer)) != null) {
            if (this._maxHeaderBytes > 0 && ++this._headerBytes > this._maxHeaderBytes) {
                boolean header = this._state == State.HEADER;
                LOG.warn("{} is too large {}>{}", header ? "Header" : "Trailer", this._headerBytes, this._maxHeaderBytes);
                throw new BadMessageException(header ? 431 : 413);
            }
            switch (this._fieldState) {
                case FIELD: {
                    switch (t.getType()) {
                        case HTAB: 
                        case SPACE: 
                        case COLON: {
                            if (this.complianceViolation(HttpComplianceSection.NO_FIELD_FOLDING, this._headerString)) {
                                throw new BadMessageException(400, "Header Folding");
                            }
                            if (StringUtil.isEmpty(this._valueString)) {
                                this._string.setLength(0);
                                this._length = 0;
                            } else {
                                this.setString(this._valueString);
                                this._string.append(' ');
                                ++this._length;
                                this._valueString = null;
                            }
                            this.setState(FieldState.VALUE);
                            continue block37;
                        }
                        case LF: {
                            if (this._state == State.HEADER) {
                                this.parsedHeader();
                            } else {
                                this.parsedTrailer();
                            }
                            this._contentPosition = 0L;
                            if (this._state == State.TRAILER) {
                                this.setState(State.END);
                                return this._handler.messageComplete();
                            }
                            if (this._hasTransferEncoding && this._endOfContent != HttpTokens.EndOfContent.CHUNKED_CONTENT && (this._responseHandler == null || this._endOfContent != HttpTokens.EndOfContent.EOF_CONTENT)) {
                                throw new BadMessageException(400, "Bad Transfer-Encoding, chunked not last");
                            }
                            if (!this._host && this._version == HttpVersion.HTTP_1_1 && this._requestHandler != null) {
                                throw new BadMessageException(400, "No Host");
                            }
                            if (this._responseHandler != null && (this._responseStatus == 304 || this._responseStatus == 204 || this._responseStatus < 200)) {
                                this._endOfContent = HttpTokens.EndOfContent.NO_CONTENT;
                            } else if (this._endOfContent == HttpTokens.EndOfContent.UNKNOWN_CONTENT) {
                                this._endOfContent = this._responseStatus == 0 || this._responseStatus == 304 || this._responseStatus == 204 || this._responseStatus < 200 ? HttpTokens.EndOfContent.NO_CONTENT : HttpTokens.EndOfContent.EOF_CONTENT;
                            }
                            switch (this._endOfContent) {
                                case EOF_CONTENT: {
                                    this.setState(State.EOF_CONTENT);
                                    boolean handle = this._handler.headerComplete();
                                    this._headerComplete = true;
                                    return handle;
                                }
                                case CHUNKED_CONTENT: {
                                    this.setState(State.CHUNKED_CONTENT);
                                    boolean handle = this._handler.headerComplete();
                                    this._headerComplete = true;
                                    return handle;
                                }
                            }
                            this.setState(State.CONTENT);
                            boolean handle = this._handler.headerComplete();
                            this._headerComplete = true;
                            return handle;
                        }
                        case ALPHA: 
                        case DIGIT: 
                        case TCHAR: {
                            if (this._state == State.HEADER) {
                                this.parsedHeader();
                            } else {
                                this.parsedTrailer();
                            }
                            if (buffer.hasRemaining()) {
                                HttpField cachedField;
                                HttpField httpField = cachedField = this._fieldCache == null ? null : this._fieldCache.getBest(buffer, -1, buffer.remaining());
                                if (cachedField == null) {
                                    cachedField = CACHE.getBest(buffer, -1, buffer.remaining());
                                }
                                if (cachedField != null) {
                                    String ev;
                                    String en;
                                    String n = cachedField.getName();
                                    String v = cachedField.getValue();
                                    if (!this._compliances.contains((Object)HttpComplianceSection.FIELD_NAME_CASE_INSENSITIVE) && !n.equals(en = BufferUtil.toString(buffer, buffer.position() - 1, n.length(), StandardCharsets.US_ASCII))) {
                                        this.handleViolation(HttpComplianceSection.FIELD_NAME_CASE_INSENSITIVE, en);
                                        n = en;
                                        cachedField = new HttpField(cachedField.getHeader(), n, v);
                                    }
                                    if (v != null && !this._compliances.contains((Object)HttpComplianceSection.CASE_INSENSITIVE_FIELD_VALUE_CACHE) && !v.equals(ev = BufferUtil.toString(buffer, buffer.position() + n.length() + 1, v.length(), StandardCharsets.ISO_8859_1))) {
                                        this.handleViolation(HttpComplianceSection.CASE_INSENSITIVE_FIELD_VALUE_CACHE, ev + "!=" + v);
                                        v = ev;
                                        cachedField = new HttpField(cachedField.getHeader(), n, v);
                                    }
                                    this._header = cachedField.getHeader();
                                    this._headerString = n;
                                    if (v == null) {
                                        this.setState(FieldState.VALUE);
                                        this._string.setLength(0);
                                        this._length = 0;
                                        buffer.position(buffer.position() + n.length() + 1);
                                        continue block37;
                                    }
                                    int pos = buffer.position() + n.length() + v.length() + 1;
                                    byte peek = buffer.get(pos);
                                    if (peek == 13 || peek == 10) {
                                        this._field = cachedField;
                                        this._valueString = v;
                                        this.setState(FieldState.IN_VALUE);
                                        if (peek == 13) {
                                            this._cr = true;
                                            buffer.position(pos + 1);
                                            continue block37;
                                        }
                                        buffer.position(pos);
                                        continue block37;
                                    }
                                    this.setState(FieldState.IN_VALUE);
                                    this.setString(v);
                                    buffer.position(pos);
                                    continue block37;
                                }
                            }
                            this.setState(FieldState.IN_NAME);
                            this._string.setLength(0);
                            this._string.append(t.getChar());
                            this._length = 1;
                            continue block37;
                        }
                    }
                    throw new IllegalCharacterException(this._state, t, buffer);
                }
                case IN_NAME: {
                    switch (t.getType()) {
                        case HTAB: 
                        case SPACE: {
                            if (!this.complianceViolation(HttpComplianceSection.NO_WS_AFTER_FIELD_NAME, null)) {
                                this._headerString = this.takeString();
                                this._header = HttpHeader.CACHE.get(this._headerString);
                                this._length = -1;
                                this.setState(FieldState.WS_AFTER_NAME);
                                continue block37;
                            }
                            throw new IllegalCharacterException(this._state, t, buffer);
                        }
                        case COLON: {
                            this._headerString = this.takeString();
                            this._header = HttpHeader.CACHE.get(this._headerString);
                            this._length = -1;
                            this.setState(FieldState.VALUE);
                            continue block37;
                        }
                        case LF: {
                            this._headerString = this.takeString();
                            this._header = HttpHeader.CACHE.get(this._headerString);
                            this._string.setLength(0);
                            this._valueString = "";
                            this._length = -1;
                            if (!this.complianceViolation(HttpComplianceSection.FIELD_COLON, this._headerString)) {
                                this.setState(FieldState.FIELD);
                                continue block37;
                            }
                            throw new IllegalCharacterException(this._state, t, buffer);
                        }
                        case ALPHA: 
                        case DIGIT: 
                        case TCHAR: {
                            this._string.append(t.getChar());
                            this._length = this._string.length();
                            continue block37;
                        }
                    }
                    throw new IllegalCharacterException(this._state, t, buffer);
                }
                case WS_AFTER_NAME: {
                    switch (t.getType()) {
                        case HTAB: 
                        case SPACE: {
                            continue block37;
                        }
                        case COLON: {
                            this.setState(FieldState.VALUE);
                            continue block37;
                        }
                        case LF: {
                            if (!this.complianceViolation(HttpComplianceSection.FIELD_COLON, this._headerString)) {
                                this.setState(FieldState.FIELD);
                                continue block37;
                            }
                            throw new IllegalCharacterException(this._state, t, buffer);
                        }
                    }
                    throw new IllegalCharacterException(this._state, t, buffer);
                }
                case VALUE: {
                    switch (t.getType()) {
                        case LF: {
                            this._string.setLength(0);
                            this._valueString = "";
                            this._length = -1;
                            this.setState(FieldState.FIELD);
                            continue block37;
                        }
                        case HTAB: 
                        case SPACE: {
                            continue block37;
                        }
                        case ALPHA: 
                        case DIGIT: 
                        case TCHAR: 
                        case VCHAR: 
                        case OTEXT: 
                        case COLON: {
                            this._string.append(t.getChar());
                            this._length = this._string.length();
                            this.setState(FieldState.IN_VALUE);
                            continue block37;
                        }
                    }
                    throw new IllegalCharacterException(this._state, t, buffer);
                }
                case IN_VALUE: {
                    switch (t.getType()) {
                        case LF: {
                            if (this._length > 0) {
                                this._valueString = this.takeString();
                                this._length = -1;
                            }
                            this.setState(FieldState.FIELD);
                            continue block37;
                        }
                        case HTAB: 
                        case SPACE: {
                            this._string.append(t.getChar());
                            continue block37;
                        }
                        case ALPHA: 
                        case DIGIT: 
                        case TCHAR: 
                        case VCHAR: 
                        case OTEXT: 
                        case COLON: {
                            this._string.append(t.getChar());
                            this._length = this._string.length();
                            continue block37;
                        }
                    }
                    throw new IllegalCharacterException(this._state, t, buffer);
                }
            }
            throw new IllegalStateException(this._state.toString());
        }
        return false;
    }

    public boolean parseNext(ByteBuffer buffer) {
        if (this.debug) {
            LOG.debug("parseNext s={} {}", new Object[]{this._state, BufferUtil.toDetailString(buffer)});
        }
        try {
            if (this._state == State.START) {
                this._version = null;
                this._method = null;
                this._methodString = null;
                this._endOfContent = HttpTokens.EndOfContent.UNKNOWN_CONTENT;
                this._header = null;
                if (this.quickStart(buffer)) {
                    return true;
                }
            }
            if (this._state.ordinal() >= State.START.ordinal() && this._state.ordinal() < State.HEADER.ordinal() && this.parseLine(buffer)) {
                return true;
            }
            if (this._state == State.HEADER && this.parseFields(buffer)) {
                return true;
            }
            if (this._state.ordinal() >= State.CONTENT.ordinal() && this._state.ordinal() < State.TRAILER.ordinal()) {
                if (this._responseStatus > 0 && this._headResponse) {
                    if (this._state != State.CONTENT_END) {
                        this.setState(State.CONTENT_END);
                        return this.handleContentMessage();
                    }
                    this.setState(State.END);
                    return this._handler.messageComplete();
                }
                if (this.parseContent(buffer)) {
                    return true;
                }
            }
            if (this._state == State.TRAILER && this.parseFields(buffer)) {
                return true;
            }
            if (this._state == State.END) {
                byte b;
                int whiteSpace = 0;
                while (buffer.remaining() > 0 && ((b = buffer.get(buffer.position())) == 13 || b == 10)) {
                    buffer.get();
                    ++whiteSpace;
                }
                if (this.debug && whiteSpace > 0) {
                    LOG.debug("Discarded {} CR or LF characters", whiteSpace);
                }
            } else if (this.isClose() || this.isClosed()) {
                BufferUtil.clear(buffer);
            }
            if (this.isAtEOF() && !buffer.hasRemaining()) {
                switch (this._state) {
                    case CLOSED: {
                        break;
                    }
                    case END: 
                    case CLOSE: {
                        this.setState(State.CLOSED);
                        break;
                    }
                    case EOF_CONTENT: 
                    case TRAILER: {
                        if (this._fieldState == FieldState.FIELD) {
                            this.setState(State.CONTENT_END);
                            boolean handle = this.handleContentMessage();
                            if (handle && this._state == State.CONTENT_END) {
                                return true;
                            }
                            this.setState(State.CLOSED);
                            return handle;
                        }
                        this.setState(State.CLOSED);
                        this._handler.earlyEOF();
                        break;
                    }
                    case START: 
                    case CONTENT: 
                    case CHUNKED_CONTENT: 
                    case CHUNK_SIZE: 
                    case CHUNK_PARAMS: 
                    case CHUNK: {
                        this.setState(State.CLOSED);
                        this._handler.earlyEOF();
                        break;
                    }
                    default: {
                        if (this.debug) {
                            LOG.debug("{} EOF in {}", new Object[]{this, this._state});
                        }
                        this.setState(State.CLOSED);
                        this._handler.badMessage(new BadMessageException(400));
                    }
                }
            }
        }
        catch (BadMessageException x) {
            BufferUtil.clear(buffer);
            this.badMessage(x);
        }
        catch (Throwable x) {
            BufferUtil.clear(buffer);
            this.badMessage(new BadMessageException(400, this._requestHandler != null ? "Bad Request" : "Bad Response", x));
        }
        return false;
    }

    protected void badMessage(BadMessageException x) {
        if (this.debug) {
            LOG.debug("Parse exception: " + this + " for " + this._handler, x);
        }
        this.setState(State.CLOSE);
        if (this._headerComplete) {
            this._handler.earlyEOF();
        } else {
            this._handler.badMessage(x);
        }
    }

    protected boolean parseContent(ByteBuffer buffer) {
        long content;
        int remaining = buffer.remaining();
        if (remaining == 0) {
            switch (this._state) {
                case CONTENT: {
                    content = this._contentLength - this._contentPosition;
                    if (this._endOfContent != HttpTokens.EndOfContent.NO_CONTENT && content != 0L) break;
                    this.setState(State.CONTENT_END);
                    return this.handleContentMessage();
                }
                case CONTENT_END: {
                    this.setState(this._endOfContent == HttpTokens.EndOfContent.EOF_CONTENT ? State.CLOSED : State.END);
                    return this._handler.messageComplete();
                }
                default: {
                    return false;
                }
            }
        }
        while (this._state.ordinal() < State.TRAILER.ordinal() && remaining > 0) {
            block4 : switch (this._state) {
                case EOF_CONTENT: {
                    this._contentChunk = buffer.asReadOnlyBuffer();
                    this._contentPosition += (long)remaining;
                    buffer.position(buffer.position() + remaining);
                    if (!this._handler.content(this._contentChunk)) break;
                    return true;
                }
                case CONTENT: {
                    content = this._contentLength - this._contentPosition;
                    if (this._endOfContent == HttpTokens.EndOfContent.NO_CONTENT || content == 0L) {
                        this.setState(State.CONTENT_END);
                        return this.handleContentMessage();
                    }
                    this._contentChunk = buffer.asReadOnlyBuffer();
                    if ((long)remaining > content) {
                        this._contentChunk.limit(this._contentChunk.position() + (int)content);
                    }
                    this._contentPosition += (long)this._contentChunk.remaining();
                    buffer.position(buffer.position() + this._contentChunk.remaining());
                    if (this._handler.content(this._contentChunk)) {
                        return true;
                    }
                    if (this._contentPosition != this._contentLength) break;
                    this.setState(State.CONTENT_END);
                    return this.handleContentMessage();
                }
                case CHUNKED_CONTENT: {
                    HttpTokens.Token t = this.next(buffer);
                    if (t == null) break;
                    switch (t.getType()) {
                        case LF: {
                            break block4;
                        }
                        case DIGIT: {
                            this._chunkLength = t.getHexDigit();
                            this._chunkPosition = 0;
                            this.setState(State.CHUNK_SIZE);
                            break block4;
                        }
                        case ALPHA: {
                            if (t.isHexDigit()) {
                                this._chunkLength = t.getHexDigit();
                                this._chunkPosition = 0;
                                this.setState(State.CHUNK_SIZE);
                                break block4;
                            }
                            throw new IllegalCharacterException(this._state, t, buffer);
                        }
                    }
                    throw new IllegalCharacterException(this._state, t, buffer);
                }
                case CHUNK_SIZE: {
                    HttpTokens.Token t = this.next(buffer);
                    if (t == null) break;
                    switch (t.getType()) {
                        case LF: {
                            if (this._chunkLength == 0) {
                                this.setState(State.TRAILER);
                                if (!this._handler.contentComplete()) break block4;
                                return true;
                            }
                            this.setState(State.CHUNK);
                            break block4;
                        }
                        case SPACE: {
                            this.setState(State.CHUNK_PARAMS);
                            break block4;
                        }
                    }
                    if (t.isHexDigit()) {
                        if (this._chunkLength > 0x7FFFFEF) {
                            throw new BadMessageException(413);
                        }
                        this._chunkLength = this._chunkLength * 16 + t.getHexDigit();
                        break;
                    }
                    this.setState(State.CHUNK_PARAMS);
                    break;
                }
                case CHUNK_PARAMS: {
                    HttpTokens.Token t = this.next(buffer);
                    if (t == null) break;
                    switch (t.getType()) {
                        case LF: {
                            if (this._chunkLength == 0) {
                                this.setState(State.TRAILER);
                                if (!this._handler.contentComplete()) break block4;
                                return true;
                            }
                            this.setState(State.CHUNK);
                            break block4;
                        }
                    }
                    break;
                }
                case CHUNK: {
                    int chunk = this._chunkLength - this._chunkPosition;
                    if (chunk == 0) {
                        this.setState(State.CHUNKED_CONTENT);
                        break;
                    }
                    this._contentChunk = buffer.asReadOnlyBuffer();
                    if (remaining > chunk) {
                        this._contentChunk.limit(this._contentChunk.position() + chunk);
                    }
                    chunk = this._contentChunk.remaining();
                    this._contentPosition += (long)chunk;
                    this._chunkPosition += chunk;
                    buffer.position(buffer.position() + chunk);
                    if (!this._handler.content(this._contentChunk)) break;
                    return true;
                }
                case CONTENT_END: {
                    this.setState(this._endOfContent == HttpTokens.EndOfContent.EOF_CONTENT ? State.CLOSED : State.END);
                    return this._handler.messageComplete();
                }
            }
            remaining = buffer.remaining();
        }
        return false;
    }

    public boolean isAtEOF() {
        return this._eof;
    }

    public void atEOF() {
        if (this.debug) {
            LOG.debug("atEOF {}", this);
        }
        this._eof = true;
    }

    public void close() {
        if (this.debug) {
            LOG.debug("close {}", this);
        }
        this.setState(State.CLOSE);
    }

    public void reset() {
        if (this.debug) {
            LOG.debug("reset {}", this);
        }
        if (this._state == State.CLOSE || this._state == State.CLOSED) {
            return;
        }
        this.setState(State.START);
        this._endOfContent = HttpTokens.EndOfContent.UNKNOWN_CONTENT;
        this._contentLength = -1L;
        this._hasContentLength = false;
        this._hasTransferEncoding = false;
        this._contentPosition = 0L;
        this._responseStatus = 0;
        this._contentChunk = null;
        this._headerBytes = 0;
        this._host = false;
        this._headerComplete = false;
    }

    protected void setState(State state) {
        if (this.debug) {
            LOG.debug("{} --> {}", new Object[]{this._state, state});
        }
        this._state = state;
    }

    protected void setState(FieldState state) {
        if (this.debug) {
            LOG.debug("{}:{} --> {}", new Object[]{this._state, this._field != null ? this._field : (this._headerString != null ? this._headerString : this._string), state});
        }
        this._fieldState = state;
    }

    public Trie<HttpField> getFieldCache() {
        return this._fieldCache;
    }

    public String toString() {
        return String.format("%s{s=%s,%d of %d}", new Object[]{this.getClass().getSimpleName(), this._state, this.getContentRead(), this.getContentLength()});
    }

    static {
        CACHE.put(new HttpField(HttpHeader.CONNECTION, HttpHeaderValue.CLOSE));
        CACHE.put(new HttpField(HttpHeader.CONNECTION, HttpHeaderValue.KEEP_ALIVE));
        CACHE.put(new HttpField(HttpHeader.CONNECTION, HttpHeaderValue.UPGRADE));
        CACHE.put(new HttpField(HttpHeader.ACCEPT_ENCODING, "gzip"));
        CACHE.put(new HttpField(HttpHeader.ACCEPT_ENCODING, "gzip, deflate"));
        CACHE.put(new HttpField(HttpHeader.ACCEPT_ENCODING, "gzip, deflate, br"));
        CACHE.put(new HttpField(HttpHeader.ACCEPT_ENCODING, "gzip,deflate,sdch"));
        CACHE.put(new HttpField(HttpHeader.ACCEPT_LANGUAGE, "en-US,en;q=0.5"));
        CACHE.put(new HttpField(HttpHeader.ACCEPT_LANGUAGE, "en-GB,en-US;q=0.8,en;q=0.6"));
        CACHE.put(new HttpField(HttpHeader.ACCEPT_LANGUAGE, "en-AU,en;q=0.9,it-IT;q=0.8,it;q=0.7,en-GB;q=0.6,en-US;q=0.5"));
        CACHE.put(new HttpField(HttpHeader.ACCEPT_CHARSET, "ISO-8859-1,utf-8;q=0.7,*;q=0.3"));
        CACHE.put(new HttpField(HttpHeader.ACCEPT, "*/*"));
        CACHE.put(new HttpField(HttpHeader.ACCEPT, "image/png,image/*;q=0.8,*/*;q=0.5"));
        CACHE.put(new HttpField(HttpHeader.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"));
        CACHE.put(new HttpField(HttpHeader.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8"));
        CACHE.put(new HttpField(HttpHeader.ACCEPT_RANGES, HttpHeaderValue.BYTES));
        CACHE.put(new HttpField(HttpHeader.PRAGMA, "no-cache"));
        CACHE.put(new HttpField(HttpHeader.CACHE_CONTROL, "private, no-cache, no-cache=Set-Cookie, proxy-revalidate"));
        CACHE.put(new HttpField(HttpHeader.CACHE_CONTROL, "no-cache"));
        CACHE.put(new HttpField(HttpHeader.CACHE_CONTROL, "max-age=0"));
        CACHE.put(new HttpField(HttpHeader.CONTENT_LENGTH, "0"));
        CACHE.put(new HttpField(HttpHeader.CONTENT_ENCODING, "gzip"));
        CACHE.put(new HttpField(HttpHeader.CONTENT_ENCODING, "deflate"));
        CACHE.put(new HttpField(HttpHeader.TRANSFER_ENCODING, "chunked"));
        CACHE.put(new HttpField(HttpHeader.EXPIRES, "Fri, 01 Jan 1990 00:00:00 GMT"));
        for (String string : new String[]{"text/plain", "text/html", "text/xml", "text/json", "application/json", "application/x-www-form-urlencoded"}) {
            PreEncodedHttpField field = new PreEncodedHttpField(HttpHeader.CONTENT_TYPE, string);
            CACHE.put(field);
            for (String charset : new String[]{"utf-8", "iso-8859-1"}) {
                CACHE.put(new PreEncodedHttpField(HttpHeader.CONTENT_TYPE, string + ";charset=" + charset));
                CACHE.put(new PreEncodedHttpField(HttpHeader.CONTENT_TYPE, string + "; charset=" + charset));
                CACHE.put(new PreEncodedHttpField(HttpHeader.CONTENT_TYPE, string + ";charset=" + charset.toUpperCase(Locale.ENGLISH)));
                CACHE.put(new PreEncodedHttpField(HttpHeader.CONTENT_TYPE, string + "; charset=" + charset.toUpperCase(Locale.ENGLISH)));
            }
        }
        for (HttpHeader httpHeader : HttpHeader.values()) {
            if (httpHeader.isPseudo() || CACHE.put(new HttpField(httpHeader, (String)null))) continue;
            throw new IllegalStateException("CACHE FULL");
        }
    }

    private static class IllegalCharacterException
    extends BadMessageException {
        private IllegalCharacterException(State state, HttpTokens.Token token, ByteBuffer buffer) {
            super(400, String.format("Illegal character %s", token));
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Illegal character %s in state=%s for buffer %s", new Object[]{token, state, BufferUtil.toDetailString(buffer)}), new Object[0]);
            }
        }
    }

    public static interface ComplianceHandler
    extends HttpHandler {
        @Deprecated
        default public void onComplianceViolation(HttpCompliance compliance, HttpCompliance required, String reason) {
        }

        default public void onComplianceViolation(HttpCompliance compliance, HttpComplianceSection violation, String details) {
            this.onComplianceViolation(compliance, HttpCompliance.requiredCompliance(violation), details);
        }
    }

    public static interface ResponseHandler
    extends HttpHandler {
        public boolean startResponse(HttpVersion var1, int var2, String var3);
    }

    public static interface RequestHandler
    extends HttpHandler {
        public boolean startRequest(String var1, String var2, HttpVersion var3);
    }

    public static interface HttpHandler {
        public boolean content(ByteBuffer var1);

        public boolean headerComplete();

        public boolean contentComplete();

        public boolean messageComplete();

        public void parsedHeader(HttpField var1);

        default public void parsedTrailer(HttpField field) {
        }

        public void earlyEOF();

        default public void badMessage(BadMessageException failure) {
            this.badMessage(failure.getCode(), failure.getReason());
        }

        @Deprecated
        default public void badMessage(int status, String reason) {
        }

        public int getHeaderCacheSize();
    }

    public static enum State {
        START,
        METHOD,
        RESPONSE_VERSION,
        SPACE1,
        STATUS,
        URI,
        SPACE2,
        REQUEST_VERSION,
        REASON,
        PROXY,
        HEADER,
        CONTENT,
        EOF_CONTENT,
        CHUNKED_CONTENT,
        CHUNK_SIZE,
        CHUNK_PARAMS,
        CHUNK,
        CONTENT_END,
        TRAILER,
        END,
        CLOSE,
        CLOSED;

    }

    public static enum FieldState {
        FIELD,
        IN_NAME,
        VALUE,
        IN_VALUE,
        WS_AFTER_NAME;

    }
}

