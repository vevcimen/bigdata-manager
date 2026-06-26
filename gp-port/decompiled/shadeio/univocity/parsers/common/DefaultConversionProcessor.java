/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import shadeio.univocity.parsers.common.ArgumentUtils;
import shadeio.univocity.parsers.common.Context;
import shadeio.univocity.parsers.common.ConversionProcessor;
import shadeio.univocity.parsers.common.DataProcessingException;
import shadeio.univocity.parsers.common.NoopProcessorErrorHandler;
import shadeio.univocity.parsers.common.NormalizedString;
import shadeio.univocity.parsers.common.ProcessorErrorHandler;
import shadeio.univocity.parsers.common.RetryableErrorHandler;
import shadeio.univocity.parsers.common.fields.FieldConversionMapping;
import shadeio.univocity.parsers.common.fields.FieldSet;
import shadeio.univocity.parsers.conversions.Conversion;

public abstract class DefaultConversionProcessor
implements ConversionProcessor {
    private Map<Class<?>, Conversion[]> conversionsByType;
    protected FieldConversionMapping conversions;
    private boolean conversionsInitialized;
    private int[] fieldIndexes;
    private int[] reverseFieldIndexes;
    private boolean fieldsReordered;
    ProcessorErrorHandler errorHandler = NoopProcessorErrorHandler.instance;
    Context context;

    @Override
    public final FieldSet<Integer> convertIndexes(Conversion ... conversions) {
        return this.getConversions().applyConversionsOnFieldIndexes(conversions);
    }

    @Override
    public final void convertAll(Conversion ... conversions) {
        this.getConversions().applyConversionsOnAllFields(conversions);
    }

    @Override
    public final FieldSet<String> convertFields(Conversion ... conversions) {
        return this.getConversions().applyConversionsOnFieldNames(conversions);
    }

    private FieldConversionMapping getConversions() {
        if (this.conversions == null) {
            this.conversions = new FieldConversionMapping();
        }
        return this.conversions;
    }

    protected void initializeConversions(String[] row, Context context) {
        String[] contextHeaders;
        this.conversionsInitialized = true;
        this.fieldIndexes = null;
        this.fieldsReordered = false;
        String[] stringArray = contextHeaders = context == null ? null : context.headers();
        if (contextHeaders != null && contextHeaders.length > 0) {
            this.getConversions().prepareExecution(false, contextHeaders);
        } else {
            this.getConversions().prepareExecution(false, row);
        }
        if (context != null) {
            this.fieldIndexes = context.extractedFieldIndexes();
            this.fieldsReordered = context.columnsReordered();
        }
    }

    public final Object[] applyConversions(String[] row, Context context) {
        boolean keepRow = true;
        Object[] objectRow = new Object[row.length];
        boolean[] convertedFlags = this.conversionsByType != null ? new boolean[row.length] : null;
        System.arraycopy(row, 0, objectRow, 0, row.length);
        if (this.conversions != null) {
            if (!this.conversionsInitialized) {
                this.initializeConversions(row, context);
            }
            int length = !this.fieldsReordered && this.fieldIndexes == null ? objectRow.length : this.fieldIndexes.length;
            for (int i = 0; i < length; ++i) {
                try {
                    if (!this.fieldsReordered) {
                        if (this.fieldIndexes == null) {
                            objectRow[i] = this.conversions.applyConversions(i, row[i], convertedFlags);
                            continue;
                        }
                        int index = this.fieldIndexes[i];
                        objectRow[index] = this.conversions.applyConversions(index, row[index], convertedFlags);
                        continue;
                    }
                    objectRow[i] = this.conversions.applyConversions(this.fieldIndexes[i], row[i], convertedFlags);
                    continue;
                }
                catch (Throwable ex) {
                    keepRow = this.handleConversionError(ex, objectRow, i);
                }
            }
        }
        if (keepRow && convertedFlags != null) {
            keepRow = this.applyConversionsByType(false, objectRow, convertedFlags);
        }
        if (keepRow && this.validateAllValues(objectRow)) {
            return objectRow;
        }
        return null;
    }

    private void populateReverseFieldIndexes() {
        int i;
        int max = 0;
        for (i = 0; i < this.fieldIndexes.length; ++i) {
            if (this.fieldIndexes[i] <= max) continue;
            max = this.fieldIndexes[i];
        }
        this.reverseFieldIndexes = new int[max + 1];
        Arrays.fill(this.reverseFieldIndexes, -1);
        for (i = 0; i < this.fieldIndexes.length; ++i) {
            this.reverseFieldIndexes[this.fieldIndexes[i]] = i;
        }
    }

    private boolean validateAllValues(Object[] objectRow) {
        if (this.conversions != null && this.conversions.validatedIndexes != null) {
            boolean keepRow = true;
            for (int i = 0; keepRow && i < this.conversions.validatedIndexes.length; ++i) {
                int index;
                int valueIndex = index = this.conversions.validatedIndexes[i];
                if (this.fieldsReordered) {
                    if (this.reverseFieldIndexes == null) {
                        this.populateReverseFieldIndexes();
                    }
                    valueIndex = this.reverseFieldIndexes[index];
                }
                try {
                    Object value = index < objectRow.length ? objectRow[valueIndex] : null;
                    this.conversions.executeValidations(index, value);
                    continue;
                }
                catch (Throwable ex) {
                    keepRow = this.handleConversionError(ex, objectRow, index);
                }
            }
            return keepRow;
        }
        return true;
    }

    public final boolean reverseConversions(boolean executeInReverseOrder, Object[] row, NormalizedString[] headers, int[] indexesToWrite) {
        boolean[] convertedFlags;
        boolean keepRow = true;
        boolean[] blArray = convertedFlags = this.conversionsByType != null ? new boolean[row.length] : null;
        if (this.conversions != null) {
            if (!this.conversionsInitialized) {
                this.conversionsInitialized = true;
                this.conversions.prepareExecution(true, headers != null ? NormalizedString.toArray(headers) : new String[row.length]);
                this.fieldIndexes = indexesToWrite;
            }
            if (executeInReverseOrder) {
                keepRow = this.validateAllValues(row);
            }
            int last = this.fieldIndexes == null ? row.length : this.fieldIndexes.length;
            for (int i = 0; i < last; ++i) {
                try {
                    if (this.fieldIndexes == null || this.fieldIndexes[i] == -1) {
                        row[i] = this.conversions.reverseConversions(executeInReverseOrder, i, row[i], convertedFlags);
                        continue;
                    }
                    int index = this.fieldIndexes[i];
                    row[index] = this.conversions.reverseConversions(executeInReverseOrder, index, row[index], convertedFlags);
                    continue;
                }
                catch (Throwable ex) {
                    keepRow = this.handleConversionError(ex, row, i);
                }
            }
        }
        if (keepRow && convertedFlags != null) {
            keepRow = this.applyConversionsByType(true, row, convertedFlags);
        }
        if (executeInReverseOrder) {
            return keepRow;
        }
        return keepRow && this.validateAllValues(row);
    }

    private boolean applyConversionsByType(boolean reverse, Object[] row, boolean[] convertedFlags) {
        boolean keepRow = true;
        for (int i = 0; i < row.length; ++i) {
            if (convertedFlags[i]) continue;
            try {
                row[i] = this.applyTypeConversion(reverse, row[i]);
                continue;
            }
            catch (Throwable ex) {
                keepRow = this.handleConversionError(ex, row, i);
            }
        }
        return keepRow;
    }

    protected final boolean handleConversionError(Throwable ex, Object[] row, int column) {
        if (row != null && row.length < column) {
            row = Arrays.copyOf(row, column + 1);
        }
        DataProcessingException error = this.toDataProcessingException(ex, row, column);
        if (column > -1 && this.errorHandler instanceof RetryableErrorHandler) {
            ((RetryableErrorHandler)this.errorHandler).prepareToRun();
        }
        error.markAsHandled(this.errorHandler);
        this.errorHandler.handleError(error, row, this.context);
        if (column > -1 && this.errorHandler instanceof RetryableErrorHandler) {
            Object defaultValue;
            RetryableErrorHandler retry = (RetryableErrorHandler)this.errorHandler;
            row[column] = defaultValue = retry.getDefaultValue();
            return !retry.isRecordSkipped();
        }
        return false;
    }

    protected DataProcessingException toDataProcessingException(Throwable ex, Object[] row, int column) {
        DataProcessingException error;
        if (ex instanceof DataProcessingException) {
            error = (DataProcessingException)ex;
            error.setRow(row);
            error.setColumnIndex(column);
        } else {
            error = new DataProcessingException("Error processing data conversions", column, row, ex);
        }
        error.markAsNonFatal();
        error.setContext(this.context);
        return error;
    }

    @Override
    public final void convertType(Class<?> type, Conversion ... conversions) {
        ArgumentUtils.noNulls("Type to convert", type);
        ArgumentUtils.noNulls("Sequence of conversions to apply over data of type " + type.getSimpleName(), conversions);
        if (this.conversionsByType == null) {
            this.conversionsByType = new HashMap();
        }
        this.conversionsByType.put(type, conversions);
    }

    private Object applyTypeConversion(boolean revert, Object input) {
        if (this.conversionsByType == null || input == null) {
            return input;
        }
        Conversion[] conversionSequence = this.conversionsByType.get(input.getClass());
        if (conversionSequence == null) {
            return input;
        }
        if (revert) {
            for (int i = 0; i < conversionSequence.length; ++i) {
                input = conversionSequence[i].revert(input);
            }
        } else {
            for (int i = 0; i < conversionSequence.length; ++i) {
                input = conversionSequence[i].execute(input);
            }
        }
        return input;
    }
}

