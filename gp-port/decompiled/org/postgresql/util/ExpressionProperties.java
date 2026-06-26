/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.util;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.PolyNull;
import org.checkerframework.checker.regex.qual.Regex;
import org.postgresql.util.internal.Nullness;

public class ExpressionProperties
extends Properties {
    private static final @Regex(value=1) Pattern EXPRESSION = Pattern.compile("\\$\\{([^}]+)\\}");
    private final Properties[] defaults;

    public ExpressionProperties(Properties ... defaults) {
        this.defaults = defaults;
    }

    @Override
    public @Nullable String getProperty(String key) {
        String value = this.getRawPropertyValue(key);
        return this.replaceProperties(value);
    }

    @Override
    public @PolyNull String getProperty(String key, @PolyNull String defaultValue) {
        String value = this.getRawPropertyValue(key);
        if (value == null) {
            value = defaultValue;
        }
        return this.replaceProperties(value);
    }

    public @Nullable String getRawPropertyValue(String key) {
        String value = super.getProperty(key);
        if (value != null) {
            return value;
        }
        for (Properties properties : this.defaults) {
            value = properties.getProperty(key);
            if (value == null) continue;
            return value;
        }
        return null;
    }

    private @PolyNull String replaceProperties(@PolyNull String value) {
        if (value == null) {
            return null;
        }
        Matcher matcher = EXPRESSION.matcher(value);
        StringBuffer sb = null;
        while (matcher.find()) {
            String propValue;
            if (sb == null) {
                sb = new StringBuffer();
            }
            if ((propValue = this.getProperty(Nullness.castNonNull(matcher.group(1)))) == null) {
                propValue = matcher.group();
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(propValue));
        }
        if (sb == null) {
            return value;
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}

