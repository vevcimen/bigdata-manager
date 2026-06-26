/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server;

import java.nio.ByteBuffer;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.io.AbstractConnection;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.io.ssl.SslConnection;
import org.eclipse.jetty.io.ssl.SslHandshakeListener;
import org.eclipse.jetty.server.AbstractConnectionFactory;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.util.annotation.Name;
import org.eclipse.jetty.util.component.ContainerLifeCycle;
import org.eclipse.jetty.util.ssl.SslContextFactory;

public class SslConnectionFactory
extends AbstractConnectionFactory
implements ConnectionFactory.Detecting {
    private static final int TLS_ALERT_FRAME_TYPE = 21;
    private static final int TLS_HANDSHAKE_FRAME_TYPE = 22;
    private static final int TLS_MAJOR_VERSION = 3;
    private final SslContextFactory _sslContextFactory;
    private final String _nextProtocol;
    private boolean _directBuffersForEncryption = false;
    private boolean _directBuffersForDecryption = false;

    public SslConnectionFactory() {
        this(HttpVersion.HTTP_1_1.asString());
    }

    public SslConnectionFactory(@Name(value="next") String nextProtocol) {
        this((SslContextFactory)null, nextProtocol);
    }

    public SslConnectionFactory(@Name(value="sslContextFactory") SslContextFactory factory, @Name(value="next") String nextProtocol) {
        super("SSL");
        this._sslContextFactory = factory == null ? new SslContextFactory.Server() : factory;
        this._nextProtocol = nextProtocol;
        this.addBean(this._sslContextFactory);
    }

    public SslContextFactory getSslContextFactory() {
        return this._sslContextFactory;
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

    public String getNextProtocol() {
        return this._nextProtocol;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        SSLEngine engine = this._sslContextFactory.newSSLEngine();
        engine.setUseClientMode(false);
        SSLSession session = engine.getSession();
        if (session.getPacketBufferSize() > this.getInputBufferSize()) {
            this.setInputBufferSize(session.getPacketBufferSize());
        }
    }

    @Override
    public ConnectionFactory.Detecting.Detection detect(ByteBuffer buffer) {
        if (buffer.remaining() < 2) {
            return ConnectionFactory.Detecting.Detection.NEED_MORE_BYTES;
        }
        int tlsFrameType = buffer.get(0) & 0xFF;
        int tlsMajorVersion = buffer.get(1) & 0xFF;
        boolean seemsSsl = (tlsFrameType == 22 || tlsFrameType == 21) && tlsMajorVersion == 3;
        return seemsSsl ? ConnectionFactory.Detecting.Detection.RECOGNIZED : ConnectionFactory.Detecting.Detection.NOT_RECOGNIZED;
    }

    @Override
    public Connection newConnection(Connector connector, EndPoint endPoint) {
        SSLEngine engine = this._sslContextFactory.newSSLEngine(endPoint.getRemoteAddress());
        engine.setUseClientMode(false);
        SslConnection sslConnection = this.newSslConnection(connector, endPoint, engine);
        sslConnection.setRenegotiationAllowed(this._sslContextFactory.isRenegotiationAllowed());
        sslConnection.setRenegotiationLimit(this._sslContextFactory.getRenegotiationLimit());
        this.configure(sslConnection, connector, endPoint);
        ConnectionFactory next = connector.getConnectionFactory(this._nextProtocol);
        SslConnection.DecryptedEndPoint decryptedEndPoint = sslConnection.getDecryptedEndPoint();
        Connection connection = next.newConnection(connector, decryptedEndPoint);
        decryptedEndPoint.setConnection(connection);
        return sslConnection;
    }

    protected SslConnection newSslConnection(Connector connector, EndPoint endPoint, SSLEngine engine) {
        return new SslConnection(connector.getByteBufferPool(), connector.getExecutor(), endPoint, engine, this.isDirectBuffersForEncryption(), this.isDirectBuffersForDecryption());
    }

    @Override
    protected AbstractConnection configure(AbstractConnection connection, Connector connector, EndPoint endPoint) {
        if (connection instanceof SslConnection) {
            SslConnection sslConnection = (SslConnection)connection;
            if (connector instanceof ContainerLifeCycle) {
                ContainerLifeCycle container = (ContainerLifeCycle)((Object)connector);
                container.getBeans(SslHandshakeListener.class).forEach(sslConnection::addHandshakeListener);
            }
            this.getBeans(SslHandshakeListener.class).forEach(sslConnection::addHandshakeListener);
        }
        return super.configure(connection, connector, endPoint);
    }

    @Override
    public String toString() {
        return String.format("%s@%x{%s->%s}", this.getClass().getSimpleName(), this.hashCode(), this.getProtocol(), this._nextProtocol);
    }
}

