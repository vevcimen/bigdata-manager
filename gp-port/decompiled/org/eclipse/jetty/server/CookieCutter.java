/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.Cookie;
import org.eclipse.jetty.http.CookieCompliance;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class CookieCutter {
    private static final Logger LOG = Log.getLogger(CookieCutter.class);
    private final CookieCompliance _compliance;
    private Cookie[] _cookies;
    private Cookie[] _lastCookies;
    private final List<String> _fieldList = new ArrayList<String>();
    int _fields;

    public CookieCutter() {
        this(CookieCompliance.RFC6265);
    }

    public CookieCutter(CookieCompliance compliance) {
        this._compliance = compliance;
    }

    public Cookie[] getCookies() {
        if (this._cookies != null) {
            return this._cookies;
        }
        if (this._lastCookies != null && this._fields == this._fieldList.size()) {
            this._cookies = this._lastCookies;
        } else {
            this.parseFields();
        }
        this._lastCookies = this._cookies;
        return this._cookies;
    }

    public void setCookies(Cookie[] cookies) {
        this._cookies = cookies;
        this._lastCookies = null;
        this._fieldList.clear();
        this._fields = 0;
    }

    public void reset() {
        this._cookies = null;
        this._fields = 0;
    }

    public void addCookieField(String f) {
        if (f == null) {
            return;
        }
        if ((f = f.trim()).length() == 0) {
            return;
        }
        if (this._fieldList.size() > this._fields) {
            if (f.equals(this._fieldList.get(this._fields))) {
                ++this._fields;
                return;
            }
            while (this._fieldList.size() > this._fields) {
                this._fieldList.remove(this._fields);
            }
        }
        this._cookies = null;
        this._lastCookies = null;
        this._fieldList.add(this._fields++, f);
    }

    protected void parseFields() {
        this._lastCookies = null;
        this._cookies = null;
        ArrayList<Cookie> cookies = new ArrayList<Cookie>();
        int version = 0;
        while (this._fieldList.size() > this._fields) {
            this._fieldList.remove(this._fields);
        }
        StringBuilder unquoted = null;
        for (String hdr : this._fieldList) {
            String name = null;
            Cookie cookie = null;
            boolean invalue = false;
            boolean inQuoted = false;
            boolean quoted = false;
            boolean escaped = false;
            boolean reject = false;
            int tokenstart = -1;
            int tokenend = -1;
            int length = hdr.length();
            block32: for (int i = 0; i <= length; ++i) {
                char c;
                char c2 = c = i == length ? (char)'\u0000' : hdr.charAt(i);
                if (inQuoted) {
                    if (escaped) {
                        escaped = false;
                        if (c > '\u0000') {
                            unquoted.append(c);
                            continue;
                        }
                        unquoted.setLength(0);
                        inQuoted = false;
                        --i;
                        continue;
                    }
                    switch (c) {
                        case '\"': {
                            inQuoted = false;
                            quoted = true;
                            tokenstart = i;
                            tokenend = -1;
                            break;
                        }
                        case '\\': {
                            escaped = true;
                            break;
                        }
                        case '\u0000': {
                            unquoted.setLength(0);
                            inQuoted = false;
                            --i;
                            break;
                        }
                        default: {
                            unquoted.append(c);
                            break;
                        }
                    }
                    continue;
                }
                if (invalue) {
                    switch (c) {
                        case '\t': 
                        case ' ': {
                            break;
                        }
                        case ',': {
                            if (this._compliance != CookieCompliance.RFC2965) {
                                if (quoted) {
                                    unquoted.append(hdr, tokenstart, i--);
                                    inQuoted = true;
                                    quoted = false;
                                    break;
                                }
                                if (tokenstart < 0) {
                                    tokenstart = i;
                                }
                                tokenend = i;
                                break;
                            }
                        }
                        case '\u0000': 
                        case ';': {
                            String value;
                            if (quoted) {
                                value = unquoted.toString();
                                unquoted.setLength(0);
                                quoted = false;
                            } else {
                                value = tokenstart >= 0 ? (tokenend >= tokenstart ? hdr.substring(tokenstart, tokenend + 1) : hdr.substring(tokenstart)) : "";
                            }
                            try {
                                if (name.startsWith("$")) {
                                    if (this._compliance == CookieCompliance.RFC2965) {
                                        String lowercaseName;
                                        switch (lowercaseName = name.toLowerCase(Locale.ENGLISH)) {
                                            case "$path": {
                                                if (cookie == null) break;
                                                cookie.setPath(value);
                                                break;
                                            }
                                            case "$domain": {
                                                if (cookie == null) break;
                                                cookie.setDomain(value);
                                                break;
                                            }
                                            case "$port": {
                                                if (cookie == null) break;
                                                cookie.setComment("$port=" + value);
                                                break;
                                            }
                                            case "$version": {
                                                version = Integer.parseInt(value);
                                                break;
                                            }
                                        }
                                    }
                                } else {
                                    cookie = new Cookie(name, value);
                                    if (version > 0) {
                                        cookie.setVersion(version);
                                    }
                                    if (!reject) {
                                        cookies.add(cookie);
                                    }
                                }
                            }
                            catch (Exception e) {
                                LOG.debug(e);
                            }
                            name = null;
                            tokenstart = -1;
                            invalue = false;
                            reject = false;
                            break;
                        }
                        case '\"': {
                            if (tokenstart < 0) {
                                tokenstart = i;
                                inQuoted = true;
                                if (unquoted != null) continue block32;
                                unquoted = new StringBuilder();
                                break;
                            }
                        }
                        default: {
                            if (quoted) {
                                unquoted.append(hdr, tokenstart, i--);
                                inQuoted = true;
                                quoted = false;
                                break;
                            }
                            if (this._compliance == CookieCompliance.RFC6265 && this.isRFC6265RejectedCharacter(inQuoted, c)) {
                                reject = true;
                            }
                            if (tokenstart < 0) {
                                tokenstart = i;
                            }
                            tokenend = i;
                            break;
                        }
                    }
                    continue;
                }
                switch (c) {
                    case '\t': 
                    case ' ': {
                        continue block32;
                    }
                    case ';': {
                        tokenstart = -1;
                        invalue = false;
                        reject = false;
                        continue block32;
                    }
                    case '=': {
                        if (quoted) {
                            name = unquoted.toString();
                            unquoted.setLength(0);
                            quoted = false;
                        } else if (tokenstart >= 0) {
                            name = tokenend >= tokenstart ? hdr.substring(tokenstart, tokenend + 1) : hdr.substring(tokenstart);
                        }
                        tokenstart = -1;
                        invalue = true;
                        continue block32;
                    }
                    default: {
                        if (quoted) {
                            unquoted.append(hdr, tokenstart, i--);
                            inQuoted = true;
                            quoted = false;
                            continue block32;
                        }
                        if (this._compliance == CookieCompliance.RFC6265 && this.isRFC6265RejectedCharacter(inQuoted, c)) {
                            reject = true;
                        }
                        if (tokenstart < 0) {
                            tokenstart = i;
                        }
                        tokenend = i;
                        continue block32;
                    }
                }
            }
        }
        this._cookies = cookies.toArray(new Cookie[0]);
        this._lastCookies = this._cookies;
    }

    protected boolean isRFC6265RejectedCharacter(boolean inQuoted, char c) {
        if (inQuoted) {
            return Character.isISOControl(c);
        }
        return Character.isISOControl(c) || c > '\u007f' || c == ',' || c == ';';
    }
}

