/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.conversions;

import shadeio.univocity.parsers.conversions.NullConversion;

public abstract class ObjectConversion<T>
extends NullConversion<String, T> {
    public ObjectConversion() {
        super(null, null);
    }

    public ObjectConversion(T valueIfStringIsNull, String valueIfObjectIsNull) {
        super(valueIfStringIsNull, valueIfObjectIsNull);
    }

    @Override
    public T execute(String input) {
        return (T)super.execute(input);
    }

    @Override
    protected final T fromInput(String input) {
        return this.fromString(input);
    }

    protected abstract T fromString(String var1);

    @Override
    public String revert(T input) {
        return (String)super.revert(input);
    }

    @Override
    protected final String undo(T input) {
        return String.valueOf(input);
    }

    public T getValueIfStringIsNull() {
        return (T)this.getValueOnNullInput();
    }

    public String getValueIfObjectIsNull() {
        return (String)this.getValueOnNullOutput();
    }

    public void setValueIfStringIsNull(T valueIfStringIsNull) {
        this.setValueOnNullInput(valueIfStringIsNull);
    }

    public void setValueIfObjectIsNull(String valueIfObjectIsNull) {
        this.setValueOnNullOutput(valueIfObjectIsNull);
    }
}

