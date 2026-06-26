/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.eclipse.jetty.server.resource.RangeWriter;
import org.eclipse.jetty.util.IO;

public class InputStreamRangeWriter
implements RangeWriter {
    public static final int NO_PROGRESS_LIMIT = 3;
    private final InputStreamSupplier inputStreamSupplier;
    private boolean closed = false;
    private InputStream inputStream;
    private long pos;

    public InputStreamRangeWriter(InputStreamSupplier inputStreamSupplier) {
        this.inputStreamSupplier = inputStreamSupplier;
    }

    @Override
    public void close() throws IOException {
        this.closed = true;
        if (this.inputStream != null) {
            this.inputStream.close();
        }
    }

    @Override
    public void writeTo(OutputStream outputStream, long skipTo, long length) throws IOException {
        if (this.closed) {
            throw new IOException("RangeWriter is closed");
        }
        if (this.inputStream == null) {
            this.inputStream = this.inputStreamSupplier.newInputStream();
            this.pos = 0L;
        }
        if (skipTo < this.pos) {
            this.inputStream.close();
            this.inputStream = this.inputStreamSupplier.newInputStream();
            this.pos = 0L;
        }
        if (this.pos < skipTo) {
            long skipSoFar = this.pos;
            int noProgressLoopLimit = 3;
            while (noProgressLoopLimit > 0 && skipSoFar < skipTo) {
                long actualSkipped = this.inputStream.skip(skipTo - skipSoFar);
                if (actualSkipped == 0L) {
                    --noProgressLoopLimit;
                    continue;
                }
                if (actualSkipped > 0L) {
                    skipSoFar += actualSkipped;
                    noProgressLoopLimit = 3;
                    continue;
                }
                throw new IOException("EOF reached before InputStream skip destination");
            }
            if (noProgressLoopLimit <= 0) {
                throw new IOException("No progress made to reach InputStream skip position " + (skipTo - this.pos));
            }
            this.pos = skipTo;
        }
        IO.copy(this.inputStream, outputStream, length);
        this.pos += length;
    }

    public static interface InputStreamSupplier {
        public InputStream newInputStream() throws IOException;
    }
}

