/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.jdbc;

import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.Driver;
import org.postgresql.core.BaseConnection;
import org.postgresql.core.Parser;
import org.postgresql.jdbc.BooleanTypeUtil;
import org.postgresql.jdbc.PgArray;
import org.postgresql.jdbc.PgResultSet;
import org.postgresql.jdbc2.ArrayAssistant;
import org.postgresql.jdbc2.ArrayAssistantRegistry;
import org.postgresql.util.GT;
import org.postgresql.util.PGbytea;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

final class ArrayDecoding {
    private static final ArrayDecoder<Long[]> LONG_OBJ_ARRAY = new AbstractObjectArrayDecoder<Long[]>(Long.class){

        @Override
        Object parseValue(int length, ByteBuffer bytes, BaseConnection connection) {
            return bytes.getLong();
        }

        @Override
        Object parseValue(String stringVal, BaseConnection connection) throws SQLException {
            return PgResultSet.toLong(stringVal);
        }
    };
    private static final ArrayDecoder<Long[]> INT4_UNSIGNED_OBJ_ARRAY = new AbstractObjectArrayDecoder<Long[]>(Long.class){

        @Override
        Object parseValue(int length, ByteBuffer bytes, BaseConnection connection) {
            long value = (long)bytes.getInt() & 0xFFFFFFFFL;
            return value;
        }

        @Override
        Object parseValue(String stringVal, BaseConnection connection) throws SQLException {
            return PgResultSet.toLong(stringVal);
        }
    };
    private static final ArrayDecoder<Integer[]> INTEGER_OBJ_ARRAY = new AbstractObjectArrayDecoder<Integer[]>(Integer.class){

        @Override
        Object parseValue(int length, ByteBuffer bytes, BaseConnection connection) {
            return bytes.getInt();
        }

        @Override
        Object parseValue(String stringVal, BaseConnection connection) throws SQLException {
            return PgResultSet.toInt(stringVal);
        }
    };
    private static final ArrayDecoder<Short[]> SHORT_OBJ_ARRAY = new AbstractObjectArrayDecoder<Short[]>(Short.class){

        @Override
        Object parseValue(int length, ByteBuffer bytes, BaseConnection connection) {
            return bytes.getShort();
        }

        @Override
        Object parseValue(String stringVal, BaseConnection connection) throws SQLException {
            return PgResultSet.toShort(stringVal);
        }
    };
    private static final ArrayDecoder<Double[]> DOUBLE_OBJ_ARRAY = new AbstractObjectArrayDecoder<Double[]>(Double.class){

        @Override
        Object parseValue(int length, ByteBuffer bytes, BaseConnection connection) {
            return bytes.getDouble();
        }

        @Override
        Object parseValue(String stringVal, BaseConnection connection) throws SQLException {
            return PgResultSet.toDouble(stringVal);
        }
    };
    private static final ArrayDecoder<Float[]> FLOAT_OBJ_ARRAY = new AbstractObjectArrayDecoder<Float[]>(Float.class){

        @Override
        Object parseValue(int length, ByteBuffer bytes, BaseConnection connection) {
            return Float.valueOf(bytes.getFloat());
        }

        @Override
        Object parseValue(String stringVal, BaseConnection connection) throws SQLException {
            return Float.valueOf(PgResultSet.toFloat(stringVal));
        }
    };
    private static final ArrayDecoder<Boolean[]> BOOLEAN_OBJ_ARRAY = new AbstractObjectArrayDecoder<Boolean[]>(Boolean.class){

        @Override
        Object parseValue(int length, ByteBuffer bytes, BaseConnection connection) {
            return bytes.get() == 1;
        }

        @Override
        Object parseValue(String stringVal, BaseConnection connection) throws SQLException {
            return BooleanTypeUtil.fromString(stringVal);
        }
    };
    private static final ArrayDecoder<String[]> STRING_ARRAY = new AbstractObjectArrayDecoder<String[]>(String.class){

        @Override
        Object parseValue(int length, ByteBuffer bytes, BaseConnection connection) throws SQLException {
            String val;
            assert (bytes.hasArray());
            byte[] byteArray = bytes.array();
            int offset = bytes.arrayOffset() + bytes.position();
            try {
                val = connection.getEncoding().decode(byteArray, offset, length);
            }
            catch (IOException e) {
                throw new PSQLException(GT.tr("Invalid character data was found.  This is most likely caused by stored data containing characters that are invalid for the character set the database was created in.  The most common example of this is storing 8bit data in a SQL_ASCII database.", new Object[0]), PSQLState.DATA_ERROR, (Throwable)e);
            }
            bytes.position(bytes.position() + length);
            return val;
        }

        @Override
        Object parseValue(String stringVal, BaseConnection connection) throws SQLException {
            return stringVal;
        }
    };
    private static final ArrayDecoder<byte[][]> BYTE_ARRAY_ARRAY = new AbstractObjectArrayDecoder<byte[][]>(byte[].class){

        @Override
        Object parseValue(int length, ByteBuffer bytes, BaseConnection connection) throws SQLException {
            byte[] array = new byte[length];
            bytes.get(array);
            return array;
        }

        @Override
        Object parseValue(String stringVal, BaseConnection connection) throws SQLException {
            return PGbytea.toBytes(stringVal.getBytes(StandardCharsets.US_ASCII));
        }
    };
    private static final ArrayDecoder<BigDecimal[]> BIG_DECIMAL_STRING_DECODER = new AbstractObjectStringArrayDecoder<BigDecimal[]>(BigDecimal.class){

        @Override
        Object parseValue(String stringVal, BaseConnection connection) throws SQLException {
            return PgResultSet.toBigDecimal(stringVal);
        }
    };
    private static final ArrayDecoder<String[]> STRING_ONLY_DECODER = new AbstractObjectStringArrayDecoder<String[]>(String.class){

        @Override
        Object parseValue(String stringVal, BaseConnection connection) throws SQLException {
            return stringVal;
        }
    };
    private static final ArrayDecoder<Date[]> DATE_DECODER = new AbstractObjectStringArrayDecoder<Date[]>(Date.class){

        @Override
        Object parseValue(String stringVal, BaseConnection connection) throws SQLException {
            return connection.getTimestampUtils().toDate(null, stringVal);
        }
    };
    private static final ArrayDecoder<Time[]> TIME_DECODER = new AbstractObjectStringArrayDecoder<Time[]>(Time.class){

        @Override
        Object parseValue(String stringVal, BaseConnection connection) throws SQLException {
            return connection.getTimestampUtils().toTime(null, stringVal);
        }
    };
    private static final ArrayDecoder<Timestamp[]> TIMESTAMP_DECODER = new AbstractObjectStringArrayDecoder<Timestamp[]>(Timestamp.class){

        @Override
        Object parseValue(String stringVal, BaseConnection connection) throws SQLException {
            return connection.getTimestampUtils().toTimestamp(null, stringVal);
        }
    };
    private static final Map<Integer, ArrayDecoder> OID_TO_DECODER = new HashMap<Integer, ArrayDecoder>(29);

    ArrayDecoding() {
    }

    private static <A> ArrayDecoder<A> getDecoder(int oid, BaseConnection connection) throws SQLException {
        Integer key = oid;
        ArrayDecoder decoder = OID_TO_DECODER.get(key);
        if (decoder != null) {
            return decoder;
        }
        ArrayAssistant assistant = ArrayAssistantRegistry.getAssistant(oid);
        if (assistant != null) {
            return new ArrayAssistantObjectArrayDecoder(assistant);
        }
        String typeName = connection.getTypeInfo().getPGType(oid);
        if (typeName == null) {
            throw Driver.notImplemented(PgArray.class, "readArray(data,oid)");
        }
        int type = connection.getTypeInfo().getSQLType(typeName);
        if (type == 1 || type == 12) {
            return STRING_ONLY_DECODER;
        }
        return new MappedTypeObjectArrayDecoder(typeName);
    }

    public static Object readBinaryArray(int index, int count, byte[] bytes, BaseConnection connection) throws SQLException {
        int adjustedSkipIndex;
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.BIG_ENDIAN);
        int dimensions = buffer.getInt();
        boolean hasNulls = buffer.getInt() != 0;
        int elementOid = buffer.getInt();
        ArrayDecoder decoder = ArrayDecoding.getDecoder(elementOid, connection);
        if (!decoder.supportBinary()) {
            throw Driver.notImplemented(PgArray.class, "readBinaryArray(data,oid)");
        }
        if (dimensions == 0) {
            return decoder.createArray(0);
        }
        int n = adjustedSkipIndex = index > 0 ? index - 1 : 0;
        if (dimensions == 1) {
            int length = buffer.getInt();
            buffer.position(buffer.position() + 4);
            if (count > 0) {
                length = Math.min(length, count);
            }
            Object array = decoder.createArray(length);
            decoder.populateFromBinary(array, adjustedSkipIndex, length, buffer, connection);
            return array;
        }
        int[] dimensionLengths = new int[dimensions];
        for (int i = 0; i < dimensions; ++i) {
            dimensionLengths[i] = buffer.getInt();
            buffer.position(buffer.position() + 4);
        }
        if (count > 0) {
            dimensionLengths[0] = Math.min(count, dimensionLengths[0]);
        }
        Object[] array = decoder.createMultiDimensionalArray(dimensionLengths);
        ArrayDecoding.storeValues(array, decoder, buffer, adjustedSkipIndex, dimensionLengths, 0, connection);
        return array;
    }

    private static <A> void storeValues(A[] array, ArrayDecoder<A> decoder, ByteBuffer bytes, int skip, int[] dimensionLengths, int dim, BaseConnection connection) throws SQLException {
        int i;
        assert (dim <= dimensionLengths.length - 2);
        for (i = 0; i < skip; ++i) {
            if (dim == dimensionLengths.length - 2) {
                decoder.populateFromBinary(array[0], 0, dimensionLengths[dim + 1], bytes, connection);
                continue;
            }
            ArrayDecoding.storeValues((Object[])array[0], decoder, bytes, 0, dimensionLengths, dim + 1, connection);
        }
        for (i = 0; i < dimensionLengths[dim]; ++i) {
            if (dim == dimensionLengths.length - 2) {
                decoder.populateFromBinary(array[i], 0, dimensionLengths[dim + 1], bytes, connection);
                continue;
            }
            ArrayDecoding.storeValues((Object[])array[i], decoder, bytes, 0, dimensionLengths, dim + 1, connection);
        }
    }

    static PgArrayList buildArrayList(String fieldString, char delim) {
        PgArrayList arrayList = new PgArrayList();
        if (fieldString == null) {
            return arrayList;
        }
        char[] chars = fieldString.toCharArray();
        StringBuilder buffer = null;
        boolean insideString = false;
        boolean wasInsideString = false;
        ArrayList<PgArrayList> dims = new ArrayList<PgArrayList>();
        PgArrayList curArray = arrayList;
        int startOffset = 0;
        if (chars[0] == '[') {
            while (chars[startOffset] != '=') {
                ++startOffset;
            }
            ++startOffset;
        }
        for (int i = startOffset; i < chars.length; ++i) {
            if (chars[i] == '\\') {
                ++i;
            } else {
                if (!insideString && chars[i] == '{') {
                    if (dims.isEmpty()) {
                        dims.add(arrayList);
                    } else {
                        PgArrayList a = new PgArrayList();
                        PgArrayList p = (PgArrayList)dims.get(dims.size() - 1);
                        p.add(a);
                        dims.add(a);
                    }
                    curArray = (PgArrayList)dims.get(dims.size() - 1);
                    for (int t = i + 1; t < chars.length; ++t) {
                        if (Character.isWhitespace(chars[t])) continue;
                        if (chars[t] != '{') break;
                        ++curArray.dimensionsCount;
                    }
                    buffer = new StringBuilder();
                    continue;
                }
                if (chars[i] == '\"') {
                    insideString = !insideString;
                    wasInsideString = true;
                    continue;
                }
                if (!insideString && Parser.isArrayWhiteSpace(chars[i])) continue;
                if (!insideString && (chars[i] == delim || chars[i] == '}') || i == chars.length - 1) {
                    String b;
                    if (chars[i] != '\"' && chars[i] != '}' && chars[i] != delim && buffer != null) {
                        buffer.append(chars[i]);
                    }
                    String string = b = buffer == null ? null : buffer.toString();
                    if (b != null && (!b.isEmpty() || wasInsideString)) {
                        curArray.add(!wasInsideString && b.equals("NULL") ? null : b);
                    }
                    wasInsideString = false;
                    buffer = new StringBuilder();
                    if (chars[i] != '}') continue;
                    dims.remove(dims.size() - 1);
                    if (!dims.isEmpty()) {
                        curArray = (PgArrayList)dims.get(dims.size() - 1);
                    }
                    buffer = null;
                    continue;
                }
            }
            if (buffer == null) continue;
            buffer.append(chars[i]);
        }
        return arrayList;
    }

    public static Object readStringArray(int index, int count, int oid, PgArrayList list, BaseConnection connection) throws SQLException {
        PgArrayList adjustedList;
        ArrayDecoder decoder = ArrayDecoding.getDecoder(oid, connection);
        int dims = list.dimensionsCount;
        if (dims == 0) {
            return decoder.createArray(0);
        }
        boolean sublist = false;
        int adjustedSkipIndex = 0;
        if (index > 1) {
            sublist = true;
            adjustedSkipIndex = index - 1;
        }
        int adjustedCount = list.size();
        if (count > 0 && count != adjustedCount) {
            sublist = true;
            adjustedCount = Math.min(adjustedCount, count);
        }
        List<Object> list2 = adjustedList = sublist ? list.subList(adjustedSkipIndex, adjustedSkipIndex + adjustedCount) : list;
        if (dims == 1) {
            int length = adjustedList.size();
            if (count > 0) {
                length = Math.min(length, count);
            }
            Object array = decoder.createArray(length);
            decoder.populateFromString(array, adjustedList, connection);
            return array;
        }
        int[] dimensionLengths = new int[dims];
        dimensionLengths[0] = adjustedCount;
        List tmpList = (List)adjustedList.get(0);
        for (int i = 1; i < dims; ++i) {
            dimensionLengths[i] = tmpList.size();
            if (i == dims - 1) continue;
            tmpList = (List)tmpList.get(0);
        }
        Object[] array = decoder.createMultiDimensionalArray(dimensionLengths);
        ArrayDecoding.storeStringValues(array, decoder, adjustedList, dimensionLengths, 0, connection);
        return array;
    }

    private static <A> void storeStringValues(A[] array, ArrayDecoder<A> decoder, List list, int @NonNull [] dimensionLengths, int dim, BaseConnection connection) throws SQLException {
        assert (dim <= dimensionLengths.length - 2);
        for (int i = 0; i < dimensionLengths[dim]; ++i) {
            if (dim == dimensionLengths.length - 2) {
                decoder.populateFromString(array[i], (List)list.get(i), connection);
                continue;
            }
            ArrayDecoding.storeStringValues((Object[])array[i], decoder, (List)list.get(i), dimensionLengths, dim + 1, connection);
        }
    }

    static {
        OID_TO_DECODER.put(26, INT4_UNSIGNED_OBJ_ARRAY);
        OID_TO_DECODER.put(20, LONG_OBJ_ARRAY);
        OID_TO_DECODER.put(23, INTEGER_OBJ_ARRAY);
        OID_TO_DECODER.put(21, SHORT_OBJ_ARRAY);
        OID_TO_DECODER.put(790, DOUBLE_OBJ_ARRAY);
        OID_TO_DECODER.put(701, DOUBLE_OBJ_ARRAY);
        OID_TO_DECODER.put(700, FLOAT_OBJ_ARRAY);
        OID_TO_DECODER.put(25, STRING_ARRAY);
        OID_TO_DECODER.put(1043, STRING_ARRAY);
        OID_TO_DECODER.put(3802, STRING_ONLY_DECODER);
        OID_TO_DECODER.put(1560, BOOLEAN_OBJ_ARRAY);
        OID_TO_DECODER.put(16, BOOLEAN_OBJ_ARRAY);
        OID_TO_DECODER.put(17, BYTE_ARRAY_ARRAY);
        OID_TO_DECODER.put(1700, BIG_DECIMAL_STRING_DECODER);
        OID_TO_DECODER.put(1042, STRING_ONLY_DECODER);
        OID_TO_DECODER.put(18, STRING_ONLY_DECODER);
        OID_TO_DECODER.put(114, STRING_ONLY_DECODER);
        OID_TO_DECODER.put(1082, DATE_DECODER);
        OID_TO_DECODER.put(1083, TIME_DECODER);
        OID_TO_DECODER.put(1266, TIME_DECODER);
        OID_TO_DECODER.put(1114, TIMESTAMP_DECODER);
        OID_TO_DECODER.put(1184, TIMESTAMP_DECODER);
    }

    private static final class MappedTypeObjectArrayDecoder
    extends AbstractObjectArrayDecoder<Object[]> {
        private final String typeName;

        MappedTypeObjectArrayDecoder(String baseTypeName) {
            super(Object.class);
            this.typeName = baseTypeName;
        }

        @Override
        Object parseValue(int length, ByteBuffer bytes, BaseConnection connection) throws SQLException {
            byte[] copy = new byte[length];
            bytes.get(copy);
            return connection.getObject(this.typeName, null, copy);
        }

        @Override
        Object parseValue(String stringVal, BaseConnection connection) throws SQLException {
            return connection.getObject(this.typeName, stringVal, null);
        }
    }

    private static final class ArrayAssistantObjectArrayDecoder
    extends AbstractObjectArrayDecoder {
        private final ArrayAssistant arrayAssistant;

        ArrayAssistantObjectArrayDecoder(ArrayAssistant arrayAssistant) {
            super(arrayAssistant.baseType());
            this.arrayAssistant = arrayAssistant;
        }

        @Override
        Object parseValue(int length, ByteBuffer bytes, BaseConnection connection) throws SQLException {
            assert (bytes.hasArray());
            byte[] byteArray = bytes.array();
            int offset = bytes.arrayOffset() + bytes.position();
            Object val = this.arrayAssistant.buildElement(byteArray, offset, length);
            bytes.position(bytes.position() + length);
            return val;
        }

        @Override
        Object parseValue(String stringVal, BaseConnection connection) throws SQLException {
            return this.arrayAssistant.buildElement(stringVal);
        }
    }

    private static abstract class AbstractObjectArrayDecoder<A>
    extends AbstractObjectStringArrayDecoder<A> {
        AbstractObjectArrayDecoder(Class<?> baseClazz) {
            super(baseClazz);
        }

        @Override
        public boolean supportBinary() {
            return true;
        }

        @Override
        public void populateFromBinary(A arr, @NonNegative int index, @NonNegative int count, ByteBuffer bytes, BaseConnection connection) throws SQLException {
            int length;
            int i;
            @Nullable Object[] array = (Object[])arr;
            for (i = 0; i < index; ++i) {
                length = bytes.getInt();
                if (length <= 0) continue;
                bytes.position(bytes.position() + length);
            }
            for (i = 0; i < count; ++i) {
                length = bytes.getInt();
                array[i] = length != -1 ? this.parseValue(length, bytes, connection) : null;
            }
        }

        abstract Object parseValue(int var1, ByteBuffer var2, BaseConnection var3) throws SQLException;
    }

    private static abstract class AbstractObjectStringArrayDecoder<A>
    implements ArrayDecoder<A> {
        final Class<?> baseClazz;

        AbstractObjectStringArrayDecoder(Class<?> baseClazz) {
            this.baseClazz = baseClazz;
        }

        @Override
        public boolean supportBinary() {
            return false;
        }

        @Override
        public A createArray(int size) {
            return (A)Array.newInstance(this.baseClazz, size);
        }

        @Override
        public Object[] createMultiDimensionalArray(int[] sizes) {
            return (Object[])Array.newInstance(this.baseClazz, sizes);
        }

        @Override
        public void populateFromBinary(A arr, int index, int count, ByteBuffer bytes, BaseConnection connection) throws SQLException {
            throw new SQLFeatureNotSupportedException();
        }

        @Override
        public void populateFromString(A arr, List<@Nullable String> strings, BaseConnection connection) throws SQLException {
            @Nullable Object[] array = (Object[])arr;
            int j = strings.size();
            for (int i = 0; i < j; ++i) {
                String stringVal = strings.get(i);
                array[i] = stringVal != null ? this.parseValue(stringVal, connection) : null;
            }
        }

        abstract Object parseValue(String var1, BaseConnection var2) throws SQLException;
    }

    private static interface ArrayDecoder<A> {
        public A createArray(@NonNegative int var1);

        public Object[] createMultiDimensionalArray(@NonNegative int[] var1);

        public boolean supportBinary();

        public void populateFromBinary(A var1, @NonNegative int var2, @NonNegative int var3, ByteBuffer var4, BaseConnection var5) throws SQLException;

        public void populateFromString(A var1, List<@Nullable String> var2, BaseConnection var3) throws SQLException;
    }

    static final class PgArrayList
    extends ArrayList<Object> {
        private static final long serialVersionUID = 1L;
        int dimensionsCount = 1;

        PgArrayList() {
        }
    }
}

