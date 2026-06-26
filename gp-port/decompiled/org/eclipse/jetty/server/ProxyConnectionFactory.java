/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ReadPendingException;
import java.nio.channels.WritePendingException;
import java.nio.charset.StandardCharsets;
import org.eclipse.jetty.io.AbstractConnection;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.server.AbstractConnectionFactory;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.DetectorConnectionFactory;
import org.eclipse.jetty.util.AttributesMap;
import org.eclipse.jetty.util.BufferUtil;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.TypeUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class ProxyConnectionFactory
extends DetectorConnectionFactory {
    public static final String TLS_VERSION = "TLS_VERSION";
    private static final Logger LOG = Log.getLogger(ProxyConnectionFactory.class);

    public ProxyConnectionFactory() {
        this((String)null);
    }

    public ProxyConnectionFactory(String nextProtocol) {
        super(new ProxyV1ConnectionFactory(nextProtocol), new ProxyV2ConnectionFactory(nextProtocol));
    }

    private static ConnectionFactory findNextConnectionFactory(String nextProtocol, Connector connector, String currentProtocol, EndPoint endp) {
        currentProtocol = "[" + currentProtocol + "]";
        if (LOG.isDebugEnabled()) {
            LOG.debug("finding connection factory following {} for protocol {}", currentProtocol, nextProtocol);
        }
        String nextProtocolToFind = nextProtocol;
        if (nextProtocol == null) {
            nextProtocolToFind = AbstractConnectionFactory.findNextProtocol(connector, currentProtocol);
        }
        if (nextProtocolToFind == null) {
            throw new IllegalStateException("Cannot find protocol following '" + currentProtocol + "' in connector's protocol list " + connector.getProtocols() + " for " + endp);
        }
        ConnectionFactory connectionFactory = connector.getConnectionFactory(nextProtocolToFind);
        if (connectionFactory == null) {
            throw new IllegalStateException("Cannot find protocol '" + nextProtocol + "' in connector's protocol list " + connector.getProtocols() + " for " + endp);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("found next connection factory {} for protocol {}", connectionFactory, nextProtocol);
        }
        return connectionFactory;
    }

    public int getMaxProxyHeader() {
        ProxyV2ConnectionFactory v2 = this.getBean(ProxyV2ConnectionFactory.class);
        return v2.getMaxProxyHeader();
    }

    public void setMaxProxyHeader(int maxProxyHeader) {
        ProxyV2ConnectionFactory v2 = this.getBean(ProxyV2ConnectionFactory.class);
        v2.setMaxProxyHeader(maxProxyHeader);
    }

    public static class ProxyEndPoint
    extends AttributesMap
    implements EndPoint {
        private final EndPoint _endp;
        private final InetSocketAddress _remote;
        private final InetSocketAddress _local;

        public ProxyEndPoint(EndPoint endp, InetSocketAddress remote, InetSocketAddress local) {
            this._endp = endp;
            this._remote = remote;
            this._local = local;
        }

        public EndPoint unwrap() {
            return this._endp;
        }

        @Override
        public void close() {
            this._endp.close();
        }

        @Override
        public int fill(ByteBuffer buffer) throws IOException {
            return this._endp.fill(buffer);
        }

        @Override
        public void fillInterested(Callback callback) throws ReadPendingException {
            this._endp.fillInterested(callback);
        }

        @Override
        public boolean flush(ByteBuffer ... buffer) throws IOException {
            return this._endp.flush(buffer);
        }

        @Override
        public Connection getConnection() {
            return this._endp.getConnection();
        }

        @Override
        public void setConnection(Connection connection) {
            this._endp.setConnection(connection);
        }

        @Override
        public long getCreatedTimeStamp() {
            return this._endp.getCreatedTimeStamp();
        }

        @Override
        public long getIdleTimeout() {
            return this._endp.getIdleTimeout();
        }

        @Override
        public void setIdleTimeout(long idleTimeout) {
            this._endp.setIdleTimeout(idleTimeout);
        }

        @Override
        public InetSocketAddress getLocalAddress() {
            return this._local;
        }

        @Override
        public InetSocketAddress getRemoteAddress() {
            return this._remote;
        }

        @Override
        public Object getTransport() {
            return this._endp.getTransport();
        }

        @Override
        public boolean isFillInterested() {
            return this._endp.isFillInterested();
        }

        @Override
        public boolean isInputShutdown() {
            return this._endp.isInputShutdown();
        }

        @Override
        public boolean isOpen() {
            return this._endp.isOpen();
        }

        @Override
        public boolean isOptimizedForDirectBuffers() {
            return this._endp.isOptimizedForDirectBuffers();
        }

        @Override
        public boolean isOutputShutdown() {
            return this._endp.isOutputShutdown();
        }

        @Override
        public void onClose() {
            this._endp.onClose();
        }

        @Override
        public void onOpen() {
            this._endp.onOpen();
        }

        @Override
        public void shutdownOutput() {
            this._endp.shutdownOutput();
        }

        @Override
        public String toString() {
            return String.format("%s@%x[remote=%s,local=%s,endpoint=%s]", this.getClass().getSimpleName(), this.hashCode(), this._remote, this._local, this._endp);
        }

        @Override
        public boolean tryFillInterested(Callback callback) {
            return this._endp.tryFillInterested(callback);
        }

        @Override
        public void upgrade(Connection newConnection) {
            this._endp.upgrade(newConnection);
        }

        @Override
        public void write(Callback callback, ByteBuffer ... buffers) throws WritePendingException {
            this._endp.write(callback, buffers);
        }
    }

    private static class ProxyV2ConnectionFactory
    extends AbstractConnectionFactory
    implements ConnectionFactory.Detecting {
        private static final byte[] SIGNATURE = new byte[]{13, 10, 13, 10, 0, 13, 10, 81, 85, 73, 84, 10};
        private final String _nextProtocol;
        private int _maxProxyHeader = 1024;

        private ProxyV2ConnectionFactory(String nextProtocol) {
            super("proxy");
            this._nextProtocol = nextProtocol;
        }

        @Override
        public ConnectionFactory.Detecting.Detection detect(ByteBuffer buffer) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Proxy v2 attempting detection with {} bytes", buffer.remaining());
            }
            if (buffer.remaining() < SIGNATURE.length) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Proxy v2 detection requires more bytes", new Object[0]);
                }
                return ConnectionFactory.Detecting.Detection.NEED_MORE_BYTES;
            }
            for (int i = 0; i < SIGNATURE.length; ++i) {
                byte signatureByte = SIGNATURE[i];
                byte byteInBuffer = buffer.get(i);
                if (byteInBuffer == signatureByte) continue;
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Proxy v2 detection unsuccessful", new Object[0]);
                }
                return ConnectionFactory.Detecting.Detection.NOT_RECOGNIZED;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Proxy v2 detection succeeded", new Object[0]);
            }
            return ConnectionFactory.Detecting.Detection.RECOGNIZED;
        }

        public int getMaxProxyHeader() {
            return this._maxProxyHeader;
        }

        public void setMaxProxyHeader(int maxProxyHeader) {
            this._maxProxyHeader = maxProxyHeader;
        }

        @Override
        public Connection newConnection(Connector connector, EndPoint endp) {
            ConnectionFactory nextConnectionFactory = ProxyConnectionFactory.findNextConnectionFactory(this._nextProtocol, connector, this.getProtocol(), endp);
            return this.configure(new ProxyProtocolV2Connection(endp, connector, nextConnectionFactory), connector, endp);
        }

        private class ProxyProtocolV2Connection
        extends AbstractConnection
        implements Connection.UpgradeFrom,
        Connection.UpgradeTo {
            private static final int HEADER_LENGTH = 16;
            private final Connector _connector;
            private final ConnectionFactory _next;
            private final ByteBuffer _buffer;
            private boolean _local;
            private Family _family;
            private int _length;
            private boolean _headerParsed;

            protected ProxyProtocolV2Connection(EndPoint endp, Connector connector, ConnectionFactory next) {
                super(endp, connector.getExecutor());
                this._connector = connector;
                this._next = next;
                this._buffer = this._connector.getByteBufferPool().acquire(this.getInputBufferSize(), true);
            }

            @Override
            public void onUpgradeTo(ByteBuffer buffer) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Proxy v2 copying unconsumed buffer {}", BufferUtil.toDetailString(buffer));
                }
                BufferUtil.append(this._buffer, buffer);
            }

            @Override
            public void onOpen() {
                super.onOpen();
                try {
                    this.parseHeader();
                    if (this._headerParsed && this._buffer.remaining() >= this._length) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Proxy v2 onOpen parsing fixed length packet part done, now upgrading", new Object[0]);
                        }
                        this.parseBodyAndUpgrade();
                    } else {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Proxy v2 onOpen parsing fixed length packet ran out of bytes, marking as fillInterested", new Object[0]);
                        }
                        this.fillInterested();
                    }
                }
                catch (Exception x) {
                    LOG.warn("Proxy v2 error for {}", this.getEndPoint(), x);
                    this.releaseAndClose();
                }
            }

            @Override
            public void onFillable() {
                try {
                    int fill;
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Proxy v2 onFillable header parsed? ", this._headerParsed);
                    }
                    while (!this._headerParsed) {
                        fill = this.getEndPoint().fill(this._buffer);
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Proxy v2 filled buffer with {} bytes", fill);
                        }
                        if (fill < 0) {
                            this._connector.getByteBufferPool().release(this._buffer);
                            this.getEndPoint().shutdownOutput();
                            return;
                        }
                        if (fill == 0) {
                            this.fillInterested();
                            return;
                        }
                        this.parseHeader();
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Proxy v2 onFillable header parsed, length = {}, buffer = {}", this._length, BufferUtil.toDetailString(this._buffer));
                    }
                    while (this._buffer.remaining() < this._length) {
                        fill = this.getEndPoint().fill(this._buffer);
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Proxy v2 filled buffer with {} bytes", fill);
                        }
                        if (fill < 0) {
                            this._connector.getByteBufferPool().release(this._buffer);
                            this.getEndPoint().shutdownOutput();
                            return;
                        }
                        if (fill != 0) continue;
                        this.fillInterested();
                        return;
                    }
                    this.parseBodyAndUpgrade();
                }
                catch (Throwable x) {
                    LOG.warn("Proxy v2 error for " + this.getEndPoint(), x);
                    this.releaseAndClose();
                }
            }

            @Override
            public ByteBuffer onUpgradeFrom() {
                if (this._buffer.hasRemaining()) {
                    ByteBuffer unconsumed = ByteBuffer.allocateDirect(this._buffer.remaining());
                    unconsumed.put(this._buffer);
                    unconsumed.flip();
                    this._connector.getByteBufferPool().release(this._buffer);
                    return unconsumed;
                }
                return null;
            }

            private void parseBodyAndUpgrade() throws IOException {
                int nonProxyRemaining = this._buffer.remaining() - this._length;
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Proxy v2 parsing body, length = {}, buffer = {}", this._length, BufferUtil.toHexSummary(this._buffer));
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Proxy v2 body {} from {} for {}", this._next, BufferUtil.toHexSummary(this._buffer), this);
                }
                EndPoint endPoint = this.getEndPoint();
                if (!this._local) {
                    char dp;
                    char sp2;
                    InetAddress dst;
                    InetAddress src;
                    switch (this._family) {
                        case INET: {
                            byte[] addr = new byte[4];
                            this._buffer.get(addr);
                            src = Inet4Address.getByAddress(addr);
                            this._buffer.get(addr);
                            dst = Inet4Address.getByAddress(addr);
                            sp2 = this._buffer.getChar();
                            dp = this._buffer.getChar();
                            break;
                        }
                        case INET6: {
                            byte[] addr = new byte[16];
                            this._buffer.get(addr);
                            src = Inet6Address.getByAddress(addr);
                            this._buffer.get(addr);
                            dst = Inet6Address.getByAddress(addr);
                            sp2 = this._buffer.getChar();
                            dp = this._buffer.getChar();
                            break;
                        }
                        default: {
                            throw new IllegalStateException();
                        }
                    }
                    InetSocketAddress remote = new InetSocketAddress(src, (int)sp2);
                    InetSocketAddress local = new InetSocketAddress(dst, (int)dp);
                    ProxyEndPoint proxyEndPoint = new ProxyEndPoint(endPoint, remote, local);
                    endPoint = proxyEndPoint;
                    while (this._buffer.remaining() > nonProxyRemaining) {
                        int type = 0xFF & this._buffer.get();
                        int length = this._buffer.getChar();
                        byte[] value = new byte[length];
                        this._buffer.get(value);
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(String.format("Proxy v2 T=%x L=%d V=%s for %s", type, length, TypeUtil.toHexString(value), this), new Object[0]);
                        }
                        block4 : switch (type) {
                            case 32: {
                                int client = value[0] & 0xFF;
                                switch (client) {
                                    case 1: {
                                        int i = 5;
                                        while (i < length) {
                                            int subType = value[i++] & 0xFF;
                                            int subLength = (value[i++] & 0xFF) * 256 + (value[i++] & 0xFF);
                                            byte[] subValue = new byte[subLength];
                                            System.arraycopy(value, i, subValue, 0, subLength);
                                            i += subLength;
                                            switch (subType) {
                                                case 33: {
                                                    String tlsVersion = new String(subValue, StandardCharsets.US_ASCII);
                                                    proxyEndPoint.setAttribute(ProxyConnectionFactory.TLS_VERSION, tlsVersion);
                                                    break;
                                                }
                                            }
                                        }
                                        break block4;
                                    }
                                }
                                break;
                            }
                        }
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Proxy v2 {} {}", this.getEndPoint(), proxyEndPoint.toString());
                    }
                } else {
                    this._buffer.position(this._buffer.position() + this._length);
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Proxy v2 parsing dynamic packet part is now done, upgrading to {}", ProxyV2ConnectionFactory.this._nextProtocol);
                }
                DetectorConnectionFactory.upgradeToConnectionFactory(this._next, this._connector, endPoint);
            }

            private void parseHeader() throws IOException {
                Transport transport;
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Proxy v2 parsing fixed length packet part, buffer = {}", BufferUtil.toDetailString(this._buffer));
                }
                if (this._buffer.remaining() < 16) {
                    return;
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Proxy v2 header {} for {}", BufferUtil.toHexSummary(this._buffer), this);
                }
                for (byte signatureByte : SIGNATURE) {
                    if (this._buffer.get() == signatureByte) continue;
                    throw new IOException("Proxy v2 bad PROXY signature");
                }
                int versionAndCommand = 0xFF & this._buffer.get();
                if ((versionAndCommand & 0xF0) != 32) {
                    throw new IOException("Proxy v2 bad PROXY version");
                }
                this._local = (versionAndCommand & 0xF) == 0;
                int transportAndFamily = 0xFF & this._buffer.get();
                switch (transportAndFamily >> 4) {
                    case 0: {
                        this._family = Family.UNSPEC;
                        break;
                    }
                    case 1: {
                        this._family = Family.INET;
                        break;
                    }
                    case 2: {
                        this._family = Family.INET6;
                        break;
                    }
                    case 3: {
                        this._family = Family.UNIX;
                        break;
                    }
                    default: {
                        throw new IOException("Proxy v2 bad PROXY family");
                    }
                }
                switch (0xF & transportAndFamily) {
                    case 0: {
                        transport = Transport.UNSPEC;
                        break;
                    }
                    case 1: {
                        transport = Transport.STREAM;
                        break;
                    }
                    case 2: {
                        transport = Transport.DGRAM;
                        break;
                    }
                    default: {
                        throw new IOException("Proxy v2 bad PROXY family");
                    }
                }
                this._length = this._buffer.getChar();
                if (!(this._local || this._family != Family.UNSPEC && this._family != Family.UNIX && transport == Transport.STREAM)) {
                    throw new IOException(String.format("Proxy v2 unsupported PROXY mode 0x%x,0x%x", versionAndCommand, transportAndFamily));
                }
                if (this._length > ProxyV2ConnectionFactory.this.getMaxProxyHeader()) {
                    throw new IOException(String.format("Proxy v2 Unsupported PROXY mode 0x%x,0x%x,0x%x", versionAndCommand, transportAndFamily, this._length));
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Proxy v2 fixed length packet part is now parsed", new Object[0]);
                }
                this._headerParsed = true;
            }

            private void releaseAndClose() {
                this._connector.getByteBufferPool().release(this._buffer);
                this.close();
            }
        }

        private static enum Transport {
            UNSPEC,
            STREAM,
            DGRAM;

        }

        private static enum Family {
            UNSPEC,
            INET,
            INET6,
            UNIX;

        }
    }

    private static class ProxyV1ConnectionFactory
    extends AbstractConnectionFactory
    implements ConnectionFactory.Detecting {
        private static final byte[] SIGNATURE = "PROXY".getBytes(StandardCharsets.US_ASCII);
        private final String _nextProtocol;

        private ProxyV1ConnectionFactory(String nextProtocol) {
            super("proxy");
            this._nextProtocol = nextProtocol;
        }

        @Override
        public ConnectionFactory.Detecting.Detection detect(ByteBuffer buffer) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Proxy v1 attempting detection with {} bytes", buffer.remaining());
            }
            if (buffer.remaining() < SIGNATURE.length) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Proxy v1 detection requires more bytes", new Object[0]);
                }
                return ConnectionFactory.Detecting.Detection.NEED_MORE_BYTES;
            }
            for (int i = 0; i < SIGNATURE.length; ++i) {
                byte signatureByte = SIGNATURE[i];
                byte byteInBuffer = buffer.get(i);
                if (byteInBuffer == signatureByte) continue;
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Proxy v1 detection unsuccessful", new Object[0]);
                }
                return ConnectionFactory.Detecting.Detection.NOT_RECOGNIZED;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Proxy v1 detection succeeded", new Object[0]);
            }
            return ConnectionFactory.Detecting.Detection.RECOGNIZED;
        }

        @Override
        public Connection newConnection(Connector connector, EndPoint endp) {
            ConnectionFactory nextConnectionFactory = ProxyConnectionFactory.findNextConnectionFactory(this._nextProtocol, connector, this.getProtocol(), endp);
            return this.configure(new ProxyProtocolV1Connection(endp, connector, nextConnectionFactory), connector, endp);
        }

        private static class ProxyProtocolV1Connection
        extends AbstractConnection
        implements Connection.UpgradeFrom,
        Connection.UpgradeTo {
            private static final int CR_INDEX = 6;
            private static final int LF_INDEX = 7;
            private final Connector _connector;
            private final ConnectionFactory _next;
            private final ByteBuffer _buffer;
            private final StringBuilder _builder = new StringBuilder();
            private final String[] _fields = new String[6];
            private int _index;
            private int _length;

            private ProxyProtocolV1Connection(EndPoint endp, Connector connector, ConnectionFactory next) {
                super(endp, connector.getExecutor());
                this._connector = connector;
                this._next = next;
                this._buffer = this._connector.getByteBufferPool().acquire(this.getInputBufferSize(), true);
            }

            @Override
            public void onFillable() {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Proxy v1 onFillable current index = ", this._index);
                }
                try {
                    while (this._index < 7) {
                        int fill = this.getEndPoint().fill(this._buffer);
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Proxy v1 filled buffer with {} bytes", fill);
                        }
                        if (fill < 0) {
                            this._connector.getByteBufferPool().release(this._buffer);
                            this.getEndPoint().shutdownOutput();
                            return;
                        }
                        if (fill == 0) {
                            this.fillInterested();
                            return;
                        }
                        if (!this.parse()) continue;
                        break;
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Proxy v1 onFillable parsing done, now upgrading", new Object[0]);
                    }
                    this.upgrade();
                }
                catch (Throwable x) {
                    LOG.warn("Proxy v1 error for {}", this.getEndPoint(), x);
                    this.releaseAndClose();
                }
            }

            @Override
            public void onOpen() {
                super.onOpen();
                try {
                    while (this._index < 7) {
                        if (this.parse()) continue;
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Proxy v1 onOpen parsing ran out of bytes, marking as fillInterested", new Object[0]);
                        }
                        this.fillInterested();
                        return;
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Proxy v1 onOpen parsing done, now upgrading", new Object[0]);
                    }
                    this.upgrade();
                }
                catch (Throwable x) {
                    LOG.warn("Proxy v1 error for {}", this.getEndPoint(), x);
                    this.releaseAndClose();
                }
            }

            @Override
            public ByteBuffer onUpgradeFrom() {
                if (this._buffer.hasRemaining()) {
                    ByteBuffer unconsumed = ByteBuffer.allocateDirect(this._buffer.remaining());
                    unconsumed.put(this._buffer);
                    unconsumed.flip();
                    this._connector.getByteBufferPool().release(this._buffer);
                    return unconsumed;
                }
                return null;
            }

            @Override
            public void onUpgradeTo(ByteBuffer buffer) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Proxy v1 copying unconsumed buffer {}", BufferUtil.toDetailString(buffer));
                }
                BufferUtil.append(this._buffer, buffer);
            }

            private boolean parse() throws IOException {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Proxy v1 parsing {}", BufferUtil.toDetailString(this._buffer));
                }
                this._length += this._buffer.remaining();
                while (this._buffer.hasRemaining()) {
                    byte b = this._buffer.get();
                    if (this._index < 6) {
                        if (b == 32 || b == 13) {
                            this._fields[this._index++] = this._builder.toString();
                            this._builder.setLength(0);
                            if (b != 13) continue;
                            this._index = 6;
                            continue;
                        }
                        if (b < 32) {
                            throw new IOException("Proxy v1 bad character " + (b & 0xFF));
                        }
                        this._builder.append((char)b);
                        continue;
                    }
                    if (b == 10) {
                        this._index = 7;
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Proxy v1 parsing is done", new Object[0]);
                        }
                        return true;
                    }
                    throw new IOException("Proxy v1 bad CRLF " + (b & 0xFF));
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Proxy v1 parsing requires more bytes", new Object[0]);
                }
                return false;
            }

            private void releaseAndClose() {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Proxy v1 releasing buffer and closing", new Object[0]);
                }
                this._connector.getByteBufferPool().release(this._buffer);
                this.close();
            }

            private void upgrade() {
                int proxyLineLength = this._length - this._buffer.remaining();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Proxy v1 pre-upgrade packet length (including CRLF) is {}", proxyLineLength);
                }
                if (proxyLineLength >= 110) {
                    LOG.warn("Proxy v1 PROXY line too long {} for {}", proxyLineLength, this.getEndPoint());
                    this.releaseAndClose();
                    return;
                }
                if (!"PROXY".equals(this._fields[0])) {
                    LOG.warn("Proxy v1 not PROXY protocol for {}", this.getEndPoint());
                    this.releaseAndClose();
                    return;
                }
                String srcIP = this._fields[2];
                String srcPort = this._fields[4];
                String dstIP = this._fields[3];
                String dstPort = this._fields[5];
                boolean unknown = "UNKNOWN".equalsIgnoreCase(this._fields[1]);
                if (unknown) {
                    srcIP = this.getEndPoint().getRemoteAddress().getAddress().getHostAddress();
                    srcPort = String.valueOf(this.getEndPoint().getRemoteAddress().getPort());
                    dstIP = this.getEndPoint().getLocalAddress().getAddress().getHostAddress();
                    dstPort = String.valueOf(this.getEndPoint().getLocalAddress().getPort());
                }
                InetSocketAddress remote = new InetSocketAddress(srcIP, Integer.parseInt(srcPort));
                InetSocketAddress local = new InetSocketAddress(dstIP, Integer.parseInt(dstPort));
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Proxy v1 next protocol '{}' for {} r={} l={}", this._next, this.getEndPoint(), remote, local);
                }
                ProxyEndPoint endPoint = new ProxyEndPoint(this.getEndPoint(), remote, local);
                DetectorConnectionFactory.upgradeToConnectionFactory(this._next, this._connector, endPoint);
            }
        }
    }
}

