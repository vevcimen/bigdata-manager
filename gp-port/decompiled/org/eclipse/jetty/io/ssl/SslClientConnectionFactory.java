/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.io.ssl;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLPeerUnverifiedException;
import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.io.ClientConnectionFactory;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.io.ssl.SslConnection;
import org.eclipse.jetty.io.ssl.SslHandshakeListener;
import org.eclipse.jetty.util.component.ContainerLifeCycle;
import org.eclipse.jetty.util.ssl.SslContextFactory;

public class SslClientConnectionFactory
implements ClientConnectionFactory {
    public static final String SSL_CONTEXT_FACTORY_CONTEXT_KEY = "ssl.context.factory";
    public static final String SSL_PEER_HOST_CONTEXT_KEY = "ssl.peer.host";
    public static final String SSL_PEER_PORT_CONTEXT_KEY = "ssl.peer.port";
    public static final String SSL_ENGINE_CONTEXT_KEY = "ssl.engine";
    private final SslContextFactory sslContextFactory;
    private final ByteBufferPool byteBufferPool;
    private final Executor executor;
    private final ClientConnectionFactory connectionFactory;
    private boolean _directBuffersForEncryption = true;
    private boolean _directBuffersForDecryption = true;
    private boolean _requireCloseMessage;

    public SslClientConnectionFactory(SslContextFactory sslContextFactory, ByteBufferPool byteBufferPool, Executor executor, ClientConnectionFactory connectionFactory) {
        this.sslContextFactory = Objects.requireNonNull(sslContextFactory, "Missing SslContextFactory");
        this.byteBufferPool = byteBufferPool;
        this.executor = executor;
        this.connectionFactory = connectionFactory;
    }

    public void setDirectBuffersForEncryption(boolean useDirectBuffers) {
        this._directBuffersForEncryption = useDirectBuffers;
    }

    public void setDirectBuffersForDecryption(boolean useDirectBuffers) {
        this._directBuffersForDecryption = useDirectBuffers;
    }

    public boolean isDirectBuffersForDecryption() {
        return this._directBuffersForDecryption;
    }

    public boolean isDirectBuffersForEncryption() {
        return this._directBuffersForEncryption;
    }

    @Deprecated
    public boolean isAllowMissingCloseMessage() {
        return !this.isRequireCloseMessage();
    }

    @Deprecated
    public void setAllowMissingCloseMessage(boolean allowMissingCloseMessage) {
        this.setRequireCloseMessage(!allowMissingCloseMessage);
    }

    public boolean isRequireCloseMessage() {
        return this._requireCloseMessage;
    }

    public void setRequireCloseMessage(boolean requireCloseMessage) {
        this._requireCloseMessage = requireCloseMessage;
    }

    @Override
    public Connection newConnection(EndPoint endPoint, Map<String, Object> context) throws IOException {
        String host = (String)context.get(SSL_PEER_HOST_CONTEXT_KEY);
        int port = (Integer)context.get(SSL_PEER_PORT_CONTEXT_KEY);
        SSLEngine engine = this.sslContextFactory instanceof SslEngineFactory ? ((SslEngineFactory)((Object)this.sslContextFactory)).newSslEngine(host, port, context) : this.sslContextFactory.newSSLEngine(host, port);
        engine.setUseClientMode(true);
        context.put(SSL_ENGINE_CONTEXT_KEY, engine);
        SslConnection sslConnection = this.newSslConnection(this.byteBufferPool, this.executor, endPoint, engine);
        SslConnection.DecryptedEndPoint appEndPoint = sslConnection.getDecryptedEndPoint();
        appEndPoint.setConnection(this.connectionFactory.newConnection(appEndPoint, context));
        sslConnection.addHandshakeListener(new HTTPSHandshakeListener(context));
        this.customize(sslConnection, context);
        return sslConnection;
    }

    protected SslConnection newSslConnection(ByteBufferPool byteBufferPool, Executor executor, EndPoint endPoint, SSLEngine engine) {
        return new SslConnection(byteBufferPool, executor, endPoint, engine, this.isDirectBuffersForEncryption(), this.isDirectBuffersForDecryption());
    }

    @Override
    public Connection customize(Connection connection, Map<String, Object> context) {
        if (connection instanceof SslConnection) {
            SslConnection sslConnection = (SslConnection)connection;
            sslConnection.setRenegotiationAllowed(this.sslContextFactory.isRenegotiationAllowed());
            sslConnection.setRenegotiationLimit(this.sslContextFactory.getRenegotiationLimit());
            sslConnection.setRequireCloseMessage(this.isRequireCloseMessage());
            ContainerLifeCycle connector = (ContainerLifeCycle)context.get("client.connector");
            connector.getBeans(SslHandshakeListener.class).forEach(sslConnection::addHandshakeListener);
        }
        return ClientConnectionFactory.super.customize(connection, context);
    }

    private class HTTPSHandshakeListener
    implements SslHandshakeListener {
        private final Map<String, Object> context;

        private HTTPSHandshakeListener(Map<String, Object> context) {
            this.context = context;
        }

        @Override
        public void handshakeSucceeded(SslHandshakeListener.Event event) throws SSLException {
            HostnameVerifier verifier = SslClientConnectionFactory.this.sslContextFactory.getHostnameVerifier();
            if (verifier != null) {
                String host = (String)this.context.get(SslClientConnectionFactory.SSL_PEER_HOST_CONTEXT_KEY);
                try {
                    if (!verifier.verify(host, event.getSSLEngine().getSession())) {
                        throw new SSLPeerUnverifiedException("Host name verification failed for host: " + host);
                    }
                }
                catch (SSLException x) {
                    throw x;
                }
                catch (Throwable x) {
                    throw (SSLException)new SSLPeerUnverifiedException("Host name verification failed for host: " + host).initCause(x);
                }
            }
        }
    }

    public static interface SslEngineFactory {
        public SSLEngine newSslEngine(String var1, int var2, Map<String, Object> var3);
    }
}

