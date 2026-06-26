/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.http;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.Objects;
import org.eclipse.jetty.http.BadMessageException;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.util.ArrayTrie;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.Trie;
import org.eclipse.jetty.util.TypeUtil;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.UrlEncoded;

public class HttpURI {
    private static final Trie<Boolean> __ambiguousSegments = new ArrayTrie<Boolean>();
    private String _scheme;
    private String _user;
    private String _host;
    private int _port;
    private String _path;
    private String _param;
    private String _query;
    private String _fragment;
    private String _uri;
    private String _decodedPath;
    private final EnumSet<Ambiguous> _ambiguous = EnumSet.noneOf(Ambiguous.class);

    public static HttpURI createHttpURI(String scheme, String host, int port, String path, String param, String query, String fragment) {
        if (port == 80 && HttpScheme.HTTP.is(scheme)) {
            port = 0;
        }
        if (port == 443 && HttpScheme.HTTPS.is(scheme)) {
            port = 0;
        }
        return new HttpURI(scheme, host, port, path, param, query, fragment);
    }

    public HttpURI() {
    }

    public HttpURI(String scheme, String host, int port, String path, String param, String query, String fragment) {
        this._scheme = scheme;
        this._host = host;
        this._port = port;
        if (path != null) {
            this.parse(State.PATH, path, 0, path.length());
        }
        if (param != null) {
            this._param = param;
        }
        if (query != null) {
            this._query = query;
        }
        if (fragment != null) {
            this._fragment = fragment;
        }
    }

    public HttpURI(HttpURI uri) {
        this._scheme = uri._scheme;
        this._user = uri._user;
        this._host = uri._host;
        this._port = uri._port;
        this._path = uri._path;
        this._param = uri._param;
        this._query = uri._query;
        this._fragment = uri._fragment;
        this._uri = uri._uri;
        this._decodedPath = uri._decodedPath;
        this._ambiguous.addAll(uri._ambiguous);
    }

    public HttpURI(String uri) {
        this._port = -1;
        this.parse(State.START, uri, 0, uri.length());
    }

    public HttpURI(URI uri) {
        this._uri = null;
        this._scheme = uri.getScheme();
        this._host = uri.getHost();
        if (this._host == null && uri.getRawSchemeSpecificPart().startsWith("//")) {
            this._host = "";
        }
        this._port = uri.getPort();
        this._user = uri.getUserInfo();
        String path = uri.getRawPath();
        if (path != null) {
            this.parse(State.PATH, path, 0, path.length());
        }
        this._query = uri.getRawQuery();
        this._fragment = uri.getFragment();
    }

    public HttpURI(String scheme, String host, int port, String pathQuery) {
        this._uri = null;
        this._scheme = scheme;
        this._host = host;
        this._port = port;
        if (pathQuery != null) {
            this.parse(State.PATH, pathQuery, 0, pathQuery.length());
        }
    }

    public void clear() {
        this._uri = null;
        this._scheme = null;
        this._user = null;
        this._host = null;
        this._port = -1;
        this._path = null;
        this._param = null;
        this._query = null;
        this._fragment = null;
        this._decodedPath = null;
        this._ambiguous.clear();
    }

    public void parse(String uri) {
        this.clear();
        this._uri = uri;
        this.parse(State.START, uri, 0, uri.length());
    }

    public void parseRequestTarget(String method, String uri) {
        this.clear();
        this._uri = uri;
        if (HttpMethod.CONNECT.is(method)) {
            this._path = uri;
        } else {
            this.parse(uri.startsWith("/") ? State.PATH : State.START, uri, 0, uri.length());
        }
    }

    @Deprecated
    public void parseConnect(String uri) {
        this.clear();
        this._uri = uri;
        this._path = uri;
    }

    public void parse(String uri, int offset, int length) {
        this.clear();
        int end = offset + length;
        this._uri = uri.substring(offset, end);
        this.parse(State.START, uri, offset, end);
    }

    private void parse(State state, String uri, int offset, int end) {
        int mark = offset;
        int pathMark = 0;
        int segment = 0;
        boolean encoded = false;
        boolean dot = false;
        int escapedSlash = 0;
        block73: for (int i = offset; i < end; ++i) {
            char c = uri.charAt(i);
            switch (state) {
                case START: {
                    switch (c) {
                        case '/': {
                            mark = i;
                            state = State.HOST_OR_PATH;
                            continue block73;
                        }
                        case ';': {
                            mark = i + 1;
                            state = State.PARAM;
                            continue block73;
                        }
                        case '?': {
                            this._path = "";
                            mark = i + 1;
                            state = State.QUERY;
                            continue block73;
                        }
                        case '#': {
                            mark = i + 1;
                            state = State.FRAGMENT;
                            continue block73;
                        }
                        case '*': {
                            this._path = "*";
                            state = State.ASTERISK;
                            continue block73;
                        }
                        case '%': {
                            encoded = true;
                            escapedSlash = 1;
                            pathMark = segment = i;
                            mark = segment;
                            state = State.PATH;
                            continue block73;
                        }
                        case '.': {
                            dot = true;
                            pathMark = segment = i;
                            state = State.PATH;
                            continue block73;
                        }
                    }
                    mark = i;
                    if (this._scheme == null) {
                        state = State.SCHEME_OR_PATH;
                        continue block73;
                    }
                    pathMark = segment = i;
                    state = State.PATH;
                    continue block73;
                }
                case SCHEME_OR_PATH: {
                    switch (c) {
                        case ':': {
                            this._scheme = uri.substring(mark, i);
                            state = State.START;
                            continue block73;
                        }
                        case '/': {
                            segment = i + 1;
                            state = State.PATH;
                            continue block73;
                        }
                        case ';': {
                            mark = i + 1;
                            state = State.PARAM;
                            continue block73;
                        }
                        case '?': {
                            this._path = uri.substring(mark, i);
                            mark = i + 1;
                            state = State.QUERY;
                            continue block73;
                        }
                        case '%': {
                            encoded = true;
                            escapedSlash = 1;
                            state = State.PATH;
                            continue block73;
                        }
                        case '#': {
                            this._path = uri.substring(mark, i);
                            state = State.FRAGMENT;
                            continue block73;
                        }
                    }
                    continue block73;
                }
                case HOST_OR_PATH: {
                    switch (c) {
                        case '/': {
                            this._host = "";
                            mark = i + 1;
                            state = State.HOST;
                            continue block73;
                        }
                        case '#': 
                        case '%': 
                        case '.': 
                        case ';': 
                        case '?': 
                        case '@': {
                            --i;
                            pathMark = mark;
                            segment = mark + 1;
                            state = State.PATH;
                            continue block73;
                        }
                    }
                    pathMark = mark;
                    segment = mark + 1;
                    state = State.PATH;
                    continue block73;
                }
                case HOST: {
                    switch (c) {
                        case '/': {
                            this._host = uri.substring(mark, i);
                            pathMark = mark = i;
                            segment = mark + 1;
                            state = State.PATH;
                            continue block73;
                        }
                        case ':': {
                            if (i > mark) {
                                this._host = uri.substring(mark, i);
                            }
                            mark = i + 1;
                            state = State.PORT;
                            continue block73;
                        }
                        case '@': {
                            if (this._user != null) {
                                throw new IllegalArgumentException("Bad authority");
                            }
                            this._user = uri.substring(mark, i);
                            mark = i + 1;
                            continue block73;
                        }
                        case '[': {
                            state = State.IPV6;
                            continue block73;
                        }
                    }
                    continue block73;
                }
                case IPV6: {
                    switch (c) {
                        case '/': {
                            throw new IllegalArgumentException("No closing ']' for ipv6 in " + uri);
                        }
                        case ']': {
                            c = uri.charAt(++i);
                            this._host = uri.substring(mark, i);
                            if (c == ':') {
                                mark = i + 1;
                                state = State.PORT;
                                continue block73;
                            }
                            pathMark = mark = i;
                            state = State.PATH;
                            continue block73;
                        }
                    }
                    continue block73;
                }
                case PORT: {
                    if (c == '@') {
                        if (this._user != null) {
                            throw new IllegalArgumentException("Bad authority");
                        }
                        this._user = this._host + ":" + uri.substring(mark, i);
                        mark = i + 1;
                        state = State.HOST;
                        continue block73;
                    }
                    if (c != '/') continue block73;
                    this._port = TypeUtil.parseInt(uri, mark, i - mark, 10);
                    pathMark = mark = i;
                    segment = i + 1;
                    state = State.PATH;
                    continue block73;
                }
                case PATH: {
                    switch (c) {
                        case ';': {
                            this.checkSegment(uri, segment, i, true);
                            mark = i + 1;
                            state = State.PARAM;
                            continue block73;
                        }
                        case '?': {
                            this.checkSegment(uri, segment, i, false);
                            this._path = uri.substring(pathMark, i);
                            mark = i + 1;
                            state = State.QUERY;
                            continue block73;
                        }
                        case '#': {
                            this.checkSegment(uri, segment, i, false);
                            this._path = uri.substring(pathMark, i);
                            mark = i + 1;
                            state = State.FRAGMENT;
                            continue block73;
                        }
                        case '/': {
                            this.checkSegment(uri, segment, i, false);
                            segment = i + 1;
                            continue block73;
                        }
                        case '.': {
                            dot |= segment == i;
                            continue block73;
                        }
                        case '%': {
                            encoded = true;
                            escapedSlash = 1;
                            continue block73;
                        }
                        case '2': {
                            escapedSlash = escapedSlash == 1 ? 2 : 0;
                            continue block73;
                        }
                        case 'F': 
                        case 'f': {
                            if (escapedSlash == 2) {
                                this._ambiguous.add(Ambiguous.SEPARATOR);
                            }
                            escapedSlash = 0;
                            continue block73;
                        }
                    }
                    escapedSlash = 0;
                    continue block73;
                }
                case PARAM: {
                    switch (c) {
                        case '?': {
                            this._path = uri.substring(pathMark, i);
                            this._param = uri.substring(mark, i);
                            mark = i + 1;
                            state = State.QUERY;
                            continue block73;
                        }
                        case '#': {
                            this._path = uri.substring(pathMark, i);
                            this._param = uri.substring(mark, i);
                            mark = i + 1;
                            state = State.FRAGMENT;
                            continue block73;
                        }
                        case '/': {
                            encoded = true;
                            segment = i + 1;
                            state = State.PATH;
                            continue block73;
                        }
                        case ';': {
                            mark = i + 1;
                            continue block73;
                        }
                    }
                    continue block73;
                }
                case QUERY: {
                    if (c != '#') continue block73;
                    this._query = uri.substring(mark, i);
                    mark = i + 1;
                    state = State.FRAGMENT;
                    continue block73;
                }
                case ASTERISK: {
                    throw new IllegalArgumentException("Bad character '*'");
                }
                case FRAGMENT: {
                    this._fragment = uri.substring(mark, end);
                    i = end;
                    continue block73;
                }
            }
        }
        switch (state) {
            case START: {
                break;
            }
            case SCHEME_OR_PATH: {
                this._path = uri.substring(mark, end);
                break;
            }
            case HOST_OR_PATH: {
                this._path = uri.substring(mark, end);
                break;
            }
            case HOST: {
                if (end <= mark) break;
                this._host = uri.substring(mark, end);
                break;
            }
            case IPV6: {
                throw new IllegalArgumentException("No closing ']' for ipv6 in " + uri);
            }
            case PORT: {
                this._port = TypeUtil.parseInt(uri, mark, end - mark, 10);
                break;
            }
            case ASTERISK: {
                break;
            }
            case FRAGMENT: {
                this._fragment = uri.substring(mark, end);
                break;
            }
            case PARAM: {
                this._path = uri.substring(pathMark, end);
                this._param = uri.substring(mark, end);
                break;
            }
            case PATH: {
                this.checkSegment(uri, segment, end, false);
                this._path = uri.substring(pathMark, end);
                break;
            }
            case QUERY: {
                this._query = uri.substring(mark, end);
                break;
            }
        }
        if (!encoded && !dot) {
            this._decodedPath = this._param == null ? this._path : this._path.substring(0, this._path.length() - this._param.length() - 1);
        } else if (this._path != null) {
            String canonical = URIUtil.canonicalPath(this._path);
            if (canonical == null) {
                throw new BadMessageException("Bad URI");
            }
            this._decodedPath = URIUtil.decodePath(canonical);
        }
    }

    private void checkSegment(String uri, int segment, int end, boolean param) {
        if (!this._ambiguous.contains((Object)Ambiguous.SEGMENT)) {
            Boolean ambiguous = __ambiguousSegments.get(uri, segment, end - segment);
            if (ambiguous == Boolean.TRUE) {
                this._ambiguous.add(Ambiguous.SEGMENT);
            } else if (param && ambiguous == Boolean.FALSE) {
                this._ambiguous.add(Ambiguous.PARAM);
            }
        }
    }

    public boolean hasAmbiguousSegment() {
        return this._ambiguous.contains((Object)Ambiguous.SEGMENT);
    }

    public boolean hasAmbiguousSeparator() {
        return this._ambiguous.contains((Object)Ambiguous.SEPARATOR);
    }

    public boolean hasAmbiguousParameter() {
        return this._ambiguous.contains((Object)Ambiguous.PARAM);
    }

    public boolean isAmbiguous() {
        return !this._ambiguous.isEmpty();
    }

    public String getScheme() {
        return this._scheme;
    }

    public String getHost() {
        if (this._host != null && this._host.isEmpty()) {
            return null;
        }
        return this._host;
    }

    public int getPort() {
        return this._port;
    }

    public String getPath() {
        return this._path;
    }

    public String getDecodedPath() {
        return this._decodedPath;
    }

    public String getParam() {
        return this._param;
    }

    public void setParam(String param) {
        if (!Objects.equals(this._param, param)) {
            if (this._param != null && this._path.endsWith(";" + this._param)) {
                this._path = this._path.substring(0, this._path.length() - 1 - this._param.length());
            }
            this._param = param;
            if (this._param != null) {
                this._path = (this._path == null ? "" : this._path) + ";" + this._param;
            }
            this._uri = null;
        }
    }

    public String getQuery() {
        return this._query;
    }

    public boolean hasQuery() {
        return this._query != null && !this._query.isEmpty();
    }

    public String getFragment() {
        return this._fragment;
    }

    public void decodeQueryTo(MultiMap<String> parameters) {
        if (this._query == null) {
            return;
        }
        UrlEncoded.decodeUtf8To(this._query, parameters);
    }

    public void decodeQueryTo(MultiMap<String> parameters, String encoding) throws UnsupportedEncodingException {
        this.decodeQueryTo(parameters, Charset.forName(encoding));
    }

    public void decodeQueryTo(MultiMap<String> parameters, Charset encoding) throws UnsupportedEncodingException {
        if (this._query == null) {
            return;
        }
        if (encoding == null || StandardCharsets.UTF_8.equals(encoding)) {
            UrlEncoded.decodeUtf8To(this._query, parameters);
        } else {
            UrlEncoded.decodeTo(this._query, parameters, encoding);
        }
    }

    public boolean isAbsolute() {
        return this._scheme != null && !this._scheme.isEmpty();
    }

    public String toString() {
        if (this._uri == null) {
            StringBuilder out = new StringBuilder();
            if (this._scheme != null) {
                out.append(this._scheme).append(':');
            }
            if (this._host != null) {
                out.append("//");
                if (this._user != null) {
                    out.append(this._user).append('@');
                }
                out.append(this._host);
            }
            if (this._port > 0) {
                out.append(':').append(this._port);
            }
            if (this._path != null) {
                out.append(this._path);
            }
            if (this._query != null) {
                out.append('?').append(this._query);
            }
            if (this._fragment != null) {
                out.append('#').append(this._fragment);
            }
            this._uri = out.length() > 0 ? out.toString() : "";
        }
        return this._uri;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof HttpURI)) {
            return false;
        }
        return this.toString().equals(o.toString());
    }

    public int hashCode() {
        return this.toString().hashCode();
    }

    public void setScheme(String scheme) {
        this._scheme = scheme;
        this._uri = null;
    }

    public void setAuthority(String host, int port) {
        this._host = host;
        this._port = port;
        this._uri = null;
    }

    public void setPath(String path) {
        this._uri = null;
        this._path = null;
        if (path != null) {
            this.parse(State.PATH, path, 0, path.length());
        }
    }

    public void setPathQuery(String pathQuery) {
        this._uri = null;
        this._path = null;
        this._decodedPath = null;
        this._param = null;
        this._fragment = null;
        if (pathQuery != null) {
            this.parse(State.PATH, pathQuery, 0, pathQuery.length());
        }
    }

    public void setQuery(String query) {
        this._query = query;
        this._uri = null;
    }

    public URI toURI() throws URISyntaxException {
        return new URI(this._scheme, null, this._host, this._port, this._path, this._query == null ? null : UrlEncoded.decodeString(this._query), this._fragment);
    }

    public String getPathQuery() {
        if (this._query == null) {
            return this._path;
        }
        return this._path + "?" + this._query;
    }

    public String getAuthority() {
        if (this._port > 0) {
            return this._host + ":" + this._port;
        }
        return this._host;
    }

    public String getUser() {
        return this._user;
    }

    static {
        __ambiguousSegments.put("%2e", Boolean.TRUE);
        __ambiguousSegments.put("%2e%2e", Boolean.TRUE);
        __ambiguousSegments.put(".%2e", Boolean.TRUE);
        __ambiguousSegments.put("%2e.", Boolean.TRUE);
        __ambiguousSegments.put("..", Boolean.FALSE);
        __ambiguousSegments.put(".", Boolean.FALSE);
    }

    static enum Ambiguous {
        SEGMENT,
        SEPARATOR,
        PARAM;

    }

    private static enum State {
        START,
        HOST_OR_PATH,
        SCHEME_OR_PATH,
        HOST,
        IPV6,
        PORT,
        PATH,
        PARAM,
        QUERY,
        FRAGMENT,
        ASTERISK;

    }
}

