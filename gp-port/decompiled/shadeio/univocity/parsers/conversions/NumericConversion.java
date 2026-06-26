/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.conversions;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.util.Arrays;
import shadeio.univocity.parsers.annotations.helpers.AnnotationHelper;
import shadeio.univocity.parsers.common.ArgumentUtils;
import shadeio.univocity.parsers.common.DataProcessingException;
import shadeio.univocity.parsers.conversions.FormattedConversion;
import shadeio.univocity.parsers.conversions.ObjectConversion;

public abstract class NumericConversion<T extends Number>
extends ObjectConversion<T>
implements FormattedConversion<DecimalFormat> {
    private DecimalFormat[] formatters = new DecimalFormat[0];
    private String[] formats = new String[0];
    private final ParsePosition position = new ParsePosition(0);
    private Class<? extends Number> numberType = Number.class;

    public NumericConversion(T valueIfStringIsNull, String valueIfObjectIsNull, String ... numericFormats) {
        super(valueIfStringIsNull, valueIfObjectIsNull);
        ArgumentUtils.noNulls("Numeric formats", numericFormats);
        this.formats = (String[])numericFormats.clone();
        this.formatters = new DecimalFormat[numericFormats.length];
        for (int i = 0; i < numericFormats.length; ++i) {
            String numericFormat = numericFormats[i];
            this.formatters[i] = new DecimalFormat(numericFormat);
            this.configureFormatter(this.formatters[i]);
        }
    }

    public NumericConversion(T valueIfStringIsNull, String valueIfObjectIsNull, DecimalFormat ... numericFormatters) {
        super(valueIfStringIsNull, valueIfObjectIsNull);
        ArgumentUtils.noNulls("Numeric formatters", numericFormatters);
        this.formatters = (DecimalFormat[])numericFormatters.clone();
        this.formats = new String[numericFormatters.length];
        for (int i = 0; i < numericFormatters.length; ++i) {
            this.formats[i] = numericFormatters[i].toPattern();
        }
    }

    public NumericConversion(T valueIfStringIsNull, String valueIfObjectIsNull) {
        super(valueIfStringIsNull, valueIfObjectIsNull);
    }

    public NumericConversion(String ... numericFormats) {
        this((null), (String)null, numericFormats);
    }

    public NumericConversion(DecimalFormat ... numericFormatters) {
        this((null), (String)null, numericFormatters);
    }

    public NumericConversion() {
    }

    public Class<? extends Number> getNumberType() {
        return this.numberType;
    }

    public void setNumberType(Class<? extends Number> numberType) {
        this.numberType = numberType;
    }

    public DecimalFormat[] getFormatterObjects() {
        return this.formatters;
    }

    protected abstract void configureFormatter(DecimalFormat var1);

    @Override
    protected T fromString(String input) {
        for (int i = 0; i < this.formatters.length; ++i) {
            this.position.setIndex(0);
            Number out = this.formatters[i].parse(input, this.position);
            if (this.formatters.length != 1 && this.position.getIndex() != input.length()) continue;
            if (out == null || this.numberType == Number.class) {
                return (T)out;
            }
            if (this.numberType == Double.class) {
                return (T)Double.valueOf(out.doubleValue());
            }
            if (this.numberType == Float.class) {
                return (T)Float.valueOf(out.floatValue());
            }
            if (this.numberType == BigDecimal.class) {
                if (out instanceof BigDecimal) {
                    return (T)out;
                }
                return (T)new BigDecimal(String.valueOf(out));
            }
            if (this.numberType == BigInteger.class) {
                if (out instanceof BigInteger) {
                    return (T)out;
                }
                return (T)BigInteger.valueOf(out.longValue());
            }
            if (this.numberType == Long.class) {
                return (T)Long.valueOf(out.longValue());
            }
            if (this.numberType == Integer.class) {
                return (T)Integer.valueOf(out.intValue());
            }
            if (this.numberType == Short.class) {
                return (T)Short.valueOf(out.shortValue());
            }
            if (this.numberType == Byte.class) {
                return (T)Byte.valueOf(out.byteValue());
            }
            return (T)out;
        }
        DataProcessingException exception = new DataProcessingException("Cannot parse '{value}' as a valid number. Supported formats are: " + Arrays.toString(this.formats));
        exception.setValue(input);
        throw exception;
    }

    @Override
    public String revert(T input) {
        if (input == null) {
            return super.revert((Object)null);
        }
        for (DecimalFormat formatter : this.formatters) {
            try {
                return formatter.format(input);
            }
            catch (Throwable ex) {
            }
        }
        DataProcessingException exception = new DataProcessingException("Cannot format '{value}'. No valid formatters were defined.");
        exception.setValue(input);
        throw exception;
    }

    public void addFormat(String format, String ... formatOptions) {
        DecimalFormat formatter = new DecimalFormat(format);
        this.configureFormatter(formatter);
        AnnotationHelper.applyFormatSettings(formatter, formatOptions);
        this.formats = Arrays.copyOf(this.formats, this.formats.length + 1);
        this.formatters = Arrays.copyOf(this.formatters, this.formatters.length + 1);
        this.formats[this.formats.length - 1] = format;
        this.formatters[this.formatters.length - 1] = formatter;
    }
}

