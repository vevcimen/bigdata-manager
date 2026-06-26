/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common;

import java.io.Reader;

class LineReader
extends Reader {
    private String line;
    private int length;
    private int next = 0;

    public void setLine(String line) {
        this.line = line;
        this.length = line.length();
        this.next = 0;
    }

    @Override
    public int read(char[] cbuf, int off, int len) {
        if (len == 0) {
            return 0;
        }
        if (this.next >= this.length) {
            return -1;
        }
        int read = Math.min(this.length - this.next, len);
        this.line.getChars(this.next, this.next + read, cbuf, off);
        this.next += read;
        return read;
    }

    @Override
    public long skip(long ns) {
        return 0L;
    }

    @Override
    public boolean ready() {
        return this.line != null;
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public void close() {
        this.line = null;
    }
}

