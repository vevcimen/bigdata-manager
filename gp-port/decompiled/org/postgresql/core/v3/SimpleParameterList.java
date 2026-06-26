/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core.v3;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Arrays;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.index.qual.Positive;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.core.PGStream;
import org.postgresql.core.ParameterList;
import org.postgresql.core.Utils;
import org.postgresql.core.v3.TypeTransferModeRegistry;
import org.postgresql.core.v3.V3ParameterList;
import org.postgresql.geometric.PGbox;
import org.postgresql.geometric.PGpoint;
import org.postgresql.jdbc.UUIDArrayAssistant;
import org.postgresql.util.ByteConverter;
import org.postgresql.util.ByteStreamWriter;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.StreamWrapper;

class SimpleParameterList
implements V3ParameterList {
    private static final byte IN = 1;
    private static final byte OUT = 2;
    private static final byte INOUT = 3;
    private static final byte TEXT = 0;
    private static final byte BINARY = 4;
    private final @Nullable Object[] paramValues;
    private final int[] paramTypes;
    private final byte[] flags;
    private final byte[] @Nullable [] encoded;
    private final @Nullable TypeTransferModeRegistry transferModeRegistry;
    private static final Object NULL_OBJECT = new Object();
    private int pos = 0;

    SimpleParameterList(int paramCount, @Nullable TypeTransferModeRegistry transferModeRegistry) {
        this.paramValues = new Object[paramCount];
        this.paramTypes = new int[paramCount];
        this.encoded = new byte[paramCount][];
        this.flags = new byte[paramCount];
        this.transferModeRegistry = transferModeRegistry;
    }

    @Override
    public void registerOutParameter(int index, int sqlType) throws SQLException {
        if (index < 1 || index > this.paramValues.length) {
            throw new PSQLException(GT.tr("The column index is out of range: {0}, number of columns: {1}.", index, this.paramValues.length), PSQLState.INVALID_PARAMETER_VALUE);
        }
        int n = index - 1;
        this.flags[n] = (byte)(this.flags[n] | 2);
    }

    private void bind(int index, Object value, int oid, byte binary) throws SQLException {
        if (index < 1 || index > this.paramValues.length) {
            throw new PSQLException(GT.tr("The column index is out of range: {0}, number of columns: {1}.", index, this.paramValues.length), PSQLState.INVALID_PARAMETER_VALUE);
        }
        this.encoded[--index] = null;
        this.paramValues[index] = value;
        this.flags[index] = (byte)(this.direction(index) | 1 | binary);
        if (oid == 0 && this.paramTypes[index] != 0 && value == NULL_OBJECT) {
            return;
        }
        this.paramTypes[index] = oid;
        this.pos = index + 1;
    }

    @Override
    public @NonNegative int getParameterCount() {
        return this.paramValues.length;
    }

    @Override
    public @NonNegative int getOutParameterCount() {
        int count = 0;
        for (int i = 0; i < this.paramTypes.length; ++i) {
            if ((this.direction(i) & 2) != 2) continue;
            ++count;
        }
        if (count == 0) {
            count = 1;
        }
        return count;
    }

    @Override
    public @NonNegative int getInParameterCount() {
        int count = 0;
        for (int i = 0; i < this.paramTypes.length; ++i) {
            if (this.direction(i) == 2) continue;
            ++count;
        }
        return count;
    }

    @Override
    public void setIntParameter(@Positive int index, int value) throws SQLException {
        byte[] data = new byte[4];
        ByteConverter.int4(data, 0, value);
        this.bind(index, data, 23, (byte)4);
    }

    @Override
    public void setLiteralParameter(@Positive int index, String value, int oid) throws SQLException {
        this.bind(index, value, oid, (byte)0);
    }

    @Override
    public void setStringParameter(@Positive int index, String value, int oid) throws SQLException {
        this.bind(index, value, oid, (byte)0);
    }

    @Override
    public void setBinaryParameter(@Positive int index, byte[] value, int oid) throws SQLException {
        this.bind(index, value, oid, (byte)4);
    }

    @Override
    public void setBytea(@Positive int index, byte[] data, int offset, @NonNegative int length) throws SQLException {
        this.bind(index, new StreamWrapper(data, offset, length), 17, (byte)4);
    }

    @Override
    public void setBytea(@Positive int index, InputStream stream, @NonNegative int length) throws SQLException {
        this.bind(index, new StreamWrapper(stream, length), 17, (byte)4);
    }

    @Override
    public void setBytea(@Positive int index, InputStream stream) throws SQLException {
        this.bind(index, new StreamWrapper(stream), 17, (byte)4);
    }

    @Override
    public void setBytea(@Positive int index, ByteStreamWriter writer) throws SQLException {
        this.bind(index, writer, 17, (byte)4);
    }

    @Override
    public void setText(@Positive int index, InputStream stream) throws SQLException {
        this.bind(index, new StreamWrapper(stream), 25, (byte)0);
    }

    @Override
    public void setNull(@Positive int index, int oid) throws SQLException {
        int binaryTransfer = 0;
        if (this.transferModeRegistry != null && this.transferModeRegistry.useBinaryForReceive(oid)) {
            binaryTransfer = 4;
        }
        this.bind(index, NULL_OBJECT, oid, (byte)binaryTransfer);
    }

    @Override
    public String toString(@Positive int index, boolean standardConformingStrings) {
        Object paramValue;
        if ((paramValue = this.paramValues[--index]) == null) {
            return "?";
        }
        if (paramValue == NULL_OBJECT) {
            return "NULL";
        }
        if ((this.flags[index] & 4) == 4) {
            switch (this.paramTypes[index]) {
                case 21: {
                    short s2 = ByteConverter.int2((byte[])paramValue, 0);
                    return Short.toString(s2);
                }
                case 23: {
                    int i = ByteConverter.int4((byte[])paramValue, 0);
                    return Integer.toString(i);
                }
                case 20: {
                    long l = ByteConverter.int8((byte[])paramValue, 0);
                    return Long.toString(l);
                }
                case 700: {
                    float f = ByteConverter.float4((byte[])paramValue, 0);
                    if (Float.isNaN(f)) {
                        return "'NaN'::real";
                    }
                    return Float.toString(f);
                }
                case 701: {
                    double d = ByteConverter.float8((byte[])paramValue, 0);
                    if (Double.isNaN(d)) {
                        return "'NaN'::double precision";
                    }
                    return Double.toString(d);
                }
                case 1700: {
                    Number n = ByteConverter.numeric((byte[])paramValue);
                    if (n instanceof Double) {
                        assert (((Double)n).isNaN());
                        return "'NaN'::numeric";
                    }
                    return n.toString();
                }
                case 2950: {
                    String uuid = new UUIDArrayAssistant().buildElement((byte[])paramValue, 0, 16).toString();
                    return "'" + uuid + "'::uuid";
                }
                case 600: {
                    PGpoint pgPoint = new PGpoint();
                    pgPoint.setByteValue((byte[])paramValue, 0);
                    return "'" + pgPoint.toString() + "'::point";
                }
                case 603: {
                    PGbox pgBox = new PGbox();
                    pgBox.setByteValue((byte[])paramValue, 0);
                    return "'" + pgBox.toString() + "'::box";
                }
            }
            return "?";
        }
        String param = paramValue.toString();
        StringBuilder p = new StringBuilder(3 + (param.length() + 10) / 10 * 11);
        p.append('\'');
        try {
            p = Utils.escapeLiteral(p, param, standardConformingStrings);
        }
        catch (SQLException sqle) {
            p.append(param);
        }
        p.append('\'');
        int paramType = this.paramTypes[index];
        if (paramType == 1114) {
            p.append("::timestamp");
        } else if (paramType == 1184) {
            p.append("::timestamp with time zone");
        } else if (paramType == 1083) {
            p.append("::time");
        } else if (paramType == 1266) {
            p.append("::time with time zone");
        } else if (paramType == 1082) {
            p.append("::date");
        } else if (paramType == 1186) {
            p.append("::interval");
        } else if (paramType == 1700) {
            p.append("::numeric");
        }
        return p.toString();
    }

    @Override
    public void checkAllParametersSet() throws SQLException {
        for (int i = 0; i < this.paramTypes.length; ++i) {
            if (this.direction(i) == 2 || this.paramValues[i] != null) continue;
            throw new PSQLException(GT.tr("No value specified for parameter {0}.", i + 1), PSQLState.INVALID_PARAMETER_VALUE);
        }
    }

    @Override
    public void convertFunctionOutParameters() {
        for (int i = 0; i < this.paramTypes.length; ++i) {
            if (this.direction(i) != 2) continue;
            this.paramTypes[i] = 2278;
            this.paramValues[i] = NULL_OBJECT;
        }
    }

    private static void streamBytea(PGStream pgStream, StreamWrapper wrapper) throws IOException {
        byte[] rawData = wrapper.getBytes();
        if (rawData != null) {
            pgStream.send(rawData, wrapper.getOffset(), wrapper.getLength());
            return;
        }
        pgStream.sendStream(wrapper.getStream(), wrapper.getLength());
    }

    private static void streamBytea(PGStream pgStream, ByteStreamWriter writer) throws IOException {
        pgStream.send(writer);
    }

    @Override
    public int[] getTypeOIDs() {
        return this.paramTypes;
    }

    int getTypeOID(@Positive int index) {
        return this.paramTypes[index - 1];
    }

    boolean hasUnresolvedTypes() {
        for (int paramType : this.paramTypes) {
            if (paramType != 0) continue;
            return true;
        }
        return false;
    }

    void setResolvedType(@Positive int index, int oid) {
        if (this.paramTypes[index - 1] == 0) {
            this.paramTypes[index - 1] = oid;
        } else if (this.paramTypes[index - 1] != oid) {
            throw new IllegalArgumentException("Can't change resolved type for param: " + index + " from " + this.paramTypes[index - 1] + " to " + oid);
        }
    }

    boolean isNull(@Positive int index) {
        return this.paramValues[index - 1] == NULL_OBJECT;
    }

    boolean isBinary(@Positive int index) {
        return (this.flags[index - 1] & 4) != 0;
    }

    private byte direction(@Positive int index) {
        return (byte)(this.flags[index] & 3);
    }

    int getV3Length(@Positive int index) {
        Object value;
        if ((value = this.paramValues[--index]) == null || value == NULL_OBJECT) {
            throw new IllegalArgumentException("can't getV3Length() on a null parameter");
        }
        if (value instanceof byte[]) {
            return ((byte[])value).length;
        }
        if (value instanceof StreamWrapper) {
            return ((StreamWrapper)value).getLength();
        }
        if (value instanceof ByteStreamWriter) {
            return ((ByteStreamWriter)value).getLength();
        }
        byte[] encoded = this.encoded[index];
        if (encoded == null) {
            encoded = value.toString().getBytes(StandardCharsets.UTF_8);
            this.encoded[index] = encoded;
        }
        return encoded.length;
    }

    void writeV3Value(@Positive int index, PGStream pgStream) throws IOException {
        Object paramValue;
        if ((paramValue = this.paramValues[--index]) == null || paramValue == NULL_OBJECT) {
            throw new IllegalArgumentException("can't writeV3Value() on a null parameter");
        }
        if (paramValue instanceof byte[]) {
            pgStream.send((byte[])paramValue);
            return;
        }
        if (paramValue instanceof StreamWrapper) {
            SimpleParameterList.streamBytea(pgStream, (StreamWrapper)paramValue);
            return;
        }
        if (paramValue instanceof ByteStreamWriter) {
            SimpleParameterList.streamBytea(pgStream, (ByteStreamWriter)paramValue);
            return;
        }
        if (this.encoded[index] == null) {
            this.encoded[index] = ((String)paramValue).getBytes(StandardCharsets.UTF_8);
        }
        pgStream.send(this.encoded[index]);
    }

    @Override
    public ParameterList copy() {
        SimpleParameterList newCopy = new SimpleParameterList(this.paramValues.length, this.transferModeRegistry);
        System.arraycopy(this.paramValues, 0, newCopy.paramValues, 0, this.paramValues.length);
        System.arraycopy(this.paramTypes, 0, newCopy.paramTypes, 0, this.paramTypes.length);
        System.arraycopy(this.flags, 0, newCopy.flags, 0, this.flags.length);
        newCopy.pos = this.pos;
        return newCopy;
    }

    @Override
    public void clear() {
        Arrays.fill(this.paramValues, null);
        Arrays.fill(this.paramTypes, 0);
        Arrays.fill((Object[])this.encoded, null);
        Arrays.fill(this.flags, (byte)0);
        this.pos = 0;
    }

    @Override
    public SimpleParameterList @Nullable [] getSubparams() {
        return null;
    }

    @Override
    public @Nullable Object[] getValues() {
        return this.paramValues;
    }

    @Override
    public int[] getParamTypes() {
        return this.paramTypes;
    }

    @Override
    public byte[] getFlags() {
        return this.flags;
    }

    @Override
    public byte[] @Nullable [] getEncoding() {
        return this.encoded;
    }

    @Override
    public void appendAll(ParameterList list) throws SQLException {
        if (list instanceof SimpleParameterList) {
            SimpleParameterList spl = (SimpleParameterList)list;
            int inParamCount = spl.getInParameterCount();
            if (this.pos + inParamCount > this.paramValues.length) {
                throw new PSQLException(GT.tr("Added parameters index out of range: {0}, number of columns: {1}.", this.pos + inParamCount, this.paramValues.length), PSQLState.INVALID_PARAMETER_VALUE);
            }
            System.arraycopy(spl.getValues(), 0, this.paramValues, this.pos, inParamCount);
            System.arraycopy(spl.getParamTypes(), 0, this.paramTypes, this.pos, inParamCount);
            System.arraycopy(spl.getFlags(), 0, this.flags, this.pos, inParamCount);
            System.arraycopy(spl.getEncoding(), 0, this.encoded, this.pos, inParamCount);
            this.pos += inParamCount;
        }
    }

    public String toString() {
        StringBuilder ts = new StringBuilder("<[");
        if (this.paramValues.length > 0) {
            ts.append(this.toString(1, true));
            for (int c = 2; c <= this.paramValues.length; ++c) {
                ts.append(" ,").append(this.toString(c, true));
            }
        }
        ts.append("]>");
        return ts.toString();
    }
}

