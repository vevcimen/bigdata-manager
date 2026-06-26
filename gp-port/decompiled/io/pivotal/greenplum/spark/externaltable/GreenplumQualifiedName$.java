/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Serializable
 */
package io.pivotal.greenplum.spark.externaltable;

import io.pivotal.greenplum.spark.externaltable.GreenplumQualifiedName;
import scala.Serializable;

public final class GreenplumQualifiedName$
implements Serializable {
    public static GreenplumQualifiedName$ MODULE$;

    static {
        new GreenplumQualifiedName$();
    }

    public GreenplumQualifiedName.Table forTable(String schema, String name) {
        this.validateSchema(schema);
        this.validateName(name);
        return new GreenplumQualifiedName.Table(schema, name);
    }

    public GreenplumQualifiedName.TempTable forTempTable(String name) {
        this.validateName(name);
        return new GreenplumQualifiedName.TempTable(name);
    }

    private void validateSchema(String schema) {
        if (schema == null) {
            throw new NullPointerException("schema passed to GreenplumQualifiedName was null");
        }
        if (schema.isEmpty()) {
            throw new IllegalArgumentException("schema passed to GreenplumQualifiedName was empty");
        }
    }

    private void validateName(String name) {
        if (name == null) {
            throw new NullPointerException("name passed to GreenplumQualifiedName was null");
        }
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name passed to GreenplumQualifiedName was empty");
        }
    }

    private Object readResolve() {
        return MODULE$;
    }

    private GreenplumQualifiedName$() {
        MODULE$ = this;
    }
}

