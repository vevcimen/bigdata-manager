/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.csv;

import java.util.Arrays;
import java.util.Map;
import shadeio.univocity.parsers.common.CommonWriterSettings;
import shadeio.univocity.parsers.common.fields.FieldEnumSelector;
import shadeio.univocity.parsers.common.fields.FieldIndexSelector;
import shadeio.univocity.parsers.common.fields.FieldNameSelector;
import shadeio.univocity.parsers.common.fields.FieldSelector;
import shadeio.univocity.parsers.common.fields.FieldSet;
import shadeio.univocity.parsers.csv.CsvFormat;

public class CsvWriterSettings
extends CommonWriterSettings<CsvFormat> {
    private boolean escapeUnquotedValues = false;
    private boolean quoteAllFields = false;
    private boolean isInputEscaped = false;
    private boolean normalizeLineEndingsWithinQuotes = true;
    private char[] quotationTriggers = new char[0];
    private boolean quoteEscapingEnabled = false;
    private boolean quoteNulls = true;
    private FieldSelector quotedFieldSelector = null;

    public boolean getQuoteAllFields() {
        return this.quoteAllFields;
    }

    public void setQuoteAllFields(boolean quoteAllFields) {
        this.quoteAllFields = quoteAllFields;
    }

    public boolean isEscapeUnquotedValues() {
        return this.escapeUnquotedValues;
    }

    public void setEscapeUnquotedValues(boolean escapeUnquotedValues) {
        this.escapeUnquotedValues = escapeUnquotedValues;
    }

    public final boolean isInputEscaped() {
        return this.isInputEscaped;
    }

    public final void setInputEscaped(boolean isInputEscaped) {
        this.isInputEscaped = isInputEscaped;
    }

    public boolean isNormalizeLineEndingsWithinQuotes() {
        return this.normalizeLineEndingsWithinQuotes;
    }

    public void setNormalizeLineEndingsWithinQuotes(boolean normalizeLineEndingsWithinQuotes) {
        this.normalizeLineEndingsWithinQuotes = normalizeLineEndingsWithinQuotes;
    }

    @Override
    protected CsvFormat createDefaultFormat() {
        return new CsvFormat();
    }

    public char[] getQuotationTriggers() {
        return this.quotationTriggers;
    }

    public void setQuotationTriggers(char ... quotationTriggers) {
        this.quotationTriggers = quotationTriggers == null ? new char[]{} : quotationTriggers;
    }

    public boolean isQuotationTrigger(char ch) {
        for (int i = 0; i < this.quotationTriggers.length; ++i) {
            if (this.quotationTriggers[i] != ch) continue;
            return true;
        }
        return false;
    }

    public boolean isQuoteEscapingEnabled() {
        return this.quoteEscapingEnabled;
    }

    public void setQuoteEscapingEnabled(boolean quoteEscapingEnabled) {
        this.quoteEscapingEnabled = quoteEscapingEnabled;
    }

    @Override
    protected void addConfiguration(Map<String, Object> out) {
        super.addConfiguration(out);
        out.put("Quote all fields", this.quoteAllFields);
        out.put("Escape unquoted values", this.escapeUnquotedValues);
        out.put("Normalize escaped line separators", this.normalizeLineEndingsWithinQuotes);
        out.put("Input escaped", this.isInputEscaped);
        out.put("Quote escaping enabled", this.quoteEscapingEnabled);
        out.put("Quotation triggers", Arrays.toString(this.quotationTriggers));
    }

    @Override
    public final CsvWriterSettings clone() {
        return (CsvWriterSettings)super.clone();
    }

    @Override
    public final CsvWriterSettings clone(boolean clearInputSpecificSettings) {
        return (CsvWriterSettings)super.clone(clearInputSpecificSettings);
    }

    final FieldSelector getQuotedFieldSelector() {
        return this.quotedFieldSelector;
    }

    private <T> FieldSet<T> setFieldSet(FieldSet<T> fieldSet, T ... values) {
        this.quotedFieldSelector = (FieldSelector)((Object)fieldSet);
        fieldSet.add(values);
        return fieldSet;
    }

    public final FieldSet<Enum> quoteFields(Enum ... columns) {
        return this.setFieldSet(new FieldEnumSelector(), columns);
    }

    public final FieldSet<String> quoteFields(String ... columns) {
        return this.setFieldSet(new FieldNameSelector(), columns);
    }

    public final FieldSet<Integer> quoteIndexes(Integer ... columns) {
        return this.setFieldSet(new FieldIndexSelector(), columns);
    }

    public void setQuoteNulls(boolean quoteNulls) {
        this.quoteNulls = quoteNulls;
    }

    public boolean getQuoteNulls() {
        return this.quoteNulls;
    }
}

