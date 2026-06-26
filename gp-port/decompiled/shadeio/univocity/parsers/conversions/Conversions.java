/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.conversions;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import shadeio.univocity.parsers.conversions.BigDecimalConversion;
import shadeio.univocity.parsers.conversions.BigIntegerConversion;
import shadeio.univocity.parsers.conversions.BooleanConversion;
import shadeio.univocity.parsers.conversions.ByteConversion;
import shadeio.univocity.parsers.conversions.CalendarConversion;
import shadeio.univocity.parsers.conversions.CharacterConversion;
import shadeio.univocity.parsers.conversions.DateConversion;
import shadeio.univocity.parsers.conversions.DoubleConversion;
import shadeio.univocity.parsers.conversions.EnumConversion;
import shadeio.univocity.parsers.conversions.EnumSelector;
import shadeio.univocity.parsers.conversions.FloatConversion;
import shadeio.univocity.parsers.conversions.FormattedBigDecimalConversion;
import shadeio.univocity.parsers.conversions.FormattedDateConversion;
import shadeio.univocity.parsers.conversions.IntegerConversion;
import shadeio.univocity.parsers.conversions.LongConversion;
import shadeio.univocity.parsers.conversions.LowerCaseConversion;
import shadeio.univocity.parsers.conversions.NullStringConversion;
import shadeio.univocity.parsers.conversions.NumericConversion;
import shadeio.univocity.parsers.conversions.RegexConversion;
import shadeio.univocity.parsers.conversions.ShortConversion;
import shadeio.univocity.parsers.conversions.ToStringConversion;
import shadeio.univocity.parsers.conversions.TrimConversion;
import shadeio.univocity.parsers.conversions.UpperCaseConversion;
import shadeio.univocity.parsers.conversions.ValidatedConversion;

public class Conversions {
    private static final UpperCaseConversion upperCase = new UpperCaseConversion();
    private static final LowerCaseConversion lowerCase = new LowerCaseConversion();
    private static final TrimConversion trim = new TrimConversion();
    private static final ToStringConversion toString = new ToStringConversion();

    private Conversions() {
    }

    public static ToStringConversion string() {
        return toString;
    }

    public static UpperCaseConversion toUpperCase() {
        return upperCase;
    }

    public static LowerCaseConversion toLowerCase() {
        return lowerCase;
    }

    public static TrimConversion trim() {
        return trim;
    }

    public static TrimConversion trim(int length) {
        return new TrimConversion(length);
    }

    public static RegexConversion replace(String replaceRegex, String replacement) {
        return new RegexConversion(replaceRegex, replacement);
    }

    public static NullStringConversion toNull(String ... nullRepresentations) {
        return new NullStringConversion(nullRepresentations);
    }

    public static DateConversion toDate(Locale locale, String ... dateFormats) {
        return new DateConversion(locale, dateFormats);
    }

    public static DateConversion toDate(String ... dateFormats) {
        return new DateConversion(Locale.getDefault(), dateFormats);
    }

    public static DateConversion toDate(Locale locale, Date dateIfNull, String ... dateFormats) {
        return new DateConversion(locale, dateIfNull, null, dateFormats);
    }

    public static DateConversion toDate(Date dateIfNull, String ... dateFormats) {
        return new DateConversion(Locale.getDefault(), dateIfNull, null, dateFormats);
    }

    public static DateConversion toDate(TimeZone timeZone, Locale locale, Date dateIfNull, String stringIfNull, String ... dateFormats) {
        return new DateConversion(timeZone, locale, dateIfNull, stringIfNull, dateFormats);
    }

    public static DateConversion toDate(Locale locale, Date dateIfNull, String stringIfNull, String ... dateFormats) {
        return new DateConversion(locale, dateIfNull, stringIfNull, dateFormats);
    }

    public static DateConversion toDate(Date dateIfNull, String stringIfNull, String ... dateFormats) {
        return new DateConversion(Locale.getDefault(), dateIfNull, stringIfNull, dateFormats);
    }

    public static CalendarConversion toCalendar(Locale locale, String ... dateFormats) {
        return new CalendarConversion(locale, dateFormats);
    }

    public static CalendarConversion toCalendar(String ... dateFormats) {
        return new CalendarConversion(Locale.getDefault(), dateFormats);
    }

    public static CalendarConversion toCalendar(Locale locale, Calendar dateIfNull, String ... dateFormats) {
        return new CalendarConversion(locale, dateIfNull, null, dateFormats);
    }

    public static CalendarConversion toCalendar(Calendar dateIfNull, String ... dateFormats) {
        return new CalendarConversion(Locale.getDefault(), dateIfNull, null, dateFormats);
    }

    public static CalendarConversion toCalendar(Locale locale, Calendar dateIfNull, String stringIfNull, String ... dateFormats) {
        return new CalendarConversion(TimeZone.getDefault(), locale, dateIfNull, stringIfNull, dateFormats);
    }

    public static CalendarConversion toCalendar(TimeZone timeZone, Locale locale, Calendar dateIfNull, String stringIfNull, String ... dateFormats) {
        return new CalendarConversion(timeZone, locale, dateIfNull, stringIfNull, dateFormats);
    }

    public static CalendarConversion toCalendar(Calendar dateIfNull, String stringIfNull, String ... dateFormats) {
        return new CalendarConversion(Locale.getDefault(), dateIfNull, stringIfNull, dateFormats);
    }

    public static ByteConversion toByte() {
        return new ByteConversion();
    }

    public static ShortConversion toShort() {
        return new ShortConversion();
    }

    public static IntegerConversion toInteger() {
        return new IntegerConversion();
    }

    public static LongConversion toLong() {
        return new LongConversion();
    }

    public static BigIntegerConversion toBigInteger() {
        return new BigIntegerConversion();
    }

    public static FloatConversion toFloat() {
        return new FloatConversion();
    }

    public static DoubleConversion toDouble() {
        return new DoubleConversion();
    }

    public static BigDecimalConversion toBigDecimal() {
        return new BigDecimalConversion();
    }

    public static NumericConversion<Number> formatToNumber(String ... numberFormats) {
        return new NumericConversion<Number>(numberFormats){

            @Override
            protected void configureFormatter(DecimalFormat formatter) {
            }
        };
    }

    public static <T extends Number> NumericConversion<T> formatToNumber(Class<T> numberType, String ... numberFormats) {
        return new NumericConversion<T>(numberFormats){

            @Override
            protected void configureFormatter(DecimalFormat formatter) {
            }
        };
    }

    public static FormattedBigDecimalConversion formatToBigDecimal(String ... numberFormats) {
        return new FormattedBigDecimalConversion(numberFormats);
    }

    public static FormattedBigDecimalConversion formatToBigDecimal(BigDecimal defaultValueForNullString, String ... numberFormats) {
        return new FormattedBigDecimalConversion(defaultValueForNullString, null, numberFormats);
    }

    public static FormattedBigDecimalConversion formatToBigDecimal(BigDecimal defaultValueForNullString, String stringIfNull, String ... numberFormats) {
        return new FormattedBigDecimalConversion(defaultValueForNullString, stringIfNull, numberFormats);
    }

    public static BooleanConversion toBoolean(Boolean defaultValueForNullString, String defaultValueForNullBoolean, String[] valuesForTrue, String[] valuesForFalse) {
        return new BooleanConversion(defaultValueForNullString, defaultValueForNullBoolean, valuesForTrue, valuesForFalse);
    }

    public static BooleanConversion toBoolean(Boolean defaultValueForNullString, String defaultValueForNullBoolean, String valueForTrue, String valueForFalse) {
        return new BooleanConversion(defaultValueForNullString, defaultValueForNullBoolean, new String[]{valueForTrue}, new String[]{valueForFalse});
    }

    public static BooleanConversion toBoolean(String[] valuesForTrue, String[] valuesForFalse) {
        return new BooleanConversion(valuesForTrue, valuesForFalse);
    }

    public static BooleanConversion toBoolean() {
        return Conversions.toBoolean("true", "false");
    }

    public static BooleanConversion toBoolean(String valueForTrue, String valueForFalse) {
        return new BooleanConversion(new String[]{valueForTrue}, new String[]{valueForFalse});
    }

    public static CharacterConversion toChar() {
        return new CharacterConversion();
    }

    public static CharacterConversion toChar(Character defaultValueForNullString, String defaultValueForNullChar) {
        return new CharacterConversion(defaultValueForNullString, defaultValueForNullChar);
    }

    public static CharacterConversion toChar(Character defaultValueForNullString) {
        return new CharacterConversion(defaultValueForNullString, null);
    }

    public static <T extends Enum<T>> EnumConversion<T> toEnum(Class<T> enumType) {
        return new EnumConversion<T>(enumType);
    }

    public static <T extends Enum<T>> EnumConversion<T> toEnum(Class<T> enumType, EnumSelector ... selectors) {
        return Conversions.toEnum(enumType, null, null, null, selectors);
    }

    public static <T extends Enum<T>> EnumConversion<T> toEnum(Class<T> enumType, String customEnumElement, EnumSelector ... selectors) {
        return Conversions.toEnum(enumType, null, null, customEnumElement, new EnumSelector[0]);
    }

    public static <T extends Enum<T>> EnumConversion<T> toEnum(Class<T> enumType, T valueIfStringIsNull, String valueIfEnumIsNull, String customEnumElement, EnumSelector ... selectors) {
        return new EnumConversion<T>(enumType, valueIfStringIsNull, valueIfEnumIsNull, customEnumElement, selectors);
    }

    public static FormattedDateConversion toFormattedDate(String pattern) {
        return Conversions.toFormattedDate(pattern, null, null);
    }

    public static FormattedDateConversion toFormattedDate(String pattern, String valueIfObjectIsNull) {
        return Conversions.toFormattedDate(pattern, null, valueIfObjectIsNull);
    }

    public static FormattedDateConversion toFormattedDate(String pattern, Locale locale) {
        return Conversions.toFormattedDate(pattern, locale, null);
    }

    public static FormattedDateConversion toFormattedDate(String pattern, Locale locale, String valueIfObjectIsNull) {
        return new FormattedDateConversion(pattern, locale, valueIfObjectIsNull);
    }

    public static ValidatedConversion notNull() {
        return Conversions.validate(false, true, null, null);
    }

    public static ValidatedConversion notBlank() {
        return Conversions.validate(false, false, null, null);
    }

    public static ValidatedConversion notBlank(String regexToMatch) {
        return Conversions.validate(false, false, null, null, regexToMatch);
    }

    public static ValidatedConversion validate(boolean nullable, boolean allowBlanks) {
        return new ValidatedConversion(nullable, allowBlanks, null, null, null);
    }

    public static ValidatedConversion validate(boolean nullable, boolean allowBlanks, String[] oneOf, String[] noneOf) {
        return new ValidatedConversion(nullable, allowBlanks, oneOf, noneOf, null);
    }

    public static ValidatedConversion validate(boolean nullable, boolean allowBlanks, String regexToMatch) {
        return new ValidatedConversion(nullable, allowBlanks, null, null, regexToMatch);
    }

    public static ValidatedConversion validate(boolean nullable, boolean allowBlanks, String[] oneOf, String[] noneOf, String regexToMatch) {
        return new ValidatedConversion(nullable, allowBlanks, oneOf, noneOf, regexToMatch);
    }

    public static ValidatedConversion oneOf(String ... oneOf) {
        return new ValidatedConversion(false, false, oneOf, null, null);
    }

    public static ValidatedConversion noneOf(String ... noneOf) {
        return new ValidatedConversion(false, false, null, noneOf, null);
    }
}

