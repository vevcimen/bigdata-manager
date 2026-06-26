/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import org.postgresql.util.ByteStreamWriter;

public class ByteBufferByteStreamWriter
implements ByteStreamWriter {
    private final ByteBuffer buf;
    private final int length;

    public ByteBufferByteStreamWriter(ByteBuffer buf) {
        this.buf = buf;
        this.length = buf.remaining();
    }

    @Override
    public int getLength() {
        return this.length;
    }

    @Override
    public void writeTo(ByteStreamWriter.ByteStreamTarget target) throws IOException {
        try (WritableByteChannel c = Channels.newChannel(target.getOutputStream());){
            c.write(this.buf);
        }
    }
}

