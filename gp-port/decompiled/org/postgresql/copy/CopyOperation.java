/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.copy;

import java.sql.SQLException;

public interface CopyOperation {
    public int getFieldCount();

    public int getFormat();

    public int getFieldFormat(int var1);

    public boolean isActive();

    public void cancelCopy() throws SQLException;

    public long getHandledRowCount();
}

