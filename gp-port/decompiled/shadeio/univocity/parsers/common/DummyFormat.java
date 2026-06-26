/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common;

import java.util.TreeMap;
import shadeio.univocity.parsers.common.Format;

final class DummyFormat
extends Format {
    static final DummyFormat instance = new DummyFormat();

    private DummyFormat() {
    }

    @Override
    protected final TreeMap<String, Object> getConfiguration() {
        return new TreeMap<String, Object>();
    }
}

