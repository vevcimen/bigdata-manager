/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core;

import org.postgresql.core.SqlCommandType;

public class SqlCommand {
    public static final SqlCommand BLANK = SqlCommand.createStatementTypeInfo(SqlCommandType.BLANK);
    private final SqlCommandType commandType;
    private final boolean parsedSQLhasRETURNINGKeyword;
    private final int valuesBraceOpenPosition;
    private final int valuesBraceClosePosition;

    public boolean isBatchedReWriteCompatible() {
        return this.valuesBraceOpenPosition >= 0;
    }

    public int getBatchRewriteValuesBraceOpenPosition() {
        return this.valuesBraceOpenPosition;
    }

    public int getBatchRewriteValuesBraceClosePosition() {
        return this.valuesBraceClosePosition;
    }

    public SqlCommandType getType() {
        return this.commandType;
    }

    public boolean isReturningKeywordPresent() {
        return this.parsedSQLhasRETURNINGKeyword;
    }

    public boolean returnsRows() {
        return this.parsedSQLhasRETURNINGKeyword || this.commandType == SqlCommandType.SELECT || this.commandType == SqlCommandType.WITH;
    }

    public static SqlCommand createStatementTypeInfo(SqlCommandType type, boolean isBatchedReWritePropertyConfigured, int valuesBraceOpenPosition, int valuesBraceClosePosition, boolean isRETURNINGkeywordPresent, int priorQueryCount) {
        return new SqlCommand(type, isBatchedReWritePropertyConfigured, valuesBraceOpenPosition, valuesBraceClosePosition, isRETURNINGkeywordPresent, priorQueryCount);
    }

    public static SqlCommand createStatementTypeInfo(SqlCommandType type) {
        return new SqlCommand(type, false, -1, -1, false, 0);
    }

    public static SqlCommand createStatementTypeInfo(SqlCommandType type, boolean isRETURNINGkeywordPresent) {
        return new SqlCommand(type, false, -1, -1, isRETURNINGkeywordPresent, 0);
    }

    private SqlCommand(SqlCommandType type, boolean isBatchedReWriteConfigured, int valuesBraceOpenPosition, int valuesBraceClosePosition, boolean isPresent, int priorQueryCount) {
        this.commandType = type;
        this.parsedSQLhasRETURNINGKeyword = isPresent;
        boolean batchedReWriteCompatible = type == SqlCommandType.INSERT && isBatchedReWriteConfigured && valuesBraceOpenPosition >= 0 && valuesBraceClosePosition > valuesBraceOpenPosition && !isPresent && priorQueryCount == 0;
        this.valuesBraceOpenPosition = batchedReWriteCompatible ? valuesBraceOpenPosition : -1;
        this.valuesBraceClosePosition = batchedReWriteCompatible ? valuesBraceClosePosition : -1;
    }
}

