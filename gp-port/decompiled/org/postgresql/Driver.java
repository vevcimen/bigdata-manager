/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.PGProperty;
import org.postgresql.jdbc.PgConnection;
import org.postgresql.util.GT;
import org.postgresql.util.HostSpec;
import org.postgresql.util.PGPropertyPasswordParser;
import org.postgresql.util.PGPropertyServiceParser;
import org.postgresql.util.PGPropertyUtil;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.SharedTimer;
import org.postgresql.util.URLCoder;
import org.postgresql.util.internal.Nullness;

public class Driver
implements java.sql.Driver {
    private static @Nullable Driver registeredDriver;
    private static final Logger PARENT_LOGGER;
    private static final Logger LOGGER;
    private static final SharedTimer SHARED_TIMER;
    private @Nullable Properties defaultProperties;

    private synchronized Properties getDefaultProperties() throws IOException {
        if (this.defaultProperties != null) {
            return this.defaultProperties;
        }
        try {
            this.defaultProperties = Driver.doPrivileged(new PrivilegedExceptionAction<Properties>(){

                @Override
                public Properties run() throws IOException {
                    return Driver.this.loadDefaultProperties();
                }
            });
        }
        catch (PrivilegedActionException e) {
            Exception ex = e.getException();
            if (ex instanceof IOException) {
                throw (IOException)ex;
            }
            throw new RuntimeException(e);
        }
        catch (Throwable e) {
            if (e instanceof IOException) {
                throw (IOException)e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            }
            if (e instanceof Error) {
                throw (Error)e;
            }
            throw new RuntimeException(e);
        }
        return this.defaultProperties;
    }

    private static <T> T doPrivileged(PrivilegedExceptionAction<T> action) throws Throwable {
        try {
            Class<?> accessControllerClass = Class.forName("java.security.AccessController");
            Method doPrivileged = accessControllerClass.getMethod("doPrivileged", PrivilegedExceptionAction.class);
            return (T)doPrivileged.invoke(null, action);
        }
        catch (ClassNotFoundException e) {
            return action.run();
        }
        catch (InvocationTargetException e) {
            throw Nullness.castNonNull(e.getCause());
        }
    }

    private Properties loadDefaultProperties() throws IOException {
        Properties merged = new Properties();
        try {
            PGProperty.USER.set(merged, System.getProperty("user.name"));
        }
        catch (SecurityException securityException) {
            // empty catch block
        }
        ClassLoader cl = this.getClass().getClassLoader();
        if (cl == null) {
            LOGGER.log(Level.FINE, "Can't find our classloader for the Driver; attempt to use the system class loader");
            cl = ClassLoader.getSystemClassLoader();
        }
        if (cl == null) {
            LOGGER.log(Level.WARNING, "Can't find a classloader for the Driver; not loading driver configuration from org/postgresql/driverconfig.properties");
            return merged;
        }
        LOGGER.log(Level.FINE, "Loading driver configuration via classloader {0}", cl);
        ArrayList<URL> urls = new ArrayList<URL>();
        Enumeration<URL> urlEnum = cl.getResources("org/postgresql/driverconfig.properties");
        while (urlEnum.hasMoreElements()) {
            urls.add(urlEnum.nextElement());
        }
        for (int i = urls.size() - 1; i >= 0; --i) {
            URL url = (URL)urls.get(i);
            LOGGER.log(Level.FINE, "Loading driver configuration from: {0}", url);
            InputStream is = url.openStream();
            merged.load(is);
            is.close();
        }
        return merged;
    }

    @Override
    public @Nullable Connection connect(String url, @Nullable Properties info) throws SQLException {
        Properties defaults;
        if (url == null) {
            throw new SQLException("url is null");
        }
        if (!url.startsWith("jdbc:postgresql:")) {
            return null;
        }
        try {
            defaults = this.getDefaultProperties();
        }
        catch (IOException ioe) {
            throw new PSQLException(GT.tr("Error loading default settings from driverconfig.properties", new Object[0]), PSQLState.UNEXPECTED_ERROR, (Throwable)ioe);
        }
        Properties props = new Properties(defaults);
        if (info != null) {
            Set<String> e = info.stringPropertyNames();
            for (String propName : e) {
                String propValue = info.getProperty(propName);
                if (propValue == null) {
                    throw new PSQLException(GT.tr("Properties for the driver contains a non-string value for the key ", new Object[0]) + propName, PSQLState.UNEXPECTED_ERROR);
                }
                props.setProperty(propName, propValue);
            }
        }
        if ((props = Driver.parseURL(url, props)) == null) {
            throw new PSQLException(GT.tr("Unable to parse URL {0}", url), PSQLState.UNEXPECTED_ERROR);
        }
        try {
            LOGGER.log(Level.FINE, "Connecting with URL: {0}", url);
            long timeout = Driver.timeout(props);
            if (timeout <= 0L) {
                return Driver.makeConnection(url, props);
            }
            ConnectThread ct = new ConnectThread(url, props);
            Thread thread = new Thread((Runnable)ct, "PostgreSQL JDBC driver connection thread");
            thread.setDaemon(true);
            thread.start();
            return ct.getResult(timeout);
        }
        catch (PSQLException ex1) {
            LOGGER.log(Level.FINE, "Connection error: ", ex1);
            throw ex1;
        }
        catch (Exception ex2) {
            if ("java.security.AccessControlException".equals(ex2.getClass().getName())) {
                throw new PSQLException(GT.tr("Your security policy has prevented the connection from being attempted.  You probably need to grant the connect java.net.SocketPermission to the database server host and port that you wish to connect to.", new Object[0]), PSQLState.UNEXPECTED_ERROR, (Throwable)ex2);
            }
            LOGGER.log(Level.FINE, "Unexpected connection error: ", ex2);
            throw new PSQLException(GT.tr("Something unusual has occurred to cause the driver to fail. Please report this exception.", new Object[0]), PSQLState.UNEXPECTED_ERROR, (Throwable)ex2);
        }
    }

    private void setupLoggerFromProperties(Properties props) {
    }

    private static Connection makeConnection(String url, Properties props) throws SQLException {
        return new PgConnection(Driver.hostSpecs(props), props, url);
    }

    @Override
    public boolean acceptsURL(String url) {
        return Driver.parseURL(url, null) != null;
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) {
        Properties copy = new Properties(info);
        Properties parse = Driver.parseURL(url, copy);
        if (parse != null) {
            copy = parse;
        }
        PGProperty[] knownProperties = PGProperty.values();
        DriverPropertyInfo[] props = new DriverPropertyInfo[knownProperties.length];
        for (int i = 0; i < props.length; ++i) {
            props[i] = knownProperties[i].toDriverPropertyInfo(copy);
        }
        return props;
    }

    @Override
    public int getMajorVersion() {
        return 42;
    }

    @Override
    public int getMinorVersion() {
        return 4;
    }

    @Deprecated
    public static String getVersion() {
        return "PostgreSQL JDBC Driver 42.4.3";
    }

    @Override
    public boolean jdbcCompliant() {
        return false;
    }

    public static @Nullable Properties parseURL(String url, @Nullable Properties defaults) {
        String password;
        Properties result;
        Properties priority1Url = new Properties();
        Properties priority3Service = new Properties();
        String urlServer = url;
        String urlArgs = "";
        int qPos = url.indexOf(63);
        if (qPos != -1) {
            urlServer = url.substring(0, qPos);
            urlArgs = url.substring(qPos + 1);
        }
        if (!urlServer.startsWith("jdbc:postgresql:")) {
            LOGGER.log(Level.FINE, "JDBC URL must start with \"jdbc:postgresql:\" but was: {0}", url);
            return null;
        }
        if ((urlServer = urlServer.substring("jdbc:postgresql:".length())).equals("//") || urlServer.equals("///")) {
            urlServer = "";
        } else if (urlServer.startsWith("//")) {
            long slashCount = (urlServer = urlServer.substring(2)).chars().filter(ch -> ch == 47).count();
            if (slashCount > 1L) {
                LOGGER.log(Level.WARNING, "JDBC URL contains too many / characters: {0}", url);
                return null;
            }
            int slash = urlServer.indexOf(47);
            if (slash == -1) {
                LOGGER.log(Level.WARNING, "JDBC URL must contain a / at the end of the host or port: {0}", url);
                return null;
            }
            if (!urlServer.endsWith("/")) {
                String value = Driver.urlDecode(urlServer.substring(slash + 1));
                if (value == null) {
                    return null;
                }
                PGProperty.PG_DBNAME.set(priority1Url, value);
            }
            urlServer = urlServer.substring(0, slash);
            String[] addresses = urlServer.split(",");
            StringBuilder hosts = new StringBuilder();
            StringBuilder ports = new StringBuilder();
            for (String address : addresses) {
                int portIdx = address.lastIndexOf(58);
                if (portIdx != -1 && address.lastIndexOf(93) < portIdx) {
                    String portStr = address.substring(portIdx + 1);
                    ports.append(portStr);
                    CharSequence hostStr = address.subSequence(0, portIdx);
                    if (hostStr.length() == 0) {
                        hosts.append(PGProperty.PG_HOST.getDefaultValue());
                    } else {
                        hosts.append(hostStr);
                    }
                } else {
                    ports.append(PGProperty.PG_PORT.getDefaultValue());
                    hosts.append(address);
                }
                ports.append(',');
                hosts.append(',');
            }
            ports.setLength(ports.length() - 1);
            hosts.setLength(hosts.length() - 1);
            PGProperty.PG_HOST.set(priority1Url, hosts.toString());
            PGProperty.PG_PORT.set(priority1Url, ports.toString());
        } else {
            if (urlServer.startsWith("/")) {
                return null;
            }
            String value = Driver.urlDecode(urlServer);
            if (value == null) {
                return null;
            }
            priority1Url.setProperty(PGProperty.PG_DBNAME.getName(), value);
        }
        String[] args = urlArgs.split("&");
        String serviceName = null;
        for (String token : args) {
            if (token.isEmpty()) continue;
            int pos = token.indexOf(61);
            if (pos == -1) {
                priority1Url.setProperty(token, "");
                continue;
            }
            String pName = PGPropertyUtil.translatePGServiceToPGProperty(token.substring(0, pos));
            String pValue = Driver.urlDecode(token.substring(pos + 1));
            if (pValue == null) {
                return null;
            }
            if (PGProperty.SERVICE.getName().equals(pName)) {
                serviceName = pValue;
                continue;
            }
            priority1Url.setProperty(pName, pValue);
        }
        if (serviceName != null) {
            LOGGER.log(Level.FINE, "Processing option [?service={0}]", serviceName);
            result = PGPropertyServiceParser.getServiceProperties(serviceName);
            if (result == null) {
                LOGGER.log(Level.WARNING, "Definition of service [{0}] not found", serviceName);
                return null;
            }
            priority3Service.putAll((Map<?, ?>)result);
        }
        result = new Properties();
        result.putAll((Map<?, ?>)priority1Url);
        if (defaults != null) {
            defaults.forEach((BiConsumer<? super Object, ? super Object>)((BiConsumer<Object, Object>)result::putIfAbsent));
        }
        priority3Service.forEach((BiConsumer<? super Object, ? super Object>)((BiConsumer<Object, Object>)result::putIfAbsent));
        if (defaults != null) {
            defaults.stringPropertyNames().forEach(s2 -> result.putIfAbsent(s2, Nullness.castNonNull(defaults.getProperty((String)s2))));
        }
        result.putIfAbsent(PGProperty.PG_PORT.getName(), Nullness.castNonNull(PGProperty.PG_PORT.getDefaultValue()));
        result.putIfAbsent(PGProperty.PG_HOST.getName(), Nullness.castNonNull(PGProperty.PG_HOST.getDefaultValue()));
        if (PGProperty.USER.get(result) != null) {
            result.putIfAbsent(PGProperty.PG_DBNAME.getName(), Nullness.castNonNull(PGProperty.USER.get(result)));
        }
        if (!PGPropertyUtil.propertiesConsistencyCheck(result)) {
            return null;
        }
        if (PGProperty.PASSWORD.get(result) == null && (password = PGPropertyPasswordParser.getPassword(PGProperty.PG_HOST.get(result), PGProperty.PG_PORT.get(result), PGProperty.PG_DBNAME.get(result), PGProperty.USER.get(result))) != null && !password.isEmpty()) {
            PGProperty.PASSWORD.set(result, password);
        }
        return result;
    }

    private static @Nullable String urlDecode(String url) {
        try {
            return URLCoder.decode(url);
        }
        catch (IllegalArgumentException e) {
            LOGGER.log(Level.FINE, "Url [{0}] parsing failed with error [{1}]", new Object[]{url, e.getMessage()});
            return null;
        }
    }

    private static HostSpec[] hostSpecs(Properties props) {
        String[] hosts = Nullness.castNonNull(PGProperty.PG_HOST.get(props)).split(",");
        String[] ports = Nullness.castNonNull(PGProperty.PG_PORT.get(props)).split(",");
        String localSocketAddress = PGProperty.LOCAL_SOCKET_ADDRESS.get(props);
        HostSpec[] hostSpecs = new HostSpec[hosts.length];
        for (int i = 0; i < hostSpecs.length; ++i) {
            hostSpecs[i] = new HostSpec(hosts[i], Integer.parseInt(ports[i]), localSocketAddress);
        }
        return hostSpecs;
    }

    private static long timeout(Properties props) {
        String timeout = PGProperty.LOGIN_TIMEOUT.get(props);
        if (timeout != null) {
            try {
                return (long)(Float.parseFloat(timeout) * 1000.0f);
            }
            catch (NumberFormatException e) {
                LOGGER.log(Level.WARNING, "Couldn't parse loginTimeout value: {0}", timeout);
            }
        }
        return (long)DriverManager.getLoginTimeout() * 1000L;
    }

    public static SQLFeatureNotSupportedException notImplemented(Class<?> callClass, String functionName) {
        return new SQLFeatureNotSupportedException(GT.tr("Method {0} is not yet implemented.", callClass.getName() + "." + functionName), PSQLState.NOT_IMPLEMENTED.getState());
    }

    @Override
    public Logger getParentLogger() {
        return PARENT_LOGGER;
    }

    public static SharedTimer getSharedTimer() {
        return SHARED_TIMER;
    }

    public static void register() throws SQLException {
        if (Driver.isRegistered()) {
            throw new IllegalStateException("Driver is already registered. It can only be registered once.");
        }
        Driver registeredDriver = new Driver();
        DriverManager.registerDriver(registeredDriver);
        Driver.registeredDriver = registeredDriver;
    }

    public static void deregister() throws SQLException {
        if (registeredDriver == null) {
            throw new IllegalStateException("Driver is not registered (or it has not been registered using Driver.register() method)");
        }
        DriverManager.deregisterDriver(registeredDriver);
        registeredDriver = null;
    }

    public static boolean isRegistered() {
        return registeredDriver != null;
    }

    static {
        PARENT_LOGGER = Logger.getLogger("org.postgresql");
        LOGGER = Logger.getLogger("org.postgresql.Driver");
        SHARED_TIMER = new SharedTimer();
        try {
            Driver.register();
        }
        catch (SQLException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static class ConnectThread
    implements Runnable {
        private final String url;
        private final Properties props;
        private @Nullable Connection result;
        private @Nullable Throwable resultException;
        private boolean abandoned;

        ConnectThread(String url, Properties props) {
            this.url = url;
            this.props = props;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void run() {
            Throwable error;
            Connection conn;
            try {
                conn = Driver.makeConnection(this.url, this.props);
                error = null;
            }
            catch (Throwable t) {
                conn = null;
                error = t;
            }
            ConnectThread connectThread = this;
            synchronized (connectThread) {
                if (this.abandoned) {
                    if (conn != null) {
                        try {
                            conn.close();
                        }
                        catch (SQLException sQLException) {}
                    }
                } else {
                    this.result = conn;
                    this.resultException = error;
                    this.notify();
                }
            }
        }

        /*
         * Enabled aggressive block sorting
         * Enabled unnecessary exception pruning
         * Enabled aggressive exception aggregation
         */
        public Connection getResult(long timeout) throws SQLException {
            long expiry = TimeUnit.NANOSECONDS.toMillis(System.nanoTime()) + timeout;
            ConnectThread connectThread = this;
            synchronized (connectThread) {
                while (this.result == null) {
                    if (this.resultException != null) {
                        if (this.resultException instanceof SQLException) {
                            this.resultException.fillInStackTrace();
                            throw (SQLException)this.resultException;
                        }
                        throw new PSQLException(GT.tr("Something unusual has occurred to cause the driver to fail. Please report this exception.", new Object[0]), PSQLState.UNEXPECTED_ERROR, this.resultException);
                    }
                    long delay = expiry - TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
                    if (delay <= 0L) {
                        this.abandoned = true;
                        throw new PSQLException(GT.tr("Connection attempt timed out.", new Object[0]), PSQLState.CONNECTION_UNABLE_TO_CONNECT);
                    }
                    try {
                        this.wait(delay);
                    }
                    catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        this.abandoned = true;
                        throw new RuntimeException(GT.tr("Interrupted while attempting to connect.", new Object[0]));
                    }
                }
                return this.result;
            }
        }
    }
}

