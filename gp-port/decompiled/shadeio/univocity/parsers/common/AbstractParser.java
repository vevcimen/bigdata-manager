/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import shadeio.univocity.parsers.common.AbstractException;
import shadeio.univocity.parsers.common.ArgumentUtils;
import shadeio.univocity.parsers.common.CommonParserSettings;
import shadeio.univocity.parsers.common.CommonSettings;
import shadeio.univocity.parsers.common.DataProcessingException;
import shadeio.univocity.parsers.common.DefaultConversionProcessor;
import shadeio.univocity.parsers.common.DefaultParsingContext;
import shadeio.univocity.parsers.common.Format;
import shadeio.univocity.parsers.common.Internal;
import shadeio.univocity.parsers.common.IterableResult;
import shadeio.univocity.parsers.common.LineReader;
import shadeio.univocity.parsers.common.ParserOutput;
import shadeio.univocity.parsers.common.ParsingContext;
import shadeio.univocity.parsers.common.ProcessorErrorHandler;
import shadeio.univocity.parsers.common.TextParsingException;
import shadeio.univocity.parsers.common.input.AbstractCharInputReader;
import shadeio.univocity.parsers.common.input.CharInputReader;
import shadeio.univocity.parsers.common.input.DefaultCharInputReader;
import shadeio.univocity.parsers.common.input.EOFException;
import shadeio.univocity.parsers.common.input.InputAnalysisProcess;
import shadeio.univocity.parsers.common.input.LookaheadCharInputReader;
import shadeio.univocity.parsers.common.iterators.RecordIterator;
import shadeio.univocity.parsers.common.iterators.RowIterator;
import shadeio.univocity.parsers.common.processor.core.NoopProcessor;
import shadeio.univocity.parsers.common.processor.core.Processor;
import shadeio.univocity.parsers.common.record.Record;
import shadeio.univocity.parsers.common.record.RecordMetaData;

public abstract class AbstractParser<T extends CommonParserSettings<?>> {
    protected final T settings;
    protected final ParserOutput output;
    private final long recordsToRead;
    protected final char comment;
    private final LineReader lineReader = new LineReader();
    protected ParsingContext context;
    protected Processor processor;
    protected CharInputReader input;
    protected char ch;
    private final ProcessorErrorHandler errorHandler;
    private final long rowsToSkip;
    protected final Map<Long, String> comments;
    protected String lastComment;
    private final boolean collectComments;
    private final int errorContentLength;
    private boolean extractingHeaders = false;
    private final boolean extractHeaders;
    protected final int whitespaceRangeStart;
    protected boolean ignoreTrailingWhitespace;
    protected boolean ignoreLeadingWhitespace;
    private final boolean processComments;

    public AbstractParser(T settings) {
        ((CommonSettings)settings).autoConfigure();
        this.settings = settings;
        this.errorContentLength = ((CommonSettings)settings).getErrorContentLength();
        this.ignoreTrailingWhitespace = ((CommonSettings)settings).getIgnoreTrailingWhitespaces();
        this.ignoreLeadingWhitespace = ((CommonSettings)settings).getIgnoreLeadingWhitespaces();
        this.output = new ParserOutput(this, (CommonParserSettings<?>)settings);
        this.processor = ((CommonParserSettings)settings).getProcessor();
        this.recordsToRead = ((CommonParserSettings)settings).getNumberOfRecordsToRead();
        this.comment = ((Format)((CommonSettings)settings).getFormat()).getComment();
        this.errorHandler = ((CommonSettings)settings).getProcessorErrorHandler();
        this.rowsToSkip = ((CommonParserSettings)settings).getNumberOfRowsToSkip();
        this.collectComments = ((CommonParserSettings)settings).isCommentCollectionEnabled();
        this.comments = this.collectComments ? new TreeMap() : Collections.emptyMap();
        this.extractHeaders = ((CommonParserSettings)settings).isHeaderExtractionEnabled();
        this.whitespaceRangeStart = ((CommonSettings)settings).getWhitespaceRangeStart();
        this.processComments = ((CommonParserSettings)settings).isCommentProcessingEnabled();
    }

    protected void processComment() {
        if (this.collectComments) {
            long line = this.input.lineCount();
            String comment = this.input.readComment();
            if (comment != null) {
                this.lastComment = comment;
                this.comments.put(line, this.lastComment);
            }
        } else {
            try {
                this.input.skipLines(1L);
            }
            catch (IllegalArgumentException illegalArgumentException) {
                // empty catch block
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void parse(Reader reader) {
        this.beginParsing(reader);
        try {
            while (!this.context.isStopped()) {
                String[] row;
                this.input.markRecordStart();
                this.ch = this.input.nextChar();
                if (this.processComments && this.inComment()) {
                    this.processComment();
                    continue;
                }
                if (this.output.pendingRecords.isEmpty()) {
                    this.parseRecord();
                }
                if ((row = this.output.rowParsed()) == null) continue;
                if (this.recordsToRead >= 0L && this.context.currentRecord() >= this.recordsToRead) {
                    this.context.stop();
                    if (this.recordsToRead == 0L) {
                        this.stopParsing();
                        return;
                    }
                }
                if (this.processor == NoopProcessor.instance) continue;
                this.rowProcessed(row);
            }
            this.stopParsing();
        }
        catch (EOFException ex) {
            try {
                this.handleEOF();
                while (!this.output.pendingRecords.isEmpty()) {
                    this.handleEOF();
                }
            }
            finally {
                this.stopParsing();
            }
        }
        catch (Throwable ex2) {
            try {
                TextParsingException ex2 = this.handleException(ex2);
                this.stopParsing(ex2);
            }
            catch (Throwable throwable) {
                this.stopParsing(ex2);
                throw throwable;
            }
        }
    }

    protected abstract void parseRecord();

    protected boolean consumeValueOnEOF() {
        return false;
    }

    private String[] handleEOF() {
        String[] row = null;
        try {
            boolean consumeValueOnEOF = this.consumeValueOnEOF();
            if (this.output.column != 0 || consumeValueOnEOF && !this.context.isStopped()) {
                if (this.output.appender.length() > 0 || consumeValueOnEOF) {
                    this.output.valueParsed();
                } else if (this.input.currentParsedContentLength() > 0) {
                    this.output.emptyParsed();
                }
                row = this.output.rowParsed();
            } else if (this.output.appender.length() > 0 || this.input.currentParsedContentLength() > 0) {
                if (this.output.appender.length() == 0) {
                    this.output.emptyParsed();
                } else {
                    this.output.valueParsed();
                }
                row = this.output.rowParsed();
            } else if (!this.output.pendingRecords.isEmpty()) {
                row = this.output.pendingRecords.poll();
            }
        }
        catch (Throwable e) {
            throw this.handleException(e);
        }
        if (row != null && this.processor != NoopProcessor.instance) {
            this.rowProcessed(row);
        }
        return row;
    }

    public final void beginParsing(Reader reader) {
        this.output.reset();
        this.input = reader instanceof LineReader ? new DefaultCharInputReader(((Format)((CommonSettings)this.settings).getFormat()).getLineSeparator(), ((Format)((CommonSettings)this.settings).getFormat()).getNormalizedNewline(), ((CommonParserSettings)this.settings).getInputBufferSize(), this.whitespaceRangeStart, true) : ((CommonParserSettings)this.settings).newCharInputReader(this.whitespaceRangeStart);
        this.input.enableNormalizeLineEndings(true);
        this.context = this.createParsingContext();
        if (this.processor instanceof DefaultConversionProcessor) {
            DefaultConversionProcessor conversionProcessor = (DefaultConversionProcessor)((Object)this.processor);
            conversionProcessor.errorHandler = this.errorHandler;
            conversionProcessor.context = this.context;
        }
        if (this.input instanceof AbstractCharInputReader) {
            AbstractCharInputReader inputReader = (AbstractCharInputReader)this.input;
            inputReader.addInputAnalysisProcess(this.getInputAnalysisProcess());
            for (InputAnalysisProcess p : ((CommonParserSettings)this.settings).getInputAnalysisProcesses()) {
                inputReader.addInputAnalysisProcess(p);
            }
        }
        try {
            this.input.start(reader);
        }
        catch (Throwable t) {
            throw this.handleException(t);
        }
        this.input.skipLines(this.rowsToSkip);
        this.initialize();
        this.processor.processStarted(this.context);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void extractHeadersIfRequired() {
        while (this.extractHeaders && this.output.parsedHeaders == null && !this.context.isStopped() && !this.extractingHeaders) {
            Processor userProvidedProcessor = this.processor;
            try {
                this.processor = NoopProcessor.instance;
                this.extractingHeaders = true;
                this.parseNext();
            }
            finally {
                this.extractingHeaders = false;
                this.processor = userProvidedProcessor;
            }
        }
    }

    protected ParsingContext createParsingContext() {
        DefaultParsingContext out = new DefaultParsingContext(this, this.errorContentLength);
        out.stopped = false;
        return out;
    }

    protected void initialize() {
    }

    protected InputAnalysisProcess getInputAnalysisProcess() {
        return null;
    }

    private String getParsedContent(CharSequence tmp) {
        return "Parsed content: " + AbstractException.restrictContent(this.errorContentLength, tmp);
    }

    private TextParsingException handleException(Throwable ex) {
        if (this.context != null) {
            this.context.stop();
        }
        if (ex instanceof DataProcessingException) {
            DataProcessingException error = (DataProcessingException)ex;
            error.restrictContent(this.errorContentLength);
            error.setContext(this.context);
            throw error;
        }
        String message = ex.getClass().getName() + " - " + ex.getMessage();
        char[] chars = this.output.appender.getChars();
        if (chars != null) {
            String tmp;
            int length = this.output.appender.length();
            if (length > chars.length) {
                message = "Length of parsed input (" + length + ") exceeds the maximum number of characters defined in" + " your parser settings (" + ((CommonSettings)this.settings).getMaxCharsPerColumn() + "). ";
                length = chars.length;
            }
            if ((tmp = new String(chars)).contains("\n") || tmp.contains("\r")) {
                tmp = ArgumentUtils.displayLineSeparators(tmp, true);
                String lineSeparator = ArgumentUtils.displayLineSeparators(((Format)((CommonSettings)this.settings).getFormat()).getLineSeparatorString(), false);
                message = message + "\nIdentified line separator characters in the parsed content. This may be the cause of the error. The line separator in your parser settings is set to '" + lineSeparator + "'. " + this.getParsedContent(tmp);
            }
            int nullCharacterCount = 0;
            int maxLength = length > 0x3FFFFFFF ? 0x3FFFFFFE : length;
            StringBuilder s2 = new StringBuilder(maxLength);
            for (int i = 0; i < maxLength; ++i) {
                if (chars[i] == '\u0000') {
                    s2.append('\\');
                    s2.append('0');
                    ++nullCharacterCount;
                    continue;
                }
                s2.append(chars[i]);
            }
            tmp = s2.toString();
            if (nullCharacterCount > 0) {
                message = message + "\nIdentified " + nullCharacterCount + " null characters ('\u0000') on parsed content. This may " + "indicate the data is corrupt or its encoding is invalid. Parsed content:\n\t" + this.getParsedContent(tmp);
            }
        }
        if (ex instanceof ArrayIndexOutOfBoundsException) {
            try {
                int index = Integer.parseInt(ex.getMessage());
                if (index == ((CommonSettings)this.settings).getMaxCharsPerColumn()) {
                    message = message + "\nHint: Number of characters processed may have exceeded limit of " + index + " characters per column. Use settings.setMaxCharsPerColumn(int) to define the maximum number of characters a column can have";
                }
                if (index == ((CommonSettings)this.settings).getMaxColumns()) {
                    message = message + "\nHint: Number of columns processed may have exceeded limit of " + index + " columns. Use settings.setMaxColumns(int) to define the maximum number of columns your input can have";
                }
                message = message + "\nEnsure your configuration is correct, with delimiters, quotes and escape sequences that match the input format you are trying to parse";
            }
            catch (Throwable t) {
                // empty catch block
            }
        }
        try {
            if (!message.isEmpty()) {
                message = message + "\n";
            }
            message = message + "Parser Configuration: " + ((CommonSettings)this.settings).toString();
        }
        catch (Exception t) {
            // empty catch block
        }
        if (this.errorContentLength == 0) {
            this.output.appender.reset();
        }
        TextParsingException out = new TextParsingException(this.context, message, ex);
        out.setErrorContentLength(this.errorContentLength);
        return out;
    }

    private void stopParsing(Throwable error) {
        if (error != null) {
            try {
                this.stopParsing();
            }
            catch (Throwable ex) {
                // empty catch block
            }
            if (error instanceof DataProcessingException) {
                DataProcessingException ex = (DataProcessingException)error;
                ex.setContext(this.context);
                throw ex;
            }
            if (error instanceof RuntimeException) {
                throw (RuntimeException)error;
            }
            if (error instanceof Error) {
                throw (Error)error;
            }
            throw new IllegalStateException(error.getMessage(), error);
        }
        this.stopParsing();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void stopParsing() {
        try {
            this.ch = '\u0000';
            try {
                if (this.context != null) {
                    this.context.stop();
                }
            }
            finally {
                try {
                    if (this.processor != null) {
                        this.processor.processEnded(this.context);
                    }
                }
                finally {
                    if (this.output != null) {
                        this.output.appender.reset();
                    }
                    if (this.input != null) {
                        this.input.stop();
                    }
                }
            }
        }
        catch (Throwable error) {
            throw this.handleException(error);
        }
    }

    private <T> List<T> beginParseAll(boolean validateReader, Reader reader, int expectedRowCount) {
        if (reader == null) {
            if (validateReader) {
                throw new IllegalStateException("Input reader must not be null");
            }
            if (this.context == null) {
                throw new IllegalStateException("Input not defined. Please call method 'beginParsing()' with a valid input.");
            }
            if (this.context.isStopped()) {
                return Collections.emptyList();
            }
        }
        ArrayList out = new ArrayList(expectedRowCount <= 0 ? 10000 : expectedRowCount);
        if (reader != null) {
            this.beginParsing(reader);
        }
        return out;
    }

    public List<String[]> parseAll(int expectedRowCount) {
        return this.internalParseAll(false, null, expectedRowCount);
    }

    public List<String[]> parseAll() {
        return this.internalParseAll(false, null, -1);
    }

    public List<Record> parseAllRecords(int expectedRowCount) {
        return this.internalParseAllRecords(false, null, expectedRowCount);
    }

    public List<Record> parseAllRecords() {
        return this.internalParseAllRecords(false, null, -1);
    }

    public final List<String[]> parseAll(Reader reader) {
        return this.parseAll(reader, 0);
    }

    public final List<String[]> parseAll(Reader reader, int expectedRowCount) {
        return this.internalParseAll(true, reader, expectedRowCount);
    }

    private final List<String[]> internalParseAll(boolean validateReader, Reader reader, int expectedRowCount) {
        String[] row;
        List<String[]> out = this.beginParseAll(validateReader, reader, expectedRowCount);
        while ((row = this.parseNext()) != null) {
            out.add(row);
        }
        return out;
    }

    protected boolean inComment() {
        return this.ch == this.comment;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final String[] parseNext() {
        try {
            while (!this.context.isStopped()) {
                String[] row;
                this.input.markRecordStart();
                this.ch = this.input.nextChar();
                if (this.processComments && this.inComment()) {
                    this.processComment();
                    continue;
                }
                if (this.output.pendingRecords.isEmpty()) {
                    this.parseRecord();
                }
                if ((row = this.output.rowParsed()) != null) {
                    if (this.recordsToRead >= 0L && this.context.currentRecord() >= this.recordsToRead) {
                        this.context.stop();
                        if (this.recordsToRead == 0L) {
                            this.stopParsing();
                            return null;
                        }
                    }
                    if (this.processor != NoopProcessor.instance) {
                        this.rowProcessed(row);
                    }
                    return row;
                }
                if (!this.extractingHeaders) continue;
                return null;
            }
            if (this.output.column != 0) {
                return this.output.rowParsed();
            }
            this.stopParsing();
            return null;
        }
        catch (EOFException ex) {
            String[] row = this.handleEOF();
            if (this.output.pendingRecords.isEmpty()) {
                this.stopParsing();
            }
            return row;
        }
        catch (NullPointerException ex) {
            if (this.context == null) {
                throw new IllegalStateException("Cannot parse without invoking method beginParsing(Reader) first");
            }
            if (this.input != null) {
                this.stopParsing();
            }
            throw new IllegalStateException("Error parsing next record.", ex);
        }
        catch (Throwable ex2) {
            try {
                TextParsingException ex2 = this.handleException(ex2);
                this.stopParsing(ex2);
            }
            catch (Throwable throwable) {
                this.stopParsing(ex2);
                throw throwable;
            }
            return null;
        }
    }

    protected final void reloadHeaders() {
        this.output.initializeHeaders();
        if (this.context instanceof DefaultParsingContext) {
            ((DefaultParsingContext)this.context).reset();
        }
    }

    public final Record parseRecord(String line) {
        String[] values = this.parseLine(line);
        if (values == null) {
            return null;
        }
        return this.context.toRecord(values);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final String[] parseLine(String line) {
        if (line == null || line.isEmpty()) {
            return null;
        }
        this.lineReader.setLine(line);
        if (this.context == null || this.context.isStopped()) {
            this.beginParsing(this.lineReader);
        } else if (this.input instanceof DefaultCharInputReader) {
            ((DefaultCharInputReader)this.input).reloadBuffer();
        } else if (this.input instanceof LookaheadCharInputReader) {
            ((LookaheadCharInputReader)this.input).reloadBuffer();
        }
        try {
            while (!this.context.isStopped()) {
                String[] row;
                this.input.markRecordStart();
                this.ch = this.input.nextChar();
                if (this.processComments && this.inComment()) {
                    this.processComment();
                    return null;
                }
                if (this.output.pendingRecords.isEmpty()) {
                    this.parseRecord();
                }
                if ((row = this.output.rowParsed()) == null) continue;
                if (this.processor != NoopProcessor.instance) {
                    this.rowProcessed(row);
                }
                return row;
            }
            return null;
        }
        catch (EOFException ex) {
            return this.handleEOF();
        }
        catch (NullPointerException ex) {
            if (this.input != null) {
                this.stopParsing(null);
            }
            throw new IllegalStateException("Error parsing next record.", ex);
        }
        catch (Throwable ex2) {
            try {
                TextParsingException ex2 = this.handleException(ex2);
                this.stopParsing(ex2);
            }
            catch (Throwable throwable) {
                this.stopParsing(ex2);
                throw throwable;
            }
            return null;
        }
    }

    private void rowProcessed(String[] row) {
        Internal.process(row, this.processor, this.context, this.errorHandler);
    }

    public final void parse(File file) {
        this.parse(ArgumentUtils.newReader(file));
    }

    public final void parse(File file, String encoding) {
        this.parse(ArgumentUtils.newReader(file, encoding));
    }

    public final void parse(File file, Charset encoding) {
        this.parse(ArgumentUtils.newReader(file, encoding));
    }

    public final void parse(InputStream input) {
        this.parse(ArgumentUtils.newReader(input));
    }

    public final void parse(InputStream input, String encoding) {
        this.parse(ArgumentUtils.newReader(input, encoding));
    }

    public final void parse(InputStream input, Charset encoding) {
        this.parse(ArgumentUtils.newReader(input, encoding));
    }

    public final void beginParsing(File file) {
        this.beginParsing(ArgumentUtils.newReader(file));
    }

    public final void beginParsing(File file, String encoding) {
        this.beginParsing(ArgumentUtils.newReader(file, encoding));
    }

    public final void beginParsing(File file, Charset encoding) {
        this.beginParsing(ArgumentUtils.newReader(file, encoding));
    }

    public final void beginParsing(InputStream input) {
        this.beginParsing(ArgumentUtils.newReader(input));
    }

    public final void beginParsing(InputStream input, String encoding) {
        this.beginParsing(ArgumentUtils.newReader(input, encoding));
    }

    public final void beginParsing(InputStream input, Charset encoding) {
        this.beginParsing(ArgumentUtils.newReader(input, encoding));
    }

    public final List<String[]> parseAll(File file, int expectedRowCount) {
        return this.parseAll(ArgumentUtils.newReader(file), expectedRowCount);
    }

    public final List<String[]> parseAll(File file, String encoding, int expectedRowCount) {
        return this.parseAll(ArgumentUtils.newReader(file, encoding), expectedRowCount);
    }

    public final List<String[]> parseAll(File file, Charset encoding, int expectedRowCount) {
        return this.parseAll(ArgumentUtils.newReader(file, encoding), expectedRowCount);
    }

    public final List<String[]> parseAll(InputStream input, int expectedRowCount) {
        return this.parseAll(ArgumentUtils.newReader(input), expectedRowCount);
    }

    public final List<String[]> parseAll(InputStream input, String encoding, int expectedRowCount) {
        return this.parseAll(ArgumentUtils.newReader(input, encoding), expectedRowCount);
    }

    public final List<String[]> parseAll(InputStream input, Charset encoding, int expectedRowCount) {
        return this.parseAll(ArgumentUtils.newReader(input, encoding), expectedRowCount);
    }

    public final List<String[]> parseAll(File file) {
        return this.parseAll(ArgumentUtils.newReader(file));
    }

    public final List<String[]> parseAll(File file, String encoding) {
        return this.parseAll(ArgumentUtils.newReader(file, encoding));
    }

    public final List<String[]> parseAll(File file, Charset encoding) {
        return this.parseAll(ArgumentUtils.newReader(file, encoding));
    }

    public final List<String[]> parseAll(InputStream input) {
        return this.parseAll(ArgumentUtils.newReader(input));
    }

    public final List<String[]> parseAll(InputStream input, String encoding) {
        return this.parseAll(ArgumentUtils.newReader(input, encoding));
    }

    public final List<String[]> parseAll(InputStream input, Charset encoding) {
        return this.parseAll(ArgumentUtils.newReader(input, encoding));
    }

    public final List<Record> parseAllRecords(File file, int expectedRowCount) {
        return this.parseAllRecords(ArgumentUtils.newReader(file), expectedRowCount);
    }

    public final List<Record> parseAllRecords(File file, String encoding, int expectedRowCount) {
        return this.parseAllRecords(ArgumentUtils.newReader(file, encoding), expectedRowCount);
    }

    public final List<Record> parseAllRecords(File file, Charset encoding, int expectedRowCount) {
        return this.parseAllRecords(ArgumentUtils.newReader(file, encoding), expectedRowCount);
    }

    public final List<Record> parseAllRecords(InputStream input, int expectedRowCount) {
        return this.parseAllRecords(ArgumentUtils.newReader(input), expectedRowCount);
    }

    public final List<Record> parseAllRecords(InputStream input, String encoding, int expectedRowCount) {
        return this.parseAllRecords(ArgumentUtils.newReader(input, encoding), expectedRowCount);
    }

    public final List<Record> parseAllRecords(InputStream input, Charset encoding, int expectedRowCount) {
        return this.parseAllRecords(ArgumentUtils.newReader(input, encoding), expectedRowCount);
    }

    public final List<Record> parseAllRecords(File file) {
        return this.parseAllRecords(ArgumentUtils.newReader(file));
    }

    public final List<Record> parseAllRecords(File file, String encoding) {
        return this.parseAllRecords(ArgumentUtils.newReader(file, encoding));
    }

    public final List<Record> parseAllRecords(File file, Charset encoding) {
        return this.parseAllRecords(ArgumentUtils.newReader(file, encoding));
    }

    public final List<Record> parseAllRecords(InputStream input) {
        return this.parseAllRecords(ArgumentUtils.newReader(input));
    }

    public final List<Record> parseAllRecords(InputStream input, String encoding) {
        return this.parseAllRecords(ArgumentUtils.newReader(input, encoding));
    }

    public final List<Record> parseAllRecords(InputStream input, Charset encoding) {
        return this.parseAllRecords(ArgumentUtils.newReader(input, encoding));
    }

    public final List<Record> parseAllRecords(Reader reader, int expectedRowCount) {
        return this.internalParseAllRecords(true, reader, expectedRowCount);
    }

    private List<Record> internalParseAllRecords(boolean validateReader, Reader reader, int expectedRowCount) {
        Record record;
        List<Record> out = this.beginParseAll(validateReader, reader, expectedRowCount);
        if (this.context.isStopped()) {
            return out;
        }
        while ((record = this.parseNextRecord()) != null) {
            out.add(record);
        }
        return out;
    }

    public final List<Record> parseAllRecords(Reader reader) {
        return this.parseAllRecords(reader, 0);
    }

    public final Record parseNextRecord() {
        String[] row = this.parseNext();
        if (row != null) {
            return this.context.toRecord(row);
        }
        return null;
    }

    final Map<Long, String> getComments() {
        return this.comments;
    }

    final String getLastComment() {
        return this.lastComment;
    }

    final String[] getParsedHeaders() {
        this.extractHeadersIfRequired();
        return this.output.parsedHeaders;
    }

    public final ParsingContext getContext() {
        return this.context;
    }

    public final RecordMetaData getRecordMetadata() {
        if (this.context == null) {
            throw new IllegalStateException("Record metadata not available. The parser has not been started.");
        }
        return this.context.recordMetaData();
    }

    public final IterableResult<String[], ParsingContext> iterate(File input, String encoding) {
        return this.iterate(input, Charset.forName(encoding));
    }

    public final IterableResult<String[], ParsingContext> iterate(final File input, final Charset encoding) {
        return new RowIterator(this){

            @Override
            protected void beginParsing() {
                this.parser.beginParsing(input, encoding);
            }
        };
    }

    public final IterableResult<String[], ParsingContext> iterate(final File input) {
        return new RowIterator(this){

            @Override
            protected void beginParsing() {
                this.parser.beginParsing(input);
            }
        };
    }

    public final IterableResult<String[], ParsingContext> iterate(final Reader input) {
        return new RowIterator(this){

            @Override
            protected void beginParsing() {
                this.parser.beginParsing(input);
            }
        };
    }

    public final IterableResult<String[], ParsingContext> iterate(InputStream input, String encoding) {
        return this.iterate(input, Charset.forName(encoding));
    }

    public final IterableResult<String[], ParsingContext> iterate(final InputStream input, final Charset encoding) {
        return new RowIterator(this){

            @Override
            protected void beginParsing() {
                this.parser.beginParsing(input, encoding);
            }
        };
    }

    public final IterableResult<String[], ParsingContext> iterate(final InputStream input) {
        return new RowIterator(this){

            @Override
            protected void beginParsing() {
                this.parser.beginParsing(input);
            }
        };
    }

    public final IterableResult<Record, ParsingContext> iterateRecords(File input, String encoding) {
        return this.iterateRecords(input, Charset.forName(encoding));
    }

    public final IterableResult<Record, ParsingContext> iterateRecords(final File input, final Charset encoding) {
        return new RecordIterator(this){

            @Override
            protected void beginParsing() {
                this.parser.beginParsing(input, encoding);
            }
        };
    }

    public final IterableResult<Record, ParsingContext> iterateRecords(final File input) {
        return new RecordIterator(this){

            @Override
            protected void beginParsing() {
                this.parser.beginParsing(input);
            }
        };
    }

    public final IterableResult<Record, ParsingContext> iterateRecords(final Reader input) {
        return new RecordIterator(this){

            @Override
            protected void beginParsing() {
                this.parser.beginParsing(input);
            }
        };
    }

    public final IterableResult<Record, ParsingContext> iterateRecords(InputStream input, String encoding) {
        return this.iterateRecords(input, Charset.forName(encoding));
    }

    public final IterableResult<Record, ParsingContext> iterateRecords(final InputStream input, final Charset encoding) {
        return new RecordIterator(this){

            @Override
            protected void beginParsing() {
                this.parser.beginParsing(input, encoding);
            }
        };
    }

    public final IterableResult<Record, ParsingContext> iterateRecords(final InputStream input) {
        return new RecordIterator(this){

            @Override
            protected void beginParsing() {
                this.parser.beginParsing(input);
            }
        };
    }
}

