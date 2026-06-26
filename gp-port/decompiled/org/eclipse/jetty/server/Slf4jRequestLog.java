/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server;

import java.io.IOException;
import org.eclipse.jetty.server.AbstractNCSARequestLog;
import org.eclipse.jetty.server.Slf4jRequestLogWriter;
import org.eclipse.jetty.util.annotation.ManagedObject;

@Deprecated
@ManagedObject(value="NCSA standard format request log to slf4j bridge")
public class Slf4jRequestLog
extends AbstractNCSARequestLog {
    private final Slf4jRequestLogWriter _requestLogWriter;

    public Slf4jRequestLog() {
        this(new Slf4jRequestLogWriter());
    }

    public Slf4jRequestLog(Slf4jRequestLogWriter writer) {
        super(writer);
        this._requestLogWriter = writer;
    }

    public void setLoggerName(String loggerName) {
        this._requestLogWriter.setLoggerName(loggerName);
    }

    public String getLoggerName() {
        return this._requestLogWriter.getLoggerName();
    }

    @Override
    protected boolean isEnabled() {
        return this._requestLogWriter.isEnabled();
    }

    @Override
    public void write(String requestEntry) throws IOException {
        this._requestLogWriter.write(requestEntry);
    }
}

