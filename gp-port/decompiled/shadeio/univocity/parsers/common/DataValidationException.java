/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common;

import shadeio.univocity.parsers.common.DataProcessingException;

public class DataValidationException
extends DataProcessingException {
    private static final long serialVersionUID = 3110975527111918123L;

    public DataValidationException(String message) {
        super(message, -1, null, null);
    }

    public DataValidationException(String message, Throwable cause) {
        super(message, -1, null, cause);
    }

    public DataValidationException(String message, Object[] row) {
        super(message, -1, row, null);
    }

    public DataValidationException(String message, Object[] row, Throwable cause) {
        super(message, -1, row, cause);
    }

    public DataValidationException(String message, int columnIndex) {
        super(message, columnIndex, null, null);
    }

    @Override
    protected String getErrorDescription() {
        return "Error validating parsed input";
    }
}

