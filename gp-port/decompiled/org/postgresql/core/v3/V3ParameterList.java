/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core.v3;

import java.sql.SQLException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.core.ParameterList;
import org.postgresql.core.v3.SimpleParameterList;

interface V3ParameterList
extends ParameterList {
    public void checkAllParametersSet() throws SQLException;

    public void convertFunctionOutParameters();

    public SimpleParameterList @Nullable [] getSubparams();

    public int @Nullable [] getParamTypes();

    public byte @Nullable [] getFlags();

    public byte @Nullable [] @Nullable [] getEncoding();
}

