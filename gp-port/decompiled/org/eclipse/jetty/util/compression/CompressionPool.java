/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.util.compression;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import org.eclipse.jetty.util.component.AbstractLifeCycle;

public abstract class CompressionPool<T>
extends AbstractLifeCycle {
    public static final int INFINITE_CAPACITY = -1;
    private final Queue<T> _pool;
    private final AtomicInteger _numObjects = new AtomicInteger(0);
    private final int _capacity;

    public CompressionPool(int capacity) {
        this._capacity = capacity;
        this._pool = this._capacity == 0 ? null : new ConcurrentLinkedQueue();
    }

    protected abstract T newObject();

    protected abstract void end(T var1);

    protected abstract void reset(T var1);

    public T acquire() {
        T object;
        if (this._capacity == 0) {
            object = this.newObject();
        } else {
            object = this._pool.poll();
            if (object == null) {
                object = this.newObject();
            } else if (this._capacity > 0) {
                this._numObjects.decrementAndGet();
            }
        }
        return object;
    }

    public void release(T object) {
        block5: {
            if (object == null) {
                return;
            }
            if (this._capacity == 0 || !this.isRunning()) {
                this.end(object);
                return;
            }
            if (this._capacity < 0) {
                this.reset(object);
                this._pool.add(object);
            } else {
                int d;
                do {
                    if ((d = this._numObjects.get()) < this._capacity) continue;
                    this.end(object);
                    break block5;
                } while (!this._numObjects.compareAndSet(d, d + 1));
                this.reset(object);
                this._pool.add(object);
            }
        }
    }

    @Override
    public void doStop() {
        T t = this._pool.poll();
        while (t != null) {
            this.end(t);
            t = this._pool.poll();
        }
        this._numObjects.set(0);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(this.getClass().getSimpleName());
        str.append('@').append(Integer.toHexString(this.hashCode()));
        str.append('{').append(this.getState());
        str.append(",size=").append(this._pool == null ? -1 : this._pool.size());
        str.append(",capacity=").append(this._capacity <= 0 ? "UNLIMITED" : Integer.valueOf(this._capacity));
        str.append('}');
        return str.toString();
    }
}

