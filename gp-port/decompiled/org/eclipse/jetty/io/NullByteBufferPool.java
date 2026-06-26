/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.io;

import java.nio.ByteBuffer;
import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.util.BufferUtil;

public class NullByteBufferPool
implements ByteBufferPool {
    @Override
    public ByteBuffer acquire(int size, boolean direct) {
        if (direct) {
            return BufferUtil.allocateDirect(size);
        }
        return BufferUtil.allocate(size);
    }

    @Override
    public void release(ByteBuffer buffer) {
        BufferUtil.clear(buffer);
    }
}

