/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.conversions;

public interface Conversion<I, O> {
    public O execute(I var1);

    public I revert(O var1);
}

