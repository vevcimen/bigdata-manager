/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common;

import java.io.File;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import shadeio.univocity.parsers.common.AbstractException;
import shadeio.univocity.parsers.common.ArgumentUtils;
import shadeio.univocity.parsers.common.CommonSettings;
import shadeio.univocity.parsers.common.CommonWriterSettings;
import shadeio.univocity.parsers.common.DataProcessingException;
import shadeio.univocity.parsers.common.DefaultConversionProcessor;
import shadeio.univocity.parsers.common.DummyFormat;
import shadeio.univocity.parsers.common.Format;
import shadeio.univocity.parsers.common.NormalizedString;
import shadeio.univocity.parsers.common.TextWritingException;
import shadeio.univocity.parsers.common.fields.ExcludeFieldNameSelector;
import shadeio.univocity.parsers.common.fields.FieldIndexSelector;
import shadeio.univocity.parsers.common.fields.FieldNameSelector;
import shadeio.univocity.parsers.common.fields.FieldSelector;
import shadeio.univocity.parsers.common.input.WriterCharAppender;
import shadeio.univocity.parsers.common.processor.RowWriterProcessor;
import shadeio.univocity.parsers.common.processor.RowWriterProcessorSwitch;
import shadeio.univocity.parsers.common.record.Record;

public abstract class AbstractWriter<S extends CommonWriterSettings<?>> {
    private final RowWriterProcessor writerProcessor;
    private Writer writer;
    private final boolean skipEmptyLines;
    protected final char comment;
    private final WriterCharAppender rowAppender;
    private final boolean isHeaderWritingEnabled;
    private Object[] outputRow;
    private int[] indexesToWrite;
    private final char[] lineSeparator;
    protected NormalizedString[] headers;
    protected long recordCount = 0L;
    protected final String nullValue;
    protected final String emptyValue;
    protected final WriterCharAppender appender;
    private final Object[] partialLine;
    private int partialLineIndex = 0;
    private Map<NormalizedString[], Map<NormalizedString, Integer>> headerIndexes;
    private int largestRowLength = -1;
    protected boolean writingHeaders = false;
    protected boolean[] headerTrimFlags;
    private NormalizedString[] dummyHeaderRow;
    protected boolean expandRows;
    private boolean usingSwitch;
    private boolean enableNewlineAfterRecord = true;
    protected boolean usingNullOrEmptyValue;
    protected final int whitespaceRangeStart;
    private final boolean columnReorderingEnabled;
    protected boolean ignoreLeading;
    protected boolean ignoreTrailing;
    private final CommonSettings<DummyFormat> internalSettings = new CommonSettings<DummyFormat>(){

        @Override
        protected DummyFormat createDefaultFormat() {
            return DummyFormat.instance;
        }
    };
    private final int errorContentLength;

    public AbstractWriter(S settings) {
        this((Writer)null, settings);
    }

    public AbstractWriter(File file, S settings) {
        this(ArgumentUtils.newWriter(file), settings);
    }

    public AbstractWriter(File file, String encoding, S settings) {
        this(ArgumentUtils.newWriter(file, encoding), settings);
    }

    public AbstractWriter(File file, Charset encoding, S settings) {
        this(ArgumentUtils.newWriter(file, encoding), settings);
    }

    public AbstractWriter(OutputStream output, S settings) {
        this(ArgumentUtils.newWriter(output), settings);
    }

    public AbstractWriter(OutputStream output, String encoding, S settings) {
        this(ArgumentUtils.newWriter(output, encoding), settings);
    }

    public AbstractWriter(OutputStream output, Charset encoding, S settings) {
        this(ArgumentUtils.newWriter(output, encoding), settings);
    }

    public AbstractWriter(Writer writer, S settings) {
        ((CommonSettings)settings).autoConfigure();
        this.ignoreLeading = ((CommonSettings)settings).getIgnoreLeadingWhitespaces();
        this.ignoreTrailing = ((CommonSettings)settings).getIgnoreTrailingWhitespaces();
        this.internalSettings.setMaxColumns(((CommonSettings)settings).getMaxColumns());
        this.errorContentLength = ((CommonSettings)settings).getErrorContentLength();
        this.nullValue = ((CommonSettings)settings).getNullValue();
        this.emptyValue = ((CommonWriterSettings)settings).getEmptyValue();
        this.lineSeparator = ((Format)((CommonSettings)settings).getFormat()).getLineSeparator();
        this.comment = ((Format)((CommonSettings)settings).getFormat()).getComment();
        this.skipEmptyLines = ((CommonSettings)settings).getSkipEmptyLines();
        this.writerProcessor = ((CommonWriterSettings)settings).getRowWriterProcessor();
        this.usingSwitch = this.writerProcessor instanceof RowWriterProcessorSwitch;
        this.expandRows = ((CommonWriterSettings)settings).getExpandIncompleteRows();
        this.columnReorderingEnabled = ((CommonWriterSettings)settings).isColumnReorderingEnabled();
        this.whitespaceRangeStart = ((CommonSettings)settings).getWhitespaceRangeStart();
        this.appender = new WriterCharAppender(((CommonSettings)settings).getMaxCharsPerColumn(), "", this.whitespaceRangeStart, (Format)((CommonSettings)settings).getFormat());
        this.rowAppender = new WriterCharAppender(((CommonSettings)settings).getMaxCharsPerColumn(), "", this.whitespaceRangeStart, (Format)((CommonSettings)settings).getFormat());
        this.writer = writer;
        this.headers = NormalizedString.toIdentifierGroupArray(((CommonSettings)settings).getHeaders());
        this.updateIndexesToWrite((CommonSettings<?>)settings);
        this.partialLine = new Object[((CommonSettings)settings).getMaxColumns()];
        this.isHeaderWritingEnabled = ((CommonWriterSettings)settings).isHeaderWritingEnabled();
        if (this.writerProcessor instanceof DefaultConversionProcessor) {
            DefaultConversionProcessor conversionProcessor = (DefaultConversionProcessor)((Object)this.writerProcessor);
            conversionProcessor.context = null;
            conversionProcessor.errorHandler = ((CommonSettings)settings).getProcessorErrorHandler();
        }
        this.initialize(settings);
    }

    protected void enableNewlineAfterRecord(boolean enableNewlineAfterRecord) {
        this.enableNewlineAfterRecord = enableNewlineAfterRecord;
    }

    protected abstract void initialize(S var1);

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void updateIndexesToWrite(CommonSettings<?> settings) {
        FieldSelector selector = settings.getFieldSelector();
        if (selector != null) {
            if (this.headers != null && this.headers.length > 0) {
                this.indexesToWrite = selector.getFieldIndexes(this.headers);
                if (this.columnReorderingEnabled) {
                    int size = ArgumentUtils.removeAll(this.indexesToWrite, -1).length;
                    this.outputRow = new Object[size];
                    return;
                } else {
                    this.outputRow = new Object[this.headers.length];
                }
                return;
            } else {
                if (selector instanceof FieldNameSelector || selector instanceof ExcludeFieldNameSelector) throw new IllegalStateException("Cannot select fields by name with no headers defined");
                int rowLength = this.largestRowLength;
                if (selector instanceof FieldIndexSelector) {
                    boolean gotLengthFromSelection = false;
                    for (Integer index : ((FieldIndexSelector)selector).get()) {
                        if (rowLength > index) continue;
                        rowLength = index;
                        gotLengthFromSelection = true;
                    }
                    if (gotLengthFromSelection) {
                        ++rowLength;
                    }
                    if (rowLength < this.largestRowLength) {
                        rowLength = this.largestRowLength;
                    }
                } else {
                    rowLength = settings.getMaxColumns();
                }
                this.indexesToWrite = selector.getFieldIndexes(new NormalizedString[rowLength]);
                if (this.columnReorderingEnabled) {
                    int size = ArgumentUtils.removeAll(this.indexesToWrite, -1).length;
                    this.outputRow = new Object[size];
                    return;
                } else {
                    this.outputRow = new Object[rowLength];
                }
            }
            return;
        } else {
            this.outputRow = null;
            this.indexesToWrite = null;
        }
    }

    public void updateFieldSelection(String ... newFieldSelection) {
        if (this.headers == null) {
            throw new IllegalStateException("Cannot select fields by name. Headers not defined.");
        }
        this.internalSettings.selectFields(newFieldSelection);
        this.updateIndexesToWrite(this.internalSettings);
    }

    public void updateFieldSelection(Integer ... newFieldSelectionByIndex) {
        this.internalSettings.selectIndexes(newFieldSelectionByIndex);
        this.updateIndexesToWrite(this.internalSettings);
    }

    public void updateFieldExclusion(String ... fieldsToExclude) {
        if (this.headers == null) {
            throw new IllegalStateException("Cannot de-select fields by name. Headers not defined.");
        }
        this.internalSettings.excludeFields(fieldsToExclude);
        this.updateIndexesToWrite(this.internalSettings);
    }

    public void updateFieldExclusion(Integer ... fieldIndexesToExclude) {
        this.internalSettings.excludeIndexes(fieldIndexesToExclude);
        this.updateIndexesToWrite(this.internalSettings);
    }

    private void submitRow(Object[] row) {
        if (this.largestRowLength < row.length) {
            this.largestRowLength = row.length;
        }
        if (this.writingHeaders) {
            this.headerTrimFlags = new boolean[this.headers.length];
            for (int i = 0; i < this.headers.length; ++i) {
                this.headerTrimFlags[i] = !this.headers[i].isLiteral();
            }
        }
        this.processRow(row);
    }

    protected abstract void processRow(Object[] var1);

    protected final void appendValueToRow() {
        this.rowAppender.append(this.appender);
    }

    protected final void appendToRow(char ch) {
        this.rowAppender.append(ch);
    }

    protected final void appendToRow(char[] chars) {
        this.rowAppender.append(chars);
    }

    public final void writeHeaders() {
        this.writeHeaders(NormalizedString.toArray(this.headers));
    }

    public final void writeHeaders(Collection<?> headers) {
        if (headers == null || headers.size() <= 0) {
            throw this.throwExceptionAndClose("No headers defined.");
        }
        this.writeHeaders(headers.toArray(new String[headers.size()]));
    }

    public final void writeHeaders(String ... headers) {
        if (this.recordCount > 0L) {
            throw this.throwExceptionAndClose("Cannot write headers after records have been written.", headers, null);
        }
        if (headers != null && headers.length > 0) {
            this.writingHeaders = true;
            if (this.columnReorderingEnabled && this.outputRow != null) {
                this.fillOutputRow(headers);
                headers = (String[])Arrays.copyOf(this.outputRow, this.outputRow.length, String[].class);
            }
        } else {
            throw this.throwExceptionAndClose("No headers defined.", headers, null);
        }
        this.headers = NormalizedString.toIdentifierGroupArray(headers);
        this.submitRow(headers);
        this.internalWriteRow();
        this.writingHeaders = false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void processRecordsAndClose(Iterable<?> allRecords) {
        try {
            this.processRecords(allRecords);
        }
        finally {
            this.close();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void processRecordsAndClose(Object[] allRecords) {
        try {
            this.processRecords(allRecords);
        }
        finally {
            this.close();
        }
    }

    public final void processRecords(Iterable<?> records) {
        for (Object record : records) {
            this.processRecord(record);
        }
    }

    public final void processRecords(Object[] records) {
        for (Object record : records) {
            this.processRecord(record);
        }
    }

    public final <T extends Record> void processRecords(T[] records) {
        for (T record : records) {
            this.processRecord(record);
        }
    }

    public final void processRecord(Object ... record) {
        this.processRecord((Object)record);
    }

    public final <T extends Record> void processRecord(T record) {
        this.processRecord((Object)(record == null ? null : record.getValues()));
    }

    public final void processRecord(Object record) {
        Object[] row;
        if (this.writerProcessor == null) {
            String recordDescription = record instanceof Object[] ? Arrays.toString((Object[])record) : String.valueOf(record);
            String message = "Cannot process record '" + recordDescription + "' without a writer processor. Please define a writer processor instance in the settings or use the 'writeRow' methods.";
            this.throwExceptionAndClose(message);
        }
        try {
            if (this.usingSwitch) {
                this.dummyHeaderRow = ((RowWriterProcessorSwitch)this.writerProcessor).getHeaders(record);
                if (this.dummyHeaderRow == null) {
                    this.dummyHeaderRow = this.headers;
                }
                row = this.writerProcessor.write(record, this.dummyHeaderRow, this.indexesToWrite);
            } else {
                row = this.writerProcessor.write(record, this.getRowProcessorHeaders(), this.indexesToWrite);
            }
        }
        catch (DataProcessingException e) {
            e.setErrorContentLength(this.errorContentLength);
            throw e;
        }
        if (row != null) {
            this.writeRow(row);
        }
    }

    private NormalizedString[] getRowProcessorHeaders() {
        if (this.headers == null && this.indexesToWrite == null) {
            return null;
        }
        return this.headers;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final <C extends Collection<?>> void writeRowsAndClose(Iterable<C> allRows) {
        try {
            this.writeRows(allRows);
        }
        finally {
            this.close();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void writeRowsAndClose(Collection<Object[]> allRows) {
        try {
            this.writeRows(allRows);
        }
        finally {
            this.close();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void writeStringRowsAndClose(Collection<String[]> allRows) {
        try {
            this.writeStringRows(allRows);
        }
        finally {
            this.close();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void writeRecordsAndClose(Collection<? extends Record> allRows) {
        try {
            this.writeRecords(allRows);
        }
        finally {
            this.close();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void writeRowsAndClose(Object[][] allRows) {
        try {
            this.writeRows(allRows);
        }
        finally {
            this.close();
        }
    }

    public final void writeRows(Object[][] rows) {
        for (Object[] row : rows) {
            this.writeRow(row);
        }
    }

    public final <C extends Collection<?>> void writeRows(Iterable<C> rows) {
        for (Collection row : rows) {
            this.writeRow(row);
        }
    }

    public final void writeStringRows(Collection<String[]> rows) {
        for (String[] row : rows) {
            this.writeRow(row);
        }
    }

    public final void writeRecords(Collection<? extends Record> rows) {
        for (Record record : rows) {
            this.writeRecord(record);
        }
    }

    public final <C extends Collection<?>> void writeStringRows(Iterable<C> rows) {
        for (Collection row : rows) {
            this.writeRow(row.toArray());
        }
    }

    public final void writeRows(Collection<Object[]> rows) {
        for (Object[] row : rows) {
            this.writeRow(row);
        }
    }

    public final void writeRow(Collection<?> row) {
        if (row == null) {
            return;
        }
        this.writeRow(row.toArray());
    }

    public final void writeRow(String[] row) {
        this.writeRow((Object[])row);
    }

    public final <T extends Record> void writeRecord(T row) {
        String[] headers;
        if (row == null) {
            if (this.skipEmptyLines) {
                return;
            }
            this.writeEmptyRow();
            return;
        }
        if (this.recordCount == 0L && this.isHeaderWritingEnabled && this.headers == null && (headers = row.getMetaData().headers()) != null) {
            this.headers = NormalizedString.toArray(headers);
        }
        this.writeRow((Object[])row.getValues());
    }

    public final void writeRow(Object ... row) {
        try {
            if (this.recordCount == 0L && this.isHeaderWritingEnabled && this.headers != null) {
                this.writeHeaders();
            }
            if (row == null || row.length == 0 && !this.expandRows) {
                if (this.skipEmptyLines) {
                    return;
                }
                this.writeEmptyRow();
                return;
            }
            row = this.adjustRowLength(row);
            this.submitRow(row);
            this.internalWriteRow();
        }
        catch (Throwable ex) {
            throw this.throwExceptionAndClose("Error writing row.", row, ex);
        }
    }

    protected Object[] expand(Object[] row, int length, Integer h2) {
        if (row.length < length) {
            return Arrays.copyOf(row, length);
        }
        if (h2 != null && row.length < h2) {
            return Arrays.copyOf(row, (int)h2);
        }
        if (length == -1 && h2 == null && row.length < this.largestRowLength) {
            return Arrays.copyOf(row, this.largestRowLength);
        }
        return row;
    }

    public final void writeRow(String row) {
        try {
            this.writer.write(row);
            if (this.enableNewlineAfterRecord) {
                this.writer.write(this.lineSeparator);
            }
        }
        catch (Throwable ex) {
            throw this.throwExceptionAndClose("Error writing row.", row, ex);
        }
    }

    public final void writeEmptyRow() {
        try {
            if (this.enableNewlineAfterRecord) {
                this.writer.write(this.lineSeparator);
            }
        }
        catch (Throwable ex) {
            throw this.throwExceptionAndClose("Error writing empty row.", Arrays.toString(this.lineSeparator), ex);
        }
    }

    public final void commentRow(String comment) {
        this.writeRow(this.comment + comment);
    }

    private <T> void fillOutputRow(T[] row) {
        if (!this.columnReorderingEnabled && row.length > this.outputRow.length) {
            this.outputRow = Arrays.copyOf(this.outputRow, row.length);
        }
        if (this.indexesToWrite.length < row.length) {
            if (this.columnReorderingEnabled) {
                for (int i = 0; i < this.indexesToWrite.length; ++i) {
                    this.outputRow[i] = row[this.indexesToWrite[i]];
                }
            } else {
                for (int i = 0; i < this.indexesToWrite.length; ++i) {
                    this.outputRow[this.indexesToWrite[i]] = row[this.indexesToWrite[i]];
                }
            }
        } else {
            for (int i = 0; i < row.length && i < this.indexesToWrite.length; ++i) {
                if (this.indexesToWrite[i] == -1) continue;
                this.outputRow[this.indexesToWrite[i]] = row[i];
            }
        }
    }

    private void internalWriteRow() {
        try {
            if (this.skipEmptyLines && this.rowAppender.length() == 0) {
                return;
            }
            if (this.enableNewlineAfterRecord) {
                this.rowAppender.appendNewLine();
            }
            this.rowAppender.writeCharsAndReset(this.writer);
            ++this.recordCount;
        }
        catch (Throwable ex) {
            throw this.throwExceptionAndClose("Error writing row.", this.rowAppender.getAndReset(), ex);
        }
    }

    protected static int skipLeadingWhitespace(int whitespaceRangeStart, String element) {
        if (element.isEmpty()) {
            return 0;
        }
        for (int i = 0; i < element.length(); ++i) {
            char nextChar = element.charAt(i);
            if (nextChar <= ' ' && whitespaceRangeStart < nextChar) continue;
            return i;
        }
        return element.length();
    }

    public final void flush() {
        try {
            this.writer.flush();
        }
        catch (Throwable ex) {
            throw this.throwExceptionAndClose("Error flushing output.", this.rowAppender.getAndReset(), ex);
        }
    }

    public final void close() {
        try {
            this.headerIndexes = null;
            if (this.writer != null) {
                this.writer.close();
                this.writer = null;
            }
        }
        catch (Throwable ex) {
            throw new IllegalStateException("Error closing the output.", ex);
        }
        if (this.partialLineIndex != 0) {
            throw new TextWritingException("Not all values associated with the last record have been written to the output. \n\tHint: use 'writeValuesToRow()' or 'writeValuesToString()' to flush the partially written values to a row.", this.recordCount, this.getContent(Arrays.copyOf(this.partialLine, this.partialLineIndex)));
        }
    }

    private TextWritingException throwExceptionAndClose(String message) {
        return this.throwExceptionAndClose(message, (Object[])null, null);
    }

    private TextWritingException throwExceptionAndClose(String message, Throwable cause) {
        return this.throwExceptionAndClose(message, (Object[])null, cause);
    }

    private TextWritingException throwExceptionAndClose(String message, String recordCharacters, Throwable cause) {
        try {
            if (cause instanceof NullPointerException && this.writer == null) {
                message = message + " No writer provided in the constructor of " + this.getClass().getName() + ". You can only use operations that write to Strings.";
            }
            throw new TextWritingException(message, this.recordCount, this.getContent(recordCharacters), cause);
        }
        catch (Throwable throwable) {
            this.close();
            throw throwable;
        }
    }

    private TextWritingException throwExceptionAndClose(String message, Object[] recordValues, Throwable cause) {
        try {
            throw new TextWritingException(message, this.recordCount, this.getContent(recordValues), cause);
        }
        catch (Throwable throwable) {
            try {
                this.close();
            }
            catch (Throwable t) {
                // empty catch block
            }
            throw throwable;
        }
    }

    protected String getStringValue(Object element) {
        int start;
        this.usingNullOrEmptyValue = false;
        if (element == null) {
            this.usingNullOrEmptyValue = true;
            return this.nullValue;
        }
        String string = String.valueOf(element);
        if (string.isEmpty()) {
            this.usingNullOrEmptyValue = true;
            return this.emptyValue;
        }
        if (this.ignoreLeading && (start = AbstractWriter.skipLeadingWhitespace(this.whitespaceRangeStart, string)) == string.length()) {
            this.usingNullOrEmptyValue = true;
            return this.emptyValue;
        }
        return string;
    }

    public final void addValues(Object ... values) {
        try {
            System.arraycopy(values, 0, this.partialLine, this.partialLineIndex, values.length);
            this.partialLineIndex += values.length;
        }
        catch (Throwable t) {
            throw this.throwExceptionAndClose("Error adding values to in-memory row", values, t);
        }
    }

    public final void addStringValues(Collection<String> values) {
        if (values != null) {
            try {
                for (String o : values) {
                    this.partialLine[this.partialLineIndex++] = o;
                }
            }
            catch (Throwable t) {
                throw this.throwExceptionAndClose("Error adding values to in-memory row", values.toArray(), t);
            }
        }
    }

    public final void addValues(Collection<?> values) {
        if (values != null) {
            try {
                for (Object o : values) {
                    this.partialLine[this.partialLineIndex++] = o;
                }
            }
            catch (Throwable t) {
                throw this.throwExceptionAndClose("Error adding values to in-memory row", values.toArray(), t);
            }
        }
    }

    public final void addValue(Object value) {
        try {
            this.partialLine[this.partialLineIndex++] = value;
        }
        catch (Throwable t) {
            throw this.throwExceptionAndClose("Error adding value to in-memory row", new Object[]{value}, t);
        }
    }

    private void fillPartialLineToMatchHeaders() {
        if (this.headers != null && this.partialLineIndex < this.headers.length) {
            while (this.partialLineIndex < this.headers.length) {
                this.partialLine[this.partialLineIndex++] = null;
            }
        }
    }

    public final void writeValuesToRow() {
        this.fillPartialLineToMatchHeaders();
        this.writeRow(Arrays.copyOf(this.partialLine, this.partialLineIndex));
        this.discardValues();
    }

    public final void addValue(int index, Object value) {
        if (index >= this.partialLine.length) {
            throw this.throwExceptionAndClose("Cannot write '" + value + "' to index '" + index + "'. Maximum number of columns (" + this.partialLine.length + ") exceeded.", new Object[]{value}, null);
        }
        this.partialLine[index] = value;
        if (this.partialLineIndex <= index) {
            this.partialLineIndex = index + 1;
        }
    }

    public final void addValue(String headerName, Object value) {
        this.addValue(this.getFieldIndex(this.headers, NormalizedString.valueOf(headerName), false), value);
    }

    private final void addValue(NormalizedString[] headersInContext, NormalizedString headerName, boolean ignoreOnMismatch, Object value) {
        int index = this.getFieldIndex(headersInContext, headerName, ignoreOnMismatch);
        if (index != -1) {
            this.addValue(index, value);
        }
    }

    private int getFieldIndex(NormalizedString[] headersInContext, NormalizedString headerName, boolean ignoreOnMismatch) {
        Integer index;
        Map<NormalizedString, Integer> indexes;
        if (this.headerIndexes == null) {
            this.headerIndexes = new HashMap<NormalizedString[], Map<NormalizedString, Integer>>();
        }
        if ((indexes = this.headerIndexes.get(headersInContext)) == null) {
            indexes = new HashMap<NormalizedString, Integer>();
            this.headerIndexes.put(headersInContext, indexes);
        }
        if ((index = indexes.get(headerName)) == null) {
            if (headersInContext == null) {
                throw this.throwExceptionAndClose("Cannot calculate position of header '" + headerName + "' as no headers were defined.", null);
            }
            index = ArgumentUtils.indexOf(NormalizedString.toArray(headersInContext), NormalizedString.valueOf(headerName));
            if (index == -1 && !ignoreOnMismatch) {
                throw this.throwExceptionAndClose("Header '" + headerName + "' could not be found. Defined headers are: " + Arrays.toString(headersInContext) + '.', null);
            }
            indexes.put(headerName, index);
        }
        return index;
    }

    public final void discardValues() {
        Arrays.fill(this.partialLine, 0, this.partialLineIndex, null);
        this.partialLineIndex = 0;
    }

    public final String writeHeadersToString() {
        return this.writeHeadersToString(NormalizedString.toArray(this.headers));
    }

    public final String writeHeadersToString(Collection<?> headers) {
        if (headers != null && headers.size() > 0) {
            return this.writeHeadersToString(headers.toArray(new String[headers.size()]));
        }
        throw this.throwExceptionAndClose("No headers defined");
    }

    public final String writeHeadersToString(String ... headers) {
        if (headers != null && headers.length > 0) {
            this.writingHeaders = true;
            this.submitRow(headers);
            this.writingHeaders = false;
            this.headers = NormalizedString.toIdentifierGroupArray(headers);
            return this.internalWriteRowToString();
        }
        throw this.throwExceptionAndClose("No headers defined.");
    }

    public final List<String> processRecordsToString(Iterable<?> records) {
        try {
            ArrayList<String> out = new ArrayList<String>(1000);
            for (Object record : records) {
                out.add(this.processRecordToString(record));
            }
            return out;
        }
        catch (Throwable t) {
            throw this.throwExceptionAndClose("Unable process input records", t);
        }
    }

    public final List<String> processRecordsToString(Object[] records) {
        try {
            ArrayList<String> out = new ArrayList<String>(1000);
            for (Object record : records) {
                out.add(this.processRecordToString(record));
            }
            return out;
        }
        catch (Throwable t) {
            throw this.throwExceptionAndClose("Unable process input records", records, t);
        }
    }

    public final String processRecordToString(Object ... record) {
        return this.processRecordToString((Object)record);
    }

    public final <T extends Record> String processRecordToString(T record) {
        return this.processRecordToString((Object)(record == null ? null : record.getValues()));
    }

    public final String processRecordToString(Object record) {
        if (this.writerProcessor == null) {
            throw this.throwExceptionAndClose("Cannot process record '" + record + "' without a writer processor. Please define a writer processor instance in the settings or use the 'writeRow' methods.");
        }
        try {
            Object[] row = this.writerProcessor.write(record, this.getRowProcessorHeaders(), this.indexesToWrite);
            if (row != null) {
                return this.writeRowToString(row);
            }
        }
        catch (Throwable t) {
            throw this.throwExceptionAndClose("Could not process record '" + record + "'", t);
        }
        return null;
    }

    public final List<String> writeRowsToString(Object[][] rows) {
        try {
            ArrayList<String> out = new ArrayList<String>(rows.length);
            for (Object[] row : rows) {
                String string = this.writeRowToString(row);
                if (string == null) continue;
                out.add(string);
            }
            return out;
        }
        catch (Throwable t) {
            throw this.throwExceptionAndClose("Error writing input rows", t);
        }
    }

    public final <C extends Collection<?>> List<String> writeRowsToString(Iterable<C> rows) {
        try {
            ArrayList<String> out = new ArrayList<String>(1000);
            for (Collection row : rows) {
                out.add(this.writeRowToString(row));
            }
            return out;
        }
        catch (Throwable t) {
            throw this.throwExceptionAndClose("Error writing input rows", t);
        }
    }

    public final <C extends Collection<?>> List<String> writeStringRowsToString(Iterable<C> rows) {
        try {
            ArrayList<String> out = new ArrayList<String>(1000);
            for (Collection row : rows) {
                String string = this.writeRowToString(row);
                if (string == null) continue;
                out.add(string);
            }
            return out;
        }
        catch (Throwable t) {
            throw this.throwExceptionAndClose("Error writing input rows", t);
        }
    }

    public final List<String> writeRowsToString(Collection<Object[]> rows) {
        try {
            ArrayList<String> out = new ArrayList<String>(rows.size());
            for (Object[] row : rows) {
                out.add(this.writeRowToString(row));
            }
            return out;
        }
        catch (Throwable t) {
            throw this.throwExceptionAndClose("Error writing input rows", t);
        }
    }

    public final List<String> writeStringRowsToString(Collection<String[]> rows) {
        try {
            ArrayList<String> out = new ArrayList<String>(rows.size());
            for (String[] row : rows) {
                out.add(this.writeRowToString(row));
            }
            return out;
        }
        catch (Throwable t) {
            throw this.throwExceptionAndClose("Error writing input rows", t);
        }
    }

    public final List<String> writeRecordsToString(Collection<? extends Record> rows) {
        try {
            ArrayList<String> out = new ArrayList<String>(rows.size());
            for (Record record : rows) {
                out.add(this.writeRecordToString(record));
            }
            return out;
        }
        catch (Throwable t) {
            throw this.throwExceptionAndClose("Error writing input rows", t);
        }
    }

    public final String writeRowToString(Collection<?> row) {
        try {
            if (row == null) {
                return null;
            }
            return this.writeRowToString(row.toArray());
        }
        catch (Throwable t) {
            throw this.throwExceptionAndClose("Error writing input row ", t);
        }
    }

    public final String writeRowToString(String[] row) {
        return this.writeRowToString((Object[])row);
    }

    public final <T extends Record> String writeRecordToString(T row) {
        return this.writeRowToString((Object[])(row == null ? null : row.getValues()));
    }

    public final String writeRowToString(Object ... row) {
        try {
            if ((row == null || row.length == 0 && !this.expandRows) && this.skipEmptyLines) {
                return null;
            }
            row = this.adjustRowLength(row);
            this.submitRow(row);
            return this.internalWriteRowToString();
        }
        catch (Throwable ex) {
            throw this.throwExceptionAndClose("Error writing row.", row, ex);
        }
    }

    private Object[] adjustRowLength(Object[] row) {
        if (this.outputRow != null) {
            this.fillOutputRow(row);
            row = this.outputRow;
        } else if (this.expandRows) {
            if (this.usingSwitch) {
                row = this.expand(row, this.dummyHeaderRow == null ? -1 : this.dummyHeaderRow.length, this.headers == null ? null : Integer.valueOf(this.headers.length));
                this.dummyHeaderRow = null;
            } else {
                row = this.expand(row, this.headers == null ? -1 : this.headers.length, null);
            }
        }
        return row;
    }

    public final String commentRowToString(String comment) {
        return this.writeRowToString(this.comment + comment);
    }

    private String internalWriteRowToString() {
        if (this.skipEmptyLines && this.rowAppender.length() == 0) {
            return null;
        }
        String out = this.rowAppender.getAndReset();
        ++this.recordCount;
        return out;
    }

    public final String writeValuesToString() {
        this.fillPartialLineToMatchHeaders();
        String out = this.writeRowToString(Arrays.copyOf(this.partialLine, this.partialLineIndex));
        this.discardValues();
        return out;
    }

    public final void processValuesToRow() {
        this.fillPartialLineToMatchHeaders();
        this.processRecord(Arrays.copyOf(this.partialLine, this.partialLineIndex));
        this.discardValues();
    }

    public final String processValuesToString() {
        this.fillPartialLineToMatchHeaders();
        String out = this.processRecordToString(Arrays.copyOf(this.partialLine, this.partialLineIndex));
        this.discardValues();
        return out;
    }

    public final long getRecordCount() {
        return this.recordCount;
    }

    private <K> void writeValuesFromMap(Map<K, String> headerMapping, Map<K, ?> rowData) {
        try {
            if (rowData != null && !rowData.isEmpty()) {
                this.dummyHeaderRow = this.headers;
                if (this.usingSwitch) {
                    this.dummyHeaderRow = ((RowWriterProcessorSwitch)this.writerProcessor).getHeaders(headerMapping, rowData);
                    if (this.dummyHeaderRow == null) {
                        this.dummyHeaderRow = this.headers;
                    }
                }
                if (this.dummyHeaderRow != null) {
                    if (headerMapping == null) {
                        for (Map.Entry<K, ?> e : rowData.entrySet()) {
                            this.addValue(this.dummyHeaderRow, NormalizedString.valueOf(e.getKey()), true, e.getValue());
                        }
                    } else {
                        for (Map.Entry<K, ?> e : rowData.entrySet()) {
                            String header = headerMapping.get(e.getKey());
                            if (header == null) continue;
                            this.addValue(this.dummyHeaderRow, NormalizedString.valueOf(header), true, e.getValue());
                        }
                    }
                } else if (headerMapping != null) {
                    this.setHeadersFromMap(headerMapping, false);
                    this.writeValuesFromMap(headerMapping, rowData);
                } else {
                    this.setHeadersFromMap(rowData, true);
                    this.writeValuesFromMap(null, rowData);
                }
            }
        }
        catch (Throwable t) {
            throw this.throwExceptionAndClose("Error processing data from input map", t);
        }
    }

    private void setHeadersFromMap(Map<?, ?> map, boolean keys) {
        this.headers = new NormalizedString[map.size()];
        int i = 0;
        for (Object header : keys ? map.keySet() : map.values()) {
            this.headers[i++] = NormalizedString.valueOf(header);
        }
    }

    public final String writeRowToString(Map<?, ?> rowData) {
        return this.writeRowToString(null, rowData);
    }

    public final void writeRow(Map<?, ?> rowData) {
        this.writeRow(null, rowData);
    }

    public final <K> String writeRowToString(Map<K, String> headerMapping, Map<K, ?> rowData) {
        this.writeValuesFromMap(headerMapping, rowData);
        return this.writeValuesToString();
    }

    public final <K> void writeRow(Map<K, String> headerMapping, Map<K, ?> rowData) {
        this.writeValuesFromMap(headerMapping, rowData);
        this.writeValuesToRow();
    }

    public final <K, I extends Iterable<?>> List<String> writeRowsToString(Map<K, I> rowData) {
        return this.writeRowsToString(null, rowData);
    }

    public final <K, I extends Iterable<?>> void writeRows(Map<K, I> rowData) {
        this.writeRows(null, rowData, null, false);
    }

    public final <K, I extends Iterable<?>> List<String> writeRowsToString(Map<K, String> headerMapping, Map<K, I> rowData) {
        ArrayList<String> writtenRows = new ArrayList<String>();
        this.writeRows(headerMapping, rowData, writtenRows, false);
        return writtenRows;
    }

    public final <K, I extends Iterable<?>> void writeRows(Map<K, String> headerMapping, Map<K, I> rowData) {
        this.writeRows(headerMapping, rowData, null, false);
    }

    private <K, I extends Iterable<?>> void writeRows(Map<K, String> headerMapping, Map<K, I> rowData, List<String> outputList, boolean useRowProcessor) {
        try {
            boolean nullsOnly;
            Iterator[] iterators = new Iterator[rowData.size()];
            Object[] keys = new Object[rowData.size()];
            LinkedHashMap rowValues = new LinkedHashMap(rowData.size());
            if (outputList != null && this.headers == null) {
                if (headerMapping != null) {
                    this.setHeadersFromMap(headerMapping, true);
                } else {
                    this.setHeadersFromMap(rowData, true);
                }
            }
            if (this.recordCount == 0L && this.headers != null && this.isHeaderWritingEnabled) {
                outputList.add(this.writeHeadersToString());
            }
            int length = 0;
            for (Map.Entry<K, I> rowEntry : rowData.entrySet()) {
                iterators[length] = rowEntry.getValue() == null ? null : ((Iterable)rowEntry.getValue()).iterator();
                keys[length] = rowEntry.getKey();
                rowValues.put(rowEntry.getKey(), null);
                ++length;
            }
            do {
                nullsOnly = true;
                for (int i = 0; i < length; ++i) {
                    Iterator iterator = iterators[i];
                    boolean isNull = iterator == null || !iterator.hasNext();
                    nullsOnly &= isNull;
                    if (isNull) {
                        rowValues.put(keys[i], null);
                        continue;
                    }
                    rowValues.put(keys[i], iterator.next());
                }
                if (nullsOnly) continue;
                if (outputList == null) {
                    if (useRowProcessor) {
                        this.processRecord(headerMapping, rowValues);
                        continue;
                    }
                    this.writeRow(headerMapping, rowValues);
                    continue;
                }
                if (useRowProcessor) {
                    outputList.add(this.processRecordToString(headerMapping, rowValues));
                    continue;
                }
                outputList.add(this.writeRowToString(headerMapping, rowValues));
            } while (!nullsOnly);
        }
        catch (Throwable t) {
            throw this.throwExceptionAndClose("Error processing input rows from map", t);
        }
    }

    public final <K> List<String> writeStringRowsToString(Map<K, String> headerMapping, Map<K, String[]> rowData) {
        ArrayList<String> writtenRows = new ArrayList<String>();
        this.writeRows(headerMapping, this.wrapStringArray(rowData), writtenRows, false);
        return writtenRows;
    }

    public final <K> List<String> writeRecordsToString(Map<K, String> headerMapping, Map<K, ? extends Record> rowData) {
        ArrayList<String> writtenRows = new ArrayList<String>();
        this.writeRows(headerMapping, this.wrapRecordValues(rowData), writtenRows, false);
        return writtenRows;
    }

    public final <K> void writeStringRows(Map<K, String> headerMapping, Map<K, String[]> rowData) {
        this.writeRows(headerMapping, this.wrapStringArray(rowData), null, false);
    }

    public final <K> void writeRecords(Map<K, String> headerMapping, Map<K, ? extends Record> rowData) {
        this.writeRows(headerMapping, this.wrapRecordValues(rowData), null, false);
    }

    public final <K> List<String> writeObjectRowsToString(Map<K, String> headerMapping, Map<K, Object[]> rowData) {
        ArrayList<String> writtenRows = new ArrayList<String>();
        this.writeRows(headerMapping, this.wrapObjectArray(rowData), writtenRows, false);
        return writtenRows;
    }

    public final <K> void writeObjectRows(Map<K, String> headerMapping, Map<K, Object[]> rowData) {
        this.writeRows(headerMapping, this.wrapObjectArray(rowData), null, false);
    }

    private <K> Map<K, Iterable<Object>> wrapObjectArray(Map<K, Object[]> rowData) {
        LinkedHashMap<K, List<Object>> out = new LinkedHashMap<K, List<Object>>(rowData.size());
        for (Map.Entry<K, Object[]> e : rowData.entrySet()) {
            if (e.getValue() == null) {
                out.put(e.getKey(), Collections.emptyList());
                continue;
            }
            out.put(e.getKey(), Arrays.asList(e.getValue()));
        }
        return out;
    }

    private <K> Map<K, Iterable<String>> wrapStringArray(Map<K, String[]> rowData) {
        LinkedHashMap<K, List<Object>> out = new LinkedHashMap<K, List<Object>>(rowData.size());
        for (Map.Entry<K, String[]> e : rowData.entrySet()) {
            if (e.getValue() == null) {
                out.put(e.getKey(), Collections.emptyList());
                continue;
            }
            out.put(e.getKey(), Arrays.asList((Object[])e.getValue()));
        }
        return out;
    }

    private <K> Map<K, Iterable<String>> wrapRecordValues(Map<K, ? extends Record> rowData) {
        LinkedHashMap<K, List<Object>> out = new LinkedHashMap<K, List<Object>>(rowData.size());
        for (Map.Entry<K, Record> e : rowData.entrySet()) {
            if (e.getValue() == null) {
                out.put(e.getKey(), Collections.emptyList());
                continue;
            }
            out.put(e.getKey(), Arrays.asList(e.getValue().getValues()));
        }
        return out;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final <K> void writeObjectRowsAndClose(Map<K, String> headerMapping, Map<K, Object[]> rowData) {
        try {
            this.writeObjectRows(headerMapping, rowData);
        }
        finally {
            this.close();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final <K> void writeStringRowsAndClose(Map<K, String> headerMapping, Map<K, String[]> rowData) {
        try {
            this.writeStringRows(headerMapping, rowData);
        }
        finally {
            this.close();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final <K> void writeRecordsAndClose(Map<K, String> headerMapping, Map<K, ? extends Record> rowData) {
        try {
            this.writeRecords(headerMapping, rowData);
        }
        finally {
            this.close();
        }
    }

    public final <K> void writeObjectRowsAndClose(Map<K, Object[]> rowData) {
        this.writeObjectRowsAndClose(null, rowData);
    }

    public final <K> void writeStringRowsAndClose(Map<K, String[]> rowData) {
        this.writeStringRowsAndClose(null, rowData);
    }

    public final <K> void writeRecordsAndClose(Map<K, ? extends Record> rowData) {
        this.writeRecordsAndClose(null, rowData);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final <K, I extends Iterable<?>> void writeRowsAndClose(Map<K, String> headerMapping, Map<K, I> rowData) {
        try {
            this.writeRows(headerMapping, rowData);
        }
        finally {
            this.close();
        }
    }

    public final <K, I extends Iterable<?>> void writeRowsAndClose(Map<K, I> rowData) {
        this.writeRowsAndClose(null, rowData);
    }

    public final String processRecordToString(Map<?, ?> rowData) {
        return this.processRecordToString(null, rowData);
    }

    public final void processRecord(Map<?, ?> rowData) {
        this.processRecord(null, rowData);
    }

    public final <K> String processRecordToString(Map<K, String> headerMapping, Map<K, ?> rowData) {
        this.writeValuesFromMap(headerMapping, rowData);
        return this.processValuesToString();
    }

    public final <K> void processRecord(Map<K, String> headerMapping, Map<K, ?> rowData) {
        this.writeValuesFromMap(headerMapping, rowData);
        this.processValuesToRow();
    }

    public final <K, I extends Iterable<?>> List<String> processRecordsToString(Map<K, I> rowData) {
        return this.processRecordsToString(null, rowData);
    }

    public final <K, I extends Iterable<?>> void processRecords(Map<K, I> rowData) {
        this.writeRows(null, rowData, null, true);
    }

    public final <K, I extends Iterable<?>> List<String> processRecordsToString(Map<K, String> headerMapping, Map<K, I> rowData) {
        ArrayList<String> writtenRows = new ArrayList<String>();
        this.writeRows(headerMapping, rowData, writtenRows, true);
        return writtenRows;
    }

    public final <K, I extends Iterable<?>> void processRecords(Map<K, String> headerMapping, Map<K, I> rowData) {
        this.writeRows(headerMapping, rowData, null, true);
    }

    public final <K> List<String> processObjectRecordsToString(Map<K, Object[]> rowData) {
        return this.processObjectRecordsToString(null, rowData);
    }

    public final <K> List<String> processObjectRecordsToString(Map<K, String> headerMapping, Map<K, Object[]> rowData) {
        ArrayList<String> writtenRows = new ArrayList<String>();
        this.writeRows(headerMapping, this.wrapObjectArray(rowData), writtenRows, true);
        return writtenRows;
    }

    public final <K> void processObjectRecords(Map<K, String> headerMapping, Map<K, Object[]> rowData) {
        this.writeRows(headerMapping, this.wrapObjectArray(rowData), null, true);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final <K> void processObjectRecordsAndClose(Map<K, String> headerMapping, Map<K, Object[]> rowData) {
        try {
            this.processObjectRecords(headerMapping, rowData);
        }
        finally {
            this.close();
        }
    }

    public final <K> void processObjectRecordsAndClose(Map<K, Object[]> rowData) {
        this.processRecordsAndClose(null, this.wrapObjectArray(rowData));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final <K, I extends Iterable<?>> void processRecordsAndClose(Map<K, String> headerMapping, Map<K, I> rowData) {
        try {
            this.processRecords(headerMapping, rowData);
        }
        finally {
            this.close();
        }
    }

    public final <K, I extends Iterable<?>> void processRecordsAndClose(Map<K, I> rowData) {
        this.processRecordsAndClose(null, rowData);
    }

    private Object[] getContent(Object[] tmp) {
        return AbstractException.restrictContent(this.errorContentLength, tmp);
    }

    private String getContent(CharSequence tmp) {
        return AbstractException.restrictContent(this.errorContentLength, tmp);
    }

    protected final boolean allowTrim(int fieldIndex) {
        return !this.writingHeaders || fieldIndex >= this.headerTrimFlags.length || this.headerTrimFlags[fieldIndex];
    }
}

