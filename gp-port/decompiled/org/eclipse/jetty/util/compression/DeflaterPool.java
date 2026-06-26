/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.util.compression;

import java.util.zip.Deflater;
import org.eclipse.jetty.util.compression.CompressionPool;

public class DeflaterPool
extends CompressionPool<Deflater> {
    private final int compressionLevel;
    private final boolean nowrap;

    public DeflaterPool(int capacity, int compressionLevel, boolean nowrap) {
        super(capacity);
        this.compressionLevel = compressionLevel;
        this.nowrap = nowrap;
    }

    @Override
    protected Deflater newObject() {
        return new Deflater(this.compressionLevel, this.nowrap);
    }

    @Override
    protected void end(Deflater deflater) {
        deflater.end();
    }

    @Override
    protected void reset(Deflater deflater) {
        deflater.reset();
    }
}

