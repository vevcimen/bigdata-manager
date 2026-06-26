/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.annotations.helpers;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormatSymbols;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TimeZone;
import shadeio.univocity.parsers.annotations.BooleanString;
import shadeio.univocity.parsers.annotations.Convert;
import shadeio.univocity.parsers.annotations.Copy;
import shadeio.univocity.parsers.annotations.EnumOptions;
import shadeio.univocity.parsers.annotations.Format;
import shadeio.univocity.parsers.annotations.HeaderTransformer;
import shadeio.univocity.parsers.annotations.Headers;
import shadeio.univocity.parsers.annotations.LowerCase;
import shadeio.univocity.parsers.annotations.Nested;
import shadeio.univocity.parsers.annotations.NullString;
import shadeio.univocity.parsers.annotations.Parsed;
import shadeio.univocity.parsers.annotations.Replace;
import shadeio.univocity.parsers.annotations.Trim;
import shadeio.univocity.parsers.annotations.UpperCase;
import shadeio.univocity.parsers.annotations.Validate;
import shadeio.univocity.parsers.annotations.helpers.AnnotationRegistry;
import shadeio.univocity.parsers.annotations.helpers.MethodFilter;
import shadeio.univocity.parsers.annotations.helpers.TransformedHeader;
import shadeio.univocity.parsers.common.ArgumentUtils;
import shadeio.univocity.parsers.common.DataProcessingException;
import shadeio.univocity.parsers.common.beans.BeanHelper;
import shadeio.univocity.parsers.common.beans.PropertyWrapper;
import shadeio.univocity.parsers.common.input.DefaultCharAppender;
import shadeio.univocity.parsers.conversions.BooleanConversion;
import shadeio.univocity.parsers.conversions.Conversion;
import shadeio.univocity.parsers.conversions.Conversions;
import shadeio.univocity.parsers.conversions.EnumConversion;
import shadeio.univocity.parsers.conversions.EnumSelector;
import shadeio.univocity.parsers.conversions.FormattedConversion;
import shadeio.univocity.parsers.conversions.NumericConversion;
import shadeio.univocity.parsers.conversions.ObjectConversion;
import shadeio.univocity.parsers.conversions.ToStringConversion;
import shadeio.univocity.parsers.conversions.ValidatedConversion;

public class AnnotationHelper {
    private static AnnotatedElement lastProcessedElement;
    private static Class<? extends Annotation> lastProcessedAnnotationType;
    private static Annotation lastAnnotationFound;
    private static final Set<Class> javaLangAnnotationTypes;
    private static final Set<Class> customAnnotationTypes;

    private AnnotationHelper() {
    }

    private static String getNullValue(String defaultValue) {
        if ("null".equals(defaultValue)) {
            return null;
        }
        if ("'null'".equals(defaultValue)) {
            return "null";
        }
        return defaultValue;
    }

    public static String getNullWriteValue(AnnotatedElement target, Parsed parsed) {
        if (parsed == null) {
            return null;
        }
        return AnnotationHelper.getNullValue(AnnotationRegistry.getValue(target, parsed, "defaultNullWrite", parsed.defaultNullWrite()));
    }

    public static String getNullReadValue(AnnotatedElement target, Parsed parsed) {
        if (parsed == null) {
            return null;
        }
        return AnnotationHelper.getNullValue(AnnotationRegistry.getValue(target, parsed, "defaultNullRead", parsed.defaultNullRead()));
    }

    public static Conversion getConversion(Class classType, Annotation annotation) {
        return AnnotationHelper.getConversion(classType, null, annotation, null, null);
    }

    public static EnumConversion createDefaultEnumConversion(Class fieldType, String nullRead, String nullWrite) {
        Object nullReadValue = nullRead == null ? null : (Object)Enum.valueOf(fieldType, nullRead);
        return new EnumConversion<Object>((Class<Object>)fieldType, nullReadValue, nullWrite, null, EnumSelector.NAME, EnumSelector.ORDINAL, EnumSelector.STRING);
    }

    public static Conversion getConversion(Class fieldType, AnnotatedElement target, Annotation annotation, String nullRead, String nullWrite) {
        try {
            Class<? extends Annotation> annType = annotation.annotationType();
            if (annType == NullString.class) {
                NullString nullString = (NullString)annotation;
                String[] nulls = AnnotationRegistry.getValue(target, nullString, "nulls", nullString.nulls());
                return Conversions.toNull(nulls);
            }
            if (annType == Validate.class) {
                Validate validate = (Validate)annotation;
                boolean nullable = AnnotationRegistry.getValue(target, validate, "nullable", validate.nullable());
                boolean allowBlanks = AnnotationRegistry.getValue(target, validate, "allowBlanks", validate.allowBlanks());
                String[] oneOf = AnnotationRegistry.getValue(target, validate, "oneOf", validate.oneOf());
                String[] noneOf = AnnotationRegistry.getValue(target, validate, "noneOf", validate.noneOf());
                String matches = AnnotationRegistry.getValue(target, validate, "matches", validate.matches());
                Class[] validators = AnnotationRegistry.getValue(target, validate, "validators", validate.validators());
                return new ValidatedConversion(nullable, allowBlanks, oneOf, noneOf, matches, validators);
            }
            if (annType == EnumOptions.class) {
                if (!fieldType.isEnum()) {
                    if (target == null) {
                        throw new IllegalStateException("Invalid " + EnumOptions.class.getName() + " instance for converting class " + fieldType.getName() + ". Not an enum type.");
                    }
                    throw new IllegalStateException("Invalid " + EnumOptions.class.getName() + " annotation on " + AnnotationHelper.describeElement(target) + ". Attribute must be an enum type.");
                }
                EnumOptions enumOptions = (EnumOptions)annotation;
                String customElement = AnnotationRegistry.getValue(target, enumOptions, "customElement", enumOptions.customElement());
                String element = customElement.trim();
                if (element.isEmpty()) {
                    element = null;
                }
                Object nullReadValue = nullRead == null ? null : (Object)Enum.valueOf(fieldType, nullRead);
                EnumSelector[] selectors = AnnotationRegistry.getValue(target, enumOptions, "selectors", enumOptions.selectors());
                return new EnumConversion<Object>((Class<Object>)fieldType, nullReadValue, nullWrite, element, selectors);
            }
            if (annType == Trim.class) {
                Trim trim = (Trim)annotation;
                int length = AnnotationRegistry.getValue(target, trim, "length", trim.length());
                if (length == -1) {
                    return Conversions.trim();
                }
                return Conversions.trim(length);
            }
            if (annType == LowerCase.class) {
                return Conversions.toLowerCase();
            }
            if (annType == UpperCase.class) {
                return Conversions.toUpperCase();
            }
            if (annType == Replace.class) {
                Replace replace = (Replace)annotation;
                String expression = AnnotationRegistry.getValue(target, replace, "expression", replace.expression());
                String replacement = AnnotationRegistry.getValue(target, replace, "replacement", replace.replacement());
                return Conversions.replace(expression, replacement);
            }
            if (annType == BooleanString.class) {
                Boolean valueForNull;
                if (fieldType != Boolean.TYPE && fieldType != Boolean.class) {
                    if (target == null) {
                        throw new DataProcessingException("Invalid  usage of " + BooleanString.class.getName() + ". Got type " + fieldType.getName() + " instead of boolean.");
                    }
                    throw new DataProcessingException("Invalid annotation: " + AnnotationHelper.describeElement(target) + " has type " + fieldType.getName() + " instead of boolean.");
                }
                BooleanString boolString = (BooleanString)annotation;
                String[] falseStrings = AnnotationRegistry.getValue(target, boolString, "falseStrings", boolString.falseStrings());
                String[] trueStrings = AnnotationRegistry.getValue(target, boolString, "trueStrings", boolString.trueStrings());
                Boolean bl = valueForNull = nullRead == null ? null : BooleanConversion.getBoolean(nullRead, trueStrings, falseStrings);
                if (valueForNull == null && fieldType == Boolean.TYPE) {
                    valueForNull = Boolean.FALSE;
                }
                return Conversions.toBoolean(valueForNull, nullWrite, trueStrings, falseStrings);
            }
            if (annType == Format.class) {
                Format format = (Format)annotation;
                String[] formats = AnnotationRegistry.getValue(target, format, "formats", format.formats());
                Object[] options = AnnotationRegistry.getValue(target, format, "options", format.options());
                Locale locale = AnnotationHelper.extractLocale((String[])options);
                TimeZone timezone = AnnotationHelper.extractTimeZone((String[])options);
                ObjectConversion conversion = null;
                if (fieldType == BigDecimal.class) {
                    BigDecimal defaultForNull = nullRead == null ? null : new BigDecimal(nullRead);
                    conversion = Conversions.formatToBigDecimal(defaultForNull, nullWrite, formats);
                } else if (Number.class.isAssignableFrom(fieldType) || fieldType.isPrimitive() && fieldType != Boolean.TYPE && fieldType != Character.TYPE) {
                    conversion = Conversions.formatToNumber(formats);
                    ((NumericConversion)conversion).setNumberType(fieldType);
                } else {
                    Date dateIfNull = null;
                    if (nullRead != null) {
                        if ("now".equalsIgnoreCase(nullRead)) {
                            dateIfNull = new Date();
                        } else {
                            if (formats.length == 0) {
                                throw new DataProcessingException("No format defined");
                            }
                            SimpleDateFormat sdf = new SimpleDateFormat(formats[0], locale);
                            sdf.setTimeZone(timezone);
                            dateIfNull = sdf.parse(nullRead);
                        }
                    }
                    if (Date.class == fieldType) {
                        conversion = Conversions.toDate(timezone, locale, dateIfNull, nullWrite, formats);
                    } else if (Calendar.class == fieldType) {
                        Calendar calendarIfNull = null;
                        if (dateIfNull != null) {
                            calendarIfNull = Calendar.getInstance();
                            calendarIfNull.setTime(dateIfNull);
                            calendarIfNull.setTimeZone(timezone);
                        }
                        conversion = Conversions.toCalendar(timezone, locale, calendarIfNull, nullWrite, formats);
                    }
                }
                if (conversion != null) {
                    if (options.length > 0) {
                        if (conversion instanceof FormattedConversion) {
                            T[] formatters;
                            for (Object formatter : formatters = ((FormattedConversion)((Object)conversion)).getFormatterObjects()) {
                                AnnotationHelper.applyFormatSettings(formatter, (String[])options);
                            }
                        } else {
                            throw new DataProcessingException("Options '" + Arrays.toString(options) + "' not supported by conversion of type '" + conversion.getClass() + "'. It must implement " + FormattedConversion.class);
                        }
                    }
                    return conversion;
                }
            } else if (annType == Convert.class) {
                Convert convert = (Convert)annotation;
                String[] args = AnnotationRegistry.getValue(target, convert, "args", convert.args());
                Class<? extends Conversion> conversionClass = AnnotationRegistry.getValue(target, convert, "conversionClass", convert.conversionClass());
                return AnnotationHelper.newInstance(Conversion.class, conversionClass, args);
            }
            if (fieldType == String.class && (nullRead != null || nullWrite != null)) {
                return new ToStringConversion(nullRead, nullWrite);
            }
            return null;
        }
        catch (DataProcessingException ex) {
            throw ex;
        }
        catch (Throwable ex) {
            if (target == null) {
                throw new DataProcessingException("Unexpected error identifying conversions to apply over type " + fieldType, ex);
            }
            throw new DataProcessingException("Unexpected error identifying conversions to apply over " + AnnotationHelper.describeElement(target), ex);
        }
    }

    private static String extractOption(String[] options, String key) {
        for (int i = 0; i < options.length; ++i) {
            if (options[i] == null || !options[i].trim().toLowerCase().startsWith(key)) continue;
            String out = options[i].split("=")[1].trim();
            options[i] = null;
            return out;
        }
        return null;
    }

    private static TimeZone extractTimeZone(String[] options) {
        String code = AnnotationHelper.extractOption(options, "timezone=");
        if (code != null) {
            return TimeZone.getTimeZone(code);
        }
        return TimeZone.getDefault();
    }

    private static Locale extractLocale(String[] options) {
        String locale = AnnotationHelper.extractOption(options, "locale=");
        if (locale != null) {
            char ch;
            int j;
            DefaultCharAppender appender = new DefaultCharAppender(100, "", 0);
            for (j = 0; j < locale.length() && Character.isLetterOrDigit(ch = locale.charAt(j)); ++j) {
                appender.append(ch);
            }
            String languageCode = appender.getAndReset();
            ++j;
            while (j < locale.length() && Character.isLetterOrDigit(ch = locale.charAt(j))) {
                ++j;
                appender.append(ch);
            }
            String countryCode = appender.getAndReset();
            ++j;
            while (j < locale.length() && Character.isLetterOrDigit(ch = locale.charAt(j))) {
                ++j;
                appender.append(ch);
            }
            String variant = appender.getAndReset();
            return new Locale(languageCode, countryCode, variant);
        }
        return Locale.getDefault();
    }

    public static <T> T newInstance(Class parent, Class<T> type, String[] args) {
        if (!parent.isAssignableFrom(type)) {
            throw new DataProcessingException("Not a valid " + parent.getSimpleName() + " class: '" + type.getSimpleName() + "' (" + type.getName() + ')');
        }
        try {
            Constructor<T> constructor = type.getConstructor(String[].class);
            return constructor.newInstance(new Object[]{args});
        }
        catch (NoSuchMethodException e) {
            if (args.length == 0) {
                try {
                    return type.newInstance();
                }
                catch (Exception ex) {
                    throw new DataProcessingException("Unexpected error instantiating custom " + parent.getSimpleName() + " class '" + type.getSimpleName() + "' (" + type.getName() + ')', (Throwable)e);
                }
            }
            throw new DataProcessingException("Could not find a public constructor with a String[] parameter in custom " + parent.getSimpleName() + " class '" + type.getSimpleName() + "' (" + type.getName() + ')', (Throwable)e);
        }
        catch (Exception e) {
            throw new DataProcessingException("Unexpected error instantiating custom " + parent.getSimpleName() + " class '" + type.getSimpleName() + "' (" + type.getName() + ')', (Throwable)e);
        }
    }

    public static Conversion getDefaultConversion(Class fieldType, AnnotatedElement target, Parsed parsed) {
        String nullRead = AnnotationHelper.getNullReadValue(target, parsed);
        Comparable<Boolean> valueIfStringIsNull = null;
        ObjectConversion conversion = null;
        if (fieldType == Boolean.class || fieldType == Boolean.TYPE) {
            conversion = Conversions.toBoolean();
            valueIfStringIsNull = nullRead == null ? null : Boolean.valueOf(nullRead);
        } else if (fieldType == Character.class || fieldType == Character.TYPE) {
            conversion = Conversions.toChar();
            if (nullRead != null && nullRead.length() > 1) {
                throw new DataProcessingException("Invalid default value for character '" + nullRead + "'. It should contain one character only.");
            }
            valueIfStringIsNull = nullRead == null ? null : Character.valueOf(nullRead.charAt(0));
        } else if (fieldType == Byte.class || fieldType == Byte.TYPE) {
            conversion = Conversions.toByte();
            valueIfStringIsNull = nullRead == null ? null : Byte.valueOf(nullRead);
        } else if (fieldType == Short.class || fieldType == Short.TYPE) {
            conversion = Conversions.toShort();
            valueIfStringIsNull = nullRead == null ? null : Short.valueOf(nullRead);
        } else if (fieldType == Integer.class || fieldType == Integer.TYPE) {
            conversion = Conversions.toInteger();
            valueIfStringIsNull = nullRead == null ? null : Integer.valueOf(nullRead);
        } else if (fieldType == Long.class || fieldType == Long.TYPE) {
            conversion = Conversions.toLong();
            valueIfStringIsNull = nullRead == null ? null : Long.valueOf(nullRead);
        } else if (fieldType == Float.class || fieldType == Float.TYPE) {
            conversion = Conversions.toFloat();
            valueIfStringIsNull = nullRead == null ? null : Float.valueOf(nullRead);
        } else if (fieldType == Double.class || fieldType == Double.TYPE) {
            conversion = Conversions.toDouble();
            valueIfStringIsNull = nullRead == null ? null : Double.valueOf(nullRead);
        } else if (fieldType == BigInteger.class) {
            conversion = Conversions.toBigInteger();
            valueIfStringIsNull = nullRead == null ? null : new BigInteger(nullRead);
        } else if (fieldType == BigDecimal.class) {
            conversion = Conversions.toBigDecimal();
            valueIfStringIsNull = nullRead == null ? null : new BigDecimal(nullRead);
        } else if (Enum.class.isAssignableFrom(fieldType)) {
            conversion = Conversions.toEnum(fieldType);
        }
        if (conversion != null) {
            conversion.setValueIfStringIsNull(valueIfStringIsNull);
            conversion.setValueIfObjectIsNull(AnnotationHelper.getNullWriteValue(target, parsed));
        }
        return conversion;
    }

    public static Conversion getDefaultConversion(AnnotatedElement target) {
        Parsed parsed = AnnotationHelper.findAnnotation(target, Parsed.class);
        return AnnotationHelper.getDefaultConversion(AnnotationHelper.getType(target), target, parsed);
    }

    public static void applyFormatSettings(Object formatter, String[] propertiesAndValues) {
        if (propertiesAndValues.length == 0) {
            return;
        }
        HashMap<String, String> values = new HashMap<String, String>();
        for (String setting : propertiesAndValues) {
            if (setting == null) continue;
            String[] pair = setting.split("=");
            if (pair.length != 2) {
                throw new DataProcessingException("Illegal format setting '" + setting + "' among: " + Arrays.toString(propertiesAndValues));
            }
            values.put(pair[0], pair[1]);
        }
        try {
            for (PropertyWrapper property : BeanHelper.getPropertyDescriptors(formatter.getClass())) {
                String name = property.getName();
                String value = (String)values.remove(name);
                if (value != null) {
                    AnnotationHelper.invokeSetter(formatter, property, value);
                }
                if (!"decimalFormatSymbols".equals(property.getName())) continue;
                DecimalFormatSymbols modifiedDecimalSymbols = new DecimalFormatSymbols();
                boolean modified = false;
                try {
                    for (PropertyWrapper prop : BeanHelper.getPropertyDescriptors(modifiedDecimalSymbols.getClass())) {
                        value = (String)values.remove(prop.getName());
                        if (value == null) continue;
                        AnnotationHelper.invokeSetter(modifiedDecimalSymbols, prop, value);
                        modified = true;
                    }
                    if (!modified) continue;
                    Method writeMethod = property.getWriteMethod();
                    if (writeMethod != null) {
                        writeMethod.invoke(formatter, modifiedDecimalSymbols);
                        continue;
                    }
                    throw new IllegalStateException("No write method defined for property " + property.getName());
                }
                catch (Throwable ex) {
                    throw new DataProcessingException("Error trying to configure decimal symbols of formatter '" + formatter.getClass() + '.', ex);
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (!values.isEmpty()) {
            throw new DataProcessingException("Cannot find properties in formatter of type '" + formatter.getClass() + "': " + values);
        }
    }

    private static void invokeSetter(Object formatter, PropertyWrapper property, String value) {
        Method writeMethod = property.getWriteMethod();
        if (writeMethod == null) {
            DataProcessingException exception = new DataProcessingException("Cannot set property '" + property.getName() + "' of formatter '" + formatter.getClass() + "' to '{value}'. No setter defined");
            exception.setValue(value);
            throw exception;
        }
        Class<?> parameterType = writeMethod.getParameterTypes()[0];
        Object parameterValue = null;
        if (parameterType == String.class) {
            parameterValue = value;
        } else if (parameterType == Integer.class || parameterType == Integer.TYPE) {
            parameterValue = Integer.parseInt(value);
        } else if (parameterType == Character.class || parameterType == Character.TYPE) {
            parameterValue = Character.valueOf(value.charAt(0));
        } else if (parameterType == Currency.class) {
            parameterValue = Currency.getInstance(value);
        } else if (parameterType == Boolean.class || parameterType == Boolean.TYPE) {
            parameterValue = Boolean.valueOf(value);
        } else if (parameterType == TimeZone.class) {
            parameterValue = TimeZone.getTimeZone(value);
        } else if (parameterType == DateFormatSymbols.class) {
            parameterValue = DateFormatSymbols.getInstance(new Locale(value));
        }
        if (parameterValue == null) {
            DataProcessingException exception = new DataProcessingException("Cannot set property '" + property.getName() + "' of formatter '" + formatter.getClass() + ". Cannot convert '{value}' to instance of " + parameterType);
            exception.setValue(value);
            throw exception;
        }
        try {
            writeMethod.invoke(formatter, parameterValue);
        }
        catch (Throwable e) {
            DataProcessingException exception = new DataProcessingException("Error setting property '" + property.getName() + "' of formatter '" + formatter.getClass() + ", with '{parameterValue}' (converted from '{value}')", e);
            exception.setValue("parameterValue", parameterValue);
            exception.setValue(value);
            throw exception;
        }
    }

    private static boolean allFieldsIndexOrNameBased(boolean searchName, Class<?> beanClass, MethodFilter filter) {
        boolean hasAnnotation = false;
        for (TransformedHeader header : AnnotationHelper.getFieldSequence(beanClass, true, null, filter)) {
            Parsed annotation;
            AnnotatedElement element;
            if (header == null || header.getTarget() == null || (element = header.getTarget()) instanceof Method && filter.reject((Method)element) || (annotation = AnnotationHelper.findAnnotation(element, Parsed.class)) == null) continue;
            hasAnnotation = true;
            int index = AnnotationRegistry.getValue(element, annotation, "index", annotation.index());
            if ((index == -1 || !searchName) && (index != -1 || searchName)) continue;
            return false;
        }
        return hasAnnotation;
    }

    public static boolean allFieldsIndexBasedForParsing(Class<?> beanClass) {
        return AnnotationHelper.allFieldsIndexOrNameBased(false, beanClass, MethodFilter.ONLY_SETTERS);
    }

    public static boolean allFieldsNameBasedForParsing(Class<?> beanClass) {
        return AnnotationHelper.allFieldsIndexOrNameBased(true, beanClass, MethodFilter.ONLY_SETTERS);
    }

    public static boolean allFieldsIndexBasedForWriting(Class<?> beanClass) {
        return AnnotationHelper.allFieldsIndexOrNameBased(false, beanClass, MethodFilter.ONLY_GETTERS);
    }

    public static boolean allFieldsNameBasedForWriting(Class<?> beanClass) {
        return AnnotationHelper.allFieldsIndexOrNameBased(true, beanClass, MethodFilter.ONLY_GETTERS);
    }

    public static Integer[] getSelectedIndexes(Class<?> beanClass, MethodFilter filter) {
        ArrayList<Integer> indexes = new ArrayList<Integer>();
        for (TransformedHeader header : AnnotationHelper.getFieldSequence(beanClass, true, null, filter)) {
            int index;
            if (header == null || (index = header.getHeaderIndex()) == -1) continue;
            if (filter == MethodFilter.ONLY_GETTERS && indexes.contains(index)) {
                throw new IllegalArgumentException("Duplicate field index '" + index + "' found in attribute '" + header.getTargetName() + "' of class " + beanClass.getName());
            }
            indexes.add(index);
        }
        return indexes.toArray(new Integer[indexes.size()]);
    }

    public static String[] deriveHeaderNamesFromFields(Class<?> beanClass, MethodFilter filter) {
        List<TransformedHeader> sequence = AnnotationHelper.getFieldSequence(beanClass, true, null, filter);
        ArrayList<String> out = new ArrayList<String>(sequence.size());
        for (TransformedHeader field : sequence) {
            if (field == null) {
                return ArgumentUtils.EMPTY_STRING_ARRAY;
            }
            out.add(field.getHeaderName());
        }
        return out.toArray(new String[out.size()]);
    }

    public static <T extends Annotation> T findAnnotationInClass(Class<?> beanClass, Class<T> annotation) {
        Class<?> parent = beanClass;
        do {
            T out;
            if ((out = parent.getAnnotation(annotation)) != null) {
                return out;
            }
            for (Class<?> iface : parent.getInterfaces()) {
                out = AnnotationHelper.findAnnotationInClass(iface, annotation);
                if (out == null) continue;
                return out;
            }
        } while ((parent = parent.getSuperclass()) != null);
        return null;
    }

    public static Headers findHeadersAnnotation(Class<?> beanClass) {
        return AnnotationHelper.findAnnotationInClass(beanClass, Headers.class);
    }

    public static Class<?> getType(AnnotatedElement element) {
        if (element instanceof Field) {
            return ((Field)element).getType();
        }
        Method method = (Method)element;
        Class<?>[] params = method.getParameterTypes();
        if (params.length == 1) {
            return params[0];
        }
        if (params.length > 1) {
            throw new IllegalArgumentException("Method " + AnnotationHelper.describeElement(element) + " cannot have multiple parameters");
        }
        Class<?> returnType = method.getReturnType();
        if (returnType != Void.TYPE) {
            return returnType;
        }
        throw new IllegalArgumentException("Method " + AnnotationHelper.describeElement(element) + " must return a value if it has no input parameter");
    }

    public static Class<?> getDeclaringClass(AnnotatedElement element) {
        if (element instanceof Field) {
            return ((Field)element).getDeclaringClass();
        }
        return ((Method)element).getDeclaringClass();
    }

    public static String getName(AnnotatedElement element) {
        if (element instanceof Field) {
            return ((Field)element).getName();
        }
        return ((Method)element).getName();
    }

    static String describeElement(AnnotatedElement element) {
        String description = element instanceof Field ? "attribute '" + ((Field)element).getName() + "'" : "method '" + ((Method)element).getName() + "'";
        return description + " of class " + AnnotationHelper.getDeclaringClass(element).getName();
    }

    private static void processAnnotations(AnnotatedElement element, boolean processNested, List<Integer> indexes, List<TransformedHeader> tmp, Map<AnnotatedElement, List<TransformedHeader>> nestedReplacements, HeaderTransformer transformer, MethodFilter filter) {
        Nested nested;
        Parsed annotation = AnnotationHelper.findAnnotation(element, Parsed.class);
        if (annotation != null) {
            TransformedHeader header = new TransformedHeader(element, transformer);
            if (filter == MethodFilter.ONLY_GETTERS && header.getHeaderIndex() >= 0 && indexes.contains(header.getHeaderIndex())) {
                throw new IllegalArgumentException("Duplicate field index '" + header.getHeaderIndex() + "' found in " + AnnotationHelper.describeElement(element));
            }
            tmp.add(header);
            indexes.add(header.getHeaderIndex());
        }
        if (processNested && (nested = AnnotationHelper.findAnnotation(element, Nested.class)) != null) {
            Class<? extends HeaderTransformer> transformerType;
            tmp.add(new TransformedHeader(element, null));
            Class<?> nestedBeanType = AnnotationRegistry.getValue(element, nested, "type", nested.type());
            if (nestedBeanType == Object.class) {
                nestedBeanType = AnnotationHelper.getType(element);
            }
            if ((transformerType = AnnotationRegistry.getValue(element, nested, "headerTransformer", nested.headerTransformer())) != HeaderTransformer.class) {
                String[] args = AnnotationRegistry.getValue(element, nested, "args", nested.args());
                HeaderTransformer innerTransformer = AnnotationHelper.newInstance(HeaderTransformer.class, transformerType, args);
                nestedReplacements.put(element, AnnotationHelper.getFieldSequence(nestedBeanType, true, indexes, innerTransformer, filter));
            } else {
                nestedReplacements.put(element, AnnotationHelper.getFieldSequence(nestedBeanType, true, indexes, transformer, filter));
            }
        }
    }

    public static List<TransformedHeader> getFieldSequence(Class beanClass, boolean processNested, HeaderTransformer transformer, MethodFilter filter) {
        ArrayList<Integer> indexes = new ArrayList<Integer>();
        List<TransformedHeader> tmp = AnnotationHelper.getFieldSequence(beanClass, processNested, indexes, transformer, filter);
        Collections.sort(tmp, new Comparator<TransformedHeader>(){

            @Override
            public int compare(TransformedHeader t1, TransformedHeader t2) {
                int i2;
                int i1 = t1.getHeaderIndex();
                return i1 < (i2 = t2.getHeaderIndex()) ? -1 : (i1 == i2 ? 0 : 1);
            }
        });
        Collections.sort(indexes);
        int col = -1;
        Iterator i$ = indexes.iterator();
        while (i$.hasNext()) {
            int i = (Integer)i$.next();
            if (i < 0 || i == ++col) continue;
            while (i >= tmp.size()) {
                tmp.add(null);
            }
            Collections.swap(tmp, i, col);
        }
        return tmp;
    }

    private static List<TransformedHeader> getFieldSequence(Class beanClass, boolean processNested, List<Integer> indexes, HeaderTransformer transformer, MethodFilter filter) {
        ArrayList<TransformedHeader> tmp = new ArrayList<TransformedHeader>();
        LinkedHashMap<AnnotatedElement, List<TransformedHeader>> nestedReplacements = new LinkedHashMap<AnnotatedElement, List<TransformedHeader>>();
        for (Field field : AnnotationHelper.getAllFields(beanClass).keySet()) {
            AnnotationHelper.processAnnotations(field, processNested, indexes, tmp, nestedReplacements, transformer, filter);
        }
        for (Method method : AnnotationHelper.getAnnotatedMethods(beanClass, filter)) {
            AnnotationHelper.processAnnotations(method, processNested, indexes, tmp, nestedReplacements, transformer, filter);
        }
        if (!nestedReplacements.isEmpty()) {
            int size = tmp.size();
            for (int i = size - 1; i >= 0; --i) {
                TransformedHeader field = (TransformedHeader)tmp.get(i);
                List nestedFields = (List)nestedReplacements.remove(field.getTarget());
                if (nestedFields == null) continue;
                tmp.remove(i);
                tmp.addAll(i, nestedFields);
                if (nestedReplacements.isEmpty()) break;
            }
        }
        return tmp;
    }

    public static Map<Field, PropertyWrapper> getAllFields(Class<?> beanClass) {
        LinkedHashMap<String, PropertyWrapper> properties = new LinkedHashMap<String, PropertyWrapper>();
        try {
            for (PropertyWrapper property : BeanHelper.getPropertyDescriptors(beanClass)) {
                String name = property.getName();
                if (name == null) continue;
                properties.put(name, property);
            }
        }
        catch (Exception e) {
            // empty catch block
        }
        HashSet<String> used = new HashSet<String>();
        Class<?> clazz = beanClass;
        LinkedHashMap<Field, PropertyWrapper> out = new LinkedHashMap<Field, PropertyWrapper>();
        do {
            Field[] declared;
            for (Field field : declared = clazz.getDeclaredFields()) {
                if (used.contains(field.getName())) continue;
                used.add(field.getName());
                out.put(field, (PropertyWrapper)properties.get(field.getName()));
            }
        } while ((clazz = clazz.getSuperclass()) != null && clazz != Object.class);
        return out;
    }

    public static <A extends Annotation> List<Method> getAnnotatedMethods(Class<?> beanClass, MethodFilter filter, Class<A> annotationType) {
        ArrayList<Method> out = new ArrayList<Method>();
        Class<?> clazz = beanClass;
        do {
            Method[] declared;
            block1: for (Method method : declared = clazz.getDeclaredMethods()) {
                Annotation[] annotations;
                if (!method.isSynthetic() && annotationType == NO_ANNOTATIONS.class) {
                    if (filter.reject(method)) continue;
                    out.add(method);
                    continue;
                }
                for (Annotation annotation : annotations = method.getDeclaredAnnotations()) {
                    if ((annotationType != null || !AnnotationHelper.isCustomAnnotation(annotation)) && annotationType != annotation.annotationType()) continue;
                    if (filter.reject(method)) continue block1;
                    out.add(method);
                    continue block1;
                }
            }
        } while ((clazz = clazz.getSuperclass()) != null && clazz != Object.class);
        return out;
    }

    public static List<Method> getAllMethods(Class<?> beanClass, MethodFilter filter) {
        return AnnotationHelper.getAnnotatedMethods(beanClass, filter, NO_ANNOTATIONS.class);
    }

    public static List<Method> getAnnotatedMethods(Class<?> beanClass, MethodFilter filter) {
        return AnnotationHelper.getAnnotatedMethods(beanClass, filter, null);
    }

    public static List<Field> getAnnotatedFields(Class<?> beanClass) {
        return AnnotationHelper.getAnnotatedFields(beanClass, null);
    }

    public static <A extends Annotation> List<Field> getAnnotatedFields(Class<?> beanClass, Class<A> annotationType) {
        ArrayList<Field> out = new ArrayList<Field>();
        Class<?> clazz = beanClass;
        do {
            Field[] declared;
            block1: for (Field field : declared = clazz.getDeclaredFields()) {
                Annotation[] annotations;
                for (Annotation annotation : annotations = field.getDeclaredAnnotations()) {
                    if ((annotationType != null || !AnnotationHelper.isCustomAnnotation(annotation)) && annotationType != annotation.annotationType()) continue;
                    out.add(field);
                    continue block1;
                }
            }
        } while ((clazz = clazz.getSuperclass()) != null && clazz != Object.class);
        return out;
    }

    public static synchronized <A extends Annotation> A findAnnotation(AnnotatedElement annotatedElement, Class<A> annotationType) {
        if (annotatedElement == null || annotationType == null) {
            return null;
        }
        if (annotatedElement.equals(lastProcessedElement) && annotationType == lastProcessedAnnotationType) {
            return (A)lastAnnotationFound;
        }
        lastProcessedElement = annotatedElement;
        lastProcessedAnnotationType = annotationType;
        Stack<Annotation> path = new Stack<Annotation>();
        Annotation annotation = (Annotation)AnnotationHelper.findAnnotation(annotatedElement, annotationType, new HashSet<Annotation>(), path);
        if (annotation == null || path.isEmpty()) {
            lastAnnotationFound = annotation;
            return (A)annotation;
        }
        while (!path.isEmpty()) {
            Annotation parent = path.pop();
            Annotation target = path.isEmpty() ? annotation : path.peek();
            for (Method method : parent.annotationType().getDeclaredMethods()) {
                Object existingValue;
                Copy copy = method.getAnnotation(Copy.class);
                if (copy == null) continue;
                Class targetClass = copy.to();
                String targetProperty = copy.property();
                if (targetProperty.trim().isEmpty()) {
                    targetProperty = method.getName();
                }
                Object value = (existingValue = AnnotationRegistry.getValue(annotatedElement, target, method.getName())) != null ? existingValue : AnnotationHelper.invoke(parent, method);
                Class<?> sourceValueType = method.getReturnType();
                Class<?> targetPropertyType = AnnotationHelper.findAnnotationMethodType(targetClass, targetProperty);
                if (targetPropertyType != null && targetPropertyType.isArray() && !value.getClass().isArray()) {
                    Object array = Array.newInstance(sourceValueType, 1);
                    Array.set(array, 0, value);
                    value = array;
                }
                if (targetClass == target.annotationType()) {
                    AnnotationRegistry.setValue(annotatedElement, annotation, targetProperty, value);
                    continue;
                }
                Annotation ann = (Annotation)AnnotationHelper.findAnnotation(annotatedElement, targetClass, new HashSet<Annotation>(), new Stack<Annotation>());
                if (ann != null) {
                    AnnotationRegistry.setValue(annotatedElement, ann, targetProperty, value);
                    continue;
                }
                throw new IllegalStateException("Can't process @Copy annotation on '" + method + "'. " + "Annotation '" + targetClass.getName() + "' not used in " + parent.annotationType().getName() + ". Unable to process field " + annotatedElement + "'");
            }
        }
        lastAnnotationFound = annotation;
        return (A)annotation;
    }

    private static Class<?> findAnnotationMethodType(Class<? extends Annotation> type, String methodName) {
        for (Method method : type.getDeclaredMethods()) {
            if (!method.getName().equals(methodName)) continue;
            return method.getReturnType();
        }
        return null;
    }

    private static Object invoke(Annotation annotation, Method method) {
        try {
            return method.invoke(annotation, null);
        }
        catch (Exception e) {
            throw new IllegalStateException("Can't read value from annotation " + annotation, e);
        }
    }

    private static <A> A findAnnotation(AnnotatedElement annotatedElement, Class<A> annotationType, Set<Annotation> visited, Stack<Annotation> path) {
        Annotation ann;
        int i;
        Annotation[] declaredAnnotations = annotatedElement.getDeclaredAnnotations();
        for (i = 0; i < declaredAnnotations.length; ++i) {
            ann = declaredAnnotations[i];
            if (ann.annotationType() != annotationType) continue;
            return (A)ann;
        }
        for (i = 0; i < declaredAnnotations.length; ++i) {
            ann = declaredAnnotations[i];
            if (!AnnotationHelper.isCustomAnnotation(ann) || !visited.add(ann)) continue;
            A annotation = AnnotationHelper.findAnnotation(ann.annotationType(), annotationType, visited, path);
            path.push(ann);
            if (annotation == null) continue;
            return annotation;
        }
        return null;
    }

    private static boolean isCustomAnnotation(Annotation annotation) {
        Class<? extends Annotation> annotationType = annotation.annotationType();
        if (customAnnotationTypes.contains(annotationType)) {
            return true;
        }
        if (javaLangAnnotationTypes.contains(annotationType)) {
            return false;
        }
        if (annotationType.getName().startsWith("java.lang.annotation")) {
            javaLangAnnotationTypes.add(annotationType);
            return false;
        }
        customAnnotationTypes.add(annotationType);
        return true;
    }

    public static List<Annotation> findAllAnnotationsInPackage(AnnotatedElement annotatedElement, Package aPackage) {
        ArrayList<Annotation> found = new ArrayList<Annotation>();
        AnnotationHelper.findAllAnnotationsInPackage(annotatedElement, aPackage, found, new HashSet<Annotation>());
        return found;
    }

    private static void findAllAnnotationsInPackage(AnnotatedElement annotatedElement, Package aPackage, ArrayList<? super Annotation> found, Set<Annotation> visited) {
        Annotation[] declaredAnnotations = annotatedElement.getDeclaredAnnotations();
        for (int i = 0; i < declaredAnnotations.length; ++i) {
            Annotation ann = declaredAnnotations[i];
            if (aPackage.equals(ann.annotationType().getPackage())) {
                found.add(ann);
            }
            if (!AnnotationHelper.isCustomAnnotation(ann) || !visited.add(ann)) continue;
            AnnotationHelper.findAllAnnotationsInPackage(ann.annotationType(), aPackage, found, visited);
        }
    }

    public static final Object getDefaultPrimitiveValue(Class type) {
        if (type == Integer.TYPE) {
            return 0;
        }
        if (type == Double.TYPE) {
            return 0.0;
        }
        if (type == Boolean.TYPE) {
            return Boolean.FALSE;
        }
        if (type == Long.TYPE) {
            return 0L;
        }
        if (type == Float.TYPE) {
            return Float.valueOf(0.0f);
        }
        if (type == Byte.TYPE) {
            return (byte)0;
        }
        if (type == Character.TYPE) {
            return Character.valueOf('\u0000');
        }
        if (type == Short.TYPE) {
            return (short)0;
        }
        return null;
    }

    static {
        javaLangAnnotationTypes = new HashSet<Class>();
        customAnnotationTypes = new HashSet<Class>();
    }

    private static final class NO_ANNOTATIONS
    implements Annotation {
        private NO_ANNOTATIONS() {
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return NO_ANNOTATIONS.class;
        }
    }
}

