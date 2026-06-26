/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.conversions;

import shadeio.univocity.parsers.conversions.NullConversion;

public class ToStringConversion
extends NullConversion<Object, Object> {
    public ToStringConversion() {
    }

    public ToStringConversion(Object valueOnNullInput, Object valueOnNullOutput) {
        super(valueOnNullInput, valueOnNullOutput);
    }

    @Override
    protected Object fromInput(Object input) {
        if (input != null) {
            return input.toString();
        }
        return null;
    }

    @Override
    protected Object undo(Object input) {
        return this.execute(input);
    }
}

