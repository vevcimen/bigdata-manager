/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core;

import org.postgresql.PGNotification;

public class Notification
implements PGNotification {
    private final String name;
    private final String parameter;
    private final int pid;

    public Notification(String name, int pid) {
        this(name, pid, "");
    }

    public Notification(String name, int pid, String parameter) {
        this.name = name;
        this.pid = pid;
        this.parameter = parameter;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getPID() {
        return this.pid;
    }

    @Override
    public String getParameter() {
        return this.parameter;
    }
}

