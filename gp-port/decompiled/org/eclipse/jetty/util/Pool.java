/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.eclipse.jetty.util.AtomicBiInteger;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.component.Dumpable;
import org.eclipse.jetty.util.component.DumpableCollection;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.thread.Locker;

public class Pool<T>
implements AutoCloseable,
Dumpable {
    private static final Logger LOGGER = Log.getLogger(Pool.class);
    private final List<Entry> entries = new CopyOnWriteArrayList<Entry>();
    private final int maxEntries;
    private final StrategyType strategyType;
    private final Locker locker = new Locker();
    private final ThreadLocal<Entry> cache;
    private final AtomicInteger nextIndex;
    private volatile boolean closed;
    private volatile int maxMultiplex = 1;
    private volatile int maxUsageCount = -1;

    public Pool(StrategyType strategyType, int maxEntries) {
        this(strategyType, maxEntries, false);
    }

    public Pool(StrategyType strategyType, int maxEntries, boolean cache) {
        this.maxEntries = maxEntries;
        this.strategyType = strategyType;
        this.cache = cache ? new ThreadLocal() : null;
        this.nextIndex = strategyType == StrategyType.ROUND_ROBIN ? new AtomicInteger() : null;
    }

    public int getReservedCount() {
        return (int)this.entries.stream().filter(Entry::isReserved).count();
    }

    public int getIdleCount() {
        return (int)this.entries.stream().filter(Entry::isIdle).count();
    }

    public int getInUseCount() {
        return (int)this.entries.stream().filter(Entry::isInUse).count();
    }

    public int getClosedCount() {
        return (int)this.entries.stream().filter(Entry::isClosed).count();
    }

    public int getMaxEntries() {
        return this.maxEntries;
    }

    public int getMaxMultiplex() {
        return this.maxMultiplex;
    }

    public final void setMaxMultiplex(int maxMultiplex) {
        if (maxMultiplex < 1) {
            throw new IllegalArgumentException("Max multiplex must be >= 1");
        }
        this.maxMultiplex = maxMultiplex;
    }

    public int getMaxUsageCount() {
        return this.maxUsageCount;
    }

    public final void setMaxUsageCount(int maxUsageCount) {
        List<Closeable> copy;
        if (maxUsageCount == 0) {
            throw new IllegalArgumentException("Max usage count must be != 0");
        }
        this.maxUsageCount = maxUsageCount;
        try (Locker.Lock l = this.locker.lock();){
            if (this.closed) {
                return;
            }
            copy = this.entries.stream().filter(entry -> entry.isIdleAndOverUsed() && this.remove((Entry)entry) && ((Entry)entry).pooled instanceof Closeable).map(entry -> (Closeable)((Entry)entry).pooled).collect(Collectors.toList());
        }
        copy.forEach(IO::close);
    }

    @Deprecated
    public Entry reserve(int allotment) {
        try (Locker.Lock l = this.locker.lock();){
            if (this.closed) {
                Entry entry = null;
                return entry;
            }
            int space = this.maxEntries - this.entries.size();
            if (space <= 0) {
                Entry entry = null;
                return entry;
            }
            if (allotment >= 0 && this.getReservedCount() * this.getMaxMultiplex() >= allotment) {
                Entry entry = null;
                return entry;
            }
            Entry entry = new Entry();
            this.entries.add(entry);
            Entry entry2 = entry;
            return entry2;
        }
    }

    public Entry reserve() {
        try (Locker.Lock l = this.locker.lock();){
            if (this.closed) {
                Entry entry = null;
                return entry;
            }
            if (this.entries.size() >= this.maxEntries) {
                Entry entry = null;
                return entry;
            }
            Entry entry = new Entry();
            this.entries.add(entry);
            Entry entry2 = entry;
            return entry2;
        }
    }

    @Deprecated
    public Entry acquireAt(int idx) {
        if (this.closed) {
            return null;
        }
        try {
            Entry entry = this.entries.get(idx);
            if (entry.tryAcquire()) {
                return entry;
            }
        }
        catch (IndexOutOfBoundsException indexOutOfBoundsException) {
            // empty catch block
        }
        return null;
    }

    public Entry acquire() {
        Entry entry;
        if (this.closed) {
            return null;
        }
        int size = this.entries.size();
        if (size == 0) {
            return null;
        }
        if (this.cache != null && (entry = this.cache.get()) != null && entry.tryAcquire()) {
            return entry;
        }
        int index = this.startIndex(size);
        int tries = size;
        while (tries-- > 0) {
            try {
                Entry entry2 = this.entries.get(index);
                if (entry2 != null && entry2.tryAcquire()) {
                    return entry2;
                }
            }
            catch (IndexOutOfBoundsException e) {
                LOGGER.ignore(e);
                size = this.entries.size();
                if (size == 0) break;
            }
            index = (index + 1) % size;
        }
        return null;
    }

    private int startIndex(int size) {
        switch (this.strategyType) {
            case FIRST: {
                return 0;
            }
            case RANDOM: {
                return ThreadLocalRandom.current().nextInt(size);
            }
            case ROUND_ROBIN: {
                return this.nextIndex.getAndUpdate(c -> Math.max(0, c + 1)) % size;
            }
            case THREAD_ID: {
                return (int)(Thread.currentThread().getId() % (long)size);
            }
        }
        throw new IllegalArgumentException("Unknown strategy type: " + (Object)((Object)this.strategyType));
    }

    public Entry acquire(Function<Entry, T> creator) {
        T value;
        Entry entry = this.acquire();
        if (entry != null) {
            return entry;
        }
        entry = this.reserve();
        if (entry == null) {
            return null;
        }
        try {
            value = creator.apply(entry);
        }
        catch (Throwable th) {
            this.remove(entry);
            throw th;
        }
        if (value == null) {
            this.remove(entry);
            return null;
        }
        return entry.enable(value, true) ? entry : null;
    }

    public boolean release(Entry entry) {
        if (this.closed) {
            return false;
        }
        boolean released = entry.tryRelease();
        if (released && this.cache != null) {
            this.cache.set(entry);
        }
        return released;
    }

    public boolean remove(Entry entry) {
        if (this.closed) {
            return false;
        }
        if (!entry.tryRemove()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Attempt to remove an object from the pool that is still in use: {}", entry);
            }
            return false;
        }
        boolean removed = this.entries.remove(entry);
        if (!removed && LOGGER.isDebugEnabled()) {
            LOGGER.debug("Attempt to remove an object from the pool that does not exist: {}", entry);
        }
        return removed;
    }

    public boolean isClosed() {
        return this.closed;
    }

    @Override
    public void close() {
        ArrayList<Entry> copy;
        try (Locker.Lock l = this.locker.lock();){
            this.closed = true;
            copy = new ArrayList<Entry>(this.entries);
            this.entries.clear();
        }
        for (Entry entry : copy) {
            if (!entry.tryRemove() || !(entry.pooled instanceof Closeable)) continue;
            IO.close((Closeable)entry.pooled);
        }
    }

    public int size() {
        return this.entries.size();
    }

    public Collection<Entry> values() {
        return Collections.unmodifiableCollection(this.entries);
    }

    @Override
    public void dump(Appendable out, String indent) throws IOException {
        Dumpable.dumpObjects(out, indent, this, new DumpableCollection("entries", this.entries));
    }

    public String toString() {
        return String.format("%s@%x[size=%d closed=%s]", this.getClass().getSimpleName(), this.hashCode(), this.entries.size(), this.closed);
    }

    public class Entry {
        private final AtomicBiInteger state = new AtomicBiInteger(Integer.MIN_VALUE, 0);
        private T pooled;

        Entry() {
        }

        void setUsageCount(int usageCount) {
            this.state.getAndSetHi(usageCount);
        }

        public boolean enable(T pooled, boolean acquire) {
            int usage;
            Objects.requireNonNull(pooled);
            if (this.state.getHi() != Integer.MIN_VALUE) {
                if (this.state.getHi() == -1) {
                    return false;
                }
                throw new IllegalStateException("Entry already enabled: " + this);
            }
            this.pooled = pooled;
            int n = usage = acquire ? 1 : 0;
            if (!this.state.compareAndSet(Integer.MIN_VALUE, usage, 0, usage)) {
                this.pooled = null;
                if (this.state.getHi() == -1) {
                    return false;
                }
                throw new IllegalStateException("Entry already enabled: " + this);
            }
            return true;
        }

        public T getPooled() {
            return this.pooled;
        }

        public boolean release() {
            return Pool.this.release(this);
        }

        public boolean remove() {
            return Pool.this.remove(this);
        }

        boolean tryAcquire() {
            int multiplexingCount;
            int usageCount;
            int newUsageCount;
            long encoded;
            do {
                boolean closed = (usageCount = AtomicBiInteger.getHi(encoded = this.state.get())) < 0;
                multiplexingCount = AtomicBiInteger.getLo(encoded);
                int currentMaxUsageCount = Pool.this.maxUsageCount;
                if (!closed && multiplexingCount < Pool.this.maxMultiplex && (currentMaxUsageCount <= 0 || usageCount < currentMaxUsageCount)) continue;
                return false;
            } while (!this.state.compareAndSet(encoded, newUsageCount = usageCount == Integer.MAX_VALUE ? Integer.MAX_VALUE : usageCount + 1, multiplexingCount + 1));
            return true;
        }

        boolean tryRelease() {
            int newMultiplexingCount;
            int usageCount;
            long encoded;
            do {
                boolean closed;
                boolean bl = closed = (usageCount = AtomicBiInteger.getHi(encoded = this.state.get())) < 0;
                if (closed) {
                    return false;
                }
                newMultiplexingCount = AtomicBiInteger.getLo(encoded) - 1;
                if (newMultiplexingCount >= 0) continue;
                throw new IllegalStateException("Cannot release an already released entry");
            } while (!this.state.compareAndSet(encoded, usageCount, newMultiplexingCount));
            int currentMaxUsageCount = Pool.this.maxUsageCount;
            boolean overUsed = currentMaxUsageCount > 0 && usageCount >= currentMaxUsageCount;
            return !overUsed || newMultiplexingCount != 0;
        }

        boolean tryRemove() {
            int newMultiplexCount;
            int multiplexCount;
            long encoded;
            int usageCount;
            boolean removed;
            while (!(removed = this.state.compareAndSet(usageCount = AtomicBiInteger.getHi(encoded = this.state.get()), -1, multiplexCount = AtomicBiInteger.getLo(encoded), newMultiplexCount = Math.max(multiplexCount - 1, 0)))) {
            }
            return newMultiplexCount == 0;
        }

        public boolean isClosed() {
            return this.state.getHi() < 0;
        }

        public boolean isReserved() {
            return this.state.getHi() == Integer.MIN_VALUE;
        }

        public boolean isIdle() {
            long encoded = this.state.get();
            return AtomicBiInteger.getHi(encoded) >= 0 && AtomicBiInteger.getLo(encoded) == 0;
        }

        public boolean isInUse() {
            long encoded = this.state.get();
            return AtomicBiInteger.getHi(encoded) >= 0 && AtomicBiInteger.getLo(encoded) > 0;
        }

        public boolean isOverUsed() {
            int currentMaxUsageCount = Pool.this.maxUsageCount;
            int usageCount = this.state.getHi();
            return currentMaxUsageCount > 0 && usageCount >= currentMaxUsageCount;
        }

        boolean isIdleAndOverUsed() {
            int currentMaxUsageCount = Pool.this.maxUsageCount;
            long encoded = this.state.get();
            int usageCount = AtomicBiInteger.getHi(encoded);
            int multiplexCount = AtomicBiInteger.getLo(encoded);
            return currentMaxUsageCount > 0 && usageCount >= currentMaxUsageCount && multiplexCount == 0;
        }

        public int getUsageCount() {
            return Math.max(this.state.getHi(), 0);
        }

        public String toString() {
            long encoded = this.state.get();
            int usageCount = AtomicBiInteger.getHi(encoded);
            int multiplexCount = AtomicBiInteger.getLo(encoded);
            String state = usageCount < 0 ? "CLOSED" : (multiplexCount == 0 ? "IDLE" : "INUSE");
            return String.format("%s@%x{%s, usage=%d, multiplex=%d/%d, pooled=%s}", this.getClass().getSimpleName(), this.hashCode(), state, Math.max(usageCount, 0), Math.max(multiplexCount, 0), Pool.this.getMaxMultiplex(), this.pooled);
        }
    }

    public static enum StrategyType {
        FIRST,
        RANDOM,
        THREAD_ID,
        ROUND_ROBIN;

    }
}

