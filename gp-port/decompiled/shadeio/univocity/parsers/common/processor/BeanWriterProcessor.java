/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor;

import shadeio.univocity.parsers.annotations.helpers.MethodFilter;
import shadeio.univocity.parsers.common.NormalizedString;
import shadeio.univocity.parsers.common.fields.FieldConversionMapping;
import shadeio.univocity.parsers.common.processor.RowWriterProcessor;
import shadeio.univocity.parsers.common.processor.core.BeanConversionProcessor;

public class BeanWriterProcessor<T>
extends BeanConversionProcessor<T>
implements RowWriterProcessor<T> {
    private NormalizedString[] normalizedHeaders;
    private String[] previousHeaders;

    public BeanWriterProcessor(Class<T> beanType) {
        super(beanType, MethodFilter.ONLY_GETTERS);
    }

    public Object[] write(T input, String[] headers, int[] indexesToWrite) {
        if (this.previousHeaders != headers) {
            this.previousHeaders = headers;
            this.normalizedHeaders = NormalizedString.toArray(headers);
        }
        return this.write(input, this.normalizedHeaders, indexesToWrite);
    }

    @Override
    public Object[] write(T input, NormalizedString[] headers, int[] indexesToWrite) {
        if (!this.initialized) {
            super.initialize(headers);
        }
        return this.reverseConversions(input, headers, indexesToWrite);
    }

    @Override
    protected FieldConversionMapping cloneConversions() {
        return null;
    }
}

