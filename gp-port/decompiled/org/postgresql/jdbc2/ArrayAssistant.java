/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.jdbc2;

public interface ArrayAssistant {
    public Class<?> baseType();

    public Object buildElement(byte[] var1, int var2, int var3);

    public Object buildElement(String var1);
}

