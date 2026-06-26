/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.osgi.framework.BundleActivator
 *  org.osgi.framework.BundleContext
 *  org.osgi.framework.ServiceRegistration
 *  org.osgi.service.jdbc.DataSourceFactory
 */
package org.postgresql.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.jdbc.DataSourceFactory;
import org.postgresql.Driver;
import org.postgresql.osgi.PGDataSourceFactory;

public class PGBundleActivator
implements BundleActivator {
    private @Nullable ServiceRegistration<?> registration;

    public void start(BundleContext context) throws Exception {
        if (!Driver.isRegistered()) {
            Driver.register();
        }
        if (PGBundleActivator.dataSourceFactoryExists()) {
            this.registerDataSourceFactory(context);
        }
    }

    private static boolean dataSourceFactoryExists() {
        try {
            Class.forName("org.osgi.service.jdbc.DataSourceFactory");
            return true;
        }
        catch (ClassNotFoundException classNotFoundException) {
            return false;
        }
    }

    private void registerDataSourceFactory(BundleContext context) {
        Hashtable<String, String> properties = new Hashtable<String, String>();
        ((Dictionary)properties).put("osgi.jdbc.driver.class", Driver.class.getName());
        ((Dictionary)properties).put("osgi.jdbc.driver.name", "PostgreSQL JDBC Driver");
        ((Dictionary)properties).put("osgi.jdbc.driver.version", "42.4.3");
        this.registration = context.registerService(DataSourceFactory.class, (Object)new PGDataSourceFactory(), properties);
    }

    public void stop(BundleContext context) throws Exception {
        if (this.registration != null) {
            this.registration.unregister();
            this.registration = null;
        }
        if (Driver.isRegistered()) {
            Driver.deregister();
        }
    }
}

