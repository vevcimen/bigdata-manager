/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.http;

import java.nio.ByteBuffer;
import org.eclipse.jetty.util.ArrayTrie;
import org.eclipse.jetty.util.BufferUtil;
import org.eclipse.jetty.util.Trie;

public enum HttpScheme {
    HTTP("http"),
    HTTPS("https"),
    WS("ws"),
    WSS("wss");

    public static final Trie<HttpScheme> CACHE;
    private final String _string;
    private final ByteBuffer _buffer;

    private HttpScheme(String s2) {
        this._string = s2;
        this._buffer = BufferUtil.toBuffer(s2);
    }

    public ByteBuffer asByteBuffer() {
        return this._buffer.asReadOnlyBuffer();
    }

    public boolean is(String s2) {
        return s2 != null && this._string.equalsIgnoreCase(s2);
    }

    public String asString() {
        return this._string;
    }

    public String toString() {
        return this._string;
    }

    static {
        CACHE = new ArrayTrie<HttpScheme>();
        for (HttpScheme version : HttpScheme.values()) {
            CACHE.put(version.asString(), version);
        }
    }
}

