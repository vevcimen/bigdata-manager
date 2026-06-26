/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.fields;

import shadeio.univocity.parsers.common.NormalizedString;

public interface FieldSelector
extends Cloneable {
    public int[] getFieldIndexes(String[] var1);

    public int[] getFieldIndexes(NormalizedString[] var1);

    public String describe();

    public Object clone();
}

