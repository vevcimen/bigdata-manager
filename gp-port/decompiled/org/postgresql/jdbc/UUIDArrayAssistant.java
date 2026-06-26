/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.jdbc;

import java.util.UUID;
import org.postgresql.jdbc2.ArrayAssistant;
import org.postgresql.util.ByteConverter;

public class UUIDArrayAssistant
implements ArrayAssistant {
    @Override
    public Class<?> baseType() {
        return UUID.class;
    }

    @Override
    public Object buildElement(byte[] bytes, int pos, int len) {
        return new UUID(ByteConverter.int8(bytes, pos + 0), ByteConverter.int8(bytes, pos + 8));
    }

    @Override
    public Object buildElement(String literal) {
        return UUID.fromString(literal);
    }
}

