/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Serializable
 */
package org.greenplum.spark;

import scala.Serializable;

public final class GpfdistConf$
implements Serializable {
    public static GpfdistConf$ MODULE$;
    private final String KEY_PREFIX;
    private final String LISTEN_PORT_KEY;
    private final int org$greenplum$spark$GpfdistConf$$LISTEN_PORT_DEFAULT_VALUE;
    private final String org$greenplum$spark$GpfdistConf$$LOCATION_PORT_KEY;
    private final String org$greenplum$spark$GpfdistConf$$HOST_KEY;
    private final String org$greenplum$spark$GpfdistConf$$IS_SSL_KEY;
    private final boolean org$greenplum$spark$GpfdistConf$$IS_SSL_DEFAULT_VALUE;

    static {
        new GpfdistConf$();
    }

    private String KEY_PREFIX() {
        return this.KEY_PREFIX;
    }

    public String LISTEN_PORT_KEY() {
        return this.LISTEN_PORT_KEY;
    }

    public int org$greenplum$spark$GpfdistConf$$LISTEN_PORT_DEFAULT_VALUE() {
        return this.org$greenplum$spark$GpfdistConf$$LISTEN_PORT_DEFAULT_VALUE;
    }

    public String org$greenplum$spark$GpfdistConf$$LOCATION_PORT_KEY() {
        return this.org$greenplum$spark$GpfdistConf$$LOCATION_PORT_KEY;
    }

    public String org$greenplum$spark$GpfdistConf$$HOST_KEY() {
        return this.org$greenplum$spark$GpfdistConf$$HOST_KEY;
    }

    public String org$greenplum$spark$GpfdistConf$$IS_SSL_KEY() {
        return this.org$greenplum$spark$GpfdistConf$$IS_SSL_KEY;
    }

    public boolean org$greenplum$spark$GpfdistConf$$IS_SSL_DEFAULT_VALUE() {
        return this.org$greenplum$spark$GpfdistConf$$IS_SSL_DEFAULT_VALUE;
    }

    private Object readResolve() {
        return MODULE$;
    }

    private GpfdistConf$() {
        MODULE$ = this;
        this.KEY_PREFIX = "spark.greenplum.gpfdist";
        this.LISTEN_PORT_KEY = new StringBuilder(12).append(this.KEY_PREFIX()).append(".listen-port").toString();
        this.org$greenplum$spark$GpfdistConf$$LISTEN_PORT_DEFAULT_VALUE = 0;
        this.org$greenplum$spark$GpfdistConf$$LOCATION_PORT_KEY = new StringBuilder(14).append(this.KEY_PREFIX()).append(".location-port").toString();
        this.org$greenplum$spark$GpfdistConf$$HOST_KEY = new StringBuilder(5).append(this.KEY_PREFIX()).append(".host").toString();
        this.org$greenplum$spark$GpfdistConf$$IS_SSL_KEY = new StringBuilder(7).append(this.KEY_PREFIX()).append(".is-ssl").toString();
        this.org$greenplum$spark$GpfdistConf$$IS_SSL_DEFAULT_VALUE = false;
    }
}

