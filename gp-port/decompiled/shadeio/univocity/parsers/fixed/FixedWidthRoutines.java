/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.fixed;

import java.io.Writer;
import shadeio.univocity.parsers.common.routine.AbstractRoutines;
import shadeio.univocity.parsers.fixed.FixedWidthFields;
import shadeio.univocity.parsers.fixed.FixedWidthParser;
import shadeio.univocity.parsers.fixed.FixedWidthParserSettings;
import shadeio.univocity.parsers.fixed.FixedWidthWriter;
import shadeio.univocity.parsers.fixed.FixedWidthWriterSettings;

public class FixedWidthRoutines
extends AbstractRoutines<FixedWidthParserSettings, FixedWidthWriterSettings> {
    public FixedWidthRoutines() {
        this(null, null);
    }

    public FixedWidthRoutines(FixedWidthParserSettings parserSettings) {
        this(parserSettings, null);
    }

    public FixedWidthRoutines(FixedWidthWriterSettings writerSettings) {
        this((FixedWidthParserSettings)null, writerSettings);
    }

    public FixedWidthRoutines(FixedWidthParserSettings parserSettings, FixedWidthWriterSettings writerSettings) {
        super("Fixed-width parsing/writing routine", parserSettings, writerSettings);
    }

    @Override
    protected void adjustColumnLengths(String[] headers, int[] lengths) {
        if (((FixedWidthWriterSettings)this.getWriterSettings()).getFieldLengths() == null) {
            ((FixedWidthWriterSettings)this.getWriterSettings()).setFieldLengths(new FixedWidthFields(headers, lengths));
        }
    }

    protected FixedWidthParser createParser(FixedWidthParserSettings parserSettings) {
        return new FixedWidthParser(parserSettings);
    }

    protected FixedWidthWriter createWriter(Writer output, FixedWidthWriterSettings writerSettings) {
        return new FixedWidthWriter(output, writerSettings);
    }

    @Override
    protected FixedWidthParserSettings createDefaultParserSettings() {
        return new FixedWidthParserSettings();
    }

    @Override
    protected FixedWidthWriterSettings createDefaultWriterSettings() {
        return new FixedWidthWriterSettings();
    }
}

