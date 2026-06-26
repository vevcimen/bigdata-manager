/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.conversions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import shadeio.univocity.parsers.common.ArgumentUtils;
import shadeio.univocity.parsers.common.DataProcessingException;
import shadeio.univocity.parsers.conversions.FormattedConversion;
import shadeio.univocity.parsers.conversions.ObjectConversion;

public class DateConversion
extends ObjectConversion<Date>
implements FormattedConversion<SimpleDateFormat> {
    private final Locale locale;
    private final TimeZone timeZone;
    private final SimpleDateFormat[] parsers;
    private final String[] formats;

    public DateConversion(TimeZone timeZone, Locale locale, Date valueIfStringIsNull, String valueIfObjectIsNull, String ... dateFormats) {
        super(valueIfStringIsNull, valueIfObjectIsNull);
        ArgumentUtils.noNulls("Date formats", dateFormats);
        this.timeZone = timeZone == null ? TimeZone.getDefault() : timeZone;
        this.locale = locale == null ? Locale.getDefault() : locale;
        this.formats = (String[])dateFormats.clone();
        this.parsers = new SimpleDateFormat[dateFormats.length];
        for (int i = 0; i < dateFormats.length; ++i) {
            String dateFormat = dateFormats[i];
            this.parsers[i] = new SimpleDateFormat(dateFormat, this.locale);
            this.parsers[i].setTimeZone(this.timeZone);
        }
    }

    public DateConversion(Locale locale, Date valueIfStringIsNull, String valueIfObjectIsNull, String ... dateFormats) {
        this(TimeZone.getDefault(), locale, valueIfStringIsNull, valueIfObjectIsNull, dateFormats);
    }

    public DateConversion(Date valueIfStringIsNull, String valueIfObjectIsNull, String ... dateFormats) {
        this(Locale.getDefault(), valueIfStringIsNull, valueIfObjectIsNull, dateFormats);
    }

    public DateConversion(Locale locale, String ... dateFormats) {
        this(locale, (Date)null, (String)null, dateFormats);
    }

    public DateConversion(String ... dateFormats) {
        this(Locale.getDefault(), (Date)null, (String)null, dateFormats);
    }

    @Override
    public String revert(Date input) {
        if (input == null) {
            return super.revert((Object)null);
        }
        return this.parsers[0].format(input);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected Date fromString(String input) {
        for (SimpleDateFormat formatter : this.parsers) {
            try {
                SimpleDateFormat simpleDateFormat = formatter;
                synchronized (simpleDateFormat) {
                    return formatter.parse(input);
                }
            }
            catch (ParseException ex) {
            }
        }
        DataProcessingException exception = new DataProcessingException("Cannot parse '{value}' as a valid date of locale '" + this.locale + "'. Supported formats are: " + Arrays.toString(this.formats));
        exception.setValue(input);
        throw exception;
    }

    public SimpleDateFormat[] getFormatterObjects() {
        return this.parsers;
    }

    public TimeZone getTimeZone() {
        return this.timeZone;
    }
}

