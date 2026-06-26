/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.conversions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import shadeio.univocity.parsers.annotations.helpers.AnnotationHelper;
import shadeio.univocity.parsers.common.ArgumentUtils;
import shadeio.univocity.parsers.common.DataValidationException;
import shadeio.univocity.parsers.conversions.Conversion;
import shadeio.univocity.parsers.conversions.Validator;

public class ValidatedConversion
implements Conversion<Object, Object> {
    private final String regexToMatch;
    private final boolean nullable;
    private final boolean allowBlanks;
    private final Set<String> oneOf;
    private final Set<String> noneOf;
    private final Matcher matcher;
    private final Validator[] validators;

    public ValidatedConversion() {
        this(false, false, null, null, null);
    }

    public ValidatedConversion(String regexToMatch) {
        this(false, false, null, null, regexToMatch);
    }

    public ValidatedConversion(boolean nullable, boolean allowBlanks) {
        this(nullable, allowBlanks, null, null, null);
    }

    public ValidatedConversion(boolean nullable, boolean allowBlanks, String[] oneOf, String[] noneOf, String regexToMatch) {
        this(nullable, allowBlanks, oneOf, noneOf, regexToMatch, null);
    }

    public ValidatedConversion(boolean nullable, boolean allowBlanks, String[] oneOf, String[] noneOf, String regexToMatch, Class[] validators) {
        this.regexToMatch = regexToMatch;
        this.matcher = regexToMatch == null || regexToMatch.isEmpty() ? null : Pattern.compile(regexToMatch).matcher("");
        this.nullable = nullable;
        this.allowBlanks = allowBlanks;
        this.oneOf = oneOf == null || oneOf.length == 0 ? null : new HashSet<String>(Arrays.asList(oneOf));
        this.noneOf = noneOf == null || noneOf.length == 0 ? null : new HashSet<String>(Arrays.asList(noneOf));
        this.validators = validators == null || validators.length == 0 ? new Validator[]{} : this.instantiateValidators(validators);
    }

    private Validator[] instantiateValidators(Class[] validators) {
        Validator[] out = new Validator[validators.length];
        for (int i = 0; i < validators.length; ++i) {
            out[i] = (Validator)AnnotationHelper.newInstance(Validator.class, validators[i], ArgumentUtils.EMPTY_STRING_ARRAY);
        }
        return out;
    }

    @Override
    public Object execute(Object input) {
        this.validate(input);
        return input;
    }

    @Override
    public Object revert(Object input) {
        this.validate(input);
        return input;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    protected void validate(Object value) {
        DataValidationException e = null;
        String str = null;
        if (value == null) {
            if (this.nullable) {
                if (this.noneOf == null || !this.noneOf.contains(null)) return;
                e = new DataValidationException("Value '{value}' is not allowed.");
            } else {
                if (this.oneOf != null && this.oneOf.contains(null)) {
                    return;
                }
                e = new DataValidationException("Null values not allowed.");
            }
        } else {
            str = String.valueOf(value);
            if (str.trim().isEmpty()) {
                if (this.allowBlanks) {
                    if (this.noneOf == null || !this.noneOf.contains(str)) return;
                    e = new DataValidationException("Value '{value}' is not allowed.");
                } else {
                    if (this.oneOf != null && this.oneOf.contains(str)) {
                        return;
                    }
                    e = new DataValidationException("Blanks are not allowed. '{value}' is blank.");
                }
            }
            if (this.matcher != null && e == null) {
                boolean match;
                Matcher matcher = this.matcher;
                synchronized (matcher) {
                    match = this.matcher.reset(str).matches();
                }
                if (!match) {
                    e = new DataValidationException("Value '{value}' does not match expected pattern: '" + this.regexToMatch + "'");
                }
            }
        }
        if (this.oneOf != null && !this.oneOf.contains(str)) {
            e = new DataValidationException("Value '{value}' is not allowed. Expecting one of: " + this.oneOf);
        }
        if (e == null && this.noneOf != null && this.noneOf.contains(str)) {
            e = new DataValidationException("Value '{value}' is not allowed.");
        }
        for (int i = 0; e == null && i < this.validators.length; ++i) {
            String error = this.validators[i].validate(value);
            if (error == null || error.trim().isEmpty()) continue;
            e = new DataValidationException("Value '{value}' didn't pass validation: " + error);
        }
        if (e == null) return;
        e.setValue(value);
        throw e;
    }
}

