/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.beans;

import java.lang.reflect.Method;
import shadeio.univocity.parsers.common.beans.BeanHelper;

public final class PropertyWrapper {
    private static final Method NO_METHOD = PropertyWrapper.getNullMethod();
    private static final String NO_NAME = "!!NO_NAME!!";
    private final Object propertyDescriptor;
    private Method writeMethod;
    private Method readMethod;
    private String name;

    PropertyWrapper(Object propertyDescriptor) {
        this.propertyDescriptor = propertyDescriptor;
    }

    public final Method getWriteMethod() {
        if (this.writeMethod == null) {
            this.writeMethod = (Method)PropertyWrapper.invoke(this.propertyDescriptor, BeanHelper.PROPERTY_WRITE_METHOD);
        }
        return this.writeMethod == NO_METHOD ? null : this.writeMethod;
    }

    public final Method getReadMethod() {
        if (this.readMethod == null) {
            this.readMethod = (Method)PropertyWrapper.invoke(this.propertyDescriptor, BeanHelper.PROPERTY_READ_METHOD);
        }
        return this.readMethod == NO_METHOD ? null : this.readMethod;
    }

    public final String getName() {
        if (this.name == null) {
            this.name = (String)PropertyWrapper.invoke(this.propertyDescriptor, BeanHelper.PROPERTY_NAME_METHOD);
        }
        return this.name == NO_NAME ? null : this.name;
    }

    private static Object invoke(Object propertyDescriptor, Method method) {
        try {
            return method.invoke(propertyDescriptor, new Object[0]);
        }
        catch (Exception ex) {
            return null;
        }
    }

    private static Method getNullMethod() {
        try {
            return Object.class.getMethod("hashCode", new Class[0]);
        }
        catch (NoSuchMethodException e) {
            return null;
        }
    }
}

