/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.io;

import java.net.Socket;
import java.nio.ByteBuffer;

public interface NetworkTrafficListener {
    default public void opened(Socket socket) {
    }

    default public void incoming(Socket socket, ByteBuffer bytes) {
    }

    default public void outgoing(Socket socket, ByteBuffer bytes) {
    }

    default public void closed(Socket socket) {
    }

    @Deprecated
    public static class Adapter
    implements NetworkTrafficListener {
        @Override
        public void opened(Socket socket) {
        }

        @Override
        public void incoming(Socket socket, ByteBuffer bytes) {
        }

        @Override
        public void outgoing(Socket socket, ByteBuffer bytes) {
        }

        @Override
        public void closed(Socket socket) {
        }
    }
}

