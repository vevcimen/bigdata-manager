/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server;

import java.io.IOException;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.annotation.ManagedObject;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.log.Slf4jLog;

@ManagedObject(value="Slf4j RequestLog Writer")
public class Slf4jRequestLogWriter
extends AbstractLifeCycle
implements RequestLog.Writer {
    private Slf4jLog logger;
    private String loggerName = "org.eclipse.jetty.server.RequestLog";

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    @ManagedAttribute(value="logger name")
    public String getLoggerName() {
        return this.loggerName;
    }

    protected boolean isEnabled() {
        return this.logger != null;
    }

    @Override
    public void write(String requestEntry) throws IOException {
        this.logger.info(requestEntry, new Object[0]);
    }

    @Override
    protected synchronized void doStart() throws Exception {
        this.logger = new Slf4jLog(this.loggerName);
        super.doStart();
    }
}

