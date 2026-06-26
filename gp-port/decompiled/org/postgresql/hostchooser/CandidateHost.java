/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.hostchooser;

import org.postgresql.hostchooser.HostRequirement;
import org.postgresql.util.HostSpec;

public class CandidateHost {
    public final HostSpec hostSpec;
    public final HostRequirement targetServerType;

    public CandidateHost(HostSpec hostSpec, HostRequirement targetServerType) {
        this.hostSpec = hostSpec;
        this.targetServerType = targetServerType;
    }
}

