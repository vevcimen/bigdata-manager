/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.fastpath;

import java.sql.SQLException;
import org.postgresql.core.ParameterList;

@Deprecated
public class FastpathArg {
    private final byte[] bytes;
    private final int bytesStart;
    private final int bytesLength;

    public FastpathArg(int value) {
        this.bytes = new byte[4];
        this.bytes[3] = (byte)value;
        this.bytes[2] = (byte)(value >> 8);
        this.bytes[1] = (byte)(value >> 16);
        this.bytes[0] = (byte)(value >> 24);
        this.bytesStart = 0;
        this.bytesLength = 4;
    }

    public FastpathArg(long value) {
        this.bytes = new byte[8];
        this.bytes[7] = (byte)value;
        this.bytes[6] = (byte)(value >> 8);
        this.bytes[5] = (byte)(value >> 16);
        this.bytes[4] = (byte)(value >> 24);
        this.bytes[3] = (byte)(value >> 32);
        this.bytes[2] = (byte)(value >> 40);
        this.bytes[1] = (byte)(value >> 48);
        this.bytes[0] = (byte)(value >> 56);
        this.bytesStart = 0;
        this.bytesLength = 8;
    }

    public FastpathArg(byte[] bytes) {
        this(bytes, 0, bytes.length);
    }

    public FastpathArg(byte[] buf, int off, int len) {
        this.bytes = buf;
        this.bytesStart = off;
        this.bytesLength = len;
    }

    public FastpathArg(String s2) {
        this(s2.getBytes());
    }

    void populateParameter(ParameterList params, int index) throws SQLException {
        if (this.bytes == null) {
            params.setNull(index, 0);
        } else {
            params.setBytea(index, this.bytes, this.bytesStart, this.bytesLength);
        }
    }
}

