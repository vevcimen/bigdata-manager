/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.conversions;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import shadeio.univocity.parsers.common.ArgumentUtils;
import shadeio.univocity.parsers.common.DataProcessingException;
import shadeio.univocity.parsers.conversions.ObjectConversion;

public class BooleanConversion
extends ObjectConversion<Boolean> {
    private String defaultForTrue;
    private String defaultForFalse;
    private final Set<String> falseValues = new LinkedHashSet<String>();
    private final Set<String> trueValues = new LinkedHashSet<String>();

    public BooleanConversion(String[] valuesForTrue, String[] valuesForFalse) {
        this(null, null, valuesForTrue, valuesForFalse);
    }

    public BooleanConversion(Boolean valueIfStringIsNull, String valueIfObjectIsNull, String[] valuesForTrue, String[] valuesForFalse) {
        super(valueIfStringIsNull, valueIfObjectIsNull);
        ArgumentUtils.notEmpty("Values for true", valuesForTrue);
        ArgumentUtils.notEmpty("Values for false", valuesForFalse);
        Collections.addAll(this.falseValues, valuesForFalse);
        Collections.addAll(this.trueValues, valuesForTrue);
        BooleanConversion.normalize(this.falseValues);
        BooleanConversion.normalize(this.trueValues);
        for (String falseValue : this.falseValues) {
            if (!this.trueValues.contains(falseValue)) continue;
            throw new DataProcessingException("Ambiguous string representation for both false and true values: '" + falseValue + '\'');
        }
        this.defaultForTrue = valuesForTrue[0];
        this.defaultForFalse = valuesForFalse[0];
    }

    @Override
    public String revert(Boolean input) {
        if (input != null) {
            if (Boolean.FALSE.equals(input)) {
                return this.defaultForFalse;
            }
            if (Boolean.TRUE.equals(input)) {
                return this.defaultForTrue;
            }
        }
        return this.getValueIfObjectIsNull();
    }

    @Override
    protected Boolean fromString(String input) {
        if (input != null) {
            return BooleanConversion.getBoolean(input, this.trueValues, this.falseValues);
        }
        return (Boolean)super.getValueIfStringIsNull();
    }

    public static Boolean getBoolean(String booleanString, String[] trueValues, String[] falseValues) {
        String[] stringArray;
        String[] stringArray2;
        if (trueValues == null || trueValues.length == 0) {
            String[] stringArray3 = new String[1];
            stringArray2 = stringArray3;
            stringArray3[0] = "true";
        } else {
            stringArray2 = trueValues = trueValues;
        }
        if (falseValues == null || falseValues.length == 0) {
            String[] stringArray4 = new String[1];
            stringArray = stringArray4;
            stringArray4[0] = "false";
        } else {
            stringArray = falseValues;
        }
        falseValues = stringArray;
        BooleanConversion tmp = new BooleanConversion(trueValues, falseValues);
        return BooleanConversion.getBoolean(booleanString, tmp.trueValues, tmp.falseValues);
    }

    private static Boolean getBoolean(String defaultString, Set<String> trueValues, Set<String> falseValues) {
        String normalized = BooleanConversion.normalize(defaultString);
        if (falseValues.contains(normalized)) {
            return Boolean.FALSE;
        }
        if (trueValues.contains(normalized)) {
            return Boolean.TRUE;
        }
        DataProcessingException exception = new DataProcessingException("Unable to convert '{value}' to Boolean. Allowed Strings are: " + trueValues + " for true; and " + falseValues + " for false.");
        exception.setValue(defaultString);
        throw exception;
    }

    private static String normalize(String string) {
        if (string == null) {
            return null;
        }
        return string.trim().toLowerCase();
    }

    private static void normalize(Collection<String> strings) {
        LinkedHashSet<String> normalized = new LinkedHashSet<String>(strings.size());
        for (String string : strings) {
            if (string == null) {
                normalized.add(null);
                continue;
            }
            normalized.add(string.trim().toLowerCase());
        }
        strings.clear();
        strings.addAll(normalized);
    }
}

