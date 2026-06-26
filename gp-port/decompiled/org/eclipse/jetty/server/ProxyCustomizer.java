/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.ProxyConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.Attributes;

public class ProxyCustomizer
implements HttpConfiguration.Customizer {
    public static final String REMOTE_ADDRESS_ATTRIBUTE_NAME = "org.eclipse.jetty.proxy.remote.address";
    public static final String REMOTE_PORT_ATTRIBUTE_NAME = "org.eclipse.jetty.proxy.remote.port";
    public static final String LOCAL_ADDRESS_ATTRIBUTE_NAME = "org.eclipse.jetty.proxy.local.address";
    public static final String LOCAL_PORT_ATTRIBUTE_NAME = "org.eclipse.jetty.proxy.local.port";

    @Override
    public void customize(Connector connector, HttpConfiguration channelConfig, Request request) {
        EndPoint endPoint = request.getHttpChannel().getEndPoint();
        if (endPoint instanceof ProxyConnectionFactory.ProxyEndPoint) {
            EndPoint underlyingEndpoint = ((ProxyConnectionFactory.ProxyEndPoint)endPoint).unwrap();
            request.setAttributes(new ProxyAttributes(underlyingEndpoint.getRemoteAddress(), underlyingEndpoint.getLocalAddress(), request.getAttributes()));
        }
    }

    private static class ProxyAttributes
    extends Attributes.Wrapper {
        private final String _remoteAddress;
        private final String _localAddress;
        private final int _remotePort;
        private final int _localPort;

        private ProxyAttributes(InetSocketAddress remoteAddress, InetSocketAddress localAddress, Attributes attributes) {
            super(attributes);
            this._remoteAddress = remoteAddress.getAddress().getHostAddress();
            this._localAddress = localAddress.getAddress().getHostAddress();
            this._remotePort = remoteAddress.getPort();
            this._localPort = localAddress.getPort();
        }

        @Override
        public Object getAttribute(String name) {
            switch (name) {
                case "org.eclipse.jetty.proxy.remote.address": {
                    return this._remoteAddress;
                }
                case "org.eclipse.jetty.proxy.remote.port": {
                    return this._remotePort;
                }
                case "org.eclipse.jetty.proxy.local.address": {
                    return this._localAddress;
                }
                case "org.eclipse.jetty.proxy.local.port": {
                    return this._localPort;
                }
            }
            return super.getAttribute(name);
        }

        @Override
        public Set<String> getAttributeNameSet() {
            HashSet<String> names = new HashSet<String>(this._attributes.getAttributeNameSet());
            names.remove(ProxyCustomizer.REMOTE_ADDRESS_ATTRIBUTE_NAME);
            names.remove(ProxyCustomizer.LOCAL_ADDRESS_ATTRIBUTE_NAME);
            if (this._remoteAddress != null) {
                names.add(ProxyCustomizer.REMOTE_ADDRESS_ATTRIBUTE_NAME);
            }
            if (this._localAddress != null) {
                names.add(ProxyCustomizer.LOCAL_ADDRESS_ATTRIBUTE_NAME);
            }
            names.add(ProxyCustomizer.REMOTE_PORT_ATTRIBUTE_NAME);
            names.add(ProxyCustomizer.LOCAL_PORT_ATTRIBUTE_NAME);
            return names;
        }
    }
}

