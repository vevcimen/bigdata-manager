/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core.v3;

public interface TypeTransferModeRegistry {
    public boolean useBinaryForSend(int var1);

    public boolean useBinaryForReceive(int var1);
}

