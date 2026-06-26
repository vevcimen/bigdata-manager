/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.io;

import java.net.Socket;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import org.eclipse.jetty.io.ChannelEndPoint;
import org.eclipse.jetty.io.ManagedSelector;
import org.eclipse.jetty.util.thread.Scheduler;

public class SocketChannelEndPoint
extends ChannelEndPoint {
    public SocketChannelEndPoint(SelectableChannel channel, ManagedSelector selector, SelectionKey key, Scheduler scheduler) {
        this((SocketChannel)channel, selector, key, scheduler);
    }

    public SocketChannelEndPoint(SocketChannel channel, ManagedSelector selector, SelectionKey key, Scheduler scheduler) {
        super(channel, selector, key, scheduler);
    }

    public Socket getSocket() {
        return this.getChannel().socket();
    }
}

