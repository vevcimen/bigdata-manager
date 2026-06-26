/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.spark.sql.Row
 *  org.apache.spark.sql.Row$
 *  scala.Function1
 *  scala.Predef$
 *  scala.Serializable
 *  scala.collection.GenSeq
 *  scala.collection.Seq
 *  scala.collection.Seq$
 *  scala.collection.TraversableLike
 *  scala.collection.TraversableOnce
 *  scala.collection.immutable.IndexedSeq
 *  scala.collection.immutable.IndexedSeq$
 *  scala.runtime.BoxedUnit
 *  scala.runtime.BoxesRunTime
 *  scala.util.Failure
 *  scala.util.Success
 *  scala.util.Try
 */
package io.pivotal.greenplum.spark.externaltable;

import com.typesafe.scalalogging.LazyLogging;
import com.typesafe.scalalogging.Logger;
import java.io.Serializable;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.Row$;
import scala.Function1;
import scala.Predef$;
import scala.collection.GenSeq;
import scala.collection.Seq;
import scala.collection.Seq$;
import scala.collection.TraversableLike;
import scala.collection.TraversableOnce;
import scala.collection.immutable.IndexedSeq;
import scala.collection.immutable.IndexedSeq$;
import scala.runtime.BoxedUnit;
import scala.runtime.BoxesRunTime;
import scala.util.Failure;
import scala.util.Success;
import scala.util.Try;

public final class RowTransformer$
implements LazyLogging {
    public static RowTransformer$ MODULE$;
    private final Function1<Row, Row> identityFunction;
    private transient Logger logger;
    private volatile transient boolean bitmap$trans$0;

    static {
        new RowTransformer$();
    }

    private Logger logger$lzycompute() {
        RowTransformer$ rowTransformer$ = this;
        synchronized (rowTransformer$) {
            if (!this.bitmap$trans$0) {
                this.logger = LazyLogging.logger$(this);
                this.bitmap$trans$0 = true;
            }
        }
        return this.logger;
    }

    @Override
    public Logger logger() {
        if (!this.bitmap$trans$0) {
            return this.logger$lzycompute();
        }
        return this.logger;
    }

    public Function1<Row, Row> identityFunction() {
        return this.identityFunction;
    }

    public Try<Function1<Row, Row>> getFunction(Seq<String> sparkCols, Seq<String> gpdbCols) {
        Seq lowercaseGPDBCols;
        Seq lowercaseSparkCols = (Seq)sparkCols.map((Function1 & Serializable & scala.Serializable)x$1 -> x$1.toLowerCase(), Seq$.MODULE$.canBuildFrom());
        if (lowercaseSparkCols.equals((Object)(lowercaseGPDBCols = (Seq)gpdbCols.map((Function1 & Serializable & scala.Serializable)x$2 -> x$2.toLowerCase(), Seq$.MODULE$.canBuildFrom())))) {
            BoxedUnit boxedUnit;
            if (this.logger().underlying().isDebugEnabled()) {
                this.logger().underlying().debug("RowTransformer.getfunction returning identity function...");
                boxedUnit = BoxedUnit.UNIT;
            } else {
                boxedUnit = BoxedUnit.UNIT;
            }
            return new Success(this.identityFunction());
        }
        if (!lowercaseSparkCols.toSet().equals((Object)lowercaseGPDBCols.toSet())) {
            BoxedUnit boxedUnit;
            String missingColumns = this.formatColumnList((Seq<String>)((Seq)lowercaseGPDBCols.diff((GenSeq)lowercaseSparkCols)));
            String extraColumns = this.formatColumnList((Seq<String>)((Seq)lowercaseSparkCols.diff((GenSeq)lowercaseGPDBCols)));
            if (!extraColumns.isEmpty()) {
                if (this.logger().underlying().isWarnEnabled()) {
                    this.logger().underlying().warn(new StringBuilder(104).append("Spark dataframe contains extra column[s] ").append(extraColumns).append(" ").append("that will be ignored when writing to Greenplum Database table.").toString());
                    boxedUnit = BoxedUnit.UNIT;
                } else {
                    boxedUnit = BoxedUnit.UNIT;
                }
            } else {
                boxedUnit = BoxedUnit.UNIT;
            }
            if (!missingColumns.isEmpty()) {
                return new Failure((Throwable)new RuntimeException(new StringBuilder(81).append("Spark DataFrame must include column[s] ").append(missingColumns).append(" ").append("when writing to Greenplum Database table.").toString()));
            }
        }
        Seq sparkColIndexFromGpdbColIndex = (Seq)lowercaseGPDBCols.map((Function1 & Serializable & scala.Serializable)elem -> BoxesRunTime.boxToInteger((int)lowercaseSparkCols.indexOf(elem)), Seq$.MODULE$.canBuildFrom());
        Function1 & Serializable & scala.Serializable reorderColumns = (Function1 & Serializable & scala.Serializable)sparkRow -> {
            IndexedSeq reorderedValues = (IndexedSeq)((TraversableLike)lowercaseGPDBCols.indices().map((Function1)sparkColIndexFromGpdbColIndex, IndexedSeq$.MODULE$.canBuildFrom())).map((Function1 & Serializable & scala.Serializable)i -> sparkRow.get(BoxesRunTime.unboxToInt((Object)i)), IndexedSeq$.MODULE$.canBuildFrom());
            return Row$.MODULE$.fromSeq((Seq)reorderedValues);
        };
        return new Success((Object)reorderColumns);
    }

    private String formatColumnList(Seq<String> columns) {
        return ((TraversableOnce)columns.map((Function1 & Serializable & scala.Serializable)s2 -> new StringBuilder(2).append("\"").append((String)s2).append("\"").toString(), Seq$.MODULE$.canBuildFrom())).mkString(", ");
    }

    private RowTransformer$() {
        MODULE$ = this;
        LazyLogging.$init$(this);
        this.identityFunction = (Function1 & Serializable & scala.Serializable)x -> (Row)Predef$.MODULE$.identity(x);
    }
}

