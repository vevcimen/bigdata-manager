/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.util.thread.strategy;

import java.util.concurrent.Executor;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.thread.ExecutionStrategy;
import org.eclipse.jetty.util.thread.Invocable;
import org.eclipse.jetty.util.thread.Locker;

public class ProduceExecuteConsume
implements ExecutionStrategy {
    private static final Logger LOG = Log.getLogger(ProduceExecuteConsume.class);
    private final Locker _locker = new Locker();
    private final ExecutionStrategy.Producer _producer;
    private final Executor _executor;
    private State _state = State.IDLE;

    public ProduceExecuteConsume(ExecutionStrategy.Producer producer, Executor executor) {
        this._producer = producer;
        this._executor = executor;
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public void produce() {
        locked = this._locker.lock();
        try {
            switch (1.$SwitchMap$org$eclipse$jetty$util$thread$strategy$ProduceExecuteConsume$State[this._state.ordinal()]) {
                case 1: {
                    this._state = State.PRODUCE;
                    ** break;
lbl7:
                    // 1 sources

                    break;
                }
                case 2: 
                case 3: {
                    this._state = State.EXECUTE;
                    return;
                }
                ** default:
lbl12:
                // 1 sources

                break;
            }
        }
        finally {
            if (locked != null) {
                locked.close();
            }
        }
        block20: while (true) {
            task = this._producer.produce();
            if (ProduceExecuteConsume.LOG.isDebugEnabled()) {
                ProduceExecuteConsume.LOG.debug("{} produced {}", new Object[]{this._producer, task});
            }
            if (task == null) {
                locked = this._locker.lock();
                try {
                    switch (1.$SwitchMap$org$eclipse$jetty$util$thread$strategy$ProduceExecuteConsume$State[this._state.ordinal()]) {
                        case 1: {
                            throw new IllegalStateException();
                        }
                        case 2: {
                            this._state = State.IDLE;
                            return;
                        }
                        case 3: {
                            this._state = State.PRODUCE;
                            continue block20;
                        }
                        ** default:
lbl35:
                        // 1 sources

                        break;
                    }
                }
                finally {
                    if (locked == null) continue;
                    locked.close();
                    continue;
                }
            }
            if (Invocable.getInvocationType(task) == Invocable.InvocationType.NON_BLOCKING) {
                task.run();
                continue;
            }
            this._executor.execute(task);
        }
    }

    @Override
    public void dispatch() {
        this._executor.execute(() -> this.produce());
    }

    private static enum State {
        IDLE,
        PRODUCE,
        EXECUTE;

    }
}

