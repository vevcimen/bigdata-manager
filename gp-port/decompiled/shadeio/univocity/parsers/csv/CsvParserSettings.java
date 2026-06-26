/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.csv;

import java.util.Arrays;
import java.util.Map;
import shadeio.univocity.parsers.common.CommonParserSettings;
import shadeio.univocity.parsers.common.input.CharAppender;
import shadeio.univocity.parsers.common.input.DefaultCharAppender;
import shadeio.univocity.parsers.common.input.ExpandingCharAppender;
import shadeio.univocity.parsers.csv.CsvFormat;
import shadeio.univocity.parsers.csv.UnescapedQuoteHandling;

public class CsvParserSettings
extends CommonParserSettings<CsvFormat> {
    private String emptyValue = null;
    private boolean parseUnescapedQuotes = true;
    private boolean parseUnescapedQuotesUntilDelimiter = true;
    private boolean escapeUnquotedValues = false;
    private boolean keepEscapeSequences = false;
    private boolean keepQuotes = false;
    private boolean normalizeLineEndingsWithinQuotes = true;
    private boolean ignoreTrailingWhitespacesInQuotes = false;
    private boolean ignoreLeadingWhitespacesInQuotes = false;
    private boolean delimiterDetectionEnabled = false;
    private boolean quoteDetectionEnabled = false;
    private UnescapedQuoteHandling unescapedQuoteHandling = null;
    private char[] delimitersForDetection = null;
    private int formatDetectorRowSampleCount = 20;

    public String getEmptyValue() {
        return this.emptyValue;
    }

    public void setEmptyValue(String emptyValue) {
        this.emptyValue = emptyValue;
    }

    @Override
    protected CharAppender newCharAppender() {
        int chars = this.getMaxCharsPerColumn();
        if (chars != -1) {
            return new DefaultCharAppender(chars, this.emptyValue, this.getWhitespaceRangeStart());
        }
        return new ExpandingCharAppender(this.emptyValue, this.getWhitespaceRangeStart());
    }

    @Override
    protected CsvFormat createDefaultFormat() {
        return new CsvFormat();
    }

    @Deprecated
    public boolean isParseUnescapedQuotes() {
        return this.parseUnescapedQuotes || this.unescapedQuoteHandling != null && this.unescapedQuoteHandling != UnescapedQuoteHandling.RAISE_ERROR;
    }

    @Deprecated
    public void setParseUnescapedQuotes(boolean parseUnescapedQuotes) {
        this.parseUnescapedQuotes = parseUnescapedQuotes;
    }

    @Deprecated
    public void setParseUnescapedQuotesUntilDelimiter(boolean parseUnescapedQuotesUntilDelimiter) {
        if (parseUnescapedQuotesUntilDelimiter) {
            this.parseUnescapedQuotes = true;
        }
        this.parseUnescapedQuotesUntilDelimiter = parseUnescapedQuotesUntilDelimiter;
    }

    @Deprecated
    public boolean isParseUnescapedQuotesUntilDelimiter() {
        return this.parseUnescapedQuotesUntilDelimiter && this.isParseUnescapedQuotes() || this.unescapedQuoteHandling == UnescapedQuoteHandling.STOP_AT_DELIMITER || this.unescapedQuoteHandling == UnescapedQuoteHandling.SKIP_VALUE;
    }

    public boolean isEscapeUnquotedValues() {
        return this.escapeUnquotedValues;
    }

    public void setEscapeUnquotedValues(boolean escapeUnquotedValues) {
        this.escapeUnquotedValues = escapeUnquotedValues;
    }

    public final boolean isKeepEscapeSequences() {
        return this.keepEscapeSequences;
    }

    public final void setKeepEscapeSequences(boolean keepEscapeSequences) {
        this.keepEscapeSequences = keepEscapeSequences;
    }

    public final boolean isDelimiterDetectionEnabled() {
        return this.delimiterDetectionEnabled;
    }

    public final void setDelimiterDetectionEnabled(boolean separatorDetectionEnabled) {
        this.setDelimiterDetectionEnabled(separatorDetectionEnabled, new char[0]);
    }

    public final void setDelimiterDetectionEnabled(boolean separatorDetectionEnabled, char ... delimitersForDetection) {
        this.delimiterDetectionEnabled = separatorDetectionEnabled;
        this.delimitersForDetection = delimitersForDetection;
    }

    public final boolean isQuoteDetectionEnabled() {
        return this.quoteDetectionEnabled;
    }

    public final void setQuoteDetectionEnabled(boolean quoteDetectionEnabled) {
        this.quoteDetectionEnabled = quoteDetectionEnabled;
    }

    public final void detectFormatAutomatically() {
        this.detectFormatAutomatically(new char[0]);
    }

    public final void detectFormatAutomatically(char ... delimitersForDetection) {
        this.setDelimiterDetectionEnabled(true, delimitersForDetection);
        this.setQuoteDetectionEnabled(true);
        this.setLineSeparatorDetectionEnabled(true);
    }

    public boolean isNormalizeLineEndingsWithinQuotes() {
        return this.normalizeLineEndingsWithinQuotes;
    }

    public void setNormalizeLineEndingsWithinQuotes(boolean normalizeLineEndingsWithinQuotes) {
        this.normalizeLineEndingsWithinQuotes = normalizeLineEndingsWithinQuotes;
    }

    public void setUnescapedQuoteHandling(UnescapedQuoteHandling unescapedQuoteHandling) {
        this.unescapedQuoteHandling = unescapedQuoteHandling;
    }

    public UnescapedQuoteHandling getUnescapedQuoteHandling() {
        return this.unescapedQuoteHandling;
    }

    public boolean getKeepQuotes() {
        return this.keepQuotes;
    }

    public void setKeepQuotes(boolean keepQuotes) {
        this.keepQuotes = keepQuotes;
    }

    @Override
    protected void addConfiguration(Map<String, Object> out) {
        super.addConfiguration(out);
        out.put("Empty value", this.emptyValue);
        out.put("Unescaped quote handling", (Object)this.unescapedQuoteHandling);
        out.put("Escape unquoted values", this.escapeUnquotedValues);
        out.put("Keep escape sequences", this.keepEscapeSequences);
        out.put("Keep quotes", this.keepQuotes);
        out.put("Normalize escaped line separators", this.normalizeLineEndingsWithinQuotes);
        out.put("Autodetect column delimiter", this.delimiterDetectionEnabled);
        out.put("Autodetect quotes", this.quoteDetectionEnabled);
        out.put("Delimiters for detection", Arrays.toString(this.delimitersForDetection));
        out.put("Ignore leading whitespaces in quotes", this.ignoreLeadingWhitespacesInQuotes);
        out.put("Ignore trailing whitespaces in quotes", this.ignoreTrailingWhitespacesInQuotes);
    }

    @Override
    public final CsvParserSettings clone() {
        return (CsvParserSettings)super.clone();
    }

    @Override
    public final CsvParserSettings clone(boolean clearInputSpecificSettings) {
        return (CsvParserSettings)super.clone(clearInputSpecificSettings);
    }

    public final char[] getDelimitersForDetection() {
        return this.delimitersForDetection;
    }

    public boolean getIgnoreTrailingWhitespacesInQuotes() {
        return this.ignoreTrailingWhitespacesInQuotes;
    }

    public void setIgnoreTrailingWhitespacesInQuotes(boolean ignoreTrailingWhitespacesInQuotes) {
        this.ignoreTrailingWhitespacesInQuotes = ignoreTrailingWhitespacesInQuotes;
    }

    public boolean getIgnoreLeadingWhitespacesInQuotes() {
        return this.ignoreLeadingWhitespacesInQuotes;
    }

    public void setIgnoreLeadingWhitespacesInQuotes(boolean ignoreLeadingWhitespacesInQuotes) {
        this.ignoreLeadingWhitespacesInQuotes = ignoreLeadingWhitespacesInQuotes;
    }

    public final void trimQuotedValues(boolean trim) {
        this.setIgnoreTrailingWhitespacesInQuotes(trim);
        this.setIgnoreLeadingWhitespacesInQuotes(trim);
    }

    public int getFormatDetectorRowSampleCount() {
        return this.formatDetectorRowSampleCount;
    }

    public void setFormatDetectorRowSampleCount(int formatDetectorRowSampleCount) {
        this.formatDetectorRowSampleCount = formatDetectorRowSampleCount <= 0 ? 20 : formatDetectorRowSampleCount;
    }
}

