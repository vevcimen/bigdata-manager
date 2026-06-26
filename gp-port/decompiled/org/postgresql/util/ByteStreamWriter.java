/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.util;

import java.io.IOException;
import java.io.OutputStream;

public interface ByteStreamWriter {
    public int getLength();

    public void writeTo(ByteStreamTarget var1) throws IOException;

    public static interface ByteStreamTarget {
        public OutputStream getOutputStream();
    }
}

