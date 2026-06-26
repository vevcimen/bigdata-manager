/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.conversions;

import shadeio.univocity.parsers.conversions.Conversion;

public abstract class NullConversion<I, O>
implements Conversion<I, O> {
    private O valueOnNullInput;
    private I valueOnNullOutput;

    public NullConversion() {
        this(null, null);
    }

    public NullConversion(O valueOnNullInput, I valueOnNullOutput) {
        this.valueOnNullInput = valueOnNullInput;
        this.valueOnNullOutput = valueOnNullOutput;
    }

    @Override
    public O execute(I input) {
        if (input == null) {
            return this.valueOnNullInput;
        }
        return this.fromInput(input);
    }

    protected abstract O fromInput(I var1);

    @Override
    public I revert(O input) {
        if (input == null) {
            return this.valueOnNullOutput;
        }
        return this.undo(input);
    }

    protected abstract I undo(O var1);

    public O getValueOnNullInput() {
        return this.valueOnNullInput;
    }

    public I getValueOnNullOutput() {
        return this.valueOnNullOutput;
    }

    public void setValueOnNullInput(O valueOnNullInput) {
        this.valueOnNullInput = valueOnNullInput;
    }

    public void setValueOnNullOutput(I valueOnNullOutput) {
        this.valueOnNullOutput = valueOnNullOutput;
    }
}

