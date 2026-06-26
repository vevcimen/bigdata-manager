/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.core.ParameterList;
import org.postgresql.core.SqlCommand;

public class NativeQuery {
    private static final String[] BIND_NAMES = new String[1280];
    private static final int[] NO_BINDS = new int[0];
    public final String nativeSql;
    public final int[] bindPositions;
    public final SqlCommand command;
    public final boolean multiStatement;

    public NativeQuery(String nativeSql, SqlCommand dml) {
        this(nativeSql, NO_BINDS, true, dml);
    }

    public NativeQuery(String nativeSql, int[] bindPositions, boolean multiStatement, SqlCommand dml) {
        this.nativeSql = nativeSql;
        this.bindPositions = bindPositions == null || bindPositions.length == 0 ? NO_BINDS : bindPositions;
        this.multiStatement = multiStatement;
        this.command = dml;
    }

    public String toString(@Nullable ParameterList parameters) {
        if (this.bindPositions.length == 0) {
            return this.nativeSql;
        }
        int queryLength = this.nativeSql.length();
        String[] params = new String[this.bindPositions.length];
        for (int i = 1; i <= this.bindPositions.length; ++i) {
            String param;
            params[i - 1] = param = parameters == null ? "?" : parameters.toString(i, true);
            queryLength += param.length() - NativeQuery.bindName(i).length();
        }
        StringBuilder sbuf = new StringBuilder(queryLength);
        sbuf.append(this.nativeSql, 0, this.bindPositions[0]);
        for (int i = 1; i <= this.bindPositions.length; ++i) {
            sbuf.append(params[i - 1]);
            int nextBind = i < this.bindPositions.length ? this.bindPositions[i] : this.nativeSql.length();
            sbuf.append(this.nativeSql, this.bindPositions[i - 1] + NativeQuery.bindName(i).length(), nextBind);
        }
        return sbuf.toString();
    }

    public static String bindName(int index) {
        return index < BIND_NAMES.length ? BIND_NAMES[index] : "$" + index;
    }

    public static StringBuilder appendBindName(StringBuilder sb, int index) {
        if (index < BIND_NAMES.length) {
            return sb.append(NativeQuery.bindName(index));
        }
        sb.append('$');
        sb.append(index);
        return sb;
    }

    public static int calculateBindLength(int bindCount) {
        int res = 0;
        int bindLen = 2;
        int maxBindsOfLen = 9;
        while (bindCount > 0) {
            int numBinds = Math.min(maxBindsOfLen, bindCount);
            bindCount -= numBinds;
            res += bindLen * numBinds;
            ++bindLen;
            maxBindsOfLen *= 10;
        }
        return res;
    }

    public SqlCommand getCommand() {
        return this.command;
    }

    static {
        for (int i = 1; i < BIND_NAMES.length; ++i) {
            NativeQuery.BIND_NAMES[i] = "$" + i;
        }
    }
}

