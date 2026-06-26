/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.tsv;

import java.io.Writer;
import shadeio.univocity.parsers.common.routine.AbstractRoutines;
import shadeio.univocity.parsers.tsv.TsvParser;
import shadeio.univocity.parsers.tsv.TsvParserSettings;
import shadeio.univocity.parsers.tsv.TsvWriter;
import shadeio.univocity.parsers.tsv.TsvWriterSettings;

public class TsvRoutines
extends AbstractRoutines<TsvParserSettings, TsvWriterSettings> {
    public TsvRoutines() {
        this(null, null);
    }

    public TsvRoutines(TsvParserSettings parserSettings) {
        this(parserSettings, null);
    }

    public TsvRoutines(TsvWriterSettings writerSettings) {
        this((TsvParserSettings)null, writerSettings);
    }

    public TsvRoutines(TsvParserSettings parserSettings, TsvWriterSettings writerSettings) {
        super("TSV parsing/writing routine", parserSettings, writerSettings);
    }

    protected TsvParser createParser(TsvParserSettings parserSettings) {
        return new TsvParser(parserSettings);
    }

    protected TsvWriter createWriter(Writer output, TsvWriterSettings writerSettings) {
        return new TsvWriter(output, writerSettings);
    }

    @Override
    protected TsvParserSettings createDefaultParserSettings() {
        return new TsvParserSettings();
    }

    @Override
    protected TsvWriterSettings createDefaultWriterSettings() {
        return new TsvWriterSettings();
    }
}

