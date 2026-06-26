/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.eclipse.jetty.server.RequestLogWriter;
import org.eclipse.jetty.util.BlockingArrayQueue;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class AsyncRequestLogWriter
extends RequestLogWriter {
    private static final Logger LOG = Log.getLogger(AsyncRequestLogWriter.class);
    private final BlockingQueue<String> _queue;
    private transient WriterThread _thread;
    private boolean _warnedFull;

    public AsyncRequestLogWriter() {
        this(null, null);
    }

    public AsyncRequestLogWriter(String filename) {
        this(filename, null);
    }

    public AsyncRequestLogWriter(String filename, BlockingQueue<String> queue) {
        super(filename);
        if (queue == null) {
            queue = new BlockingArrayQueue<String>(1024);
        }
        this._queue = queue;
    }

    @Override
    protected synchronized void doStart() throws Exception {
        super.doStart();
        this._thread = new WriterThread();
        this._thread.start();
    }

    @Override
    protected void doStop() throws Exception {
        this._thread.interrupt();
        this._thread.join();
        super.doStop();
        this._thread = null;
    }

    @Override
    public void write(String log) throws IOException {
        if (!this._queue.offer(log)) {
            if (this._warnedFull) {
                LOG.warn("Log Queue overflow", new Object[0]);
            }
            this._warnedFull = true;
        }
    }

    private class WriterThread
    extends Thread {
        WriterThread() {
            this.setName("AsyncRequestLogWriter@" + Integer.toString(AsyncRequestLogWriter.this.hashCode(), 16));
        }

        @Override
        public void run() {
            while (AsyncRequestLogWriter.this.isRunning()) {
                try {
                    String log = (String)AsyncRequestLogWriter.this._queue.poll(10L, TimeUnit.SECONDS);
                    if (log != null) {
                        AsyncRequestLogWriter.super.write(log);
                    }
                    while (!AsyncRequestLogWriter.this._queue.isEmpty()) {
                        log = (String)AsyncRequestLogWriter.this._queue.poll();
                        if (log == null) continue;
                        AsyncRequestLogWriter.super.write(log);
                    }
                }
                catch (InterruptedException e) {
                    LOG.ignore(e);
                }
                catch (Throwable t) {
                    LOG.warn(t);
                }
            }
        }
    }
}

