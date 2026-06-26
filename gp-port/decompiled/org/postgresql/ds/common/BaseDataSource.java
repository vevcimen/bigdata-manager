/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.ds.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;
import javax.sql.CommonDataSource;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.Driver;
import org.postgresql.PGProperty;
import org.postgresql.ds.common.PGObjectFactory;
import org.postgresql.jdbc.AutoSave;
import org.postgresql.jdbc.PreferQueryMode;
import org.postgresql.util.ExpressionProperties;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.URLCoder;
import org.postgresql.util.internal.Nullness;

public abstract class BaseDataSource
implements CommonDataSource,
Referenceable {
    private static final Logger LOGGER = Logger.getLogger(BaseDataSource.class.getName());
    private String[] serverNames = new String[]{"localhost"};
    private @Nullable String databaseName = "";
    private @Nullable String user;
    private @Nullable String password;
    private int[] portNumbers = new int[]{0};
    private Properties properties = new Properties();

    public Connection getConnection() throws SQLException {
        return this.getConnection(this.user, this.password);
    }

    public Connection getConnection(@Nullable String user, @Nullable String password) throws SQLException {
        try {
            Connection con = DriverManager.getConnection(this.getUrl(), user, password);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Created a {0} for {1} at {2}", new Object[]{this.getDescription(), user, this.getUrl()});
            }
            return con;
        }
        catch (SQLException e) {
            LOGGER.log(Level.FINE, "Failed to create a {0} for {1} at {2}: {3}", new Object[]{this.getDescription(), user, this.getUrl(), e});
            throw e;
        }
    }

    @Override
    public @Nullable PrintWriter getLogWriter() {
        return null;
    }

    @Override
    public void setLogWriter(@Nullable PrintWriter printWriter) {
    }

    @Deprecated
    public String getServerName() {
        return this.serverNames[0];
    }

    public String[] getServerNames() {
        return this.serverNames;
    }

    @Deprecated
    public void setServerName(String serverName) {
        this.setServerNames(new String[]{serverName});
    }

    public void setServerNames(@Nullable String @Nullable [] serverNames) {
        if (serverNames == null || serverNames.length == 0) {
            this.serverNames = new String[]{"localhost"};
        } else {
            serverNames = (String[])serverNames.clone();
            for (int i = 0; i < serverNames.length; ++i) {
                String serverName = serverNames[i];
                if (serverName != null && !serverName.equals("")) continue;
                serverNames[i] = "localhost";
            }
            this.serverNames = serverNames;
        }
    }

    public @Nullable String getDatabaseName() {
        return this.databaseName;
    }

    public void setDatabaseName(@Nullable String databaseName) {
        this.databaseName = databaseName;
    }

    public abstract String getDescription();

    public @Nullable String getUser() {
        return this.user;
    }

    public void setUser(@Nullable String user) {
        this.user = user;
    }

    public @Nullable String getPassword() {
        return this.password;
    }

    public void setPassword(@Nullable String password) {
        this.password = password;
    }

    @Deprecated
    public int getPortNumber() {
        if (this.portNumbers == null || this.portNumbers.length == 0) {
            return 0;
        }
        return this.portNumbers[0];
    }

    public int[] getPortNumbers() {
        return this.portNumbers;
    }

    @Deprecated
    public void setPortNumber(int portNumber) {
        this.setPortNumbers(new int[]{portNumber});
    }

    public void setPortNumbers(int @Nullable [] portNumbers) {
        if (portNumbers == null || portNumbers.length == 0) {
            portNumbers = new int[]{0};
        }
        this.portNumbers = Arrays.copyOf(portNumbers, portNumbers.length);
    }

    public @Nullable String getOptions() {
        return PGProperty.OPTIONS.get(this.properties);
    }

    public void setOptions(@Nullable String options) {
        PGProperty.OPTIONS.set(this.properties, options);
    }

    @Override
    public int getLoginTimeout() {
        return PGProperty.LOGIN_TIMEOUT.getIntNoCheck(this.properties);
    }

    @Override
    public void setLoginTimeout(int loginTimeout) {
        PGProperty.LOGIN_TIMEOUT.set(this.properties, loginTimeout);
    }

    public int getConnectTimeout() {
        return PGProperty.CONNECT_TIMEOUT.getIntNoCheck(this.properties);
    }

    public void setConnectTimeout(int connectTimeout) {
        PGProperty.CONNECT_TIMEOUT.set(this.properties, connectTimeout);
    }

    public int getSslResponseTimeout() {
        return PGProperty.SSL_RESPONSE_TIMEOUT.getIntNoCheck(this.properties);
    }

    public void setSslResponseTimeout(int sslResponseTimeout) {
        PGProperty.SSL_RESPONSE_TIMEOUT.set(this.properties, sslResponseTimeout);
    }

    public int getProtocolVersion() {
        if (!PGProperty.PROTOCOL_VERSION.isPresent(this.properties)) {
            return 0;
        }
        return PGProperty.PROTOCOL_VERSION.getIntNoCheck(this.properties);
    }

    public void setProtocolVersion(int protocolVersion) {
        if (protocolVersion == 0) {
            PGProperty.PROTOCOL_VERSION.set(this.properties, null);
        } else {
            PGProperty.PROTOCOL_VERSION.set(this.properties, protocolVersion);
        }
    }

    public boolean getQuoteReturningIdentifiers() {
        return PGProperty.QUOTE_RETURNING_IDENTIFIERS.getBoolean(this.properties);
    }

    public void setQuoteReturningIdentifiers(boolean quoteIdentifiers) {
        PGProperty.QUOTE_RETURNING_IDENTIFIERS.set(this.properties, quoteIdentifiers);
    }

    public int getReceiveBufferSize() {
        return PGProperty.RECEIVE_BUFFER_SIZE.getIntNoCheck(this.properties);
    }

    public void setReceiveBufferSize(int nbytes) {
        PGProperty.RECEIVE_BUFFER_SIZE.set(this.properties, nbytes);
    }

    public int getSendBufferSize() {
        return PGProperty.SEND_BUFFER_SIZE.getIntNoCheck(this.properties);
    }

    public void setSendBufferSize(int nbytes) {
        PGProperty.SEND_BUFFER_SIZE.set(this.properties, nbytes);
    }

    public void setPrepareThreshold(int count) {
        PGProperty.PREPARE_THRESHOLD.set(this.properties, count);
    }

    public int getPrepareThreshold() {
        return PGProperty.PREPARE_THRESHOLD.getIntNoCheck(this.properties);
    }

    public int getPreparedStatementCacheQueries() {
        return PGProperty.PREPARED_STATEMENT_CACHE_QUERIES.getIntNoCheck(this.properties);
    }

    public void setPreparedStatementCacheQueries(int cacheSize) {
        PGProperty.PREPARED_STATEMENT_CACHE_QUERIES.set(this.properties, cacheSize);
    }

    public int getPreparedStatementCacheSizeMiB() {
        return PGProperty.PREPARED_STATEMENT_CACHE_SIZE_MIB.getIntNoCheck(this.properties);
    }

    public void setPreparedStatementCacheSizeMiB(int cacheSize) {
        PGProperty.PREPARED_STATEMENT_CACHE_SIZE_MIB.set(this.properties, cacheSize);
    }

    public int getDatabaseMetadataCacheFields() {
        return PGProperty.DATABASE_METADATA_CACHE_FIELDS.getIntNoCheck(this.properties);
    }

    public void setDatabaseMetadataCacheFields(int cacheSize) {
        PGProperty.DATABASE_METADATA_CACHE_FIELDS.set(this.properties, cacheSize);
    }

    public int getDatabaseMetadataCacheFieldsMiB() {
        return PGProperty.DATABASE_METADATA_CACHE_FIELDS_MIB.getIntNoCheck(this.properties);
    }

    public void setDatabaseMetadataCacheFieldsMiB(int cacheSize) {
        PGProperty.DATABASE_METADATA_CACHE_FIELDS_MIB.set(this.properties, cacheSize);
    }

    public void setDefaultRowFetchSize(int fetchSize) {
        PGProperty.DEFAULT_ROW_FETCH_SIZE.set(this.properties, fetchSize);
    }

    public int getDefaultRowFetchSize() {
        return PGProperty.DEFAULT_ROW_FETCH_SIZE.getIntNoCheck(this.properties);
    }

    public void setUnknownLength(int unknownLength) {
        PGProperty.UNKNOWN_LENGTH.set(this.properties, unknownLength);
    }

    public int getUnknownLength() {
        return PGProperty.UNKNOWN_LENGTH.getIntNoCheck(this.properties);
    }

    public void setSocketTimeout(int seconds) {
        PGProperty.SOCKET_TIMEOUT.set(this.properties, seconds);
    }

    public int getSocketTimeout() {
        return PGProperty.SOCKET_TIMEOUT.getIntNoCheck(this.properties);
    }

    public void setCancelSignalTimeout(int seconds) {
        PGProperty.CANCEL_SIGNAL_TIMEOUT.set(this.properties, seconds);
    }

    public int getCancelSignalTimeout() {
        return PGProperty.CANCEL_SIGNAL_TIMEOUT.getIntNoCheck(this.properties);
    }

    public void setSsl(boolean enabled) {
        if (enabled) {
            PGProperty.SSL.set(this.properties, true);
        } else {
            PGProperty.SSL.set(this.properties, false);
        }
    }

    public boolean getSsl() {
        return PGProperty.SSL.getBoolean(this.properties) || "".equals(PGProperty.SSL.get(this.properties));
    }

    public void setSslfactory(String classname) {
        PGProperty.SSL_FACTORY.set(this.properties, classname);
    }

    public @Nullable String getSslfactory() {
        return PGProperty.SSL_FACTORY.get(this.properties);
    }

    public @Nullable String getSslMode() {
        return PGProperty.SSL_MODE.get(this.properties);
    }

    public void setSslMode(@Nullable String mode) {
        PGProperty.SSL_MODE.set(this.properties, mode);
    }

    public @Nullable String getSslFactoryArg() {
        return PGProperty.SSL_FACTORY_ARG.get(this.properties);
    }

    public void setSslFactoryArg(@Nullable String arg) {
        PGProperty.SSL_FACTORY_ARG.set(this.properties, arg);
    }

    public @Nullable String getSslHostnameVerifier() {
        return PGProperty.SSL_HOSTNAME_VERIFIER.get(this.properties);
    }

    public void setSslHostnameVerifier(@Nullable String className) {
        PGProperty.SSL_HOSTNAME_VERIFIER.set(this.properties, className);
    }

    public @Nullable String getSslCert() {
        return PGProperty.SSL_CERT.get(this.properties);
    }

    public void setSslCert(@Nullable String file) {
        PGProperty.SSL_CERT.set(this.properties, file);
    }

    public @Nullable String getSslKey() {
        return PGProperty.SSL_KEY.get(this.properties);
    }

    public void setSslKey(@Nullable String file) {
        PGProperty.SSL_KEY.set(this.properties, file);
    }

    public @Nullable String getSslRootCert() {
        return PGProperty.SSL_ROOT_CERT.get(this.properties);
    }

    public void setSslRootCert(@Nullable String file) {
        PGProperty.SSL_ROOT_CERT.set(this.properties, file);
    }

    public @Nullable String getSslPassword() {
        return PGProperty.SSL_PASSWORD.get(this.properties);
    }

    public void setSslPassword(@Nullable String password) {
        PGProperty.SSL_PASSWORD.set(this.properties, password);
    }

    public @Nullable String getSslPasswordCallback() {
        return PGProperty.SSL_PASSWORD_CALLBACK.get(this.properties);
    }

    public void setSslPasswordCallback(@Nullable String className) {
        PGProperty.SSL_PASSWORD_CALLBACK.set(this.properties, className);
    }

    public void setApplicationName(@Nullable String applicationName) {
        PGProperty.APPLICATION_NAME.set(this.properties, applicationName);
    }

    public String getApplicationName() {
        return Nullness.castNonNull(PGProperty.APPLICATION_NAME.get(this.properties));
    }

    public void setTargetServerType(@Nullable String targetServerType) {
        PGProperty.TARGET_SERVER_TYPE.set(this.properties, targetServerType);
    }

    public String getTargetServerType() {
        return Nullness.castNonNull(PGProperty.TARGET_SERVER_TYPE.get(this.properties));
    }

    public void setLoadBalanceHosts(boolean loadBalanceHosts) {
        PGProperty.LOAD_BALANCE_HOSTS.set(this.properties, loadBalanceHosts);
    }

    public boolean getLoadBalanceHosts() {
        return PGProperty.LOAD_BALANCE_HOSTS.isPresent(this.properties);
    }

    public void setHostRecheckSeconds(int hostRecheckSeconds) {
        PGProperty.HOST_RECHECK_SECONDS.set(this.properties, hostRecheckSeconds);
    }

    public int getHostRecheckSeconds() {
        return PGProperty.HOST_RECHECK_SECONDS.getIntNoCheck(this.properties);
    }

    public void setTcpKeepAlive(boolean enabled) {
        PGProperty.TCP_KEEP_ALIVE.set(this.properties, enabled);
    }

    public boolean getTcpKeepAlive() {
        return PGProperty.TCP_KEEP_ALIVE.getBoolean(this.properties);
    }

    public void setTcpNoDelay(boolean enabled) {
        PGProperty.TCP_NO_DELAY.set(this.properties, enabled);
    }

    public boolean getTcpNoDelay() {
        return PGProperty.TCP_NO_DELAY.getBoolean(this.properties);
    }

    public void setBinaryTransfer(boolean enabled) {
        PGProperty.BINARY_TRANSFER.set(this.properties, enabled);
    }

    public boolean getBinaryTransfer() {
        return PGProperty.BINARY_TRANSFER.getBoolean(this.properties);
    }

    public void setBinaryTransferEnable(@Nullable String oidList) {
        PGProperty.BINARY_TRANSFER_ENABLE.set(this.properties, oidList);
    }

    public String getBinaryTransferEnable() {
        return Nullness.castNonNull(PGProperty.BINARY_TRANSFER_ENABLE.get(this.properties));
    }

    public void setBinaryTransferDisable(@Nullable String oidList) {
        PGProperty.BINARY_TRANSFER_DISABLE.set(this.properties, oidList);
    }

    public String getBinaryTransferDisable() {
        return Nullness.castNonNull(PGProperty.BINARY_TRANSFER_DISABLE.get(this.properties));
    }

    public @Nullable String getStringType() {
        return PGProperty.STRING_TYPE.get(this.properties);
    }

    public void setStringType(@Nullable String stringType) {
        PGProperty.STRING_TYPE.set(this.properties, stringType);
    }

    public boolean isColumnSanitiserDisabled() {
        return PGProperty.DISABLE_COLUMN_SANITISER.getBoolean(this.properties);
    }

    public boolean getDisableColumnSanitiser() {
        return PGProperty.DISABLE_COLUMN_SANITISER.getBoolean(this.properties);
    }

    public void setDisableColumnSanitiser(boolean disableColumnSanitiser) {
        PGProperty.DISABLE_COLUMN_SANITISER.set(this.properties, disableColumnSanitiser);
    }

    public @Nullable String getCurrentSchema() {
        return PGProperty.CURRENT_SCHEMA.get(this.properties);
    }

    public void setCurrentSchema(@Nullable String currentSchema) {
        PGProperty.CURRENT_SCHEMA.set(this.properties, currentSchema);
    }

    public boolean getReadOnly() {
        return PGProperty.READ_ONLY.getBoolean(this.properties);
    }

    public void setReadOnly(boolean readOnly) {
        PGProperty.READ_ONLY.set(this.properties, readOnly);
    }

    public String getReadOnlyMode() {
        return Nullness.castNonNull(PGProperty.READ_ONLY_MODE.get(this.properties));
    }

    public void setReadOnlyMode(@Nullable String mode) {
        PGProperty.READ_ONLY_MODE.set(this.properties, mode);
    }

    public boolean getLogUnclosedConnections() {
        return PGProperty.LOG_UNCLOSED_CONNECTIONS.getBoolean(this.properties);
    }

    public void setLogUnclosedConnections(boolean enabled) {
        PGProperty.LOG_UNCLOSED_CONNECTIONS.set(this.properties, enabled);
    }

    public boolean getLogServerErrorDetail() {
        return PGProperty.LOG_SERVER_ERROR_DETAIL.getBoolean(this.properties);
    }

    public void setLogServerErrorDetail(boolean enabled) {
        PGProperty.LOG_SERVER_ERROR_DETAIL.set(this.properties, enabled);
    }

    public @Nullable String getAssumeMinServerVersion() {
        return PGProperty.ASSUME_MIN_SERVER_VERSION.get(this.properties);
    }

    public void setAssumeMinServerVersion(@Nullable String minVersion) {
        PGProperty.ASSUME_MIN_SERVER_VERSION.set(this.properties, minVersion);
    }

    public boolean getGroupStartupParameters() {
        return PGProperty.GROUP_STARTUP_PARAMETERS.getBoolean(this.properties);
    }

    public void setGroupStartupParameters(boolean groupStartupParameters) {
        PGProperty.GROUP_STARTUP_PARAMETERS.set(this.properties, groupStartupParameters);
    }

    public @Nullable String getJaasApplicationName() {
        return PGProperty.JAAS_APPLICATION_NAME.get(this.properties);
    }

    public void setJaasApplicationName(@Nullable String name) {
        PGProperty.JAAS_APPLICATION_NAME.set(this.properties, name);
    }

    public boolean getJaasLogin() {
        return PGProperty.JAAS_LOGIN.getBoolean(this.properties);
    }

    public void setJaasLogin(boolean doLogin) {
        PGProperty.JAAS_LOGIN.set(this.properties, doLogin);
    }

    public @Nullable String getKerberosServerName() {
        return PGProperty.KERBEROS_SERVER_NAME.get(this.properties);
    }

    public void setKerberosServerName(@Nullable String serverName) {
        PGProperty.KERBEROS_SERVER_NAME.set(this.properties, serverName);
    }

    public boolean getUseSpNego() {
        return PGProperty.USE_SPNEGO.getBoolean(this.properties);
    }

    public void setUseSpNego(boolean use) {
        PGProperty.USE_SPNEGO.set(this.properties, use);
    }

    public @Nullable String getGssLib() {
        return PGProperty.GSS_LIB.get(this.properties);
    }

    public void setGssLib(@Nullable String lib) {
        PGProperty.GSS_LIB.set(this.properties, lib);
    }

    public String getGssEncMode() {
        return Nullness.castNonNull(PGProperty.GSS_ENC_MODE.get(this.properties));
    }

    public void setGssEncMode(@Nullable String mode) {
        PGProperty.GSS_ENC_MODE.set(this.properties, mode);
    }

    public @Nullable String getSspiServiceClass() {
        return PGProperty.SSPI_SERVICE_CLASS.get(this.properties);
    }

    public void setSspiServiceClass(@Nullable String serviceClass) {
        PGProperty.SSPI_SERVICE_CLASS.set(this.properties, serviceClass);
    }

    public boolean getAllowEncodingChanges() {
        return PGProperty.ALLOW_ENCODING_CHANGES.getBoolean(this.properties);
    }

    public void setAllowEncodingChanges(boolean allow) {
        PGProperty.ALLOW_ENCODING_CHANGES.set(this.properties, allow);
    }

    public @Nullable String getSocketFactory() {
        return PGProperty.SOCKET_FACTORY.get(this.properties);
    }

    public void setSocketFactory(@Nullable String socketFactoryClassName) {
        PGProperty.SOCKET_FACTORY.set(this.properties, socketFactoryClassName);
    }

    public @Nullable String getSocketFactoryArg() {
        return PGProperty.SOCKET_FACTORY_ARG.get(this.properties);
    }

    public void setSocketFactoryArg(@Nullable String socketFactoryArg) {
        PGProperty.SOCKET_FACTORY_ARG.set(this.properties, socketFactoryArg);
    }

    public void setReplication(@Nullable String replication) {
        PGProperty.REPLICATION.set(this.properties, replication);
    }

    public String getEscapeSyntaxCallMode() {
        return Nullness.castNonNull(PGProperty.ESCAPE_SYNTAX_CALL_MODE.get(this.properties));
    }

    public void setEscapeSyntaxCallMode(@Nullable String callMode) {
        PGProperty.ESCAPE_SYNTAX_CALL_MODE.set(this.properties, callMode);
    }

    public @Nullable String getReplication() {
        return PGProperty.REPLICATION.get(this.properties);
    }

    public @Nullable String getLocalSocketAddress() {
        return PGProperty.LOCAL_SOCKET_ADDRESS.get(this.properties);
    }

    public void setLocalSocketAddress(String localSocketAddress) {
        PGProperty.LOCAL_SOCKET_ADDRESS.set(this.properties, localSocketAddress);
    }

    @Deprecated
    public @Nullable String getLoggerLevel() {
        return PGProperty.LOGGER_LEVEL.get(this.properties);
    }

    @Deprecated
    public void setLoggerLevel(@Nullable String loggerLevel) {
        PGProperty.LOGGER_LEVEL.set(this.properties, loggerLevel);
    }

    @Deprecated
    public @Nullable String getLoggerFile() {
        ExpressionProperties exprProps = new ExpressionProperties(this.properties, System.getProperties());
        return PGProperty.LOGGER_FILE.get(exprProps);
    }

    @Deprecated
    public void setLoggerFile(@Nullable String loggerFile) {
        PGProperty.LOGGER_FILE.set(this.properties, loggerFile);
    }

    public String getUrl() {
        StringBuilder url = new StringBuilder(100);
        url.append("jdbc:postgresql://");
        for (int i = 0; i < this.serverNames.length; ++i) {
            if (i > 0) {
                url.append(",");
            }
            url.append(this.serverNames[i]);
            if (this.portNumbers == null || this.portNumbers.length < i || this.portNumbers[i] == 0) continue;
            url.append(":").append(this.portNumbers[i]);
        }
        url.append("/");
        if (this.databaseName != null) {
            url.append(URLCoder.encode(this.databaseName));
        }
        StringBuilder query = new StringBuilder(100);
        for (PGProperty property : PGProperty.values()) {
            if (!property.isPresent(this.properties)) continue;
            if (query.length() != 0) {
                query.append("&");
            }
            query.append(property.getName());
            query.append("=");
            String value = Nullness.castNonNull(property.get(this.properties));
            query.append(URLCoder.encode(value));
        }
        if (query.length() > 0) {
            url.append("?");
            url.append((CharSequence)query);
        }
        return url.toString();
    }

    public String getURL() {
        return this.getUrl();
    }

    public void setUrl(String url) {
        Properties p = Driver.parseURL(url, null);
        if (p == null) {
            throw new IllegalArgumentException("URL invalid " + url);
        }
        for (PGProperty property : PGProperty.values()) {
            if (this.properties.containsKey(property.getName())) continue;
            this.setProperty(property, property.get(p));
        }
    }

    public void setURL(String url) {
        this.setUrl(url);
    }

    public @Nullable String getAuthenticationPluginClassName() {
        return PGProperty.AUTHENTICATION_PLUGIN_CLASS_NAME.get(this.properties);
    }

    public void setAuthenticationPluginClassName(String className) {
        PGProperty.AUTHENTICATION_PLUGIN_CLASS_NAME.set(this.properties, className);
    }

    public @Nullable String getProperty(String name) throws SQLException {
        PGProperty pgProperty = PGProperty.forName(name);
        if (pgProperty != null) {
            return this.getProperty(pgProperty);
        }
        throw new PSQLException(GT.tr("Unsupported property name: {0}", name), PSQLState.INVALID_PARAMETER_VALUE);
    }

    public void setProperty(String name, @Nullable String value) throws SQLException {
        PGProperty pgProperty = PGProperty.forName(name);
        if (pgProperty == null) {
            throw new PSQLException(GT.tr("Unsupported property name: {0}", name), PSQLState.INVALID_PARAMETER_VALUE);
        }
        this.setProperty(pgProperty, value);
    }

    public @Nullable String getProperty(PGProperty property) {
        return property.get(this.properties);
    }

    public void setProperty(PGProperty property, @Nullable String value) {
        if (value == null) {
            return;
        }
        switch (property) {
            case PG_HOST: {
                this.setServerNames(value.split(","));
                break;
            }
            case PG_PORT: {
                String[] ps = value.split(",");
                int[] ports = new int[ps.length];
                for (int i = 0; i < ps.length; ++i) {
                    try {
                        ports[i] = Integer.parseInt(ps[i]);
                        continue;
                    }
                    catch (NumberFormatException e) {
                        ports[i] = 0;
                    }
                }
                this.setPortNumbers(ports);
                break;
            }
            case PG_DBNAME: {
                this.setDatabaseName(value);
                break;
            }
            case USER: {
                this.setUser(value);
                break;
            }
            case PASSWORD: {
                this.setPassword(value);
                break;
            }
            default: {
                this.properties.setProperty(property.getName(), value);
            }
        }
    }

    protected Reference createReference() {
        return new Reference(this.getClass().getName(), PGObjectFactory.class.getName(), null);
    }

    @Override
    public Reference getReference() throws NamingException {
        Reference ref = this.createReference();
        StringBuilder serverString = new StringBuilder();
        for (int i = 0; i < this.serverNames.length; ++i) {
            if (i > 0) {
                serverString.append(",");
            }
            String serverName = this.serverNames[i];
            serverString.append(serverName);
        }
        ref.add(new StringRefAddr("serverName", serverString.toString()));
        StringBuilder portString = new StringBuilder();
        for (int i = 0; i < this.portNumbers.length; ++i) {
            if (i > 0) {
                portString.append(",");
            }
            int p = this.portNumbers[i];
            portString.append(Integer.toString(p));
        }
        ref.add(new StringRefAddr("portNumber", portString.toString()));
        ref.add(new StringRefAddr("databaseName", this.databaseName));
        if (this.user != null) {
            ref.add(new StringRefAddr("user", this.user));
        }
        if (this.password != null) {
            ref.add(new StringRefAddr("password", this.password));
        }
        for (PGProperty property : PGProperty.values()) {
            if (!property.isPresent(this.properties)) continue;
            String value = Nullness.castNonNull(property.get(this.properties));
            ref.add(new StringRefAddr(property.getName(), value));
        }
        return ref;
    }

    public void setFromReference(Reference ref) {
        this.databaseName = BaseDataSource.getReferenceProperty(ref, "databaseName");
        String portNumberString = BaseDataSource.getReferenceProperty(ref, "portNumber");
        if (portNumberString != null) {
            String[] ps = portNumberString.split(",");
            int[] ports = new int[ps.length];
            for (int i = 0; i < ps.length; ++i) {
                try {
                    ports[i] = Integer.parseInt(ps[i]);
                    continue;
                }
                catch (NumberFormatException e) {
                    ports[i] = 0;
                }
            }
            this.setPortNumbers(ports);
        } else {
            this.setPortNumbers(null);
        }
        String serverName = Nullness.castNonNull(BaseDataSource.getReferenceProperty(ref, "serverName"));
        this.setServerNames(serverName.split(","));
        for (PGProperty property : PGProperty.values()) {
            this.setProperty(property, BaseDataSource.getReferenceProperty(ref, property.getName()));
        }
    }

    private static @Nullable String getReferenceProperty(Reference ref, String propertyName) {
        RefAddr addr = ref.get(propertyName);
        if (addr == null) {
            return null;
        }
        return (String)addr.getContent();
    }

    protected void writeBaseObject(ObjectOutputStream out) throws IOException {
        out.writeObject(this.serverNames);
        out.writeObject(this.databaseName);
        out.writeObject(this.user);
        out.writeObject(this.password);
        out.writeObject(this.portNumbers);
        out.writeObject(this.properties);
    }

    protected void readBaseObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.serverNames = (String[])in.readObject();
        this.databaseName = (String)in.readObject();
        this.user = (String)in.readObject();
        this.password = (String)in.readObject();
        this.portNumbers = (int[])in.readObject();
        this.properties = (Properties)in.readObject();
    }

    public void initializeFrom(BaseDataSource source) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        source.writeBaseObject(oos);
        oos.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        this.readBaseObject(ois);
    }

    public PreferQueryMode getPreferQueryMode() {
        return PreferQueryMode.of(Nullness.castNonNull(PGProperty.PREFER_QUERY_MODE.get(this.properties)));
    }

    public void setPreferQueryMode(PreferQueryMode preferQueryMode) {
        PGProperty.PREFER_QUERY_MODE.set(this.properties, preferQueryMode.value());
    }

    public AutoSave getAutosave() {
        return AutoSave.of(Nullness.castNonNull(PGProperty.AUTOSAVE.get(this.properties)));
    }

    public void setAutosave(AutoSave autoSave) {
        PGProperty.AUTOSAVE.set(this.properties, autoSave.value());
    }

    public boolean getCleanupSavepoints() {
        return PGProperty.CLEANUP_SAVEPOINTS.getBoolean(this.properties);
    }

    public void setCleanupSavepoints(boolean cleanupSavepoints) {
        PGProperty.CLEANUP_SAVEPOINTS.set(this.properties, cleanupSavepoints);
    }

    public boolean getReWriteBatchedInserts() {
        return PGProperty.REWRITE_BATCHED_INSERTS.getBoolean(this.properties);
    }

    public void setReWriteBatchedInserts(boolean reWrite) {
        PGProperty.REWRITE_BATCHED_INSERTS.set(this.properties, reWrite);
    }

    public boolean getHideUnprivilegedObjects() {
        return PGProperty.HIDE_UNPRIVILEGED_OBJECTS.getBoolean(this.properties);
    }

    public void setHideUnprivilegedObjects(boolean hideUnprivileged) {
        PGProperty.HIDE_UNPRIVILEGED_OBJECTS.set(this.properties, hideUnprivileged);
    }

    public @Nullable String getMaxResultBuffer() {
        return PGProperty.MAX_RESULT_BUFFER.get(this.properties);
    }

    public void setMaxResultBuffer(@Nullable String maxResultBuffer) {
        PGProperty.MAX_RESULT_BUFFER.set(this.properties, maxResultBuffer);
    }

    public boolean getAdaptiveFetch() {
        return PGProperty.ADAPTIVE_FETCH.getBoolean(this.properties);
    }

    public void setAdaptiveFetch(boolean adaptiveFetch) {
        PGProperty.ADAPTIVE_FETCH.set(this.properties, adaptiveFetch);
    }

    public int getAdaptiveFetchMaximum() {
        return PGProperty.ADAPTIVE_FETCH_MAXIMUM.getIntNoCheck(this.properties);
    }

    public void setAdaptiveFetchMaximum(int adaptiveFetchMaximum) {
        PGProperty.ADAPTIVE_FETCH_MAXIMUM.set(this.properties, adaptiveFetchMaximum);
    }

    public int getAdaptiveFetchMinimum() {
        return PGProperty.ADAPTIVE_FETCH_MINIMUM.getIntNoCheck(this.properties);
    }

    public void setAdaptiveFetchMinimum(int adaptiveFetchMinimum) {
        PGProperty.ADAPTIVE_FETCH_MINIMUM.set(this.properties, adaptiveFetchMinimum);
    }

    @Override
    public Logger getParentLogger() {
        return Logger.getLogger("org.postgresql");
    }

    public String getXmlFactoryFactory() {
        return Nullness.castNonNull(PGProperty.XML_FACTORY_FACTORY.get(this.properties));
    }

    public void setXmlFactoryFactory(@Nullable String xmlFactoryFactory) {
        PGProperty.XML_FACTORY_FACTORY.set(this.properties, xmlFactoryFactory);
    }

    public boolean isSsl() {
        return this.getSsl();
    }

    public @Nullable String getSslfactoryarg() {
        return this.getSslFactoryArg();
    }

    public void setSslfactoryarg(@Nullable String arg) {
        this.setSslFactoryArg(arg);
    }

    public @Nullable String getSslcert() {
        return this.getSslCert();
    }

    public void setSslcert(@Nullable String file) {
        this.setSslCert(file);
    }

    public @Nullable String getSslmode() {
        return this.getSslMode();
    }

    public void setSslmode(@Nullable String mode) {
        this.setSslMode(mode);
    }

    public @Nullable String getSslhostnameverifier() {
        return this.getSslHostnameVerifier();
    }

    public void setSslhostnameverifier(@Nullable String className) {
        this.setSslHostnameVerifier(className);
    }

    public @Nullable String getSslkey() {
        return this.getSslKey();
    }

    public void setSslkey(@Nullable String file) {
        this.setSslKey(file);
    }

    public @Nullable String getSslrootcert() {
        return this.getSslRootCert();
    }

    public void setSslrootcert(@Nullable String file) {
        this.setSslRootCert(file);
    }

    public @Nullable String getSslpasswordcallback() {
        return this.getSslPasswordCallback();
    }

    public void setSslpasswordcallback(@Nullable String className) {
        this.setSslPasswordCallback(className);
    }

    public @Nullable String getSslpassword() {
        return this.getSslPassword();
    }

    public void setSslpassword(String sslpassword) {
        this.setSslPassword(sslpassword);
    }

    public int getRecvBufferSize() {
        return this.getReceiveBufferSize();
    }

    public void setRecvBufferSize(int nbytes) {
        this.setReceiveBufferSize(nbytes);
    }

    public boolean isAllowEncodingChanges() {
        return this.getAllowEncodingChanges();
    }

    public boolean isLogUnclosedConnections() {
        return this.getLogUnclosedConnections();
    }

    public boolean isTcpKeepAlive() {
        return this.getTcpKeepAlive();
    }

    public boolean isReadOnly() {
        return this.getReadOnly();
    }

    public boolean isDisableColumnSanitiser() {
        return this.getDisableColumnSanitiser();
    }

    public boolean isLoadBalanceHosts() {
        return this.getLoadBalanceHosts();
    }

    public boolean isCleanupSavePoints() {
        return this.getCleanupSavepoints();
    }

    public void setCleanupSavePoints(boolean cleanupSavepoints) {
        this.setCleanupSavepoints(cleanupSavepoints);
    }

    public boolean isReWriteBatchedInserts() {
        return this.getReWriteBatchedInserts();
    }

    static {
        try {
            Class.forName("org.postgresql.Driver");
        }
        catch (ClassNotFoundException e) {
            throw new IllegalStateException("BaseDataSource is unable to load org.postgresql.Driver. Please check if you have proper PostgreSQL JDBC Driver jar on the classpath", e);
        }
    }
}

