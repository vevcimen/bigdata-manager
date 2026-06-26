/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.io.ssl;

import javax.net.ssl.SSLEngine;
import org.eclipse.jetty.io.Connection;

public interface ALPNProcessor {
    default public void init() {
    }

    default public boolean appliesTo(SSLEngine sslEngine) {
        return false;
    }

    default public void configure(SSLEngine sslEngine, Connection connection) {
    }

    public static interface Client
    extends ALPNProcessor {
    }

    public static interface Server
    extends ALPNProcessor {
    }
}

