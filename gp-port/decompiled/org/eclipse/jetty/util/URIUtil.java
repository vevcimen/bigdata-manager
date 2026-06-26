/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.eclipse.jetty.util.HostPort;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.TypeUtil;
import org.eclipse.jetty.util.Utf8Appendable;
import org.eclipse.jetty.util.Utf8StringBuilder;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class URIUtil
implements Cloneable {
    private static final Logger LOG = Log.getLogger(URIUtil.class);
    public static final String SLASH = "/";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    public static final Charset __CHARSET = StandardCharsets.UTF_8;

    private URIUtil() {
    }

    public static String encodePath(String path) {
        if (path == null || path.length() == 0) {
            return path;
        }
        StringBuilder buf = URIUtil.encodePath(null, path, 0);
        return buf == null ? path : buf.toString();
    }

    public static StringBuilder encodePath(StringBuilder buf, String path) {
        return URIUtil.encodePath(buf, path, 0);
    }

    private static StringBuilder encodePath(StringBuilder buf, String path, int offset) {
        int i;
        byte[] bytes = null;
        if (buf == null) {
            block41: for (i = offset; i < path.length(); ++i) {
                char c = path.charAt(i);
                switch (c) {
                    case ' ': 
                    case '\"': 
                    case '#': 
                    case '%': 
                    case '\'': 
                    case ';': 
                    case '<': 
                    case '>': 
                    case '?': 
                    case '[': 
                    case '\\': 
                    case ']': 
                    case '^': 
                    case '`': 
                    case '{': 
                    case '|': 
                    case '}': {
                        buf = new StringBuilder(path.length() * 2);
                        break block41;
                    }
                    default: {
                        if (c <= '\u007f') continue block41;
                        bytes = path.getBytes(__CHARSET);
                        buf = new StringBuilder(path.length() * 2);
                        break block41;
                    }
                }
            }
            if (buf == null) {
                return null;
            }
        }
        block42: for (i = offset; i < path.length(); ++i) {
            char c = path.charAt(i);
            switch (c) {
                case '%': {
                    buf.append("%25");
                    continue block42;
                }
                case '?': {
                    buf.append("%3F");
                    continue block42;
                }
                case ';': {
                    buf.append("%3B");
                    continue block42;
                }
                case '#': {
                    buf.append("%23");
                    continue block42;
                }
                case '\"': {
                    buf.append("%22");
                    continue block42;
                }
                case '\'': {
                    buf.append("%27");
                    continue block42;
                }
                case '<': {
                    buf.append("%3C");
                    continue block42;
                }
                case '>': {
                    buf.append("%3E");
                    continue block42;
                }
                case ' ': {
                    buf.append("%20");
                    continue block42;
                }
                case '[': {
                    buf.append("%5B");
                    continue block42;
                }
                case '\\': {
                    buf.append("%5C");
                    continue block42;
                }
                case ']': {
                    buf.append("%5D");
                    continue block42;
                }
                case '^': {
                    buf.append("%5E");
                    continue block42;
                }
                case '`': {
                    buf.append("%60");
                    continue block42;
                }
                case '{': {
                    buf.append("%7B");
                    continue block42;
                }
                case '|': {
                    buf.append("%7C");
                    continue block42;
                }
                case '}': {
                    buf.append("%7D");
                    continue block42;
                }
                default: {
                    if (c > '\u007f') {
                        bytes = path.getBytes(__CHARSET);
                        break block42;
                    }
                    buf.append(c);
                }
            }
        }
        if (bytes != null) {
            while (i < bytes.length) {
                void var5_7 = bytes[i];
                switch (var5_7) {
                    case 37: {
                        buf.append("%25");
                        break;
                    }
                    case 63: {
                        buf.append("%3F");
                        break;
                    }
                    case 59: {
                        buf.append("%3B");
                        break;
                    }
                    case 35: {
                        buf.append("%23");
                        break;
                    }
                    case 34: {
                        buf.append("%22");
                        break;
                    }
                    case 39: {
                        buf.append("%27");
                        break;
                    }
                    case 60: {
                        buf.append("%3C");
                        break;
                    }
                    case 62: {
                        buf.append("%3E");
                        break;
                    }
                    case 32: {
                        buf.append("%20");
                        break;
                    }
                    case 91: {
                        buf.append("%5B");
                        break;
                    }
                    case 92: {
                        buf.append("%5C");
                        break;
                    }
                    case 93: {
                        buf.append("%5D");
                        break;
                    }
                    case 94: {
                        buf.append("%5E");
                        break;
                    }
                    case 96: {
                        buf.append("%60");
                        break;
                    }
                    case 123: {
                        buf.append("%7B");
                        break;
                    }
                    case 124: {
                        buf.append("%7C");
                        break;
                    }
                    case 125: {
                        buf.append("%7D");
                        break;
                    }
                    default: {
                        if (var5_7 < 0) {
                            buf.append('%');
                            TypeUtil.toHex((byte)var5_7, (Appendable)buf);
                            break;
                        }
                        buf.append((char)var5_7);
                    }
                }
                ++i;
            }
        }
        return buf;
    }

    public static String encodeSpaces(String str) {
        return StringUtil.replace(str, " ", "%20");
    }

    public static String encodeSpecific(String str, String charsToEncode) {
        if (str == null || str.length() == 0) {
            return null;
        }
        if (charsToEncode == null || charsToEncode.length() == 0) {
            return str;
        }
        char[] find = charsToEncode.toCharArray();
        int len = str.length();
        StringBuilder ret = new StringBuilder((int)((double)len * 0.2));
        for (int i = 0; i < len; ++i) {
            char c = str.charAt(i);
            boolean escaped = false;
            for (char f : find) {
                if (c != f) continue;
                escaped = true;
                ret.append('%');
                int d = 0xF & (0xF0 & c) >> 4;
                ret.append((char)((d > 9 ? 55 : 48) + d));
                d = 0xF & c;
                ret.append((char)((d > 9 ? 55 : 48) + d));
                break;
            }
            if (escaped) continue;
            ret.append(c);
        }
        return ret.toString();
    }

    public static String decodeSpecific(String str, String charsToDecode) {
        if (str == null || str.length() == 0) {
            return null;
        }
        if (charsToDecode == null || charsToDecode.length() == 0) {
            return str;
        }
        int idx = str.indexOf(37);
        if (idx == -1) {
            return str;
        }
        char[] find = charsToDecode.toCharArray();
        int len = str.length();
        Utf8StringBuilder ret = new Utf8StringBuilder(len);
        ret.append(str, 0, idx);
        block3: for (int i = idx; i < len; ++i) {
            char c = str.charAt(i);
            switch (c) {
                case '%': {
                    if (i + 2 < len) {
                        char u = str.charAt(i + 1);
                        char l = str.charAt(i + 2);
                        char result = (char)(0xFF & TypeUtil.convertHexDigit(u) * 16 + TypeUtil.convertHexDigit(l));
                        boolean decoded = false;
                        for (char f : find) {
                            if (f != result) continue;
                            ret.append(result);
                            decoded = true;
                            break;
                        }
                        if (decoded) {
                            i += 2;
                            continue block3;
                        }
                        ret.append(c);
                        continue block3;
                    }
                    throw new IllegalArgumentException("Bad URI % encoding");
                }
                default: {
                    ret.append(c);
                }
            }
        }
        return ret.toString();
    }

    public static StringBuilder encodeString(StringBuilder buf, String path, String encode) {
        char c;
        int i;
        if (buf == null) {
            for (i = 0; i < path.length(); ++i) {
                c = path.charAt(i);
                if (c != '%' && encode.indexOf(c) < 0) continue;
                buf = new StringBuilder(path.length() << 1);
                break;
            }
            if (buf == null) {
                return null;
            }
        }
        for (i = 0; i < path.length(); ++i) {
            c = path.charAt(i);
            if (c == '%' || encode.indexOf(c) >= 0) {
                buf.append('%');
                StringUtil.append(buf, (byte)(0xFF & c), 16);
                continue;
            }
            buf.append(c);
        }
        return buf;
    }

    public static String decodePath(String path) {
        return URIUtil.decodePath(path, 0, path.length());
    }

    public static String decodePath(String path, int offset, int length) {
        try {
            Utf8StringBuilder builder = null;
            int end = offset + length;
            block8: for (int i = offset; i < end; ++i) {
                char c = path.charAt(i);
                switch (c) {
                    case '%': {
                        if (builder == null) {
                            builder = new Utf8StringBuilder(path.length());
                            builder.append(path, offset, i - offset);
                        }
                        if (i + 2 < end) {
                            char u = path.charAt(i + 1);
                            if (u == 'u') {
                                builder.append((char)(0xFFFF & TypeUtil.parseInt(path, i + 2, 4, 16)));
                                i += 5;
                                continue block8;
                            }
                            builder.append((byte)(0xFF & TypeUtil.convertHexDigit(u) * 16 + TypeUtil.convertHexDigit(path.charAt(i + 2))));
                            i += 2;
                            continue block8;
                        }
                        throw new IllegalArgumentException("Bad URI % encoding");
                    }
                    case ';': {
                        if (builder == null) {
                            builder = new Utf8StringBuilder(path.length());
                            builder.append(path, offset, i - offset);
                        }
                        while (++i < end) {
                            if (path.charAt(i) != '/') continue;
                            builder.append('/');
                            continue block8;
                        }
                        continue block8;
                    }
                    default: {
                        if (builder == null) continue block8;
                        builder.append(c);
                    }
                }
            }
            if (builder != null) {
                return builder.toString();
            }
            if (offset == 0 && length == path.length()) {
                return path;
            }
            return path.substring(offset, end);
        }
        catch (Utf8Appendable.NotUtf8Exception e) {
            LOG.debug(path.substring(offset, offset + length) + " " + e, new Object[0]);
            return URIUtil.decodeISO88591Path(path, offset, length);
        }
        catch (IllegalArgumentException e) {
            throw e;
        }
        catch (Exception e) {
            throw new IllegalArgumentException("cannot decode URI", e);
        }
    }

    private static String decodeISO88591Path(String path, int offset, int length) {
        StringBuilder builder = null;
        int end = offset + length;
        block4: for (int i = offset; i < end; ++i) {
            char c = path.charAt(i);
            switch (c) {
                case '%': {
                    if (builder == null) {
                        builder = new StringBuilder(path.length());
                        builder.append(path, offset, i - offset);
                    }
                    if (i + 2 < end) {
                        char u = path.charAt(i + 1);
                        if (u == 'u') {
                            builder.append((char)(0xFFFF & TypeUtil.parseInt(path, i + 2, 4, 16)));
                            i += 5;
                            continue block4;
                        }
                        builder.append((char)(0xFF & TypeUtil.convertHexDigit(u) * 16 + TypeUtil.convertHexDigit(path.charAt(i + 2))));
                        i += 2;
                        continue block4;
                    }
                    throw new IllegalArgumentException();
                }
                case ';': {
                    if (builder == null) {
                        builder = new StringBuilder(path.length());
                        builder.append(path, offset, i - offset);
                    }
                    while (++i < end) {
                        if (path.charAt(i) != '/') continue;
                        builder.append('/');
                        continue block4;
                    }
                    continue block4;
                }
                default: {
                    if (builder == null) continue block4;
                    builder.append(c);
                }
            }
        }
        if (builder != null) {
            return builder.toString();
        }
        if (offset == 0 && length == path.length()) {
            return path;
        }
        return path.substring(offset, end);
    }

    public static String addEncodedPaths(String p1, String p2) {
        if (p1 == null || p1.length() == 0) {
            if (p1 != null && p2 == null) {
                return p1;
            }
            return p2;
        }
        if (p2 == null || p2.length() == 0) {
            return p1;
        }
        int split = p1.indexOf(59);
        if (split < 0) {
            split = p1.indexOf(63);
        }
        if (split == 0) {
            return p2 + p1;
        }
        if (split < 0) {
            split = p1.length();
        }
        StringBuilder buf = new StringBuilder(p1.length() + p2.length() + 2);
        buf.append(p1);
        if (buf.charAt(split - 1) == '/') {
            if (p2.startsWith(SLASH)) {
                buf.deleteCharAt(split - 1);
                buf.insert(split - 1, p2);
            } else {
                buf.insert(split, p2);
            }
        } else if (p2.startsWith(SLASH)) {
            buf.insert(split, p2);
        } else {
            buf.insert(split, '/');
            buf.insert(split + 1, p2);
        }
        return buf.toString();
    }

    public static String addPaths(String p1, String p2) {
        if (p1 == null || p1.length() == 0) {
            if (p1 != null && p2 == null) {
                return p1;
            }
            return p2;
        }
        if (p2 == null || p2.length() == 0) {
            return p1;
        }
        boolean p1EndsWithSlash = p1.endsWith(SLASH);
        boolean p2StartsWithSlash = p2.startsWith(SLASH);
        if (p1EndsWithSlash && p2StartsWithSlash) {
            if (p2.length() == 1) {
                return p1;
            }
            if (p1.length() == 1) {
                return p2;
            }
        }
        StringBuilder buf = new StringBuilder(p1.length() + p2.length() + 2);
        buf.append(p1);
        if (p1.endsWith(SLASH)) {
            if (p2.startsWith(SLASH)) {
                buf.setLength(buf.length() - 1);
            }
        } else if (!p2.startsWith(SLASH)) {
            buf.append(SLASH);
        }
        buf.append(p2);
        return buf.toString();
    }

    public static String addPathQuery(String path, String query) {
        if (query == null) {
            return path;
        }
        if (path.indexOf(63) >= 0) {
            return path + '&' + query;
        }
        return path + '?' + query;
    }

    public static String getUriLastPathSegment(URI uri) {
        String ssp = uri.getSchemeSpecificPart();
        int idx = ssp.indexOf("!/");
        if (idx != -1) {
            ssp = ssp.substring(0, idx);
        }
        if (ssp.endsWith(SLASH)) {
            ssp = ssp.substring(0, ssp.length() - 1);
        }
        if ((idx = ssp.lastIndexOf(47)) != -1) {
            ssp = ssp.substring(idx + 1);
        }
        return ssp;
    }

    public static String parentPath(String p) {
        if (p == null || SLASH.equals(p)) {
            return null;
        }
        int slash = p.lastIndexOf(47, p.length() - 2);
        if (slash >= 0) {
            return p.substring(0, slash + 1);
        }
        return null;
    }

    public static String canonicalPath(String path) {
        int i;
        if (path == null || path.isEmpty()) {
            return path;
        }
        int end = path.length();
        int dots = 0;
        block22: for (i = 0; i < end; ++i) {
            char c = path.charAt(i);
            switch (c) {
                case '/': {
                    dots = 0;
                    continue block22;
                }
                case '.': {
                    if (dots == 0) {
                        dots = 1;
                        break block22;
                    }
                    dots = -1;
                    continue block22;
                }
                default: {
                    dots = -1;
                }
            }
        }
        if (i == end) {
            return path;
        }
        StringBuilder canonical = new StringBuilder(path.length());
        canonical.append(path, 0, i);
        ++i;
        while (i <= end) {
            char c = i < end ? path.charAt(i) : (char)'\u0000';
            block4 : switch (c) {
                case '\u0000': {
                    if (dots != 2) break;
                    if (canonical.length() < 2) {
                        return null;
                    }
                    canonical.setLength(canonical.length() - 1);
                    canonical.setLength(canonical.lastIndexOf(SLASH) + 1);
                    break;
                }
                case '/': {
                    switch (dots) {
                        case 1: {
                            break;
                        }
                        case 2: {
                            if (canonical.length() < 2) {
                                return null;
                            }
                            canonical.setLength(canonical.length() - 1);
                            canonical.setLength(canonical.lastIndexOf(SLASH) + 1);
                            break;
                        }
                        default: {
                            canonical.append(c);
                        }
                    }
                    dots = 0;
                    break;
                }
                case '.': {
                    switch (dots) {
                        case 0: {
                            dots = 1;
                            break block4;
                        }
                        case 1: {
                            dots = 2;
                            break block4;
                        }
                        case 2: {
                            canonical.append("...");
                            dots = -1;
                            break block4;
                        }
                    }
                    canonical.append('.');
                    break;
                }
                default: {
                    switch (dots) {
                        case 1: {
                            canonical.append('.');
                            break;
                        }
                        case 2: {
                            canonical.append("..");
                            break;
                        }
                    }
                    canonical.append(c);
                    dots = -1;
                }
            }
            ++i;
        }
        return canonical.toString();
    }

    public static String canonicalEncodedPath(String path) {
        int i;
        if (path == null || path.isEmpty()) {
            return path;
        }
        boolean slash = true;
        int end = path.length();
        block14: for (i = 0; i < end; ++i) {
            char c = path.charAt(i);
            switch (c) {
                case '/': {
                    slash = true;
                    continue block14;
                }
                case '.': {
                    if (slash) break block14;
                    slash = false;
                    continue block14;
                }
                case '?': {
                    return path;
                }
                default: {
                    slash = false;
                }
            }
        }
        if (i == end) {
            return path;
        }
        StringBuilder canonical = new StringBuilder(path.length());
        canonical.append(path, 0, i);
        int dots = 1;
        ++i;
        while (i <= end) {
            char c = i < end ? path.charAt(i) : (char)'\u0000';
            switch (c) {
                case '\u0000': 
                case '/': 
                case '?': {
                    switch (dots) {
                        case 0: {
                            if (c == '\u0000') break;
                            canonical.append(c);
                            break;
                        }
                        case 1: {
                            if (c != '?') break;
                            canonical.append(c);
                            break;
                        }
                        case 2: {
                            if (canonical.length() < 2) {
                                return null;
                            }
                            canonical.setLength(canonical.length() - 1);
                            canonical.setLength(canonical.lastIndexOf(SLASH) + 1);
                            if (c != '?') break;
                            canonical.append(c);
                            break;
                        }
                        default: {
                            while (dots-- > 0) {
                                canonical.append('.');
                            }
                            if (c == '\u0000') break;
                            canonical.append(c);
                        }
                    }
                    slash = true;
                    dots = 0;
                    break;
                }
                case '.': {
                    if (dots > 0) {
                        ++dots;
                    } else if (slash) {
                        dots = 1;
                    } else {
                        canonical.append('.');
                    }
                    slash = false;
                    break;
                }
                default: {
                    while (dots-- > 0) {
                        canonical.append('.');
                    }
                    canonical.append(c);
                    dots = 0;
                    slash = false;
                }
            }
            ++i;
        }
        return canonical.toString();
    }

    public static String compactPath(String path) {
        int i;
        if (path == null || path.length() == 0) {
            return path;
        }
        int state = 0;
        int end = path.length();
        block8: for (i = 0; i < end; ++i) {
            char c = path.charAt(i);
            switch (c) {
                case '?': {
                    return path;
                }
                case '/': {
                    if (++state != 2) continue block8;
                    break block8;
                }
                default: {
                    state = 0;
                }
            }
        }
        if (state < 2) {
            return path;
        }
        StringBuilder buf = new StringBuilder(path.length());
        buf.append(path, 0, i);
        block9: while (i < end) {
            char c = path.charAt(i);
            switch (c) {
                case '?': {
                    buf.append(path, i, end);
                    break block9;
                }
                case '/': {
                    if (state++ != 0) break;
                    buf.append(c);
                    break;
                }
                default: {
                    state = 0;
                    buf.append(c);
                }
            }
            ++i;
        }
        return buf.toString();
    }

    public static boolean hasScheme(String uri) {
        for (int i = 0; i < uri.length(); ++i) {
            char c = uri.charAt(i);
            if (c == ':') {
                return true;
            }
            if (!(c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || i > 0 && (c >= '0' && c <= '9' || c == '.' || c == '+' || c == '-'))) break;
        }
        return false;
    }

    public static String newURI(String scheme, String server, int port, String path, String query) {
        StringBuilder builder = URIUtil.newURIBuilder(scheme, server, port);
        builder.append(path);
        if (query != null && query.length() > 0) {
            builder.append('?').append(query);
        }
        return builder.toString();
    }

    public static StringBuilder newURIBuilder(String scheme, String server, int port) {
        StringBuilder builder = new StringBuilder();
        URIUtil.appendSchemeHostPort(builder, scheme, server, port);
        return builder;
    }

    public static void appendSchemeHostPort(StringBuilder url, String scheme, String server, int port) {
        url.append(scheme).append("://").append(HostPort.normalizeHost(server));
        if (port > 0) {
            switch (scheme) {
                case "http": {
                    if (port == 80) break;
                    url.append(':').append(port);
                    break;
                }
                case "https": {
                    if (port == 443) break;
                    url.append(':').append(port);
                    break;
                }
                default: {
                    url.append(':').append(port);
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void appendSchemeHostPort(StringBuffer url, String scheme, String server, int port) {
        StringBuffer stringBuffer = url;
        synchronized (stringBuffer) {
            url.append(scheme).append("://").append(HostPort.normalizeHost(server));
            if (port > 0) {
                switch (scheme) {
                    case "http": {
                        if (port == 80) break;
                        url.append(':').append(port);
                        break;
                    }
                    case "https": {
                        if (port == 443) break;
                        url.append(':').append(port);
                        break;
                    }
                    default: {
                        url.append(':').append(port);
                    }
                }
            }
        }
    }

    public static boolean equalsIgnoreEncodings(String uriA, String uriB) {
        int lenA = uriA.length();
        int lenB = uriB.length();
        int a = 0;
        int b = 0;
        while (a < lenA && b < lenB) {
            int ob;
            int cb;
            int oa;
            int ca;
            if ((ca = (oa = uriA.charAt(a++))) == 37) {
                ca = URIUtil.lenientPercentDecode(uriA, a);
                if (ca == -1) {
                    ca = 37;
                } else {
                    a += 2;
                }
            }
            if ((cb = (ob = uriB.charAt(b++))) == 37) {
                cb = URIUtil.lenientPercentDecode(uriB, b);
                if (cb == -1) {
                    cb = 37;
                } else {
                    b += 2;
                }
            }
            if (ca == 47 && oa != ob) {
                return false;
            }
            if (ca == cb) continue;
            return false;
        }
        return a == lenA && b == lenB;
    }

    private static int lenientPercentDecode(String str, int offset) {
        if (offset >= str.length()) {
            return -1;
        }
        if (StringUtil.isHex(str, offset, 2)) {
            return TypeUtil.parseInt(str, offset, 2, 16);
        }
        return -1;
    }

    public static boolean equalsIgnoreEncodings(URI uriA, URI uriB) {
        if (uriA.equals(uriB)) {
            return true;
        }
        if (uriA.getScheme() == null ? uriB.getScheme() != null : !uriA.getScheme().equalsIgnoreCase(uriB.getScheme())) {
            return false;
        }
        if ("jar".equalsIgnoreCase(uriA.getScheme())) {
            URI uriAssp = URI.create(uriA.getSchemeSpecificPart());
            URI uriBssp = URI.create(uriB.getSchemeSpecificPart());
            return URIUtil.equalsIgnoreEncodings(uriAssp, uriBssp);
        }
        if (uriA.getAuthority() == null ? uriB.getAuthority() != null : !uriA.getAuthority().equals(uriB.getAuthority())) {
            return false;
        }
        return URIUtil.equalsIgnoreEncodings(uriA.getPath(), uriB.getPath());
    }

    public static URI addPath(URI uri, String path) {
        String base = uri.toASCIIString();
        StringBuilder buf = new StringBuilder(base.length() + path.length() * 3);
        buf.append(base);
        if (buf.charAt(base.length() - 1) != '/') {
            buf.append('/');
        }
        int offset = path.charAt(0) == '/' ? 1 : 0;
        URIUtil.encodePath(buf, path, offset);
        return URI.create(buf.toString());
    }

    public static String addQueries(String query1, String query2) {
        if (StringUtil.isEmpty(query1)) {
            return query2;
        }
        if (StringUtil.isEmpty(query2)) {
            return query1;
        }
        return query1 + '&' + query2;
    }

    public static URI getJarSource(URI uri) {
        try {
            if (!"jar".equals(uri.getScheme())) {
                return uri;
            }
            String s2 = uri.getRawSchemeSpecificPart();
            int bangSlash = s2.indexOf("!/");
            if (bangSlash >= 0) {
                s2 = s2.substring(0, bangSlash);
            }
            return new URI(s2);
        }
        catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String getJarSource(String uri) {
        if (!uri.startsWith("jar:")) {
            return uri;
        }
        int bangSlash = uri.indexOf("!/");
        return bangSlash >= 0 ? uri.substring(4, bangSlash) : uri.substring(4);
    }
}

