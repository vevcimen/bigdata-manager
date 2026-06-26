/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.io;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import org.eclipse.jetty.io.AbstractEndPoint;
import org.eclipse.jetty.io.EofException;
import org.eclipse.jetty.io.ManagedSelector;
import org.eclipse.jetty.util.BufferUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.thread.Invocable;
import org.eclipse.jetty.util.thread.Scheduler;

public abstract class ChannelEndPoint
extends AbstractEndPoint
implements ManagedSelector.Selectable {
    private static final Logger LOG = Log.getLogger(ChannelEndPoint.class);
    private final SocketChannel _channel;
    private final ManagedSelector _selector;
    private SelectionKey _key;
    private boolean _updatePending;
    private int _currentInterestOps;
    private int _desiredInterestOps;
    private final ManagedSelector.SelectorUpdate _updateKeyAction = this::updateKeyAction;
    private final Runnable _runFillable = new RunnableCloseable("runFillable"){

        @Override
        public Invocable.InvocationType getInvocationType() {
            return ChannelEndPoint.this.getFillInterest().getCallbackInvocationType();
        }

        @Override
        public void run() {
            ChannelEndPoint.this.getFillInterest().fillable();
        }
    };
    private final Runnable _runCompleteWrite = new RunnableCloseable("runCompleteWrite"){

        @Override
        public Invocable.InvocationType getInvocationType() {
            return ChannelEndPoint.this.getWriteFlusher().getCallbackInvocationType();
        }

        @Override
        public void run() {
            ChannelEndPoint.this.getWriteFlusher().completeWrite();
        }

        @Override
        public String toString() {
            return String.format("CEP:%s:%s:%s->%s", new Object[]{ChannelEndPoint.this, this._operation, this.getInvocationType(), ChannelEndPoint.this.getWriteFlusher()});
        }
    };
    private final Runnable _runCompleteWriteFillable = new RunnableCloseable("runCompleteWriteFillable"){

        @Override
        public Invocable.InvocationType getInvocationType() {
            Invocable.InvocationType flushT;
            Invocable.InvocationType fillT = ChannelEndPoint.this.getFillInterest().getCallbackInvocationType();
            if (fillT == (flushT = ChannelEndPoint.this.getWriteFlusher().getCallbackInvocationType())) {
                return fillT;
            }
            if (fillT == Invocable.InvocationType.EITHER && flushT == Invocable.InvocationType.NON_BLOCKING) {
                return Invocable.InvocationType.EITHER;
            }
            if (fillT == Invocable.InvocationType.NON_BLOCKING && flushT == Invocable.InvocationType.EITHER) {
                return Invocable.InvocationType.EITHER;
            }
            return Invocable.InvocationType.BLOCKING;
        }

        @Override
        public void run() {
            ChannelEndPoint.this.getWriteFlusher().completeWrite();
            ChannelEndPoint.this.getFillInterest().fillable();
        }
    };

    public ChannelEndPoint(SocketChannel channel, ManagedSelector selector, SelectionKey key, Scheduler scheduler) {
        super(scheduler);
        this._channel = channel;
        this._selector = selector;
        this._key = key;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return (InetSocketAddress)this._channel.socket().getLocalSocketAddress();
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return (InetSocketAddress)this._channel.socket().getRemoteSocketAddress();
    }

    @Override
    public boolean isOptimizedForDirectBuffers() {
        return true;
    }

    @Override
    public boolean isOpen() {
        return this._channel.isOpen();
    }

    @Override
    protected void doShutdownOutput() {
        try {
            Socket socket = this._channel.socket();
            if (!socket.isOutputShutdown()) {
                socket.shutdownOutput();
            }
        }
        catch (IOException e) {
            LOG.debug(e);
        }
    }

    @Override
    public void doClose() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("doClose {}", this);
        }
        try {
            this._channel.close();
        }
        catch (IOException e) {
            LOG.debug(e);
        }
        finally {
            super.doClose();
        }
    }

    @Override
    public void onClose() {
        try {
            super.onClose();
        }
        finally {
            if (this._selector != null) {
                this._selector.destroyEndPoint(this);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int fill(ByteBuffer buffer) throws IOException {
        int filled;
        if (this.isInputShutdown()) {
            return -1;
        }
        int pos = BufferUtil.flipToFill(buffer);
        try {
            filled = this._channel.read(buffer);
            if (filled > 0) {
                this.notIdle();
            } else if (filled == -1) {
                this.shutdownInput();
            }
        }
        catch (IOException e) {
            LOG.debug(e);
            this.shutdownInput();
            filled = -1;
        }
        finally {
            BufferUtil.flipToFlush(buffer, pos);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("filled {} {}", filled, BufferUtil.toDetailString(buffer));
        }
        return filled;
    }

    @Override
    public boolean flush(ByteBuffer ... buffers) throws IOException {
        long flushed;
        try {
            flushed = this._channel.write(buffers);
            if (LOG.isDebugEnabled()) {
                LOG.debug("flushed {} {}", flushed, this);
            }
        }
        catch (IOException e) {
            throw new EofException(e);
        }
        if (flushed > 0L) {
            this.notIdle();
        }
        for (ByteBuffer b : buffers) {
            if (BufferUtil.isEmpty(b)) continue;
            return false;
        }
        return true;
    }

    public SocketChannel getChannel() {
        return this._channel;
    }

    @Override
    public Object getTransport() {
        return this._channel;
    }

    @Override
    protected void needsFillInterest() {
        this.changeInterests(1);
    }

    @Override
    protected void onIncompleteFlush() {
        this.changeInterests(4);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Runnable onSelected() {
        Runnable task;
        boolean flushable;
        int newInterestOps;
        int oldInterestOps;
        int readyOps = this._key.readyOps();
        ChannelEndPoint channelEndPoint = this;
        synchronized (channelEndPoint) {
            this._updatePending = true;
            oldInterestOps = this._desiredInterestOps;
            this._desiredInterestOps = newInterestOps = oldInterestOps & ~readyOps;
        }
        boolean fillable = (readyOps & 1) != 0;
        boolean bl = flushable = (readyOps & 4) != 0;
        if (LOG.isDebugEnabled()) {
            LOG.debug("onSelected {}->{} r={} w={} for {}", oldInterestOps, newInterestOps, fillable, flushable, this);
        }
        Runnable runnable = fillable ? (flushable ? this._runCompleteWriteFillable : this._runFillable) : (task = flushable ? this._runCompleteWrite : null);
        if (LOG.isDebugEnabled()) {
            LOG.debug("task {}", task);
        }
        return task;
    }

    private void updateKeyAction(Selector selector) {
        this.updateKey();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void updateKey() {
        try {
            int newInterestOps;
            int oldInterestOps;
            ChannelEndPoint channelEndPoint = this;
            synchronized (channelEndPoint) {
                this._updatePending = false;
                oldInterestOps = this._currentInterestOps;
                newInterestOps = this._desiredInterestOps;
                if (oldInterestOps != newInterestOps) {
                    this._currentInterestOps = newInterestOps;
                    this._key.interestOps(newInterestOps);
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Key interests updated {} -> {} on {}", oldInterestOps, newInterestOps, this);
            }
        }
        catch (CancelledKeyException x) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Ignoring key update for cancelled key {}", this, x);
            }
            this.close();
        }
        catch (Throwable x) {
            LOG.warn("Ignoring key update for {}", this, x);
            this.close();
        }
    }

    @Override
    public void replaceKey(SelectionKey newKey) {
        this._key = newKey;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void changeInterests(int operation) {
        int newInterestOps;
        int oldInterestOps;
        boolean pending;
        ChannelEndPoint channelEndPoint = this;
        synchronized (channelEndPoint) {
            pending = this._updatePending;
            oldInterestOps = this._desiredInterestOps;
            newInterestOps = oldInterestOps | operation;
            if (newInterestOps != oldInterestOps) {
                this._desiredInterestOps = newInterestOps;
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("changeInterests p={} {}->{} for {}", pending, oldInterestOps, newInterestOps, this);
        }
        if (!pending && this._selector != null) {
            this._selector.submit(this._updateKeyAction);
        }
    }

    @Override
    public String toEndPointString() {
        return String.format("%s{io=%d/%d,kio=%d,kro=%d}", super.toEndPointString(), this._currentInterestOps, this._desiredInterestOps, ManagedSelector.safeInterestOps(this._key), ManagedSelector.safeReadyOps(this._key));
    }

    private abstract class RunnableCloseable
    extends RunnableTask
    implements Closeable {
        protected RunnableCloseable(String op) {
            super(op);
        }

        @Override
        public void close() {
            try {
                ChannelEndPoint.this.close();
            }
            catch (Throwable x) {
                LOG.warn(x);
            }
        }
    }

    private abstract class RunnableTask
    implements Runnable,
    Invocable {
        final String _operation;

        protected RunnableTask(String op) {
            this._operation = op;
        }

        public String toString() {
            return String.format("CEP:%s:%s:%s", new Object[]{ChannelEndPoint.this, this._operation, this.getInvocationType()});
        }
    }
}

