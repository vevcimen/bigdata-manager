/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common;

import shadeio.univocity.parsers.common.AbstractException;
import shadeio.univocity.parsers.common.Context;
import shadeio.univocity.parsers.common.ParsingContext;

public class TextParsingException
extends AbstractException {
    private static final long serialVersionUID = 1410975527141918214L;
    private long lineIndex;
    private long charIndex;
    private long recordNumber;
    private int columnIndex;
    private String content;
    private String[] headers;
    protected int[] extractedIndexes;

    public TextParsingException(Context context, String message, Throwable cause) {
        super(message, cause);
        this.setContext(context);
    }

    protected void setContext(Context context) {
        if (context instanceof ParsingContext) {
            this.setParsingContext((ParsingContext)context);
        } else {
            this.setParsingContext(null);
        }
        this.columnIndex = context == null ? -1 : context.currentColumn();
        long l = this.recordNumber = context == null ? -1L : context.currentRecord();
        if (this.headers == null) {
            this.headers = context == null ? null : context.headers();
        }
        this.extractedIndexes = context == null ? null : context.extractedFieldIndexes();
    }

    private void setParsingContext(ParsingContext parsingContext) {
        this.lineIndex = parsingContext == null ? -1L : parsingContext.currentLine();
        this.charIndex = parsingContext == null ? 0L : parsingContext.currentChar();
        this.content = parsingContext == null ? null : parsingContext.fieldContentOnError();
    }

    public TextParsingException(ParsingContext context, String message) {
        this(context, message, null);
    }

    public TextParsingException(ParsingContext context, Throwable cause) {
        this(context, cause != null ? cause.getMessage() : null, cause);
    }

    public TextParsingException(ParsingContext context) {
        this(context, null, null);
    }

    @Override
    protected String getErrorDescription() {
        return "Error parsing input";
    }

    @Override
    protected String getDetails() {
        String details = "";
        details = TextParsingException.printIfNotEmpty(details, "line", this.lineIndex);
        details = TextParsingException.printIfNotEmpty(details, "column", this.columnIndex);
        details = TextParsingException.printIfNotEmpty(details, "record", this.recordNumber);
        details = this.charIndex == 0L ? details : TextParsingException.printIfNotEmpty(details, "charIndex", this.charIndex);
        details = TextParsingException.printIfNotEmpty(details, "headers", this.headers);
        details = TextParsingException.printIfNotEmpty(details, "content parsed", this.restrictContent(this.content));
        return details;
    }

    public long getRecordNumber() {
        return this.recordNumber;
    }

    public int getColumnIndex() {
        return this.columnIndex;
    }

    public long getLineIndex() {
        return this.lineIndex;
    }

    public long getCharIndex() {
        return this.charIndex;
    }

    public final String getParsedContent() {
        if (this.errorContentLength == 0) {
            return null;
        }
        return this.content;
    }

    public final String[] getHeaders() {
        return this.headers;
    }
}

