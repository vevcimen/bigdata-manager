/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server;

import org.eclipse.jetty.io.ConnectionStatistics;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;

@Deprecated
public class ServerConnectionStatistics
extends ConnectionStatistics {
    public static void addToAllConnectors(Server server) {
        for (Connector connector : server.getConnectors()) {
            connector.addBean(new ConnectionStatistics());
        }
    }
}

