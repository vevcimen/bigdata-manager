/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor;

import shadeio.univocity.parsers.common.ParsingContext;
import shadeio.univocity.parsers.common.ParsingContextWrapper;
import shadeio.univocity.parsers.common.processor.RowProcessor;
import shadeio.univocity.parsers.common.processor.core.AbstractInputValueSwitch;

public class InputValueSwitch
extends AbstractInputValueSwitch<ParsingContext>
implements RowProcessor {
    public InputValueSwitch() {
        this(0);
    }

    public InputValueSwitch(int columnIndex) {
        super(columnIndex);
    }

    public InputValueSwitch(String columnName) {
        super(columnName);
    }

    @Override
    protected final ParsingContext wrapContext(ParsingContext context) {
        return new ParsingContextWrapper(context){
            private final String[] fieldNames;
            private final int[] indexes;
            {
                this.fieldNames = InputValueSwitch.this.getHeaders();
                this.indexes = InputValueSwitch.this.getIndexes();
            }

            @Override
            public String[] headers() {
                return this.fieldNames == null || this.fieldNames.length == 0 ? ((ParsingContext)this.context).headers() : this.fieldNames;
            }

            @Override
            public int[] extractedFieldIndexes() {
                return this.indexes == null || this.indexes.length == 0 ? ((ParsingContext)this.context).extractedFieldIndexes() : this.indexes;
            }
        };
    }
}

