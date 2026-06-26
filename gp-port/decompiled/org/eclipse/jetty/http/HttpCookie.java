/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.http;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletContext;
import org.eclipse.jetty.http.CookieCompliance;
import org.eclipse.jetty.http.DateGenerator;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.Syntax;
import org.eclipse.jetty.util.QuotedStringTokenizer;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class HttpCookie {
    private static final Logger LOG = Log.getLogger(HttpCookie.class);
    private static final String __COOKIE_DELIM = "\",;\\ \t";
    private static final String __01Jan1970_COOKIE = DateGenerator.formatCookieDate(0L).trim();
    public static final String HTTP_ONLY_COMMENT = "__HTTP_ONLY__";
    private static final String SAME_SITE_COMMENT = "__SAME_SITE_";
    public static final String SAME_SITE_NONE_COMMENT = "__SAME_SITE_NONE__";
    public static final String SAME_SITE_LAX_COMMENT = "__SAME_SITE_LAX__";
    public static final String SAME_SITE_STRICT_COMMENT = "__SAME_SITE_STRICT__";
    public static final String SAME_SITE_DEFAULT_ATTRIBUTE = "org.eclipse.jetty.cookie.sameSiteDefault";
    private final String _name;
    private final String _value;
    private final String _comment;
    private final String _domain;
    private final long _maxAge;
    private final String _path;
    private final boolean _secure;
    private final int _version;
    private final boolean _httpOnly;
    private final long _expiration;
    private final SameSite _sameSite;

    public HttpCookie(String name, String value) {
        this(name, value, -1L);
    }

    public HttpCookie(String name, String value, String domain, String path) {
        this(name, value, domain, path, -1L, false, false);
    }

    public HttpCookie(String name, String value, long maxAge) {
        this(name, value, null, null, maxAge, false, false);
    }

    public HttpCookie(String name, String value, String domain, String path, long maxAge, boolean httpOnly, boolean secure) {
        this(name, value, domain, path, maxAge, httpOnly, secure, null, 0);
    }

    public HttpCookie(String name, String value, String domain, String path, long maxAge, boolean httpOnly, boolean secure, String comment, int version) {
        this(name, value, domain, path, maxAge, httpOnly, secure, comment, version, null);
    }

    public HttpCookie(String name, String value, String domain, String path, long maxAge, boolean httpOnly, boolean secure, String comment, int version, SameSite sameSite) {
        this._name = name;
        this._value = value;
        this._domain = domain;
        this._path = path;
        this._maxAge = maxAge;
        this._httpOnly = httpOnly;
        this._secure = secure;
        this._comment = comment;
        this._version = version;
        this._expiration = maxAge < 0L ? -1L : System.nanoTime() + TimeUnit.SECONDS.toNanos(maxAge);
        this._sameSite = sameSite;
    }

    public HttpCookie(String setCookie) {
        List<java.net.HttpCookie> cookies = java.net.HttpCookie.parse(setCookie);
        if (cookies.size() != 1) {
            throw new IllegalStateException();
        }
        java.net.HttpCookie cookie = cookies.get(0);
        this._name = cookie.getName();
        this._value = cookie.getValue();
        this._domain = cookie.getDomain();
        this._path = cookie.getPath();
        this._maxAge = cookie.getMaxAge();
        this._httpOnly = cookie.isHttpOnly();
        this._secure = cookie.getSecure();
        this._comment = cookie.getComment();
        this._version = cookie.getVersion();
        this._expiration = this._maxAge < 0L ? -1L : System.nanoTime() + TimeUnit.SECONDS.toNanos(this._maxAge);
        this._sameSite = HttpCookie.getSameSiteFromComment(cookie.getComment());
    }

    public String getName() {
        return this._name;
    }

    public String getValue() {
        return this._value;
    }

    public String getComment() {
        return this._comment;
    }

    public String getDomain() {
        return this._domain;
    }

    public long getMaxAge() {
        return this._maxAge;
    }

    public String getPath() {
        return this._path;
    }

    public boolean isSecure() {
        return this._secure;
    }

    public int getVersion() {
        return this._version;
    }

    public SameSite getSameSite() {
        return this._sameSite;
    }

    public boolean isHttpOnly() {
        return this._httpOnly;
    }

    public boolean isExpired(long timeNanos) {
        return this._expiration >= 0L && timeNanos >= this._expiration;
    }

    public String asString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getName()).append("=").append(this.getValue());
        if (this.getDomain() != null) {
            builder.append(";$Domain=").append(this.getDomain());
        }
        if (this.getPath() != null) {
            builder.append(";$Path=").append(this.getPath());
        }
        return builder.toString();
    }

    private static void quoteOnlyOrAppend(StringBuilder buf, String s2, boolean quote) {
        if (quote) {
            QuotedStringTokenizer.quoteOnly(buf, s2);
        } else {
            buf.append(s2);
        }
    }

    private static boolean isQuoteNeededForCookie(String s2) {
        if (s2 == null || s2.length() == 0) {
            return true;
        }
        if (QuotedStringTokenizer.isQuoted(s2)) {
            return false;
        }
        for (int i = 0; i < s2.length(); ++i) {
            char c = s2.charAt(i);
            if (__COOKIE_DELIM.indexOf(c) >= 0) {
                return true;
            }
            if (c >= ' ' && c < '\u007f') continue;
            throw new IllegalArgumentException("Illegal character in cookie value");
        }
        return false;
    }

    public String getSetCookie(CookieCompliance compliance) {
        switch (compliance) {
            case RFC2965: {
                return this.getRFC2965SetCookie();
            }
            case RFC6265: {
                return this.getRFC6265SetCookie();
            }
        }
        throw new IllegalStateException();
    }

    public String getRFC2965SetCookie() {
        if (this._name == null || this._name.length() == 0) {
            throw new IllegalArgumentException("Bad cookie name");
        }
        StringBuilder buf = new StringBuilder();
        boolean quoteName = HttpCookie.isQuoteNeededForCookie(this._name);
        HttpCookie.quoteOnlyOrAppend(buf, this._name, quoteName);
        buf.append('=');
        boolean quoteValue = HttpCookie.isQuoteNeededForCookie(this._value);
        HttpCookie.quoteOnlyOrAppend(buf, this._value, quoteValue);
        boolean hasDomain = this._domain != null && this._domain.length() > 0;
        boolean quoteDomain = hasDomain && HttpCookie.isQuoteNeededForCookie(this._domain);
        boolean hasPath = this._path != null && this._path.length() > 0;
        boolean quotePath = hasPath && HttpCookie.isQuoteNeededForCookie(this._path);
        int version = this._version;
        if (version == 0 && (this._comment != null || quoteName || quoteValue || quoteDomain || quotePath || QuotedStringTokenizer.isQuoted(this._name) || QuotedStringTokenizer.isQuoted(this._value) || QuotedStringTokenizer.isQuoted(this._path) || QuotedStringTokenizer.isQuoted(this._domain))) {
            version = 1;
        }
        if (version == 1) {
            buf.append(";Version=1");
        } else if (version > 1) {
            buf.append(";Version=").append(version);
        }
        if (hasPath) {
            buf.append(";Path=");
            HttpCookie.quoteOnlyOrAppend(buf, this._path, quotePath);
        }
        if (hasDomain) {
            buf.append(";Domain=");
            HttpCookie.quoteOnlyOrAppend(buf, this._domain, quoteDomain);
        }
        if (this._maxAge >= 0L) {
            buf.append(";Expires=");
            if (this._maxAge == 0L) {
                buf.append(__01Jan1970_COOKIE);
            } else {
                DateGenerator.formatCookieDate(buf, System.currentTimeMillis() + 1000L * this._maxAge);
            }
            if (version >= 1) {
                buf.append(";Max-Age=");
                buf.append(this._maxAge);
            }
        }
        if (this._secure) {
            buf.append(";Secure");
        }
        if (this._httpOnly) {
            buf.append(";HttpOnly");
        }
        if (this._comment != null) {
            buf.append(";Comment=");
            HttpCookie.quoteOnlyOrAppend(buf, this._comment, HttpCookie.isQuoteNeededForCookie(this._comment));
        }
        return buf.toString();
    }

    public String getRFC6265SetCookie() {
        if (this._name == null || this._name.length() == 0) {
            throw new IllegalArgumentException("Bad cookie name");
        }
        Syntax.requireValidRFC2616Token(this._name, "RFC6265 Cookie name");
        Syntax.requireValidRFC6265CookieValue(this._value);
        StringBuilder buf = new StringBuilder();
        buf.append(this._name).append('=').append(this._value == null ? "" : this._value);
        if (this._path != null && this._path.length() > 0) {
            buf.append("; Path=").append(this._path);
        }
        if (this._domain != null && this._domain.length() > 0) {
            buf.append("; Domain=").append(this._domain);
        }
        if (this._maxAge >= 0L) {
            buf.append("; Expires=");
            if (this._maxAge == 0L) {
                buf.append(__01Jan1970_COOKIE);
            } else {
                DateGenerator.formatCookieDate(buf, System.currentTimeMillis() + 1000L * this._maxAge);
            }
            buf.append("; Max-Age=");
            buf.append(this._maxAge);
        }
        if (this._secure) {
            buf.append("; Secure");
        }
        if (this._httpOnly) {
            buf.append("; HttpOnly");
        }
        if (this._sameSite != null) {
            buf.append("; SameSite=");
            buf.append(this._sameSite.getAttributeValue());
        }
        return buf.toString();
    }

    public static boolean isHttpOnlyInComment(String comment) {
        return comment != null && comment.contains(HTTP_ONLY_COMMENT);
    }

    public static SameSite getSameSiteFromComment(String comment) {
        if (comment != null) {
            if (comment.contains(SAME_SITE_STRICT_COMMENT)) {
                return SameSite.STRICT;
            }
            if (comment.contains(SAME_SITE_LAX_COMMENT)) {
                return SameSite.LAX;
            }
            if (comment.contains(SAME_SITE_NONE_COMMENT)) {
                return SameSite.NONE;
            }
        }
        return null;
    }

    public static SameSite getSameSiteDefault(ServletContext context) {
        if (context == null) {
            return null;
        }
        Object o = context.getAttribute(SAME_SITE_DEFAULT_ATTRIBUTE);
        if (o == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No default value for SameSite", new Object[0]);
            }
            return null;
        }
        if (o instanceof SameSite) {
            return (SameSite)((Object)o);
        }
        try {
            SameSite samesite = Enum.valueOf(SameSite.class, o.toString().trim().toUpperCase(Locale.ENGLISH));
            context.setAttribute(SAME_SITE_DEFAULT_ATTRIBUTE, (Object)samesite);
            return samesite;
        }
        catch (Exception e) {
            LOG.warn("Bad default value {} for SameSite", o);
            throw new IllegalStateException(e);
        }
    }

    public static String getCommentWithoutAttributes(String comment) {
        if (comment == null) {
            return null;
        }
        String strippedComment = comment.trim();
        strippedComment = StringUtil.strip(strippedComment, HTTP_ONLY_COMMENT);
        strippedComment = StringUtil.strip(strippedComment, SAME_SITE_NONE_COMMENT);
        strippedComment = StringUtil.strip(strippedComment, SAME_SITE_LAX_COMMENT);
        return (strippedComment = StringUtil.strip(strippedComment, SAME_SITE_STRICT_COMMENT)).length() == 0 ? null : strippedComment;
    }

    public static String getCommentWithAttributes(String comment, boolean httpOnly, SameSite sameSite) {
        if (comment == null && sameSite == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        if (StringUtil.isNotBlank(comment) && StringUtil.isNotBlank(comment = HttpCookie.getCommentWithoutAttributes(comment))) {
            builder.append(comment);
        }
        if (httpOnly) {
            builder.append(HTTP_ONLY_COMMENT);
        }
        if (sameSite != null) {
            switch (sameSite) {
                case NONE: {
                    builder.append(SAME_SITE_NONE_COMMENT);
                    break;
                }
                case STRICT: {
                    builder.append(SAME_SITE_STRICT_COMMENT);
                    break;
                }
                case LAX: {
                    builder.append(SAME_SITE_LAX_COMMENT);
                    break;
                }
                default: {
                    throw new IllegalArgumentException(sameSite.toString());
                }
            }
        }
        if (builder.length() == 0) {
            return null;
        }
        return builder.toString();
    }

    public static class SetCookieHttpField
    extends HttpField {
        final HttpCookie _cookie;

        public SetCookieHttpField(HttpCookie cookie, CookieCompliance compliance) {
            super(HttpHeader.SET_COOKIE, cookie.getSetCookie(compliance));
            this._cookie = cookie;
        }

        public HttpCookie getHttpCookie() {
            return this._cookie;
        }
    }

    public static enum SameSite {
        NONE("None"),
        STRICT("Strict"),
        LAX("Lax");

        private String attributeValue;

        private SameSite(String attributeValue) {
            this.attributeValue = attributeValue;
        }

        public String getAttributeValue() {
            return this.attributeValue;
        }
    }
}

