/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server.resource;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import org.eclipse.jetty.server.resource.RangeWriter;
import org.eclipse.jetty.util.BufferUtil;

public class ByteBufferRangeWriter
implements RangeWriter {
    private final ByteBuffer buffer;
    private boolean closed = false;

    public ByteBufferRangeWriter(ByteBuffer buffer) {
        this.buffer = buffer.asReadOnlyBuffer();
    }

    @Override
    public void close() throws IOException {
        this.closed = true;
    }

    @Override
    public void writeTo(OutputStream outputStream, long skipTo, long length) throws IOException {
        if (skipTo > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Unsupported skipTo " + skipTo + " > " + Integer.MAX_VALUE);
        }
        if (length > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Unsupported length " + skipTo + " > " + Integer.MAX_VALUE);
        }
        ByteBuffer src = this.buffer.slice();
        src.position((int)skipTo);
        src.limit(Math.addExact((int)skipTo, (int)length));
        BufferUtil.writeTo(src, outputStream);
    }
}

