/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core.v3;

import java.io.InputStream;
import java.sql.SQLException;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.index.qual.Positive;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.core.ParameterList;
import org.postgresql.core.v3.SimpleParameterList;
import org.postgresql.core.v3.V3ParameterList;
import org.postgresql.util.ByteStreamWriter;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

class CompositeParameterList
implements V3ParameterList {
    private final @Positive int total;
    private final SimpleParameterList[] subparams;
    private final int[] offsets;

    CompositeParameterList(SimpleParameterList[] subparams, int[] offsets) {
        this.subparams = subparams;
        this.offsets = offsets;
        this.total = offsets[offsets.length - 1] + subparams[offsets.length - 1].getInParameterCount();
    }

    private int findSubParam(@Positive int index) throws SQLException {
        if (index < 1 || index > this.total) {
            throw new PSQLException(GT.tr("The column index is out of range: {0}, number of columns: {1}.", index, this.total), PSQLState.INVALID_PARAMETER_VALUE);
        }
        for (int i = this.offsets.length - 1; i >= 0; --i) {
            if (this.offsets[i] >= index) continue;
            return i;
        }
        throw new IllegalArgumentException("I am confused; can't find a subparam for index " + index);
    }

    @Override
    public void registerOutParameter(@Positive int index, int sqlType) {
    }

    public int getDirection(int i) {
        return 0;
    }

    @Override
    public @NonNegative int getParameterCount() {
        return this.total;
    }

    @Override
    public @NonNegative int getInParameterCount() {
        return this.total;
    }

    @Override
    public @NonNegative int getOutParameterCount() {
        return 0;
    }

    @Override
    public int[] getTypeOIDs() {
        int[] oids = new int[this.total];
        for (int i = 0; i < this.offsets.length; ++i) {
            int[] subOids = this.subparams[i].getTypeOIDs();
            System.arraycopy(subOids, 0, oids, this.offsets[i], subOids.length);
        }
        return oids;
    }

    @Override
    public void setIntParameter(@Positive int index, int value) throws SQLException {
        int sub = this.findSubParam(index);
        this.subparams[sub].setIntParameter(index - this.offsets[sub], value);
    }

    @Override
    public void setLiteralParameter(@Positive int index, String value, int oid) throws SQLException {
        int sub = this.findSubParam(index);
        this.subparams[sub].setStringParameter(index - this.offsets[sub], value, oid);
    }

    @Override
    public void setStringParameter(@Positive int index, String value, int oid) throws SQLException {
        int sub = this.findSubParam(index);
        this.subparams[sub].setStringParameter(index - this.offsets[sub], value, oid);
    }

    @Override
    public void setBinaryParameter(@Positive int index, byte[] value, int oid) throws SQLException {
        int sub = this.findSubParam(index);
        this.subparams[sub].setBinaryParameter(index - this.offsets[sub], value, oid);
    }

    @Override
    public void setBytea(@Positive int index, byte[] data, int offset, @NonNegative int length) throws SQLException {
        int sub = this.findSubParam(index);
        this.subparams[sub].setBytea(index - this.offsets[sub], data, offset, length);
    }

    @Override
    public void setBytea(@Positive int index, InputStream stream, @NonNegative int length) throws SQLException {
        int sub = this.findSubParam(index);
        this.subparams[sub].setBytea(index - this.offsets[sub], stream, length);
    }

    @Override
    public void setBytea(@Positive int index, InputStream stream) throws SQLException {
        int sub = this.findSubParam(index);
        this.subparams[sub].setBytea(index - this.offsets[sub], stream);
    }

    @Override
    public void setBytea(@Positive int index, ByteStreamWriter writer) throws SQLException {
        int sub = this.findSubParam(index);
        this.subparams[sub].setBytea(index - this.offsets[sub], writer);
    }

    @Override
    public void setText(@Positive int index, InputStream stream) throws SQLException {
        int sub = this.findSubParam(index);
        this.subparams[sub].setText(index - this.offsets[sub], stream);
    }

    @Override
    public void setNull(@Positive int index, int oid) throws SQLException {
        int sub = this.findSubParam(index);
        this.subparams[sub].setNull(index - this.offsets[sub], oid);
    }

    @Override
    public String toString(@Positive int index, boolean standardConformingStrings) {
        try {
            int sub = this.findSubParam(index);
            return this.subparams[sub].toString(index - this.offsets[sub], standardConformingStrings);
        }
        catch (SQLException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    @Override
    public ParameterList copy() {
        SimpleParameterList[] copySub = new SimpleParameterList[this.subparams.length];
        for (int sub = 0; sub < this.subparams.length; ++sub) {
            copySub[sub] = (SimpleParameterList)this.subparams[sub].copy();
        }
        return new CompositeParameterList(copySub, this.offsets);
    }

    @Override
    public void clear() {
        for (SimpleParameterList subparam : this.subparams) {
            subparam.clear();
        }
    }

    @Override
    public SimpleParameterList @Nullable [] getSubparams() {
        return this.subparams;
    }

    @Override
    public void checkAllParametersSet() throws SQLException {
        for (SimpleParameterList subparam : this.subparams) {
            subparam.checkAllParametersSet();
        }
    }

    @Override
    public byte @Nullable [][] getEncoding() {
        return null;
    }

    @Override
    public byte @Nullable [] getFlags() {
        return null;
    }

    @Override
    public int @Nullable [] getParamTypes() {
        return null;
    }

    @Override
    public @Nullable Object @Nullable [] getValues() {
        return null;
    }

    @Override
    public void appendAll(ParameterList list) throws SQLException {
    }

    @Override
    public void convertFunctionOutParameters() {
        for (SimpleParameterList subparam : this.subparams) {
            subparam.convertFunctionOutParameters();
        }
    }
}

