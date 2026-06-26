/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.annotations.helpers;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import shadeio.univocity.parsers.annotations.HeaderTransformer;
import shadeio.univocity.parsers.annotations.Parsed;
import shadeio.univocity.parsers.annotations.helpers.AnnotationHelper;
import shadeio.univocity.parsers.annotations.helpers.AnnotationRegistry;
import shadeio.univocity.parsers.common.DataProcessingException;
import shadeio.univocity.parsers.common.NormalizedString;
import shadeio.univocity.parsers.common.beans.PropertyWrapper;

public class FieldMapping {
    private final Class parentClass;
    private final AnnotatedElement target;
    private int index;
    private NormalizedString fieldName;
    private final Class<?> beanClass;
    private final Method readMethod;
    private final Method writeMethod;
    private boolean accessible;
    private final boolean primitive;
    private final Object defaultPrimitiveValue;
    private Boolean applyDefault = null;
    private Class fieldType;
    private boolean primitiveNumber;

    public FieldMapping(Class<?> beanClass, AnnotatedElement target, PropertyWrapper property, HeaderTransformer transformer, NormalizedString[] headers) {
        Class<Object> typeToSet;
        this.beanClass = beanClass;
        this.target = target;
        if (target instanceof Field) {
            this.readMethod = property != null ? property.getReadMethod() : null;
            this.writeMethod = property != null ? property.getWriteMethod() : null;
        } else {
            Method method = (Method)target;
            this.readMethod = method.getReturnType() != Void.class ? method : null;
            Method method2 = this.writeMethod = method.getParameterTypes().length != 0 ? method : null;
        }
        if (target != null) {
            typeToSet = AnnotationHelper.getType(target);
            this.parentClass = AnnotationHelper.getDeclaringClass(target);
        } else if (this.writeMethod != null && this.writeMethod.getParameterTypes().length == 1) {
            typeToSet = this.writeMethod.getParameterTypes()[0];
            this.parentClass = this.writeMethod.getDeclaringClass();
        } else {
            typeToSet = Object.class;
            this.parentClass = this.readMethod != null ? this.readMethod.getDeclaringClass() : beanClass;
        }
        this.primitive = typeToSet.isPrimitive();
        this.defaultPrimitiveValue = AnnotationHelper.getDefaultPrimitiveValue(typeToSet);
        this.primitiveNumber = this.defaultPrimitiveValue instanceof Number;
        this.fieldType = typeToSet;
        this.determineFieldMapping(transformer, headers);
    }

    private void determineFieldMapping(HeaderTransformer transformer, NormalizedString[] headers) {
        Parsed parsed = AnnotationHelper.findAnnotation(this.target, Parsed.class);
        String name = "";
        if (parsed != null) {
            this.index = AnnotationRegistry.getValue(this.target, parsed, "index", parsed.index());
            if (this.index >= 0) {
                this.fieldName = null;
                if (transformer != null) {
                    this.index = transformer.transformIndex(this.target, this.index);
                }
                return;
            }
            String[] fields = AnnotationRegistry.getValue(this.target, parsed, "field", parsed.field());
            if (fields.length > 1 && headers != null) {
                block0: for (int i = 0; i < headers.length; ++i) {
                    NormalizedString header = headers[i];
                    if (header == null) continue;
                    for (int j = 0; j < fields.length; ++j) {
                        String field = fields[j];
                        if (!header.equals(field)) continue;
                        name = field;
                        continue block0;
                    }
                }
            }
            if (name.isEmpty()) {
                String string = name = fields.length == 0 ? "" : fields[0];
            }
        }
        if (name.isEmpty()) {
            name = AnnotationHelper.getName(this.target);
        }
        this.fieldName = NormalizedString.valueOf(name);
        if (parsed != null && transformer != null) {
            if (this.index >= 0) {
                this.index = transformer.transformIndex(this.target, this.index);
            } else if (this.fieldName != null) {
                this.fieldName = NormalizedString.valueOf(transformer.transformName(this.target, this.fieldName.toString()));
            }
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        FieldMapping that = (FieldMapping)o;
        if (this.index != that.index) {
            return false;
        }
        if (!this.target.equals(that.target)) {
            return false;
        }
        if (this.fieldName != null ? !this.fieldName.equals(that.fieldName) : that.fieldName != null) {
            return false;
        }
        return this.beanClass.equals(that.beanClass);
    }

    public int hashCode() {
        int result = this.target.hashCode();
        result = 31 * result + this.index;
        result = 31 * result + (this.fieldName != null ? this.fieldName.hashCode() : 0);
        result = 31 * result + this.beanClass.hashCode();
        return result;
    }

    public boolean isMappedToIndex() {
        return this.index >= 0;
    }

    public boolean isMappedToField() {
        return this.index < 0;
    }

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = NormalizedString.valueOf(fieldName);
    }

    public void setFieldName(NormalizedString fieldName) {
        this.fieldName = fieldName;
    }

    public NormalizedString getFieldName() {
        return this.fieldName;
    }

    public AnnotatedElement getTarget() {
        return this.target;
    }

    private void setAccessible() {
        if (!this.accessible) {
            Method method;
            if (this.target instanceof Field) {
                Field field = (Field)this.target;
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
            } else if (this.target instanceof Method && !(method = (Method)this.target).isAccessible()) {
                method.setAccessible(true);
            }
            this.accessible = true;
        }
    }

    public Class<?> getFieldParent() {
        return this.parentClass;
    }

    public Class<?> getFieldType() {
        return this.fieldType;
    }

    public boolean canWrite(Object instance) {
        if (!this.primitive) {
            if (instance == null) {
                return true;
            }
            return this.fieldType.isAssignableFrom(instance.getClass());
        }
        if (instance instanceof Number) {
            return this.primitiveNumber;
        }
        if (instance instanceof Boolean) {
            return this.fieldType == Boolean.TYPE;
        }
        if (instance instanceof Character) {
            return this.fieldType == Character.TYPE;
        }
        return false;
    }

    public Object read(Object instance) {
        return this.read(instance, false);
    }

    private Object read(Object instance, boolean ignoreErrors) {
        this.setAccessible();
        try {
            if (this.readMethod != null) {
                return this.readMethod.invoke(instance, new Object[0]);
            }
            return ((Field)this.target).get(instance);
        }
        catch (Throwable e) {
            if (e instanceof InvocationTargetException) {
                e = e.getCause();
            }
            if (!ignoreErrors) {
                String msg = "Unable to get value from field: " + this.toString();
                if (e instanceof DataProcessingException) {
                    DataProcessingException ex = (DataProcessingException)e;
                    ex.setDetails(msg);
                    throw ex;
                }
                throw new DataProcessingException(msg, e);
            }
            return null;
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public void write(Object instance, Object value) {
        this.setAccessible();
        try {
            if (this.primitive) {
                if (value == null) {
                    if (this.applyDefault == null) {
                        Object currentValue = this.read(instance, true);
                        this.applyDefault = this.defaultPrimitiveValue.equals(currentValue);
                    }
                    if (this.applyDefault != Boolean.TRUE) return;
                    value = this.defaultPrimitiveValue;
                } else if (this.defaultPrimitiveValue.getClass() != value.getClass() && value instanceof Number) {
                    Number number = (Number)value;
                    if (this.fieldType == Integer.TYPE) {
                        value = number.intValue();
                    } else if (this.fieldType == Long.TYPE) {
                        value = number.longValue();
                    } else if (this.fieldType == Double.TYPE) {
                        value = number.doubleValue();
                    } else if (this.fieldType == Float.TYPE) {
                        value = Float.valueOf(number.floatValue());
                    } else if (this.fieldType == Byte.TYPE) {
                        value = number.byteValue();
                    } else if (this.fieldType == Short.TYPE) {
                        value = number.shortValue();
                    }
                }
            }
            if (this.writeMethod != null) {
                this.writeMethod.invoke(instance, value);
                return;
            } else {
                ((Field)this.target).set(instance, value);
            }
            return;
        }
        catch (Throwable e) {
            String valueTypeName = value == null ? null : value.getClass().getName();
            String details = null;
            String msg = valueTypeName != null ? "Unable to set value '{value}' of type '" + valueTypeName + "' to " + this.toString() : "Unable to set value 'null' to " + this.toString();
            if (e instanceof InvocationTargetException) {
                e = e.getCause();
                details = msg;
            }
            if (e instanceof DataProcessingException) {
                DataProcessingException ex = (DataProcessingException)e;
                ex.markAsNonFatal();
                ex.setValue(value);
                ex.setDetails(details);
                throw (DataProcessingException)e;
            }
            DataProcessingException ex = new DataProcessingException(msg, e);
            ex.markAsNonFatal();
            ex.setValue(value);
            throw ex;
        }
    }

    public String toString() {
        return AnnotationHelper.describeElement(this.target);
    }
}

