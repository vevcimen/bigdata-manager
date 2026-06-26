/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.conversions;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import shadeio.univocity.parsers.common.DataProcessingException;
import shadeio.univocity.parsers.conversions.EnumSelector;
import shadeio.univocity.parsers.conversions.ObjectConversion;

public class EnumConversion<T extends Enum<T>>
extends ObjectConversion<T> {
    private final Class<T> enumType;
    private final Field customEnumField;
    private final Method customEnumMethod;
    private final EnumSelector[] selectors;
    private final Map<String, T>[] conversions;

    public EnumConversion(Class<T> enumType) {
        this(enumType, EnumSelector.NAME, EnumSelector.ORDINAL, EnumSelector.STRING);
    }

    public EnumConversion(Class<T> enumType, EnumSelector ... selectors) {
        this((Class<Object>)enumType, (null), (String)null, (String)null, selectors);
    }

    public EnumConversion(Class<T> enumType, String customEnumElement, EnumSelector ... selectors) {
        this(enumType, null, null, customEnumElement, new EnumSelector[0]);
    }

    public EnumConversion(Class<T> enumType, T valueIfStringIsNull, String valueIfEnumIsNull, String customEnumElement, EnumSelector ... selectors) {
        super(valueIfStringIsNull, valueIfEnumIsNull);
        this.enumType = enumType;
        if (customEnumElement != null && (customEnumElement = customEnumElement.trim()).isEmpty()) {
            customEnumElement = null;
        }
        LinkedHashSet<EnumSelector> selectorSet = new LinkedHashSet<EnumSelector>();
        Collections.addAll(selectorSet, selectors);
        if ((selectorSet.contains((Object)EnumSelector.CUSTOM_FIELD) || selectorSet.contains((Object)EnumSelector.CUSTOM_METHOD)) && customEnumElement == null) {
            throw new IllegalArgumentException("Cannot create custom enum conversion without a field name to use");
        }
        Field field = null;
        Method method = null;
        if (customEnumElement != null) {
            IllegalStateException fieldError = null;
            IllegalStateException methodError = null;
            try {
                field = enumType.getDeclaredField(customEnumElement);
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
            }
            catch (Throwable e) {
                fieldError = new IllegalStateException("Unable to access custom field '" + customEnumElement + "' in enumeration type " + enumType.getName(), e);
            }
            if (field == null) {
                try {
                    block23: {
                        try {
                            method = enumType.getDeclaredMethod(customEnumElement, new Class[0]);
                        }
                        catch (NoSuchMethodException e) {
                            method = enumType.getDeclaredMethod(customEnumElement, String.class);
                            if (!Modifier.isStatic(method.getModifiers())) {
                                throw new IllegalArgumentException("Custom method '" + customEnumElement + "' in enumeration type " + enumType.getName() + " must be static");
                            }
                            if (method.getReturnType() == enumType) break block23;
                            throw new IllegalArgumentException("Custom method '" + customEnumElement + "' in enumeration type " + enumType.getName() + " must return " + enumType.getName());
                        }
                    }
                    if (!method.isAccessible()) {
                        method.setAccessible(true);
                    }
                }
                catch (Throwable e) {
                    methodError = new IllegalStateException("Unable to access custom method '" + customEnumElement + "' in enumeration type " + enumType.getName(), e);
                }
                if (method != null) {
                    if (method.getReturnType() == Void.TYPE) {
                        throw new IllegalArgumentException("Custom method '" + customEnumElement + "' in enumeration type " + enumType.getName() + " must return a value");
                    }
                    if (!selectorSet.contains((Object)EnumSelector.CUSTOM_METHOD)) {
                        selectorSet.add(EnumSelector.CUSTOM_METHOD);
                    }
                }
            } else if (!selectorSet.contains((Object)EnumSelector.CUSTOM_FIELD)) {
                selectorSet.add(EnumSelector.CUSTOM_FIELD);
            }
            if (selectorSet.contains((Object)EnumSelector.CUSTOM_FIELD) && fieldError != null) {
                throw fieldError;
            }
            if (selectorSet.contains((Object)EnumSelector.CUSTOM_METHOD) && methodError != null) {
                throw methodError;
            }
            if (field == null && method == null) {
                throw new IllegalStateException("No method/field named '" + customEnumElement + "' found in enumeration type " + enumType.getName());
            }
        }
        if (selectorSet.contains((Object)EnumSelector.CUSTOM_FIELD) && selectorSet.contains((Object)EnumSelector.CUSTOM_METHOD)) {
            throw new IllegalArgumentException("Cannot create custom enum conversion using both method and field values");
        }
        if (selectorSet.isEmpty()) {
            throw new IllegalArgumentException("Selection of enum conversion types cannot be empty.");
        }
        this.customEnumField = field;
        this.customEnumMethod = method;
        this.selectors = selectorSet.toArray(new EnumSelector[selectorSet.size()]);
        this.conversions = new Map[selectorSet.size()];
        this.initializeMappings(selectorSet);
    }

    private void initializeMappings(Set<EnumSelector> conversionTypes) {
        Enum[] constants = (Enum[])this.enumType.getEnumConstants();
        int i = 0;
        for (EnumSelector conversionType : conversionTypes) {
            HashMap<String, T> map = new HashMap<String, T>(constants.length);
            this.conversions[i++] = map;
            for (Enum constant : constants) {
                String key = this.getKey(constant, conversionType);
                if (key == null) continue;
                if (map.containsKey(key)) {
                    throw new IllegalArgumentException("Enumeration element type " + (Object)((Object)conversionType) + " does not uniquely identify elements of " + this.enumType.getName() + ". Got duplicate value '" + key + "' from constants '" + constant + "' and '" + map.get(key) + "'.");
                }
                map.put(key, constant);
            }
        }
    }

    private String getKey(T constant, EnumSelector conversionType) {
        switch (conversionType) {
            case NAME: {
                return ((Enum)constant).name();
            }
            case ORDINAL: {
                return String.valueOf(((Enum)constant).ordinal());
            }
            case STRING: {
                return ((Enum)constant).toString();
            }
            case CUSTOM_FIELD: {
                try {
                    return String.valueOf(this.customEnumField.get(constant));
                }
                catch (Throwable e) {
                    throw new IllegalStateException("Error reading custom field '" + this.customEnumField.getName() + "' from enumeration constant '" + constant + "' of type " + this.enumType.getName(), e);
                }
            }
            case CUSTOM_METHOD: {
                try {
                    if (this.customEnumMethod.getParameterTypes().length == 0) {
                        return String.valueOf(this.customEnumMethod.invoke(constant, new Object[0]));
                    }
                    return null;
                }
                catch (Throwable e) {
                    throw new IllegalStateException("Error reading custom method '" + this.customEnumMethod.getName() + "' from enumeration constant '" + constant + "' of type " + this.enumType.getName(), e);
                }
            }
        }
        throw new IllegalStateException("Unsupported enumeration selector type " + (Object)((Object)conversionType));
    }

    @Override
    public String revert(T input) {
        if (input == null) {
            return super.revert((Object)null);
        }
        return this.getKey(input, this.selectors[0]);
    }

    @Override
    protected T fromString(String input) {
        for (Map<String, T> conversion : this.conversions) {
            Enum value = (Enum)conversion.get(input);
            if (value == null) continue;
            return (T)value;
        }
        DataProcessingException exception = null;
        if (this.customEnumMethod != null && this.customEnumMethod.getParameterTypes().length == 1) {
            try {
                Enum out = (Enum)this.customEnumMethod.invoke(null, input);
                return (T)out;
            }
            catch (Exception e) {
                exception = new DataProcessingException("Cannot convert '{value}' to enumeration of type " + this.enumType.getName() + " using method " + this.customEnumMethod.getName(), (Throwable)e);
            }
        }
        if (exception == null) {
            exception = new DataProcessingException("Cannot convert '{value}' to enumeration of type " + this.enumType.getName());
        }
        exception.setValue(input);
        exception.markAsNonFatal();
        throw exception;
    }
}

