/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.csv;

import shadeio.univocity.parsers.csv.CsvFormat;
import shadeio.univocity.parsers.csv.CsvParserSettings;
import shadeio.univocity.parsers.csv.CsvWriterSettings;

public class Csv {
    public static CsvParserSettings parseExcel() {
        CsvParserSettings settings = new CsvParserSettings();
        ((CsvFormat)settings.getFormat()).setLineSeparator("\r\n");
        ((CsvFormat)settings.getFormat()).setComment('\u0000');
        settings.setParseUnescapedQuotes(false);
        settings.setSkipEmptyLines(false);
        settings.trimValues(false);
        return settings;
    }

    public static CsvParserSettings parseRfc4180() {
        CsvParserSettings settings = Csv.parseExcel();
        settings.setNormalizeLineEndingsWithinQuotes(false);
        return settings;
    }

    public static CsvWriterSettings writeExcel() {
        CsvWriterSettings settings = new CsvWriterSettings();
        ((CsvFormat)settings.getFormat()).setLineSeparator("\r\n");
        ((CsvFormat)settings.getFormat()).setComment('\u0000');
        settings.setEmptyValue(null);
        settings.setSkipEmptyLines(false);
        settings.trimValues(false);
        return settings;
    }

    public static CsvWriterSettings writeRfc4180() {
        CsvWriterSettings settings = Csv.writeExcel();
        settings.setNormalizeLineEndingsWithinQuotes(false);
        settings.setQuoteEscapingEnabled(true);
        return settings;
    }
}

