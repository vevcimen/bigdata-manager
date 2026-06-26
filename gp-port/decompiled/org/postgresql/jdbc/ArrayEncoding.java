/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.jdbc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.HashMap;
import java.util.Map;
import org.checkerframework.checker.index.qual.Positive;
import org.postgresql.core.BaseConnection;
import org.postgresql.core.Encoding;
import org.postgresql.jdbc.PgArray;
import org.postgresql.util.ByteConverter;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

final class ArrayEncoding {
    private static final AbstractArrayEncoder<long[]> LONG_ARRAY = new FixedSizePrimitiveArrayEncoder<long[]>(8, 20, 1016){

        @Override
        public void appendArray(StringBuilder sb, char delim, long[] array) {
            sb.append('{');
            for (int i = 0; i < array.length; ++i) {
                if (i > 0) {
                    sb.append(delim);
                }
                sb.append(array[i]);
            }
            sb.append('}');
        }

        @Override
        protected void write(long[] array, byte[] bytes, int offset) {
            int idx = offset;
            for (int i = 0; i < array.length; ++i) {
                bytes[idx + 3] = 8;
                ByteConverter.int8(bytes, idx + 4, array[i]);
                idx += 12;
            }
        }
    };
    private static final AbstractArrayEncoder<Long[]> LONG_OBJ_ARRAY = new NumberArrayEncoder<Long>(8, 20, 1016){

        @Override
        protected void write(Long number, byte[] bytes, int offset) {
            ByteConverter.int8(bytes, offset, number);
        }
    };
    private static final AbstractArrayEncoder<int[]> INT_ARRAY = new FixedSizePrimitiveArrayEncoder<int[]>(4, 23, 1007){

        @Override
        public void appendArray(StringBuilder sb, char delim, int[] array) {
            sb.append('{');
            for (int i = 0; i < array.length; ++i) {
                if (i > 0) {
                    sb.append(delim);
                }
                sb.append(array[i]);
            }
            sb.append('}');
        }

        @Override
        protected void write(int[] array, byte[] bytes, int offset) {
            int idx = offset;
            for (int i = 0; i < array.length; ++i) {
                bytes[idx + 3] = 4;
                ByteConverter.int4(bytes, idx + 4, array[i]);
                idx += 8;
            }
        }
    };
    private static final AbstractArrayEncoder<Integer[]> INT_OBJ_ARRAY = new NumberArrayEncoder<Integer>(4, 23, 1007){

        @Override
        protected void write(Integer number, byte[] bytes, int offset) {
            ByteConverter.int4(bytes, offset, number);
        }
    };
    private static final AbstractArrayEncoder<short[]> SHORT_ARRAY = new FixedSizePrimitiveArrayEncoder<short[]>(2, 21, 1005){

        @Override
        public void appendArray(StringBuilder sb, char delim, short[] array) {
            sb.append('{');
            for (int i = 0; i < array.length; ++i) {
                if (i > 0) {
                    sb.append(delim);
                }
                sb.append(array[i]);
            }
            sb.append('}');
        }

        @Override
        protected void write(short[] array, byte[] bytes, int offset) {
            int idx = offset;
            for (int i = 0; i < array.length; ++i) {
                bytes[idx + 3] = 2;
                ByteConverter.int2(bytes, idx + 4, array[i]);
                idx += 6;
            }
        }
    };
    private static final AbstractArrayEncoder<Short[]> SHORT_OBJ_ARRAY = new NumberArrayEncoder<Short>(2, 21, 1005){

        @Override
        protected void write(Short number, byte[] bytes, int offset) {
            ByteConverter.int2(bytes, offset, number.shortValue());
        }
    };
    private static final AbstractArrayEncoder<double[]> DOUBLE_ARRAY = new FixedSizePrimitiveArrayEncoder<double[]>(8, 701, 1022){

        @Override
        public void appendArray(StringBuilder sb, char delim, double[] array) {
            sb.append('{');
            for (int i = 0; i < array.length; ++i) {
                if (i > 0) {
                    sb.append(delim);
                }
                sb.append('\"');
                sb.append(array[i]);
                sb.append('\"');
            }
            sb.append('}');
        }

        @Override
        protected void write(double[] array, byte[] bytes, int offset) {
            int idx = offset;
            for (int i = 0; i < array.length; ++i) {
                bytes[idx + 3] = 8;
                ByteConverter.float8(bytes, idx + 4, array[i]);
                idx += 12;
            }
        }
    };
    private static final AbstractArrayEncoder<Double[]> DOUBLE_OBJ_ARRAY = new NumberArrayEncoder<Double>(8, 701, 1022){

        @Override
        protected void write(Double number, byte[] bytes, int offset) {
            ByteConverter.float8(bytes, offset, number);
        }
    };
    private static final AbstractArrayEncoder<float[]> FLOAT_ARRAY = new FixedSizePrimitiveArrayEncoder<float[]>(4, 700, 1021){

        @Override
        public void appendArray(StringBuilder sb, char delim, float[] array) {
            sb.append('{');
            for (int i = 0; i < array.length; ++i) {
                if (i > 0) {
                    sb.append(delim);
                }
                sb.append('\"');
                sb.append(array[i]);
                sb.append('\"');
            }
            sb.append('}');
        }

        @Override
        protected void write(float[] array, byte[] bytes, int offset) {
            int idx = offset;
            for (int i = 0; i < array.length; ++i) {
                bytes[idx + 3] = 4;
                ByteConverter.float4(bytes, idx + 4, array[i]);
                idx += 8;
            }
        }
    };
    private static final AbstractArrayEncoder<Float[]> FLOAT_OBJ_ARRAY = new NumberArrayEncoder<Float>(4, 700, 1021){

        @Override
        protected void write(Float number, byte[] bytes, int offset) {
            ByteConverter.float4(bytes, offset, number.floatValue());
        }
    };
    private static final AbstractArrayEncoder<boolean[]> BOOLEAN_ARRAY = new FixedSizePrimitiveArrayEncoder<boolean[]>(1, 16, 1000){

        @Override
        public void appendArray(StringBuilder sb, char delim, boolean[] array) {
            sb.append('{');
            for (int i = 0; i < array.length; ++i) {
                if (i > 0) {
                    sb.append(delim);
                }
                sb.append(array[i] ? (char)'1' : '0');
            }
            sb.append('}');
        }

        @Override
        protected void write(boolean[] array, byte[] bytes, int offset) {
            int idx = offset;
            for (int i = 0; i < array.length; ++i) {
                bytes[idx + 3] = 1;
                ByteConverter.bool(bytes, idx + 4, array[i]);
                idx += 5;
            }
        }
    };
    private static final AbstractArrayEncoder<Boolean[]> BOOLEAN_OBJ_ARRAY = new AbstractArrayEncoder<Boolean[]>(16, 1000){

        @Override
        public byte[] toBinaryRepresentation(BaseConnection connection, Boolean[] array, int oid) throws SQLException, SQLFeatureNotSupportedException {
            assert (oid == this.arrayOid);
            int nullCount = this.countNulls(array);
            byte[] bytes = this.writeBytes(array, nullCount, 20);
            ByteConverter.int4(bytes, 0, 1);
            ByteConverter.int4(bytes, 4, nullCount == 0 ? 0 : 1);
            ByteConverter.int4(bytes, 8, this.getTypeOID(oid));
            ByteConverter.int4(bytes, 12, array.length);
            ByteConverter.int4(bytes, 16, 1);
            return bytes;
        }

        private byte[] writeBytes(Boolean[] array, int nullCount, int offset) {
            int length = offset + 4 * array.length + (array.length - nullCount);
            byte[] bytes = new byte[length];
            int idx = offset;
            for (int i = 0; i < array.length; ++i) {
                if (array[i] == null) {
                    ByteConverter.int4(bytes, idx, -1);
                    idx += 4;
                    continue;
                }
                ByteConverter.int4(bytes, idx, 1);
                this.write(array[i], bytes, idx += 4);
                ++idx;
            }
            return bytes;
        }

        private void write(Boolean bool, byte[] bytes, int idx) {
            ByteConverter.bool(bytes, idx, bool);
        }

        @Override
        byte[] toSingleDimensionBinaryRepresentation(BaseConnection connection, Boolean[] array) throws SQLException, SQLFeatureNotSupportedException {
            int nullCount = this.countNulls(array);
            return this.writeBytes(array, nullCount, 0);
        }

        @Override
        public void appendArray(StringBuilder sb, char delim, Boolean[] array) {
            sb.append('{');
            for (int i = 0; i < array.length; ++i) {
                if (i != 0) {
                    sb.append(delim);
                }
                if (array[i] == null) {
                    sb.append('N').append('U').append('L').append('L');
                    continue;
                }
                sb.append(array[i] != false ? (char)'1' : '0');
            }
            sb.append('}');
        }
    };
    private static final AbstractArrayEncoder<String[]> STRING_ARRAY = new AbstractArrayEncoder<String[]>(1043, 1015){

        @Override
        int countNulls(String[] array) {
            int count = 0;
            for (int i = 0; i < array.length; ++i) {
                if (array[i] != null) continue;
                ++count;
            }
            return count;
        }

        @Override
        public boolean supportBinaryRepresentation(int oid) {
            return oid == 1015 || oid == 1009;
        }

        @Override
        int getTypeOID(int arrayOid) {
            if (arrayOid == 1015) {
                return 1043;
            }
            if (arrayOid == 1009) {
                return 25;
            }
            throw new IllegalStateException("Invalid array oid: " + arrayOid);
        }

        @Override
        public void appendArray(StringBuilder sb, char delim, String[] array) {
            sb.append('{');
            for (int i = 0; i < array.length; ++i) {
                if (i > 0) {
                    sb.append(delim);
                }
                if (array[i] == null) {
                    sb.append('N').append('U').append('L').append('L');
                    continue;
                }
                PgArray.escapeArrayElement(sb, array[i]);
            }
            sb.append('}');
        }

        @Override
        public byte[] toBinaryRepresentation(BaseConnection connection, String[] array, int oid) throws SQLException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(Math.min(1024, array.length * 32 + 20));
            assert (this.supportBinaryRepresentation(oid));
            byte[] buffer = new byte[4];
            try {
                ByteConverter.int4(buffer, 0, 1);
                baos.write(buffer);
                ByteConverter.int4(buffer, 0, this.countNulls(array) > 0 ? 1 : 0);
                baos.write(buffer);
                ByteConverter.int4(buffer, 0, this.getTypeOID(oid));
                baos.write(buffer);
                ByteConverter.int4(buffer, 0, array.length);
                baos.write(buffer);
                ByteConverter.int4(buffer, 0, 1);
                baos.write(buffer);
                Encoding encoding = connection.getEncoding();
                for (int i = 0; i < array.length; ++i) {
                    String string = array[i];
                    if (string != null) {
                        byte[] encoded;
                        try {
                            encoded = encoding.encode(string);
                        }
                        catch (IOException e) {
                            throw new PSQLException(GT.tr("Unable to translate data into the desired encoding.", new Object[0]), PSQLState.DATA_ERROR, (Throwable)e);
                        }
                        ByteConverter.int4(buffer, 0, encoded.length);
                        baos.write(buffer);
                        baos.write(encoded);
                        continue;
                    }
                    ByteConverter.int4(buffer, 0, -1);
                    baos.write(buffer);
                }
                return baos.toByteArray();
            }
            catch (IOException e) {
                throw new AssertionError((Object)e);
            }
        }

        @Override
        byte[] toSingleDimensionBinaryRepresentation(BaseConnection connection, String[] array) throws SQLException, SQLFeatureNotSupportedException {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream(Math.min(1024, array.length * 32 + 20));
                byte[] buffer = new byte[4];
                Encoding encoding = connection.getEncoding();
                for (int i = 0; i < array.length; ++i) {
                    String string = array[i];
                    if (string != null) {
                        byte[] encoded;
                        try {
                            encoded = encoding.encode(string);
                        }
                        catch (IOException e) {
                            throw new PSQLException(GT.tr("Unable to translate data into the desired encoding.", new Object[0]), PSQLState.DATA_ERROR, (Throwable)e);
                        }
                        ByteConverter.int4(buffer, 0, encoded.length);
                        baos.write(buffer);
                        baos.write(encoded);
                        continue;
                    }
                    ByteConverter.int4(buffer, 0, -1);
                    baos.write(buffer);
                }
                return baos.toByteArray();
            }
            catch (IOException e) {
                throw new AssertionError((Object)e);
            }
        }
    };
    private static final AbstractArrayEncoder<byte[][]> BYTEA_ARRAY = new AbstractArrayEncoder<byte[][]>(17, 1001){
        private final char[] hexDigits = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        @Override
        public byte[] toBinaryRepresentation(BaseConnection connection, byte[][] array, int oid) throws SQLException, SQLFeatureNotSupportedException {
            assert (oid == this.arrayOid);
            int length = 20;
            for (int i = 0; i < array.length; ++i) {
                length += 4;
                if (array[i] == null) continue;
                length += array[i].length;
            }
            byte[] bytes = new byte[length];
            ByteConverter.int4(bytes, 0, 1);
            ByteConverter.int4(bytes, 4, 0);
            ByteConverter.int4(bytes, 8, this.getTypeOID(oid));
            ByteConverter.int4(bytes, 12, array.length);
            ByteConverter.int4(bytes, 16, 1);
            this.write(array, bytes, 20);
            return bytes;
        }

        @Override
        byte[] toSingleDimensionBinaryRepresentation(BaseConnection connection, byte[][] array) throws SQLException, SQLFeatureNotSupportedException {
            int length = 0;
            for (int i = 0; i < array.length; ++i) {
                length += 4;
                if (array[i] == null) continue;
                length += array[i].length;
            }
            byte[] bytes = new byte[length];
            this.write(array, bytes, 0);
            return bytes;
        }

        @Override
        int countNulls(byte[][] array) {
            int nulls = 0;
            for (int i = 0; i < array.length; ++i) {
                if (array[i] != null) continue;
                ++nulls;
            }
            return nulls;
        }

        private void write(byte[][] array, byte[] bytes, int offset) {
            int idx = offset;
            for (int i = 0; i < array.length; ++i) {
                if (array[i] != null) {
                    ByteConverter.int4(bytes, idx, array[i].length);
                    System.arraycopy(array[i], 0, bytes, idx += 4, array[i].length);
                    idx += array[i].length;
                    continue;
                }
                ByteConverter.int4(bytes, idx, -1);
                idx += 4;
            }
        }

        @Override
        public void appendArray(StringBuilder sb, char delim, byte[][] array) {
            sb.append('{');
            for (int i = 0; i < array.length; ++i) {
                if (i > 0) {
                    sb.append(delim);
                }
                if (array[i] != null) {
                    sb.append("\"\\\\x");
                    for (int j = 0; j < array[i].length; ++j) {
                        byte b = array[i][j];
                        sb.append(this.hexDigits[(b & 0xF0) >>> 4]);
                        sb.append(this.hexDigits[b & 0xF]);
                    }
                    sb.append('\"');
                    continue;
                }
                sb.append("NULL");
            }
            sb.append('}');
        }
    };
    private static final AbstractArrayEncoder<Object[]> OBJECT_ARRAY = new AbstractArrayEncoder<Object[]>(0, 0){

        @Override
        public int getDefaultArrayTypeOid() {
            return 0;
        }

        @Override
        public boolean supportBinaryRepresentation(int oid) {
            return false;
        }

        @Override
        public byte[] toBinaryRepresentation(BaseConnection connection, Object[] array, int oid) throws SQLException, SQLFeatureNotSupportedException {
            throw new SQLFeatureNotSupportedException();
        }

        @Override
        byte[] toSingleDimensionBinaryRepresentation(BaseConnection connection, Object[] array) throws SQLException, SQLFeatureNotSupportedException {
            throw new SQLFeatureNotSupportedException();
        }

        @Override
        public void appendArray(StringBuilder sb, char delim, Object[] array) {
            sb.append('{');
            for (int i = 0; i < array.length; ++i) {
                if (i > 0) {
                    sb.append(delim);
                }
                if (array[i] == null) {
                    sb.append('N').append('U').append('L').append('L');
                    continue;
                }
                if (array[i].getClass().isArray()) {
                    if (array[i] instanceof byte[]) {
                        throw new UnsupportedOperationException("byte[] nested inside Object[]");
                    }
                    try {
                        ArrayEncoding.getArrayEncoder(array[i]).appendArray(sb, delim, array[i]);
                        continue;
                    }
                    catch (PSQLException e) {
                        throw new IllegalStateException(e);
                    }
                }
                PgArray.escapeArrayElement(sb, array[i].toString());
            }
            sb.append('}');
        }
    };
    private static final Map<Class, AbstractArrayEncoder> ARRAY_CLASS_TO_ENCODER = new HashMap<Class, AbstractArrayEncoder>(19);

    ArrayEncoding() {
    }

    public static <A> ArrayEncoder<A> getArrayEncoder(A array) throws PSQLException {
        Class<?> arrayClazz = array.getClass();
        Class<?> subClazz = arrayClazz.getComponentType();
        if (subClazz == null) {
            throw new PSQLException(GT.tr("Invalid elements {0}", array), PSQLState.INVALID_PARAMETER_TYPE);
        }
        AbstractArrayEncoder support = ARRAY_CLASS_TO_ENCODER.get(subClazz);
        if (support != null) {
            return support;
        }
        Class<?> subSubClazz = subClazz.getComponentType();
        if (subSubClazz == null) {
            if (Object.class.isAssignableFrom(subClazz)) {
                return OBJECT_ARRAY;
            }
            throw new PSQLException(GT.tr("Invalid elements {0}", array), PSQLState.INVALID_PARAMETER_TYPE);
        }
        subClazz = subSubClazz;
        int dimensions = 2;
        while (subClazz != null) {
            support = ARRAY_CLASS_TO_ENCODER.get(subClazz);
            if (support != null) {
                if (dimensions == 2) {
                    return new TwoDimensionPrimitiveArrayEncoder(support);
                }
                return new RecursiveArrayEncoder(support, dimensions);
            }
            subSubClazz = subClazz.getComponentType();
            if (subSubClazz == null && Object.class.isAssignableFrom(subClazz)) {
                if (dimensions == 2) {
                    return new TwoDimensionPrimitiveArrayEncoder<Object[]>(OBJECT_ARRAY);
                }
                return new RecursiveArrayEncoder(OBJECT_ARRAY, dimensions);
            }
            ++dimensions;
            subClazz = subSubClazz;
        }
        throw new PSQLException(GT.tr("Invalid elements {0}", array), PSQLState.INVALID_PARAMETER_TYPE);
    }

    static {
        ARRAY_CLASS_TO_ENCODER.put(Long.TYPE, LONG_ARRAY);
        ARRAY_CLASS_TO_ENCODER.put(Long.class, LONG_OBJ_ARRAY);
        ARRAY_CLASS_TO_ENCODER.put(Integer.TYPE, INT_ARRAY);
        ARRAY_CLASS_TO_ENCODER.put(Integer.class, INT_OBJ_ARRAY);
        ARRAY_CLASS_TO_ENCODER.put(Short.TYPE, SHORT_ARRAY);
        ARRAY_CLASS_TO_ENCODER.put(Short.class, SHORT_OBJ_ARRAY);
        ARRAY_CLASS_TO_ENCODER.put(Double.TYPE, DOUBLE_ARRAY);
        ARRAY_CLASS_TO_ENCODER.put(Double.class, DOUBLE_OBJ_ARRAY);
        ARRAY_CLASS_TO_ENCODER.put(Float.TYPE, FLOAT_ARRAY);
        ARRAY_CLASS_TO_ENCODER.put(Float.class, FLOAT_OBJ_ARRAY);
        ARRAY_CLASS_TO_ENCODER.put(Boolean.TYPE, BOOLEAN_ARRAY);
        ARRAY_CLASS_TO_ENCODER.put(Boolean.class, BOOLEAN_OBJ_ARRAY);
        ARRAY_CLASS_TO_ENCODER.put(byte[].class, BYTEA_ARRAY);
        ARRAY_CLASS_TO_ENCODER.put(String.class, STRING_ARRAY);
    }

    private static final class RecursiveArrayEncoder
    implements ArrayEncoder {
        private final AbstractArrayEncoder support;
        private final @Positive int dimensions;

        RecursiveArrayEncoder(AbstractArrayEncoder support, @Positive int dimensions) {
            this.support = support;
            this.dimensions = dimensions;
            assert (dimensions >= 2);
        }

        @Override
        public int getDefaultArrayTypeOid() {
            return this.support.getDefaultArrayTypeOid();
        }

        public String toArrayString(char delim, Object array) {
            StringBuilder sb = new StringBuilder(2048);
            this.arrayString(sb, array, delim, this.dimensions);
            return sb.toString();
        }

        public void appendArray(StringBuilder sb, char delim, Object array) {
            this.arrayString(sb, array, delim, this.dimensions);
        }

        private void arrayString(StringBuilder sb, Object array, char delim, int depth) {
            if (depth > 1) {
                sb.append('{');
                int j = Array.getLength(array);
                for (int i = 0; i < j; ++i) {
                    if (i > 0) {
                        sb.append(delim);
                    }
                    this.arrayString(sb, Array.get(array, i), delim, depth - 1);
                }
                sb.append('}');
            } else {
                this.support.appendArray(sb, delim, array);
            }
        }

        @Override
        public boolean supportBinaryRepresentation(int oid) {
            return this.support.supportBinaryRepresentation(oid);
        }

        private boolean hasNulls(Object array, int depth) {
            if (depth > 1) {
                int j = Array.getLength(array);
                for (int i = 0; i < j; ++i) {
                    if (!this.hasNulls(Array.get(array, i), depth - 1)) continue;
                    return true;
                }
                return false;
            }
            return this.support.countNulls(array) > 0;
        }

        public byte[] toBinaryRepresentation(BaseConnection connection, Object array, int oid) throws SQLException, SQLFeatureNotSupportedException {
            boolean hasNulls = this.hasNulls(array, this.dimensions);
            ByteArrayOutputStream baos = new ByteArrayOutputStream(1024 * this.dimensions);
            byte[] buffer = new byte[4];
            try {
                ByteConverter.int4(buffer, 0, this.dimensions);
                baos.write(buffer);
                ByteConverter.int4(buffer, 0, hasNulls ? 1 : 0);
                baos.write(buffer);
                ByteConverter.int4(buffer, 0, this.support.getTypeOID(oid));
                baos.write(buffer);
                ByteConverter.int4(buffer, 0, Array.getLength(array));
                baos.write(buffer);
                ByteConverter.int4(buffer, 0, 1);
                baos.write(buffer);
                this.writeArray(connection, buffer, baos, array, this.dimensions, true);
                return baos.toByteArray();
            }
            catch (IOException e) {
                throw new AssertionError((Object)e);
            }
        }

        private void writeArray(BaseConnection connection, byte[] buffer, ByteArrayOutputStream baos, Object array, int depth, boolean first) throws IOException, SQLException {
            int length = Array.getLength(array);
            if (first) {
                ByteConverter.int4(buffer, 0, length > 0 ? Array.getLength(Array.get(array, 0)) : 0);
                baos.write(buffer);
                ByteConverter.int4(buffer, 0, 1);
                baos.write(buffer);
            }
            for (int i = 0; i < length; ++i) {
                Object subArray = Array.get(array, i);
                if (depth > 2) {
                    this.writeArray(connection, buffer, baos, subArray, depth - 1, i == 0);
                    continue;
                }
                baos.write(this.support.toSingleDimensionBinaryRepresentation(connection, subArray));
            }
        }
    }

    private static final class TwoDimensionPrimitiveArrayEncoder<A>
    implements ArrayEncoder<A[]> {
        private final AbstractArrayEncoder<A> support;

        TwoDimensionPrimitiveArrayEncoder(AbstractArrayEncoder<A> support) {
            this.support = support;
        }

        @Override
        public int getDefaultArrayTypeOid() {
            return this.support.getDefaultArrayTypeOid();
        }

        @Override
        public String toArrayString(char delim, A[] array) {
            StringBuilder sb = new StringBuilder(1024);
            this.appendArray(sb, delim, array);
            return sb.toString();
        }

        @Override
        public void appendArray(StringBuilder sb, char delim, A[] array) {
            sb.append('{');
            for (int i = 0; i < array.length; ++i) {
                if (i > 0) {
                    sb.append(delim);
                }
                this.support.appendArray(sb, delim, array[i]);
            }
            sb.append('}');
        }

        @Override
        public boolean supportBinaryRepresentation(int oid) {
            return this.support.supportBinaryRepresentation(oid);
        }

        @Override
        public byte[] toBinaryRepresentation(BaseConnection connection, A[] array, int oid) throws SQLException, SQLFeatureNotSupportedException {
            int i;
            ByteArrayOutputStream baos = new ByteArrayOutputStream(Math.min(1024, array.length * 32 + 20));
            byte[] buffer = new byte[4];
            boolean hasNulls = false;
            for (i = 0; !hasNulls && i < array.length; ++i) {
                if (this.support.countNulls(array[i]) <= 0) continue;
                hasNulls = true;
            }
            try {
                ByteConverter.int4(buffer, 0, 2);
                baos.write(buffer);
                ByteConverter.int4(buffer, 0, hasNulls ? 1 : 0);
                baos.write(buffer);
                ByteConverter.int4(buffer, 0, this.support.getTypeOID(oid));
                baos.write(buffer);
                ByteConverter.int4(buffer, 0, array.length);
                baos.write(buffer);
                ByteConverter.int4(buffer, 0, 1);
                baos.write(buffer);
                ByteConverter.int4(buffer, 0, array.length > 0 ? Array.getLength(array[0]) : 0);
                baos.write(buffer);
                ByteConverter.int4(buffer, 0, 1);
                baos.write(buffer);
                for (i = 0; i < array.length; ++i) {
                    baos.write(this.support.toSingleDimensionBinaryRepresentation(connection, array[i]));
                }
                return baos.toByteArray();
            }
            catch (IOException e) {
                throw new AssertionError((Object)e);
            }
        }
    }

    private static abstract class FixedSizePrimitiveArrayEncoder<A>
    extends AbstractArrayEncoder<A> {
        private final int fieldSize;

        FixedSizePrimitiveArrayEncoder(int fieldSize, int oid, int arrayOid) {
            super(oid, arrayOid);
            this.fieldSize = fieldSize;
        }

        @Override
        final int countNulls(A array) {
            return 0;
        }

        @Override
        public final byte[] toBinaryRepresentation(BaseConnection connection, A array, int oid) throws SQLException, SQLFeatureNotSupportedException {
            assert (oid == this.arrayOid);
            int arrayLength = Array.getLength(array);
            int length = 20 + (this.fieldSize + 4) * arrayLength;
            byte[] bytes = new byte[length];
            ByteConverter.int4(bytes, 0, 1);
            ByteConverter.int4(bytes, 4, 0);
            ByteConverter.int4(bytes, 8, this.getTypeOID(oid));
            ByteConverter.int4(bytes, 12, arrayLength);
            ByteConverter.int4(bytes, 16, 1);
            this.write(array, bytes, 20);
            return bytes;
        }

        @Override
        final byte[] toSingleDimensionBinaryRepresentation(BaseConnection connection, A array) throws SQLException, SQLFeatureNotSupportedException {
            int length = (this.fieldSize + 4) * Array.getLength(array);
            byte[] bytes = new byte[length];
            this.write(array, bytes, 0);
            return bytes;
        }

        protected abstract void write(A var1, byte[] var2, int var3);
    }

    private static abstract class NumberArrayEncoder<N extends Number>
    extends AbstractArrayEncoder<N[]> {
        private final int fieldSize;

        NumberArrayEncoder(int fieldSize, int oid, int arrayOid) {
            super(oid, arrayOid);
            this.fieldSize = fieldSize;
        }

        @Override
        final int countNulls(N[] array) {
            int count = 0;
            for (int i = 0; i < array.length; ++i) {
                if (array[i] != null) continue;
                ++count;
            }
            return count;
        }

        @Override
        public final byte[] toBinaryRepresentation(BaseConnection connection, N[] array, int oid) throws SQLException, SQLFeatureNotSupportedException {
            assert (oid == this.arrayOid);
            int nullCount = ((AbstractArrayEncoder)this).countNulls(array);
            byte[] bytes = this.writeBytes((Number[])array, nullCount, 20);
            ByteConverter.int4(bytes, 0, 1);
            ByteConverter.int4(bytes, 4, nullCount == 0 ? 0 : 1);
            ByteConverter.int4(bytes, 8, this.getTypeOID(oid));
            ByteConverter.int4(bytes, 12, array.length);
            ByteConverter.int4(bytes, 16, 1);
            return bytes;
        }

        @Override
        final byte[] toSingleDimensionBinaryRepresentation(BaseConnection connection, N[] array) throws SQLException, SQLFeatureNotSupportedException {
            int nullCount = ((AbstractArrayEncoder)this).countNulls(array);
            return this.writeBytes((Number[])array, nullCount, 0);
        }

        private byte[] writeBytes(N[] array, int nullCount, int offset) {
            int length = offset + 4 * array.length + this.fieldSize * (array.length - nullCount);
            byte[] bytes = new byte[length];
            int idx = offset;
            for (int i = 0; i < array.length; ++i) {
                if (array[i] == null) {
                    ByteConverter.int4(bytes, idx, -1);
                    idx += 4;
                    continue;
                }
                ByteConverter.int4(bytes, idx, this.fieldSize);
                this.write(array[i], bytes, idx += 4);
                idx += this.fieldSize;
            }
            return bytes;
        }

        protected abstract void write(N var1, byte[] var2, int var3);

        @Override
        public final void appendArray(StringBuilder sb, char delim, N[] array) {
            sb.append('{');
            for (int i = 0; i < array.length; ++i) {
                if (i != 0) {
                    sb.append(delim);
                }
                if (array[i] == null) {
                    sb.append('N').append('U').append('L').append('L');
                    continue;
                }
                sb.append('\"');
                sb.append(array[i].toString());
                sb.append('\"');
            }
            sb.append('}');
        }
    }

    private static abstract class AbstractArrayEncoder<A>
    implements ArrayEncoder<A> {
        private final int oid;
        final int arrayOid;

        AbstractArrayEncoder(int oid, int arrayOid) {
            this.oid = oid;
            this.arrayOid = arrayOid;
        }

        int getTypeOID(int arrayOid) {
            return this.oid;
        }

        @Override
        public int getDefaultArrayTypeOid() {
            return this.arrayOid;
        }

        int countNulls(A array) {
            int nulls = 0;
            int arrayLength = Array.getLength(array);
            for (int i = 0; i < arrayLength; ++i) {
                if (Array.get(array, i) != null) continue;
                ++nulls;
            }
            return nulls;
        }

        abstract byte[] toSingleDimensionBinaryRepresentation(BaseConnection var1, A var2) throws SQLException, SQLFeatureNotSupportedException;

        @Override
        public String toArrayString(char delim, A array) {
            StringBuilder sb = new StringBuilder(1024);
            this.appendArray(sb, delim, array);
            return sb.toString();
        }

        @Override
        public boolean supportBinaryRepresentation(int oid) {
            return oid == this.arrayOid;
        }
    }

    static interface ArrayEncoder<A> {
        public int getDefaultArrayTypeOid();

        public String toArrayString(char var1, A var2);

        public boolean supportBinaryRepresentation(int var1);

        public byte[] toBinaryRepresentation(BaseConnection var1, A var2, int var3) throws SQLException, SQLFeatureNotSupportedException;

        public void appendArray(StringBuilder var1, char var2, A var3);
    }
}

