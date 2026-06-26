/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.io;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;
import org.eclipse.jetty.io.ManagedSelector;
import org.eclipse.jetty.io.NetworkTrafficListener;
import org.eclipse.jetty.io.NetworkTrafficSocketChannelEndPoint;
import org.eclipse.jetty.util.thread.Scheduler;

@Deprecated
public class NetworkTrafficSelectChannelEndPoint
extends NetworkTrafficSocketChannelEndPoint {
    public NetworkTrafficSelectChannelEndPoint(SocketChannel channel, ManagedSelector selectSet, SelectionKey key, Scheduler scheduler, long idleTimeout, List<NetworkTrafficListener> listeners) {
        super(channel, selectSet, key, scheduler, idleTimeout, listeners);
    }
}

