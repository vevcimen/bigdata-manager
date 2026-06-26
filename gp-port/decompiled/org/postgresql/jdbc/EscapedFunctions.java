/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.jdbc;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

@Deprecated
public class EscapedFunctions {
    public static final String ABS = "abs";
    public static final String ACOS = "acos";
    public static final String ASIN = "asin";
    public static final String ATAN = "atan";
    public static final String ATAN2 = "atan2";
    public static final String CEILING = "ceiling";
    public static final String COS = "cos";
    public static final String COT = "cot";
    public static final String DEGREES = "degrees";
    public static final String EXP = "exp";
    public static final String FLOOR = "floor";
    public static final String LOG = "log";
    public static final String LOG10 = "log10";
    public static final String MOD = "mod";
    public static final String PI = "pi";
    public static final String POWER = "power";
    public static final String RADIANS = "radians";
    public static final String ROUND = "round";
    public static final String SIGN = "sign";
    public static final String SIN = "sin";
    public static final String SQRT = "sqrt";
    public static final String TAN = "tan";
    public static final String TRUNCATE = "truncate";
    public static final String ASCII = "ascii";
    public static final String CHAR = "char";
    public static final String CONCAT = "concat";
    public static final String INSERT = "insert";
    public static final String LCASE = "lcase";
    public static final String LEFT = "left";
    public static final String LENGTH = "length";
    public static final String LOCATE = "locate";
    public static final String LTRIM = "ltrim";
    public static final String REPEAT = "repeat";
    public static final String REPLACE = "replace";
    public static final String RIGHT = "right";
    public static final String RTRIM = "rtrim";
    public static final String SPACE = "space";
    public static final String SUBSTRING = "substring";
    public static final String UCASE = "ucase";
    public static final String CURDATE = "curdate";
    public static final String CURTIME = "curtime";
    public static final String DAYNAME = "dayname";
    public static final String DAYOFMONTH = "dayofmonth";
    public static final String DAYOFWEEK = "dayofweek";
    public static final String DAYOFYEAR = "dayofyear";
    public static final String HOUR = "hour";
    public static final String MINUTE = "minute";
    public static final String MONTH = "month";
    public static final String MONTHNAME = "monthname";
    public static final String NOW = "now";
    public static final String QUARTER = "quarter";
    public static final String SECOND = "second";
    public static final String WEEK = "week";
    public static final String YEAR = "year";
    public static final String TIMESTAMPADD = "timestampadd";
    public static final String TIMESTAMPDIFF = "timestampdiff";
    public static final String SQL_TSI_ROOT = "SQL_TSI_";
    public static final String SQL_TSI_DAY = "DAY";
    public static final String SQL_TSI_FRAC_SECOND = "FRAC_SECOND";
    public static final String SQL_TSI_HOUR = "HOUR";
    public static final String SQL_TSI_MINUTE = "MINUTE";
    public static final String SQL_TSI_MONTH = "MONTH";
    public static final String SQL_TSI_QUARTER = "QUARTER";
    public static final String SQL_TSI_SECOND = "SECOND";
    public static final String SQL_TSI_WEEK = "WEEK";
    public static final String SQL_TSI_YEAR = "YEAR";
    public static final String DATABASE = "database";
    public static final String IFNULL = "ifnull";
    public static final String USER = "user";
    private static Map<String, Method> functionMap = EscapedFunctions.createFunctionMap();

    private static Map<String, Method> createFunctionMap() {
        Method[] arrayMeths = EscapedFunctions.class.getDeclaredMethods();
        HashMap<String, Method> functionMap = new HashMap<String, Method>(arrayMeths.length * 2);
        for (Method meth : arrayMeths) {
            if (!meth.getName().startsWith("sql")) continue;
            functionMap.put(meth.getName().toLowerCase(Locale.US), meth);
        }
        return functionMap;
    }

    public static @Nullable Method getFunction(String functionName) {
        return functionMap.get("sql" + functionName.toLowerCase(Locale.US));
    }

    public static String sqlceiling(List<?> parsedArgs) throws SQLException {
        return EscapedFunctions.singleArgumentFunctionCall("ceil(", CEILING, parsedArgs);
    }

    public static String sqllog(List<?> parsedArgs) throws SQLException {
        return EscapedFunctions.singleArgumentFunctionCall("ln(", LOG, parsedArgs);
    }

    public static String sqllog10(List<?> parsedArgs) throws SQLException {
        return EscapedFunctions.singleArgumentFunctionCall("log(", LOG10, parsedArgs);
    }

    public static String sqlpower(List<?> parsedArgs) throws SQLException {
        return EscapedFunctions.twoArgumentsFunctionCall("pow(", POWER, parsedArgs);
    }

    public static String sqltruncate(List<?> parsedArgs) throws SQLException {
        return EscapedFunctions.twoArgumentsFunctionCall("trunc(", TRUNCATE, parsedArgs);
    }

    public static String sqlchar(List<?> parsedArgs) throws SQLException {
        return EscapedFunctions.singleArgumentFunctionCall("chr(", CHAR, parsedArgs);
    }

    public static String sqlconcat(List<?> parsedArgs) {
        StringBuilder buf = new StringBuilder();
        buf.append('(');
        for (int iArg = 0; iArg < parsedArgs.size(); ++iArg) {
            buf.append(parsedArgs.get(iArg));
            if (iArg == parsedArgs.size() - 1) continue;
            buf.append(" || ");
        }
        return buf.append(')').toString();
    }

    public static String sqlinsert(List<?> parsedArgs) throws SQLException {
        if (parsedArgs.size() != 4) {
            throw new PSQLException(GT.tr("{0} function takes four and only four argument.", INSERT), PSQLState.SYNTAX_ERROR);
        }
        StringBuilder buf = new StringBuilder();
        buf.append("overlay(");
        buf.append(parsedArgs.get(0)).append(" placing ").append(parsedArgs.get(3));
        buf.append(" from ").append(parsedArgs.get(1)).append(" for ").append(parsedArgs.get(2));
        return buf.append(')').toString();
    }

    public static String sqllcase(List<?> parsedArgs) throws SQLException {
        return EscapedFunctions.singleArgumentFunctionCall("lower(", LCASE, parsedArgs);
    }

    public static String sqlleft(List<?> parsedArgs) throws SQLException {
        if (parsedArgs.size() != 2) {
            throw new PSQLException(GT.tr("{0} function takes two and only two arguments.", LEFT), PSQLState.SYNTAX_ERROR);
        }
        StringBuilder buf = new StringBuilder();
        buf.append("substring(");
        buf.append(parsedArgs.get(0)).append(" for ").append(parsedArgs.get(1));
        return buf.append(')').toString();
    }

    public static String sqllength(List<?> parsedArgs) throws SQLException {
        if (parsedArgs.size() != 1) {
            throw new PSQLException(GT.tr("{0} function takes one and only one argument.", LENGTH), PSQLState.SYNTAX_ERROR);
        }
        StringBuilder buf = new StringBuilder();
        buf.append("length(trim(trailing from ");
        buf.append(parsedArgs.get(0));
        return buf.append("))").toString();
    }

    public static String sqllocate(List<?> parsedArgs) throws SQLException {
        if (parsedArgs.size() == 2) {
            return "position(" + parsedArgs.get(0) + " in " + parsedArgs.get(1) + ")";
        }
        if (parsedArgs.size() == 3) {
            String tmp = "position(" + parsedArgs.get(0) + " in substring(" + parsedArgs.get(1) + " from " + parsedArgs.get(2) + "))";
            return "(" + parsedArgs.get(2) + "*sign(" + tmp + ")+" + tmp + ")";
        }
        throw new PSQLException(GT.tr("{0} function takes two or three arguments.", LOCATE), PSQLState.SYNTAX_ERROR);
    }

    public static String sqlltrim(List<?> parsedArgs) throws SQLException {
        return EscapedFunctions.singleArgumentFunctionCall("trim(leading from ", LTRIM, parsedArgs);
    }

    public static String sqlright(List<?> parsedArgs) throws SQLException {
        if (parsedArgs.size() != 2) {
            throw new PSQLException(GT.tr("{0} function takes two and only two arguments.", RIGHT), PSQLState.SYNTAX_ERROR);
        }
        StringBuilder buf = new StringBuilder();
        buf.append("substring(");
        buf.append(parsedArgs.get(0)).append(" from (length(").append(parsedArgs.get(0)).append(")+1-").append(parsedArgs.get(1));
        return buf.append("))").toString();
    }

    public static String sqlrtrim(List<?> parsedArgs) throws SQLException {
        return EscapedFunctions.singleArgumentFunctionCall("trim(trailing from ", RTRIM, parsedArgs);
    }

    public static String sqlspace(List<?> parsedArgs) throws SQLException {
        return EscapedFunctions.singleArgumentFunctionCall("repeat(' ',", SPACE, parsedArgs);
    }

    public static String sqlsubstring(List<?> parsedArgs) throws SQLException {
        if (parsedArgs.size() == 2) {
            return "substr(" + parsedArgs.get(0) + "," + parsedArgs.get(1) + ")";
        }
        if (parsedArgs.size() == 3) {
            return "substr(" + parsedArgs.get(0) + "," + parsedArgs.get(1) + "," + parsedArgs.get(2) + ")";
        }
        throw new PSQLException(GT.tr("{0} function takes two or three arguments.", SUBSTRING), PSQLState.SYNTAX_ERROR);
    }

    public static String sqlucase(List<?> parsedArgs) throws SQLException {
        return EscapedFunctions.singleArgumentFunctionCall("upper(", UCASE, parsedArgs);
    }

    public static String sqlcurdate(List<?> parsedArgs) throws SQLException {
        if (!parsedArgs.isEmpty()) {
            throw new PSQLException(GT.tr("{0} function doesn''t take any argument.", CURDATE), PSQLState.SYNTAX_ERROR);
        }
        return "current_date";
    }

    public static String sqlcurtime(List<?> parsedArgs) throws SQLException {
        if (!parsedArgs.isEmpty()) {
            throw new PSQLException(GT.tr("{0} function doesn''t take any argument.", CURTIME), PSQLState.SYNTAX_ERROR);
        }
        return "current_time";
    }

    public static String sqldayname(List<?> parsedArgs) throws SQLException {
        if (parsedArgs.size() != 1) {
            throw new PSQLException(GT.tr("{0} function takes one and only one argument.", DAYNAME), PSQLState.SYNTAX_ERROR);
        }
        return "to_char(" + parsedArgs.get(0) + ",'Day')";
    }

    public static String sqldayofmonth(List<?> parsedArgs) throws SQLException {
        return EscapedFunctions.singleArgumentFunctionCall("extract(day from ", DAYOFMONTH, parsedArgs);
    }

    public static String sqldayofweek(List<?> parsedArgs) throws SQLException {
        if (parsedArgs.size() != 1) {
            throw new PSQLException(GT.tr("{0} function takes one and only one argument.", DAYOFWEEK), PSQLState.SYNTAX_ERROR);
        }
        return "extract(dow from " + parsedArgs.get(0) + ")+1";
    }

    public static String sqldayofyear(List<?> parsedArgs) throws SQLException {
        return EscapedFunctions.singleArgumentFunctionCall("extract(doy from ", DAYOFYEAR, parsedArgs);
    }

    public static String sqlhour(List<?> parsedArgs) throws SQLException {
        return EscapedFunctions.singleArgumentFunctionCall("extract(hour from ", HOUR, parsedArgs);
    }

    public static String sqlminute(List<?> parsedArgs) throws SQLException {
        return EscapedFunctions.singleArgumentFunctionCall("extract(minute from ", MINUTE, parsedArgs);
    }

    public static String sqlmonth(List<?> parsedArgs) throws SQLException {
        return EscapedFunctions.singleArgumentFunctionCall("extract(month from ", MONTH, parsedArgs);
    }

    public static String sqlmonthname(List<?> parsedArgs) throws SQLException {
        if (parsedArgs.size() != 1) {
            throw new PSQLException(GT.tr("{0} function takes one and only one argument.", MONTHNAME), PSQLState.SYNTAX_ERROR);
        }
        return "to_char(" + parsedArgs.get(0) + ",'Month')";
    }

    public static String sqlquarter(List<?> parsedArgs) throws SQLException {
        return EscapedFunctions.singleArgumentFunctionCall("extract(quarter from ", QUARTER, parsedArgs);
    }

    public static String sqlsecond(List<?> parsedArgs) throws SQLException {
        return EscapedFunctions.singleArgumentFunctionCall("extract(second from ", SECOND, parsedArgs);
    }

    public static String sqlweek(List<?> parsedArgs) throws SQLException {
        return EscapedFunctions.singleArgumentFunctionCall("extract(week from ", WEEK, parsedArgs);
    }

    public static String sqlyear(List<?> parsedArgs) throws SQLException {
        return EscapedFunctions.singleArgumentFunctionCall("extract(year from ", YEAR, parsedArgs);
    }

    public static String sqltimestampadd(List<? extends Object> parsedArgs) throws SQLException {
        if (parsedArgs.size() != 3) {
            throw new PSQLException(GT.tr("{0} function takes three and only three arguments.", TIMESTAMPADD), PSQLState.SYNTAX_ERROR);
        }
        String interval = EscapedFunctions.constantToInterval(parsedArgs.get(0).toString(), parsedArgs.get(1).toString());
        StringBuilder buf = new StringBuilder();
        buf.append("(").append(interval).append("+");
        buf.append(parsedArgs.get(2)).append(")");
        return buf.toString();
    }

    private static String constantToInterval(String type, String value) throws SQLException {
        if (!type.startsWith(SQL_TSI_ROOT)) {
            throw new PSQLException(GT.tr("Interval {0} not yet implemented", type), PSQLState.SYNTAX_ERROR);
        }
        String shortType = type.substring(SQL_TSI_ROOT.length());
        if (SQL_TSI_DAY.equalsIgnoreCase(shortType)) {
            return "CAST(" + value + " || ' day' as interval)";
        }
        if (SQL_TSI_SECOND.equalsIgnoreCase(shortType)) {
            return "CAST(" + value + " || ' second' as interval)";
        }
        if (SQL_TSI_HOUR.equalsIgnoreCase(shortType)) {
            return "CAST(" + value + " || ' hour' as interval)";
        }
        if (SQL_TSI_MINUTE.equalsIgnoreCase(shortType)) {
            return "CAST(" + value + " || ' minute' as interval)";
        }
        if (SQL_TSI_MONTH.equalsIgnoreCase(shortType)) {
            return "CAST(" + value + " || ' month' as interval)";
        }
        if (SQL_TSI_QUARTER.equalsIgnoreCase(shortType)) {
            return "CAST((" + value + "::int * 3) || ' month' as interval)";
        }
        if (SQL_TSI_WEEK.equalsIgnoreCase(shortType)) {
            return "CAST(" + value + " || ' week' as interval)";
        }
        if (SQL_TSI_YEAR.equalsIgnoreCase(shortType)) {
            return "CAST(" + value + " || ' year' as interval)";
        }
        if (SQL_TSI_FRAC_SECOND.equalsIgnoreCase(shortType)) {
            throw new PSQLException(GT.tr("Interval {0} not yet implemented", "SQL_TSI_FRAC_SECOND"), PSQLState.SYNTAX_ERROR);
        }
        throw new PSQLException(GT.tr("Interval {0} not yet implemented", type), PSQLState.SYNTAX_ERROR);
    }

    public static String sqltimestampdiff(List<? extends Object> parsedArgs) throws SQLException {
        if (parsedArgs.size() != 3) {
            throw new PSQLException(GT.tr("{0} function takes three and only three arguments.", TIMESTAMPDIFF), PSQLState.SYNTAX_ERROR);
        }
        String datePart = EscapedFunctions.constantToDatePart(parsedArgs.get(0).toString());
        StringBuilder buf = new StringBuilder();
        buf.append("extract( ").append(datePart).append(" from (").append(parsedArgs.get(2)).append("-").append(parsedArgs.get(1)).append("))");
        return buf.toString();
    }

    private static String constantToDatePart(String type) throws SQLException {
        if (!type.startsWith(SQL_TSI_ROOT)) {
            throw new PSQLException(GT.tr("Interval {0} not yet implemented", type), PSQLState.SYNTAX_ERROR);
        }
        String shortType = type.substring(SQL_TSI_ROOT.length());
        if (SQL_TSI_DAY.equalsIgnoreCase(shortType)) {
            return "day";
        }
        if (SQL_TSI_SECOND.equalsIgnoreCase(shortType)) {
            return SECOND;
        }
        if (SQL_TSI_HOUR.equalsIgnoreCase(shortType)) {
            return HOUR;
        }
        if (SQL_TSI_MINUTE.equalsIgnoreCase(shortType)) {
            return MINUTE;
        }
        if (SQL_TSI_FRAC_SECOND.equalsIgnoreCase(shortType)) {
            throw new PSQLException(GT.tr("Interval {0} not yet implemented", "SQL_TSI_FRAC_SECOND"), PSQLState.SYNTAX_ERROR);
        }
        throw new PSQLException(GT.tr("Interval {0} not yet implemented", type), PSQLState.SYNTAX_ERROR);
    }

    public static String sqldatabase(List<?> parsedArgs) throws SQLException {
        if (!parsedArgs.isEmpty()) {
            throw new PSQLException(GT.tr("{0} function doesn''t take any argument.", DATABASE), PSQLState.SYNTAX_ERROR);
        }
        return "current_database()";
    }

    public static String sqlifnull(List<?> parsedArgs) throws SQLException {
        return EscapedFunctions.twoArgumentsFunctionCall("coalesce(", IFNULL, parsedArgs);
    }

    public static String sqluser(List<?> parsedArgs) throws SQLException {
        if (!parsedArgs.isEmpty()) {
            throw new PSQLException(GT.tr("{0} function doesn''t take any argument.", USER), PSQLState.SYNTAX_ERROR);
        }
        return USER;
    }

    private static String singleArgumentFunctionCall(String call, String functionName, List<?> parsedArgs) throws PSQLException {
        if (parsedArgs.size() != 1) {
            throw new PSQLException(GT.tr("{0} function takes one and only one argument.", functionName), PSQLState.SYNTAX_ERROR);
        }
        StringBuilder buf = new StringBuilder();
        buf.append(call);
        buf.append(parsedArgs.get(0));
        return buf.append(')').toString();
    }

    private static String twoArgumentsFunctionCall(String call, String functionName, List<?> parsedArgs) throws PSQLException {
        if (parsedArgs.size() != 2) {
            throw new PSQLException(GT.tr("{0} function takes two and only two arguments.", functionName), PSQLState.SYNTAX_ERROR);
        }
        StringBuilder buf = new StringBuilder();
        buf.append(call);
        buf.append(parsedArgs.get(0)).append(',').append(parsedArgs.get(1));
        return buf.append(')').toString();
    }
}

