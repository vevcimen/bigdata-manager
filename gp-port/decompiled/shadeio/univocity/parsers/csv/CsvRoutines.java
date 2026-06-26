/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.csv;

import java.io.Writer;
import shadeio.univocity.parsers.common.routine.AbstractRoutines;
import shadeio.univocity.parsers.csv.CsvParser;
import shadeio.univocity.parsers.csv.CsvParserSettings;
import shadeio.univocity.parsers.csv.CsvWriter;
import shadeio.univocity.parsers.csv.CsvWriterSettings;

public class CsvRoutines
extends AbstractRoutines<CsvParserSettings, CsvWriterSettings> {
    public CsvRoutines() {
        this(null, null);
    }

    public CsvRoutines(CsvParserSettings parserSettings) {
        this(parserSettings, null);
    }

    public CsvRoutines(CsvWriterSettings writerSettings) {
        this((CsvParserSettings)null, writerSettings);
    }

    public CsvRoutines(CsvParserSettings parserSettings, CsvWriterSettings writerSettings) {
        super("CSV parsing/writing routine", parserSettings, writerSettings);
    }

    protected CsvParser createParser(CsvParserSettings parserSettings) {
        return new CsvParser(parserSettings);
    }

    protected CsvWriter createWriter(Writer output, CsvWriterSettings writerSettings) {
        return new CsvWriter(output, writerSettings);
    }

    @Override
    protected CsvParserSettings createDefaultParserSettings() {
        return new CsvParserSettings();
    }

    @Override
    protected CsvWriterSettings createDefaultWriterSettings() {
        return new CsvWriterSettings();
    }
}

