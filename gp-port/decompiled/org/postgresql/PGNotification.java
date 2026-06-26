/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql;

public interface PGNotification {
    public String getName();

    public int getPID();

    public String getParameter();
}

