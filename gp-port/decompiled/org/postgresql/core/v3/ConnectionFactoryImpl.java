/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core.v3;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.net.SocketFactory;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.PGProperty;
import org.postgresql.core.ConnectionFactory;
import org.postgresql.core.PGStream;
import org.postgresql.core.QueryExecutor;
import org.postgresql.core.ServerVersion;
import org.postgresql.core.SetupQueryRunner;
import org.postgresql.core.SocketFactoryFactory;
import org.postgresql.core.Tuple;
import org.postgresql.core.Utils;
import org.postgresql.core.Version;
import org.postgresql.core.v3.AuthenticationPluginManager;
import org.postgresql.core.v3.QueryExecutorImpl;
import org.postgresql.gss.MakeGSS;
import org.postgresql.hostchooser.CandidateHost;
import org.postgresql.hostchooser.GlobalHostStatusTracker;
import org.postgresql.hostchooser.HostChooser;
import org.postgresql.hostchooser.HostChooserFactory;
import org.postgresql.hostchooser.HostRequirement;
import org.postgresql.hostchooser.HostStatus;
import org.postgresql.jdbc.GSSEncMode;
import org.postgresql.jdbc.SslMode;
import org.postgresql.jre7.sasl.ScramAuthenticator;
import org.postgresql.plugin.AuthenticationRequestType;
import org.postgresql.ssl.MakeSSL;
import org.postgresql.sspi.ISSPIClient;
import org.postgresql.util.GT;
import org.postgresql.util.HostSpec;
import org.postgresql.util.MD5Digest;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.ServerErrorMessage;
import org.postgresql.util.internal.Nullness;

public class ConnectionFactoryImpl
extends ConnectionFactory {
    private static final Logger LOGGER = Logger.getLogger(ConnectionFactoryImpl.class.getName());
    private static final int AUTH_REQ_OK = 0;
    private static final int AUTH_REQ_KRB4 = 1;
    private static final int AUTH_REQ_KRB5 = 2;
    private static final int AUTH_REQ_PASSWORD = 3;
    private static final int AUTH_REQ_CRYPT = 4;
    private static final int AUTH_REQ_MD5 = 5;
    private static final int AUTH_REQ_SCM = 6;
    private static final int AUTH_REQ_GSS = 7;
    private static final int AUTH_REQ_GSS_CONTINUE = 8;
    private static final int AUTH_REQ_SSPI = 9;
    private static final int AUTH_REQ_SASL = 10;
    private static final int AUTH_REQ_SASL_CONTINUE = 11;
    private static final int AUTH_REQ_SASL_FINAL = 12;
    private static final String IN_HOT_STANDBY = "in_hot_standby";

    private ISSPIClient createSSPI(PGStream pgStream, @Nullable String spnServiceClass, boolean enableNegotiate) {
        try {
            Class<?> c = Class.forName("org.postgresql.sspi.SSPIClient");
            return (ISSPIClient)c.getDeclaredConstructor(PGStream.class, String.class, Boolean.TYPE).newInstance(pgStream, spnServiceClass, enableNegotiate);
        }
        catch (Exception e) {
            throw new IllegalStateException("Unable to load org.postgresql.sspi.SSPIClient. Please check that SSPIClient is included in your pgjdbc distribution.", e);
        }
    }

    private PGStream tryConnect(Properties info, SocketFactory socketFactory, HostSpec hostSpec, SslMode sslMode, GSSEncMode gssEncMode) throws SQLException, IOException {
        int connectTimeout = PGProperty.CONNECT_TIMEOUT.getInt(info) * 1000;
        String user = PGProperty.USER.get(info);
        String database = PGProperty.PG_DBNAME.get(info);
        if (user == null) {
            throw new PSQLException(GT.tr("User cannot be null", new Object[0]), PSQLState.INVALID_NAME);
        }
        if (database == null) {
            throw new PSQLException(GT.tr("Database cannot be null", new Object[0]), PSQLState.INVALID_NAME);
        }
        PGStream newStream = new PGStream(socketFactory, hostSpec, connectTimeout);
        try {
            int sendBufferSize;
            int socketTimeout = PGProperty.SOCKET_TIMEOUT.getInt(info);
            if (socketTimeout > 0) {
                newStream.setNetworkTimeout(socketTimeout * 1000);
            }
            String maxResultBuffer = PGProperty.MAX_RESULT_BUFFER.get(info);
            newStream.setMaxResultBuffer(maxResultBuffer);
            boolean requireTCPKeepAlive = PGProperty.TCP_KEEP_ALIVE.getBoolean(info);
            newStream.getSocket().setKeepAlive(requireTCPKeepAlive);
            boolean requireTCPNoDelay = PGProperty.TCP_NO_DELAY.getBoolean(info);
            newStream.getSocket().setTcpNoDelay(requireTCPNoDelay);
            int receiveBufferSize = PGProperty.RECEIVE_BUFFER_SIZE.getInt(info);
            if (receiveBufferSize > -1) {
                if (receiveBufferSize > 0) {
                    newStream.getSocket().setReceiveBufferSize(receiveBufferSize);
                } else {
                    LOGGER.log(Level.WARNING, "Ignore invalid value for receiveBufferSize: {0}", receiveBufferSize);
                }
            }
            if ((sendBufferSize = PGProperty.SEND_BUFFER_SIZE.getInt(info)) > -1) {
                if (sendBufferSize > 0) {
                    newStream.getSocket().setSendBufferSize(sendBufferSize);
                } else {
                    LOGGER.log(Level.WARNING, "Ignore invalid value for sendBufferSize: {0}", sendBufferSize);
                }
            }
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Receive Buffer Size is {0}", newStream.getSocket().getReceiveBufferSize());
                LOGGER.log(Level.FINE, "Send Buffer Size is {0}", newStream.getSocket().getSendBufferSize());
            }
            if (!(newStream = this.enableGSSEncrypted(newStream, gssEncMode, hostSpec.getHost(), info, connectTimeout)).isGssEncrypted()) {
                newStream = this.enableSSL(newStream, sslMode, info, connectTimeout);
            }
            if (socketTimeout > 0) {
                newStream.setNetworkTimeout(socketTimeout * 1000);
            }
            List<String[]> paramList = this.getParametersForStartup(user, database, info);
            this.sendStartupPacket(newStream, paramList);
            this.doAuthentication(newStream, hostSpec.getHost(), user, info);
            return newStream;
        }
        catch (Exception e) {
            this.closeStream(newStream);
            throw e;
        }
    }

    @Override
    public QueryExecutor openConnectionImpl(HostSpec[] hostSpecs, Properties info) throws SQLException {
        HostRequirement targetServerType;
        SslMode sslMode = SslMode.of(info);
        GSSEncMode gssEncMode = GSSEncMode.of(info);
        String targetServerTypeStr = Nullness.castNonNull(PGProperty.TARGET_SERVER_TYPE.get(info));
        try {
            targetServerType = HostRequirement.getTargetServerType(targetServerTypeStr);
        }
        catch (IllegalArgumentException ex) {
            throw new PSQLException(GT.tr("Invalid targetServerType value: {0}", targetServerTypeStr), PSQLState.CONNECTION_UNABLE_TO_CONNECT);
        }
        SocketFactory socketFactory = SocketFactoryFactory.getSocketFactory(info);
        HostChooser hostChooser = HostChooserFactory.createHostChooser(hostSpecs, targetServerType, info);
        Iterator<CandidateHost> hostIter = hostChooser.iterator();
        HashMap<HostSpec, HostStatus> knownStates = new HashMap<HostSpec, HostStatus>();
        while (hostIter.hasNext()) {
            CandidateHost candidateHost = hostIter.next();
            HostSpec hostSpec = candidateHost.hostSpec;
            LOGGER.log(Level.FINE, "Trying to establish a protocol version 3 connection to {0}", hostSpec);
            HostStatus knownStatus = (HostStatus)((Object)knownStates.get(hostSpec));
            if (knownStatus != null && !candidateHost.targetServerType.allowConnectingTo(knownStatus)) {
                if (!LOGGER.isLoggable(Level.FINER)) continue;
                LOGGER.log(Level.FINER, "Known status of host {0} is {1}, and required status was {2}. Will try next host", new Object[]{hostSpec, knownStatus, candidateHost.targetServerType});
                continue;
            }
            PGStream newStream = null;
            try {
                try {
                    newStream = this.tryConnect(info, socketFactory, hostSpec, sslMode, gssEncMode);
                }
                catch (SQLException e) {
                    Exception ex;
                    if (sslMode == SslMode.PREFER && PSQLState.INVALID_AUTHORIZATION_SPECIFICATION.getState().equals(e.getSQLState())) {
                        ex = null;
                        try {
                            newStream = this.tryConnect(info, socketFactory, hostSpec, SslMode.DISABLE, gssEncMode);
                            LOGGER.log(Level.FINE, "Downgraded to non-encrypted connection for host {0}", hostSpec);
                        }
                        catch (IOException | SQLException ee) {
                            ex = ee;
                        }
                        if (ex != null) {
                            ConnectionFactoryImpl.log(Level.FINE, "sslMode==PREFER, however non-SSL connection failed as well", ex, new Object[0]);
                            e.addSuppressed(ex);
                            throw e;
                        }
                    }
                    if (sslMode == SslMode.ALLOW && PSQLState.INVALID_AUTHORIZATION_SPECIFICATION.getState().equals(e.getSQLState())) {
                        ex = null;
                        try {
                            newStream = this.tryConnect(info, socketFactory, hostSpec, SslMode.REQUIRE, gssEncMode);
                            LOGGER.log(Level.FINE, "Upgraded to encrypted connection for host {0}", hostSpec);
                        }
                        catch (SQLException ee) {
                            ex = ee;
                        }
                        catch (IOException ee) {
                            ex = ee;
                        }
                        if (ex != null) {
                            ConnectionFactoryImpl.log(Level.FINE, "sslMode==ALLOW, however SSL connection failed as well", ex, new Object[0]);
                            e.addSuppressed(ex);
                            throw e;
                        }
                    }
                    throw e;
                }
                int cancelSignalTimeout = PGProperty.CANCEL_SIGNAL_TIMEOUT.getInt(info) * 1000;
                Nullness.castNonNull(newStream);
                QueryExecutorImpl queryExecutor = new QueryExecutorImpl(newStream, cancelSignalTimeout, info);
                HostStatus hostStatus = HostStatus.ConnectOK;
                if (candidateHost.targetServerType != HostRequirement.any) {
                    hostStatus = this.isPrimary(queryExecutor) ? HostStatus.Primary : HostStatus.Secondary;
                }
                GlobalHostStatusTracker.reportHostStatus(hostSpec, hostStatus);
                knownStates.put(hostSpec, hostStatus);
                if (!candidateHost.targetServerType.allowConnectingTo(hostStatus)) {
                    queryExecutor.close();
                    continue;
                }
                this.runInitialQueries(queryExecutor, info);
                return queryExecutor;
            }
            catch (ConnectException cex) {
                GlobalHostStatusTracker.reportHostStatus(hostSpec, HostStatus.ConnectFail);
                knownStates.put(hostSpec, HostStatus.ConnectFail);
                if (hostIter.hasNext()) {
                    ConnectionFactoryImpl.log(Level.FINE, "ConnectException occurred while connecting to {0}", cex, hostSpec);
                    continue;
                }
                throw new PSQLException(GT.tr("Connection to {0} refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.", hostSpec), PSQLState.CONNECTION_UNABLE_TO_CONNECT, (Throwable)cex);
            }
            catch (IOException ioe) {
                this.closeStream(newStream);
                GlobalHostStatusTracker.reportHostStatus(hostSpec, HostStatus.ConnectFail);
                knownStates.put(hostSpec, HostStatus.ConnectFail);
                if (hostIter.hasNext()) {
                    ConnectionFactoryImpl.log(Level.FINE, "IOException occurred while connecting to {0}", ioe, hostSpec);
                    continue;
                }
                throw new PSQLException(GT.tr("The connection attempt failed.", new Object[0]), PSQLState.CONNECTION_UNABLE_TO_CONNECT, (Throwable)ioe);
            }
            catch (SQLException se) {
                this.closeStream(newStream);
                GlobalHostStatusTracker.reportHostStatus(hostSpec, HostStatus.ConnectFail);
                knownStates.put(hostSpec, HostStatus.ConnectFail);
                if (hostIter.hasNext()) {
                    ConnectionFactoryImpl.log(Level.FINE, "SQLException occurred while connecting to {0}", se, hostSpec);
                    continue;
                }
                throw se;
            }
        }
        throw new PSQLException(GT.tr("Could not find a server with specified targetServerType: {0}", new Object[]{targetServerType}), PSQLState.CONNECTION_UNABLE_TO_CONNECT);
    }

    private List<String[]> getParametersForStartup(String user, String database, Properties info) {
        String options;
        String currentSchema;
        ArrayList<String[]> paramList = new ArrayList<String[]>();
        paramList.add(new String[]{"user", user});
        paramList.add(new String[]{"database", database});
        paramList.add(new String[]{"client_encoding", "UTF8"});
        paramList.add(new String[]{"DateStyle", "ISO"});
        paramList.add(new String[]{"TimeZone", ConnectionFactoryImpl.createPostgresTimeZone()});
        Version assumeVersion = ServerVersion.from(PGProperty.ASSUME_MIN_SERVER_VERSION.get(info));
        if (assumeVersion.getVersionNum() >= ServerVersion.v9_0.getVersionNum()) {
            paramList.add(new String[]{"extra_float_digits", "3"});
            String appName = PGProperty.APPLICATION_NAME.get(info);
            if (appName != null) {
                paramList.add(new String[]{"application_name", appName});
            }
        } else {
            paramList.add(new String[]{"extra_float_digits", "2"});
        }
        String replication = PGProperty.REPLICATION.get(info);
        if (replication != null && assumeVersion.getVersionNum() >= ServerVersion.v9_4.getVersionNum()) {
            paramList.add(new String[]{"replication", replication});
        }
        if ((currentSchema = PGProperty.CURRENT_SCHEMA.get(info)) != null) {
            paramList.add(new String[]{"search_path", currentSchema});
        }
        if ((options = PGProperty.OPTIONS.get(info)) != null) {
            paramList.add(new String[]{"options", options});
        }
        return paramList;
    }

    private static void log(Level level, String msg, Throwable thrown, Object ... params) {
        if (!LOGGER.isLoggable(level)) {
            return;
        }
        LogRecord rec = new LogRecord(level, msg);
        rec.setLoggerName(LOGGER.getName());
        rec.setParameters(params);
        rec.setThrown(thrown);
        LOGGER.log(rec);
    }

    private static String createPostgresTimeZone() {
        String start;
        String tz = TimeZone.getDefault().getID();
        if (tz.length() <= 3 || !tz.startsWith("GMT")) {
            return tz;
        }
        char sign = tz.charAt(3);
        switch (sign) {
            case '+': {
                start = "GMT-";
                break;
            }
            case '-': {
                start = "GMT+";
                break;
            }
            default: {
                return tz;
            }
        }
        return start + tz.substring(4);
    }

    private PGStream enableGSSEncrypted(PGStream pgStream, GSSEncMode gssEncMode, String host, Properties info, int connectTimeout) throws IOException, PSQLException {
        if (gssEncMode == GSSEncMode.DISABLE) {
            return pgStream;
        }
        if (gssEncMode == GSSEncMode.ALLOW) {
            return pgStream;
        }
        String user = PGProperty.USER.get(info);
        if (user == null) {
            throw new PSQLException("GSSAPI encryption required but was impossible user is null", PSQLState.CONNECTION_REJECTED);
        }
        LOGGER.log(Level.FINEST, " FE=> GSSENCRequest");
        pgStream.sendInteger4(8);
        pgStream.sendInteger2(1234);
        pgStream.sendInteger2(5680);
        pgStream.flush();
        int beresp = pgStream.receiveChar();
        switch (beresp) {
            case 69: {
                LOGGER.log(Level.FINEST, " <=BE GSSEncrypted Error");
                if (gssEncMode.requireEncryption()) {
                    throw new PSQLException(GT.tr("The server does not support GSS Encoding.", new Object[0]), PSQLState.CONNECTION_REJECTED);
                }
                pgStream.close();
                return new PGStream(pgStream.getSocketFactory(), pgStream.getHostSpec(), connectTimeout);
            }
            case 78: {
                LOGGER.log(Level.FINEST, " <=BE GSSEncrypted Refused");
                if (gssEncMode.requireEncryption()) {
                    throw new PSQLException(GT.tr("The server does not support GSS Encryption.", new Object[0]), PSQLState.CONNECTION_REJECTED);
                }
                return pgStream;
            }
            case 71: {
                LOGGER.log(Level.FINEST, " <=BE GSSEncryptedOk");
                try {
                    AuthenticationPluginManager.withPassword(AuthenticationRequestType.GSS, info, password -> {
                        MakeGSS.authenticate(true, pgStream, host, user, password, PGProperty.JAAS_APPLICATION_NAME.get(info), PGProperty.KERBEROS_SERVER_NAME.get(info), false, PGProperty.JAAS_LOGIN.getBoolean(info), PGProperty.LOG_SERVER_ERROR_DETAIL.getBoolean(info));
                        return Void.TYPE;
                    });
                    return pgStream;
                }
                catch (PSQLException ex) {
                    if (gssEncMode != GSSEncMode.PREFER) break;
                    return new PGStream(pgStream, connectTimeout);
                }
            }
        }
        throw new PSQLException(GT.tr("An error occurred while setting up the GSS Encoded connection.", new Object[0]), PSQLState.PROTOCOL_VIOLATION);
    }

    private PGStream enableSSL(PGStream pgStream, SslMode sslMode, Properties info, int connectTimeout) throws IOException, PSQLException {
        if (sslMode == SslMode.DISABLE) {
            return pgStream;
        }
        if (sslMode == SslMode.ALLOW) {
            return pgStream;
        }
        LOGGER.log(Level.FINEST, " FE=> SSLRequest");
        int sslTimeout = PGProperty.SSL_RESPONSE_TIMEOUT.getInt(info);
        int currentTimeout = pgStream.getSocket().getSoTimeout();
        if (currentTimeout > 0 && currentTimeout < sslTimeout) {
            sslTimeout = currentTimeout;
        }
        pgStream.getSocket().setSoTimeout(sslTimeout);
        pgStream.sendInteger4(8);
        pgStream.sendInteger2(1234);
        pgStream.sendInteger2(5679);
        pgStream.flush();
        int beresp = pgStream.receiveChar();
        pgStream.getSocket().setSoTimeout(currentTimeout);
        switch (beresp) {
            case 69: {
                LOGGER.log(Level.FINEST, " <=BE SSLError");
                if (sslMode.requireEncryption()) {
                    throw new PSQLException(GT.tr("The server does not support SSL.", new Object[0]), PSQLState.CONNECTION_REJECTED);
                }
                return new PGStream(pgStream, connectTimeout);
            }
            case 78: {
                LOGGER.log(Level.FINEST, " <=BE SSLRefused");
                if (sslMode.requireEncryption()) {
                    throw new PSQLException(GT.tr("The server does not support SSL.", new Object[0]), PSQLState.CONNECTION_REJECTED);
                }
                return pgStream;
            }
            case 83: {
                LOGGER.log(Level.FINEST, " <=BE SSLOk");
                MakeSSL.convert(pgStream, info);
                return pgStream;
            }
        }
        throw new PSQLException(GT.tr("An error occurred while setting up the SSL connection.", new Object[0]), PSQLState.PROTOCOL_VIOLATION);
    }

    private void sendStartupPacket(PGStream pgStream, List<String[]> params) throws IOException {
        if (LOGGER.isLoggable(Level.FINEST)) {
            StringBuilder details = new StringBuilder();
            for (int i = 0; i < params.size(); ++i) {
                if (i != 0) {
                    details.append(", ");
                }
                details.append(params.get(i)[0]);
                details.append("=");
                details.append(params.get(i)[1]);
            }
            LOGGER.log(Level.FINEST, " FE=> StartupPacket({0})", details);
        }
        int length = 8;
        byte[][] encodedParams = new byte[params.size() * 2][];
        for (int i = 0; i < params.size(); ++i) {
            encodedParams[i * 2] = params.get(i)[0].getBytes(StandardCharsets.UTF_8);
            encodedParams[i * 2 + 1] = params.get(i)[1].getBytes(StandardCharsets.UTF_8);
            length += encodedParams[i * 2].length + 1 + encodedParams[i * 2 + 1].length + 1;
        }
        pgStream.sendInteger4(++length);
        pgStream.sendInteger2(3);
        pgStream.sendInteger2(0);
        for (byte[] encodedParam : encodedParams) {
            pgStream.send(encodedParam);
            pgStream.sendChar(0);
        }
        pgStream.sendChar(0);
        pgStream.flush();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void doAuthentication(PGStream pgStream, String host, String user, Properties info) throws IOException, SQLException {
        block36: {
            ISSPIClient sspiClient = null;
            ScramAuthenticator scramAuthenticator = null;
            try {
                while (true) {
                    int beresp = pgStream.receiveChar();
                    block4 : switch (beresp) {
                        case 69: {
                            int elen = pgStream.receiveInteger4();
                            ServerErrorMessage errorMsg = new ServerErrorMessage(pgStream.receiveErrorString(elen - 4));
                            LOGGER.log(Level.FINEST, " <=BE ErrorMessage({0})", errorMsg);
                            throw new PSQLException(errorMsg, PGProperty.LOG_SERVER_ERROR_DETAIL.getBoolean(info));
                        }
                        case 82: {
                            int msgLen = pgStream.receiveInteger4();
                            int areq = pgStream.receiveInteger4();
                            switch (areq) {
                                case 5: {
                                    byte[] md5Salt = pgStream.receive(4);
                                    if (LOGGER.isLoggable(Level.FINEST)) {
                                        LOGGER.log(Level.FINEST, " <=BE AuthenticationReqMD5(salt={0})", Utils.toHexString(md5Salt));
                                    }
                                    byte[] digest = AuthenticationPluginManager.withEncodedPassword(AuthenticationRequestType.MD5_PASSWORD, info, encodedPassword -> MD5Digest.encode(user.getBytes(StandardCharsets.UTF_8), encodedPassword, md5Salt));
                                    if (LOGGER.isLoggable(Level.FINEST)) {
                                        LOGGER.log(Level.FINEST, " FE=> Password(md5digest={0})", new String(digest, StandardCharsets.US_ASCII));
                                    }
                                    try {
                                        pgStream.sendChar(112);
                                        pgStream.sendInteger4(4 + digest.length + 1);
                                        pgStream.send(digest);
                                    }
                                    finally {
                                        Arrays.fill(digest, (byte)0);
                                    }
                                    pgStream.sendChar(0);
                                    pgStream.flush();
                                    break block4;
                                }
                                case 3: {
                                    LOGGER.log(Level.FINEST, "<=BE AuthenticationReqPassword");
                                    LOGGER.log(Level.FINEST, " FE=> Password(password=<not shown>)");
                                    AuthenticationPluginManager.withEncodedPassword(AuthenticationRequestType.CLEARTEXT_PASSWORD, info, encodedPassword -> {
                                        pgStream.sendChar(112);
                                        pgStream.sendInteger4(4 + ((byte[])encodedPassword).length + 1);
                                        pgStream.send((byte[])encodedPassword);
                                        return Void.TYPE;
                                    });
                                    pgStream.sendChar(0);
                                    pgStream.flush();
                                    break block4;
                                }
                                case 7: 
                                case 9: {
                                    String gsslib = PGProperty.GSS_LIB.get(info);
                                    boolean usespnego = PGProperty.USE_SPNEGO.getBoolean(info);
                                    boolean useSSPI = false;
                                    if ("gssapi".equals(gsslib)) {
                                        LOGGER.log(Level.FINE, "Using JSSE GSSAPI, param gsslib=gssapi");
                                    } else if (areq == 7 && !"sspi".equals(gsslib)) {
                                        LOGGER.log(Level.FINE, "Using JSSE GSSAPI, gssapi requested by server and gsslib=sspi not forced");
                                    } else {
                                        sspiClient = this.createSSPI(pgStream, PGProperty.SSPI_SERVICE_CLASS.get(info), areq == 9 || areq == 7 && usespnego);
                                        useSSPI = sspiClient.isSSPISupported();
                                        LOGGER.log(Level.FINE, "SSPI support detected: {0}", useSSPI);
                                        if (!useSSPI) {
                                            sspiClient = null;
                                            if ("sspi".equals(gsslib)) {
                                                throw new PSQLException("SSPI forced with gsslib=sspi, but SSPI not available; set loglevel=2 for details", PSQLState.CONNECTION_UNABLE_TO_CONNECT);
                                            }
                                        }
                                        if (LOGGER.isLoggable(Level.FINE)) {
                                            LOGGER.log(Level.FINE, "Using SSPI: {0}, gsslib={1} and SSPI support detected", new Object[]{useSSPI, gsslib});
                                        }
                                    }
                                    if (useSSPI) {
                                        Nullness.castNonNull(sspiClient).startSSPI();
                                        break block4;
                                    }
                                    AuthenticationPluginManager.withPassword(AuthenticationRequestType.GSS, info, password -> {
                                        MakeGSS.authenticate(false, pgStream, host, user, password, PGProperty.JAAS_APPLICATION_NAME.get(info), PGProperty.KERBEROS_SERVER_NAME.get(info), usespnego, PGProperty.JAAS_LOGIN.getBoolean(info), PGProperty.LOG_SERVER_ERROR_DETAIL.getBoolean(info));
                                        return Void.TYPE;
                                    });
                                    break block4;
                                }
                                case 8: {
                                    Nullness.castNonNull(sspiClient).continueSSPI(msgLen - 8);
                                    break block4;
                                }
                                case 10: {
                                    LOGGER.log(Level.FINEST, " <=BE AuthenticationSASL");
                                    scramAuthenticator = AuthenticationPluginManager.withPassword(AuthenticationRequestType.SASL, info, password -> {
                                        if (password == null) {
                                            throw new PSQLException(GT.tr("The server requested SCRAM-based authentication, but no password was provided.", new Object[0]), PSQLState.CONNECTION_REJECTED);
                                        }
                                        if (((char[])password).length == 0) {
                                            throw new PSQLException(GT.tr("The server requested SCRAM-based authentication, but the password is an empty string.", new Object[0]), PSQLState.CONNECTION_REJECTED);
                                        }
                                        return new ScramAuthenticator(user, String.valueOf(password), pgStream);
                                    });
                                    scramAuthenticator.processServerMechanismsAndInit();
                                    scramAuthenticator.sendScramClientFirstMessage();
                                    break block4;
                                }
                                case 11: {
                                    ((ScramAuthenticator)Nullness.castNonNull(scramAuthenticator)).processServerFirstMessage(msgLen - 4 - 4);
                                    break block4;
                                }
                                case 12: {
                                    ((ScramAuthenticator)Nullness.castNonNull(scramAuthenticator)).verifyServerSignature(msgLen - 4 - 4);
                                    break block4;
                                }
                                case 0: {
                                    LOGGER.log(Level.FINEST, " <=BE AuthenticationOk");
                                    break block36;
                                }
                                default: {
                                    LOGGER.log(Level.FINEST, " <=BE AuthenticationReq (unsupported type {0})", areq);
                                    throw new PSQLException(GT.tr("The authentication type {0} is not supported. Check that you have configured the pg_hba.conf file to include the client''s IP address or subnet, and that it is using an authentication scheme supported by the driver.", areq), PSQLState.CONNECTION_REJECTED);
                                }
                            }
                        }
                        default: {
                            throw new PSQLException(GT.tr("Protocol error.  Session setup failed.", new Object[0]), PSQLState.PROTOCOL_VIOLATION);
                        }
                    }
                }
            }
            finally {
                if (sspiClient != null) {
                    try {
                        sspiClient.dispose();
                    }
                    catch (RuntimeException ex) {
                        LOGGER.log(Level.FINE, "Unexpected error during SSPI context disposal", ex);
                    }
                }
            }
        }
    }

    private void runInitialQueries(QueryExecutor queryExecutor, Properties info) throws SQLException {
        String appName;
        String assumeMinServerVersion = PGProperty.ASSUME_MIN_SERVER_VERSION.get(info);
        if (Utils.parseServerVersionStr(assumeMinServerVersion) >= ServerVersion.v9_0.getVersionNum()) {
            return;
        }
        int dbVersion = queryExecutor.getServerVersionNum();
        if (PGProperty.GROUP_STARTUP_PARAMETERS.getBoolean(info) && dbVersion >= ServerVersion.v9_0.getVersionNum()) {
            SetupQueryRunner.run(queryExecutor, "BEGIN", false);
        }
        if (dbVersion >= ServerVersion.v9_0.getVersionNum()) {
            SetupQueryRunner.run(queryExecutor, "SET extra_float_digits = 3", false);
        }
        if ((appName = PGProperty.APPLICATION_NAME.get(info)) != null && dbVersion >= ServerVersion.v9_0.getVersionNum()) {
            StringBuilder sql = new StringBuilder();
            sql.append("SET application_name = '");
            Utils.escapeLiteral(sql, appName, queryExecutor.getStandardConformingStrings());
            sql.append("'");
            SetupQueryRunner.run(queryExecutor, sql.toString(), false);
        }
        if (PGProperty.GROUP_STARTUP_PARAMETERS.getBoolean(info) && dbVersion >= ServerVersion.v9_0.getVersionNum()) {
            SetupQueryRunner.run(queryExecutor, "COMMIT", false);
        }
    }

    private boolean isPrimary(QueryExecutor queryExecutor) throws SQLException, IOException {
        String inHotStandby = queryExecutor.getParameterStatus(IN_HOT_STANDBY);
        if ("on".equalsIgnoreCase(inHotStandby)) {
            return false;
        }
        Tuple results = SetupQueryRunner.run(queryExecutor, "show transaction_read_only", true);
        Tuple nonNullResults = Nullness.castNonNull(results);
        String queriedTransactionReadonly = queryExecutor.getEncoding().decode(Nullness.castNonNull(nonNullResults.get(0)));
        return queriedTransactionReadonly.equalsIgnoreCase("off");
    }
}

