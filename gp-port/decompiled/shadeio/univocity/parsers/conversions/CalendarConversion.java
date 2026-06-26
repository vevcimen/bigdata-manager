/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.conversions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import shadeio.univocity.parsers.common.ArgumentUtils;
import shadeio.univocity.parsers.conversions.DateConversion;
import shadeio.univocity.parsers.conversions.FormattedConversion;
import shadeio.univocity.parsers.conversions.ObjectConversion;

public class CalendarConversion
extends ObjectConversion<Calendar>
implements FormattedConversion<SimpleDateFormat> {
    private final DateConversion dateConversion;

    public CalendarConversion(TimeZone timeZone, Locale locale, Calendar valueIfStringIsNull, String valueIfObjectIsNull, String ... dateFormats) {
        super(valueIfStringIsNull, valueIfObjectIsNull);
        ArgumentUtils.noNulls("Date formats", dateFormats);
        this.dateConversion = new DateConversion(locale, dateFormats);
    }

    public CalendarConversion(Locale locale, Calendar valueIfStringIsNull, String valueIfObjectIsNull, String ... dateFormats) {
        super(valueIfStringIsNull, valueIfObjectIsNull);
        ArgumentUtils.noNulls("Date formats", dateFormats);
        this.dateConversion = new DateConversion(locale, dateFormats);
    }

    public CalendarConversion(Calendar valueIfStringIsNull, String valueIfObjectIsNull, String ... dateFormats) {
        super(valueIfStringIsNull, valueIfObjectIsNull);
        ArgumentUtils.noNulls("Date formats", dateFormats);
        this.dateConversion = new DateConversion(Locale.getDefault(), dateFormats);
    }

    public CalendarConversion(Locale locale, String ... dateFormats) {
        this(locale, (Calendar)null, (String)null, dateFormats);
    }

    public CalendarConversion(String ... dateFormats) {
        this(Locale.getDefault(), (Calendar)null, (String)null, dateFormats);
    }

    @Override
    public String revert(Calendar input) {
        if (input == null) {
            return super.revert((Object)null);
        }
        return this.dateConversion.revert(input.getTime());
    }

    @Override
    protected Calendar fromString(String input) {
        Date date = (Date)this.dateConversion.execute(input);
        Calendar out = Calendar.getInstance();
        out.setTime(date);
        out.setTimeZone(this.dateConversion.getTimeZone());
        return out;
    }

    public SimpleDateFormat[] getFormatterObjects() {
        return this.dateConversion.getFormatterObjects();
    }
}

