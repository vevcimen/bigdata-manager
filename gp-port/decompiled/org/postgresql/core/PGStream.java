/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.FilterOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import javax.net.SocketFactory;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.MessageProp;
import org.postgresql.core.Encoding;
import org.postgresql.core.EncodingPredictor;
import org.postgresql.core.FixedLengthOutputStream;
import org.postgresql.core.PGBindException;
import org.postgresql.core.Tuple;
import org.postgresql.core.VisibleBufferedInputStream;
import org.postgresql.gss.GSSInputStream;
import org.postgresql.gss.GSSOutputStream;
import org.postgresql.util.ByteStreamWriter;
import org.postgresql.util.GT;
import org.postgresql.util.HostSpec;
import org.postgresql.util.PGPropertyMaxResultBufferParser;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

public class PGStream
implements Closeable,
Flushable {
    private final SocketFactory socketFactory;
    private final HostSpec hostSpec;
    private final byte[] int4Buf;
    private final byte[] int2Buf;
    private Socket connection;
    private VisibleBufferedInputStream pgInput;
    private OutputStream pgOutput;
    private byte @Nullable [] streamBuffer;
    boolean gssEncrypted = false;
    private long nextStreamAvailableCheckTime;
    private int minStreamAvailableCheckDelay = 1000;
    private Encoding encoding;
    private Writer encodingWriter;
    private long maxResultBuffer = -1L;
    private long resultBufferByteCount = 0L;
    private int maxRowSizeBytes = -1;

    public boolean isGssEncrypted() {
        return this.gssEncrypted;
    }

    public void setSecContext(GSSContext secContext) {
        MessageProp messageProp = new MessageProp(0, true);
        this.pgInput = new VisibleBufferedInputStream(new GSSInputStream(this.pgInput.getWrapped(), secContext, messageProp), 8192);
        this.pgOutput = new GSSOutputStream(this.pgOutput, secContext, messageProp, 16384);
        this.gssEncrypted = true;
    }

    public PGStream(SocketFactory socketFactory, HostSpec hostSpec, int timeout) throws IOException {
        this.socketFactory = socketFactory;
        this.hostSpec = hostSpec;
        Socket socket = this.createSocket(timeout);
        this.changeSocket(socket);
        this.setEncoding(Encoding.getJVMEncoding("UTF-8"));
        this.int2Buf = new byte[2];
        this.int4Buf = new byte[4];
    }

    public PGStream(PGStream pgStream, int timeout) throws IOException {
        int sendBufferSize = 1024;
        int receiveBufferSize = 1024;
        int soTimeout = 0;
        boolean keepAlive = false;
        boolean tcpNoDelay = true;
        try {
            sendBufferSize = pgStream.getSocket().getSendBufferSize();
            receiveBufferSize = pgStream.getSocket().getReceiveBufferSize();
            soTimeout = pgStream.getSocket().getSoTimeout();
            keepAlive = pgStream.getSocket().getKeepAlive();
            tcpNoDelay = pgStream.getSocket().getTcpNoDelay();
        }
        catch (SocketException socketException) {
            // empty catch block
        }
        pgStream.close();
        this.socketFactory = pgStream.socketFactory;
        this.hostSpec = pgStream.hostSpec;
        Socket socket = this.createSocket(timeout);
        this.changeSocket(socket);
        this.setEncoding(Encoding.getJVMEncoding("UTF-8"));
        socket.setReceiveBufferSize(receiveBufferSize);
        socket.setSendBufferSize(sendBufferSize);
        this.setNetworkTimeout(soTimeout);
        socket.setKeepAlive(keepAlive);
        socket.setTcpNoDelay(tcpNoDelay);
        this.int2Buf = new byte[2];
        this.int4Buf = new byte[4];
    }

    @Deprecated
    public PGStream(SocketFactory socketFactory, HostSpec hostSpec) throws IOException {
        this(socketFactory, hostSpec, 0);
    }

    public HostSpec getHostSpec() {
        return this.hostSpec;
    }

    public Socket getSocket() {
        return this.connection;
    }

    public SocketFactory getSocketFactory() {
        return this.socketFactory;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean hasMessagePending() throws IOException {
        boolean available = false;
        if (this.pgInput.available() > 0) {
            return true;
        }
        long now = System.nanoTime() / 1000000L;
        if (now < this.nextStreamAvailableCheckTime && this.minStreamAvailableCheckDelay != 0) {
            return false;
        }
        int soTimeout = this.getNetworkTimeout();
        this.connection.setSoTimeout(1);
        try {
            if (!this.pgInput.ensureBytes(1, false)) {
                boolean bl = false;
                return bl;
            }
            available = this.pgInput.peek() != -1;
        }
        catch (SocketTimeoutException e) {
            boolean bl = false;
            return bl;
        }
        finally {
            this.connection.setSoTimeout(soTimeout);
        }
        if (!available) {
            this.nextStreamAvailableCheckTime = now + (long)this.minStreamAvailableCheckDelay;
        }
        return available;
    }

    public void setMinStreamAvailableCheckDelay(int delay) {
        this.minStreamAvailableCheckDelay = delay;
    }

    private Socket createSocket(int timeout) throws IOException {
        Socket socket = this.socketFactory.createSocket();
        String localSocketAddress = this.hostSpec.getLocalSocketAddress();
        if (localSocketAddress != null) {
            socket.bind(new InetSocketAddress(InetAddress.getByName(localSocketAddress), 0));
        }
        if (!socket.isConnected()) {
            InetSocketAddress address = this.hostSpec.shouldResolve() != false ? new InetSocketAddress(this.hostSpec.getHost(), this.hostSpec.getPort()) : InetSocketAddress.createUnresolved(this.hostSpec.getHost(), this.hostSpec.getPort());
            socket.connect(address, timeout);
        }
        return socket;
    }

    public void changeSocket(Socket socket) throws IOException {
        assert (this.connection != socket) : "changeSocket is called with the current socket as argument. This is a no-op, however, it re-allocates buffered streams, so refrain from excessive changeSocket calls";
        this.connection = socket;
        this.connection.setTcpNoDelay(true);
        this.pgInput = new VisibleBufferedInputStream(this.connection.getInputStream(), 8192);
        this.pgOutput = new BufferedOutputStream(this.connection.getOutputStream(), 8192);
        if (this.encoding != null) {
            this.setEncoding(this.encoding);
        }
    }

    public Encoding getEncoding() {
        return this.encoding;
    }

    public void setEncoding(Encoding encoding) throws IOException {
        if (this.encoding != null && this.encoding.name().equals(encoding.name())) {
            return;
        }
        if (this.encodingWriter != null) {
            this.encodingWriter.close();
        }
        this.encoding = encoding;
        FilterOutputStream interceptor = new FilterOutputStream(this.pgOutput){

            @Override
            public void flush() throws IOException {
            }

            @Override
            public void close() throws IOException {
                super.flush();
            }
        };
        this.encodingWriter = encoding.getEncodingWriter(interceptor);
    }

    public Writer getEncodingWriter() throws IOException {
        if (this.encodingWriter == null) {
            throw new IOException("No encoding has been set on this connection");
        }
        return this.encodingWriter;
    }

    public void sendChar(int val) throws IOException {
        this.pgOutput.write(val);
    }

    public void sendInteger4(int val) throws IOException {
        this.int4Buf[0] = (byte)(val >>> 24);
        this.int4Buf[1] = (byte)(val >>> 16);
        this.int4Buf[2] = (byte)(val >>> 8);
        this.int4Buf[3] = (byte)val;
        this.pgOutput.write(this.int4Buf);
    }

    public void sendInteger2(int val) throws IOException {
        if (val < 0 || val > 65535) {
            throw new IllegalArgumentException("Tried to send an out-of-range integer as a 2-byte unsigned int value: " + val);
        }
        this.int2Buf[0] = (byte)(val >>> 8);
        this.int2Buf[1] = (byte)val;
        this.pgOutput.write(this.int2Buf);
    }

    public void send(byte[] buf) throws IOException {
        this.pgOutput.write(buf);
    }

    public void send(byte[] buf, int siz) throws IOException {
        this.send(buf, 0, siz);
    }

    public void send(byte[] buf, int off, int siz) throws IOException {
        int bufamt = buf.length - off;
        this.pgOutput.write(buf, off, bufamt < siz ? bufamt : siz);
        for (int i = bufamt; i < siz; ++i) {
            this.pgOutput.write(0);
        }
    }

    public void send(ByteStreamWriter writer) throws IOException {
        final FixedLengthOutputStream fixedLengthStream = new FixedLengthOutputStream(writer.getLength(), this.pgOutput);
        try {
            writer.writeTo(new ByteStreamWriter.ByteStreamTarget(){

                @Override
                public OutputStream getOutputStream() {
                    return fixedLengthStream;
                }
            });
        }
        catch (IOException ioe) {
            throw ioe;
        }
        catch (Exception re) {
            throw new IOException("Error writing bytes to stream", re);
        }
        for (int i = fixedLengthStream.remaining(); i > 0; --i) {
            this.pgOutput.write(0);
        }
    }

    public int peekChar() throws IOException {
        int c = this.pgInput.peek();
        if (c < 0) {
            throw new EOFException();
        }
        return c;
    }

    public int receiveChar() throws IOException {
        int c = this.pgInput.read();
        if (c < 0) {
            throw new EOFException();
        }
        return c;
    }

    public int receiveInteger4() throws IOException {
        if (this.pgInput.read(this.int4Buf) != 4) {
            throw new EOFException();
        }
        return (this.int4Buf[0] & 0xFF) << 24 | (this.int4Buf[1] & 0xFF) << 16 | (this.int4Buf[2] & 0xFF) << 8 | this.int4Buf[3] & 0xFF;
    }

    public int receiveInteger2() throws IOException {
        if (this.pgInput.read(this.int2Buf) != 2) {
            throw new EOFException();
        }
        return (this.int2Buf[0] & 0xFF) << 8 | this.int2Buf[1] & 0xFF;
    }

    public String receiveString(int len) throws IOException {
        if (!this.pgInput.ensureBytes(len)) {
            throw new EOFException();
        }
        String res = this.encoding.decode(this.pgInput.getBuffer(), this.pgInput.getIndex(), len);
        this.pgInput.skip(len);
        return res;
    }

    public EncodingPredictor.DecodeResult receiveErrorString(int len) throws IOException {
        EncodingPredictor.DecodeResult res;
        block3: {
            if (!this.pgInput.ensureBytes(len)) {
                throw new EOFException();
            }
            try {
                String value = this.encoding.decode(this.pgInput.getBuffer(), this.pgInput.getIndex(), len);
                res = new EncodingPredictor.DecodeResult(value, null);
            }
            catch (IOException e) {
                res = EncodingPredictor.decode(this.pgInput.getBuffer(), this.pgInput.getIndex(), len);
                if (res != null) break block3;
                Encoding enc = Encoding.defaultEncoding();
                String value = enc.decode(this.pgInput.getBuffer(), this.pgInput.getIndex(), len);
                res = new EncodingPredictor.DecodeResult(value, enc.name());
            }
        }
        this.pgInput.skip(len);
        return res;
    }

    public String receiveString() throws IOException {
        int len = this.pgInput.scanCStringLength();
        String res = this.encoding.decode(this.pgInput.getBuffer(), this.pgInput.getIndex(), len - 1);
        this.pgInput.skip(len);
        return res;
    }

    public String receiveCanonicalString() throws IOException {
        int len = this.pgInput.scanCStringLength();
        String res = this.encoding.decodeCanonicalized(this.pgInput.getBuffer(), this.pgInput.getIndex(), len - 1);
        this.pgInput.skip(len);
        return res;
    }

    public String receiveCanonicalStringIfPresent() throws IOException {
        int len = this.pgInput.scanCStringLength();
        String res = this.encoding.decodeCanonicalizedIfPresent(this.pgInput.getBuffer(), this.pgInput.getIndex(), len - 1);
        this.pgInput.skip(len);
        return res;
    }

    public Tuple receiveTupleV3() throws IOException, OutOfMemoryError, SQLException {
        int messageSize = this.receiveInteger4();
        int nf = this.receiveInteger2();
        int dataToReadSize = messageSize - 4 - 2 - 4 * nf;
        this.setMaxRowSizeBytes(dataToReadSize);
        byte[][] answer = new byte[nf][];
        this.increaseByteCounter(dataToReadSize);
        OutOfMemoryError oom = null;
        for (int i = 0; i < nf; ++i) {
            int size = this.receiveInteger4();
            if (size == -1) continue;
            try {
                answer[i] = new byte[size];
                this.receive(answer[i], 0, size);
                continue;
            }
            catch (OutOfMemoryError oome) {
                oom = oome;
                this.skip(size);
            }
        }
        if (oom != null) {
            throw oom;
        }
        return new Tuple(answer);
    }

    public byte[] receive(int siz) throws IOException {
        byte[] answer = new byte[siz];
        this.receive(answer, 0, siz);
        return answer;
    }

    public void receive(byte[] buf, int off, int siz) throws IOException {
        int w;
        for (int s2 = 0; s2 < siz; s2 += w) {
            w = this.pgInput.read(buf, off + s2, siz - s2);
            if (w >= 0) continue;
            throw new EOFException();
        }
    }

    public void skip(int size) throws IOException {
        for (long s2 = 0L; s2 < (long)size; s2 += this.pgInput.skip((long)size - s2)) {
        }
    }

    public void sendStream(InputStream inStream, int remaining) throws IOException {
        int expectedLength = remaining;
        if (this.streamBuffer == null) {
            this.streamBuffer = new byte[8192];
        }
        while (remaining > 0) {
            int readCount;
            int count = remaining > this.streamBuffer.length ? this.streamBuffer.length : remaining;
            try {
                readCount = inStream.read(this.streamBuffer, 0, count);
                if (readCount < 0) {
                    throw new EOFException(GT.tr("Premature end of input stream, expected {0} bytes, but only read {1}.", expectedLength, expectedLength - remaining));
                }
            }
            catch (IOException ioe) {
                while (remaining > 0) {
                    this.send(this.streamBuffer, count);
                    count = (remaining -= count) > this.streamBuffer.length ? this.streamBuffer.length : remaining;
                }
                throw new PGBindException(ioe);
            }
            this.send(this.streamBuffer, readCount);
            remaining -= readCount;
        }
    }

    @Override
    public void flush() throws IOException {
        if (this.encodingWriter != null) {
            this.encodingWriter.flush();
        }
        this.pgOutput.flush();
    }

    public void receiveEOF() throws SQLException, IOException {
        int c = this.pgInput.read();
        if (c < 0) {
            return;
        }
        throw new PSQLException(GT.tr("Expected an EOF from server, got: {0}", c), PSQLState.COMMUNICATION_ERROR);
    }

    @Override
    public void close() throws IOException {
        if (this.encodingWriter != null) {
            this.encodingWriter.close();
        }
        this.pgOutput.close();
        this.pgInput.close();
        this.connection.close();
    }

    public void setNetworkTimeout(int milliseconds) throws IOException {
        this.connection.setSoTimeout(milliseconds);
        this.pgInput.setTimeoutRequested(milliseconds != 0);
    }

    public int getNetworkTimeout() throws IOException {
        return this.connection.getSoTimeout();
    }

    public void setMaxResultBuffer(@Nullable String value) throws PSQLException {
        this.maxResultBuffer = PGPropertyMaxResultBufferParser.parseProperty(value);
    }

    public long getMaxResultBuffer() {
        return this.maxResultBuffer;
    }

    public void setMaxRowSizeBytes(int rowSizeBytes) {
        if (rowSizeBytes > this.maxRowSizeBytes) {
            this.maxRowSizeBytes = rowSizeBytes;
        }
    }

    public int getMaxRowSizeBytes() {
        return this.maxRowSizeBytes;
    }

    public void clearMaxRowSizeBytes() {
        this.maxRowSizeBytes = -1;
    }

    public void clearResultBufferCount() {
        this.resultBufferByteCount = 0L;
    }

    private void increaseByteCounter(long value) throws SQLException {
        if (this.maxResultBuffer != -1L) {
            this.resultBufferByteCount += value;
            if (this.resultBufferByteCount > this.maxResultBuffer) {
                throw new PSQLException(GT.tr("Result set exceeded maxResultBuffer limit. Received:  {0}; Current limit: {1}", String.valueOf(this.resultBufferByteCount), String.valueOf(this.maxResultBuffer)), PSQLState.COMMUNICATION_ERROR);
            }
        }
    }

    public boolean isClosed() {
        return this.connection.isClosed();
    }
}

