/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import shadeio.univocity.parsers.annotations.Headers;
import shadeio.univocity.parsers.annotations.helpers.AnnotationHelper;
import shadeio.univocity.parsers.annotations.helpers.MethodFilter;
import shadeio.univocity.parsers.common.ArgumentUtils;
import shadeio.univocity.parsers.common.CommonSettings;
import shadeio.univocity.parsers.common.Context;
import shadeio.univocity.parsers.common.Format;
import shadeio.univocity.parsers.common.fields.FieldSelector;
import shadeio.univocity.parsers.common.fields.FieldSet;
import shadeio.univocity.parsers.common.input.CharAppender;
import shadeio.univocity.parsers.common.input.CharInputReader;
import shadeio.univocity.parsers.common.input.DefaultCharAppender;
import shadeio.univocity.parsers.common.input.DefaultCharInputReader;
import shadeio.univocity.parsers.common.input.ExpandingCharAppender;
import shadeio.univocity.parsers.common.input.InputAnalysisProcess;
import shadeio.univocity.parsers.common.input.concurrent.ConcurrentCharInputReader;
import shadeio.univocity.parsers.common.processor.NoopRowProcessor;
import shadeio.univocity.parsers.common.processor.RowProcessor;
import shadeio.univocity.parsers.common.processor.core.AbstractBeanProcessor;
import shadeio.univocity.parsers.common.processor.core.AbstractMultiBeanProcessor;
import shadeio.univocity.parsers.common.processor.core.ColumnOrderDependent;
import shadeio.univocity.parsers.common.processor.core.NoopProcessor;
import shadeio.univocity.parsers.common.processor.core.Processor;

public abstract class CommonParserSettings<F extends Format>
extends CommonSettings<F> {
    protected Boolean headerExtractionEnabled = null;
    private Processor<? extends Context> processor;
    private boolean columnReorderingEnabled = true;
    private int inputBufferSize = 0x100000;
    private boolean readInputOnSeparateThread = Runtime.getRuntime().availableProcessors() > 1;
    private long numberOfRecordsToRead = -1L;
    private boolean lineSeparatorDetectionEnabled = false;
    private long numberOfRowsToSkip = 0L;
    private boolean commentCollectionEnabled = false;
    private boolean autoClosingEnabled = true;
    private boolean commentProcessingEnabled = true;
    private List<InputAnalysisProcess> inputAnalysisProcesses = new ArrayList<InputAnalysisProcess>();

    public boolean getReadInputOnSeparateThread() {
        return this.readInputOnSeparateThread;
    }

    public void setReadInputOnSeparateThread(boolean readInputOnSeparateThread) {
        this.readInputOnSeparateThread = readInputOnSeparateThread;
    }

    public boolean isHeaderExtractionEnabled() {
        return this.headerExtractionEnabled == null ? false : this.headerExtractionEnabled;
    }

    public void setHeaderExtractionEnabled(boolean headerExtractionEnabled) {
        this.headerExtractionEnabled = headerExtractionEnabled;
    }

    @Deprecated
    public RowProcessor getRowProcessor() {
        if (this.processor == null) {
            return NoopRowProcessor.instance;
        }
        return (RowProcessor)this.processor;
    }

    @Deprecated
    public void setRowProcessor(RowProcessor processor) {
        this.processor = processor;
    }

    public <T extends Context> Processor<T> getProcessor() {
        if (this.processor == null) {
            return NoopProcessor.instance;
        }
        return this.processor;
    }

    public void setProcessor(Processor<? extends Context> processor) {
        this.processor = processor;
    }

    protected CharInputReader newCharInputReader(int whitespaceRangeStart) {
        if (this.readInputOnSeparateThread) {
            if (this.lineSeparatorDetectionEnabled) {
                return new ConcurrentCharInputReader(((Format)this.getFormat()).getNormalizedNewline(), this.getInputBufferSize(), 10, whitespaceRangeStart, this.autoClosingEnabled);
            }
            return new ConcurrentCharInputReader(((Format)this.getFormat()).getLineSeparator(), ((Format)this.getFormat()).getNormalizedNewline(), this.getInputBufferSize(), 10, whitespaceRangeStart, this.autoClosingEnabled);
        }
        if (this.lineSeparatorDetectionEnabled) {
            return new DefaultCharInputReader(((Format)this.getFormat()).getNormalizedNewline(), this.getInputBufferSize(), whitespaceRangeStart, this.autoClosingEnabled);
        }
        return new DefaultCharInputReader(((Format)this.getFormat()).getLineSeparator(), ((Format)this.getFormat()).getNormalizedNewline(), this.getInputBufferSize(), whitespaceRangeStart, this.autoClosingEnabled);
    }

    public long getNumberOfRecordsToRead() {
        return this.numberOfRecordsToRead;
    }

    public void setNumberOfRecordsToRead(long numberOfRecordsToRead) {
        this.numberOfRecordsToRead = numberOfRecordsToRead;
    }

    public boolean isColumnReorderingEnabled() {
        return !this.preventReordering() && this.columnReorderingEnabled;
    }

    @Override
    FieldSet<?> getFieldSet() {
        return this.preventReordering() ? null : super.getFieldSet();
    }

    @Override
    FieldSelector getFieldSelector() {
        return this.preventReordering() ? null : super.getFieldSelector();
    }

    public void setColumnReorderingEnabled(boolean columnReorderingEnabled) {
        if (columnReorderingEnabled && this.preventReordering()) {
            throw new IllegalArgumentException("Cannot reorder columns when using a row processor that manipulates nested rows.");
        }
        this.columnReorderingEnabled = columnReorderingEnabled;
    }

    public int getInputBufferSize() {
        return this.inputBufferSize;
    }

    public void setInputBufferSize(int inputBufferSize) {
        this.inputBufferSize = inputBufferSize;
    }

    protected CharAppender newCharAppender() {
        int chars = this.getMaxCharsPerColumn();
        if (chars != -1) {
            return new DefaultCharAppender(chars, this.getNullValue(), this.getWhitespaceRangeStart());
        }
        return new ExpandingCharAppender(this.getNullValue(), this.getWhitespaceRangeStart());
    }

    public final boolean isLineSeparatorDetectionEnabled() {
        return this.lineSeparatorDetectionEnabled;
    }

    public final void setLineSeparatorDetectionEnabled(boolean lineSeparatorDetectionEnabled) {
        this.lineSeparatorDetectionEnabled = lineSeparatorDetectionEnabled;
    }

    public final long getNumberOfRowsToSkip() {
        return this.numberOfRowsToSkip;
    }

    public final void setNumberOfRowsToSkip(long numberOfRowsToSkip) {
        if (numberOfRowsToSkip < 0L) {
            throw new IllegalArgumentException("Number of rows to skip from the input must be 0 or greater");
        }
        this.numberOfRowsToSkip = numberOfRowsToSkip;
    }

    @Override
    protected void addConfiguration(Map<String, Object> out) {
        super.addConfiguration(out);
        out.put("Header extraction enabled", this.headerExtractionEnabled);
        out.put("Processor", this.processor == null ? "none" : this.processor.getClass().getName());
        out.put("Column reordering enabled", this.columnReorderingEnabled);
        out.put("Input buffer size", this.inputBufferSize);
        out.put("Input reading on separate thread", this.readInputOnSeparateThread);
        out.put("Number of records to read", this.numberOfRecordsToRead == -1L ? "all" : Long.valueOf(this.numberOfRecordsToRead));
        out.put("Line separator detection enabled", this.lineSeparatorDetectionEnabled);
        out.put("Auto-closing enabled", this.autoClosingEnabled);
    }

    private boolean preventReordering() {
        if (this.processor instanceof ColumnOrderDependent) {
            return ((ColumnOrderDependent)((Object)this.processor)).preventColumnReordering();
        }
        return false;
    }

    public boolean isCommentCollectionEnabled() {
        return this.commentCollectionEnabled;
    }

    public void setCommentCollectionEnabled(boolean commentCollectionEnabled) {
        this.commentCollectionEnabled = commentCollectionEnabled;
    }

    @Override
    final void runAutomaticConfiguration() {
        Class[] classes;
        Class beanClass = null;
        if (this.processor instanceof AbstractBeanProcessor) {
            beanClass = ((AbstractBeanProcessor)this.processor).getBeanClass();
        } else if (this.processor instanceof AbstractMultiBeanProcessor && (classes = ((AbstractMultiBeanProcessor)this.processor).getBeanClasses()).length > 0) {
            beanClass = classes[0];
        }
        if (beanClass != null) {
            this.configureFromAnnotations(beanClass);
        }
    }

    protected synchronized void configureFromAnnotations(Class<?> beanClass) {
        boolean extractHeaders;
        if (!this.deriveHeadersFrom(beanClass)) {
            return;
        }
        Headers headerAnnotation = AnnotationHelper.findHeadersAnnotation(beanClass);
        String[] headersFromBean = ArgumentUtils.EMPTY_STRING_ARRAY;
        boolean allFieldsIndexBased = AnnotationHelper.allFieldsIndexBasedForParsing(beanClass);
        boolean bl = extractHeaders = !allFieldsIndexBased;
        if (headerAnnotation != null) {
            if (headerAnnotation.sequence().length > 0) {
                headersFromBean = headerAnnotation.sequence();
            }
            extractHeaders = headerAnnotation.extract();
        }
        if (this.headerExtractionEnabled == null) {
            this.setHeaderExtractionEnabled(extractHeaders);
        }
        if (this.getHeaders() == null && headersFromBean.length > 0 && !this.headerExtractionEnabled.booleanValue()) {
            this.setHeadersDerivedFromClass(beanClass, headersFromBean);
        }
        if (this.getFieldSet() == null) {
            if (allFieldsIndexBased) {
                this.selectIndexes(AnnotationHelper.getSelectedIndexes(beanClass, MethodFilter.ONLY_SETTERS));
            } else if (headersFromBean.length > 0 && AnnotationHelper.allFieldsNameBasedForParsing(beanClass)) {
                this.selectFields(headersFromBean);
            }
        }
    }

    @Override
    protected CommonParserSettings clone(boolean clearInputSpecificSettings) {
        return (CommonParserSettings)super.clone(clearInputSpecificSettings);
    }

    @Override
    protected CommonParserSettings clone() {
        return (CommonParserSettings)super.clone();
    }

    @Override
    protected void clearInputSpecificSettings() {
        super.clearInputSpecificSettings();
        this.processor = null;
        this.numberOfRecordsToRead = -1L;
        this.numberOfRowsToSkip = 0L;
    }

    public boolean isAutoClosingEnabled() {
        return this.autoClosingEnabled;
    }

    public void setAutoClosingEnabled(boolean autoClosingEnabled) {
        this.autoClosingEnabled = autoClosingEnabled;
    }

    public boolean isCommentProcessingEnabled() {
        return this.commentProcessingEnabled;
    }

    public void setCommentProcessingEnabled(boolean commentProcessingEnabled) {
        this.commentProcessingEnabled = commentProcessingEnabled;
    }

    public void addInputAnalysisProcess(InputAnalysisProcess inputAnalysisProcess) {
        if (inputAnalysisProcess == null) {
            return;
        }
        if (this.inputAnalysisProcesses == null) {
            this.inputAnalysisProcesses = new ArrayList<InputAnalysisProcess>();
        }
        this.inputAnalysisProcesses.add(inputAnalysisProcess);
    }

    public List<InputAnalysisProcess> getInputAnalysisProcesses() {
        return this.inputAnalysisProcesses;
    }
}

