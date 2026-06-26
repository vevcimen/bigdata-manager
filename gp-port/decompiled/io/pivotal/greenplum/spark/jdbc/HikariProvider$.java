/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.spark.sql.execution.datasources.jdbc.DriverRegistry$
 *  scala.MatchError
 *  scala.None$
 *  scala.Option
 *  scala.Some
 *  scala.runtime.BoxedUnit
 */
package io.pivotal.greenplum.spark.jdbc;

import com.typesafe.scalalogging.LazyLogging;
import com.typesafe.scalalogging.Logger;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.pivotal.greenplum.spark.conf.ConnectionPoolOptions;
import io.pivotal.greenplum.spark.jdbc.ConnectionKey;
import io.pivotal.greenplum.spark.jdbc.DataSourceProvider;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.spark.sql.execution.datasources.jdbc.DriverRegistry$;
import scala.MatchError;
import scala.None$;
import scala.Option;
import scala.Some;
import scala.runtime.BoxedUnit;

public final class HikariProvider$
implements DataSourceProvider,
LazyLogging {
    public static HikariProvider$ MODULE$;
    private final String PROPERTY_PREFIX;
    private transient Logger logger;
    private volatile transient boolean bitmap$trans$0;

    static {
        new HikariProvider$();
    }

    private Logger logger$lzycompute() {
        HikariProvider$ hikariProvider$ = this;
        synchronized (hikariProvider$) {
            if (!this.bitmap$trans$0) {
                this.logger = LazyLogging.logger$(this);
                this.bitmap$trans$0 = true;
            }
        }
        return this.logger;
    }

    @Override
    public Logger logger() {
        if (!this.bitmap$trans$0) {
            return this.logger$lzycompute();
        }
        return this.logger;
    }

    private String PROPERTY_PREFIX() {
        return this.PROPERTY_PREFIX;
    }

    public HikariConfig getHikariConfig(ConnectionPoolOptions connectionPoolOptions) {
        HikariConfig config;
        block2: {
            Properties properties = connectionPoolOptions.getPropertiesWithSubPrefix(this.PROPERTY_PREFIX());
            config = new HikariConfig(properties);
            if (!properties.containsKey("maximumPoolSize")) {
                config.setMaximumPoolSize(connectionPoolOptions.maximumPoolSize());
            }
            if (!properties.containsKey("idleTimeout")) {
                config.setIdleTimeout(connectionPoolOptions.idleTimeoutMs());
            }
            if (properties.containsKey("minimumIdle")) break block2;
            config.setMinimumIdle(connectionPoolOptions.minimumIdle());
        }
        return config;
    }

    @Override
    public DataSource createDataSource(ConnectionKey key, Option<String> password, String driver, ConnectionPoolOptions options) {
        BoxedUnit boxedUnit;
        DriverRegistry$.MODULE$.register(driver);
        HikariConfig hikariConfig = this.getHikariConfig(options);
        hikariConfig.setJdbcUrl(key.jdbcUrl());
        hikariConfig.setUsername(key.userName());
        Option<String> option = password;
        if (option instanceof Some) {
            Some some = (Some)option;
            String pass = (String)some.value();
            hikariConfig.setPassword(pass);
        } else if (None$.MODULE$.equals(option)) {
            if (this.logger().underlying().isDebugEnabled()) {
                this.logger().underlying().debug("No password is used");
            }
        } else {
            throw new MatchError(option);
        }
        if (this.logger().underlying().isDebugEnabled()) {
            this.logger().underlying().debug(new StringBuilder(89).append("Creating connection pool with ").append(options.maximumPoolSize()).append(" as a max number of ").append("connections for a jdbc url: ").append(key.jdbcUrl()).append(" and user: ").append(key.userName()).toString());
            boxedUnit = BoxedUnit.UNIT;
        } else {
            boxedUnit = BoxedUnit.UNIT;
        }
        return new HikariDataSource(hikariConfig);
    }

    private HikariProvider$() {
        MODULE$ = this;
        LazyLogging.$init$(this);
        this.PROPERTY_PREFIX = "hikari";
    }
}

