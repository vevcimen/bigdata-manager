/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.osgi.service.jdbc.DataSourceFactory
 */
package org.postgresql.osgi;

import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.osgi.service.jdbc.DataSourceFactory;
import org.postgresql.Driver;
import org.postgresql.ds.common.BaseDataSource;
import org.postgresql.jdbc2.optional.ConnectionPool;
import org.postgresql.jdbc2.optional.PoolingDataSource;
import org.postgresql.jdbc2.optional.SimpleDataSource;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.internal.Nullness;
import org.postgresql.xa.PGXADataSource;

public class PGDataSourceFactory
implements DataSourceFactory {
    private void configureBaseDataSource(BaseDataSource ds, Properties props) throws SQLException {
        if (props.containsKey("url")) {
            ds.setUrl(Nullness.castNonNull(props.getProperty("url")));
        }
        if (props.containsKey("serverName")) {
            ds.setServerName(Nullness.castNonNull(props.getProperty("serverName")));
        }
        if (props.containsKey("portNumber")) {
            ds.setPortNumber(Integer.parseInt(Nullness.castNonNull(props.getProperty("portNumber"))));
        }
        if (props.containsKey("databaseName")) {
            ds.setDatabaseName(props.getProperty("databaseName"));
        }
        if (props.containsKey("user")) {
            ds.setUser(props.getProperty("user"));
        }
        if (props.containsKey("password")) {
            ds.setPassword(props.getProperty("password"));
        }
        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            ds.setProperty((String)entry.getKey(), (String)entry.getValue());
        }
    }

    public java.sql.Driver createDriver(Properties props) throws SQLException {
        if (props != null && !props.isEmpty()) {
            throw new PSQLException(GT.tr("Unsupported properties: {0}", props.stringPropertyNames()), PSQLState.INVALID_PARAMETER_VALUE);
        }
        return new Driver();
    }

    private DataSource createPoolingDataSource(Properties props) throws SQLException {
        PoolingDataSource dataSource = new PoolingDataSource();
        if (props.containsKey("initialPoolSize")) {
            String initialPoolSize = Nullness.castNonNull(props.getProperty("initialPoolSize"));
            dataSource.setInitialConnections(Integer.parseInt(initialPoolSize));
        }
        if (props.containsKey("maxPoolSize")) {
            String maxPoolSize = Nullness.castNonNull(props.getProperty("maxPoolSize"));
            dataSource.setMaxConnections(Integer.parseInt(maxPoolSize));
        }
        if (props.containsKey("dataSourceName")) {
            dataSource.setDataSourceName(Nullness.castNonNull(props.getProperty("dataSourceName")));
        }
        this.configureBaseDataSource(dataSource, props);
        return dataSource;
    }

    private DataSource createSimpleDataSource(Properties props) throws SQLException {
        SimpleDataSource dataSource = new SimpleDataSource();
        this.configureBaseDataSource(dataSource, props);
        return dataSource;
    }

    public DataSource createDataSource(Properties props) throws SQLException {
        if ((props = new SingleUseProperties(props)).containsKey("initialPoolSize") || props.containsKey("minPoolSize") || props.containsKey("maxPoolSize") || props.containsKey("maxIdleTime") || props.containsKey("maxStatements")) {
            return this.createPoolingDataSource(props);
        }
        return this.createSimpleDataSource(props);
    }

    public ConnectionPoolDataSource createConnectionPoolDataSource(Properties props) throws SQLException {
        props = new SingleUseProperties(props);
        ConnectionPool dataSource = new ConnectionPool();
        this.configureBaseDataSource(dataSource, props);
        return dataSource;
    }

    public XADataSource createXADataSource(Properties props) throws SQLException {
        props = new SingleUseProperties(props);
        PGXADataSource dataSource = new PGXADataSource();
        this.configureBaseDataSource(dataSource, props);
        return dataSource;
    }

    private static class SingleUseProperties
    extends Properties {
        private static final long serialVersionUID = 1L;

        SingleUseProperties(Properties initialProperties) {
            if (initialProperties != null) {
                this.putAll((Map<?, ?>)initialProperties);
            }
        }

        @Override
        public @Nullable String getProperty(String key) {
            String value = super.getProperty(key);
            this.remove(key);
            return value;
        }
    }
}

