/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.hostchooser;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import org.postgresql.hostchooser.CandidateHost;
import org.postgresql.hostchooser.HostChooser;
import org.postgresql.hostchooser.HostRequirement;
import org.postgresql.util.HostSpec;

class SingleHostChooser
implements HostChooser {
    private final Collection<CandidateHost> candidateHost;

    SingleHostChooser(HostSpec hostSpec, HostRequirement targetServerType) {
        this.candidateHost = Collections.singletonList(new CandidateHost(hostSpec, targetServerType));
    }

    @Override
    public Iterator<CandidateHost> iterator() {
        return this.candidateHost.iterator();
    }
}

