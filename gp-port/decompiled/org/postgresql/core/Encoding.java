/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.checkerframework.checker.nullness.qual.PolyNull;
import org.postgresql.core.AsciiStringInterner;

public class Encoding {
    private static final Logger LOGGER = Logger.getLogger(Encoding.class.getName());
    private static final Encoding DEFAULT_ENCODING = new Encoding();
    private static final Encoding UTF8_ENCODING = new Encoding(StandardCharsets.UTF_8, true);
    private static final HashMap<String, String[]> encodings = new HashMap();
    static final AsciiStringInterner INTERNER;
    private final Charset encoding;
    private final boolean fastASCIINumbers;

    private Encoding() {
        this(Charset.defaultCharset());
    }

    protected Encoding(Charset encoding, boolean fastASCIINumbers) {
        if (encoding == null) {
            throw new NullPointerException("Null encoding charset not supported");
        }
        this.encoding = encoding;
        this.fastASCIINumbers = fastASCIINumbers;
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "Creating new Encoding {0} with fastASCIINumbers {1}", new Object[]{encoding, fastASCIINumbers});
        }
    }

    protected Encoding(Charset encoding) {
        this(encoding, Encoding.testAsciiNumbers(encoding));
    }

    public boolean hasAsciiNumbers() {
        return this.fastASCIINumbers;
    }

    public static Encoding getJVMEncoding(String jvmEncoding) {
        if ("UTF-8".equals(jvmEncoding)) {
            return UTF8_ENCODING;
        }
        if (Charset.isSupported(jvmEncoding)) {
            return new Encoding(Charset.forName(jvmEncoding));
        }
        return DEFAULT_ENCODING;
    }

    public static Encoding getDatabaseEncoding(String databaseEncoding) {
        if ("UTF8".equals(databaseEncoding) || "UNICODE".equals(databaseEncoding)) {
            return UTF8_ENCODING;
        }
        String[] candidates = encodings.get(databaseEncoding);
        if (candidates != null) {
            for (String candidate : candidates) {
                LOGGER.log(Level.FINEST, "Search encoding candidate {0}", candidate);
                if (!Charset.isSupported(candidate)) continue;
                return new Encoding(Charset.forName(candidate));
            }
        }
        if (Charset.isSupported(databaseEncoding)) {
            return new Encoding(Charset.forName(databaseEncoding));
        }
        LOGGER.log(Level.FINEST, "{0} encoding not found, returning default encoding", databaseEncoding);
        return DEFAULT_ENCODING;
    }

    public static void canonicalize(String string) {
        INTERNER.putString(string);
    }

    public String name() {
        return this.encoding.name();
    }

    public byte @PolyNull [] encode(@PolyNull String s2) throws IOException {
        if (s2 == null) {
            return null;
        }
        return s2.getBytes(this.encoding);
    }

    public String decodeCanonicalized(byte[] encodedString, int offset, int length) throws IOException {
        if (length == 0) {
            return "";
        }
        return this.fastASCIINumbers ? INTERNER.getString(encodedString, offset, length, this) : this.decode(encodedString, offset, length);
    }

    public String decodeCanonicalizedIfPresent(byte[] encodedString, int offset, int length) throws IOException {
        if (length == 0) {
            return "";
        }
        return this.fastASCIINumbers ? INTERNER.getStringIfPresent(encodedString, offset, length, this) : this.decode(encodedString, offset, length);
    }

    public String decodeCanonicalized(byte[] encodedString) throws IOException {
        return this.decodeCanonicalized(encodedString, 0, encodedString.length);
    }

    public String decode(byte[] encodedString, int offset, int length) throws IOException {
        return new String(encodedString, offset, length, this.encoding);
    }

    public String decode(byte[] encodedString) throws IOException {
        return this.decode(encodedString, 0, encodedString.length);
    }

    public Reader getDecodingReader(InputStream in) throws IOException {
        return new InputStreamReader(in, this.encoding);
    }

    public Writer getEncodingWriter(OutputStream out) throws IOException {
        return new OutputStreamWriter(out, this.encoding);
    }

    public static Encoding defaultEncoding() {
        return DEFAULT_ENCODING;
    }

    public String toString() {
        return this.encoding.name();
    }

    private static boolean testAsciiNumbers(Charset encoding) {
        String test = "-0123456789";
        byte[] bytes = test.getBytes(encoding);
        String res = new String(bytes, StandardCharsets.US_ASCII);
        return test.equals(res);
    }

    static {
        encodings.put("SQL_ASCII", new String[]{"ASCII", "US-ASCII"});
        encodings.put("UNICODE", new String[]{"UTF-8", "UTF8"});
        encodings.put("UTF8", new String[]{"UTF-8", "UTF8"});
        encodings.put("LATIN1", new String[]{"ISO8859_1"});
        encodings.put("LATIN2", new String[]{"ISO8859_2"});
        encodings.put("LATIN3", new String[]{"ISO8859_3"});
        encodings.put("LATIN4", new String[]{"ISO8859_4"});
        encodings.put("ISO_8859_5", new String[]{"ISO8859_5"});
        encodings.put("ISO_8859_6", new String[]{"ISO8859_6"});
        encodings.put("ISO_8859_7", new String[]{"ISO8859_7"});
        encodings.put("ISO_8859_8", new String[]{"ISO8859_8"});
        encodings.put("LATIN5", new String[]{"ISO8859_9"});
        encodings.put("LATIN7", new String[]{"ISO8859_13"});
        encodings.put("LATIN9", new String[]{"ISO8859_15_FDIS"});
        encodings.put("EUC_JP", new String[]{"EUC_JP"});
        encodings.put("EUC_CN", new String[]{"EUC_CN"});
        encodings.put("EUC_KR", new String[]{"EUC_KR"});
        encodings.put("JOHAB", new String[]{"Johab"});
        encodings.put("EUC_TW", new String[]{"EUC_TW"});
        encodings.put("SJIS", new String[]{"MS932", "SJIS"});
        encodings.put("BIG5", new String[]{"Big5", "MS950", "Cp950"});
        encodings.put("GBK", new String[]{"GBK", "MS936"});
        encodings.put("UHC", new String[]{"MS949", "Cp949", "Cp949C"});
        encodings.put("TCVN", new String[]{"Cp1258"});
        encodings.put("WIN1256", new String[]{"Cp1256"});
        encodings.put("WIN1250", new String[]{"Cp1250"});
        encodings.put("WIN874", new String[]{"MS874", "Cp874"});
        encodings.put("WIN", new String[]{"Cp1251"});
        encodings.put("ALT", new String[]{"Cp866"});
        encodings.put("KOI8", new String[]{"KOI8_U", "KOI8_R"});
        encodings.put("UNKNOWN", new String[0]);
        encodings.put("MULE_INTERNAL", new String[0]);
        encodings.put("LATIN6", new String[0]);
        encodings.put("LATIN8", new String[0]);
        encodings.put("LATIN10", new String[0]);
        INTERNER = new AsciiStringInterner();
    }
}

