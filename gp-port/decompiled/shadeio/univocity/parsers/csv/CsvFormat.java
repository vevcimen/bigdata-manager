/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.csv;

import java.util.TreeMap;
import shadeio.univocity.parsers.common.Format;

public class CsvFormat
extends Format {
    private char quote = (char)34;
    private char quoteEscape = (char)34;
    private String delimiter = ",";
    private Character charToEscapeQuoteEscaping = null;

    public char getQuote() {
        return this.quote;
    }

    public void setQuote(char quote) {
        this.quote = quote;
    }

    public boolean isQuote(char ch) {
        return this.quote == ch;
    }

    public char getQuoteEscape() {
        return this.quoteEscape;
    }

    public void setQuoteEscape(char quoteEscape) {
        this.quoteEscape = quoteEscape;
    }

    public boolean isQuoteEscape(char ch) {
        return this.quoteEscape == ch;
    }

    public char getDelimiter() {
        if (this.delimiter.length() > 1) {
            throw new UnsupportedOperationException("Delimiter '" + this.delimiter + "' has more than one character. Use method getDelimiterString()");
        }
        return this.delimiter.charAt(0);
    }

    public String getDelimiterString() {
        return this.delimiter;
    }

    public void setDelimiter(char delimiter) {
        this.delimiter = String.valueOf(delimiter);
    }

    public void setDelimiter(String delimiter) {
        if (delimiter == null) {
            throw new IllegalArgumentException("Delimiter cannot be null");
        }
        if (delimiter.isEmpty()) {
            throw new IllegalArgumentException("Delimiter cannot be empty");
        }
        this.delimiter = delimiter;
    }

    public boolean isDelimiter(char ch) {
        if (this.delimiter.length() > 1) {
            throw new UnsupportedOperationException("Delimiter '" + this.delimiter + "' has more than one character. Use method isDelimiter(String)");
        }
        return this.delimiter.charAt(0) == ch;
    }

    public boolean isDelimiter(String sequence) {
        return this.delimiter.equals(sequence);
    }

    public final char getCharToEscapeQuoteEscaping() {
        if (this.charToEscapeQuoteEscaping == null) {
            if (this.quote == this.quoteEscape) {
                return '\u0000';
            }
            return this.quoteEscape;
        }
        return this.charToEscapeQuoteEscaping.charValue();
    }

    public final void setCharToEscapeQuoteEscaping(char charToEscapeQuoteEscaping) {
        this.charToEscapeQuoteEscaping = Character.valueOf(charToEscapeQuoteEscaping);
    }

    public final boolean isCharToEscapeQuoteEscaping(char ch) {
        char current = this.getCharToEscapeQuoteEscaping();
        return current != '\u0000' && current == ch;
    }

    @Override
    protected TreeMap<String, Object> getConfiguration() {
        TreeMap<String, Object> out = new TreeMap<String, Object>();
        out.put("Quote character", Character.valueOf(this.quote));
        out.put("Quote escape character", Character.valueOf(this.quoteEscape));
        out.put("Quote escape escape character", this.charToEscapeQuoteEscaping);
        out.put("Field delimiter", this.delimiter);
        return out;
    }

    @Override
    public final CsvFormat clone() {
        return (CsvFormat)super.clone();
    }
}

