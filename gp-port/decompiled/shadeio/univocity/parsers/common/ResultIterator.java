/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common;

import java.util.Iterator;
import shadeio.univocity.parsers.common.Context;

public interface ResultIterator<T, C extends Context>
extends Iterator<T> {
    public C getContext();
}

