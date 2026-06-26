/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common;

import shadeio.univocity.parsers.common.AbstractException;

public class TextWritingException
extends AbstractException {
    private static final long serialVersionUID = 7198462597717255519L;
    private final long recordCount;
    private final Object[] recordData;
    private final String recordCharacters;

    private TextWritingException(String message, long recordCount, Object[] row, String recordCharacters, Throwable cause) {
        super(message, cause);
        this.recordCount = recordCount;
        this.recordData = row;
        this.recordCharacters = recordCharacters;
    }

    public TextWritingException(String message, long recordCount, String recordCharacters, Throwable cause) {
        this(message, recordCount, null, recordCharacters, cause);
    }

    public TextWritingException(String message, long recordCount, Object[] row, Throwable cause) {
        this(message, recordCount, row, null, cause);
    }

    public TextWritingException(String message) {
        this(message, 0L, null, null, null);
    }

    public TextWritingException(Throwable cause) {
        this(cause != null ? cause.getMessage() : null, 0L, null, null, cause);
    }

    public TextWritingException(String message, long line, Object[] row) {
        this(message, line, row, null);
    }

    public TextWritingException(String message, long line, String recordCharacters) {
        this(message, line, null, recordCharacters, null);
    }

    public long getRecordCount() {
        return this.recordCount;
    }

    public Object[] getRecordData() {
        return this.restrictContent(this.recordData);
    }

    public String getRecordCharacters() {
        if (this.errorContentLength == 0) {
            return null;
        }
        return this.recordCharacters;
    }

    @Override
    protected String getDetails() {
        String details = "";
        details = TextWritingException.printIfNotEmpty(details, "recordCount", this.recordCount);
        details = TextWritingException.printIfNotEmpty(details, "recordData", this.restrictContent(this.recordData));
        details = TextWritingException.printIfNotEmpty(details, "recordCharacters", this.restrictContent(this.recordCharacters));
        return details;
    }

    @Override
    protected String getErrorDescription() {
        return "Error writing data";
    }
}

