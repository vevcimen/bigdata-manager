/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core.v3;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.core.v3.SimpleParameterList;
import org.postgresql.core.v3.SimpleQuery;

class DescribeRequest {
    public final SimpleQuery query;
    public final SimpleParameterList parameterList;
    public final boolean describeOnly;
    public final @Nullable String statementName;

    DescribeRequest(SimpleQuery query, SimpleParameterList parameterList, boolean describeOnly, @Nullable String statementName) {
        this.query = query;
        this.parameterList = parameterList;
        this.describeOnly = describeOnly;
        this.statementName = statementName;
    }
}

