/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.hostchooser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.hostchooser.HostRequirement;
import org.postgresql.hostchooser.HostStatus;
import org.postgresql.util.HostSpec;

public class GlobalHostStatusTracker {
    private static final Map<HostSpec, HostSpecStatus> hostStatusMap = new HashMap<HostSpec, HostSpecStatus>();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void reportHostStatus(HostSpec hostSpec, HostStatus hostStatus) {
        long now = System.nanoTime() / 1000000L;
        Map<HostSpec, HostSpecStatus> map = hostStatusMap;
        synchronized (map) {
            HostSpecStatus hostSpecStatus = hostStatusMap.get(hostSpec);
            if (hostSpecStatus == null) {
                hostSpecStatus = new HostSpecStatus(hostSpec);
                hostStatusMap.put(hostSpec, hostSpecStatus);
            }
            hostSpecStatus.status = hostStatus;
            hostSpecStatus.lastUpdated = now;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static List<HostSpec> getCandidateHosts(HostSpec[] hostSpecs, HostRequirement targetServerType, long hostRecheckMillis) {
        ArrayList<HostSpec> candidates = new ArrayList<HostSpec>(hostSpecs.length);
        long latestAllowedUpdate = System.nanoTime() / 1000000L - hostRecheckMillis;
        Map<HostSpec, HostSpecStatus> map = hostStatusMap;
        synchronized (map) {
            for (HostSpec hostSpec : hostSpecs) {
                HostSpecStatus hostInfo = hostStatusMap.get(hostSpec);
                if (hostInfo != null && hostInfo.lastUpdated >= latestAllowedUpdate && !targetServerType.allowConnectingTo(hostInfo.status)) continue;
                candidates.add(hostSpec);
            }
        }
        return candidates;
    }

    static class HostSpecStatus {
        final HostSpec host;
        @Nullable HostStatus status;
        long lastUpdated;

        HostSpecStatus(HostSpec host) {
            this.host = host;
        }

        public String toString() {
            return this.host.toString() + '=' + (Object)((Object)this.status);
        }
    }
}

