/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.jdbc2;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.jdbc2.ArrayAssistant;

public class ArrayAssistantRegistry {
    private static final ConcurrentMap<Integer, ArrayAssistant> ARRAY_ASSISTANT_MAP = new ConcurrentHashMap<Integer, ArrayAssistant>();

    public static @Nullable ArrayAssistant getAssistant(int oid) {
        return (ArrayAssistant)ARRAY_ASSISTANT_MAP.get(oid);
    }

    public static void register(int oid, ArrayAssistant assistant) {
        ARRAY_ASSISTANT_MAP.put(oid, assistant);
    }
}

