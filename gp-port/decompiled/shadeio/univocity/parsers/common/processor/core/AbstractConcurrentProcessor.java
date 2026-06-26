/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.processor.core;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import shadeio.univocity.parsers.common.Context;
import shadeio.univocity.parsers.common.DataProcessingException;
import shadeio.univocity.parsers.common.processor.core.Processor;

public abstract class AbstractConcurrentProcessor<T extends Context>
implements Processor<T> {
    private final Processor processor;
    private boolean ended = false;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile long rowCount;
    private Future<Void> process;
    private T currentContext;
    private Node<T> inputQueue;
    private volatile Node<T> outputQueue;
    private final int limit;
    private volatile long input;
    private volatile long output;
    private final Object lock;
    private boolean contextCopyingEnabled = false;

    public AbstractConcurrentProcessor(Processor<T> processor) {
        this(processor, -1);
    }

    public AbstractConcurrentProcessor(Processor<T> processor, int limit) {
        if (processor == null) {
            throw new IllegalArgumentException("Row processor cannot be null");
        }
        this.processor = processor;
        this.input = 0L;
        this.output = 0L;
        this.lock = new Object();
        this.limit = limit;
    }

    public boolean isContextCopyingEnabled() {
        return this.contextCopyingEnabled;
    }

    public void setContextCopyingEnabled(boolean contextCopyingEnabled) {
        this.contextCopyingEnabled = contextCopyingEnabled;
    }

    @Override
    public final void processStarted(T context) {
        this.currentContext = this.wrapContext(context);
        this.processor.processStarted(this.currentContext);
        this.startProcess();
    }

    private void startProcess() {
        this.ended = false;
        this.rowCount = 0L;
        this.process = this.executor.submit(new Callable<Void>(){

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public Void call() {
                while (AbstractConcurrentProcessor.this.outputQueue == null && !AbstractConcurrentProcessor.this.ended) {
                    Thread.yield();
                }
                while (!AbstractConcurrentProcessor.this.ended) {
                    AbstractConcurrentProcessor.this.rowCount++;
                    AbstractConcurrentProcessor.this.processor.rowProcessed(((AbstractConcurrentProcessor)AbstractConcurrentProcessor.this).outputQueue.row, (Context)((AbstractConcurrentProcessor)AbstractConcurrentProcessor.this).outputQueue.context);
                    while (((AbstractConcurrentProcessor)AbstractConcurrentProcessor.this).outputQueue.next == null) {
                        if (AbstractConcurrentProcessor.this.ended && ((AbstractConcurrentProcessor)AbstractConcurrentProcessor.this).outputQueue.next == null) {
                            return null;
                        }
                        Thread.yield();
                    }
                    AbstractConcurrentProcessor.this.outputQueue = ((AbstractConcurrentProcessor)AbstractConcurrentProcessor.this).outputQueue.next;
                    AbstractConcurrentProcessor.this.output++;
                    if (AbstractConcurrentProcessor.this.limit <= 1) continue;
                    Object object = AbstractConcurrentProcessor.this.lock;
                    synchronized (object) {
                        AbstractConcurrentProcessor.this.lock.notify();
                    }
                }
                while (AbstractConcurrentProcessor.this.outputQueue != null) {
                    AbstractConcurrentProcessor.this.rowCount++;
                    AbstractConcurrentProcessor.this.processor.rowProcessed(((AbstractConcurrentProcessor)AbstractConcurrentProcessor.this).outputQueue.row, (Context)((AbstractConcurrentProcessor)AbstractConcurrentProcessor.this).outputQueue.context);
                    AbstractConcurrentProcessor.this.outputQueue = ((AbstractConcurrentProcessor)AbstractConcurrentProcessor.this).outputQueue.next;
                }
                return null;
            }
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final void rowProcessed(String[] row, T context) {
        if (this.inputQueue == null) {
            this.inputQueue = new Node<T>(row, this.grabContext(context));
            this.outputQueue = this.inputQueue;
        } else {
            if (this.limit > 1) {
                Object object = this.lock;
                synchronized (object) {
                    try {
                        if (this.input - this.output >= (long)this.limit) {
                            this.lock.wait();
                        }
                    }
                    catch (InterruptedException e) {
                        this.ended = true;
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
            this.inputQueue.next = new Node<T>(row, this.grabContext(context));
            this.inputQueue = this.inputQueue.next;
        }
        ++this.input;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final void processEnded(T context) {
        this.ended = true;
        if (this.limit > 1) {
            Object object = this.lock;
            synchronized (object) {
                this.lock.notify();
            }
        }
        try {
            this.process.get();
        }
        catch (ExecutionException e) {
            throw new DataProcessingException("Error executing process", (Throwable)e);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        finally {
            try {
                this.processor.processEnded(this.grabContext(context));
            }
            finally {
                this.executor.shutdown();
            }
        }
    }

    private T grabContext(T context) {
        if (this.contextCopyingEnabled) {
            return this.copyContext(context);
        }
        return this.currentContext;
    }

    protected final long getRowCount() {
        return this.rowCount;
    }

    protected abstract T copyContext(T var1);

    protected abstract T wrapContext(T var1);

    private static class Node<T> {
        public final T context;
        public final String[] row;
        public Node next;

        public Node(String[] row, T context) {
            this.row = row;
            this.context = context;
        }
    }
}

