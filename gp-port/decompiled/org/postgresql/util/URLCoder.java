/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public final class URLCoder {
    private static final String ENCODING_FOR_URL = System.getProperty("postgresql.url.encoding", "UTF-8");

    public static String decode(String encoded) {
        try {
            return URLDecoder.decode(encoded, ENCODING_FOR_URL);
        }
        catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unable to decode URL entry via " + ENCODING_FOR_URL + ". This should not happen", e);
        }
    }

    public static String encode(String plain) {
        try {
            return URLEncoder.encode(plain, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unable to encode URL entry via " + ENCODING_FOR_URL + ". This should not happen", e);
        }
    }
}

