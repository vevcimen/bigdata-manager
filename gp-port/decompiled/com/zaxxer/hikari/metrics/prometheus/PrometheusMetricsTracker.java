/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.prometheus.client.Counter
 *  io.prometheus.client.Counter$Builder
 *  io.prometheus.client.Counter$Child
 *  io.prometheus.client.Summary
 *  io.prometheus.client.Summary$Builder
 *  io.prometheus.client.Summary$Child
 */
package com.zaxxer.hikari.metrics.prometheus;

import com.zaxxer.hikari.metrics.IMetricsTracker;
import io.prometheus.client.Counter;
import io.prometheus.client.Summary;
import java.util.concurrent.TimeUnit;

class PrometheusMetricsTracker
implements IMetricsTracker {
    private static final Counter CONNECTION_TIMEOUT_COUNTER = (Counter)((Counter.Builder)((Counter.Builder)((Counter.Builder)Counter.build().name("hikaricp_connection_timeout_total")).labelNames(new String[]{"pool"})).help("Connection timeout total count")).register();
    private static final Summary ELAPSED_ACQUIRED_SUMMARY = PrometheusMetricsTracker.registerSummary("hikaricp_connection_acquired_nanos", "Connection acquired time (ns)");
    private static final Summary ELAPSED_BORROWED_SUMMARY = PrometheusMetricsTracker.registerSummary("hikaricp_connection_usage_millis", "Connection usage (ms)");
    private static final Summary ELAPSED_CREATION_SUMMARY = PrometheusMetricsTracker.registerSummary("hikaricp_connection_creation_millis", "Connection creation (ms)");
    private final Counter.Child connectionTimeoutCounterChild;
    private final Summary.Child elapsedAcquiredSummaryChild;
    private final Summary.Child elapsedBorrowedSummaryChild;
    private final Summary.Child elapsedCreationSummaryChild;

    private static Summary registerSummary(String name, String help) {
        return (Summary)((Summary.Builder)((Summary.Builder)((Summary.Builder)Summary.build().name(name)).labelNames(new String[]{"pool"})).help(help)).quantile(0.5, 0.05).quantile(0.95, 0.01).quantile(0.99, 0.001).maxAgeSeconds(TimeUnit.MINUTES.toSeconds(5L)).ageBuckets(5).register();
    }

    PrometheusMetricsTracker(String poolName) {
        this.connectionTimeoutCounterChild = (Counter.Child)CONNECTION_TIMEOUT_COUNTER.labels(new String[]{poolName});
        this.elapsedAcquiredSummaryChild = (Summary.Child)ELAPSED_ACQUIRED_SUMMARY.labels(new String[]{poolName});
        this.elapsedBorrowedSummaryChild = (Summary.Child)ELAPSED_BORROWED_SUMMARY.labels(new String[]{poolName});
        this.elapsedCreationSummaryChild = (Summary.Child)ELAPSED_CREATION_SUMMARY.labels(new String[]{poolName});
    }

    @Override
    public void recordConnectionAcquiredNanos(long elapsedAcquiredNanos) {
        this.elapsedAcquiredSummaryChild.observe((double)elapsedAcquiredNanos);
    }

    @Override
    public void recordConnectionUsageMillis(long elapsedBorrowedMillis) {
        this.elapsedBorrowedSummaryChild.observe((double)elapsedBorrowedMillis);
    }

    @Override
    public void recordConnectionCreatedMillis(long connectionCreatedMillis) {
        this.elapsedCreationSummaryChild.observe((double)connectionCreatedMillis);
    }

    @Override
    public void recordConnectionTimeout() {
        this.connectionTimeoutCounterChild.inc();
    }
}

