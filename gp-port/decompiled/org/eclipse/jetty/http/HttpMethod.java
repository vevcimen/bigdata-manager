/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.http;

import java.nio.ByteBuffer;
import org.eclipse.jetty.util.ArrayTernaryTrie;
import org.eclipse.jetty.util.ArrayTrie;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.Trie;

public enum HttpMethod {
    ACL(Type.IDEMPOTENT),
    BASELINE_CONTROL(Type.IDEMPOTENT),
    BIND(Type.IDEMPOTENT),
    CHECKIN(Type.IDEMPOTENT),
    CHECKOUT(Type.IDEMPOTENT),
    CONNECT(Type.NORMAL),
    COPY(Type.IDEMPOTENT),
    DELETE(Type.IDEMPOTENT),
    GET(Type.SAFE),
    HEAD(Type.SAFE),
    LABEL(Type.IDEMPOTENT),
    LINK(Type.IDEMPOTENT),
    LOCK(Type.NORMAL),
    MERGE(Type.IDEMPOTENT),
    MKACTIVITY(Type.IDEMPOTENT),
    MKCALENDAR(Type.IDEMPOTENT),
    MKCOL(Type.IDEMPOTENT),
    MKREDIRECTREF(Type.IDEMPOTENT),
    MKWORKSPACE(Type.IDEMPOTENT),
    MOVE(Type.IDEMPOTENT),
    OPTIONS(Type.SAFE),
    ORDERPATCH(Type.IDEMPOTENT),
    PATCH(Type.NORMAL),
    POST(Type.NORMAL),
    PRI(Type.SAFE),
    PROPFIND(Type.SAFE),
    PROPPATCH(Type.IDEMPOTENT),
    PUT(Type.IDEMPOTENT),
    REBIND(Type.IDEMPOTENT),
    REPORT(Type.SAFE),
    SEARCH(Type.SAFE),
    TRACE(Type.SAFE),
    UNBIND(Type.IDEMPOTENT),
    UNCHECKOUT(Type.IDEMPOTENT),
    UNLINK(Type.IDEMPOTENT),
    UNLOCK(Type.IDEMPOTENT),
    UPDATE(Type.IDEMPOTENT),
    UPDATEREDIRECTREF(Type.IDEMPOTENT),
    VERSION_CONTROL(Type.IDEMPOTENT),
    PROXY(Type.NORMAL);

    private final String _method = this.name().replace('_', '-');
    private final byte[] _bytes;
    private final ByteBuffer _buffer;
    private final Type _type;
    public static final Trie<HttpMethod> INSENSITIVE_CACHE;
    public static final Trie<HttpMethod> CACHE;
    public static final Trie<HttpMethod> LOOK_AHEAD;
    public static final int ACL_AS_INT = 1094929440;
    public static final int GET_AS_INT = 1195725856;
    public static final int PRI_AS_INT = 1347569952;
    public static final int PUT_AS_INT = 1347769376;
    public static final int POST_AS_INT = 1347375956;
    public static final int HEAD_AS_INT = 1212498244;

    private HttpMethod(Type type) {
        this._type = type;
        this._bytes = StringUtil.getBytes(this._method);
        this._buffer = ByteBuffer.wrap(this._bytes);
    }

    public byte[] getBytes() {
        return this._bytes;
    }

    public boolean is(String s2) {
        return this.toString().equalsIgnoreCase(s2);
    }

    public boolean isSafe() {
        return this._type == Type.SAFE;
    }

    public boolean isIdempotent() {
        return this._type.ordinal() >= Type.IDEMPOTENT.ordinal();
    }

    public ByteBuffer asBuffer() {
        return this._buffer.asReadOnlyBuffer();
    }

    public String asString() {
        return this._method;
    }

    public String toString() {
        return this._method;
    }

    @Deprecated
    public static HttpMethod lookAheadGet(byte[] bytes, int position, int limit) {
        return LOOK_AHEAD.getBest(bytes, position, limit - position);
    }

    public static HttpMethod lookAheadGet(ByteBuffer buffer) {
        int len = buffer.remaining();
        if (len > 3) {
            switch (buffer.getInt(buffer.position())) {
                case 1094929440: {
                    return ACL;
                }
                case 1195725856: {
                    return GET;
                }
                case 1347569952: {
                    return PRI;
                }
                case 1347769376: {
                    return PUT;
                }
                case 1347375956: {
                    if (len <= 4 || buffer.get(buffer.position() + 4) != 32) break;
                    return POST;
                }
                case 1212498244: {
                    if (len <= 4 || buffer.get(buffer.position() + 4) != 32) break;
                    return HEAD;
                }
            }
        }
        return LOOK_AHEAD.getBest(buffer, 0, len);
    }

    public static HttpMethod fromString(String method) {
        return CACHE.get(method);
    }

    static {
        INSENSITIVE_CACHE = new ArrayTrie<HttpMethod>(252);
        CACHE = new ArrayTernaryTrie<HttpMethod>(false, 300);
        LOOK_AHEAD = new ArrayTernaryTrie<HttpMethod>(false, 330);
        for (HttpMethod method : HttpMethod.values()) {
            if (!INSENSITIVE_CACHE.put(method.asString(), method)) {
                throw new IllegalStateException("INSENSITIVE_CACHE too small: " + (Object)((Object)method));
            }
            if (!CACHE.put(method.asString(), method)) {
                throw new IllegalStateException("CACHE too small: " + (Object)((Object)method));
            }
            if (LOOK_AHEAD.put(method.asString() + ' ', method)) continue;
            throw new IllegalStateException("LOOK_AHEAD too small: " + (Object)((Object)method));
        }
    }

    private static enum Type {
        NORMAL,
        IDEMPOTENT,
        SAFE;

    }
}

