/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.checkerframework.checker.nullness.qual.Nullable;

public class JdbcBlackHole {
    public static void close(@Nullable Connection con) {
        try {
            if (con != null) {
                con.close();
            }
        }
        catch (SQLException sQLException) {
            // empty catch block
        }
    }

    public static void close(@Nullable Statement s2) {
        try {
            if (s2 != null) {
                s2.close();
            }
        }
        catch (SQLException sQLException) {
            // empty catch block
        }
    }

    public static void close(@Nullable ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        }
        catch (SQLException sQLException) {
            // empty catch block
        }
    }
}

