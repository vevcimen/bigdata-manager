/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.input;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import shadeio.univocity.parsers.common.ArgumentUtils;

public final class BomInput
extends InputStream {
    public static final byte[] UTF_8_BOM = ArgumentUtils.toByteArray(239, 187, 191);
    public static final byte[] UTF_16BE_BOM = ArgumentUtils.toByteArray(254, 255);
    public static final byte[] UTF_16LE_BOM = ArgumentUtils.toByteArray(255, 254);
    public static final byte[] UTF_32BE_BOM = ArgumentUtils.toByteArray(0, 0, 254, 255);
    public static final byte[] UTF_32LE_BOM = ArgumentUtils.toByteArray(255, 254, 0, 0);
    private int bytesRead;
    private int[] bytes = new int[4];
    private String encoding;
    private int consumed = 0;
    private final InputStream input;
    private IOException exception;

    public BomInput(InputStream input) {
        this.input = input;
        try {
            this.bytes[0] = this.next();
            if (this.bytes[0] == 239) {
                this.bytes[1] = this.next();
                if (this.bytes[1] == 187 && (this.bytes[2] = this.next()) == 191) {
                    this.setEncoding("UTF-8");
                }
            } else if (this.bytes[0] == 254) {
                this.bytes[1] = this.next();
                if (this.bytes[1] == 255) {
                    this.setEncoding("UTF-16BE");
                }
            } else if (this.bytes[0] == 255) {
                this.bytes[1] = this.next();
                if (this.bytes[1] == 254) {
                    this.bytes[2] = this.next();
                    if (this.bytes[2] == 0) {
                        this.bytes[3] = this.next();
                        if (this.bytes[3] == 0) {
                            this.setEncoding("UTF-32LE");
                        } else {
                            this.setEncoding("UTF-16LE");
                        }
                    } else {
                        this.setEncoding("UTF-16LE");
                    }
                }
            } else if (this.bytes[0] == 0 && (this.bytes[1] = this.next()) == 0 && (this.bytes[2] = this.next()) == 254 && (this.bytes[3] = this.next()) == 255) {
                this.setEncoding("UTF-32BE");
            }
        }
        catch (IOException e) {
            this.exception = e;
        }
    }

    private void setEncoding(String encoding) {
        this.encoding = encoding;
        if (encoding.equals("UTF-16LE")) {
            if (this.bytesRead == 3) {
                this.bytesRead = 1;
                this.bytes[0] = this.bytes[2];
                try {
                    this.bytes[1] = this.next();
                }
                catch (Exception e) {
                    this.exception = (IOException)e;
                }
                return;
            }
            if (this.bytesRead == 4) {
                this.bytesRead = 2;
                this.bytes[0] = this.bytes[2];
                this.bytes[1] = this.bytes[3];
                return;
            }
        }
        this.bytesRead = 0;
    }

    private int next() throws IOException {
        int out = this.input.read();
        ++this.bytesRead;
        return out;
    }

    @Override
    public final int read() throws IOException {
        if (this.bytesRead > 0 && this.bytesRead > this.consumed) {
            int out = this.bytes[this.consumed];
            if (++this.consumed == this.bytesRead && this.exception != null) {
                throw this.exception;
            }
            return out;
        }
        if (this.consumed == this.bytesRead) {
            ++this.consumed;
            return -1;
        }
        throw new BytesProcessedNotification(this.input, this.encoding);
    }

    public final boolean hasBytesStored() {
        return this.bytesRead > 0;
    }

    public final Charset getCharset() {
        if (this.encoding == null) {
            return null;
        }
        return Charset.forName(this.encoding);
    }

    public final String getEncoding() {
        return this.encoding;
    }

    @Override
    public void close() throws IOException {
        this.input.close();
    }

    public static final class BytesProcessedNotification
    extends RuntimeException {
        public final InputStream input;
        public final String encoding;

        public BytesProcessedNotification(InputStream input, String encoding) {
            this.input = input;
            this.encoding = encoding;
        }

        @Override
        public Throwable fillInStackTrace() {
            return this;
        }
    }
}

