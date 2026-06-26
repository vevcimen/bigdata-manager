/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.spark.sql.DataFrameReader
 *  org.apache.spark.sql.types.DataType
 *  org.apache.spark.sql.types.NumericType
 *  org.apache.spark.sql.types.StructField
 *  scala.package$
 *  scala.runtime.BoxedUnit
 *  scala.util.Either
 */
package io.pivotal.greenplum.spark;

import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.NumericType;
import org.apache.spark.sql.types.StructField;
import scala.package$;
import scala.runtime.BoxedUnit;
import scala.util.Either;

public final class GreenplumRelationProvider$ {
    public static GreenplumRelationProvider$ MODULE$;

    static {
        new GreenplumRelationProvider$();
    }

    public <A> DataFrameReader apply(DataFrameReader t) {
        return t;
    }

    public DataFrameReader GreenplumDataFrameReader(DataFrameReader reader) {
        return reader;
    }

    public Either<BoxedUnit, IllegalArgumentException> checkPartitionColumnType(StructField column) {
        DataType dataType = column.dataType();
        if (dataType instanceof NumericType) {
            return package$.MODULE$.Left().apply((Object)BoxedUnit.UNIT);
        }
        String message = new StringBuilder(114).append("data type of '").append(column.name()).append("' is not supported for partitioning; ").append("supported data types are bigint, bigserial, integer, and serial").toString();
        return package$.MODULE$.Right().apply((Object)new IllegalArgumentException(message));
    }

    private GreenplumRelationProvider$() {
        MODULE$ = this;
    }
}

