/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import shadeio.univocity.parsers.common.AbstractParser;
import shadeio.univocity.parsers.common.ArgumentUtils;
import shadeio.univocity.parsers.common.CommonParserSettings;
import shadeio.univocity.parsers.common.NormalizedString;
import shadeio.univocity.parsers.common.fields.FieldIndexSelector;
import shadeio.univocity.parsers.common.fields.FieldSelector;
import shadeio.univocity.parsers.common.input.CharAppender;
import shadeio.univocity.parsers.common.input.NoopCharAppender;

public class ParserOutput {
    protected int column = 0;
    protected final String[] parsedValues;
    private final CharAppender[] appenders;
    protected final CommonParserSettings<?> settings;
    private final boolean skipEmptyLines;
    private final String nullValue;
    public CharAppender appender;
    private final CharAppender appenderInstance;
    private boolean columnsToExtractInitialized;
    private boolean columnsReordered;
    private boolean columnReorderingEnabledSetting;
    private String[] headerStrings;
    private NormalizedString[] headers;
    private int[] selectedIndexes;
    private long currentRecord;
    public boolean trim = false;
    public final Deque<String[]> pendingRecords = new LinkedList<String[]>();
    String[] parsedHeaders;
    private final AbstractParser<?> parser;

    public ParserOutput(CommonParserSettings<?> settings) {
        this(null, settings);
    }

    public ParserOutput(AbstractParser<?> parser, CommonParserSettings<?> settings) {
        this.parser = parser;
        this.appender = this.appenderInstance = settings.newCharAppender();
        this.parsedValues = new String[settings.getMaxColumns()];
        this.appenders = new CharAppender[settings.getMaxColumns() + 1];
        Arrays.fill(this.appenders, this.appender);
        this.settings = settings;
        this.skipEmptyLines = settings.getSkipEmptyLines();
        this.nullValue = settings.getNullValue();
        this.columnsToExtractInitialized = false;
        this.currentRecord = 0L;
        if (settings.isHeaderExtractionEnabled() && parser != null) {
            parser.ignoreTrailingWhitespace = false;
            parser.ignoreLeadingWhitespace = false;
        }
        if (settings.getHeaders() != null) {
            this.initializeHeaders();
        }
        this.columnReorderingEnabledSetting = settings.isColumnReorderingEnabled();
    }

    protected void initializeHeaders() {
        this.columnsReordered = false;
        this.selectedIndexes = null;
        this.appender = this.appenderInstance;
        Arrays.fill(this.appenders, this.appender);
        if (this.column > 0) {
            this.parsedHeaders = new String[this.column];
            System.arraycopy(this.parsedValues, 0, this.parsedHeaders, 0, this.column);
        }
        boolean usingParsedHeaders = false;
        this.headers = NormalizedString.toIdentifierGroupArray(this.settings.getHeaders());
        if (this.headers != null) {
            this.headers = (NormalizedString[])this.headers.clone();
        } else if (this.column > 0) {
            this.headers = NormalizedString.toIdentifierGroupArray((String[])this.parsedHeaders.clone());
            usingParsedHeaders = true;
        }
        if (this.parser != null) {
            this.parser.ignoreTrailingWhitespace = this.settings.getIgnoreTrailingWhitespaces();
            this.parser.ignoreLeadingWhitespace = this.settings.getIgnoreLeadingWhitespaces();
            if (usingParsedHeaders) {
                this.parser.initialize();
            }
        }
        if (usingParsedHeaders) {
            for (int i = 0; i < this.headers.length; ++i) {
                NormalizedString header = this.headers[i];
                if (header == null || header.isLiteral()) continue;
                if (this.settings.getIgnoreLeadingWhitespaces()) {
                    if (this.settings.getIgnoreTrailingWhitespaces()) {
                        this.headers[i] = NormalizedString.valueOf(this.headers[i].toString().trim());
                        continue;
                    }
                    this.headers[i] = NormalizedString.valueOf(ArgumentUtils.trim(this.headers[i].toString(), true, false));
                    continue;
                }
                if (!this.settings.getIgnoreTrailingWhitespaces()) continue;
                this.headers[i] = NormalizedString.valueOf(ArgumentUtils.trim(this.headers[i].toString(), false, true));
            }
        }
        this.columnsToExtractInitialized = true;
        this.initializeColumnsToExtract(this.headers);
    }

    public String[] rowParsed() {
        if (!this.pendingRecords.isEmpty()) {
            return this.pendingRecords.poll();
        }
        if (this.column > 0) {
            if (!this.columnsToExtractInitialized) {
                this.initializeHeaders();
                if (this.settings.isHeaderExtractionEnabled()) {
                    Arrays.fill(this.parsedValues, null);
                    this.column = 0;
                    this.appender = this.appenders[0];
                    return null;
                }
                if (!this.columnsReordered && this.selectedIndexes != null) {
                    String[] out = new String[this.column];
                    for (int i = 0; i < this.selectedIndexes.length; ++i) {
                        int index = this.selectedIndexes[i];
                        if (index >= this.column) continue;
                        out[index] = this.parsedValues[index];
                    }
                    this.column = 0;
                    return out;
                }
            }
            ++this.currentRecord;
            if (this.columnsReordered) {
                if (this.selectedIndexes.length == 0) {
                    this.column = 0;
                    return ArgumentUtils.EMPTY_STRING_ARRAY;
                }
                String[] reorderedValues = new String[this.selectedIndexes.length];
                for (int i = 0; i < this.selectedIndexes.length; ++i) {
                    int index = this.selectedIndexes[i];
                    reorderedValues[i] = index >= this.column || index == -1 ? this.nullValue : this.parsedValues[index];
                }
                this.column = 0;
                this.appender = this.appenders[0];
                return reorderedValues;
            }
            int last = this.columnReorderingEnabledSetting ? this.column : (this.column < this.headers.length ? this.headers.length : this.column);
            String[] out = new String[last];
            System.arraycopy(this.parsedValues, 0, out, 0, this.column);
            this.column = 0;
            this.appender = this.appenders[0];
            return out;
        }
        if (!this.skipEmptyLines) {
            if (!this.columnsToExtractInitialized) {
                this.initializeHeaders();
            }
            ++this.currentRecord;
            if (this.columnsReordered) {
                if (this.selectedIndexes.length == 0) {
                    return ArgumentUtils.EMPTY_STRING_ARRAY;
                }
                Object[] out = new String[this.selectedIndexes.length];
                Arrays.fill(out, this.nullValue);
                return out;
            }
            return new String[]{this.nullValue};
        }
        return null;
    }

    FieldSelector getFieldSelector() {
        return this.settings.getFieldSelector();
    }

    private void initializeColumnsToExtract(NormalizedString[] values) {
        FieldSelector selector = this.settings.getFieldSelector();
        if (selector != null) {
            this.selectedIndexes = selector.getFieldIndexes(values);
            if (this.selectedIndexes != null) {
                int length;
                Arrays.fill(this.appenders, NoopCharAppender.getInstance());
                for (int i = 0; i < this.selectedIndexes.length; ++i) {
                    int index = this.selectedIndexes[i];
                    if (index == -1) continue;
                    this.appenders[index] = this.appender;
                }
                this.columnsReordered = this.settings.isColumnReorderingEnabled();
                int n = length = values == null ? this.selectedIndexes.length : values.length;
                if (!this.columnsReordered && length < this.appenders.length && !(selector instanceof FieldIndexSelector)) {
                    Arrays.fill(this.appenders, length, this.appenders.length, this.appender);
                }
                this.appender = this.appenders[0];
            }
        }
    }

    public String[] getHeaderAsStringArray() {
        if (this.headerStrings == null) {
            this.headerStrings = NormalizedString.toArray(this.getHeaders());
        }
        return this.headerStrings;
    }

    public NormalizedString[] getHeaders() {
        if (this.parser != null) {
            this.parser.extractHeadersIfRequired();
        }
        if (this.headers == null) {
            this.headers = NormalizedString.toIdentifierGroupArray(this.settings.getHeaders());
        }
        return this.headers;
    }

    public int[] getSelectedIndexes() {
        return this.selectedIndexes;
    }

    public boolean isColumnReorderingEnabled() {
        return this.columnsReordered;
    }

    public int getCurrentColumn() {
        return this.column;
    }

    public void emptyParsed() {
        this.parsedValues[this.column++] = this.nullValue;
        this.appender = this.appenders[this.column];
    }

    public void valueParsed() {
        if (this.trim) {
            this.appender.updateWhitespace();
        }
        this.parsedValues[this.column++] = this.appender.getAndReset();
        this.appender = this.appenders[this.column];
    }

    public void valueParsed(String value) {
        this.parsedValues[this.column++] = value;
        this.appender = this.appenders[this.column];
    }

    public long getCurrentRecord() {
        return this.currentRecord;
    }

    public final void discardValues() {
        this.column = 0;
        this.appender = this.appenders[0];
    }

    final void reset() {
        this.columnsToExtractInitialized = false;
        this.currentRecord = 0L;
        this.column = 0;
        this.headers = null;
        this.headerStrings = null;
    }
}

