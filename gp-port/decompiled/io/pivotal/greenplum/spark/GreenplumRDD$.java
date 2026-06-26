/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.spark.sql.execution.datasources.jdbc.JDBCRDD$
 *  org.apache.spark.sql.jdbc.JdbcDialect
 *  org.apache.spark.sql.jdbc.JdbcDialects$
 *  org.apache.spark.sql.sources.Filter
 *  scala.Option
 *  scala.Serializable
 */
package io.pivotal.greenplum.spark;

import org.apache.spark.sql.execution.datasources.jdbc.JDBCRDD$;
import org.apache.spark.sql.jdbc.JdbcDialect;
import org.apache.spark.sql.jdbc.JdbcDialects$;
import org.apache.spark.sql.sources.Filter;
import scala.Option;
import scala.Serializable;

public final class GreenplumRDD$
implements Serializable {
    public static GreenplumRDD$ MODULE$;

    static {
        new GreenplumRDD$();
    }

    public Option<String> compileFilter(Filter f, JdbcDialect dialect) {
        return JDBCRDD$.MODULE$.compileFilter(f, dialect);
    }

    public Option<String> compileFilter(Filter f) {
        return this.compileFilter(f, JdbcDialects$.MODULE$.get("jdbc:postgresql"));
    }

    private Object readResolve() {
        return MODULE$;
    }

    private GreenplumRDD$() {
        MODULE$ = this;
    }
}

