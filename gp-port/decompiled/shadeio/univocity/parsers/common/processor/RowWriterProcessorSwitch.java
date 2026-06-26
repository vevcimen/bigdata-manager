/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor;

import java.util.Arrays;
import java.util.Map;
import shadeio.univocity.parsers.common.DataProcessingException;
import shadeio.univocity.parsers.common.NormalizedString;
import shadeio.univocity.parsers.common.processor.RowWriterProcessor;

public abstract class RowWriterProcessorSwitch
implements RowWriterProcessor<Object> {
    private RowWriterProcessor selectedRowWriterProcessor = null;
    private int minimumRowLength = Integer.MIN_VALUE;
    private NormalizedString[] normalizedHeaders;
    private String[] previousHeaders;

    protected abstract RowWriterProcessor<?> switchRowProcessor(Object var1);

    protected NormalizedString[] getHeaders() {
        return null;
    }

    protected int[] getIndexes() {
        return null;
    }

    public void rowProcessorSwitched(RowWriterProcessor<?> from, RowWriterProcessor<?> to) {
    }

    public abstract NormalizedString[] getHeaders(Map var1, Map var2);

    public abstract NormalizedString[] getHeaders(Object var1);

    protected abstract String describeSwitch();

    public final int getMinimumRowLength() {
        if (this.minimumRowLength == Integer.MIN_VALUE) {
            this.minimumRowLength = 0;
            if (this.getHeaders() != null) {
                this.minimumRowLength = this.getHeaders().length;
            }
            if (this.getIndexes() != null) {
                for (int index : this.getIndexes()) {
                    if (index + 1 <= this.minimumRowLength) continue;
                    this.minimumRowLength = index + 1;
                }
            }
        }
        return this.minimumRowLength;
    }

    public Object[] write(Object input, String[] headers, int[] indicesToWrite) {
        if (this.previousHeaders != headers) {
            this.previousHeaders = headers;
            this.normalizedHeaders = NormalizedString.toArray(headers);
        }
        return this.write(input, this.normalizedHeaders, indicesToWrite);
    }

    @Override
    public Object[] write(Object input, NormalizedString[] headers, int[] indicesToWrite) {
        RowWriterProcessor<?> processor = this.switchRowProcessor(input);
        if (processor == null) {
            DataProcessingException ex = new DataProcessingException("Cannot find switch for input. Headers: {headers}, indices to write: " + Arrays.toString(indicesToWrite) + ". " + this.describeSwitch());
            ex.setValue("headers", Arrays.toString(headers));
            ex.setValue(input);
            throw ex;
        }
        if (processor != this.selectedRowWriterProcessor) {
            this.rowProcessorSwitched(this.selectedRowWriterProcessor, processor);
            this.selectedRowWriterProcessor = processor;
        }
        NormalizedString[] headersToUse = this.getHeaders();
        int[] indexesToUse = this.getIndexes();
        headersToUse = headersToUse == null ? headers : headersToUse;
        indexesToUse = indexesToUse == null ? indicesToWrite : indexesToUse;
        return this.selectedRowWriterProcessor.write(input, headersToUse, indexesToUse);
    }
}

