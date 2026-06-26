/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.csv;

public enum UnescapedQuoteHandling {
    STOP_AT_CLOSING_QUOTE,
    BACK_TO_DELIMITER,
    STOP_AT_DELIMITER,
    SKIP_VALUE,
    RAISE_ERROR;

}

