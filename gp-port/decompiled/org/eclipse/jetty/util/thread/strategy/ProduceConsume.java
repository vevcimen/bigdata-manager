/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.util.thread.strategy;

import java.util.concurrent.Executor;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.thread.ExecutionStrategy;
import org.eclipse.jetty.util.thread.Locker;
import org.eclipse.jetty.util.thread.strategy.ExecuteProduceConsume;

public class ProduceConsume
implements ExecutionStrategy,
Runnable {
    private static final Logger LOG = Log.getLogger(ExecuteProduceConsume.class);
    private final Locker _locker = new Locker();
    private final ExecutionStrategy.Producer _producer;
    private final Executor _executor;
    private State _state = State.IDLE;

    public ProduceConsume(ExecutionStrategy.Producer producer, Executor executor) {
        this._producer = producer;
        this._executor = executor;
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public void produce() {
        lock = this._locker.lock();
        try {
            switch (1.$SwitchMap$org$eclipse$jetty$util$thread$strategy$ProduceConsume$State[this._state.ordinal()]) {
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
            if (lock != null) {
                lock.close();
            }
        }
        block20: while (true) {
            task = this._producer.produce();
            if (ProduceConsume.LOG.isDebugEnabled()) {
                ProduceConsume.LOG.debug("{} produced {}", new Object[]{this._producer, task});
            }
            if (task == null) {
                lock = this._locker.lock();
                try {
                    switch (1.$SwitchMap$org$eclipse$jetty$util$thread$strategy$ProduceConsume$State[this._state.ordinal()]) {
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
                    if (lock == null) continue;
                    lock.close();
                    continue;
                }
            }
            task.run();
        }
    }

    @Override
    public void dispatch() {
        this._executor.execute(this);
    }

    @Override
    public void run() {
        this.produce();
    }

    private static enum State {
        IDLE,
        PRODUCE,
        EXECUTE;

    }
}

