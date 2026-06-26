/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Serializable
 */
package io.pivotal.greenplum.spark;

import io.pivotal.greenplum.spark.util.NetworkOperations;
import scala.Serializable;

public final class ConnectorUtils$
implements Serializable {
    public static ConnectorUtils$ MODULE$;

    static {
        new ConnectorUtils$();
    }

    public NetworkOperations $lessinit$greater$default$1() {
        return new NetworkOperations();
    }

    public String getLocationPathPrefix(String applicationId, String executorId) {
        return new StringBuilder(7).append("/").append(applicationId).append("/exec/").append(executorId).toString();
    }

    private Object readResolve() {
        return MODULE$;
    }

    private ConnectorUtils$() {
        MODULE$ = this;
    }
}

