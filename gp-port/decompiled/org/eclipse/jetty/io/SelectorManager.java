/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.io;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.io.ManagedSelector;
import org.eclipse.jetty.util.ProcessorUtils;
import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.annotation.ManagedObject;
import org.eclipse.jetty.util.component.ContainerLifeCycle;
import org.eclipse.jetty.util.component.Dumpable;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.thread.Scheduler;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.eclipse.jetty.util.thread.ThreadPoolBudget;

@ManagedObject(value="Manager of the NIO Selectors")
public abstract class SelectorManager
extends ContainerLifeCycle
implements Dumpable {
    public static final int DEFAULT_CONNECT_TIMEOUT = 15000;
    protected static final Logger LOG = Log.getLogger(SelectorManager.class);
    private final Executor executor;
    private final Scheduler scheduler;
    private final ManagedSelector[] _selectors;
    private final AtomicInteger _selectorIndex = new AtomicInteger();
    private final IntUnaryOperator _selectorIndexUpdate;
    private final List<AcceptListener> _acceptListeners = new ArrayList<AcceptListener>();
    private long _connectTimeout = 15000L;
    private ThreadPoolBudget.Lease _lease;

    private static int defaultSelectors(Executor executor) {
        if (executor instanceof ThreadPool.SizedThreadPool) {
            int threads = ((ThreadPool.SizedThreadPool)executor).getMaxThreads();
            int cpus = ProcessorUtils.availableProcessors();
            return Math.max(1, Math.min(cpus / 2, threads / 16));
        }
        return Math.max(1, ProcessorUtils.availableProcessors() / 2);
    }

    protected SelectorManager(Executor executor, Scheduler scheduler) {
        this(executor, scheduler, -1);
    }

    protected SelectorManager(Executor executor, Scheduler scheduler, int selectors) {
        if (selectors <= 0) {
            selectors = SelectorManager.defaultSelectors(executor);
        }
        this.executor = executor;
        this.scheduler = scheduler;
        this._selectors = new ManagedSelector[selectors];
        this._selectorIndexUpdate = index -> (index + 1) % this._selectors.length;
    }

    @ManagedAttribute(value="The Executor")
    public Executor getExecutor() {
        return this.executor;
    }

    @ManagedAttribute(value="The Scheduler")
    public Scheduler getScheduler() {
        return this.scheduler;
    }

    @ManagedAttribute(value="The Connection timeout (ms)")
    public long getConnectTimeout() {
        return this._connectTimeout;
    }

    public void setConnectTimeout(long milliseconds) {
        this._connectTimeout = milliseconds;
    }

    @Deprecated
    public int getReservedThreads() {
        return -1;
    }

    @Deprecated
    public void setReservedThreads(int threads) {
        throw new UnsupportedOperationException();
    }

    protected void execute(Runnable task) {
        this.executor.execute(task);
    }

    @ManagedAttribute(value="The number of NIO Selectors")
    public int getSelectorCount() {
        return this._selectors.length;
    }

    private ManagedSelector chooseSelector() {
        return this._selectors[this._selectorIndex.updateAndGet(this._selectorIndexUpdate)];
    }

    public void connect(SelectableChannel channel, Object attachment) {
        ManagedSelector set;
        ManagedSelector managedSelector = set = this.chooseSelector();
        Objects.requireNonNull(managedSelector);
        set.submit(managedSelector.new ManagedSelector.Connect(channel, attachment));
    }

    public void accept(SelectableChannel channel) {
        this.accept(channel, null);
    }

    public void accept(SelectableChannel channel, Object attachment) {
        ManagedSelector selector;
        ManagedSelector managedSelector = selector = this.chooseSelector();
        Objects.requireNonNull(managedSelector);
        selector.submit(managedSelector.new ManagedSelector.Accept(channel, attachment));
    }

    public Closeable acceptor(SelectableChannel server) {
        ManagedSelector selector;
        ManagedSelector managedSelector = selector = this.chooseSelector();
        Objects.requireNonNull(managedSelector);
        ManagedSelector.Acceptor acceptor = managedSelector.new ManagedSelector.Acceptor(server);
        selector.submit(acceptor);
        return acceptor;
    }

    protected void accepted(SelectableChannel channel) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void doStart() throws Exception {
        this._lease = ThreadPoolBudget.leaseFrom(this.getExecutor(), this, this._selectors.length);
        for (int i = 0; i < this._selectors.length; ++i) {
            ManagedSelector selector;
            this._selectors[i] = selector = this.newSelector(i);
            this.addBean(selector);
        }
        super.doStart();
    }

    protected ManagedSelector newSelector(int id) {
        return new ManagedSelector(this, id);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void doStop() throws Exception {
        try {
            super.doStop();
        }
        finally {
            for (ManagedSelector selector : this._selectors) {
                if (selector == null) continue;
                this.removeBean(selector);
            }
            Arrays.fill(this._selectors, null);
            if (this._lease != null) {
                this._lease.close();
            }
        }
    }

    protected void endPointOpened(EndPoint endpoint) {
    }

    protected void endPointClosed(EndPoint endpoint) {
    }

    public void connectionOpened(Connection connection) {
        try {
            connection.onOpen();
        }
        catch (Throwable x) {
            if (this.isRunning()) {
                LOG.warn("Exception while notifying connection " + connection, x);
            } else {
                LOG.debug("Exception while notifying connection " + connection, x);
            }
            throw x;
        }
    }

    public void connectionClosed(Connection connection) {
        try {
            connection.onClose();
        }
        catch (Throwable x) {
            LOG.debug("Exception while notifying connection " + connection, x);
        }
    }

    protected boolean doFinishConnect(SelectableChannel channel) throws IOException {
        return ((SocketChannel)channel).finishConnect();
    }

    protected boolean isConnectionPending(SelectableChannel channel) {
        return ((SocketChannel)channel).isConnectionPending();
    }

    protected SelectableChannel doAccept(SelectableChannel server) throws IOException {
        return ((ServerSocketChannel)server).accept();
    }

    protected void connectionFailed(SelectableChannel channel, Throwable ex, Object attachment) {
        LOG.warn(String.format("%s - %s", channel, attachment), ex);
    }

    protected Selector newSelector() throws IOException {
        return Selector.open();
    }

    protected abstract EndPoint newEndPoint(SelectableChannel var1, ManagedSelector var2, SelectionKey var3) throws IOException;

    public abstract Connection newConnection(SelectableChannel var1, EndPoint var2, Object var3) throws IOException;

    public void addEventListener(EventListener listener) {
        if (this.isRunning()) {
            throw new IllegalStateException(this.toString());
        }
        if (listener instanceof AcceptListener) {
            this.addAcceptListener((AcceptListener)listener);
        }
    }

    public void removeEventListener(EventListener listener) {
        if (this.isRunning()) {
            throw new IllegalStateException(this.toString());
        }
        if (listener instanceof AcceptListener) {
            this.removeAcceptListener((AcceptListener)listener);
        }
    }

    public void addAcceptListener(AcceptListener listener) {
        if (!this._acceptListeners.contains(listener)) {
            this._acceptListeners.add(listener);
        }
    }

    public void removeAcceptListener(AcceptListener listener) {
        this._acceptListeners.remove(listener);
    }

    protected void onAccepting(SelectableChannel channel) {
        for (AcceptListener l : this._acceptListeners) {
            try {
                l.onAccepting(channel);
            }
            catch (Throwable x) {
                LOG.warn(x);
            }
        }
    }

    protected void onAcceptFailed(SelectableChannel channel, Throwable cause) {
        for (AcceptListener l : this._acceptListeners) {
            try {
                l.onAcceptFailed(channel, cause);
            }
            catch (Throwable x) {
                LOG.warn(x);
            }
        }
    }

    protected void onAccepted(SelectableChannel channel) {
        for (AcceptListener l : this._acceptListeners) {
            try {
                l.onAccepted(channel);
            }
            catch (Throwable x) {
                LOG.warn(x);
            }
        }
    }

    public static interface AcceptListener
    extends EventListener {
        default public void onAccepting(SelectableChannel channel) {
        }

        default public void onAcceptFailed(SelectableChannel channel, Throwable cause) {
        }

        default public void onAccepted(SelectableChannel channel) {
        }
    }
}

