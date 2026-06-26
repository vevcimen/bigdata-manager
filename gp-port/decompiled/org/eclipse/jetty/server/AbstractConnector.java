/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server;

import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.stream.Collectors;
import org.eclipse.jetty.io.ArrayByteBufferPool;
import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.HttpChannelListeners;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ProcessorUtils;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.annotation.ManagedObject;
import org.eclipse.jetty.util.component.Container;
import org.eclipse.jetty.util.component.ContainerLifeCycle;
import org.eclipse.jetty.util.component.Dumpable;
import org.eclipse.jetty.util.component.Graceful;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.thread.Locker;
import org.eclipse.jetty.util.thread.ScheduledExecutorScheduler;
import org.eclipse.jetty.util.thread.Scheduler;
import org.eclipse.jetty.util.thread.ThreadPoolBudget;

@ManagedObject(value="Abstract implementation of the Connector Interface")
public abstract class AbstractConnector
extends ContainerLifeCycle
implements Connector,
Dumpable {
    protected static final Logger LOG = Log.getLogger(AbstractConnector.class);
    private final Locker _locker = new Locker();
    private final Condition _setAccepting = this._locker.newCondition();
    private final Map<String, ConnectionFactory> _factories = new LinkedHashMap<String, ConnectionFactory>();
    private final Server _server;
    private final Executor _executor;
    private final Scheduler _scheduler;
    private final ByteBufferPool _byteBufferPool;
    private final Thread[] _acceptors;
    private final Set<EndPoint> _endpoints = Collections.newSetFromMap(new ConcurrentHashMap());
    private final Set<EndPoint> _immutableEndPoints = Collections.unmodifiableSet(this._endpoints);
    private final Graceful.Shutdown _shutdown = new Graceful.Shutdown();
    private HttpChannel.Listener _httpChannelListeners = HttpChannel.NOOP_LISTENER;
    private CountDownLatch _stopping;
    private long _idleTimeout = 30000L;
    private String _defaultProtocol;
    private ConnectionFactory _defaultConnectionFactory;
    private String _name;
    private int _acceptorPriorityDelta = -2;
    private boolean _accepting = true;
    private ThreadPoolBudget.Lease _lease;

    public AbstractConnector(Server server, Executor executor, Scheduler scheduler, ByteBufferPool pool, int acceptors, ConnectionFactory ... factories) {
        this._server = server;
        Executor executor2 = this._executor = executor != null ? executor : this._server.getThreadPool();
        if (scheduler == null) {
            scheduler = this._server.getBean(Scheduler.class);
        }
        Scheduler scheduler2 = this._scheduler = scheduler != null ? scheduler : new ScheduledExecutorScheduler(String.format("Connector-Scheduler-%x", this.hashCode()), false);
        if (pool == null) {
            pool = this._server.getBean(ByteBufferPool.class);
        }
        this._byteBufferPool = pool != null ? pool : new ArrayByteBufferPool();
        this.addEventListener(new Container.Listener(){

            @Override
            public void beanAdded(Container parent, Object bean) {
                if (bean instanceof HttpChannel.Listener) {
                    AbstractConnector.this._httpChannelListeners = new HttpChannelListeners(AbstractConnector.this.getBeans(HttpChannel.Listener.class));
                }
            }

            @Override
            public void beanRemoved(Container parent, Object bean) {
                if (bean instanceof HttpChannel.Listener) {
                    AbstractConnector.this._httpChannelListeners = new HttpChannelListeners(AbstractConnector.this.getBeans(HttpChannel.Listener.class));
                }
            }
        });
        this.addBean(this._server, false);
        this.addBean(this._executor);
        if (executor == null) {
            this.unmanage(this._executor);
        }
        this.addBean(this._scheduler);
        this.addBean(this._byteBufferPool);
        for (ConnectionFactory factory : factories) {
            this.addConnectionFactory(factory);
        }
        int cores = ProcessorUtils.availableProcessors();
        if (acceptors < 0) {
            acceptors = Math.max(1, Math.min(4, cores / 8));
        }
        if (acceptors > cores) {
            LOG.warn("Acceptors should be <= availableProcessors: " + this, new Object[0]);
        }
        this._acceptors = new Thread[acceptors];
    }

    public HttpChannel.Listener getHttpChannelListeners() {
        return this._httpChannelListeners;
    }

    @Override
    public Server getServer() {
        return this._server;
    }

    @Override
    public Executor getExecutor() {
        return this._executor;
    }

    @Override
    public ByteBufferPool getByteBufferPool() {
        return this._byteBufferPool;
    }

    @Override
    @ManagedAttribute(value="The connection idle timeout in milliseconds")
    public long getIdleTimeout() {
        return this._idleTimeout;
    }

    public void setIdleTimeout(long idleTimeout) {
        this._idleTimeout = idleTimeout;
    }

    @ManagedAttribute(value="number of acceptor threads")
    public int getAcceptors() {
        return this._acceptors.length;
    }

    @Override
    protected void doStart() throws Exception {
        String next;
        ConnectionFactory cf;
        this._shutdown.cancel();
        if (this._defaultProtocol == null) {
            throw new IllegalStateException("No default protocol for " + this);
        }
        this._defaultConnectionFactory = this.getConnectionFactory(this._defaultProtocol);
        if (this._defaultConnectionFactory == null) {
            throw new IllegalStateException("No protocol factory for default protocol '" + this._defaultProtocol + "' in " + this);
        }
        SslConnectionFactory ssl = this.getConnectionFactory(SslConnectionFactory.class);
        if (ssl != null && (cf = this.getConnectionFactory(next = ssl.getNextProtocol())) == null) {
            throw new IllegalStateException("No protocol factory for SSL next protocol: '" + next + "' in " + this);
        }
        this._lease = ThreadPoolBudget.leaseFrom(this.getExecutor(), this, this._acceptors.length);
        super.doStart();
        this._stopping = new CountDownLatch(this._acceptors.length);
        for (int i = 0; i < this._acceptors.length; ++i) {
            Acceptor a = new Acceptor(i);
            this.addBean(a);
            this.getExecutor().execute(a);
        }
        LOG.info("Started {}", this);
    }

    protected void interruptAcceptors() {
        try (Locker.Lock lock = this._locker.lock();){
            for (Thread thread : this._acceptors) {
                if (thread == null) continue;
                thread.interrupt();
            }
        }
    }

    @Override
    public Future<Void> shutdown() {
        return this._shutdown.shutdown();
    }

    @Override
    public boolean isShutdown() {
        return this._shutdown.isShutdown();
    }

    @Override
    protected void doStop() throws Exception {
        if (this._lease != null) {
            this._lease.close();
        }
        this.interruptAcceptors();
        long stopTimeout = this.getStopTimeout();
        CountDownLatch stopping = this._stopping;
        if (stopTimeout > 0L && stopping != null && this.getAcceptors() > 0) {
            stopping.await(stopTimeout, TimeUnit.MILLISECONDS);
        }
        this._stopping = null;
        super.doStop();
        for (Acceptor a : this.getBeans(Acceptor.class)) {
            this.removeBean(a);
        }
        LOG.info("Stopped {}", this);
    }

    public void join() throws InterruptedException {
        this.join(0L);
    }

    public void join(long timeout) throws InterruptedException {
        try (Locker.Lock lock = this._locker.lock();){
            for (Thread thread : this._acceptors) {
                if (thread == null) continue;
                thread.join(timeout);
            }
        }
    }

    protected abstract void accept(int var1) throws IOException, InterruptedException;

    public boolean isAccepting() {
        try (Locker.Lock lock = this._locker.lock();){
            boolean bl = this._accepting;
            return bl;
        }
    }

    public void setAccepting(boolean accepting) {
        try (Locker.Lock lock = this._locker.lock();){
            this._accepting = accepting;
            this._setAccepting.signalAll();
        }
    }

    @Override
    public ConnectionFactory getConnectionFactory(String protocol) {
        try (Locker.Lock lock = this._locker.lock();){
            ConnectionFactory connectionFactory = this._factories.get(StringUtil.asciiToLowerCase(protocol));
            return connectionFactory;
        }
    }

    @Override
    public <T> T getConnectionFactory(Class<T> factoryType) {
        try (Locker.Lock lock = this._locker.lock();){
            for (ConnectionFactory f : this._factories.values()) {
                if (!factoryType.isAssignableFrom(f.getClass())) continue;
                ConnectionFactory connectionFactory = f;
                return (T)connectionFactory;
            }
            Iterator<ConnectionFactory> iterator = null;
            return (T)iterator;
        }
    }

    public void addConnectionFactory(ConnectionFactory factory) {
        if (this.isRunning()) {
            throw new IllegalStateException(this.getState());
        }
        HashSet<ConnectionFactory> toRemove = new HashSet<ConnectionFactory>();
        for (String key : factory.getProtocols()) {
            ConnectionFactory old = this._factories.remove(key = StringUtil.asciiToLowerCase(key));
            if (old != null) {
                if (old.getProtocol().equals(this._defaultProtocol)) {
                    this._defaultProtocol = null;
                }
                toRemove.add(old);
            }
            this._factories.put(key, factory);
        }
        for (ConnectionFactory f : this._factories.values()) {
            toRemove.remove(f);
        }
        for (ConnectionFactory old : toRemove) {
            this.removeBean(old);
            if (!LOG.isDebugEnabled()) continue;
            LOG.debug("{} removed {}", this, old);
        }
        this.addBean(factory);
        if (this._defaultProtocol == null) {
            this._defaultProtocol = factory.getProtocol();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("{} added {}", this, factory);
        }
    }

    public void addFirstConnectionFactory(ConnectionFactory factory) {
        if (this.isRunning()) {
            throw new IllegalStateException(this.getState());
        }
        ArrayList<ConnectionFactory> existings = new ArrayList<ConnectionFactory>(this._factories.values());
        this._factories.clear();
        this.addConnectionFactory(factory);
        for (ConnectionFactory existing : existings) {
            this.addConnectionFactory(existing);
        }
        this._defaultProtocol = factory.getProtocol();
    }

    public void addIfAbsentConnectionFactory(ConnectionFactory factory) {
        if (this.isRunning()) {
            throw new IllegalStateException(this.getState());
        }
        String key = StringUtil.asciiToLowerCase(factory.getProtocol());
        if (this._factories.containsKey(key)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("{} addIfAbsent ignored {}", this, factory);
            }
        } else {
            this._factories.put(key, factory);
            this.addBean(factory);
            if (this._defaultProtocol == null) {
                this._defaultProtocol = factory.getProtocol();
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("{} addIfAbsent added {}", this, factory);
            }
        }
    }

    public ConnectionFactory removeConnectionFactory(String protocol) {
        if (this.isRunning()) {
            throw new IllegalStateException(this.getState());
        }
        ConnectionFactory factory = this._factories.remove(StringUtil.asciiToLowerCase(protocol));
        this.removeBean(factory);
        return factory;
    }

    @Override
    public Collection<ConnectionFactory> getConnectionFactories() {
        return this._factories.values();
    }

    public void setConnectionFactories(Collection<ConnectionFactory> factories) {
        if (this.isRunning()) {
            throw new IllegalStateException(this.getState());
        }
        ArrayList<ConnectionFactory> existing = new ArrayList<ConnectionFactory>(this._factories.values());
        for (ConnectionFactory factory : existing) {
            this.removeConnectionFactory(factory.getProtocol());
        }
        for (ConnectionFactory factory : factories) {
            if (factory == null) continue;
            this.addConnectionFactory(factory);
        }
    }

    @ManagedAttribute(value="The priority delta to apply to acceptor threads")
    public int getAcceptorPriorityDelta() {
        return this._acceptorPriorityDelta;
    }

    public void setAcceptorPriorityDelta(int acceptorPriorityDelta) {
        int old = this._acceptorPriorityDelta;
        this._acceptorPriorityDelta = acceptorPriorityDelta;
        if (old != acceptorPriorityDelta && this.isStarted()) {
            for (Thread thread : this._acceptors) {
                thread.setPriority(Math.max(1, Math.min(10, thread.getPriority() - old + acceptorPriorityDelta)));
            }
        }
    }

    @Override
    @ManagedAttribute(value="Protocols supported by this connector")
    public List<String> getProtocols() {
        return new ArrayList<String>(this._factories.keySet());
    }

    public void clearConnectionFactories() {
        if (this.isRunning()) {
            throw new IllegalStateException(this.getState());
        }
        this._factories.clear();
    }

    @ManagedAttribute(value="This connector's default protocol")
    public String getDefaultProtocol() {
        return this._defaultProtocol;
    }

    public void setDefaultProtocol(String defaultProtocol) {
        this._defaultProtocol = StringUtil.asciiToLowerCase(defaultProtocol);
        if (this.isRunning()) {
            this._defaultConnectionFactory = this.getConnectionFactory(this._defaultProtocol);
        }
    }

    @Override
    public ConnectionFactory getDefaultConnectionFactory() {
        if (this.isStarted()) {
            return this._defaultConnectionFactory;
        }
        return this.getConnectionFactory(this._defaultProtocol);
    }

    protected boolean handleAcceptFailure(Throwable ex) {
        if (this.isRunning()) {
            if (ex instanceof InterruptedException) {
                LOG.debug(ex);
                return true;
            }
            if (ex instanceof ClosedByInterruptException) {
                LOG.debug(ex);
                return false;
            }
            LOG.warn(ex);
            try {
                Thread.sleep(1000L);
                return true;
            }
            catch (Throwable x) {
                LOG.ignore(x);
                return false;
            }
        }
        LOG.ignore(ex);
        return false;
    }

    @Override
    public Collection<EndPoint> getConnectedEndPoints() {
        return this._immutableEndPoints;
    }

    protected void onEndPointOpened(EndPoint endp) {
        this._endpoints.add(endp);
    }

    protected void onEndPointClosed(EndPoint endp) {
        this._endpoints.remove(endp);
    }

    @Override
    public Scheduler getScheduler() {
        return this._scheduler;
    }

    @Override
    public String getName() {
        return this._name;
    }

    public void setName(String name) {
        this._name = name;
    }

    @Override
    public String toString() {
        return String.format("%s@%x{%s, %s}", this._name == null ? this.getClass().getSimpleName() : this._name, this.hashCode(), this.getDefaultProtocol(), this.getProtocols().stream().collect(Collectors.joining(", ", "(", ")")));
    }

    private class Acceptor
    implements Runnable {
        private final int _id;
        private String _name;

        private Acceptor(int id) {
            this._id = id;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         * Enabled aggressive block sorting
         * Enabled unnecessary exception pruning
         * Enabled aggressive exception aggregation
         */
        @Override
        public void run() {
            Thread thread = Thread.currentThread();
            String name = thread.getName();
            this._name = String.format("%s-acceptor-%d@%x-%s", name, this._id, this.hashCode(), AbstractConnector.this.toString());
            thread.setName(this._name);
            int priority = thread.getPriority();
            if (AbstractConnector.this._acceptorPriorityDelta != 0) {
                thread.setPriority(Math.max(1, Math.min(10, priority + AbstractConnector.this._acceptorPriorityDelta)));
            }
            ((AbstractConnector)AbstractConnector.this)._acceptors[this._id] = thread;
            try {
                while (AbstractConnector.this.isRunning()) {
                    block22: {
                        try {
                            Locker.Lock lock = AbstractConnector.this._locker.lock();
                            try {
                                if (!AbstractConnector.this._accepting && AbstractConnector.this.isRunning()) {
                                    AbstractConnector.this._setAccepting.await();
                                    continue;
                                }
                                break block22;
                            }
                            finally {
                                if (lock == null) continue;
                                lock.close();
                            }
                        }
                        catch (InterruptedException e) {}
                        continue;
                    }
                    try {
                        AbstractConnector.this.accept(this._id);
                    }
                    catch (Throwable x) {
                        if (!AbstractConnector.this.handleAcceptFailure(x)) return;
                    }
                }
                return;
            }
            finally {
                thread.setName(name);
                if (AbstractConnector.this._acceptorPriorityDelta != 0) {
                    thread.setPriority(priority);
                }
                AbstractConnector x = AbstractConnector.this;
                synchronized (x) {
                    ((AbstractConnector)AbstractConnector.this)._acceptors[this._id] = null;
                }
                CountDownLatch stopping = AbstractConnector.this._stopping;
                if (stopping != null) {
                    stopping.countDown();
                }
            }
        }

        public String toString() {
            String name = this._name;
            if (name == null) {
                return String.format("acceptor-%d@%x", this._id, this.hashCode());
            }
            return name;
        }
    }
}

