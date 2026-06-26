/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.util.compression;

import java.util.zip.Inflater;
import org.eclipse.jetty.util.compression.CompressionPool;

public class InflaterPool
extends CompressionPool<Inflater> {
    private final boolean nowrap;

    public InflaterPool(int capacity, boolean nowrap) {
        super(capacity);
        this.nowrap = nowrap;
    }

    @Override
    protected Inflater newObject() {
        return new Inflater(this.nowrap);
    }

    @Override
    protected void end(Inflater inflater) {
        inflater.end();
    }

    @Override
    protected void reset(Inflater inflater) {
        inflater.reset();
    }
}

