/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core;

import java.io.IOException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.core.Encoding;

public class EncodingPredictor {
    private static final Translation[] FATAL_TRANSLATIONS = new Translation[]{new Translation("\u0412\u0410\u0416\u041d\u041e", null, "ru", "WIN", "ALT", "KOI8"), new Translation("\u81f4\u547d\u9519\u8bef", null, "zh_CN", "EUC_CN", "GBK", "BIG5"), new Translation("KATASTROFALNY", null, "pl", "LATIN2"), new Translation("FATALE", null, "it", "LATIN1", "LATIN9"), new Translation("FATAL", new String[]{"\u306f\u5b58\u5728\u3057\u307e\u305b\u3093", "\u30ed\u30fc\u30eb", "\u30e6\u30fc\u30b6"}, "ja", "EUC_JP", "SJIS"), new Translation(null, null, "fr/de/es/pt_BR", "LATIN1", "LATIN3", "LATIN4", "LATIN5", "LATIN7", "LATIN9")};

    public static @Nullable DecodeResult decode(byte[] bytes, int offset, int length) {
        Encoding defaultEncoding = Encoding.defaultEncoding();
        for (Translation tr : FATAL_TRANSLATIONS) {
            for (String encoding : tr.encodings) {
                Encoding encoder = Encoding.getDatabaseEncoding(encoding);
                if (encoder == defaultEncoding) continue;
                if (tr.fatalText != null) {
                    byte[] encoded;
                    try {
                        byte[] tmp = encoder.encode(tr.fatalText);
                        encoded = new byte[tmp.length + 2];
                        encoded[0] = 83;
                        encoded[encoded.length - 1] = 0;
                        System.arraycopy(tmp, 0, encoded, 1, tmp.length);
                    }
                    catch (IOException e) {
                        continue;
                    }
                    if (!EncodingPredictor.arrayContains(bytes, offset, length, encoded, 0, encoded.length)) continue;
                }
                if (tr.texts != null) {
                    boolean foundOne = false;
                    for (String text : tr.texts) {
                        try {
                            byte[] textBytes = encoder.encode(text);
                            if (!EncodingPredictor.arrayContains(bytes, offset, length, textBytes, 0, textBytes.length)) continue;
                            foundOne = true;
                            break;
                        }
                        catch (IOException iOException) {
                            // empty catch block
                        }
                    }
                    if (!foundOne) continue;
                }
                try {
                    String decoded = encoder.decode(bytes, offset, length);
                    if (decoded.indexOf(65533) != -1) continue;
                    return new DecodeResult(decoded, encoder.name());
                }
                catch (IOException iOException) {
                    // empty catch block
                }
            }
        }
        return null;
    }

    private static boolean arrayContains(byte[] first, int firstOffset, int firstLength, byte[] second, int secondOffset, int secondLength) {
        if (firstLength < secondLength) {
            return false;
        }
        for (int i = 0; i < firstLength; ++i) {
            int j;
            while (i < firstLength && first[firstOffset + i] != second[secondOffset]) {
                ++i;
            }
            for (j = 1; j < secondLength && first[firstOffset + i + j] == second[secondOffset + j]; ++j) {
            }
            if (j != secondLength) continue;
            return true;
        }
        return false;
    }

    static class Translation {
        public final @Nullable String fatalText;
        private final String @Nullable [] texts;
        public final String language;
        public final String[] encodings;

        Translation(@Nullable String fatalText, String @Nullable [] texts, String language, String ... encodings) {
            this.fatalText = fatalText;
            this.texts = texts;
            this.language = language;
            this.encodings = encodings;
        }
    }

    public static class DecodeResult {
        public final String result;
        public final @Nullable String encoding;

        DecodeResult(String result, @Nullable String encoding) {
            this.result = result;
            this.encoding = encoding;
        }
    }
}

