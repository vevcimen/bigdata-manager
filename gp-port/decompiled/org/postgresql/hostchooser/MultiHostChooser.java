/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.hostchooser;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import org.postgresql.PGProperty;
import org.postgresql.hostchooser.CandidateHost;
import org.postgresql.hostchooser.GlobalHostStatusTracker;
import org.postgresql.hostchooser.HostChooser;
import org.postgresql.hostchooser.HostRequirement;
import org.postgresql.util.HostSpec;
import org.postgresql.util.PSQLException;

class MultiHostChooser
implements HostChooser {
    private HostSpec[] hostSpecs;
    private final HostRequirement targetServerType;
    private int hostRecheckTime;
    private boolean loadBalance;

    MultiHostChooser(HostSpec[] hostSpecs, HostRequirement targetServerType, Properties info) {
        this.hostSpecs = hostSpecs;
        this.targetServerType = targetServerType;
        try {
            this.hostRecheckTime = PGProperty.HOST_RECHECK_SECONDS.getInt(info) * 1000;
            this.loadBalance = PGProperty.LOAD_BALANCE_HOSTS.getBoolean(info);
        }
        catch (PSQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iterator<CandidateHost> iterator() {
        Iterator<CandidateHost> res = this.candidateIterator();
        if (!res.hasNext()) {
            List<HostSpec> allHosts = Arrays.asList(this.hostSpecs);
            if (this.loadBalance) {
                allHosts = new ArrayList<HostSpec>(allHosts);
                Collections.shuffle(allHosts);
            }
            res = this.withReqStatus(this.targetServerType, allHosts).iterator();
        }
        return res;
    }

    private Iterator<CandidateHost> candidateIterator() {
        if (this.targetServerType != HostRequirement.preferSecondary && this.targetServerType != HostRequirement.preferPrimary) {
            return this.getCandidateHosts(this.targetServerType).iterator();
        }
        HostRequirement preferredServerType = this.targetServerType == HostRequirement.preferSecondary ? HostRequirement.secondary : HostRequirement.primary;
        List<CandidateHost> preferred = this.getCandidateHosts(preferredServerType);
        List<CandidateHost> any = this.getCandidateHosts(HostRequirement.any);
        if (!preferred.isEmpty() && !any.isEmpty() && preferred.get((int)(preferred.size() - 1)).hostSpec.equals(any.get((int)0).hostSpec)) {
            preferred = this.rtrim(1, preferred);
        }
        return this.append(preferred, any).iterator();
    }

    private List<CandidateHost> getCandidateHosts(HostRequirement hostRequirement) {
        List<HostSpec> candidates = GlobalHostStatusTracker.getCandidateHosts(this.hostSpecs, hostRequirement, this.hostRecheckTime);
        if (this.loadBalance) {
            Collections.shuffle(candidates);
        }
        return this.withReqStatus(hostRequirement, candidates);
    }

    private List<CandidateHost> withReqStatus(final HostRequirement requirement, final List<HostSpec> hosts) {
        return new AbstractList<CandidateHost>(){

            @Override
            public CandidateHost get(int index) {
                return new CandidateHost((HostSpec)hosts.get(index), requirement);
            }

            @Override
            public int size() {
                return hosts.size();
            }
        };
    }

    private <T> List<T> append(final List<T> a, final List<T> b) {
        return new AbstractList<T>(){

            @Override
            public T get(int index) {
                return index < a.size() ? a.get(index) : b.get(index - a.size());
            }

            @Override
            public int size() {
                return a.size() + b.size();
            }
        };
    }

    private <T> List<T> rtrim(final int size, final List<T> a) {
        return new AbstractList<T>(){

            @Override
            public T get(int index) {
                return a.get(index);
            }

            @Override
            public int size() {
                return Math.max(0, a.size() - size);
            }
        };
    }
}

