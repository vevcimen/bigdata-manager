/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.hostchooser;

import java.util.Iterator;
import org.postgresql.hostchooser.CandidateHost;

public interface HostChooser
extends Iterable<CandidateHost> {
    @Override
    public Iterator<CandidateHost> iterator();
}

