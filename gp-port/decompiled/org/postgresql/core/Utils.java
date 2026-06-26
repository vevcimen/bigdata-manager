/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core;

import java.io.IOException;
import java.sql.SQLException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.core.ServerVersion;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

public class Utils {
    public static String toHexString(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length * 2);
        for (byte element : data) {
            sb.append(Integer.toHexString(element >> 4 & 0xF));
            sb.append(Integer.toHexString(element & 0xF));
        }
        return sb.toString();
    }

    public static StringBuilder escapeLiteral(@Nullable StringBuilder sbuf, String value, boolean standardConformingStrings) throws SQLException {
        if (sbuf == null) {
            sbuf = new StringBuilder((value.length() + 10) / 10 * 11);
        }
        Utils.doAppendEscapedLiteral(sbuf, value, standardConformingStrings);
        return sbuf;
    }

    private static void doAppendEscapedLiteral(Appendable sbuf, String value, boolean standardConformingStrings) throws SQLException {
        try {
            if (standardConformingStrings) {
                for (int i = 0; i < value.length(); ++i) {
                    char ch = value.charAt(i);
                    if (ch == '\u0000') {
                        throw new PSQLException(GT.tr("Zero bytes may not occur in string parameters.", new Object[0]), PSQLState.INVALID_PARAMETER_VALUE);
                    }
                    if (ch == '\'') {
                        sbuf.append('\'');
                    }
                    sbuf.append(ch);
                }
            } else {
                for (int i = 0; i < value.length(); ++i) {
                    char ch = value.charAt(i);
                    if (ch == '\u0000') {
                        throw new PSQLException(GT.tr("Zero bytes may not occur in string parameters.", new Object[0]), PSQLState.INVALID_PARAMETER_VALUE);
                    }
                    if (ch == '\\' || ch == '\'') {
                        sbuf.append(ch);
                    }
                    sbuf.append(ch);
                }
            }
        }
        catch (IOException e) {
            throw new PSQLException(GT.tr("No IOException expected from StringBuffer or StringBuilder", new Object[0]), PSQLState.UNEXPECTED_ERROR, (Throwable)e);
        }
    }

    public static StringBuilder escapeIdentifier(@Nullable StringBuilder sbuf, String value) throws SQLException {
        if (sbuf == null) {
            sbuf = new StringBuilder(2 + (value.length() + 10) / 10 * 11);
        }
        Utils.doAppendEscapedIdentifier(sbuf, value);
        return sbuf;
    }

    private static void doAppendEscapedIdentifier(Appendable sbuf, String value) throws SQLException {
        try {
            sbuf.append('\"');
            for (int i = 0; i < value.length(); ++i) {
                char ch = value.charAt(i);
                if (ch == '\u0000') {
                    throw new PSQLException(GT.tr("Zero bytes may not occur in identifiers.", new Object[0]), PSQLState.INVALID_PARAMETER_VALUE);
                }
                if (ch == '\"') {
                    sbuf.append(ch);
                }
                sbuf.append(ch);
            }
            sbuf.append('\"');
        }
        catch (IOException e) {
            throw new PSQLException(GT.tr("No IOException expected from StringBuffer or StringBuilder", new Object[0]), PSQLState.UNEXPECTED_ERROR, (Throwable)e);
        }
    }

    @Deprecated
    public static int parseServerVersionStr(@Nullable String serverVersion) throws NumberFormatException {
        return ServerVersion.parseServerVersionStr(serverVersion);
    }
}

