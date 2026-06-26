/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import javax.net.ssl.SSLSocketFactory;

public abstract class WrappedFactory
extends SSLSocketFactory {
    protected SSLSocketFactory factory;

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return this.factory.createSocket(host, port);
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return this.factory.createSocket(host, port);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
        return this.factory.createSocket(host, port, localHost, localPort);
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return this.factory.createSocket(address, port, localAddress, localPort);
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
        return this.factory.createSocket(socket, host, port, autoClose);
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return this.factory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return this.factory.getSupportedCipherSuites();
    }
}

