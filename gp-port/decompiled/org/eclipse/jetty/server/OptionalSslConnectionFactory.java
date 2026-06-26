/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.DetectorConnectionFactory;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

@Deprecated
public class OptionalSslConnectionFactory
extends DetectorConnectionFactory {
    private static final Logger LOG = Log.getLogger(OptionalSslConnectionFactory.class);
    private final String _nextProtocol;

    public OptionalSslConnectionFactory(SslConnectionFactory sslConnectionFactory, String nextProtocol) {
        super(sslConnectionFactory);
        this._nextProtocol = nextProtocol;
    }

    @Override
    protected void nextProtocol(Connector connector, EndPoint endPoint, ByteBuffer buffer) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("OptionalSSL TLS detection unsuccessful, attempting to upgrade to {}", this._nextProtocol);
        }
        if (this._nextProtocol != null) {
            ConnectionFactory connectionFactory = connector.getConnectionFactory(this._nextProtocol);
            if (connectionFactory == null) {
                throw new IllegalStateException("Cannot find protocol '" + this._nextProtocol + "' in connector's protocol list " + connector.getProtocols() + " for " + endPoint);
            }
            OptionalSslConnectionFactory.upgradeToConnectionFactory(connectionFactory, connector, endPoint);
        } else {
            this.otherProtocol(buffer, endPoint);
        }
    }

    @Deprecated
    protected void otherProtocol(ByteBuffer buffer, EndPoint endPoint) {
        LOG.warn("Detected non-TLS bytes, but no other protocol to upgrade to for {}", endPoint);
        int byte1 = buffer.get(0) & 0xFF;
        int byte2 = buffer.get(1) & 0xFF;
        if (byte1 == 71 && byte2 == 69) {
            String body = "<!DOCTYPE html>\r\n<html>\r\n<head><title>Bad Request</title></head>\r\n<body><h1>Bad Request</h1><p>HTTP request to HTTPS port</p></body>\r\n</html>";
            String response = "HTTP/1.1 400 Bad Request\r\nContent-Type: text/html\r\nContent-Length: " + body.length() + "\r\nConnection: close\r\n\r\n" + body;
            Callback.Completable completable = new Callback.Completable();
            endPoint.write(completable, ByteBuffer.wrap(response.getBytes(StandardCharsets.US_ASCII)));
            completable.whenComplete((r, x) -> endPoint.close());
        } else {
            endPoint.close();
        }
    }
}

