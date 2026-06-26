/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common;

import shadeio.univocity.parsers.common.Context;
import shadeio.univocity.parsers.common.ResultIterator;

public interface IterableResult<T, C extends Context>
extends Iterable<T> {
    public C getContext();

    public ResultIterator<T, C> iterator();
}

