/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.conversions;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import shadeio.univocity.parsers.common.ArgumentUtils;
import shadeio.univocity.parsers.conversions.Conversion;

public class NullStringConversion
implements Conversion<Object, Object> {
    private final Set<String> nullStrings = new HashSet<String>();
    private final String defaultNullString;

    public NullStringConversion(String ... nullRepresentations) {
        ArgumentUtils.noNulls("Null representation strings", nullRepresentations);
        Collections.addAll(this.nullStrings, nullRepresentations);
        this.defaultNullString = nullRepresentations[0];
    }

    @Override
    public Object execute(Object input) {
        if (input == null) {
            return null;
        }
        if (this.nullStrings.contains(String.valueOf(input))) {
            return null;
        }
        return input;
    }

    @Override
    public Object revert(Object input) {
        if (input == null) {
            return this.defaultNullString;
        }
        return input;
    }
}

