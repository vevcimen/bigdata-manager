/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server;

import java.util.concurrent.BlockingQueue;
import org.eclipse.jetty.server.AsyncRequestLogWriter;
import org.eclipse.jetty.server.NCSARequestLog;

@Deprecated
public class AsyncNCSARequestLog
extends NCSARequestLog {
    public AsyncNCSARequestLog() {
        this(null, null);
    }

    public AsyncNCSARequestLog(String filename, BlockingQueue<String> queue) {
        super(new AsyncRequestLogWriter(filename, queue));
    }
}

