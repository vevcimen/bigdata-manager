/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.internal.Nullness;

public class StreamWrapper {
    private static final int MAX_MEMORY_BUFFER_BYTES = 51200;
    private static final String TEMP_FILE_PREFIX = "postgres-pgjdbc-stream";
    private final @Nullable InputStream stream;
    private final byte @Nullable [] rawData;
    private final int offset;
    private final int length;

    public StreamWrapper(byte[] data, int offset, int length) {
        this.stream = null;
        this.rawData = data;
        this.offset = offset;
        this.length = length;
    }

    public StreamWrapper(InputStream stream, int length) {
        this.stream = stream;
        this.rawData = null;
        this.offset = 0;
        this.length = length;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public StreamWrapper(InputStream stream) throws PSQLException {
        block8: {
            try {
                ByteArrayOutputStream memoryOutputStream = new ByteArrayOutputStream();
                int memoryLength = StreamWrapper.copyStream(stream, memoryOutputStream, 51200);
                byte[] rawData = memoryOutputStream.toByteArray();
                if (memoryLength == -1) {
                    int diskLength;
                    final File tempFile = Files.createTempFile(TEMP_FILE_PREFIX, null, new FileAttribute[0]).toFile();
                    diskOutputStream.write(rawData);
                    try (FileOutputStream diskOutputStream = new FileOutputStream(tempFile);){
                        diskLength = StreamWrapper.copyStream(stream, diskOutputStream, Integer.MAX_VALUE - rawData.length);
                        if (diskLength == -1) {
                            throw new PSQLException(GT.tr("Object is too large to send over the protocol.", new Object[0]), PSQLState.NUMERIC_CONSTANT_OUT_OF_RANGE);
                        }
                        diskOutputStream.flush();
                    }
                    this.offset = 0;
                    this.length = rawData.length + diskLength;
                    this.rawData = null;
                    this.stream = new FileInputStream(tempFile){
                        private boolean closed;
                        private int position;
                        {
                            super(x0);
                            this.closed = false;
                            this.position = 0;
                        }

                        private void checkShouldClose(int readResult) throws IOException {
                            if (readResult == -1) {
                                this.close();
                            } else {
                                this.position += readResult;
                                if (this.position >= StreamWrapper.this.length) {
                                    this.close();
                                }
                            }
                        }

                        @Override
                        public int read(byte[] b) throws IOException {
                            if (this.closed) {
                                return -1;
                            }
                            int result = super.read(b);
                            this.checkShouldClose(result);
                            return result;
                        }

                        @Override
                        public int read(byte[] b, int off, int len) throws IOException {
                            if (this.closed) {
                                return -1;
                            }
                            int result = super.read(b, off, len);
                            this.checkShouldClose(result);
                            return result;
                        }

                        @Override
                        public void close() throws IOException {
                            if (!this.closed) {
                                super.close();
                                tempFile.delete();
                                this.closed = true;
                            }
                        }

                        protected void finalize() throws IOException {
                            this.close();
                            try {
                                super.finalize();
                            }
                            catch (RuntimeException e) {
                                throw e;
                            }
                            catch (Error e) {
                                throw e;
                            }
                            catch (IOException e) {
                                throw e;
                            }
                            catch (Throwable e) {
                                throw new RuntimeException("Unexpected exception from finalize", e);
                            }
                        }
                    };
                    break block8;
                }
                this.rawData = rawData;
                this.stream = null;
                this.offset = 0;
                this.length = rawData.length;
            }
            catch (IOException e) {
                throw new PSQLException(GT.tr("An I/O error occurred while sending to the backend.", new Object[0]), PSQLState.IO_ERROR, (Throwable)e);
            }
        }
    }

    public InputStream getStream() {
        if (this.stream != null) {
            return this.stream;
        }
        return new ByteArrayInputStream(Nullness.castNonNull(this.rawData), this.offset, this.length);
    }

    public int getLength() {
        return this.length;
    }

    public int getOffset() {
        return this.offset;
    }

    public byte @Nullable [] getBytes() {
        return this.rawData;
    }

    public String toString() {
        return "<stream of " + this.length + " bytes>";
    }

    private static int copyStream(InputStream inputStream, OutputStream outputStream, int limit) throws IOException {
        int totalLength = 0;
        byte[] buffer = new byte[2048];
        int readLength = inputStream.read(buffer);
        while (readLength > 0) {
            outputStream.write(buffer, 0, readLength);
            if ((totalLength += readLength) >= limit) {
                return -1;
            }
            readLength = inputStream.read(buffer);
        }
        return totalLength;
    }
}

