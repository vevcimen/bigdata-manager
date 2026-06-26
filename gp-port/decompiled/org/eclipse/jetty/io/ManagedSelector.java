/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.io;

import java.io.Closeable;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.io.SelectorManager;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.annotation.ManagedOperation;
import org.eclipse.jetty.util.component.ContainerLifeCycle;
import org.eclipse.jetty.util.component.Dumpable;
import org.eclipse.jetty.util.component.DumpableCollection;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.statistic.SampleStatistic;
import org.eclipse.jetty.util.thread.ExecutionStrategy;
import org.eclipse.jetty.util.thread.Scheduler;
import org.eclipse.jetty.util.thread.strategy.EatWhatYouKill;

public class ManagedSelector
extends ContainerLifeCycle
implements Dumpable {
    private static final Logger LOG = Log.getLogger(ManagedSelector.class);
    private static final boolean FORCE_SELECT_NOW;
    private final AtomicBoolean _started = new AtomicBoolean(false);
    private boolean _selecting;
    private final SelectorManager _selectorManager;
    private final int _id;
    private final ExecutionStrategy _strategy;
    private Selector _selector;
    private Deque<SelectorUpdate> _updates = new ArrayDeque<SelectorUpdate>();
    private Deque<SelectorUpdate> _updateable = new ArrayDeque<SelectorUpdate>();
    private final SampleStatistic _keyStats = new SampleStatistic();

    public ManagedSelector(SelectorManager selectorManager, int id) {
        this._selectorManager = selectorManager;
        this._id = id;
        SelectorProducer producer = new SelectorProducer();
        Executor executor = selectorManager.getExecutor();
        this._strategy = new EatWhatYouKill(producer, executor);
        this.addBean(this._strategy, true);
        this.setStopTimeout(5000L);
    }

    public Selector getSelector() {
        return this._selector;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        this._selector = this._selectorManager.newSelector();
        this._selectorManager.execute(this._strategy::produce);
        Start start = new Start();
        this.submit(start);
        start._started.await();
    }

    @Override
    protected void doStop() throws Exception {
        if (this._started.compareAndSet(true, false) && this._selector != null) {
            CloseConnections closeConnections = new CloseConnections();
            this.submit(closeConnections);
            closeConnections._complete.await();
            StopSelector stopSelector = new StopSelector();
            this.submit(stopSelector);
            stopSelector._stopped.await();
        }
        super.doStop();
    }

    @ManagedAttribute(value="Total number of keys", readonly=true)
    public int getTotalKeys() {
        return this._selector.keys().size();
    }

    @ManagedAttribute(value="Average number of selected keys", readonly=true)
    public double getAverageSelectedKeys() {
        return this._keyStats.getMean();
    }

    @ManagedAttribute(value="Maximum number of selected keys", readonly=true)
    public double getMaxSelectedKeys() {
        return this._keyStats.getMax();
    }

    @ManagedAttribute(value="Total number of select() calls", readonly=true)
    public long getSelectCount() {
        return this._keyStats.getCount();
    }

    @ManagedOperation(value="Resets the statistics", impact="ACTION")
    public void resetStats() {
        this._keyStats.reset();
    }

    protected int nioSelect(Selector selector, boolean now) throws IOException {
        return now ? selector.selectNow() : selector.select();
    }

    protected int select(Selector selector) throws IOException {
        try {
            int selected = this.nioSelect(selector, false);
            if (selected == 0) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Selector {} woken with none selected", selector);
                }
                if (Thread.interrupted() && !this.isRunning()) {
                    throw new ClosedSelectorException();
                }
                if (FORCE_SELECT_NOW) {
                    selected = this.nioSelect(selector, true);
                }
            }
            return selected;
        }
        catch (ClosedSelectorException x) {
            throw x;
        }
        catch (Throwable x) {
            this.handleSelectFailure(selector, x);
            return 0;
        }
    }

    protected void handleSelectFailure(Selector selector, Throwable failure) throws IOException {
        LOG.info("Caught select() failure, trying to recover: {}", failure.toString());
        if (LOG.isDebugEnabled()) {
            LOG.debug(failure);
        }
        Selector newSelector = this._selectorManager.newSelector();
        for (SelectionKey oldKey : selector.keys()) {
            SelectableChannel channel = oldKey.channel();
            int interestOps = ManagedSelector.safeInterestOps(oldKey);
            if (interestOps >= 0) {
                try {
                    Object attachment = oldKey.attachment();
                    SelectionKey newKey = channel.register(newSelector, interestOps, attachment);
                    if (attachment instanceof Selectable) {
                        ((Selectable)attachment).replaceKey(newKey);
                    }
                    oldKey.cancel();
                    if (!LOG.isDebugEnabled()) continue;
                    LOG.debug("Transferred {} iOps={} att={}", channel, interestOps, attachment);
                }
                catch (Throwable t) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Could not transfer {}", channel, t);
                    }
                    IO.close(channel);
                }
                continue;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Invalid interestOps for {}", channel);
            }
            IO.close(channel);
        }
        IO.close(selector);
        this._selector = newSelector;
    }

    protected void onSelectFailed(Throwable cause) {
    }

    public int size() {
        Selector s2 = this._selector;
        if (s2 == null) {
            return 0;
        }
        Set<SelectionKey> keys = s2.keys();
        if (keys == null) {
            return 0;
        }
        return keys.size();
    }

    public void submit(SelectorUpdate update) {
        this.submit(update, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void submit(SelectorUpdate update, boolean lazy) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Queued change lazy={} {} on {}", lazy, update, this);
        }
        Selector selector = null;
        ManagedSelector managedSelector = this;
        synchronized (managedSelector) {
            this._updates.offer(update);
            if (this._selecting && !lazy) {
                selector = this._selector;
                this._selecting = false;
            }
        }
        if (selector != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Wakeup on submit {}", this);
            }
            selector.wakeup();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void wakeup() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Wakeup {}", this);
        }
        Selector selector = null;
        ManagedSelector managedSelector = this;
        synchronized (managedSelector) {
            if (this._selecting) {
                selector = this._selector;
                this._selecting = false;
            }
        }
        if (selector != null) {
            selector.wakeup();
        }
    }

    private void execute(Runnable task) {
        block2: {
            try {
                this._selectorManager.execute(task);
            }
            catch (RejectedExecutionException x) {
                if (!(task instanceof Closeable)) break block2;
                IO.close((Closeable)((Object)task));
            }
        }
    }

    private void processConnect(SelectionKey key, Connect connect) {
        SelectableChannel channel = key.channel();
        try {
            key.attach(connect.attachment);
            boolean connected = this._selectorManager.doFinishConnect(channel);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Connected {} {}", connected, channel);
            }
            if (connected) {
                if (!connect.timeout.cancel()) {
                    throw new SocketTimeoutException("Concurrent Connect Timeout");
                }
            } else {
                throw new ConnectException();
            }
            key.interestOps(0);
            this.execute(new CreateEndPoint(connect, key));
        }
        catch (Throwable x) {
            connect.failed(x);
        }
    }

    protected void endPointOpened(EndPoint endPoint) {
        this._selectorManager.endPointOpened(endPoint);
    }

    protected void endPointClosed(EndPoint endPoint) {
        this._selectorManager.endPointClosed(endPoint);
    }

    private void createEndPoint(SelectableChannel channel, SelectionKey selectionKey) throws IOException {
        EndPoint endPoint = this._selectorManager.newEndPoint(channel, this, selectionKey);
        Connection connection = this._selectorManager.newConnection(channel, endPoint, selectionKey.attachment());
        endPoint.setConnection(connection);
        this.submit(selector -> {
            SelectionKey key = selectionKey;
            if (key.selector() != selector && (key = channel.keyFor(selector)) != null && endPoint instanceof Selectable) {
                ((Selectable)((Object)endPoint)).replaceKey(key);
            }
            if (key != null) {
                key.attach(endPoint);
            }
        }, true);
        endPoint.onOpen();
        this.endPointOpened(endPoint);
        this._selectorManager.connectionOpened(connection);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Created {}", endPoint);
        }
    }

    void destroyEndPoint(EndPoint endPoint) {
        this.wakeup();
        this.execute(new DestroyEndPoint(endPoint));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int getActionSize() {
        ManagedSelector managedSelector = this;
        synchronized (managedSelector) {
            return this._updates.size();
        }
    }

    static int safeReadyOps(SelectionKey selectionKey) {
        try {
            return selectionKey.readyOps();
        }
        catch (Throwable x) {
            LOG.ignore(x);
            return -1;
        }
    }

    static int safeInterestOps(SelectionKey selectionKey) {
        try {
            return selectionKey.interestOps();
        }
        catch (Throwable x) {
            LOG.ignore(x);
            return -1;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void dump(Appendable out, String indent) throws IOException {
        Selector selector = this._selector;
        if (selector != null && selector.isOpen()) {
            ArrayList<SelectorUpdate> updates;
            DumpKeys dump = new DumpKeys();
            String updatesAt = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now());
            ManagedSelector managedSelector = this;
            synchronized (managedSelector) {
                updates = new ArrayList<SelectorUpdate>(this._updates);
                this._updates.addFirst(dump);
                this._selecting = false;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("wakeup on dump {}", this);
            }
            selector.wakeup();
            List<String> keys = dump.get(5L, TimeUnit.SECONDS);
            String keysAt = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now());
            if (keys == null) {
                keys = Collections.singletonList("No dump keys retrieved");
            }
            this.dumpObjects(out, indent, new DumpableCollection("updates @ " + updatesAt, updates), new DumpableCollection("keys @ " + keysAt, keys));
        } else {
            this.dumpObjects(out, indent, new Object[0]);
        }
    }

    @Override
    public String toString() {
        Selector selector = this._selector;
        return String.format("%s id=%s keys=%d selected=%d updates=%d", super.toString(), this._id, selector != null && selector.isOpen() ? selector.keys().size() : -1, selector != null && selector.isOpen() ? selector.selectedKeys().size() : -1, this.getActionSize());
    }

    static {
        String property = System.getProperty("org.eclipse.jetty.io.forceSelectNow");
        FORCE_SELECT_NOW = property != null ? Boolean.parseBoolean(property) : (property = System.getProperty("os.name")) != null && property.toLowerCase(Locale.ENGLISH).contains("windows");
    }

    private class DestroyEndPoint
    implements Runnable,
    Closeable {
        private final EndPoint endPoint;

        public DestroyEndPoint(EndPoint endPoint) {
            this.endPoint = endPoint;
        }

        @Override
        public void run() {
            Connection connection;
            if (LOG.isDebugEnabled()) {
                LOG.debug("Destroyed {}", this.endPoint);
            }
            if ((connection = this.endPoint.getConnection()) != null) {
                ManagedSelector.this._selectorManager.connectionClosed(connection);
            }
            ManagedSelector.this.endPointClosed(this.endPoint);
        }

        @Override
        public void close() {
            this.run();
        }
    }

    private final class CreateEndPoint
    implements Runnable {
        private final Connect _connect;
        private final SelectionKey _key;

        private CreateEndPoint(Connect connect, SelectionKey key) {
            this._connect = connect;
            this._key = key;
        }

        @Override
        public void run() {
            try {
                ManagedSelector.this.createEndPoint(this._connect.channel, this._key);
            }
            catch (Throwable failure) {
                IO.close(this._connect.channel);
                LOG.warn(String.valueOf(failure), new Object[0]);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(failure);
                }
                this._connect.failed(failure);
            }
        }

        public String toString() {
            return String.format("CreateEndPoint@%x{%s}", this.hashCode(), this._connect);
        }
    }

    private class StopSelector
    implements SelectorUpdate {
        private final CountDownLatch _stopped = new CountDownLatch(1);

        private StopSelector() {
        }

        @Override
        public void update(Selector selector) {
            for (SelectionKey key : selector.keys()) {
                Object attachment;
                if (key == null || !((attachment = key.attachment()) instanceof Closeable)) continue;
                IO.close((Closeable)attachment);
            }
            ManagedSelector.this._selector = null;
            IO.close(selector);
            this._stopped.countDown();
        }
    }

    private class CloseConnections
    implements SelectorUpdate {
        private final Set<Closeable> _closed;
        private final CountDownLatch _complete = new CountDownLatch(1);

        private CloseConnections() {
            this((Set<Closeable>)null);
        }

        private CloseConnections(Set<Closeable> closed) {
            this._closed = closed;
        }

        @Override
        public void update(Selector selector) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Closing {} connections on {}", selector.keys().size(), ManagedSelector.this);
            }
            for (SelectionKey key : selector.keys()) {
                if (key == null || !key.isValid()) continue;
                Closeable closeable = null;
                Object attachment = key.attachment();
                if (attachment instanceof EndPoint) {
                    EndPoint endPoint = (EndPoint)attachment;
                    Connection connection = endPoint.getConnection();
                    closeable = connection != null ? connection : endPoint;
                }
                if (closeable == null) continue;
                if (this._closed == null) {
                    IO.close(closeable);
                    continue;
                }
                if (this._closed.contains(closeable)) continue;
                this._closed.add(closeable);
                IO.close(closeable);
            }
            this._complete.countDown();
        }
    }

    class Connect
    implements SelectorUpdate,
    Runnable {
        private final AtomicBoolean failed = new AtomicBoolean();
        private final SelectableChannel channel;
        private final Object attachment;
        private final Scheduler.Task timeout;

        Connect(SelectableChannel channel, Object attachment) {
            this.channel = channel;
            this.attachment = attachment;
            long timeout = ManagedSelector.this._selectorManager.getConnectTimeout();
            this.timeout = timeout > 0L ? ManagedSelector.this._selectorManager.getScheduler().schedule(this, timeout, TimeUnit.MILLISECONDS) : null;
        }

        @Override
        public void update(Selector selector) {
            try {
                this.channel.register(selector, 8, this);
            }
            catch (Throwable x) {
                this.failed(x);
            }
        }

        @Override
        public void run() {
            if (ManagedSelector.this._selectorManager.isConnectionPending(this.channel)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Channel {} timed out while connecting, closing it", this.channel);
                }
                this.failed(new SocketTimeoutException("Connect Timeout"));
            }
        }

        public void failed(Throwable failure) {
            if (this.failed.compareAndSet(false, true)) {
                if (this.timeout != null) {
                    this.timeout.cancel();
                }
                IO.close(this.channel);
                ManagedSelector.this._selectorManager.connectionFailed(this.channel, failure, this.attachment);
            }
        }

        public String toString() {
            return String.format("Connect@%x{%s,%s}", this.hashCode(), this.channel, this.attachment);
        }
    }

    class Accept
    implements SelectorUpdate,
    Runnable,
    Closeable {
        private final SelectableChannel channel;
        private final Object attachment;
        private SelectionKey key;

        Accept(SelectableChannel channel, Object attachment) {
            this.channel = channel;
            this.attachment = attachment;
            ManagedSelector.this._selectorManager.onAccepting(channel);
        }

        @Override
        public void close() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("closed accept of {}", this.channel);
            }
            IO.close(this.channel);
        }

        @Override
        public void update(Selector selector) {
            block2: {
                try {
                    this.key = this.channel.register(selector, 0, this.attachment);
                    ManagedSelector.this.execute(this);
                }
                catch (Throwable x) {
                    IO.close(this.channel);
                    ManagedSelector.this._selectorManager.onAcceptFailed(this.channel, x);
                    if (!LOG.isDebugEnabled()) break block2;
                    LOG.debug(x);
                }
            }
        }

        @Override
        public void run() {
            try {
                ManagedSelector.this.createEndPoint(this.channel, this.key);
                ManagedSelector.this._selectorManager.onAccepted(this.channel);
            }
            catch (Throwable x) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(x);
                }
                this.failed(x);
            }
        }

        protected void failed(Throwable failure) {
            IO.close(this.channel);
            LOG.warn(String.valueOf(failure), new Object[0]);
            if (LOG.isDebugEnabled()) {
                LOG.debug(failure);
            }
            ManagedSelector.this._selectorManager.onAcceptFailed(this.channel, failure);
        }

        public String toString() {
            return String.format("%s@%x[%s]", this.getClass().getSimpleName(), this.hashCode(), this.channel);
        }
    }

    class Acceptor
    implements SelectorUpdate,
    Selectable,
    Closeable {
        private final SelectableChannel _channel;
        private SelectionKey _key;

        Acceptor(SelectableChannel channel) {
            this._channel = channel;
        }

        @Override
        public void update(Selector selector) {
            try {
                this._key = this._channel.register(selector, 16, this);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("{} acceptor={}", this, this._channel);
                }
            }
            catch (Throwable x) {
                IO.close(this._channel);
                LOG.warn(x);
            }
        }

        @Override
        public Runnable onSelected() {
            SelectableChannel channel = null;
            try {
                while ((channel = ManagedSelector.this._selectorManager.doAccept(this._channel)) != null) {
                    ManagedSelector.this._selectorManager.accepted(channel);
                }
            }
            catch (Throwable x) {
                LOG.warn("Accept failed for channel {}", channel, x);
                IO.close(channel);
            }
            return null;
        }

        @Override
        public void updateKey() {
        }

        @Override
        public void replaceKey(SelectionKey newKey) {
            this._key = newKey;
        }

        @Override
        public void close() throws IOException {
            ManagedSelector.this.submit(selector -> this._key.cancel());
        }
    }

    private static class DumpKeys
    implements SelectorUpdate {
        private final CountDownLatch latch = new CountDownLatch(1);
        private List<String> keys;

        private DumpKeys() {
        }

        @Override
        public void update(Selector selector) {
            Set<SelectionKey> selectionKeys = selector.keys();
            ArrayList<String> list = new ArrayList<String>(selectionKeys.size());
            for (SelectionKey key : selectionKeys) {
                if (key == null) continue;
                list.add(String.format("SelectionKey@%x{i=%d}->%s", key.hashCode(), ManagedSelector.safeInterestOps(key), key.attachment()));
            }
            this.keys = list;
            this.latch.countDown();
        }

        public List<String> get(long timeout, TimeUnit unit) {
            try {
                this.latch.await(timeout, unit);
            }
            catch (InterruptedException x) {
                LOG.ignore(x);
            }
            return this.keys;
        }
    }

    private class Start
    implements SelectorUpdate {
        private final CountDownLatch _started = new CountDownLatch(1);

        private Start() {
        }

        @Override
        public void update(Selector selector) {
            ManagedSelector.this._started.set(true);
            this._started.countDown();
        }
    }

    public static interface SelectorUpdate {
        public void update(Selector var1);
    }

    private class SelectorProducer
    implements ExecutionStrategy.Producer {
        private Set<SelectionKey> _keys = Collections.emptySet();
        private Iterator<SelectionKey> _cursor = Collections.emptyIterator();

        private SelectorProducer() {
        }

        @Override
        public Runnable produce() {
            do {
                Runnable task;
                if ((task = this.processSelected()) != null) {
                    return task;
                }
                this.processUpdates();
                this.updateKeys();
            } while (this.select());
            return null;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private void processUpdates() {
            Selector selector;
            int updates;
            ManagedSelector managedSelector = ManagedSelector.this;
            synchronized (managedSelector) {
                Deque updates2 = ManagedSelector.this._updates;
                ManagedSelector.this._updates = ManagedSelector.this._updateable;
                ManagedSelector.this._updateable = updates2;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("updateable {}", ManagedSelector.this._updateable.size());
            }
            for (SelectorUpdate update : ManagedSelector.this._updateable) {
                if (ManagedSelector.this._selector == null) break;
                try {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("update {}", update);
                    }
                    update.update(ManagedSelector.this._selector);
                }
                catch (Throwable ex) {
                    LOG.warn(ex);
                }
            }
            ManagedSelector.this._updateable.clear();
            ManagedSelector managedSelector2 = ManagedSelector.this;
            synchronized (managedSelector2) {
                updates = ManagedSelector.this._updates.size();
                ManagedSelector.this._selecting = updates == 0;
                selector = ManagedSelector.this._selecting ? null : ManagedSelector.this._selector;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("updates {}", updates);
            }
            if (selector != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("wakeup on updates {}", this);
                }
                selector.wakeup();
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private boolean select() {
            block10: {
                try {
                    int updates;
                    Selector selector = ManagedSelector.this._selector;
                    if (selector == null) break block10;
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Selector {} waiting with {} keys", selector, selector.keys().size());
                    }
                    int selected = ManagedSelector.this.select(selector);
                    selector = ManagedSelector.this._selector;
                    if (selector == null) break block10;
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Selector {} woken up from select, {}/{}/{} selected", selector, selected, selector.selectedKeys().size(), selector.keys().size());
                    }
                    ManagedSelector managedSelector = ManagedSelector.this;
                    synchronized (managedSelector) {
                        ManagedSelector.this._selecting = false;
                        updates = ManagedSelector.this._updates.size();
                    }
                    this._keys = selector.selectedKeys();
                    int selectedKeys = this._keys.size();
                    if (selectedKeys > 0) {
                        ManagedSelector.this._keyStats.record(selectedKeys);
                    }
                    Iterator<Object> iterator = this._cursor = selectedKeys > 0 ? this._keys.iterator() : Collections.emptyIterator();
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Selector {} processing {} keys, {} updates", selector, selectedKeys, updates);
                    }
                    return true;
                }
                catch (Throwable x) {
                    IO.close(ManagedSelector.this._selector);
                    ManagedSelector.this._selector = null;
                    if (ManagedSelector.this.isRunning()) {
                        LOG.warn("Fatal select() failure", x);
                        ManagedSelector.this.onSelectFailed(x);
                    }
                    LOG.warn(x.toString(), new Object[0]);
                    if (!LOG.isDebugEnabled()) break block10;
                    LOG.debug(x);
                }
            }
            return false;
        }

        private Runnable processSelected() {
            while (this._cursor.hasNext()) {
                SelectionKey key = this._cursor.next();
                Object attachment = key.attachment();
                SelectableChannel channel = key.channel();
                if (key.isValid()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("selected {} {} {} ", ManagedSelector.safeReadyOps(key), key, attachment);
                    }
                    try {
                        if (attachment instanceof Selectable) {
                            Runnable task = ((Selectable)attachment).onSelected();
                            if (task == null) continue;
                            return task;
                        }
                        if (key.isConnectable()) {
                            ManagedSelector.this.processConnect(key, (Connect)attachment);
                            continue;
                        }
                        throw new IllegalStateException("key=" + key + ", att=" + attachment + ", iOps=" + ManagedSelector.safeInterestOps(key) + ", rOps=" + ManagedSelector.safeReadyOps(key));
                    }
                    catch (CancelledKeyException x) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Ignoring cancelled key for channel {}", channel);
                        }
                        IO.close(attachment instanceof EndPoint ? (EndPoint)attachment : channel);
                        continue;
                    }
                    catch (Throwable x) {
                        LOG.warn("Could not process key for channel {}", channel, x);
                        IO.close(attachment instanceof EndPoint ? (EndPoint)attachment : channel);
                        continue;
                    }
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Selector loop ignoring invalid key for channel {}", channel);
                }
                IO.close(attachment instanceof EndPoint ? (EndPoint)attachment : channel);
            }
            return null;
        }

        private void updateKeys() {
            for (SelectionKey key : this._keys) {
                Object attachment = key.attachment();
                if (!(attachment instanceof Selectable)) continue;
                ((Selectable)attachment).updateKey();
            }
            this._keys.clear();
        }

        public String toString() {
            return String.format("%s@%x", this.getClass().getSimpleName(), this.hashCode());
        }
    }

    public static interface Selectable {
        public Runnable onSelected();

        public void updateKey();

        public void replaceKey(SelectionKey var1);
    }
}

