/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.http;

import org.eclipse.jetty.util.TypeUtil;

public class HttpTokens {
    static final byte COLON = 58;
    static final byte TAB = 9;
    static final byte LINE_FEED = 10;
    static final byte CARRIAGE_RETURN = 13;
    static final byte SPACE = 32;
    static final byte[] CRLF = new byte[]{13, 10};
    public static final Token[] TOKENS = new Token[256];

    static {
        block8: for (int b = 0; b < 256; ++b) {
            switch (b) {
                case 10: {
                    HttpTokens.TOKENS[b] = new Token((byte)b, Type.LF);
                    continue block8;
                }
                case 13: {
                    HttpTokens.TOKENS[b] = new Token((byte)b, Type.CR);
                    continue block8;
                }
                case 32: {
                    HttpTokens.TOKENS[b] = new Token((byte)b, Type.SPACE);
                    continue block8;
                }
                case 9: {
                    HttpTokens.TOKENS[b] = new Token((byte)b, Type.HTAB);
                    continue block8;
                }
                case 58: {
                    HttpTokens.TOKENS[b] = new Token((byte)b, Type.COLON);
                    continue block8;
                }
                case 33: 
                case 35: 
                case 36: 
                case 37: 
                case 38: 
                case 39: 
                case 42: 
                case 43: 
                case 45: 
                case 46: 
                case 94: 
                case 95: 
                case 96: 
                case 124: 
                case 126: {
                    HttpTokens.TOKENS[b] = new Token((byte)b, Type.TCHAR);
                    continue block8;
                }
                default: {
                    HttpTokens.TOKENS[b] = b >= 48 && b <= 57 ? new Token((byte)b, Type.DIGIT) : (b >= 65 && b <= 90 ? new Token((byte)b, Type.ALPHA) : (b >= 97 && b <= 122 ? new Token((byte)b, Type.ALPHA) : (b >= 33 && b <= 126 ? new Token((byte)b, Type.VCHAR) : (b >= 128 ? new Token((byte)b, Type.OTEXT) : new Token((byte)b, Type.CNTL)))));
                }
            }
        }
    }

    public static class Token {
        private final Type _type;
        private final byte _b;
        private final char _c;
        private final int _x;

        private Token(byte b, Type type) {
            this._type = type;
            this._b = b;
            this._c = (char)(0xFF & b);
            char lc = this._c >= 'A' & this._c <= 'Z' ? (char)(this._c - 65 + 97) : this._c;
            this._x = this._type == Type.DIGIT || this._type == Type.ALPHA && lc >= 'a' && lc <= 'f' ? TypeUtil.convertHexDigit(b) : -1;
        }

        public Type getType() {
            return this._type;
        }

        public byte getByte() {
            return this._b;
        }

        public char getChar() {
            return this._c;
        }

        public boolean isHexDigit() {
            return this._x >= 0;
        }

        public int getHexDigit() {
            return this._x;
        }

        public String toString() {
            switch (this._type) {
                case SPACE: 
                case COLON: 
                case ALPHA: 
                case DIGIT: 
                case TCHAR: 
                case VCHAR: {
                    return (Object)((Object)this._type) + "='" + this._c + "'";
                }
                case CR: {
                    return "CR=\\r";
                }
                case LF: {
                    return "LF=\\n";
                }
            }
            return String.format("%s=0x%x", new Object[]{this._type, this._b});
        }
    }

    public static enum Type {
        CNTL,
        HTAB,
        LF,
        CR,
        SPACE,
        COLON,
        DIGIT,
        ALPHA,
        TCHAR,
        VCHAR,
        OTEXT;

    }

    public static enum EndOfContent {
        UNKNOWN_CONTENT,
        NO_CONTENT,
        EOF_CONTENT,
        CONTENT_LENGTH,
        CHUNKED_CONTENT;

    }
}

