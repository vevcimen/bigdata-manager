/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.spark.sql.sources.Filter
 *  scala.Array$
 *  scala.Function1
 *  scala.Option$
 *  scala.Predef$
 *  scala.Serializable
 *  scala.collection.mutable.ArrayOps$ofRef
 *  scala.reflect.ClassTag$
 */
package io.pivotal.greenplum.spark.externaltable;

import io.pivotal.greenplum.spark.ConnectorUtils;
import io.pivotal.greenplum.spark.ConnectorUtils$;
import io.pivotal.greenplum.spark.GreenplumRDD$;
import org.apache.spark.sql.sources.Filter;
import scala.Array$;
import scala.Function1;
import scala.Option$;
import scala.Predef$;
import scala.Serializable;
import scala.collection.mutable.ArrayOps;
import scala.reflect.ClassTag$;

public final class GreenplumRowIterator$ {
    public static GreenplumRowIterator$ MODULE$;

    static {
        new GreenplumRowIterator$();
    }

    public ConnectorUtils $lessinit$greater$default$7() {
        return new ConnectorUtils(ConnectorUtils$.MODULE$.$lessinit$greater$default$1());
    }

    public String filterWherePredicate(Filter[] filters) {
        return new ArrayOps.ofRef(Predef$.MODULE$.refArrayOps((Object[])new ArrayOps.ofRef(Predef$.MODULE$.refArrayOps((Object[])new ArrayOps.ofRef(Predef$.MODULE$.refArrayOps((Object[])filters)).flatMap((Function1 & java.io.Serializable & Serializable)x$1 -> Option$.MODULE$.option2Iterable(GreenplumRDD$.MODULE$.compileFilter((Filter)x$1)), Array$.MODULE$.canBuildFrom(ClassTag$.MODULE$.apply(String.class))))).map((Function1 & java.io.Serializable & Serializable)p -> new StringBuilder(2).append("(").append((String)p).append(")").toString(), Array$.MODULE$.canBuildFrom(ClassTag$.MODULE$.apply(String.class))))).mkString(" AND ");
    }

    private GreenplumRowIterator$() {
        MODULE$ = this;
    }
}

