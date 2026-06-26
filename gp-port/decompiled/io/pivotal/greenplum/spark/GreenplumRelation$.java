/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.spark.sql.SQLContext
 *  org.apache.spark.sql.types.StructType
 *  scala.None$
 *  scala.Option
 *  scala.Serializable
 *  scala.Some
 *  scala.Tuple3
 */
package io.pivotal.greenplum.spark;

import io.pivotal.greenplum.spark.GreenplumPartition;
import io.pivotal.greenplum.spark.GreenplumRelation;
import io.pivotal.greenplum.spark.conf.GreenplumOptions;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.types.StructType;
import scala.None$;
import scala.Option;
import scala.Serializable;
import scala.Some;
import scala.Tuple3;

public final class GreenplumRelation$
implements Serializable {
    public static GreenplumRelation$ MODULE$;

    static {
        new GreenplumRelation$();
    }

    public final String toString() {
        return "GreenplumRelation";
    }

    public GreenplumRelation apply(StructType schema, GreenplumPartition[] parts, GreenplumOptions greenplumOptions, SQLContext sqlContext) {
        return new GreenplumRelation(schema, parts, greenplumOptions, sqlContext);
    }

    public Option<Tuple3<StructType, GreenplumPartition[], GreenplumOptions>> unapply(GreenplumRelation x$0) {
        if (x$0 == null) {
            return None$.MODULE$;
        }
        return new Some((Object)new Tuple3((Object)x$0.schema(), (Object)x$0.parts(), (Object)x$0.greenplumOptions()));
    }

    private Object readResolve() {
        return MODULE$;
    }

    private GreenplumRelation$() {
        MODULE$ = this;
    }
}

