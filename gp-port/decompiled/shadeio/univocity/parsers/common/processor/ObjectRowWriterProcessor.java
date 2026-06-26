/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor;

import shadeio.univocity.parsers.common.DefaultConversionProcessor;
import shadeio.univocity.parsers.common.NormalizedString;
import shadeio.univocity.parsers.common.processor.RowWriterProcessor;

public class ObjectRowWriterProcessor
extends DefaultConversionProcessor
implements RowWriterProcessor<Object[]> {
    private NormalizedString[] normalizedHeaders;
    private String[] previousHeaders;

    public Object[] write(Object[] input, String[] headers, int[] indexesToWrite) {
        if (this.previousHeaders != headers) {
            this.previousHeaders = headers;
            this.normalizedHeaders = NormalizedString.toArray(headers);
        }
        return this.write(input, this.normalizedHeaders, indexesToWrite);
    }

    @Override
    public Object[] write(Object[] input, NormalizedString[] headers, int[] indexesToWrite) {
        if (input == null) {
            return null;
        }
        Object[] output = new Object[input.length];
        System.arraycopy(input, 0, output, 0, input.length);
        if (this.reverseConversions(false, output, headers, indexesToWrite)) {
            return output;
        }
        return null;
    }
}

