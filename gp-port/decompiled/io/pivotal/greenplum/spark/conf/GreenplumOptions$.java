/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Serializable
 */
package io.pivotal.greenplum.spark.conf;

import scala.Serializable;

public final class GreenplumOptions$
implements Serializable {
    public static GreenplumOptions$ MODULE$;
    private final String GPDB_URL;
    private final String GPDB_USER;
    private final String GPDB_PASSWORD;
    private final String GPDB_SCHEMA_NAME;
    private final String GPDB_TABLE_NAME;
    private final String GPDB_PARTITION_COLUMN;
    private final String GPDB_PARTITIONS;
    private final String GPDB_DRIVER;
    private final String GPDB_DISTRIBUTED_BY;
    private final String GPDB_TRUNCATE_TABLE;
    private final String GPDB_ITERATOR_OPTIMIZATION;
    private final String DEFAULT_PARTITION_COLUMN_NAME;

    static {
        new GreenplumOptions$();
    }

    public String GPDB_URL() {
        return this.GPDB_URL;
    }

    public String GPDB_USER() {
        return this.GPDB_USER;
    }

    public String GPDB_PASSWORD() {
        return this.GPDB_PASSWORD;
    }

    public String GPDB_SCHEMA_NAME() {
        return this.GPDB_SCHEMA_NAME;
    }

    public String GPDB_TABLE_NAME() {
        return this.GPDB_TABLE_NAME;
    }

    public String GPDB_PARTITION_COLUMN() {
        return this.GPDB_PARTITION_COLUMN;
    }

    public String GPDB_PARTITIONS() {
        return this.GPDB_PARTITIONS;
    }

    public String GPDB_DRIVER() {
        return this.GPDB_DRIVER;
    }

    public String GPDB_DISTRIBUTED_BY() {
        return this.GPDB_DISTRIBUTED_BY;
    }

    public String GPDB_TRUNCATE_TABLE() {
        return this.GPDB_TRUNCATE_TABLE;
    }

    public String GPDB_ITERATOR_OPTIMIZATION() {
        return this.GPDB_ITERATOR_OPTIMIZATION;
    }

    public String DEFAULT_PARTITION_COLUMN_NAME() {
        return this.DEFAULT_PARTITION_COLUMN_NAME;
    }

    private Object readResolve() {
        return MODULE$;
    }

    private GreenplumOptions$() {
        MODULE$ = this;
        this.GPDB_URL = "url";
        this.GPDB_USER = "user";
        this.GPDB_PASSWORD = "password";
        this.GPDB_SCHEMA_NAME = "dbschema";
        this.GPDB_TABLE_NAME = "dbtable";
        this.GPDB_PARTITION_COLUMN = "partitionColumn";
        this.GPDB_PARTITIONS = "partitions";
        this.GPDB_DRIVER = "driver";
        this.GPDB_DISTRIBUTED_BY = "distributedBy";
        this.GPDB_TRUNCATE_TABLE = "truncate";
        this.GPDB_ITERATOR_OPTIMIZATION = "iteratorOptimization";
        this.DEFAULT_PARTITION_COLUMN_NAME = "gp_segment_id";
    }
}

