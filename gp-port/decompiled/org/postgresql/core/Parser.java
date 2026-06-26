/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.core.JdbcCallParseInfo;
import org.postgresql.core.NativeQuery;
import org.postgresql.core.SqlCommand;
import org.postgresql.core.SqlCommandType;
import org.postgresql.core.Utils;
import org.postgresql.jdbc.EscapeSyntaxCallMode;
import org.postgresql.jdbc.EscapedFunctions2;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

public class Parser {
    private static final int[] NO_BINDS = new int[0];
    private static final char[] QUOTE_OR_ALPHABETIC_MARKER = new char[]{'\"', '0'};
    private static final char[] QUOTE_OR_ALPHABETIC_MARKER_OR_PARENTHESIS = new char[]{'\"', '0', '('};
    private static final char[] SINGLE_QUOTE = new char[]{'\''};

    public static List<NativeQuery> parseJdbcSql(String query, boolean standardConformingStrings, boolean withParameters, boolean splitStatements, boolean isBatchedReWriteConfigured, boolean quoteReturningIdentifiers, String ... returningColumnNames) throws SQLException {
        if (!withParameters && !splitStatements && returningColumnNames != null && returningColumnNames.length == 0) {
            return Collections.singletonList(new NativeQuery(query, SqlCommand.createStatementTypeInfo(SqlCommandType.BLANK)));
        }
        int fragmentStart = 0;
        int inParen = 0;
        char[] aChars = query.toCharArray();
        StringBuilder nativeSql = new StringBuilder(query.length() + 10);
        ArrayList<Integer> bindPositions = null;
        List<NativeQuery> nativeQueries = null;
        boolean isCurrentReWriteCompatible = false;
        boolean isValuesFound = false;
        int valuesBraceOpenPosition = -1;
        int valuesBraceClosePosition = -1;
        boolean valuesBraceCloseFound = false;
        boolean isInsertPresent = false;
        boolean isReturningPresent = false;
        boolean isReturningPresentPrev = false;
        SqlCommandType currentCommandType = SqlCommandType.BLANK;
        SqlCommandType prevCommandType = SqlCommandType.BLANK;
        int numberOfStatements = 0;
        boolean whitespaceOnly = true;
        int keyWordCount = 0;
        int keywordStart = -1;
        int keywordEnd = -1;
        for (int i = 0; i < aChars.length; ++i) {
            char aChar = aChars[i];
            boolean isKeyWordChar = false;
            whitespaceOnly &= aChar == ';' || Character.isWhitespace(aChar);
            keywordEnd = i;
            switch (aChar) {
                case '\'': {
                    i = Parser.parseSingleQuotes(aChars, i, standardConformingStrings);
                    break;
                }
                case '\"': {
                    i = Parser.parseDoubleQuotes(aChars, i);
                    break;
                }
                case '-': {
                    i = Parser.parseLineComment(aChars, i);
                    break;
                }
                case '/': {
                    i = Parser.parseBlockComment(aChars, i);
                    break;
                }
                case '$': {
                    i = Parser.parseDollarQuotes(aChars, i);
                    break;
                }
                case ')': {
                    if (--inParen != 0 || !isValuesFound || valuesBraceCloseFound) break;
                    valuesBraceClosePosition = nativeSql.length() + i - fragmentStart;
                    break;
                }
                case '?': {
                    nativeSql.append(aChars, fragmentStart, i - fragmentStart);
                    if (i + 1 < aChars.length && aChars[i + 1] == '?') {
                        nativeSql.append('?');
                        ++i;
                    } else if (!withParameters) {
                        nativeSql.append('?');
                    } else {
                        if (bindPositions == null) {
                            bindPositions = new ArrayList<Integer>();
                        }
                        bindPositions.add(nativeSql.length());
                        int bindIndex = bindPositions.size();
                        nativeSql.append(NativeQuery.bindName(bindIndex));
                    }
                    fragmentStart = i + 1;
                    break;
                }
                case ';': {
                    if (inParen != 0) break;
                    if (!whitespaceOnly) {
                        ++numberOfStatements;
                        nativeSql.append(aChars, fragmentStart, i - fragmentStart);
                        whitespaceOnly = true;
                    }
                    fragmentStart = i + 1;
                    if (nativeSql.length() > 0) {
                        if (Parser.addReturning(nativeSql, currentCommandType, returningColumnNames, isReturningPresent, quoteReturningIdentifiers)) {
                            isReturningPresent = true;
                        }
                        if (splitStatements) {
                            if (nativeQueries == null) {
                                nativeQueries = new ArrayList<NativeQuery>();
                            }
                            if (!isValuesFound || !isCurrentReWriteCompatible || valuesBraceClosePosition == -1 || bindPositions != null && valuesBraceClosePosition < (Integer)bindPositions.get(bindPositions.size() - 1)) {
                                valuesBraceOpenPosition = -1;
                                valuesBraceClosePosition = -1;
                            }
                            nativeQueries.add(new NativeQuery(nativeSql.toString(), Parser.toIntArray(bindPositions), false, SqlCommand.createStatementTypeInfo(currentCommandType, isBatchedReWriteConfigured, valuesBraceOpenPosition, valuesBraceClosePosition, isReturningPresent, nativeQueries.size())));
                        }
                    }
                    prevCommandType = currentCommandType;
                    isReturningPresentPrev = isReturningPresent;
                    currentCommandType = SqlCommandType.BLANK;
                    isReturningPresent = false;
                    if (!splitStatements) break;
                    if (bindPositions != null) {
                        bindPositions.clear();
                    }
                    nativeSql.setLength(0);
                    isValuesFound = false;
                    isCurrentReWriteCompatible = false;
                    valuesBraceOpenPosition = -1;
                    valuesBraceClosePosition = -1;
                    valuesBraceCloseFound = false;
                    break;
                }
                default: {
                    if (keywordStart >= 0) {
                        isKeyWordChar = Parser.isIdentifierContChar(aChar);
                        break;
                    }
                    isKeyWordChar = Parser.isIdentifierStartChar(aChar);
                    if (!isKeyWordChar) break;
                    keywordStart = i;
                    if (valuesBraceOpenPosition == -1 || inParen != 0) break;
                    valuesBraceCloseFound = true;
                }
            }
            if (!(keywordStart < 0 || i != aChars.length - 1 && isKeyWordChar)) {
                SqlCommandType command;
                int wordLength = (isKeyWordChar ? i + 1 : keywordEnd) - keywordStart;
                if (currentCommandType == SqlCommandType.BLANK) {
                    if (wordLength == 6 && Parser.parseCreateKeyword(aChars, keywordStart)) {
                        currentCommandType = SqlCommandType.CREATE;
                    } else if (wordLength == 5 && Parser.parseAlterKeyword(aChars, keywordStart)) {
                        currentCommandType = SqlCommandType.ALTER;
                    } else if (wordLength == 6 && Parser.parseUpdateKeyword(aChars, keywordStart)) {
                        currentCommandType = SqlCommandType.UPDATE;
                    } else if (wordLength == 6 && Parser.parseDeleteKeyword(aChars, keywordStart)) {
                        currentCommandType = SqlCommandType.DELETE;
                    } else if (wordLength == 4 && Parser.parseMoveKeyword(aChars, keywordStart)) {
                        currentCommandType = SqlCommandType.MOVE;
                    } else if (wordLength == 6 && Parser.parseSelectKeyword(aChars, keywordStart)) {
                        currentCommandType = SqlCommandType.SELECT;
                    } else if (wordLength == 4 && Parser.parseWithKeyword(aChars, keywordStart)) {
                        currentCommandType = SqlCommandType.WITH;
                    } else if (wordLength == 6 && Parser.parseInsertKeyword(aChars, keywordStart)) {
                        if (!isInsertPresent && (nativeQueries == null || nativeQueries.isEmpty())) {
                            isCurrentReWriteCompatible = keyWordCount == 0;
                            isInsertPresent = true;
                            currentCommandType = SqlCommandType.INSERT;
                        } else {
                            isCurrentReWriteCompatible = false;
                        }
                    }
                } else if (currentCommandType == SqlCommandType.WITH && inParen == 0 && (command = Parser.parseWithCommandType(aChars, i, keywordStart, wordLength)) != null) {
                    currentCommandType = command;
                }
                if (inParen == 0 && aChar != ')') {
                    if (wordLength == 9 && Parser.parseReturningKeyword(aChars, keywordStart)) {
                        isReturningPresent = true;
                    } else if (wordLength == 6 && Parser.parseValuesKeyword(aChars, keywordStart)) {
                        isValuesFound = true;
                    }
                }
                keywordStart = -1;
                ++keyWordCount;
            }
            if (aChar != '(' || ++inParen != 1 || !isValuesFound || valuesBraceOpenPosition != -1) continue;
            valuesBraceOpenPosition = nativeSql.length() + i - fragmentStart;
        }
        if (!isValuesFound || !isCurrentReWriteCompatible || valuesBraceClosePosition == -1 || bindPositions != null && valuesBraceClosePosition < (Integer)bindPositions.get(bindPositions.size() - 1)) {
            valuesBraceOpenPosition = -1;
            valuesBraceClosePosition = -1;
        }
        if (fragmentStart < aChars.length && !whitespaceOnly) {
            nativeSql.append(aChars, fragmentStart, aChars.length - fragmentStart);
        } else if (numberOfStatements > 1) {
            isReturningPresent = false;
            currentCommandType = SqlCommandType.BLANK;
        } else if (numberOfStatements == 1) {
            isReturningPresent = isReturningPresentPrev;
            currentCommandType = prevCommandType;
        }
        if (nativeSql.length() == 0) {
            return nativeQueries != null ? nativeQueries : Collections.emptyList();
        }
        if (Parser.addReturning(nativeSql, currentCommandType, returningColumnNames, isReturningPresent, quoteReturningIdentifiers)) {
            isReturningPresent = true;
        }
        NativeQuery lastQuery = new NativeQuery(nativeSql.toString(), Parser.toIntArray((List<Integer>)bindPositions), !splitStatements, SqlCommand.createStatementTypeInfo(currentCommandType, isBatchedReWriteConfigured, valuesBraceOpenPosition, valuesBraceClosePosition, isReturningPresent, nativeQueries == null ? 0 : nativeQueries.size()));
        if (nativeQueries == null) {
            return Collections.singletonList(lastQuery);
        }
        if (!whitespaceOnly) {
            nativeQueries.add(lastQuery);
        }
        return nativeQueries;
    }

    private static @Nullable SqlCommandType parseWithCommandType(char[] aChars, int i, int keywordStart, int wordLength) {
        int nextInd;
        SqlCommandType command;
        if (wordLength == 6 && Parser.parseUpdateKeyword(aChars, keywordStart)) {
            command = SqlCommandType.UPDATE;
        } else if (wordLength == 6 && Parser.parseDeleteKeyword(aChars, keywordStart)) {
            command = SqlCommandType.DELETE;
        } else if (wordLength == 6 && Parser.parseInsertKeyword(aChars, keywordStart)) {
            command = SqlCommandType.INSERT;
        } else if (wordLength == 6 && Parser.parseSelectKeyword(aChars, keywordStart)) {
            command = SqlCommandType.SELECT;
        } else {
            return null;
        }
        for (nextInd = i; nextInd < aChars.length; ++nextInd) {
            char nextChar = aChars[nextInd];
            if (nextChar == '-') {
                nextInd = Parser.parseLineComment(aChars, nextInd);
                continue;
            }
            if (nextChar == '/') {
                nextInd = Parser.parseBlockComment(aChars, nextInd);
                continue;
            }
            if (!Character.isWhitespace(nextChar)) break;
        }
        if (nextInd + 2 >= aChars.length || !Parser.parseAsKeyword(aChars, nextInd) || Parser.isIdentifierContChar(aChars[nextInd + 2])) {
            return command;
        }
        return null;
    }

    private static boolean addReturning(StringBuilder nativeSql, SqlCommandType currentCommandType, String[] returningColumnNames, boolean isReturningPresent, boolean quoteReturningIdentifiers) throws SQLException {
        if (isReturningPresent || returningColumnNames.length == 0) {
            return false;
        }
        if (currentCommandType != SqlCommandType.INSERT && currentCommandType != SqlCommandType.UPDATE && currentCommandType != SqlCommandType.DELETE && currentCommandType != SqlCommandType.WITH) {
            return false;
        }
        nativeSql.append("\nRETURNING ");
        if (returningColumnNames.length == 1 && returningColumnNames[0].charAt(0) == '*') {
            nativeSql.append('*');
            return true;
        }
        for (int col = 0; col < returningColumnNames.length; ++col) {
            String columnName = returningColumnNames[col];
            if (col > 0) {
                nativeSql.append(", ");
            }
            if (quoteReturningIdentifiers) {
                Utils.escapeIdentifier(nativeSql, columnName);
                continue;
            }
            nativeSql.append(columnName);
        }
        return true;
    }

    private static int[] toIntArray(@Nullable List<Integer> list) {
        if (list == null || list.isEmpty()) {
            return NO_BINDS;
        }
        int[] res = new int[list.size()];
        for (int i = 0; i < list.size(); ++i) {
            res[i] = list.get(i);
        }
        return res;
    }

    public static int parseSingleQuotes(char[] query, int offset, boolean standardConformingStrings) {
        if (standardConformingStrings && offset >= 2 && (query[offset - 1] == 'e' || query[offset - 1] == 'E') && Parser.charTerminatesIdentifier(query[offset - 2])) {
            standardConformingStrings = false;
        }
        if (standardConformingStrings) {
            while (++offset < query.length) {
                switch (query[offset]) {
                    case '\'': {
                        return offset;
                    }
                }
            }
        } else {
            block8: while (++offset < query.length) {
                switch (query[offset]) {
                    case '\\': {
                        ++offset;
                        continue block8;
                    }
                    case '\'': {
                        return offset;
                    }
                }
            }
        }
        return query.length;
    }

    public static int parseDoubleQuotes(char[] query, int offset) {
        while (++offset < query.length && query[offset] != '\"') {
        }
        return offset;
    }

    public static int parseDollarQuotes(char[] query, int offset) {
        if (!(offset + 1 >= query.length || offset != 0 && Parser.isIdentifierContChar(query[offset - 1]))) {
            int endIdx = -1;
            if (query[offset + 1] == '$') {
                endIdx = offset + 1;
            } else if (Parser.isDollarQuoteStartChar(query[offset + 1])) {
                for (int d = offset + 2; d < query.length; ++d) {
                    if (query[d] == '$') {
                        endIdx = d;
                        break;
                    }
                    if (!Parser.isDollarQuoteContChar(query[d])) break;
                }
            }
            if (endIdx > 0) {
                int tagIdx = offset;
                int tagLen = endIdx - offset + 1;
                offset = endIdx;
                ++offset;
                while (offset < query.length) {
                    if (query[offset] == '$' && Parser.subArraysEqual(query, tagIdx, offset, tagLen)) {
                        offset += tagLen - 1;
                        break;
                    }
                    ++offset;
                }
            }
        }
        return offset;
    }

    public static int parseLineComment(char[] query, int offset) {
        block1: {
            if (offset + 1 >= query.length || query[offset + 1] != '-') break block1;
            while (offset + 1 < query.length && query[++offset] != '\r' && query[offset] != '\n') {
            }
        }
        return offset;
    }

    public static int parseBlockComment(char[] query, int offset) {
        if (offset + 1 < query.length && query[offset + 1] == '*') {
            int level = 1;
            offset += 2;
            while (offset < query.length) {
                switch (query[offset - 1]) {
                    case '*': {
                        if (query[offset] != '/') break;
                        --level;
                        ++offset;
                        break;
                    }
                    case '/': {
                        if (query[offset] != '*') break;
                        ++level;
                        ++offset;
                        break;
                    }
                }
                if (level == 0) {
                    --offset;
                    break;
                }
                ++offset;
            }
        }
        return offset;
    }

    public static boolean parseDeleteKeyword(char[] query, int offset) {
        if (query.length < offset + 6) {
            return false;
        }
        return (query[offset] | 0x20) == 100 && (query[offset + 1] | 0x20) == 101 && (query[offset + 2] | 0x20) == 108 && (query[offset + 3] | 0x20) == 101 && (query[offset + 4] | 0x20) == 116 && (query[offset + 5] | 0x20) == 101;
    }

    public static boolean parseInsertKeyword(char[] query, int offset) {
        if (query.length < offset + 7) {
            return false;
        }
        return (query[offset] | 0x20) == 105 && (query[offset + 1] | 0x20) == 110 && (query[offset + 2] | 0x20) == 115 && (query[offset + 3] | 0x20) == 101 && (query[offset + 4] | 0x20) == 114 && (query[offset + 5] | 0x20) == 116;
    }

    public static boolean parseMoveKeyword(char[] query, int offset) {
        if (query.length < offset + 4) {
            return false;
        }
        return (query[offset] | 0x20) == 109 && (query[offset + 1] | 0x20) == 111 && (query[offset + 2] | 0x20) == 118 && (query[offset + 3] | 0x20) == 101;
    }

    public static boolean parseReturningKeyword(char[] query, int offset) {
        if (query.length < offset + 9) {
            return false;
        }
        return (query[offset] | 0x20) == 114 && (query[offset + 1] | 0x20) == 101 && (query[offset + 2] | 0x20) == 116 && (query[offset + 3] | 0x20) == 117 && (query[offset + 4] | 0x20) == 114 && (query[offset + 5] | 0x20) == 110 && (query[offset + 6] | 0x20) == 105 && (query[offset + 7] | 0x20) == 110 && (query[offset + 8] | 0x20) == 103;
    }

    public static boolean parseSelectKeyword(char[] query, int offset) {
        if (query.length < offset + 6) {
            return false;
        }
        return (query[offset] | 0x20) == 115 && (query[offset + 1] | 0x20) == 101 && (query[offset + 2] | 0x20) == 108 && (query[offset + 3] | 0x20) == 101 && (query[offset + 4] | 0x20) == 99 && (query[offset + 5] | 0x20) == 116;
    }

    public static boolean parseAlterKeyword(char[] query, int offset) {
        if (query.length < offset + 5) {
            return false;
        }
        return (query[offset] | 0x20) == 97 && (query[offset + 1] | 0x20) == 108 && (query[offset + 2] | 0x20) == 116 && (query[offset + 3] | 0x20) == 101 && (query[offset + 4] | 0x20) == 114;
    }

    public static boolean parseCreateKeyword(char[] query, int offset) {
        if (query.length < offset + 6) {
            return false;
        }
        return (query[offset] | 0x20) == 99 && (query[offset + 1] | 0x20) == 114 && (query[offset + 2] | 0x20) == 101 && (query[offset + 3] | 0x20) == 97 && (query[offset + 4] | 0x20) == 116 && (query[offset + 5] | 0x20) == 101;
    }

    public static boolean parseUpdateKeyword(char[] query, int offset) {
        if (query.length < offset + 6) {
            return false;
        }
        return (query[offset] | 0x20) == 117 && (query[offset + 1] | 0x20) == 112 && (query[offset + 2] | 0x20) == 100 && (query[offset + 3] | 0x20) == 97 && (query[offset + 4] | 0x20) == 116 && (query[offset + 5] | 0x20) == 101;
    }

    public static boolean parseValuesKeyword(char[] query, int offset) {
        if (query.length < offset + 6) {
            return false;
        }
        return (query[offset] | 0x20) == 118 && (query[offset + 1] | 0x20) == 97 && (query[offset + 2] | 0x20) == 108 && (query[offset + 3] | 0x20) == 117 && (query[offset + 4] | 0x20) == 101 && (query[offset + 5] | 0x20) == 115;
    }

    public static long parseLong(String s2, int beginIndex, int endIndex) {
        if (endIndex - beginIndex > 16) {
            return Long.parseLong(s2.substring(beginIndex, endIndex));
        }
        long res = Parser.digitAt(s2, beginIndex);
        ++beginIndex;
        while (beginIndex < endIndex) {
            res = res * 10L + (long)Parser.digitAt(s2, beginIndex);
            ++beginIndex;
        }
        return res;
    }

    public static boolean parseWithKeyword(char[] query, int offset) {
        if (query.length < offset + 4) {
            return false;
        }
        return (query[offset] | 0x20) == 119 && (query[offset + 1] | 0x20) == 105 && (query[offset + 2] | 0x20) == 116 && (query[offset + 3] | 0x20) == 104;
    }

    public static boolean parseAsKeyword(char[] query, int offset) {
        if (query.length < offset + 2) {
            return false;
        }
        return (query[offset] | 0x20) == 97 && (query[offset + 1] | 0x20) == 115;
    }

    public static boolean isDigitAt(String s2, int pos) {
        return pos > 0 && pos < s2.length() && Character.isDigit(s2.charAt(pos));
    }

    public static int digitAt(String s2, int pos) {
        int c = s2.charAt(pos) - 48;
        if (c < 0 || c > 9) {
            throw new NumberFormatException("Input string: \"" + s2 + "\", position: " + pos);
        }
        return c;
    }

    public static boolean isSpace(char c) {
        return c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == '\f';
    }

    public static boolean isArrayWhiteSpace(char c) {
        return c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == '\f' || c == '\u000b';
    }

    public static boolean isOperatorChar(char c) {
        return ",()[].;:+-*/%^<>=~!@#&|`?".indexOf(c) != -1;
    }

    public static boolean isIdentifierStartChar(char c) {
        return Character.isJavaIdentifierStart(c);
    }

    public static boolean isIdentifierContChar(char c) {
        return Character.isJavaIdentifierPart(c);
    }

    public static boolean charTerminatesIdentifier(char c) {
        return c == '\"' || Parser.isSpace(c) || Parser.isOperatorChar(c);
    }

    public static boolean isDollarQuoteStartChar(char c) {
        return c != '$' && Parser.isIdentifierStartChar(c);
    }

    public static boolean isDollarQuoteContChar(char c) {
        return c != '$' && Parser.isIdentifierContChar(c);
    }

    private static boolean subArraysEqual(char[] arr, int offA, int offB, int len) {
        if (offA < 0 || offB < 0 || offA >= arr.length || offB >= arr.length || offA + len > arr.length || offB + len > arr.length) {
            return false;
        }
        for (int i = 0; i < len; ++i) {
            if (arr[offA + i] == arr[offB + i]) continue;
            return false;
        }
        return true;
    }

    public static JdbcCallParseInfo modifyJdbcCall(String jdbcSql, boolean stdStrings, int serverVersion, int protocolVersion, EscapeSyntaxCallMode escapeSyntaxCallMode) throws SQLException {
        String suffix;
        String prefix;
        char ch;
        String sql = jdbcSql;
        boolean isFunction = false;
        boolean outParamBeforeFunc = false;
        int len = jdbcSql.length();
        int state = 1;
        boolean inQuotes = false;
        boolean inEscape = false;
        int startIndex = -1;
        int endIndex = -1;
        boolean syntaxError = false;
        int i = 0;
        block10: while (i < len && !syntaxError) {
            ch = jdbcSql.charAt(i);
            switch (state) {
                case 1: {
                    if (ch == '{') {
                        ++i;
                        ++state;
                        continue block10;
                    }
                    if (Character.isWhitespace(ch)) {
                        ++i;
                        continue block10;
                    }
                    i = len;
                    continue block10;
                }
                case 2: {
                    if (ch == '?') {
                        isFunction = true;
                        outParamBeforeFunc = true;
                        ++i;
                        ++state;
                        continue block10;
                    }
                    if (ch == 'c' || ch == 'C') {
                        state += 3;
                        continue block10;
                    }
                    if (Character.isWhitespace(ch)) {
                        ++i;
                        continue block10;
                    }
                    syntaxError = true;
                    continue block10;
                }
                case 3: {
                    if (ch == '=') {
                        ++i;
                        ++state;
                        continue block10;
                    }
                    if (Character.isWhitespace(ch)) {
                        ++i;
                        continue block10;
                    }
                    syntaxError = true;
                    continue block10;
                }
                case 4: {
                    if (ch == 'c' || ch == 'C') {
                        ++state;
                        continue block10;
                    }
                    if (Character.isWhitespace(ch)) {
                        ++i;
                        continue block10;
                    }
                    syntaxError = true;
                    continue block10;
                }
                case 5: {
                    if ((ch == 'c' || ch == 'C') && i + 4 <= len && jdbcSql.substring(i, i + 4).equalsIgnoreCase("call")) {
                        isFunction = true;
                        i += 4;
                        ++state;
                        continue block10;
                    }
                    if (Character.isWhitespace(ch)) {
                        ++i;
                        continue block10;
                    }
                    syntaxError = true;
                    continue block10;
                }
                case 6: {
                    if (Character.isWhitespace(ch)) {
                        ++state;
                        startIndex = ++i;
                        continue block10;
                    }
                    syntaxError = true;
                    continue block10;
                }
                case 7: {
                    if (ch == '\'') {
                        inQuotes = !inQuotes;
                        ++i;
                        continue block10;
                    }
                    if (inQuotes && ch == '\\' && !stdStrings) {
                        i += 2;
                        continue block10;
                    }
                    if (!inQuotes && ch == '{') {
                        inEscape = !inEscape;
                        ++i;
                        continue block10;
                    }
                    if (!inQuotes && ch == '}') {
                        if (!inEscape) {
                            endIndex = i++;
                            ++state;
                            continue block10;
                        }
                        inEscape = false;
                        continue block10;
                    }
                    if (!inQuotes && ch == ';') {
                        syntaxError = true;
                        continue block10;
                    }
                    ++i;
                    continue block10;
                }
                case 8: {
                    if (Character.isWhitespace(ch)) {
                        ++i;
                        continue block10;
                    }
                    syntaxError = true;
                    continue block10;
                }
            }
            throw new IllegalStateException("somehow got into bad state " + state);
        }
        if (i == len && !syntaxError) {
            if (state == 1) {
                for (i = 0; i < len && Character.isWhitespace(jdbcSql.charAt(i)); ++i) {
                }
                if (i < len - 5 && ((ch = jdbcSql.charAt(i)) == 'c' || ch == 'C') && jdbcSql.substring(i, i + 4).equalsIgnoreCase("call") && Character.isWhitespace(jdbcSql.charAt(i + 4))) {
                    isFunction = true;
                }
                return new JdbcCallParseInfo(sql, isFunction);
            }
            if (state != 8) {
                syntaxError = true;
            }
        }
        if (syntaxError) {
            throw new PSQLException(GT.tr("Malformed function or procedure escape syntax at offset {0}.", i), PSQLState.STATEMENT_NOT_ALLOWED_IN_FUNCTION_CALL);
        }
        if (escapeSyntaxCallMode == EscapeSyntaxCallMode.SELECT || serverVersion < 110000 || outParamBeforeFunc && escapeSyntaxCallMode == EscapeSyntaxCallMode.CALL_IF_NO_RETURN) {
            prefix = "select * from ";
            suffix = " as result";
        } else {
            prefix = "call ";
            suffix = "";
        }
        String s2 = jdbcSql.substring(startIndex, endIndex);
        int prefixLength = prefix.length();
        StringBuilder sb = new StringBuilder(prefixLength + jdbcSql.length() + suffix.length() + 10);
        sb.append(prefix);
        sb.append(s2);
        int opening = s2.indexOf(40) + 1;
        if (opening == 0) {
            sb.append(outParamBeforeFunc ? "(?)" : "()");
        } else if (outParamBeforeFunc) {
            char c;
            boolean needComma = false;
            for (int j = opening + prefixLength; j < sb.length() && (c = sb.charAt(j)) != ')'; ++j) {
                if (Character.isWhitespace(c)) continue;
                needComma = true;
                break;
            }
            if (needComma) {
                sb.insert(opening + prefixLength, "?,");
            } else {
                sb.insert(opening + prefixLength, "?");
            }
        }
        sql = !suffix.isEmpty() ? sb.append(suffix).toString() : sb.toString();
        return new JdbcCallParseInfo(sql, isFunction);
    }

    public static String replaceProcessing(String sql, boolean replaceProcessingEnabled, boolean standardConformingStrings) throws SQLException {
        if (replaceProcessingEnabled) {
            int len = sql.length();
            char[] chars = sql.toCharArray();
            StringBuilder newsql = new StringBuilder(len);
            int i = 0;
            while (i < len) {
                if ((i = Parser.parseSql(chars, i, newsql, false, standardConformingStrings)) >= len) continue;
                newsql.append(chars[i]);
                ++i;
            }
            return newsql.toString();
        }
        return sql;
    }

    private static int parseSql(char[] sql, int i, StringBuilder newsql, boolean stopOnComma, boolean stdStrings) throws SQLException {
        SqlParseState state = SqlParseState.IN_SQLCODE;
        int len = sql.length;
        int nestedParenthesis = 0;
        boolean endOfNested = false;
        --i;
        while (!endOfNested && ++i < len) {
            char c = sql[i];
            block0 : switch (state) {
                case IN_SQLCODE: {
                    if (c == '$') {
                        int i0 = i;
                        i = Parser.parseDollarQuotes(sql, i);
                        Parser.checkParsePosition(i, len, i0, sql, "Unterminated dollar quote started at position {0} in SQL {1}. Expected terminating $$");
                        newsql.append(sql, i0, i - i0 + 1);
                        break;
                    }
                    if (c == '\'') {
                        int i0 = i;
                        i = Parser.parseSingleQuotes(sql, i, stdStrings);
                        Parser.checkParsePosition(i, len, i0, sql, "Unterminated string literal started at position {0} in SQL {1}. Expected ' char");
                        newsql.append(sql, i0, i - i0 + 1);
                        break;
                    }
                    if (c == '\"') {
                        int i0 = i;
                        i = Parser.parseDoubleQuotes(sql, i);
                        Parser.checkParsePosition(i, len, i0, sql, "Unterminated identifier started at position {0} in SQL {1}. Expected \" char");
                        newsql.append(sql, i0, i - i0 + 1);
                        break;
                    }
                    if (c == '/') {
                        int i0 = i;
                        i = Parser.parseBlockComment(sql, i);
                        Parser.checkParsePosition(i, len, i0, sql, "Unterminated block comment started at position {0} in SQL {1}. Expected */ sequence");
                        newsql.append(sql, i0, i - i0 + 1);
                        break;
                    }
                    if (c == '-') {
                        int i0 = i;
                        i = Parser.parseLineComment(sql, i);
                        newsql.append(sql, i0, i - i0 + 1);
                        break;
                    }
                    if (c == '(') {
                        ++nestedParenthesis;
                    } else if (c == ')') {
                        if (--nestedParenthesis < 0) {
                            endOfNested = true;
                            break;
                        }
                    } else {
                        if (stopOnComma && c == ',' && nestedParenthesis == 0) {
                            endOfNested = true;
                            break;
                        }
                        if (c == '{' && i + 1 < len) {
                            SqlParseState[] availableStates = SqlParseState.VALUES;
                            for (int j = 1; j < availableStates.length; ++j) {
                                SqlParseState availableState = availableStates[j];
                                int matchedPosition = availableState.getMatchedPosition(sql, i + 1);
                                if (matchedPosition == 0) continue;
                                i += matchedPosition;
                                if (availableState.replacementKeyword != null) {
                                    newsql.append(availableState.replacementKeyword);
                                }
                                state = availableState;
                                break block0;
                            }
                        }
                    }
                    newsql.append(c);
                    break;
                }
                case ESC_FUNCTION: {
                    i = Parser.escapeFunction(sql, i, newsql, stdStrings);
                    state = SqlParseState.IN_SQLCODE;
                    break;
                }
                case ESC_DATE: 
                case ESC_TIME: 
                case ESC_TIMESTAMP: 
                case ESC_OUTERJOIN: 
                case ESC_ESCAPECHAR: {
                    if (c == '}') {
                        state = SqlParseState.IN_SQLCODE;
                        break;
                    }
                    newsql.append(c);
                }
            }
        }
        return i;
    }

    private static int findOpenBrace(char[] sql, int i) {
        int posArgs;
        for (posArgs = i; posArgs < sql.length && sql[posArgs] != '('; ++posArgs) {
        }
        return posArgs;
    }

    private static void checkParsePosition(int i, int len, int i0, char[] sql, String message) throws PSQLException {
        if (i < len) {
            return;
        }
        throw new PSQLException(GT.tr(message, i0, new String(sql)), PSQLState.SYNTAX_ERROR);
    }

    private static int escapeFunction(char[] sql, int i, StringBuilder newsql, boolean stdStrings) throws SQLException {
        int argPos = Parser.findOpenBrace(sql, i);
        if (argPos < sql.length) {
            String functionName = new String(sql, i, argPos - i).trim();
            i = argPos + 1;
            i = Parser.escapeFunctionArguments(newsql, functionName, sql, i, stdStrings);
        }
        ++i;
        while (i < sql.length && sql[i] != '}') {
            newsql.append(sql[i++]);
        }
        return i;
    }

    private static int escapeFunctionArguments(StringBuilder newsql, String functionName, char[] sql, int i, boolean stdStrings) throws SQLException {
        ArrayList<StringBuilder> parsedArgs = new ArrayList<StringBuilder>(3);
        while (true) {
            StringBuilder arg = new StringBuilder();
            int lastPos = ++i;
            if ((i = Parser.parseSql(sql, i, arg, true, stdStrings)) != lastPos) {
                parsedArgs.add(arg);
            }
            if (i >= sql.length || sql[i] != ',') break;
        }
        Method method = EscapedFunctions2.getFunction(functionName);
        if (method == null) {
            newsql.append(functionName);
            EscapedFunctions2.appendCall(newsql, "(", ",", ")", parsedArgs);
            return i;
        }
        try {
            method.invoke(null, newsql, parsedArgs);
        }
        catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            if (targetException instanceof SQLException) {
                throw (SQLException)targetException;
            }
            String message = targetException == null ? "no message" : targetException.getMessage();
            throw new PSQLException(message, PSQLState.SYSTEM_ERROR);
        }
        catch (IllegalAccessException e) {
            throw new PSQLException(e.getMessage(), PSQLState.SYSTEM_ERROR);
        }
        return i;
    }

    static /* synthetic */ char[] access$300() {
        return SINGLE_QUOTE;
    }

    static /* synthetic */ char[] access$400() {
        return QUOTE_OR_ALPHABETIC_MARKER;
    }

    static /* synthetic */ char[] access$500() {
        return QUOTE_OR_ALPHABETIC_MARKER_OR_PARENTHESIS;
    }

    private static enum SqlParseState {
        IN_SQLCODE,
        ESC_DATE("d", Parser.access$300(), "DATE "),
        ESC_TIME("t", Parser.access$300(), "TIME "),
        ESC_TIMESTAMP("ts", Parser.access$300(), "TIMESTAMP "),
        ESC_FUNCTION("fn", Parser.access$400(), null),
        ESC_OUTERJOIN("oj", Parser.access$500(), null),
        ESC_ESCAPECHAR("escape", Parser.access$300(), "ESCAPE ");

        private static final SqlParseState[] VALUES;
        private final char[] escapeKeyword;
        private final char[] allowedValues;
        private final @Nullable String replacementKeyword;

        private SqlParseState() {
            this("", new char[0], null);
        }

        private SqlParseState(@Nullable String escapeKeyword, char[] allowedValues, String replacementKeyword) {
            this.escapeKeyword = escapeKeyword.toCharArray();
            this.allowedValues = allowedValues;
            this.replacementKeyword = replacementKeyword;
        }

        private boolean startMatches(char[] sql, int pos) {
            for (char c : this.escapeKeyword) {
                char curr;
                if (pos >= sql.length) {
                    return false;
                }
                if ((curr = sql[pos++]) == c || curr == Character.toUpperCase(c)) continue;
                return false;
            }
            return pos < sql.length;
        }

        private int getMatchedPosition(char[] sql, int pos) {
            if (!this.startMatches(sql, pos)) {
                return 0;
            }
            int newPos = pos + this.escapeKeyword.length;
            char curr = sql[newPos];
            while (curr == ' ') {
                if (++newPos >= sql.length) {
                    return 0;
                }
                curr = sql[newPos];
            }
            for (char c : this.allowedValues) {
                if (curr != c && (c != '0' || !Character.isLetter(curr))) continue;
                return newPos - pos;
            }
            return 0;
        }

        static {
            VALUES = SqlParseState.values();
        }
    }
}

