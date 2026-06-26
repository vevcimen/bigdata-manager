/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.util;

import java.io.Writer;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import org.checkerframework.checker.nullness.qual.Nullable;

public class LogWriterHandler
extends Handler {
    private @Nullable Writer writer;
    private final Object lock = new Object();

    public LogWriterHandler(Writer inWriter) {
        this.setLevel(Level.INFO);
        this.setFilter(null);
        this.setFormatter(new SimpleFormatter());
        this.setWriter(inWriter);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void publish(LogRecord record) {
        String formatted;
        Formatter formatter = this.getFormatter();
        try {
            formatted = formatter.format(record);
        }
        catch (Exception ex) {
            this.reportError("Error Formatting record", ex, 5);
            return;
        }
        if (formatted.length() == 0) {
            return;
        }
        try {
            Object ex = this.lock;
            synchronized (ex) {
                Writer writer = this.writer;
                if (writer != null) {
                    writer.write(formatted);
                }
            }
        }
        catch (Exception ex) {
            this.reportError("Error writing message", ex, 1);
        }
    }

    @Override
    public void flush() {
        try {
            Writer writer = this.writer;
            if (writer != null) {
                writer.flush();
            }
        }
        catch (Exception ex) {
            this.reportError("Error on flush", ex, 1);
        }
    }

    @Override
    public void close() throws SecurityException {
        try {
            Writer writer = this.writer;
            if (writer != null) {
                writer.close();
            }
        }
        catch (Exception ex) {
            this.reportError("Error closing writer", ex, 1);
        }
    }

    private void setWriter(Writer writer) throws IllegalArgumentException {
        if (writer == null) {
            throw new IllegalArgumentException("Writer cannot be null");
        }
        this.writer = writer;
        try {
            writer.write(this.getFormatter().getHead(this));
        }
        catch (Exception ex) {
            this.reportError("Error writing head section", ex, 1);
        }
    }
}

