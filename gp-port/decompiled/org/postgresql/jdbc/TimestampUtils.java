/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.jdbc;

import java.lang.reflect.Field;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.chrono.IsoEra;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Objects;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.PolyNull;
import org.postgresql.core.JavaVersion;
import org.postgresql.core.Provider;
import org.postgresql.util.ByteConverter;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.internal.Nullness;

public class TimestampUtils {
    private static final int ONEDAY = 86400000;
    private static final char[] ZEROS;
    private static final char[][] NUMBERS;
    private static final HashMap<String, TimeZone> GMT_ZONES;
    private static final int MAX_NANOS_BEFORE_WRAP_ON_ROUND = 999999500;
    private static final Duration ONE_MICROSECOND;
    private static final LocalTime MAX_TIME;
    private static final OffsetDateTime MAX_OFFSET_DATETIME;
    private static final LocalDateTime MAX_LOCAL_DATETIME;
    private static final LocalDate MIN_LOCAL_DATE;
    private static final LocalDateTime MIN_LOCAL_DATETIME;
    private static final OffsetDateTime MIN_OFFSET_DATETIME;
    private static final Duration PG_EPOCH_DIFF;
    private static final @Nullable Field DEFAULT_TIME_ZONE_FIELD;
    private static final TimeZone UTC_TIMEZONE;
    private @Nullable TimeZone prevDefaultZoneFieldValue;
    private @Nullable TimeZone defaultTimeZoneCache;
    private final StringBuilder sbuf = new StringBuilder();
    private final Calendar calendarWithUserTz = new GregorianCalendar();
    private @Nullable Calendar calCache;
    private @Nullable ZoneOffset calCacheZone;
    private final boolean usesDouble;
    private final Provider<TimeZone> timeZoneProvider;

    public TimestampUtils(boolean usesDouble, Provider<TimeZone> timeZoneProvider) {
        this.usesDouble = usesDouble;
        this.timeZoneProvider = timeZoneProvider;
    }

    private Calendar getCalendar(ZoneOffset offset) {
        if (this.calCache != null && Objects.equals(offset, this.calCacheZone)) {
            return this.calCache;
        }
        String tzid = offset.getTotalSeconds() == 0 ? "UTC" : "GMT".concat(offset.getId());
        SimpleTimeZone syntheticTZ = new SimpleTimeZone(offset.getTotalSeconds() * 1000, tzid);
        this.calCache = new GregorianCalendar(syntheticTZ);
        this.calCacheZone = offset;
        return this.calCache;
    }

    private ParsedTimestamp parseBackendTimestamp(String str) throws SQLException {
        char[] s2 = str.toCharArray();
        int slen = s2.length;
        ParsedTimestamp result = new ParsedTimestamp();
        try {
            char sep;
            int start = TimestampUtils.skipWhitespace(s2, 0);
            int end = TimestampUtils.firstNonDigit(s2, start);
            if (TimestampUtils.charAt(s2, end) == '-') {
                result.hasDate = true;
                result.year = TimestampUtils.number(s2, start, end);
                start = end + 1;
                end = TimestampUtils.firstNonDigit(s2, start);
                result.month = TimestampUtils.number(s2, start, end);
                sep = TimestampUtils.charAt(s2, end);
                if (sep != '-') {
                    throw new NumberFormatException("Expected date to be dash-separated, got '" + sep + "'");
                }
                start = end + 1;
                end = TimestampUtils.firstNonDigit(s2, start);
                result.day = TimestampUtils.number(s2, start, end);
                start = TimestampUtils.skipWhitespace(s2, end);
            }
            if (Character.isDigit(TimestampUtils.charAt(s2, start))) {
                result.hasTime = true;
                end = TimestampUtils.firstNonDigit(s2, start);
                result.hour = TimestampUtils.number(s2, start, end);
                sep = TimestampUtils.charAt(s2, end);
                if (sep != ':') {
                    throw new NumberFormatException("Expected time to be colon-separated, got '" + sep + "'");
                }
                start = end + 1;
                end = TimestampUtils.firstNonDigit(s2, start);
                result.minute = TimestampUtils.number(s2, start, end);
                sep = TimestampUtils.charAt(s2, end);
                if (sep != ':') {
                    throw new NumberFormatException("Expected time to be colon-separated, got '" + sep + "'");
                }
                start = end + 1;
                end = TimestampUtils.firstNonDigit(s2, start);
                result.second = TimestampUtils.number(s2, start, end);
                start = end;
                if (TimestampUtils.charAt(s2, start) == '.') {
                    end = TimestampUtils.firstNonDigit(s2, start + 1);
                    int num = TimestampUtils.number(s2, start + 1, end);
                    for (int numlength = end - (start + 1); numlength < 9; ++numlength) {
                        num *= 10;
                    }
                    result.nanos = num;
                    start = end;
                }
                start = TimestampUtils.skipWhitespace(s2, start);
            }
            if ((sep = TimestampUtils.charAt(s2, start)) == '-' || sep == '+') {
                int tzmin;
                result.hasOffset = true;
                int tzsign = sep == '-' ? -1 : 1;
                end = TimestampUtils.firstNonDigit(s2, start + 1);
                int tzhr = TimestampUtils.number(s2, start + 1, end);
                start = end;
                sep = TimestampUtils.charAt(s2, start);
                if (sep == ':') {
                    end = TimestampUtils.firstNonDigit(s2, start + 1);
                    tzmin = TimestampUtils.number(s2, start + 1, end);
                    start = end;
                } else {
                    tzmin = 0;
                }
                int tzsec = 0;
                sep = TimestampUtils.charAt(s2, start);
                if (sep == ':') {
                    end = TimestampUtils.firstNonDigit(s2, start + 1);
                    tzsec = TimestampUtils.number(s2, start + 1, end);
                    start = end;
                }
                result.offset = ZoneOffset.ofHoursMinutesSeconds(tzsign * tzhr, tzsign * tzmin, tzsign * tzsec);
                start = TimestampUtils.skipWhitespace(s2, start);
            }
            if (result.hasDate && start < slen) {
                String eraString = new String(s2, start, slen - start);
                if (eraString.startsWith("AD")) {
                    result.era = 1;
                    start += 2;
                } else if (eraString.startsWith("BC")) {
                    result.era = 0;
                    start += 2;
                }
            }
            if (start < slen) {
                throw new NumberFormatException("Trailing junk on timestamp: '" + new String(s2, start, slen - start) + "'");
            }
            if (!result.hasTime && !result.hasDate) {
                throw new NumberFormatException("Timestamp has neither date nor time");
            }
        }
        catch (NumberFormatException nfe) {
            throw new PSQLException(GT.tr("Bad value for type timestamp/date/time: {0}", str), PSQLState.BAD_DATETIME_FORMAT, (Throwable)nfe);
        }
        return result;
    }

    public synchronized @PolyNull Timestamp toTimestamp(@Nullable Calendar cal, @PolyNull String s2) throws SQLException {
        if (s2 == null) {
            return null;
        }
        int slen = s2.length();
        if (slen == 8 && s2.equals("infinity")) {
            return new Timestamp(9223372036825200000L);
        }
        if (slen == 9 && s2.equals("-infinity")) {
            return new Timestamp(-9223372036832400000L);
        }
        ParsedTimestamp ts = this.parseBackendTimestamp(s2);
        Calendar useCal = ts.hasOffset ? this.getCalendar(ts.offset) : this.setupCalendar(cal);
        useCal.set(0, ts.era);
        useCal.set(1, ts.year);
        useCal.set(2, ts.month - 1);
        useCal.set(5, ts.day);
        useCal.set(11, ts.hour);
        useCal.set(12, ts.minute);
        useCal.set(13, ts.second);
        useCal.set(14, 0);
        Timestamp result = new Timestamp(useCal.getTimeInMillis());
        result.setNanos(ts.nanos);
        return result;
    }

    public @PolyNull LocalTime toLocalTime(@PolyNull String s2) throws SQLException {
        if (s2 == null) {
            return null;
        }
        if (s2.equals("24:00:00")) {
            return LocalTime.MAX;
        }
        try {
            return LocalTime.parse(s2);
        }
        catch (DateTimeParseException nfe) {
            throw new PSQLException(GT.tr("Bad value for type timestamp/date/time: {0}", s2), PSQLState.BAD_DATETIME_FORMAT, (Throwable)nfe);
        }
    }

    public OffsetTime toOffsetTimeBin(byte[] bytes) throws PSQLException {
        long micros;
        if (bytes.length != 12) {
            throw new PSQLException(GT.tr("Unsupported binary encoding of {0}.", "time"), PSQLState.BAD_DATETIME_FORMAT);
        }
        if (this.usesDouble) {
            double seconds = ByteConverter.float8(bytes, 0);
            micros = (long)(seconds * 1000000.0);
        } else {
            micros = ByteConverter.int8(bytes, 0);
        }
        ZoneOffset timeOffset = ZoneOffset.ofTotalSeconds(-ByteConverter.int4(bytes, 8));
        return OffsetTime.of(LocalTime.ofNanoOfDay(Math.multiplyExact(micros, 1000L)), timeOffset);
    }

    public @PolyNull OffsetTime toOffsetTime(@PolyNull String s2) throws SQLException {
        if (s2 == null) {
            return null;
        }
        if (s2.startsWith("24:00:00")) {
            return OffsetTime.MAX;
        }
        ParsedTimestamp ts = this.parseBackendTimestamp(s2);
        return OffsetTime.of(ts.hour, ts.minute, ts.second, ts.nanos, ts.offset);
    }

    public @PolyNull LocalDateTime toLocalDateTime(@PolyNull String s2) throws SQLException {
        if (s2 == null) {
            return null;
        }
        int slen = s2.length();
        if (slen == 8 && s2.equals("infinity")) {
            return LocalDateTime.MAX;
        }
        if (slen == 9 && s2.equals("-infinity")) {
            return LocalDateTime.MIN;
        }
        ParsedTimestamp ts = this.parseBackendTimestamp(s2);
        LocalDateTime result = LocalDateTime.of(ts.year, ts.month, ts.day, ts.hour, ts.minute, ts.second, ts.nanos);
        if (ts.era == 0) {
            return result.with(ChronoField.ERA, IsoEra.BCE.getValue());
        }
        return result;
    }

    @Deprecated
    public OffsetDateTime toOffsetDateTime(Time t) {
        return t.toLocalTime().atDate(LocalDate.of(1970, 1, 1)).atOffset(ZoneOffset.UTC);
    }

    public @PolyNull OffsetDateTime toOffsetDateTime(@PolyNull String s2) throws SQLException {
        if (s2 == null) {
            return null;
        }
        int slen = s2.length();
        if (slen == 8 && s2.equals("infinity")) {
            return OffsetDateTime.MAX;
        }
        if (slen == 9 && s2.equals("-infinity")) {
            return OffsetDateTime.MIN;
        }
        ParsedTimestamp ts = this.parseBackendTimestamp(s2);
        OffsetDateTime result = OffsetDateTime.of(ts.year, ts.month, ts.day, ts.hour, ts.minute, ts.second, ts.nanos, ts.offset);
        if (ts.era == 0) {
            return result.with(ChronoField.ERA, IsoEra.BCE.getValue());
        }
        return result;
    }

    public OffsetDateTime toOffsetDateTimeBin(byte[] bytes) throws PSQLException {
        ParsedBinaryTimestamp parsedTimestamp = this.toProlepticParsedTimestampBin(bytes);
        if (parsedTimestamp.infinity == Infinity.POSITIVE) {
            return OffsetDateTime.MAX;
        }
        if (parsedTimestamp.infinity == Infinity.NEGATIVE) {
            return OffsetDateTime.MIN;
        }
        Instant instant = Instant.ofEpochSecond(parsedTimestamp.millis / 1000L, parsedTimestamp.nanos);
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    public synchronized @PolyNull Time toTime(@Nullable Calendar cal, @PolyNull String s2) throws SQLException {
        Calendar useCal;
        if (s2 == null) {
            return null;
        }
        ParsedTimestamp ts = this.parseBackendTimestamp(s2);
        Calendar calendar = useCal = ts.hasOffset ? this.getCalendar(ts.offset) : this.setupCalendar(cal);
        if (!ts.hasOffset) {
            useCal.set(0, ts.era);
            useCal.set(1, ts.year);
            useCal.set(2, ts.month - 1);
            useCal.set(5, ts.day);
        } else {
            useCal.set(0, 1);
            useCal.set(1, 1970);
            useCal.set(2, 0);
            useCal.set(5, 1);
        }
        useCal.set(11, ts.hour);
        useCal.set(12, ts.minute);
        useCal.set(13, ts.second);
        useCal.set(14, 0);
        long timeMillis = useCal.getTimeInMillis() + (long)(ts.nanos / 1000000);
        if (ts.hasOffset || ts.year == 1970 && ts.era == 1) {
            return new Time(timeMillis);
        }
        return this.convertToTime(timeMillis, useCal.getTimeZone());
    }

    public synchronized @PolyNull Date toDate(@Nullable Calendar cal, @PolyNull String s2) throws SQLException {
        Timestamp timestamp = this.toTimestamp(cal, s2);
        if (timestamp == null) {
            return null;
        }
        return this.convertToDate(timestamp.getTime(), cal == null ? null : cal.getTimeZone());
    }

    private Calendar setupCalendar(@Nullable Calendar cal) {
        TimeZone timeZone = cal == null ? null : cal.getTimeZone();
        return this.getSharedCalendar(timeZone);
    }

    public Calendar getSharedCalendar(@Nullable TimeZone timeZone) {
        if (timeZone == null) {
            timeZone = this.getDefaultTz();
        }
        Calendar tmp = this.calendarWithUserTz;
        tmp.setTimeZone(timeZone);
        return tmp;
    }

    private static boolean nanosExceed499(int nanos) {
        return nanos % 1000 > 499;
    }

    public synchronized String toString(@Nullable Calendar cal, Timestamp x) {
        return this.toString(cal, x, true);
    }

    public synchronized String toString(@Nullable Calendar cal, Timestamp x, boolean withTimeZone) {
        if (x.getTime() == 9223372036825200000L) {
            return "infinity";
        }
        if (x.getTime() == -9223372036832400000L) {
            return "-infinity";
        }
        cal = this.setupCalendar(cal);
        long timeMillis = x.getTime();
        int nanos = x.getNanos();
        if (nanos >= 999999500) {
            nanos = 0;
            ++timeMillis;
        } else if (TimestampUtils.nanosExceed499(nanos)) {
            nanos += 1000 - nanos % 1000;
        }
        cal.setTimeInMillis(timeMillis);
        this.sbuf.setLength(0);
        TimestampUtils.appendDate(this.sbuf, cal);
        this.sbuf.append(' ');
        TimestampUtils.appendTime(this.sbuf, cal, nanos);
        if (withTimeZone) {
            this.appendTimeZone(this.sbuf, cal);
        }
        TimestampUtils.appendEra(this.sbuf, cal);
        return this.sbuf.toString();
    }

    public synchronized String toString(@Nullable Calendar cal, Date x) {
        return this.toString(cal, x, true);
    }

    public synchronized String toString(@Nullable Calendar cal, Date x, boolean withTimeZone) {
        if (x.getTime() == 9223372036825200000L) {
            return "infinity";
        }
        if (x.getTime() == -9223372036832400000L) {
            return "-infinity";
        }
        cal = this.setupCalendar(cal);
        cal.setTime(x);
        this.sbuf.setLength(0);
        TimestampUtils.appendDate(this.sbuf, cal);
        TimestampUtils.appendEra(this.sbuf, cal);
        if (withTimeZone) {
            this.sbuf.append(' ');
            this.appendTimeZone(this.sbuf, cal);
        }
        return this.sbuf.toString();
    }

    public synchronized String toString(@Nullable Calendar cal, Time x) {
        return this.toString(cal, x, true);
    }

    public synchronized String toString(@Nullable Calendar cal, Time x, boolean withTimeZone) {
        cal = this.setupCalendar(cal);
        cal.setTime(x);
        this.sbuf.setLength(0);
        TimestampUtils.appendTime(this.sbuf, cal, cal.get(14) * 1000000);
        if (withTimeZone) {
            this.appendTimeZone(this.sbuf, cal);
        }
        return this.sbuf.toString();
    }

    private static void appendDate(StringBuilder sb, Calendar cal) {
        int year = cal.get(1);
        int month = cal.get(2) + 1;
        int day = cal.get(5);
        TimestampUtils.appendDate(sb, year, month, day);
    }

    private static void appendDate(StringBuilder sb, int year, int month, int day) {
        int prevLength = sb.length();
        sb.append(year);
        int leadingZerosForYear = 4 - (sb.length() - prevLength);
        if (leadingZerosForYear > 0) {
            sb.insert(prevLength, ZEROS, 0, leadingZerosForYear);
        }
        sb.append('-');
        sb.append(NUMBERS[month]);
        sb.append('-');
        sb.append(NUMBERS[day]);
    }

    private static void appendTime(StringBuilder sb, Calendar cal, int nanos) {
        int hours = cal.get(11);
        int minutes = cal.get(12);
        int seconds = cal.get(13);
        TimestampUtils.appendTime(sb, hours, minutes, seconds, nanos);
    }

    private static void appendTime(StringBuilder sb, int hours, int minutes, int seconds, int nanos) {
        sb.append(NUMBERS[hours]);
        sb.append(':');
        sb.append(NUMBERS[minutes]);
        sb.append(':');
        sb.append(NUMBERS[seconds]);
        if (nanos < 1000) {
            return;
        }
        sb.append('.');
        int len = sb.length();
        sb.append(nanos / 1000);
        int needZeros = 6 - (sb.length() - len);
        if (needZeros > 0) {
            sb.insert(len, ZEROS, 0, needZeros);
        }
        int end = sb.length() - 1;
        while (sb.charAt(end) == '0') {
            sb.deleteCharAt(end);
            --end;
        }
    }

    private void appendTimeZone(StringBuilder sb, Calendar cal) {
        int offset = (cal.get(15) + cal.get(16)) / 1000;
        this.appendTimeZone(sb, offset);
    }

    private void appendTimeZone(StringBuilder sb, int offset) {
        int absoff = Math.abs(offset);
        int hours = absoff / 60 / 60;
        int mins = (absoff - hours * 60 * 60) / 60;
        int secs = absoff - hours * 60 * 60 - mins * 60;
        sb.append(offset >= 0 ? "+" : "-");
        sb.append(NUMBERS[hours]);
        if (mins == 0 && secs == 0) {
            return;
        }
        sb.append(':');
        sb.append(NUMBERS[mins]);
        if (secs != 0) {
            sb.append(':');
            sb.append(NUMBERS[secs]);
        }
    }

    private static void appendEra(StringBuilder sb, Calendar cal) {
        if (cal.get(0) == 0) {
            sb.append(" BC");
        }
    }

    public synchronized String toString(LocalDate localDate) {
        if (LocalDate.MAX.equals(localDate)) {
            return "infinity";
        }
        if (localDate.isBefore(MIN_LOCAL_DATE)) {
            return "-infinity";
        }
        this.sbuf.setLength(0);
        TimestampUtils.appendDate(this.sbuf, localDate);
        TimestampUtils.appendEra(this.sbuf, localDate);
        return this.sbuf.toString();
    }

    public synchronized String toString(LocalTime localTime) {
        this.sbuf.setLength(0);
        if (localTime.isAfter(MAX_TIME)) {
            return "24:00:00";
        }
        int nano = localTime.getNano();
        if (TimestampUtils.nanosExceed499(nano)) {
            localTime = localTime.plus(ONE_MICROSECOND);
        }
        TimestampUtils.appendTime(this.sbuf, localTime);
        return this.sbuf.toString();
    }

    public synchronized String toString(OffsetTime offsetTime) {
        this.sbuf.setLength(0);
        LocalTime localTime = offsetTime.toLocalTime();
        if (localTime.isAfter(MAX_TIME)) {
            this.sbuf.append("24:00:00");
            this.appendTimeZone(this.sbuf, offsetTime.getOffset());
            return this.sbuf.toString();
        }
        int nano = offsetTime.getNano();
        if (TimestampUtils.nanosExceed499(nano)) {
            offsetTime = offsetTime.plus(ONE_MICROSECOND);
        }
        TimestampUtils.appendTime(this.sbuf, localTime);
        this.appendTimeZone(this.sbuf, offsetTime.getOffset());
        return this.sbuf.toString();
    }

    public synchronized String toString(OffsetDateTime offsetDateTime) {
        if (offsetDateTime.isAfter(MAX_OFFSET_DATETIME)) {
            return "infinity";
        }
        if (offsetDateTime.isBefore(MIN_OFFSET_DATETIME)) {
            return "-infinity";
        }
        this.sbuf.setLength(0);
        int nano = offsetDateTime.getNano();
        if (TimestampUtils.nanosExceed499(nano)) {
            offsetDateTime = offsetDateTime.plus(ONE_MICROSECOND);
        }
        LocalDateTime localDateTime = offsetDateTime.toLocalDateTime();
        LocalDate localDate = localDateTime.toLocalDate();
        TimestampUtils.appendDate(this.sbuf, localDate);
        this.sbuf.append(' ');
        TimestampUtils.appendTime(this.sbuf, localDateTime.toLocalTime());
        this.appendTimeZone(this.sbuf, offsetDateTime.getOffset());
        TimestampUtils.appendEra(this.sbuf, localDate);
        return this.sbuf.toString();
    }

    public synchronized String toString(LocalDateTime localDateTime) {
        if (localDateTime.isAfter(MAX_LOCAL_DATETIME)) {
            return "infinity";
        }
        if (localDateTime.isBefore(MIN_LOCAL_DATETIME)) {
            return "-infinity";
        }
        ZonedDateTime zonedDateTime = localDateTime.atZone(this.getDefaultTz().toZoneId());
        return this.toString(zonedDateTime.toOffsetDateTime());
    }

    private static void appendDate(StringBuilder sb, LocalDate localDate) {
        int year = localDate.get(ChronoField.YEAR_OF_ERA);
        int month = localDate.getMonthValue();
        int day = localDate.getDayOfMonth();
        TimestampUtils.appendDate(sb, year, month, day);
    }

    private static void appendTime(StringBuilder sb, LocalTime localTime) {
        int hours = localTime.getHour();
        int minutes = localTime.getMinute();
        int seconds = localTime.getSecond();
        int nanos = localTime.getNano();
        TimestampUtils.appendTime(sb, hours, minutes, seconds, nanos);
    }

    private void appendTimeZone(StringBuilder sb, ZoneOffset offset) {
        int offsetSeconds = offset.getTotalSeconds();
        this.appendTimeZone(sb, offsetSeconds);
    }

    private static void appendEra(StringBuilder sb, LocalDate localDate) {
        if (localDate.get(ChronoField.ERA) == IsoEra.BCE.getValue()) {
            sb.append(" BC");
        }
    }

    private static int skipWhitespace(char[] s2, int start) {
        int slen = s2.length;
        for (int i = start; i < slen; ++i) {
            if (Character.isSpace(s2[i])) continue;
            return i;
        }
        return slen;
    }

    private static int firstNonDigit(char[] s2, int start) {
        int slen = s2.length;
        for (int i = start; i < slen; ++i) {
            if (Character.isDigit(s2[i])) continue;
            return i;
        }
        return slen;
    }

    private static int number(char[] s2, int start, int end) {
        if (start >= end) {
            throw new NumberFormatException();
        }
        int n = 0;
        for (int i = start; i < end; ++i) {
            n = 10 * n + (s2[i] - 48);
        }
        return n;
    }

    private static char charAt(char[] s2, int pos) {
        if (pos >= 0 && pos < s2.length) {
            return s2[pos];
        }
        return '\u0000';
    }

    public Date toDateBin(@Nullable TimeZone tz, byte[] bytes) throws PSQLException {
        long secs;
        long millis;
        if (bytes.length != 4) {
            throw new PSQLException(GT.tr("Unsupported binary encoding of {0}.", "date"), PSQLState.BAD_DATETIME_FORMAT);
        }
        int days = ByteConverter.int4(bytes, 0);
        if (tz == null) {
            tz = this.getDefaultTz();
        }
        millis = (millis = (secs = TimestampUtils.toJavaSecs((long)days * 86400L)) * 1000L) <= -185543533774800000L ? -9223372036832400000L : (millis >= 185543533774800000L ? 9223372036825200000L : this.guessTimestamp(millis, tz));
        return new Date(millis);
    }

    private TimeZone getDefaultTz() {
        TimeZone tz;
        if (DEFAULT_TIME_ZONE_FIELD != null) {
            try {
                TimeZone defaultTimeZone = (TimeZone)DEFAULT_TIME_ZONE_FIELD.get(null);
                if (defaultTimeZone == this.prevDefaultZoneFieldValue) {
                    return Nullness.castNonNull(this.defaultTimeZoneCache);
                }
                this.prevDefaultZoneFieldValue = defaultTimeZone;
            }
            catch (Exception defaultTimeZone) {
                // empty catch block
            }
        }
        this.defaultTimeZoneCache = tz = TimeZone.getDefault();
        return tz;
    }

    public boolean hasFastDefaultTimeZone() {
        return DEFAULT_TIME_ZONE_FIELD != null;
    }

    public Time toTimeBin(@Nullable TimeZone tz, byte[] bytes) throws PSQLException {
        long millis;
        if (bytes.length != 8 && bytes.length != 12) {
            throw new PSQLException(GT.tr("Unsupported binary encoding of {0}.", "time"), PSQLState.BAD_DATETIME_FORMAT);
        }
        if (this.usesDouble) {
            double time = ByteConverter.float8(bytes, 0);
            millis = (long)(time * 1000.0);
        } else {
            long time = ByteConverter.int8(bytes, 0);
            millis = time / 1000L;
        }
        if (bytes.length == 12) {
            int timeOffset = ByteConverter.int4(bytes, 8);
            return new Time(millis -= (long)(timeOffset *= -1000));
        }
        if (tz == null) {
            tz = this.getDefaultTz();
        }
        millis = this.guessTimestamp(millis, tz);
        return this.convertToTime(millis, tz);
    }

    public LocalTime toLocalTimeBin(byte[] bytes) throws PSQLException {
        long micros;
        if (bytes.length != 8) {
            throw new PSQLException(GT.tr("Unsupported binary encoding of {0}.", "time"), PSQLState.BAD_DATETIME_FORMAT);
        }
        if (this.usesDouble) {
            double seconds = ByteConverter.float8(bytes, 0);
            micros = (long)(seconds * 1000000.0);
        } else {
            micros = ByteConverter.int8(bytes, 0);
        }
        return LocalTime.ofNanoOfDay(Math.multiplyExact(micros, 1000L));
    }

    public Timestamp toTimestampBin(@Nullable TimeZone tz, byte[] bytes, boolean timestamptz) throws PSQLException {
        ParsedBinaryTimestamp parsedTimestamp = this.toParsedTimestampBin(tz, bytes, timestamptz);
        if (parsedTimestamp.infinity == Infinity.POSITIVE) {
            return new Timestamp(9223372036825200000L);
        }
        if (parsedTimestamp.infinity == Infinity.NEGATIVE) {
            return new Timestamp(-9223372036832400000L);
        }
        Timestamp ts = new Timestamp(parsedTimestamp.millis);
        ts.setNanos(parsedTimestamp.nanos);
        return ts;
    }

    private ParsedBinaryTimestamp toParsedTimestampBinPlain(byte[] bytes) throws PSQLException {
        int nanos;
        long secs;
        if (bytes.length != 8) {
            throw new PSQLException(GT.tr("Unsupported binary encoding of {0}.", "timestamp"), PSQLState.BAD_DATETIME_FORMAT);
        }
        if (this.usesDouble) {
            double time = ByteConverter.float8(bytes, 0);
            if (time == Double.POSITIVE_INFINITY) {
                ParsedBinaryTimestamp ts = new ParsedBinaryTimestamp();
                ts.infinity = Infinity.POSITIVE;
                return ts;
            }
            if (time == Double.NEGATIVE_INFINITY) {
                ParsedBinaryTimestamp ts = new ParsedBinaryTimestamp();
                ts.infinity = Infinity.NEGATIVE;
                return ts;
            }
            secs = (long)time;
            nanos = (int)((time - (double)secs) * 1000000.0);
        } else {
            long time = ByteConverter.int8(bytes, 0);
            if (time == Long.MAX_VALUE) {
                ParsedBinaryTimestamp ts = new ParsedBinaryTimestamp();
                ts.infinity = Infinity.POSITIVE;
                return ts;
            }
            if (time == Long.MIN_VALUE) {
                ParsedBinaryTimestamp ts = new ParsedBinaryTimestamp();
                ts.infinity = Infinity.NEGATIVE;
                return ts;
            }
            secs = time / 1000000L;
            nanos = (int)(time - secs * 1000000L);
        }
        if (nanos < 0) {
            --secs;
            nanos += 1000000;
        }
        long millis = secs * 1000L;
        ParsedBinaryTimestamp ts = new ParsedBinaryTimestamp();
        ts.millis = millis;
        ts.nanos = nanos *= 1000;
        return ts;
    }

    private ParsedBinaryTimestamp toParsedTimestampBin(@Nullable TimeZone tz, byte[] bytes, boolean timestamptz) throws PSQLException {
        ParsedBinaryTimestamp ts = this.toParsedTimestampBinPlain(bytes);
        if (ts.infinity != null) {
            return ts;
        }
        long secs = ts.millis / 1000L;
        secs = TimestampUtils.toJavaSecs(secs);
        long millis = secs * 1000L;
        if (!timestamptz) {
            millis = this.guessTimestamp(millis, tz);
        }
        ts.millis = millis;
        return ts;
    }

    private ParsedBinaryTimestamp toProlepticParsedTimestampBin(byte[] bytes) throws PSQLException {
        long millis;
        ParsedBinaryTimestamp ts = this.toParsedTimestampBinPlain(bytes);
        if (ts.infinity != null) {
            return ts;
        }
        long secs = ts.millis / 1000L;
        ts.millis = millis = (secs += PG_EPOCH_DIFF.getSeconds()) * 1000L;
        return ts;
    }

    public LocalDateTime toLocalDateTimeBin(byte[] bytes) throws PSQLException {
        ParsedBinaryTimestamp parsedTimestamp = this.toProlepticParsedTimestampBin(bytes);
        if (parsedTimestamp.infinity == Infinity.POSITIVE) {
            return LocalDateTime.MAX;
        }
        if (parsedTimestamp.infinity == Infinity.NEGATIVE) {
            return LocalDateTime.MIN;
        }
        return LocalDateTime.ofEpochSecond(parsedTimestamp.millis / 1000L, parsedTimestamp.nanos, ZoneOffset.UTC);
    }

    public LocalDate toLocalDateBin(byte[] bytes) throws PSQLException {
        if (bytes.length != 4) {
            throw new PSQLException(GT.tr("Unsupported binary encoding of {0}.", "date"), PSQLState.BAD_DATETIME_FORMAT);
        }
        int days = ByteConverter.int4(bytes, 0);
        if (days == Integer.MAX_VALUE) {
            return LocalDate.MAX;
        }
        if (days == Integer.MIN_VALUE) {
            return LocalDate.MIN;
        }
        return LocalDate.ofEpochDay(PG_EPOCH_DIFF.toDays() + (long)days);
    }

    private long guessTimestamp(long millis, @Nullable TimeZone tz) {
        if (tz == null) {
            tz = this.getDefaultTz();
        }
        if (TimestampUtils.isSimpleTimeZone(tz.getID())) {
            return millis - (long)tz.getRawOffset();
        }
        Calendar cal = this.calendarWithUserTz;
        cal.setTimeZone(UTC_TIMEZONE);
        cal.setTimeInMillis(millis);
        int era = cal.get(0);
        int year = cal.get(1);
        int month = cal.get(2);
        int day = cal.get(5);
        int hour = cal.get(11);
        int min2 = cal.get(12);
        int sec = cal.get(13);
        int ms = cal.get(14);
        cal.setTimeZone(tz);
        cal.set(0, era);
        cal.set(1, year);
        cal.set(2, month);
        cal.set(5, day);
        cal.set(11, hour);
        cal.set(12, min2);
        cal.set(13, sec);
        cal.set(14, ms);
        return cal.getTimeInMillis();
    }

    private static boolean isSimpleTimeZone(String id) {
        return id.startsWith("GMT") || id.startsWith("UTC");
    }

    public Date convertToDate(long millis, @Nullable TimeZone tz) {
        if (millis <= -9223372036832400000L || millis >= 9223372036825200000L) {
            return new Date(millis);
        }
        if (tz == null) {
            tz = this.getDefaultTz();
        }
        if (TimestampUtils.isSimpleTimeZone(tz.getID())) {
            int offset = tz.getRawOffset();
            millis += (long)offset;
            millis = TimestampUtils.floorDiv(millis, 86400000L) * 86400000L;
            return new Date(millis -= (long)offset);
        }
        Calendar cal = this.calendarWithUserTz;
        cal.setTimeZone(tz);
        cal.setTimeInMillis(millis);
        cal.set(11, 0);
        cal.set(12, 0);
        cal.set(13, 0);
        cal.set(14, 0);
        return new Date(cal.getTimeInMillis());
    }

    public Time convertToTime(long millis, TimeZone tz) {
        if (tz == null) {
            tz = this.getDefaultTz();
        }
        if (TimestampUtils.isSimpleTimeZone(tz.getID())) {
            int offset = tz.getRawOffset();
            millis += (long)offset;
            millis = TimestampUtils.floorMod(millis, 86400000L);
            return new Time(millis -= (long)offset);
        }
        Calendar cal = this.calendarWithUserTz;
        cal.setTimeZone(tz);
        cal.setTimeInMillis(millis);
        cal.set(0, 1);
        cal.set(1, 1970);
        cal.set(2, 0);
        cal.set(5, 1);
        return new Time(cal.getTimeInMillis());
    }

    public String timeToString(java.util.Date time, boolean withTimeZone) {
        Calendar cal = null;
        if (withTimeZone) {
            cal = this.calendarWithUserTz;
            cal.setTimeZone(this.timeZoneProvider.get());
        }
        if (time instanceof Timestamp) {
            return this.toString(cal, (Timestamp)time, withTimeZone);
        }
        if (time instanceof Time) {
            return this.toString(cal, (Time)time, withTimeZone);
        }
        return this.toString(cal, (Date)time, withTimeZone);
    }

    private static long toJavaSecs(long secs) {
        if ((secs += PG_EPOCH_DIFF.getSeconds()) < -12219292800L && (secs += 864000L) < -14825808000L) {
            int extraLeaps = (int)((secs + 14825808000L) / 3155760000L);
            --extraLeaps;
            extraLeaps -= extraLeaps / 4;
            secs += (long)extraLeaps * 86400L;
        }
        return secs;
    }

    private static long toPgSecs(long secs) {
        if ((secs -= PG_EPOCH_DIFF.getSeconds()) < -13165977600L && (secs -= 864000L) < -15773356800L) {
            int years = (int)((secs + 15773356800L) / -3155823050L);
            ++years;
            years -= years / 4;
            secs += (long)years * 86400L;
        }
        return secs;
    }

    public void toBinDate(@Nullable TimeZone tz, byte[] bytes, Date value) throws PSQLException {
        long millis = value.getTime();
        if (tz == null) {
            tz = this.getDefaultTz();
        }
        millis += (long)tz.getOffset(millis);
        long secs = TimestampUtils.toPgSecs(millis / 1000L);
        ByteConverter.int4(bytes, 0, (int)(secs / 86400L));
    }

    public static TimeZone parseBackendTimeZone(String timeZone) {
        TimeZone tz;
        if (timeZone.startsWith("GMT") && (tz = GMT_ZONES.get(timeZone)) != null) {
            return tz;
        }
        return TimeZone.getTimeZone(timeZone);
    }

    private static long floorDiv(long x, long y) {
        long r = x / y;
        if ((x ^ y) < 0L && r * y != x) {
            --r;
        }
        return r;
    }

    private static long floorMod(long x, long y) {
        return x - TimestampUtils.floorDiv(x, y) * y;
    }

    static {
        Field tzField;
        int i;
        ZEROS = new char[]{'0', '0', '0', '0', '0', '0', '0', '0', '0'};
        GMT_ZONES = new HashMap();
        ONE_MICROSECOND = Duration.ofNanos(1000L);
        MAX_TIME = LocalTime.MAX.minus(Duration.ofNanos(500L));
        MAX_OFFSET_DATETIME = OffsetDateTime.MAX.minus(Duration.ofMillis(500L));
        MAX_LOCAL_DATETIME = LocalDateTime.MAX.minus(Duration.ofMillis(500L));
        MIN_LOCAL_DATE = LocalDate.of(4713, 1, 1).with(ChronoField.ERA, IsoEra.BCE.getValue());
        MIN_LOCAL_DATETIME = MIN_LOCAL_DATE.atStartOfDay();
        MIN_OFFSET_DATETIME = MIN_LOCAL_DATETIME.atOffset(ZoneOffset.UTC);
        PG_EPOCH_DIFF = Duration.between(Instant.EPOCH, LocalDate.of(2000, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC));
        UTC_TIMEZONE = TimeZone.getTimeZone(ZoneOffset.UTC);
        NUMBERS = new char[64][];
        for (i = 0; i < NUMBERS.length; ++i) {
            TimestampUtils.NUMBERS[i] = ((i < 10 ? "0" : "") + Integer.toString(i)).toCharArray();
        }
        for (i = -12; i <= 14; ++i) {
            String pgZoneName;
            TimeZone timeZone;
            if (i == 0) {
                timeZone = TimeZone.getTimeZone("GMT");
                pgZoneName = "GMT";
            } else {
                timeZone = TimeZone.getTimeZone("GMT" + (i <= 0 ? "+" : "-") + Math.abs(i));
                pgZoneName = "GMT" + (i >= 0 ? "+" : "-");
            }
            if (i == 0) {
                GMT_ZONES.put(pgZoneName, timeZone);
                continue;
            }
            GMT_ZONES.put(pgZoneName + Math.abs(i), timeZone);
            GMT_ZONES.put(pgZoneName + new String(NUMBERS[Math.abs(i)]), timeZone);
        }
        try {
            tzField = null;
            if (JavaVersion.getRuntimeVersion().compareTo(JavaVersion.v1_8) <= 0) {
                tzField = TimeZone.class.getDeclaredField("defaultTimeZone");
                tzField.setAccessible(true);
                TimeZone defaultTz = TimeZone.getDefault();
                Object tzFromField = tzField.get(null);
                if (defaultTz == null || !defaultTz.equals(tzFromField)) {
                    tzField = null;
                }
            }
        }
        catch (Exception e) {
            tzField = null;
        }
        DEFAULT_TIME_ZONE_FIELD = tzField;
    }

    static enum Infinity {
        POSITIVE,
        NEGATIVE;

    }

    private static class ParsedBinaryTimestamp {
        @Nullable Infinity infinity = null;
        long millis = 0L;
        int nanos = 0;

        private ParsedBinaryTimestamp() {
        }
    }

    private static class ParsedTimestamp {
        boolean hasDate = false;
        int era = 1;
        int year = 1970;
        int month = 1;
        boolean hasTime = false;
        int day = 1;
        int hour = 0;
        int minute = 0;
        int second = 0;
        int nanos = 0;
        boolean hasOffset = false;
        ZoneOffset offset = ZoneOffset.UTC;

        private ParsedTimestamp() {
        }
    }
}

