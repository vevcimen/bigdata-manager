/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server;

import java.io.IOException;
import org.eclipse.jetty.server.AbstractNCSARequestLog;
import org.eclipse.jetty.server.RequestLogWriter;
import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.annotation.ManagedObject;

@Deprecated
@ManagedObject(value="NCSA standard format request log")
public class NCSARequestLog
extends AbstractNCSARequestLog {
    private final RequestLogWriter _requestLogWriter;

    public NCSARequestLog() {
        this((String)null);
    }

    public NCSARequestLog(String filename) {
        this(new RequestLogWriter(filename));
    }

    public NCSARequestLog(RequestLogWriter writer) {
        super(writer);
        this._requestLogWriter = writer;
        this.setExtended(true);
    }

    public void setFilename(String filename) {
        this._requestLogWriter.setFilename(filename);
    }

    @Override
    public void setLogTimeZone(String tz) {
        super.setLogTimeZone(tz);
        this._requestLogWriter.setTimeZone(tz);
    }

    @ManagedAttribute(value="file of log")
    public String getFilename() {
        return this._requestLogWriter.getFileName();
    }

    public String getDatedFilename() {
        return this._requestLogWriter.getDatedFilename();
    }

    @Override
    protected boolean isEnabled() {
        return this._requestLogWriter.isEnabled();
    }

    public void setRetainDays(int retainDays) {
        this._requestLogWriter.setRetainDays(retainDays);
    }

    @ManagedAttribute(value="number of days that log files are kept")
    public int getRetainDays() {
        return this._requestLogWriter.getRetainDays();
    }

    public void setAppend(boolean append) {
        this._requestLogWriter.setAppend(append);
    }

    @ManagedAttribute(value="existing log files are appends to the new one")
    public boolean isAppend() {
        return this._requestLogWriter.isAppend();
    }

    public void setFilenameDateFormat(String logFileDateFormat) {
        this._requestLogWriter.setFilenameDateFormat(logFileDateFormat);
    }

    public String getFilenameDateFormat() {
        return this._requestLogWriter.getFilenameDateFormat();
    }

    @Override
    public void write(String requestEntry) throws IOException {
        this._requestLogWriter.write(requestEntry);
    }

    @Override
    protected synchronized void doStart() throws Exception {
        super.doStart();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void doStop() throws Exception {
        NCSARequestLog nCSARequestLog = this;
        synchronized (nCSARequestLog) {
            super.doStop();
        }
    }
}

