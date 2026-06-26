/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.jdbc;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

public final class EscapedFunctions2 {
    private static final String SQL_TSI_ROOT = "SQL_TSI_";
    private static final String SQL_TSI_DAY = "SQL_TSI_DAY";
    private static final String SQL_TSI_FRAC_SECOND = "SQL_TSI_FRAC_SECOND";
    private static final String SQL_TSI_HOUR = "SQL_TSI_HOUR";
    private static final String SQL_TSI_MINUTE = "SQL_TSI_MINUTE";
    private static final String SQL_TSI_MONTH = "SQL_TSI_MONTH";
    private static final String SQL_TSI_QUARTER = "SQL_TSI_QUARTER";
    private static final String SQL_TSI_SECOND = "SQL_TSI_SECOND";
    private static final String SQL_TSI_WEEK = "SQL_TSI_WEEK";
    private static final String SQL_TSI_YEAR = "SQL_TSI_YEAR";
    private static final ConcurrentMap<String, Method> FUNCTION_MAP = EscapedFunctions2.createFunctionMap("sql");

    private static ConcurrentMap<String, Method> createFunctionMap(String prefix) {
        Method[] methods = EscapedFunctions2.class.getMethods();
        ConcurrentHashMap<String, Method> functionMap = new ConcurrentHashMap<String, Method>(methods.length * 2);
        for (Method method : methods) {
            if (!method.getName().startsWith(prefix)) continue;
            functionMap.put(method.getName().substring(prefix.length()).toLowerCase(Locale.US), method);
        }
        return functionMap;
    }

    public static @Nullable Method getFunction(String functionName) {
        Method method = (Method)FUNCTION_MAP.get(functionName);
        if (method != null) {
            return method;
        }
        String nameLower = functionName.toLowerCase(Locale.US);
        if (nameLower.equals(functionName)) {
            return null;
        }
        method = (Method)FUNCTION_MAP.get(nameLower);
        if (method != null && FUNCTION_MAP.size() < 1000) {
            FUNCTION_MAP.putIfAbsent(functionName, method);
        }
        return method;
    }

    public static void sqlceiling(StringBuilder buf, List<? extends CharSequence> parsedArgs) throws SQLException {
        EscapedFunctions2.singleArgumentFunctionCall(buf, "ceil(", "ceiling", parsedArgs);
    }

    public static void sqllog(StringBuilder buf, List<? extends CharSequence> parsedArgs) throws SQLException {
        EscapedFunctions2.singleArgumentFunctionCall(buf, "ln(", "log", parsedArgs);
    }

    public static void sqllog10(StringBuilder buf, List<? extends CharSequence> parsedArgs) throws SQLException {
        EscapedFunctions2.singleArgumentFunctionCall(buf, "log(", "log10", parsedArgs);
    }

    public static void sqlpower(StringBuilder buf, List<? extends CharSequence> parsedArgs) throws SQLException {
        EscapedFunctions2.twoArgumentsFunctionCall(buf, "pow(", "power", parsedArgs);
    }

    public static void sqltruncate(StringBuilder buf, List<? extends CharSequence> parsedArgs) throws SQLException {
        EscapedFunctions2.twoArgumentsFunctionCall(buf, "trunc(", "truncate", parsedArgs);
    }

    public static void sqlchar(StringBuilder buf, List<? extends CharSequence> parsedArgs) throws SQLException {
        EscapedFunctions2.singleArgumentFunctionCall(buf, "chr(", "char", parsedArgs);
    }

    public static void sqlconcat(StringBuilder buf, List<? extends CharSequence> parsedArgs) {
        EscapedFunctions2.appendCall(buf, "(", "||", ")", parsedArgs);
    }

    public static void sqlinsert(StringBuilder buf, List<? extends CharSequence> parsedArgs) throws SQLException {
        if (parsedArgs.size() != 4) {
            throw new PSQLException(GT.tr("{0} function takes four and only four argument.", "insert"), PSQLState.SYNTAX_ERROR);
        }
        buf.append("overlay(");
        buf.append(parsedArgs.get(0)).append(" placing ").append(parsedArgs.get(3));
        buf.append(" from ").append(parsedArgs.get(1)).append(" for ").append(parsedArgs.get(2));
        buf.append(')');
    }

    public static void sqllcase(StringBuilder buf, List<? extends CharSequence> parsedArgs) throws SQLException {
        EscapedFunctions2.singleArgumentFunctionCall(buf, "lower(", "lcase", parsedArgs);
    }

    public static void sqlleft(StringBuilder buf, List<? extends CharSequence> parsedArgs) throws SQLException {
        if (parsedArgs.size() != 2) {
            throw new PSQLException(GT.tr("{0} function takes two and only two arguments.", "left"), PSQLState.SYNTAX_ERROR);
        }
        EscapedFunctions2.appendCall(buf, "substring(", " for ", ")", parsedArgs);
    }

    public static void sqllength(StringBuilder buf, List<? extends CharSequence> parsedArgs) throws SQLException {
        if (parsedArgs.size() != 1) {
            throw new PSQLException(GT.tr("{0} function takes one and only one argument.", "length"), PSQLState.SYNTAX_ERROR);
        }
        EscapedFunctions2.appendCall(buf, "length(trim(trailing from ", "", "))", parsedArgs);
    }

    public static void sqllocate(StringBuilder buf, List<? extends CharSequence> parsedArgs) throws SQLException {
        if (parsedArgs.size() == 2) {
            EscapedFunctions2.appendCall(buf, "position(", " in ", ")", parsedArgs);
        } else if (parsedArgs.size() == 3) {
            String tmp = "position(" + parsedArgs.get(0) + " in substring(" + parsedArgs.get(1) + " from " + parsedArgs.get(2) + "))";
            buf.append("(").append(parsedArgs.get(2)).append("*sign(").append(tmp).append(")+").append(tmp).append(")");
        } else {
            throw new PSQLException(GT.tr("{0} function takes two or three arguments.", "locate"), PSQLState.SYNTAX_ERROR);
        }
    }

    public static void sqlltrim(StringBuilder buf, List<? extends CharSequence> parsedArgs) throws SQLException {
        EscapedFunctions2.singleArgumentFunctionCall(buf, "trim(leading from ", "ltrim", parsedArgs);
    }

    public static void sqlright(StringBuilder buf, List<? extends CharSequence> parsedArgs) throws SQLException {
        if (parsedArgs.size() != 2) {
            throw new PSQLException(GT.tr("{0} function takes two and only two arguments.", "right"), PSQLState.SYNTAX_ERROR);
        }
        buf.append("substring(");
        buf.append(parsedArgs.get(0)).append(" from (length(").append(parsedArgs.get(0)).append(")+1-").append(parsedArgs.get(1));
        buf.append("))");
    }

    public static void sqlrtrim(StringBuilder buf, List<? extends CharSequence> parsedArgs) throws SQLException {
        EscapedFunctions2.singleArgumentFunctionCall(buf, "trim(trailing from ", "rtrim", parsedArgs);
    }

    public static void sqlspace(StringBuilder buf, List<? extends CharSequence> parsedArgs) throws SQLException {
        EscapedFunctions2.singleArgumentFunctionCall(buf, "repeat(' ',", "space", parsedArgs);
    }

    public static void sqlsubstring(StringBuilder buf, List<? extends CharSequence> parsedArgs) throws SQLException {
        int argSize = parsedArgs.size();
        if (argSize != 2 && argSize != 3) {
            throw new PSQLException(GT.tr("{0} function takes two or three arguments.", "substring"), PSQLState.SYNTAX_ERROR);
        }
        EscapedFunctions2.appendCall(buf, "substr(", ",", ")", parsedArgs);
    }

    public static void sqlucase(StringBuilder buf, List<? extends CharSequence> parsedArgs) throws SQLException {
        EscapedFunctions2.singleArgumentFunctionCall(buf, "upper(", "ucase", parsedArgs);
    }

    public static void sqlcurdate(StringBuilder buf, List<? extends CharSequence> parsedArgs) throws SQLException {
        EscapedFunctions2.zeroArgumentFunctionCall(buf, "current_date", "curdate", parsedArgs);
    }

    public static void sqlcurtime(StringBuilder buf, List<? extends CharSequence> parsedArgs) throws SQLException {
        EscapedFunctions2.zeroArgumentFunctionCall(buf, "current_time", "curtime", parsedArgs);
    }

    public static void sqldayname(StringBuilder buf, List<? extends CharSequence> parsedArgs) throws SQLException {
        if (parsedArgs.size() != 1) {
            throw new PSQLException(GT.tr("{0} function takes one and only one argument.", "dayname"), PSQLState.SYNTAX_ERROR);
        }
        EscapedFunctions2.appendCall(buf, "to_char(", ",", ",'Day')", parsedArgs);
    }

    public static void sqldayofmonth(StringBuilder buf, List<? extends CharSequence> parsedArgs) throws SQLException {
        EscapedFunctions2.singleArgumentFunctionCall(buf, "extract(day from ", "dayofmonth", parsedArgs);
    }

    public static void sqldayofweek(StringBuilder buf, List<? extends CharSequence> parsedArgs) throws SQLException {
        if (parsedArgs.size() != 1) {
            throw new PSQLException(GT.tr("{0} function takes one and only one argument.", "dayofweek"), PSQLState.SYNTAX_ERROR);
        }
        EscapedFunctions2.appendCall(buf, "extract(dow from ", ",", ")+1", parsedArgs);
    }

    public static void sqldayofyear(StringBuilder buf, List<? extends CharSequence> parsedArgs) throws SQLException {
        EscapedFunctions2.singleArgumentFunctionCall(buf, "extract(doy from ", "dayofyear", parsedArgs);
    }

    public static void sqlhour(StringBuilder buf, List<? extends CharSequence> parsedArgs) throws SQLException {
        EscapedFunctions2.singleArgumentFunctionCall(buf, "extract(hour from ", "hour", parsedArgs);
    }

    public static void sqlminute(StringBuilder buf, List<? extends CharSequence> parsedArgs) throws SQLException {
        EscapedFunctions2.singleArgumentFunctionCall(buf, "extract(minute from ", "minute", parsedArgs);
    }

    public static void sqlmonth(StringBuilder buf, List<? extends CharSequence> parsedArgs) throws SQLException {
        EscapedFunctions2.singleArgumentFunctionCall(buf, "extract(month from ", "month", parsedArgs);
    }

    public static void sqlmonthname(StringBuilder buf, List<? extends CharSequence> parsedArgs) throws SQLException {
        if (parsedArgs.size() != 1) {
            throw new PSQLException(GT.tr("{0} function takes one and only one argument.", "monthname"), PSQLState.SYNTAX_ERROR);
        }
        EscapedFunctions2.appendCall(buf, "to_char(", ",", ",'Month')", parsedArgs);
    }

    public static void sqlquarter(StringBuilder buf, List<? extends CharSequence> parsedArgs) throws SQLException {
        EscapedFunctions2.singleArgumentFunctionCall(buf, "extract(quarter from ", "quarter", parsedArgs);
    }

    public static void sqlsecond(StringBuilder buf, List<? extends CharSequence> parsedArgs) throws SQLException {
        EscapedFunctions2.singleArgumentFunctionCall(buf, "extract(second from ", "second", parsedArgs);
    }

    public static void sqlweek(StringBuilder buf, List<? extends CharSequence> parsedArgs) throws SQLException {
        EscapedFunctions2.singleArgumentFunctionCall(buf, "extract(week from ", "week", parsedArgs);
    }

    public static void sqlyear(StringBuilder buf, List<? extends CharSequence> parsedArgs) throws SQLException {
        EscapedFunctions2.singleArgumentFunctionCall(buf, "extract(year from ", "year", parsedArgs);
    }

    public static void sqltimestampadd(StringBuilder buf, List<? extends CharSequence> parsedArgs) throws SQLException {
        if (parsedArgs.size() != 3) {
            throw new PSQLException(GT.tr("{0} function takes three and only three arguments.", "timestampadd"), PSQLState.SYNTAX_ERROR);
        }
        buf.append('(');
        EscapedFunctions2.appendInterval(buf, parsedArgs.get(0).toString(), parsedArgs.get(1).toString());
        buf.append('+').append(parsedArgs.get(2)).append(')');
    }

    private static void appendInterval(StringBuilder buf, String type, String value) throws SQLException {
        if (!EscapedFunctions2.isTsi(type)) {
            throw new PSQLException(GT.tr("Interval {0} not yet implemented", type), PSQLState.SYNTAX_ERROR);
        }
        if (EscapedFunctions2.appendSingleIntervalCast(buf, SQL_TSI_DAY, type, value, "day") || EscapedFunctions2.appendSingleIntervalCast(buf, SQL_TSI_SECOND, type, value, "second") || EscapedFunctions2.appendSingleIntervalCast(buf, SQL_TSI_HOUR, type, value, "hour") || EscapedFunctions2.appendSingleIntervalCast(buf, SQL_TSI_MINUTE, type, value, "minute") || EscapedFunctions2.appendSingleIntervalCast(buf, SQL_TSI_MONTH, type, value, "month") || EscapedFunctions2.appendSingleIntervalCast(buf, SQL_TSI_WEEK, type, value, "week") || EscapedFunctions2.appendSingleIntervalCast(buf, SQL_TSI_YEAR, type, value, "year")) {
            return;
        }
        if (EscapedFunctions2.areSameTsi(SQL_TSI_QUARTER, type)) {
            buf.append("CAST((").append(value).append("::int * 3) || ' month' as interval)");
            return;
        }
        throw new PSQLException(GT.tr("Interval {0} not yet implemented", type), PSQLState.NOT_IMPLEMENTED);
    }

    private static boolean appendSingleIntervalCast(StringBuilder buf, String cmp, String type, String value, String pgType) {
        if (!EscapedFunctions2.areSameTsi(type, cmp)) {
            return false;
        }
        buf.ensureCapacity(buf.length() + 5 + 4 + 14 + value.length() + pgType.length());
        buf.append("CAST(").append(value).append("||' ").append(pgType).append("' as interval)");
        return true;
    }

    private static boolean areSameTsi(String a, String b) {
        return a.length() == b.length() && b.length() > SQL_TSI_ROOT.length() && a.regionMatches(true, SQL_TSI_ROOT.length(), b, SQL_TSI_ROOT.length(), b.length() - SQL_TSI_ROOT.length());
    }

    private static boolean isTsi(String interval) {
        return interval.regionMatches(true, 0, SQL_TSI_ROOT, 0, SQL_TSI_ROOT.length());
    }

    public static void sqltimestampdiff(StringBuilder buf, List<? extends CharSequence> parsedArgs) throws SQLException {
        if (parsedArgs.size() != 3) {
            throw new PSQLException(GT.tr("{0} function takes three and only three arguments.", "timestampdiff"), PSQLState.SYNTAX_ERROR);
        }
        buf.append("extract( ").append(EscapedFunctions2.constantToDatePart(buf, parsedArgs.get(0).toString())).append(" from (").append(parsedArgs.get(2)).append("-").append(parsedArgs.get(1)).append("))");
    }

    private static String constantToDatePart(StringBuilder buf, String type) throws SQLException {
        if (!EscapedFunctions2.isTsi(type)) {
            throw new PSQLException(GT.tr("Interval {0} not yet implemented", type), PSQLState.SYNTAX_ERROR);
        }
        if (EscapedFunctions2.areSameTsi(SQL_TSI_DAY, type)) {
            return "day";
        }
        if (EscapedFunctions2.areSameTsi(SQL_TSI_SECOND, type)) {
            return "second";
        }
        if (EscapedFunctions2.areSameTsi(SQL_TSI_HOUR, type)) {
            return "hour";
        }
        if (EscapedFunctions2.areSameTsi(SQL_TSI_MINUTE, type)) {
            return "minute";
        }
        throw new PSQLException(GT.tr("Interval {0} not yet implemented", type), PSQLState.SYNTAX_ERROR);
    }

    public static void sqldatabase(StringBuilder buf, List<? extends CharSequence> parsedArgs) throws SQLException {
        EscapedFunctions2.zeroArgumentFunctionCall(buf, "current_database()", "database", parsedArgs);
    }

    public static void sqlifnull(StringBuilder buf, List<? extends CharSequence> parsedArgs) throws SQLException {
        EscapedFunctions2.twoArgumentsFunctionCall(buf, "coalesce(", "ifnull", parsedArgs);
    }

    public static void sqluser(StringBuilder buf, List<? extends CharSequence> parsedArgs) throws SQLException {
        EscapedFunctions2.zeroArgumentFunctionCall(buf, "user", "user", parsedArgs);
    }

    private static void zeroArgumentFunctionCall(StringBuilder buf, String call, String functionName, List<? extends CharSequence> parsedArgs) throws PSQLException {
        if (!parsedArgs.isEmpty()) {
            throw new PSQLException(GT.tr("{0} function doesn''t take any argument.", functionName), PSQLState.SYNTAX_ERROR);
        }
        buf.append(call);
    }

    private static void singleArgumentFunctionCall(StringBuilder buf, String call, String functionName, List<? extends CharSequence> parsedArgs) throws PSQLException {
        if (parsedArgs.size() != 1) {
            throw new PSQLException(GT.tr("{0} function takes one and only one argument.", functionName), PSQLState.SYNTAX_ERROR);
        }
        CharSequence arg0 = parsedArgs.get(0);
        buf.ensureCapacity(buf.length() + call.length() + arg0.length() + 1);
        buf.append(call).append(arg0).append(')');
    }

    private static void twoArgumentsFunctionCall(StringBuilder buf, String call, String functionName, List<? extends CharSequence> parsedArgs) throws PSQLException {
        if (parsedArgs.size() != 2) {
            throw new PSQLException(GT.tr("{0} function takes two and only two arguments.", functionName), PSQLState.SYNTAX_ERROR);
        }
        EscapedFunctions2.appendCall(buf, call, ",", ")", parsedArgs);
    }

    public static void appendCall(StringBuilder sb, String begin, String separator, String end, List<? extends CharSequence> args) {
        int i;
        int size = begin.length();
        int numberOfArguments = args.size();
        for (i = 0; i < numberOfArguments; ++i) {
            size += args.get(i).length();
        }
        sb.ensureCapacity(sb.length() + (size += separator.length() * (numberOfArguments - 1)) + 1);
        sb.append(begin);
        for (i = 0; i < numberOfArguments; ++i) {
            if (i > 0) {
                sb.append(separator);
            }
            sb.append(args.get(i));
        }
        sb.append(end);
    }
}

