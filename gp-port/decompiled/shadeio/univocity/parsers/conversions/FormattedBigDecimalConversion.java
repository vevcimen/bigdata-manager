/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.conversions;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import shadeio.univocity.parsers.conversions.NumericConversion;

public class FormattedBigDecimalConversion
extends NumericConversion<BigDecimal> {
    public FormattedBigDecimalConversion(BigDecimal valueIfStringIsNull, String valueIfObjectIsNull, String ... numericFormats) {
        super(valueIfStringIsNull, valueIfObjectIsNull, numericFormats);
    }

    public FormattedBigDecimalConversion(BigDecimal valueIfStringIsNull, String valueIfObjectIsNull) {
        super(valueIfStringIsNull, valueIfObjectIsNull);
    }

    public FormattedBigDecimalConversion(String ... numericFormats) {
        super(null, (String)null, numericFormats);
    }

    public FormattedBigDecimalConversion(DecimalFormat ... numericFormatters) {
        super(numericFormatters);
    }

    public FormattedBigDecimalConversion() {
    }

    @Override
    protected void configureFormatter(DecimalFormat formatter) {
        formatter.setParseBigDecimal(true);
    }
}

