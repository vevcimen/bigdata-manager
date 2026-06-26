/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.sspi;

import java.io.IOException;
import java.sql.SQLException;

public interface ISSPIClient {
    public boolean isSSPISupported();

    public void startSSPI() throws SQLException, IOException;

    public void continueSSPI(int var1) throws SQLException, IOException;

    public void dispose();
}

