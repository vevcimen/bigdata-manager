/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.input;

import shadeio.univocity.parsers.common.input.ExpandingCharAppender;

public class ElasticCharAppender
extends ExpandingCharAppender {
    private static final char[] EMPTY_CHAR_ARRAY = new char[0];
    private int defaultLength;

    public ElasticCharAppender(String emptyValue) {
        this(4096, emptyValue);
    }

    public ElasticCharAppender(int defaultLength, String emptyValue) {
        super(defaultLength, emptyValue, 0);
        this.defaultLength = defaultLength;
    }

    @Override
    public String getAndReset() {
        String out = super.getAndReset();
        if (this.chars.length > this.defaultLength) {
            this.chars = new char[this.defaultLength];
        }
        return out;
    }

    @Override
    public char[] getCharsAndReset() {
        char[] out = super.getCharsAndReset();
        if (this.chars.length > this.defaultLength) {
            this.chars = new char[this.defaultLength];
        }
        return out;
    }

    @Override
    public void reset() {
        if (this.chars.length > this.defaultLength) {
            this.chars = new char[this.defaultLength];
        }
        super.reset();
    }

    public String getTrimmedStringAndReset() {
        int start;
        int length = this.index - this.whitespaceCount;
        for (start = 0; start < length && this.chars[start] <= ' '; ++start) {
        }
        if (start >= length) {
            return this.emptyValue;
        }
        while (this.chars[length - 1] <= ' ') {
            --length;
        }
        if ((length -= start) <= 0) {
            return this.emptyValue;
        }
        String out = new String(this.chars, start, length);
        this.reset();
        return out;
    }

    public char[] getTrimmedCharsAndReset() {
        int start;
        int length = this.index - this.whitespaceCount;
        for (start = 0; start < length && this.chars[start] <= ' '; ++start) {
        }
        if (start >= length) {
            return EMPTY_CHAR_ARRAY;
        }
        while (this.chars[length - 1] <= ' ') {
            --length;
        }
        if ((length -= start) <= 0) {
            return EMPTY_CHAR_ARRAY;
        }
        char[] out = new char[length];
        System.arraycopy(this.chars, start, out, 0, length);
        this.reset();
        return out;
    }
}

