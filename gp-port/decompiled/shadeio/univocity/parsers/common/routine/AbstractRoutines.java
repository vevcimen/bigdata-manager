/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.routine;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.List;
import shadeio.univocity.parsers.common.AbstractParser;
import shadeio.univocity.parsers.common.AbstractWriter;
import shadeio.univocity.parsers.common.ArgumentUtils;
import shadeio.univocity.parsers.common.CommonParserSettings;
import shadeio.univocity.parsers.common.CommonSettings;
import shadeio.univocity.parsers.common.CommonWriterSettings;
import shadeio.univocity.parsers.common.IterableResult;
import shadeio.univocity.parsers.common.ParsingContext;
import shadeio.univocity.parsers.common.ResultIterator;
import shadeio.univocity.parsers.common.TextWritingException;
import shadeio.univocity.parsers.common.fields.ColumnMapper;
import shadeio.univocity.parsers.common.fields.ColumnMapping;
import shadeio.univocity.parsers.common.processor.AbstractRowProcessor;
import shadeio.univocity.parsers.common.processor.BeanListProcessor;
import shadeio.univocity.parsers.common.processor.BeanProcessor;
import shadeio.univocity.parsers.common.processor.BeanWriterProcessor;
import shadeio.univocity.parsers.common.processor.RowProcessor;
import shadeio.univocity.parsers.common.processor.RowWriterProcessor;
import shadeio.univocity.parsers.common.routine.InputDimension;

public abstract class AbstractRoutines<P extends CommonParserSettings<?>, W extends CommonWriterSettings<?>> {
    private boolean keepResourcesOpen = false;
    private Writer previousOutput;
    private ColumnMapping columnMapper = new ColumnMapping();
    private final String routineDescription;
    private P parserSettings;
    private W writerSettings;

    protected abstract AbstractParser<P> createParser(P var1);

    protected abstract AbstractWriter<W> createWriter(Writer var1, W var2);

    protected abstract P createDefaultParserSettings();

    protected abstract W createDefaultWriterSettings();

    public AbstractRoutines(String routineDescription) {
        this(routineDescription, null, null);
    }

    public AbstractRoutines(String routineDescription, P parserSettings) {
        this(routineDescription, parserSettings, null);
    }

    public AbstractRoutines(String routineDescription, W writerSettings) {
        this(routineDescription, null, writerSettings);
    }

    public AbstractRoutines(String routineDescription, P parserSettings, W writerSettings) {
        this.routineDescription = routineDescription;
        this.parserSettings = parserSettings;
        this.writerSettings = writerSettings;
    }

    private void validateWriterSettings() {
        if (this.writerSettings == null) {
            this.writerSettings = this.createDefaultWriterSettings();
        }
    }

    private void validateParserSettings() {
        if (this.parserSettings == null) {
            this.parserSettings = this.createDefaultParserSettings();
            ((CommonParserSettings)this.parserSettings).setLineSeparatorDetectionEnabled(true);
        }
    }

    public final P getParserSettings() {
        this.validateParserSettings();
        return this.parserSettings;
    }

    public final void setParserSettings(P parserSettings) {
        this.parserSettings = parserSettings;
    }

    public final W getWriterSettings() {
        this.validateWriterSettings();
        return this.writerSettings;
    }

    public final void setWriterSettings(W writerSettings) {
        this.writerSettings = writerSettings;
    }

    protected void adjustColumnLengths(String[] headers, int[] lengths) {
    }

    public final void write(ResultSet rs, File output) {
        this.write(rs, output, (Charset)null);
    }

    public final void write(ResultSet rs, File output, String encoding) {
        this.write(rs, output, Charset.forName(encoding));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void write(ResultSet rs, File output, Charset encoding) {
        Writer writer = ArgumentUtils.newWriter(output, encoding);
        try {
            this.write(rs, writer);
        }
        finally {
            try {
                writer.close();
            }
            catch (Exception e) {
                throw new IllegalStateException("Error closing file: '" + output.getAbsolutePath() + "'", e);
            }
        }
    }

    public final void write(ResultSet rs, OutputStream output) {
        this.write(rs, ArgumentUtils.newWriter(output));
    }

    public final void write(ResultSet rs, OutputStream output, String encoding) {
        this.write(rs, ArgumentUtils.newWriter(output, encoding));
    }

    public final void write(ResultSet rs, OutputStream output, Charset encoding) {
        this.write(rs, ArgumentUtils.newWriter(output, encoding));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void write(ResultSet rs, Writer output) {
        this.validateWriterSettings();
        boolean hasWriterProcessor = ((CommonWriterSettings)this.writerSettings).getRowWriterProcessor() != null;
        AbstractWriter<W> writer = null;
        long rowCount = 0L;
        Object[] row = null;
        try {
            try {
                ResultSetMetaData md = rs.getMetaData();
                int columns = md.getColumnCount();
                String[] headers = new String[columns];
                int[] lengths = new int[columns];
                for (int i = 1; i <= columns; ++i) {
                    headers[i - 1] = md.getColumnLabel(i);
                    int precision = md.getPrecision(i);
                    int scale = md.getScale(i);
                    int length = precision != 0 && scale != 0 ? precision + scale + 2 : precision + scale;
                    lengths[i - 1] = length;
                }
                String[] userProvidedHeaders = ((CommonSettings)this.writerSettings).getHeaders();
                if (userProvidedHeaders == null) {
                    ((CommonSettings)this.writerSettings).setHeaders(headers);
                } else {
                    headers = userProvidedHeaders;
                }
                this.adjustColumnLengths(headers, lengths);
                writer = this.createWriter(output, this.writerSettings);
                if (((CommonWriterSettings)this.writerSettings).isHeaderWritingEnabled()) {
                    writer.writeHeaders();
                }
                row = new Object[columns];
                while (rs.next()) {
                    for (int i = 1; i <= columns; ++i) {
                        row[i - 1] = rs.getObject(i);
                    }
                    if (hasWriterProcessor) {
                        writer.processRecord(row);
                    } else {
                        writer.writeRow(row);
                    }
                    ++rowCount;
                }
            }
            finally {
                if (!this.keepResourcesOpen) {
                    rs.close();
                }
            }
        }
        catch (Exception e) {
            throw new TextWritingException("Error writing data from result set", rowCount, row, (Throwable)e);
        }
        finally {
            this.close(writer);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void parseAndWrite(Reader input, Writer output) {
        this.setRowWriterProcessor(null);
        this.setRowProcessor(this.createWritingRowProcessor(output));
        try {
            AbstractParser<P> parser = this.createParser(this.parserSettings);
            parser.parse(input);
        }
        finally {
            ((CommonParserSettings)this.parserSettings).setRowProcessor(null);
        }
    }

    private void setRowWriterProcessor(RowWriterProcessor rowWriterProcessor) {
        this.validateWriterSettings();
        ((CommonWriterSettings)this.writerSettings).setRowWriterProcessor(rowWriterProcessor);
    }

    private void setRowProcessor(RowProcessor rowProcessor) {
        this.validateParserSettings();
        ((CommonParserSettings)this.parserSettings).setRowProcessor(rowProcessor);
    }

    private RowProcessor createWritingRowProcessor(final Writer output) {
        return new RowProcessor(){
            private AbstractWriter<W> writer;

            @Override
            public void processStarted(ParsingContext context) {
                this.writer = AbstractRoutines.this.createWriter(output, AbstractRoutines.this.writerSettings);
            }

            @Override
            public void rowProcessed(String[] row, ParsingContext context) {
                this.writer.writeRow(row);
            }

            @Override
            public void processEnded(ParsingContext context) {
                AbstractRoutines.this.close(this.writer);
            }
        };
    }

    private void close(AbstractWriter writer) {
        if (writer != null) {
            if (!this.keepResourcesOpen) {
                writer.close();
            } else {
                writer.flush();
            }
        }
    }

    public <T> void writeAll(Iterable<T> elements, Class<T> beanType, File output, String ... headers) {
        this.writeAll(elements, beanType, ArgumentUtils.newWriter(output), headers);
    }

    public <T> void writeAll(Iterable<T> elements, Class<T> beanType, File output, String encoding, String[] headers) {
        this.writeAll(elements, beanType, ArgumentUtils.newWriter(output, encoding), headers);
    }

    public <T> void writeAll(Iterable<T> elements, Class<T> beanType, File output, Charset encoding, String ... headers) {
        this.writeAll(elements, beanType, ArgumentUtils.newWriter(output, encoding), headers);
    }

    public <T> void writeAll(Iterable<T> elements, Class<T> beanType, OutputStream output, String ... headers) {
        this.writeAll(elements, beanType, ArgumentUtils.newWriter(output), headers);
    }

    public <T> void writeAll(Iterable<T> elements, Class<T> beanType, OutputStream output, String encoding, String[] headers) {
        this.writeAll(elements, beanType, ArgumentUtils.newWriter(output, encoding), headers);
    }

    public <T> void writeAll(Iterable<T> elements, Class<T> beanType, OutputStream output, Charset encoding, String ... headers) {
        this.writeAll(elements, beanType, ArgumentUtils.newWriter(output, encoding), headers);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public <T> void writeAll(Iterable<T> elements, Class<T> beanType, Writer output, String ... headers) {
        BeanWriterProcessor<T> processor = new BeanWriterProcessor<T>(beanType);
        processor.setColumnMapper(this.columnMapper);
        this.setRowWriterProcessor(processor);
        try {
            if (headers.length > 0) {
                ((CommonSettings)this.writerSettings).setHeaders(headers);
                ((CommonWriterSettings)this.writerSettings).setHeaderWritingEnabled(true);
            }
            if (this.keepResourcesOpen && this.previousOutput == output) {
                ((CommonWriterSettings)this.writerSettings).setHeaderWritingEnabled(false);
            }
            AbstractWriter<W> writer = this.createWriter(output, this.writerSettings);
            if (this.keepResourcesOpen) {
                writer.processRecords(elements);
                this.previousOutput = output;
            } else {
                writer.processRecordsAndClose(elements);
            }
        }
        finally {
            ((CommonWriterSettings)this.writerSettings).setRowWriterProcessor(null);
        }
    }

    public <T> List<T> parseAll(Class<T> beanType, File input, int expectedBeanCount) {
        return this.parseAll(beanType, ArgumentUtils.newReader(input), expectedBeanCount);
    }

    public <T> List<T> parseAll(Class<T> beanType, File input, String encoding, int expectedBeanCount) {
        return this.parseAll(beanType, ArgumentUtils.newReader(input, encoding), expectedBeanCount);
    }

    public <T> List<T> parseAll(Class<T> beanType, File input, Charset encoding, int expectedBeanCount) {
        return this.parseAll(beanType, ArgumentUtils.newReader(input, encoding), expectedBeanCount);
    }

    public <T> List<T> parseAll(Class<T> beanType, InputStream input, int expectedBeanCount) {
        return this.parseAll(beanType, ArgumentUtils.newReader(input), expectedBeanCount);
    }

    public <T> List<T> parseAll(Class<T> beanType, InputStream input, String encoding, int expectedBeanCount) {
        return this.parseAll(beanType, ArgumentUtils.newReader(input, encoding), expectedBeanCount);
    }

    public <T> List<T> parseAll(Class<T> beanType, InputStream input, Charset encoding, int expectedBeanCount) {
        return this.parseAll(beanType, ArgumentUtils.newReader(input, encoding), expectedBeanCount);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public <T> List<T> parseAll(Class<T> beanType, Reader input, int expectedBeanCount) {
        BeanListProcessor<T> processor = new BeanListProcessor<T>(beanType, expectedBeanCount);
        processor.setColumnMapper(this.columnMapper);
        this.setRowProcessor(processor);
        try {
            this.createParser(this.parserSettings).parse(input);
            List list = processor.getBeans();
            return list;
        }
        finally {
            ((CommonParserSettings)this.parserSettings).setRowProcessor(null);
        }
    }

    public <T> List<T> parseAll(Class<T> beanType, File input) {
        return this.parseAll(beanType, input, 0);
    }

    public <T> List<T> parseAll(Class<T> beanType, File input, String encoding) {
        return this.parseAll(beanType, input, encoding, 0);
    }

    public <T> List<T> parseAll(Class<T> beanType, File input, Charset encoding) {
        return this.parseAll(beanType, input, encoding, 0);
    }

    public <T> List<T> parseAll(Class<T> beanType, InputStream input) {
        return this.parseAll(beanType, input, 0);
    }

    public <T> List<T> parseAll(Class<T> beanType, InputStream input, String encoding) {
        return this.parseAll(beanType, input, encoding, 0);
    }

    public <T> List<T> parseAll(Class<T> beanType, InputStream input, Charset encoding) {
        return this.parseAll(beanType, input, encoding, 0);
    }

    public <T> List<T> parseAll(Class<T> beanType, Reader input) {
        return this.parseAll(beanType, input, 0);
    }

    public <T> IterableResult<T, ParsingContext> iterate(Class<T> beanType, File input) {
        return this.iterate(beanType, ArgumentUtils.newReader(input));
    }

    public <T> IterableResult<T, ParsingContext> iterate(Class<T> beanType, File input, String encoding) {
        return this.iterate(beanType, ArgumentUtils.newReader(input, encoding));
    }

    public <T> IterableResult<T, ParsingContext> iterate(Class<T> beanType, File input, Charset encoding) {
        return this.iterate(beanType, ArgumentUtils.newReader(input, encoding));
    }

    public <T> IterableResult<T, ParsingContext> iterate(Class<T> beanType, InputStream input) {
        return this.iterate(beanType, ArgumentUtils.newReader(input));
    }

    public <T> IterableResult<T, ParsingContext> iterate(Class<T> beanType, InputStream input, String encoding) {
        return this.iterate(beanType, ArgumentUtils.newReader(input, encoding));
    }

    public <T> IterableResult<T, ParsingContext> iterate(Class<T> beanType, InputStream input, Charset encoding) {
        return this.iterate(beanType, ArgumentUtils.newReader(input, encoding));
    }

    public <T> IterableResult<T, ParsingContext> iterate(Class<T> beanType, final Reader input) {
        final Object[] beanHolder = new Object[1];
        BeanProcessor processor = new BeanProcessor<T>(beanType){

            @Override
            public void beanProcessed(T bean, ParsingContext context) {
                beanHolder[0] = bean;
            }

            @Override
            public void processEnded(ParsingContext context) {
                super.processEnded(context);
                AbstractRoutines.this.parserSettings.setRowProcessor(null);
            }
        };
        processor.setColumnMapper(this.columnMapper);
        this.setRowProcessor(processor);
        return new IterableResult<T, ParsingContext>(){
            private ParsingContext context;

            @Override
            public ParsingContext getContext() {
                return this.context;
            }

            @Override
            public ResultIterator<T, ParsingContext> iterator() {
                final AbstractParser<CommonParserSettings> parser = AbstractRoutines.this.createParser(AbstractRoutines.this.parserSettings);
                parser.beginParsing(input);
                this.context = parser.getContext();
                return new ResultIterator<T, ParsingContext>(){
                    String[] row;

                    @Override
                    public boolean hasNext() {
                        return beanHolder[0] != null || this.row != null || (this.row = parser.parseNext()) != null;
                    }

                    @Override
                    public T next() {
                        Object out = beanHolder[0];
                        if (out == null && this.hasNext()) {
                            out = beanHolder[0];
                        }
                        beanHolder[0] = null;
                        this.row = null;
                        return out;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("Can't remove beans");
                    }

                    @Override
                    public ParsingContext getContext() {
                        return context;
                    }
                };
            }
        };
    }

    public String toString() {
        return this.routineDescription;
    }

    public InputDimension getInputDimension(File input) {
        return this.getInputDimension(ArgumentUtils.newReader(input));
    }

    public InputDimension getInputDimension(File input, String encoding) {
        return this.getInputDimension(ArgumentUtils.newReader(input, encoding));
    }

    public InputDimension getInputDimension(InputStream input) {
        return this.getInputDimension(ArgumentUtils.newReader(input));
    }

    public InputDimension getInputDimension(InputStream input, String encoding) {
        return this.getInputDimension(ArgumentUtils.newReader(input, encoding));
    }

    public InputDimension getInputDimension(Reader input) {
        final InputDimension out = new InputDimension();
        this.setRowProcessor(new AbstractRowProcessor(){
            int lastColumn;

            @Override
            public void rowProcessed(String[] row, ParsingContext context) {
                if (this.lastColumn < row.length) {
                    this.lastColumn = row.length;
                }
            }

            @Override
            public void processEnded(ParsingContext context) {
                out.rows = context.currentRecord() + 1L;
                out.columns = this.lastColumn;
            }
        });
        P settings = this.getParserSettings();
        ((CommonSettings)settings).setMaxCharsPerColumn(-1);
        if (((CommonSettings)settings).getMaxColumns() < 1000000) {
            ((CommonSettings)settings).setMaxColumns(1000000);
        }
        ((CommonSettings)settings).selectIndexes(new Integer[0]);
        ((CommonParserSettings)settings).setColumnReorderingEnabled(false);
        this.createParser(settings).parse(input);
        return out;
    }

    public boolean getKeepResourcesOpen() {
        return this.keepResourcesOpen;
    }

    public void setKeepResourcesOpen(boolean keepResourcesOpen) {
        this.keepResourcesOpen = keepResourcesOpen;
    }

    public ColumnMapper getColumnMapper() {
        return this.columnMapper;
    }

    public void setColumnMapper(ColumnMapper columnMapper) {
        this.columnMapper = columnMapper == null ? new ColumnMapping() : (ColumnMapping)columnMapper.clone();
    }
}

