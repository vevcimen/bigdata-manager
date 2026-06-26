/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core.v3.adaptivefetch;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.postgresql.PGProperty;
import org.postgresql.core.Query;
import org.postgresql.core.v3.adaptivefetch.AdaptiveFetchCacheEntry;

public class AdaptiveFetchCache {
    private final Map<String, AdaptiveFetchCacheEntry> adaptiveFetchInfoMap = new HashMap<String, AdaptiveFetchCacheEntry>();
    private boolean adaptiveFetch = false;
    private int minimumAdaptiveFetchSize = 0;
    private int maximumAdaptiveFetchSize = -1;
    private long maximumResultBufferSize = -1L;

    public AdaptiveFetchCache(long maximumResultBufferSize, Properties info) throws SQLException {
        this.adaptiveFetch = PGProperty.ADAPTIVE_FETCH.getBoolean(info);
        this.minimumAdaptiveFetchSize = PGProperty.ADAPTIVE_FETCH_MINIMUM.getInt(info);
        this.maximumAdaptiveFetchSize = PGProperty.ADAPTIVE_FETCH_MAXIMUM.getInt(info);
        this.maximumResultBufferSize = maximumResultBufferSize;
    }

    public void addNewQuery(boolean adaptiveFetch, @NonNull Query query) {
        if (adaptiveFetch && this.maximumResultBufferSize != -1L) {
            String sql = query.getNativeSql().trim();
            AdaptiveFetchCacheEntry adaptiveFetchCacheEntry = this.adaptiveFetchInfoMap.get(sql);
            if (adaptiveFetchCacheEntry == null) {
                adaptiveFetchCacheEntry = new AdaptiveFetchCacheEntry();
            }
            adaptiveFetchCacheEntry.incrementCounter();
            this.adaptiveFetchInfoMap.put(sql, adaptiveFetchCacheEntry);
        }
    }

    public void updateQueryFetchSize(boolean adaptiveFetch, @NonNull Query query, int maximumRowSizeBytes) {
        int adaptiveMaximumRowSize;
        String sql;
        AdaptiveFetchCacheEntry adaptiveFetchCacheEntry;
        if (adaptiveFetch && this.maximumResultBufferSize != -1L && (adaptiveFetchCacheEntry = this.adaptiveFetchInfoMap.get(sql = query.getNativeSql().trim())) != null && (adaptiveMaximumRowSize = adaptiveFetchCacheEntry.getMaximumRowSizeBytes()) < maximumRowSizeBytes && maximumRowSizeBytes > 0) {
            int newFetchSize = (int)(this.maximumResultBufferSize / (long)maximumRowSizeBytes);
            newFetchSize = this.adjustFetchSize(newFetchSize);
            adaptiveFetchCacheEntry.setMaximumRowSizeBytes(maximumRowSizeBytes);
            adaptiveFetchCacheEntry.setSize(newFetchSize);
            this.adaptiveFetchInfoMap.put(sql, adaptiveFetchCacheEntry);
        }
    }

    public int getFetchSizeForQuery(boolean adaptiveFetch, @NonNull Query query) {
        String sql;
        AdaptiveFetchCacheEntry adaptiveFetchCacheEntry;
        if (adaptiveFetch && this.maximumResultBufferSize != -1L && (adaptiveFetchCacheEntry = this.adaptiveFetchInfoMap.get(sql = query.getNativeSql().trim())) != null) {
            return adaptiveFetchCacheEntry.getSize();
        }
        return -1;
    }

    public void removeQuery(boolean adaptiveFetch, @NonNull Query query) {
        String sql;
        AdaptiveFetchCacheEntry adaptiveFetchCacheEntry;
        if (adaptiveFetch && this.maximumResultBufferSize != -1L && (adaptiveFetchCacheEntry = this.adaptiveFetchInfoMap.get(sql = query.getNativeSql().trim())) != null) {
            adaptiveFetchCacheEntry.decrementCounter();
            if (adaptiveFetchCacheEntry.getCounter() < 1) {
                this.adaptiveFetchInfoMap.remove(sql);
            } else {
                this.adaptiveFetchInfoMap.put(sql, adaptiveFetchCacheEntry);
            }
        }
    }

    private int adjustFetchSize(int actualSize) {
        int size = this.adjustMaximumFetchSize(actualSize);
        size = this.adjustMinimumFetchSize(size);
        return size;
    }

    private int adjustMinimumFetchSize(int actualSize) {
        if (this.minimumAdaptiveFetchSize == 0) {
            return actualSize;
        }
        if (this.minimumAdaptiveFetchSize > actualSize) {
            return this.minimumAdaptiveFetchSize;
        }
        return actualSize;
    }

    private int adjustMaximumFetchSize(int actualSize) {
        if (this.maximumAdaptiveFetchSize == -1) {
            return actualSize;
        }
        if (this.maximumAdaptiveFetchSize < actualSize) {
            return this.maximumAdaptiveFetchSize;
        }
        return actualSize;
    }

    public boolean getAdaptiveFetch() {
        return this.adaptiveFetch;
    }

    public void setAdaptiveFetch(boolean adaptiveFetch) {
        this.adaptiveFetch = adaptiveFetch;
    }
}

