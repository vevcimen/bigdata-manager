/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.util;

import java.lang.management.ManagementFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

public class PGPropertyMaxResultBufferParser {
    private static final Logger LOGGER = Logger.getLogger(PGPropertyMaxResultBufferParser.class.getName());
    private static final String[] PERCENT_PHRASES = new String[]{"p", "pct", "percent"};

    public static long parseProperty(@Nullable String value) throws PSQLException {
        long result = -1L;
        if (value != null) {
            if (PGPropertyMaxResultBufferParser.checkIfValueContainsPercent(value)) {
                result = PGPropertyMaxResultBufferParser.parseBytePercentValue(value);
            } else if (!value.isEmpty()) {
                result = PGPropertyMaxResultBufferParser.parseByteValue(value);
            }
        }
        result = PGPropertyMaxResultBufferParser.adjustResultSize(result);
        return result;
    }

    private static boolean checkIfValueContainsPercent(String value) {
        return PGPropertyMaxResultBufferParser.getPercentPhraseLengthIfContains(value) != -1;
    }

    private static long parseBytePercentValue(String value) throws PSQLException {
        long result = -1L;
        if (!value.isEmpty()) {
            int length = PGPropertyMaxResultBufferParser.getPercentPhraseLengthIfContains(value);
            if (length == -1) {
                PGPropertyMaxResultBufferParser.throwExceptionAboutParsingError("Received MaxResultBuffer parameter can't be parsed. Value received to parse: {0}", value);
            }
            result = PGPropertyMaxResultBufferParser.calculatePercentOfMemory(value, length);
        }
        return result;
    }

    private static int getPercentPhraseLengthIfContains(String valueToCheck) {
        int result = -1;
        for (String phrase : PERCENT_PHRASES) {
            int indx = PGPropertyMaxResultBufferParser.getPhraseLengthIfContains(valueToCheck, phrase);
            if (indx == -1) continue;
            result = indx;
        }
        return result;
    }

    private static int getPhraseLengthIfContains(String valueToCheck, String phrase) {
        String subValue;
        int searchValueLength = phrase.length();
        if (valueToCheck.length() > searchValueLength && (subValue = valueToCheck.substring(valueToCheck.length() - searchValueLength)).equals(phrase)) {
            return searchValueLength;
        }
        return -1;
    }

    private static long calculatePercentOfMemory(String value, int percentPhraseLength) {
        String realValue = value.substring(0, value.length() - percentPhraseLength);
        double percent = Double.parseDouble(realValue) / 100.0;
        long result = (long)(percent * (double)ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax());
        return result;
    }

    private static long parseByteValue(String value) throws PSQLException {
        long result = -1L;
        long multiplier = 1L;
        long mul = 1000L;
        char sign = value.charAt(value.length() - 1);
        switch (sign) {
            case 'T': 
            case 't': {
                multiplier *= mul;
            }
            case 'G': 
            case 'g': {
                multiplier *= mul;
            }
            case 'M': 
            case 'm': {
                multiplier *= mul;
            }
            case 'K': 
            case 'k': {
                String realValue = value.substring(0, value.length() - 1);
                result = (long)Integer.parseInt(realValue) * (multiplier *= mul);
                break;
            }
            case '%': {
                return result;
            }
            default: {
                if (sign >= '0' && sign <= '9') {
                    result = Long.parseLong(value);
                    break;
                }
                PGPropertyMaxResultBufferParser.throwExceptionAboutParsingError("Received MaxResultBuffer parameter can't be parsed. Value received to parse: {0}", value);
            }
        }
        return result;
    }

    private static long adjustResultSize(long value) {
        if ((double)value > 0.9 * (double)ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax()) {
            long newResult = (long)(0.9 * (double)ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax());
            LOGGER.log(Level.WARNING, GT.tr("WARNING! Required to allocate {0} bytes, which exceeded possible heap memory size. Assigned {1} bytes as limit.", String.valueOf(value), String.valueOf(newResult)));
            value = newResult;
        }
        return value;
    }

    private static void throwExceptionAboutParsingError(String message, Object ... values) throws PSQLException {
        throw new PSQLException(GT.tr(message, values), PSQLState.SYNTAX_ERROR);
    }
}

