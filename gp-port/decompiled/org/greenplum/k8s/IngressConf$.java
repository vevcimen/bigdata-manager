/*
 * Decompiled with CFR 0.152.
 */
package org.greenplum.k8s;

public final class IngressConf$ {
    public static IngressConf$ MODULE$;
    private final String org$greenplum$k8s$IngressConf$$KEY_PREFIX;

    static {
        new IngressConf$();
    }

    public String org$greenplum$k8s$IngressConf$$KEY_PREFIX() {
        return this.org$greenplum$k8s$IngressConf$$KEY_PREFIX;
    }

    private IngressConf$() {
        MODULE$ = this;
        this.org$greenplum$k8s$IngressConf$$KEY_PREFIX = "spark.greenplum.k8s.ingress";
    }
}

