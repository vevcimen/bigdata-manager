/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.core.EncodingPredictor;
import org.postgresql.util.GT;

public class ServerErrorMessage
implements Serializable {
    private static final Logger LOGGER = Logger.getLogger(ServerErrorMessage.class.getName());
    private static final Character SEVERITY = Character.valueOf('S');
    private static final Character MESSAGE = Character.valueOf('M');
    private static final Character DETAIL = Character.valueOf('D');
    private static final Character HINT = Character.valueOf('H');
    private static final Character POSITION = Character.valueOf('P');
    private static final Character WHERE = Character.valueOf('W');
    private static final Character FILE = Character.valueOf('F');
    private static final Character LINE = Character.valueOf('L');
    private static final Character ROUTINE = Character.valueOf('R');
    private static final Character SQLSTATE = Character.valueOf('C');
    private static final Character INTERNAL_POSITION = Character.valueOf('p');
    private static final Character INTERNAL_QUERY = Character.valueOf('q');
    private static final Character SCHEMA = Character.valueOf('s');
    private static final Character TABLE = Character.valueOf('t');
    private static final Character COLUMN = Character.valueOf('c');
    private static final Character DATATYPE = Character.valueOf('d');
    private static final Character CONSTRAINT = Character.valueOf('n');
    private final Map<Character, String> mesgParts = new HashMap<Character, String>();

    public ServerErrorMessage(EncodingPredictor.DecodeResult serverError) {
        this(serverError.result);
        if (serverError.encoding != null) {
            this.mesgParts.put(MESSAGE, this.mesgParts.get(MESSAGE) + GT.tr(" (pgjdbc: autodetected server-encoding to be {0}, if the message is not readable, please check database logs and/or host, port, dbname, user, password, pg_hba.conf)", serverError.encoding));
        }
    }

    public ServerErrorMessage(String serverError) {
        char[] chars = serverError.toCharArray();
        int length = chars.length;
        for (int pos = 0; pos < length; ++pos) {
            char mesgType = chars[pos];
            if (mesgType == '\u0000') continue;
            int startString = ++pos;
            while (pos < length && chars[pos] != '\u0000') {
                ++pos;
            }
            String mesgPart = new String(chars, startString, pos - startString);
            this.mesgParts.put(Character.valueOf(mesgType), mesgPart);
        }
    }

    public @Nullable String getSQLState() {
        return this.mesgParts.get(SQLSTATE);
    }

    public @Nullable String getMessage() {
        return this.mesgParts.get(MESSAGE);
    }

    public @Nullable String getSeverity() {
        return this.mesgParts.get(SEVERITY);
    }

    public @Nullable String getDetail() {
        return this.mesgParts.get(DETAIL);
    }

    public @Nullable String getHint() {
        return this.mesgParts.get(HINT);
    }

    public int getPosition() {
        return this.getIntegerPart(POSITION);
    }

    public @Nullable String getWhere() {
        return this.mesgParts.get(WHERE);
    }

    public @Nullable String getSchema() {
        return this.mesgParts.get(SCHEMA);
    }

    public @Nullable String getTable() {
        return this.mesgParts.get(TABLE);
    }

    public @Nullable String getColumn() {
        return this.mesgParts.get(COLUMN);
    }

    public @Nullable String getDatatype() {
        return this.mesgParts.get(DATATYPE);
    }

    public @Nullable String getConstraint() {
        return this.mesgParts.get(CONSTRAINT);
    }

    public @Nullable String getFile() {
        return this.mesgParts.get(FILE);
    }

    public int getLine() {
        return this.getIntegerPart(LINE);
    }

    public @Nullable String getRoutine() {
        return this.mesgParts.get(ROUTINE);
    }

    public @Nullable String getInternalQuery() {
        return this.mesgParts.get(INTERNAL_QUERY);
    }

    public int getInternalPosition() {
        return this.getIntegerPart(INTERNAL_POSITION);
    }

    private int getIntegerPart(Character c) {
        String s2 = this.mesgParts.get(c);
        if (s2 == null) {
            return 0;
        }
        return Integer.parseInt(s2);
    }

    String getNonSensitiveErrorMessage() {
        StringBuilder totalMessage = new StringBuilder();
        String message = this.mesgParts.get(SEVERITY);
        if (message != null) {
            totalMessage.append(message).append(": ");
        }
        if ((message = this.mesgParts.get(MESSAGE)) != null) {
            totalMessage.append(message);
        }
        return totalMessage.toString();
    }

    public String toString() {
        StringBuilder totalMessage = new StringBuilder();
        String message = this.mesgParts.get(SEVERITY);
        if (message != null) {
            totalMessage.append(message).append(": ");
        }
        if ((message = this.mesgParts.get(MESSAGE)) != null) {
            totalMessage.append(message);
        }
        if ((message = this.mesgParts.get(DETAIL)) != null) {
            totalMessage.append("\n  ").append(GT.tr("Detail: {0}", message));
        }
        if ((message = this.mesgParts.get(HINT)) != null) {
            totalMessage.append("\n  ").append(GT.tr("Hint: {0}", message));
        }
        if ((message = this.mesgParts.get(POSITION)) != null) {
            totalMessage.append("\n  ").append(GT.tr("Position: {0}", message));
        }
        if ((message = this.mesgParts.get(WHERE)) != null) {
            totalMessage.append("\n  ").append(GT.tr("Where: {0}", message));
        }
        if (LOGGER.isLoggable(Level.FINEST)) {
            String internalPosition;
            String internalQuery = this.mesgParts.get(INTERNAL_QUERY);
            if (internalQuery != null) {
                totalMessage.append("\n  ").append(GT.tr("Internal Query: {0}", internalQuery));
            }
            if ((internalPosition = this.mesgParts.get(INTERNAL_POSITION)) != null) {
                totalMessage.append("\n  ").append(GT.tr("Internal Position: {0}", internalPosition));
            }
            String file = this.mesgParts.get(FILE);
            String line = this.mesgParts.get(LINE);
            String routine = this.mesgParts.get(ROUTINE);
            if (file != null || line != null || routine != null) {
                totalMessage.append("\n  ").append(GT.tr("Location: File: {0}, Routine: {1}, Line: {2}", file, routine, line));
            }
            if ((message = this.mesgParts.get(SQLSTATE)) != null) {
                totalMessage.append("\n  ").append(GT.tr("Server SQLState: {0}", message));
            }
        }
        return totalMessage.toString();
    }
}

