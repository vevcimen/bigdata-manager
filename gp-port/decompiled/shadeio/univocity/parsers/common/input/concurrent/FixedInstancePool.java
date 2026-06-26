/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.input.concurrent;

import java.util.Arrays;
import shadeio.univocity.parsers.common.input.concurrent.Entry;

abstract class FixedInstancePool<T> {
    final Entry<T>[] instancePool;
    private final int[] instanceIndexes;
    private int head = 0;
    private int tail = 0;
    int count = 0;
    private int lastInstanceIndex = 0;

    FixedInstancePool(int size) {
        this.instancePool = new Entry[size];
        this.instanceIndexes = new int[size];
        Arrays.fill(this.instanceIndexes, -1);
        this.instancePool[0] = new Entry<T>(this.newInstance(), 0);
        this.instanceIndexes[0] = 0;
    }

    protected abstract T newInstance();

    public synchronized Entry<T> allocate() {
        while (this.count == this.instancePool.length) {
            try {
                this.wait(50L);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return new Entry<T>(this.newInstance(), -1);
            }
        }
        int index = this.instanceIndexes[this.head];
        if (index == -1) {
            this.instanceIndexes[index] = index = ++this.lastInstanceIndex;
            this.instancePool[index] = new Entry<T>(this.newInstance(), index);
        }
        Entry<T> out = this.instancePool[index];
        ++this.head;
        if (this.head == this.instancePool.length) {
            this.head = 0;
        }
        ++this.count;
        return out;
    }

    public synchronized void release(Entry<T> e) {
        if (e.index != -1) {
            this.instanceIndexes[this.tail++] = e.index;
            if (this.tail == this.instancePool.length) {
                this.tail = 0;
            }
            --this.count;
        }
        this.notify();
    }
}

