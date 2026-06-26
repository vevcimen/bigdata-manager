/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.util.thread.strategy;

import java.io.Closeable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.LongAdder;
import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.annotation.ManagedObject;
import org.eclipse.jetty.util.annotation.ManagedOperation;
import org.eclipse.jetty.util.component.ContainerLifeCycle;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.thread.ExecutionStrategy;
import org.eclipse.jetty.util.thread.Invocable;
import org.eclipse.jetty.util.thread.TryExecutor;

@ManagedObject(value="eat what you kill execution strategy")
public class EatWhatYouKill
extends ContainerLifeCycle
implements ExecutionStrategy,
Runnable {
    private static final Logger LOG = Log.getLogger(EatWhatYouKill.class);
    private final LongAdder _pcMode = new LongAdder();
    private final LongAdder _picMode = new LongAdder();
    private final LongAdder _pecMode = new LongAdder();
    private final LongAdder _epcMode = new LongAdder();
    private final ExecutionStrategy.Producer _producer;
    private final Executor _executor;
    private final TryExecutor _tryExecutor;
    private State _state = State.IDLE;
    private boolean _pending;

    public EatWhatYouKill(ExecutionStrategy.Producer producer, Executor executor) {
        this._producer = producer;
        this._executor = executor;
        this._tryExecutor = TryExecutor.asTryExecutor(executor);
        this.addBean(this._producer);
        this.addBean(this._tryExecutor);
        if (LOG.isDebugEnabled()) {
            LOG.debug("{} created", this);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void dispatch() {
        boolean execute = false;
        EatWhatYouKill eatWhatYouKill = this;
        synchronized (eatWhatYouKill) {
            switch (this._state) {
                case IDLE: {
                    if (this._pending) break;
                    this._pending = true;
                    execute = true;
                    break;
                }
                case PRODUCING: {
                    this._state = State.REPRODUCING;
                    break;
                }
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("{} dispatch {}", this, execute);
        }
        if (execute) {
            this._executor.execute(this);
        }
    }

    @Override
    public void run() {
        this.tryProduce(true);
    }

    @Override
    public void produce() {
        this.tryProduce(false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void tryProduce(boolean wasPending) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("{} tryProduce {}", this, wasPending);
        }
        EatWhatYouKill eatWhatYouKill = this;
        synchronized (eatWhatYouKill) {
            if (wasPending) {
                this._pending = false;
            }
            switch (this._state) {
                case IDLE: {
                    this._state = State.PRODUCING;
                    break;
                }
                case PRODUCING: {
                    this._state = State.REPRODUCING;
                    return;
                }
                default: {
                    return;
                }
            }
        }
        boolean nonBlocking = Invocable.isNonBlockingInvocation();
        while (this.isRunning()) {
            try {
                if (this.doProduce(nonBlocking)) continue;
                return;
            }
            catch (Throwable ex) {
                LOG.warn(ex);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean doProduce(boolean nonBlocking) {
        EatWhatYouKill eatWhatYouKill;
        Mode mode;
        Runnable task = this.produceTask();
        if (task == null) {
            EatWhatYouKill eatWhatYouKill2 = this;
            synchronized (eatWhatYouKill2) {
                switch (this._state) {
                    case PRODUCING: {
                        this._state = State.IDLE;
                        return false;
                    }
                    case REPRODUCING: {
                        this._state = State.PRODUCING;
                        return true;
                    }
                }
                throw new IllegalStateException(this.toStringLocked());
            }
        }
        if (nonBlocking) {
            switch (Invocable.getInvocationType(task)) {
                case NON_BLOCKING: {
                    mode = Mode.PRODUCE_CONSUME;
                    break;
                }
                case EITHER: {
                    mode = Mode.PRODUCE_INVOKE_CONSUME;
                    break;
                }
                default: {
                    mode = Mode.PRODUCE_EXECUTE_CONSUME;
                    break;
                }
            }
        } else {
            switch (Invocable.getInvocationType(task)) {
                case NON_BLOCKING: {
                    mode = Mode.PRODUCE_CONSUME;
                    break;
                }
                case BLOCKING: {
                    eatWhatYouKill = this;
                    synchronized (eatWhatYouKill) {
                        if (this._pending) {
                            this._state = State.IDLE;
                            mode = Mode.EXECUTE_PRODUCE_CONSUME;
                        } else if (this._tryExecutor.tryExecute(this)) {
                            this._pending = true;
                            this._state = State.IDLE;
                            mode = Mode.EXECUTE_PRODUCE_CONSUME;
                        } else {
                            mode = Mode.PRODUCE_EXECUTE_CONSUME;
                        }
                        break;
                    }
                }
                case EITHER: {
                    eatWhatYouKill = this;
                    synchronized (eatWhatYouKill) {
                        if (this._pending) {
                            this._state = State.IDLE;
                            mode = Mode.EXECUTE_PRODUCE_CONSUME;
                        } else if (this._tryExecutor.tryExecute(this)) {
                            this._pending = true;
                            this._state = State.IDLE;
                            mode = Mode.EXECUTE_PRODUCE_CONSUME;
                        } else {
                            mode = Mode.PRODUCE_INVOKE_CONSUME;
                        }
                        break;
                    }
                }
                default: {
                    throw new IllegalStateException(this.toString());
                }
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("{} m={} t={}/{}", new Object[]{this, mode, task, Invocable.getInvocationType(task)});
        }
        switch (mode) {
            case PRODUCE_CONSUME: {
                this._pcMode.increment();
                this.runTask(task);
                return true;
            }
            case PRODUCE_INVOKE_CONSUME: {
                this._picMode.increment();
                this.invokeTask(task);
                return true;
            }
            case PRODUCE_EXECUTE_CONSUME: {
                this._pecMode.increment();
                this.execute(task);
                return true;
            }
            case EXECUTE_PRODUCE_CONSUME: {
                this._epcMode.increment();
                this.runTask(task);
                eatWhatYouKill = this;
                synchronized (eatWhatYouKill) {
                    if (this._state == State.IDLE) {
                        this._state = State.PRODUCING;
                        return true;
                    }
                }
                return false;
            }
        }
        throw new IllegalStateException(this.toString());
    }

    private void runTask(Runnable task) {
        try {
            task.run();
        }
        catch (Throwable x) {
            LOG.warn(x);
        }
    }

    private void invokeTask(Runnable task) {
        try {
            Invocable.invokeNonBlocking(task);
        }
        catch (Throwable x) {
            LOG.warn(x);
        }
    }

    private Runnable produceTask() {
        try {
            return this._producer.produce();
        }
        catch (Throwable e) {
            LOG.warn(e);
            return null;
        }
    }

    private void execute(Runnable task) {
        block6: {
            try {
                this._executor.execute(task);
            }
            catch (RejectedExecutionException e) {
                if (this.isRunning()) {
                    LOG.warn(e);
                } else {
                    LOG.ignore(e);
                }
                if (!(task instanceof Closeable)) break block6;
                try {
                    ((Closeable)((Object)task)).close();
                }
                catch (Throwable ex2) {
                    LOG.ignore(ex2);
                }
            }
        }
    }

    @ManagedAttribute(value="number of tasks consumed with PC mode", readonly=true)
    public long getPCTasksConsumed() {
        return this._pcMode.longValue();
    }

    @ManagedAttribute(value="number of tasks executed with PIC mode", readonly=true)
    public long getPICTasksExecuted() {
        return this._picMode.longValue();
    }

    @ManagedAttribute(value="number of tasks executed with PEC mode", readonly=true)
    public long getPECTasksExecuted() {
        return this._pecMode.longValue();
    }

    @ManagedAttribute(value="number of tasks consumed with EPC mode", readonly=true)
    public long getEPCTasksConsumed() {
        return this._epcMode.longValue();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @ManagedAttribute(value="whether this execution strategy is idle", readonly=true)
    public boolean isIdle() {
        EatWhatYouKill eatWhatYouKill = this;
        synchronized (eatWhatYouKill) {
            return this._state == State.IDLE;
        }
    }

    @ManagedOperation(value="resets the task counts", impact="ACTION")
    public void reset() {
        this._pcMode.reset();
        this._epcMode.reset();
        this._pecMode.reset();
        this._picMode.reset();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public String toString() {
        EatWhatYouKill eatWhatYouKill = this;
        synchronized (eatWhatYouKill) {
            return this.toStringLocked();
        }
    }

    public String toStringLocked() {
        StringBuilder builder = new StringBuilder();
        this.getString(builder);
        this.getState(builder);
        return builder.toString();
    }

    private void getString(StringBuilder builder) {
        builder.append(this.getClass().getSimpleName());
        builder.append('@');
        builder.append(Integer.toHexString(this.hashCode()));
        builder.append('/');
        builder.append(this._producer);
        builder.append('/');
    }

    private void getState(StringBuilder builder) {
        builder.append((Object)this._state);
        builder.append("/p=");
        builder.append(this._pending);
        builder.append('/');
        builder.append(this._tryExecutor);
        builder.append("[pc=");
        builder.append(this.getPCTasksConsumed());
        builder.append(",pic=");
        builder.append(this.getPICTasksExecuted());
        builder.append(",pec=");
        builder.append(this.getPECTasksExecuted());
        builder.append(",epc=");
        builder.append(this.getEPCTasksConsumed());
        builder.append("]");
        builder.append("@");
        builder.append(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now()));
    }

    private static enum Mode {
        PRODUCE_CONSUME,
        PRODUCE_INVOKE_CONSUME,
        PRODUCE_EXECUTE_CONSUME,
        EXECUTE_PRODUCE_CONSUME;

    }

    private static enum State {
        IDLE,
        PRODUCING,
        REPRODUCING;

    }
}

