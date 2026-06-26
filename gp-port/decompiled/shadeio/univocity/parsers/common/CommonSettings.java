/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import shadeio.univocity.parsers.common.Context;
import shadeio.univocity.parsers.common.Format;
import shadeio.univocity.parsers.common.NoopProcessorErrorHandler;
import shadeio.univocity.parsers.common.NoopRowProcessorErrorHandler;
import shadeio.univocity.parsers.common.ProcessorErrorHandler;
import shadeio.univocity.parsers.common.RowProcessorErrorHandler;
import shadeio.univocity.parsers.common.fields.ExcludeFieldEnumSelector;
import shadeio.univocity.parsers.common.fields.ExcludeFieldIndexSelector;
import shadeio.univocity.parsers.common.fields.ExcludeFieldNameSelector;
import shadeio.univocity.parsers.common.fields.FieldEnumSelector;
import shadeio.univocity.parsers.common.fields.FieldIndexSelector;
import shadeio.univocity.parsers.common.fields.FieldNameSelector;
import shadeio.univocity.parsers.common.fields.FieldSelector;
import shadeio.univocity.parsers.common.fields.FieldSet;

public abstract class CommonSettings<F extends Format>
implements Cloneable {
    private F format;
    private String nullValue = null;
    private int maxCharsPerColumn = 4096;
    private int maxColumns = 512;
    private boolean skipEmptyLines = true;
    private boolean ignoreTrailingWhitespaces = true;
    private boolean ignoreLeadingWhitespaces = true;
    private FieldSelector fieldSelector = null;
    private boolean autoConfigurationEnabled = true;
    private ProcessorErrorHandler<? extends Context> errorHandler;
    private int errorContentLength = -1;
    private boolean skipBitsAsWhitespace = true;
    private String[] headers;
    Class<?> headerSourceClass;

    public CommonSettings() {
        this.setFormat(this.createDefaultFormat());
    }

    public String getNullValue() {
        return this.nullValue;
    }

    public void setNullValue(String emptyValue) {
        this.nullValue = emptyValue;
    }

    public int getMaxCharsPerColumn() {
        return this.maxCharsPerColumn;
    }

    public void setMaxCharsPerColumn(int maxCharsPerColumn) {
        this.maxCharsPerColumn = maxCharsPerColumn;
    }

    public boolean getSkipEmptyLines() {
        return this.skipEmptyLines;
    }

    public void setSkipEmptyLines(boolean skipEmptyLines) {
        this.skipEmptyLines = skipEmptyLines;
    }

    public boolean getIgnoreTrailingWhitespaces() {
        return this.ignoreTrailingWhitespaces;
    }

    public void setIgnoreTrailingWhitespaces(boolean ignoreTrailingWhitespaces) {
        this.ignoreTrailingWhitespaces = ignoreTrailingWhitespaces;
    }

    public boolean getIgnoreLeadingWhitespaces() {
        return this.ignoreLeadingWhitespaces;
    }

    public void setIgnoreLeadingWhitespaces(boolean ignoreLeadingWhitespaces) {
        this.ignoreLeadingWhitespaces = ignoreLeadingWhitespaces;
    }

    public void setHeaders(String ... headers) {
        this.headers = headers == null || headers.length == 0 ? null : headers;
    }

    void setHeadersDerivedFromClass(Class<?> headerSourceClass, String ... headers) {
        this.headerSourceClass = headerSourceClass;
        this.setHeaders(headers);
    }

    boolean deriveHeadersFrom(Class<?> beanClass) {
        if (this.headerSourceClass != null) {
            if (this.headerSourceClass != beanClass) {
                this.setHeaders(null);
            } else {
                return false;
            }
        }
        return true;
    }

    public String[] getHeaders() {
        return this.headers;
    }

    public int getMaxColumns() {
        return this.maxColumns;
    }

    public void setMaxColumns(int maxColumns) {
        this.maxColumns = maxColumns;
    }

    public F getFormat() {
        return this.format;
    }

    public void setFormat(F format) {
        if (format == null) {
            throw new IllegalArgumentException("Format cannot be null");
        }
        this.format = format;
    }

    public FieldSet<String> selectFields(String ... fieldNames) {
        return this.setFieldSet(new FieldNameSelector(), fieldNames);
    }

    public FieldSet<String> excludeFields(String ... fieldNames) {
        return this.setFieldSet(new ExcludeFieldNameSelector(), fieldNames);
    }

    public FieldSet<Integer> selectIndexes(Integer ... fieldIndexes) {
        return this.setFieldSet(new FieldIndexSelector(), fieldIndexes);
    }

    public FieldSet<Integer> excludeIndexes(Integer ... fieldIndexes) {
        return this.setFieldSet(new ExcludeFieldIndexSelector(), fieldIndexes);
    }

    public FieldSet<Enum> selectFields(Enum ... columns) {
        return this.setFieldSet(new FieldEnumSelector(), columns);
    }

    public FieldSet<Enum> excludeFields(Enum ... columns) {
        return this.setFieldSet(new ExcludeFieldEnumSelector(), columns);
    }

    private <T> FieldSet<T> setFieldSet(FieldSet<T> fieldSet, T ... values) {
        this.fieldSelector = (FieldSelector)((Object)fieldSet);
        fieldSet.add(values);
        return fieldSet;
    }

    FieldSet<?> getFieldSet() {
        return (FieldSet)((Object)this.fieldSelector);
    }

    FieldSelector getFieldSelector() {
        return this.fieldSelector;
    }

    public final boolean isAutoConfigurationEnabled() {
        return this.autoConfigurationEnabled;
    }

    public final void setAutoConfigurationEnabled(boolean autoConfigurationEnabled) {
        this.autoConfigurationEnabled = autoConfigurationEnabled;
    }

    @Deprecated
    public RowProcessorErrorHandler getRowProcessorErrorHandler() {
        return this.errorHandler == null ? NoopRowProcessorErrorHandler.instance : (RowProcessorErrorHandler)this.errorHandler;
    }

    @Deprecated
    public void setRowProcessorErrorHandler(RowProcessorErrorHandler rowProcessorErrorHandler) {
        this.errorHandler = rowProcessorErrorHandler;
    }

    public <T extends Context> ProcessorErrorHandler<T> getProcessorErrorHandler() {
        return this.errorHandler == null ? NoopProcessorErrorHandler.instance : this.errorHandler;
    }

    public void setProcessorErrorHandler(ProcessorErrorHandler<? extends Context> processorErrorHandler) {
        this.errorHandler = processorErrorHandler;
    }

    public boolean isProcessorErrorHandlerDefined() {
        return this.errorHandler != null;
    }

    protected abstract F createDefaultFormat();

    final void autoConfigure() {
        if (!this.autoConfigurationEnabled) {
            return;
        }
        this.runAutomaticConfiguration();
    }

    public final void trimValues(boolean trim) {
        this.setIgnoreLeadingWhitespaces(trim);
        this.setIgnoreTrailingWhitespaces(trim);
    }

    public int getErrorContentLength() {
        return this.errorContentLength;
    }

    public void setErrorContentLength(int errorContentLength) {
        this.errorContentLength = errorContentLength;
    }

    void runAutomaticConfiguration() {
    }

    public final boolean getSkipBitsAsWhitespace() {
        return this.skipBitsAsWhitespace;
    }

    public final void setSkipBitsAsWhitespace(boolean skipBitsAsWhitespace) {
        this.skipBitsAsWhitespace = skipBitsAsWhitespace;
    }

    protected final int getWhitespaceRangeStart() {
        return this.skipBitsAsWhitespace ? -1 : 1;
    }

    public final String toString() {
        StringBuilder out = new StringBuilder();
        out.append(this.getClass().getSimpleName()).append(':');
        TreeMap<String, Object> config = new TreeMap<String, Object>();
        this.addConfiguration(config);
        for (Map.Entry<String, Object> e : config.entrySet()) {
            out.append("\n\t");
            out.append(e.getKey()).append('=').append(e.getValue());
        }
        out.append("Format configuration:\n\t").append(((Format)this.getFormat()).toString());
        return out.toString();
    }

    protected void addConfiguration(Map<String, Object> out) {
        out.put("Null value", this.nullValue);
        out.put("Maximum number of characters per column", this.maxCharsPerColumn);
        out.put("Maximum number of columns", this.maxColumns);
        out.put("Skip empty lines", this.skipEmptyLines);
        out.put("Ignore trailing whitespaces", this.ignoreTrailingWhitespaces);
        out.put("Ignore leading whitespaces", this.ignoreLeadingWhitespaces);
        out.put("Selected fields", this.fieldSelector == null ? "none" : this.fieldSelector.describe());
        out.put("Headers", Arrays.toString(this.headers));
        out.put("Auto configuration enabled", this.autoConfigurationEnabled);
        out.put("RowProcessor error handler", this.errorHandler);
        out.put("Length of content displayed on error", this.errorContentLength);
        out.put("Restricting data in exceptions", this.errorContentLength == 0);
        out.put("Skip bits as whitespace", this.skipBitsAsWhitespace);
    }

    protected CommonSettings clone(boolean clearInputSpecificSettings) {
        try {
            CommonSettings out = (CommonSettings)super.clone();
            if (out.format != null) {
                out.format = ((Format)out.format).clone();
            }
            if (clearInputSpecificSettings) {
                out.clearInputSpecificSettings();
            }
            return out;
        }
        catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }

    protected CommonSettings clone() {
        return this.clone(false);
    }

    protected void clearInputSpecificSettings() {
        this.fieldSelector = null;
        this.headers = null;
    }
}

