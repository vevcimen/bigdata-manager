/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Predef$
 *  scala.collection.immutable.StringOps
 */
package io.pivotal.greenplum.spark.externaltable;

import scala.Predef$;
import scala.collection.immutable.StringOps;

public final class SqlObjectNameUtils$ {
    public static SqlObjectNameUtils$ MODULE$;

    static {
        new SqlObjectNameUtils$();
    }

    public String escape(String s2) {
        return new StringBuilder(2).append("\"").append(new StringOps(Predef$.MODULE$.augmentString(s2)).replaceAllLiterally("\"", "\"\"")).append("\"").toString();
    }

    private SqlObjectNameUtils$() {
        MODULE$ = this;
    }
}

