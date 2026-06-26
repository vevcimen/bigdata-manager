/*
 * Decompiled with CFR 0.152.
 */
package com.zaxxer.hikari.metrics.prometheus;

import com.zaxxer.hikari.metrics.IMetricsTracker;
import com.zaxxer.hikari.metrics.MetricsTrackerFactory;
import com.zaxxer.hikari.metrics.PoolStats;
import com.zaxxer.hikari.metrics.prometheus.HikariCPCollector;
import com.zaxxer.hikari.metrics.prometheus.PrometheusMetricsTracker;

public class PrometheusMetricsTrackerFactory
implements MetricsTrackerFactory {
    private static HikariCPCollector collector;

    @Override
    public IMetricsTracker create(String poolName, PoolStats poolStats) {
        this.getCollector().add(poolName, poolStats);
        return new PrometheusMetricsTracker(poolName);
    }

    private HikariCPCollector getCollector() {
        if (collector == null) {
            collector = (HikariCPCollector)new HikariCPCollector().register();
        }
        return collector;
    }
}

