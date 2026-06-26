/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.fixed;

import java.util.HashMap;
import java.util.Map;
import shadeio.univocity.parsers.annotations.Headers;
import shadeio.univocity.parsers.annotations.helpers.AnnotationHelper;
import shadeio.univocity.parsers.common.CommonWriterSettings;
import shadeio.univocity.parsers.common.NormalizedString;
import shadeio.univocity.parsers.fixed.FieldAlignment;
import shadeio.univocity.parsers.fixed.FixedWidthFields;
import shadeio.univocity.parsers.fixed.FixedWidthFormat;
import shadeio.univocity.parsers.fixed.Lookup;

public class FixedWidthWriterSettings
extends CommonWriterSettings<FixedWidthFormat> {
    private FixedWidthFields fieldLengths;
    private Map<String, FixedWidthFields> lookaheadFormats = new HashMap<String, FixedWidthFields>();
    private Map<String, FixedWidthFields> lookbehindFormats = new HashMap<String, FixedWidthFields>();
    private boolean useDefaultPaddingForHeaders = true;
    private FieldAlignment defaultAlignmentForHeaders = null;
    private boolean writeLineSeparatorAfterRecord = true;

    public FixedWidthWriterSettings(FixedWidthFields fieldLengths) {
        this.setFieldLengths(fieldLengths);
        NormalizedString[] names = fieldLengths.getFieldNames();
        if (names != null) {
            this.setHeaders(NormalizedString.toArray(names));
        }
    }

    public FixedWidthWriterSettings() {
        this.fieldLengths = null;
    }

    final void setFieldLengths(FixedWidthFields fieldLengths) {
        if (fieldLengths == null) {
            throw new IllegalArgumentException("Field lengths cannot be null");
        }
        this.fieldLengths = fieldLengths;
    }

    int[] getFieldLengths() {
        if (this.fieldLengths == null) {
            return null;
        }
        return this.fieldLengths.getFieldLengths();
    }

    int[] getAllLengths() {
        if (this.fieldLengths == null) {
            return null;
        }
        return this.fieldLengths.getAllLengths();
    }

    FieldAlignment[] getFieldAlignments() {
        if (this.fieldLengths == null) {
            return null;
        }
        return this.fieldLengths.getFieldAlignments();
    }

    char[] getFieldPaddings() {
        if (this.fieldLengths == null) {
            return null;
        }
        return this.fieldLengths.getFieldPaddings((FixedWidthFormat)this.getFormat());
    }

    boolean[] getFieldsToIgnore() {
        if (this.fieldLengths == null) {
            return null;
        }
        return this.fieldLengths.getFieldsToIgnore();
    }

    @Override
    protected FixedWidthFormat createDefaultFormat() {
        return new FixedWidthFormat();
    }

    @Override
    public int getMaxColumns() {
        int minimum;
        int max = super.getMaxColumns();
        return max > (minimum = Lookup.calculateMaxFieldLengths(this.fieldLengths, this.lookaheadFormats, this.lookbehindFormats).length) ? max : minimum;
    }

    public void addFormatForLookahead(String lookahead, FixedWidthFields lengths) {
        Lookup.registerLookahead(lookahead, lengths, this.lookaheadFormats);
    }

    public void addFormatForLookbehind(String lookbehind, FixedWidthFields lengths) {
        Lookup.registerLookbehind(lookbehind, lengths, this.lookbehindFormats);
    }

    Lookup[] getLookaheadFormats() {
        return Lookup.getLookupFormats(this.lookaheadFormats, (FixedWidthFormat)this.getFormat());
    }

    Lookup[] getLookbehindFormats() {
        return Lookup.getLookupFormats(this.lookbehindFormats, (FixedWidthFormat)this.getFormat());
    }

    public boolean getUseDefaultPaddingForHeaders() {
        return this.useDefaultPaddingForHeaders;
    }

    public void setUseDefaultPaddingForHeaders(boolean useDefaultPaddingForHeaders) {
        this.useDefaultPaddingForHeaders = useDefaultPaddingForHeaders;
    }

    public FieldAlignment getDefaultAlignmentForHeaders() {
        return this.defaultAlignmentForHeaders;
    }

    public void setDefaultAlignmentForHeaders(FieldAlignment defaultAlignmentForHeaders) {
        this.defaultAlignmentForHeaders = defaultAlignmentForHeaders;
    }

    public boolean getWriteLineSeparatorAfterRecord() {
        return this.writeLineSeparatorAfterRecord;
    }

    public void setWriteLineSeparatorAfterRecord(boolean writeLineSeparatorAfterRecord) {
        this.writeLineSeparatorAfterRecord = writeLineSeparatorAfterRecord;
    }

    @Override
    protected void configureFromAnnotations(Class<?> beanClass) {
        if (this.fieldLengths != null) {
            return;
        }
        try {
            this.fieldLengths = FixedWidthFields.forWriting(beanClass);
            Headers headerAnnotation = AnnotationHelper.findHeadersAnnotation(beanClass);
            this.setHeaderWritingEnabled(headerAnnotation != null && headerAnnotation.write());
        }
        catch (Exception exception) {
            // empty catch block
        }
        super.configureFromAnnotations(beanClass);
        FixedWidthFields.setHeadersIfPossible(this.fieldLengths, this);
    }

    @Override
    protected void addConfiguration(Map<String, Object> out) {
        super.addConfiguration(out);
        out.put("Write line separator after record", this.writeLineSeparatorAfterRecord);
        out.put("Field lengths", this.fieldLengths);
        out.put("Lookahead formats", this.lookaheadFormats);
        out.put("Lookbehind formats", this.lookbehindFormats);
        out.put("Use default padding for headers", this.useDefaultPaddingForHeaders);
        out.put("Default alignment for headers", (Object)this.defaultAlignmentForHeaders);
    }

    @Override
    public final FixedWidthWriterSettings clone() {
        return (FixedWidthWriterSettings)super.clone(false);
    }

    @Override
    @Deprecated
    protected final FixedWidthWriterSettings clone(boolean clearInputSpecificSettings) {
        return this.clone(clearInputSpecificSettings, this.fieldLengths == null ? null : this.fieldLengths.clone());
    }

    public final FixedWidthWriterSettings clone(FixedWidthFields fields) {
        return this.clone(true, fields);
    }

    private FixedWidthWriterSettings clone(boolean clearInputSpecificSettings, FixedWidthFields fields) {
        FixedWidthWriterSettings out = (FixedWidthWriterSettings)super.clone(clearInputSpecificSettings);
        out.fieldLengths = fields;
        if (clearInputSpecificSettings) {
            out.lookaheadFormats = new HashMap<String, FixedWidthFields>();
            out.lookbehindFormats = new HashMap<String, FixedWidthFields>();
        } else {
            out.lookaheadFormats = new HashMap<String, FixedWidthFields>(this.lookaheadFormats);
            out.lookbehindFormats = new HashMap<String, FixedWidthFields>(this.lookbehindFormats);
        }
        return out;
    }
}

