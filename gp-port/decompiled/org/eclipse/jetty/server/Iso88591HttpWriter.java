/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server;

import java.io.IOException;
import org.eclipse.jetty.server.HttpOutput;
import org.eclipse.jetty.server.HttpWriter;

public class Iso88591HttpWriter
extends HttpWriter {
    public Iso88591HttpWriter(HttpOutput out) {
        super(out);
    }

    @Override
    public void write(char[] s2, int offset, int length) throws IOException {
        HttpOutput out = this._out;
        if (length == 1) {
            int c = s2[offset];
            out.write(c < 256 ? c : 63);
            return;
        }
        while (length > 0) {
            this._bytes.reset();
            int chars = Math.min(length, 512);
            byte[] buffer = this._bytes.getBuf();
            int bytes = this._bytes.getCount();
            if (chars > buffer.length - bytes) {
                chars = buffer.length - bytes;
            }
            for (int i = 0; i < chars; ++i) {
                int c = s2[offset + i];
                buffer[bytes++] = (byte)(c < 256 ? c : 63);
            }
            if (bytes >= 0) {
                this._bytes.setCount(bytes);
            }
            this._bytes.writeTo(out);
            length -= chars;
            offset += chars;
        }
    }
}

