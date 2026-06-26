/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.jdbc;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

class BooleanTypeUtil {
    private static final Logger LOGGER = Logger.getLogger(BooleanTypeUtil.class.getName());

    private BooleanTypeUtil() {
    }

    static boolean castToBoolean(Object in) throws PSQLException {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Cast to boolean: \"{0}\"", String.valueOf(in));
        }
        if (in instanceof Boolean) {
            return (Boolean)in;
        }
        if (in instanceof String) {
            return BooleanTypeUtil.fromString((String)in);
        }
        if (in instanceof Character) {
            return BooleanTypeUtil.fromCharacter((Character)in);
        }
        if (in instanceof Number) {
            return BooleanTypeUtil.fromNumber((Number)in);
        }
        throw new PSQLException("Cannot cast to boolean", PSQLState.CANNOT_COERCE);
    }

    static boolean fromString(String strval) throws PSQLException {
        String val = strval.trim();
        if ("1".equals(val) || "true".equalsIgnoreCase(val) || "t".equalsIgnoreCase(val) || "yes".equalsIgnoreCase(val) || "y".equalsIgnoreCase(val) || "on".equalsIgnoreCase(val)) {
            return true;
        }
        if ("0".equals(val) || "false".equalsIgnoreCase(val) || "f".equalsIgnoreCase(val) || "no".equalsIgnoreCase(val) || "n".equalsIgnoreCase(val) || "off".equalsIgnoreCase(val)) {
            return false;
        }
        throw BooleanTypeUtil.cannotCoerceException(strval);
    }

    private static boolean fromCharacter(Character charval) throws PSQLException {
        if ('1' == charval.charValue() || 't' == charval.charValue() || 'T' == charval.charValue() || 'y' == charval.charValue() || 'Y' == charval.charValue()) {
            return true;
        }
        if ('0' == charval.charValue() || 'f' == charval.charValue() || 'F' == charval.charValue() || 'n' == charval.charValue() || 'N' == charval.charValue()) {
            return false;
        }
        throw BooleanTypeUtil.cannotCoerceException(charval);
    }

    private static boolean fromNumber(Number numval) throws PSQLException {
        double value = numval.doubleValue();
        if (value == 1.0) {
            return true;
        }
        if (value == 0.0) {
            return false;
        }
        throw BooleanTypeUtil.cannotCoerceException(numval);
    }

    private static PSQLException cannotCoerceException(Object value) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Cannot cast to boolean: \"{0}\"", String.valueOf(value));
        }
        return new PSQLException(GT.tr("Cannot cast to boolean: \"{0}\"", String.valueOf(value)), PSQLState.CANNOT_COERCE);
    }
}

