/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.tsv;

import java.util.TreeMap;
import shadeio.univocity.parsers.common.Format;

public class TsvFormat
extends Format {
    private char escapeChar = (char)92;
    private char escapedTabChar = (char)116;

    public void setEscapeChar(char escapeChar) {
        this.escapeChar = escapeChar;
    }

    public char getEscapeChar() {
        return this.escapeChar;
    }

    public char getEscapedTabChar() {
        return this.escapedTabChar;
    }

    public void setEscapedTabChar(char escapedTabChar) {
        this.escapedTabChar = escapedTabChar;
    }

    public boolean isEscapeChar(char ch) {
        return this.escapeChar == ch;
    }

    @Override
    protected TreeMap<String, Object> getConfiguration() {
        TreeMap<String, Object> out = new TreeMap<String, Object>();
        out.put("Escape character", Character.valueOf(this.escapeChar));
        return out;
    }

    @Override
    public final TsvFormat clone() {
        return (TsvFormat)super.clone();
    }
}

