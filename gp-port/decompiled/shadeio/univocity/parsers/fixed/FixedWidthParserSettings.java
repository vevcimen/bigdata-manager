/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.fixed;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import shadeio.univocity.parsers.annotations.Headers;
import shadeio.univocity.parsers.annotations.helpers.AnnotationHelper;
import shadeio.univocity.parsers.common.CommonParserSettings;
import shadeio.univocity.parsers.common.NormalizedString;
import shadeio.univocity.parsers.common.input.CharAppender;
import shadeio.univocity.parsers.common.input.DefaultCharAppender;
import shadeio.univocity.parsers.fixed.FieldAlignment;
import shadeio.univocity.parsers.fixed.FixedWidthFields;
import shadeio.univocity.parsers.fixed.FixedWidthFormat;
import shadeio.univocity.parsers.fixed.Lookup;

public class FixedWidthParserSettings
extends CommonParserSettings<FixedWidthFormat> {
    protected boolean skipTrailingCharsUntilNewline = false;
    protected boolean recordEndsOnNewline = false;
    private boolean useDefaultPaddingForHeaders = true;
    private boolean keepPadding = false;
    private FixedWidthFields fieldLengths;
    private Map<String, FixedWidthFields> lookaheadFormats = new HashMap<String, FixedWidthFields>();
    private Map<String, FixedWidthFields> lookbehindFormats = new HashMap<String, FixedWidthFields>();

    public FixedWidthParserSettings(FixedWidthFields fieldLengths) {
        if (fieldLengths == null) {
            throw new IllegalArgumentException("Field lengths cannot be null");
        }
        this.fieldLengths = fieldLengths;
        NormalizedString[] names = fieldLengths.getFieldNames();
        if (names != null) {
            this.setHeaders(NormalizedString.toArray(names));
        }
    }

    public FixedWidthParserSettings() {
        this.fieldLengths = null;
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

    FieldAlignment[] getFieldAlignments() {
        if (this.fieldLengths == null) {
            return null;
        }
        return this.fieldLengths.getFieldAlignments();
    }

    public boolean getSkipTrailingCharsUntilNewline() {
        return this.skipTrailingCharsUntilNewline;
    }

    public void setSkipTrailingCharsUntilNewline(boolean skipTrailingCharsUntilNewline) {
        this.skipTrailingCharsUntilNewline = skipTrailingCharsUntilNewline;
    }

    public boolean getRecordEndsOnNewline() {
        return this.recordEndsOnNewline;
    }

    public void setRecordEndsOnNewline(boolean recordEndsOnNewline) {
        this.recordEndsOnNewline = recordEndsOnNewline;
    }

    @Override
    protected FixedWidthFormat createDefaultFormat() {
        return new FixedWidthFormat();
    }

    @Override
    protected CharAppender newCharAppender() {
        return new DefaultCharAppender(this.getMaxCharsPerColumn(), this.getNullValue(), this.getWhitespaceRangeStart());
    }

    @Override
    public int getMaxCharsPerColumn() {
        int max = super.getMaxCharsPerColumn();
        int minimum = 0;
        for (int length : this.calculateMaxFieldLengths()) {
            minimum += length + 2;
        }
        return max > minimum ? max : minimum;
    }

    @Override
    public int getMaxColumns() {
        int minimum;
        int max = super.getMaxColumns();
        return max > (minimum = this.calculateMaxFieldLengths().length) ? max : minimum;
    }

    private int[] calculateMaxFieldLengths() {
        return Lookup.calculateMaxFieldLengths(this.fieldLengths, this.lookaheadFormats, this.lookbehindFormats);
    }

    Lookup[] getLookaheadFormats() {
        return Lookup.getLookupFormats(this.lookaheadFormats, (FixedWidthFormat)this.getFormat());
    }

    Lookup[] getLookbehindFormats() {
        return Lookup.getLookupFormats(this.lookbehindFormats, (FixedWidthFormat)this.getFormat());
    }

    public void addFormatForLookahead(String lookahead, FixedWidthFields lengths) {
        Lookup.registerLookahead(lookahead, lengths, this.lookaheadFormats);
    }

    public void addFormatForLookbehind(String lookbehind, FixedWidthFields lengths) {
        Lookup.registerLookbehind(lookbehind, lengths, this.lookbehindFormats);
    }

    public boolean getUseDefaultPaddingForHeaders() {
        return this.useDefaultPaddingForHeaders;
    }

    public void setUseDefaultPaddingForHeaders(boolean useDefaultPaddingForHeaders) {
        this.useDefaultPaddingForHeaders = useDefaultPaddingForHeaders;
    }

    @Override
    protected void configureFromAnnotations(Class<?> beanClass) {
        if (this.fieldLengths == null) {
            try {
                this.fieldLengths = FixedWidthFields.forParsing(beanClass);
                Headers headerAnnotation = AnnotationHelper.findHeadersAnnotation(beanClass);
                if (this.headerExtractionEnabled == null && headerAnnotation != null) {
                    this.setHeaderExtractionEnabled(headerAnnotation.extract());
                }
            }
            catch (IllegalArgumentException e) {
                throw e;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (this.headerExtractionEnabled == null) {
            this.setHeaderExtractionEnabled(false);
        }
        super.configureFromAnnotations(beanClass);
        if (!this.isHeaderExtractionEnabled()) {
            FixedWidthFields.setHeadersIfPossible(this.fieldLengths, this);
        }
    }

    @Override
    protected void addConfiguration(Map<String, Object> out) {
        super.addConfiguration(out);
        out.put("Skip trailing characters until new line", this.skipTrailingCharsUntilNewline);
        out.put("Record ends on new line", this.recordEndsOnNewline);
        out.put("Field lengths", this.fieldLengths == null ? "<null>" : this.fieldLengths.toString());
        out.put("Lookahead formats", this.lookaheadFormats);
        out.put("Lookbehind formats", this.lookbehindFormats);
    }

    @Override
    public final FixedWidthParserSettings clone() {
        return (FixedWidthParserSettings)super.clone();
    }

    @Override
    @Deprecated
    protected final FixedWidthParserSettings clone(boolean clearInputSpecificSettings) {
        return this.clone(clearInputSpecificSettings, this.fieldLengths == null ? null : this.fieldLengths.clone());
    }

    public final FixedWidthParserSettings clone(FixedWidthFields fields) {
        return this.clone(true, fields);
    }

    private FixedWidthParserSettings clone(boolean clearInputSpecificSettings, FixedWidthFields fields) {
        FixedWidthParserSettings out = (FixedWidthParserSettings)super.clone(clearInputSpecificSettings);
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

    public final boolean getKeepPadding() {
        return this.keepPadding;
    }

    public final void setKeepPadding(boolean keepPadding) {
        this.keepPadding = keepPadding;
    }

    Boolean[] getKeepPaddingFlags() {
        if (this.fieldLengths == null) {
            return null;
        }
        Boolean[] keepFlags = this.fieldLengths.getKeepPaddingFlags();
        Object[] out = new Boolean[keepFlags.length];
        Arrays.fill(out, (Object)this.getKeepPadding());
        for (int i = 0; i < keepFlags.length; ++i) {
            Boolean flag = keepFlags[i];
            if (flag == null) continue;
            out[i] = flag;
        }
        return out;
    }
}

