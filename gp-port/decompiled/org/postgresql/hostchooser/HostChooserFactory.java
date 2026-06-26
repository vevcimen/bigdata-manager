/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.hostchooser;

import java.util.Properties;
import org.postgresql.hostchooser.HostChooser;
import org.postgresql.hostchooser.HostRequirement;
import org.postgresql.hostchooser.MultiHostChooser;
import org.postgresql.hostchooser.SingleHostChooser;
import org.postgresql.util.HostSpec;

public class HostChooserFactory {
    public static HostChooser createHostChooser(HostSpec[] hostSpecs, HostRequirement targetServerType, Properties info) {
        if (hostSpecs.length == 1) {
            return new SingleHostChooser(hostSpecs[0], targetServerType);
        }
        return new MultiHostChooser(hostSpecs, targetServerType, info);
    }
}

