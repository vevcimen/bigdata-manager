/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.common.beans;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import shadeio.univocity.parsers.common.beans.PropertyWrapper;

public final class BeanHelper {
    private static final PropertyWrapper[] EMPTY = new PropertyWrapper[0];
    private static final Class<?> introspectorClass = BeanHelper.findIntrospectorImplementationClass();
    private static final Method beanInfoMethod = BeanHelper.getBeanInfoMethod();
    private static final Method propertyDescriptorMethod = BeanHelper.getMethod("getPropertyDescriptors", beanInfoMethod, false);
    static Method PROPERTY_WRITE_METHOD = BeanHelper.getMethod("getWriteMethod", propertyDescriptorMethod, true);
    static Method PROPERTY_READ_METHOD = BeanHelper.getMethod("getReadMethod", propertyDescriptorMethod, true);
    static Method PROPERTY_NAME_METHOD = BeanHelper.getMethod("getName", propertyDescriptorMethod, true);
    private static final Map<Class<?>, WeakReference<PropertyWrapper[]>> descriptors = new ConcurrentHashMap();

    private BeanHelper() {
    }

    public static PropertyWrapper[] getPropertyDescriptors(Class<?> beanClass) {
        if (propertyDescriptorMethod == null) {
            return EMPTY;
        }
        PropertyWrapper[] out = null;
        WeakReference<PropertyWrapper[]> reference = descriptors.get(beanClass);
        if (reference != null) {
            out = (PropertyWrapper[])reference.get();
        }
        if (out == null) {
            try {
                Object beanInfo = beanInfoMethod.invoke(null, beanClass, Object.class);
                Object[] propertyDescriptors = (Object[])propertyDescriptorMethod.invoke(beanInfo, new Object[0]);
                out = new PropertyWrapper[propertyDescriptors.length];
                for (int i = 0; i < propertyDescriptors.length; ++i) {
                    out[i] = new PropertyWrapper(propertyDescriptors[i]);
                }
            }
            catch (Exception ex) {
                out = EMPTY;
            }
            descriptors.put(beanClass, new WeakReference<PropertyWrapper[]>(out));
        }
        return out;
    }

    private static Class<?> findIntrospectorImplementationClass() {
        try {
            return Class.forName("com.googlecode.openbeans.Introspector");
        }
        catch (Throwable e1) {
            try {
                return Class.forName("java.beans.Introspector");
            }
            catch (Throwable e2) {
                return null;
            }
        }
    }

    private static Method getBeanInfoMethod() {
        if (introspectorClass == null) {
            return null;
        }
        try {
            return introspectorClass.getMethod("getBeanInfo", Class.class, Class.class);
        }
        catch (Throwable e) {
            return null;
        }
    }

    private static Method getMethod(String methodName, Method method, boolean fromComponentType) {
        if (method == null) {
            return null;
        }
        try {
            Class<?> returnType = method.getReturnType();
            if (fromComponentType) {
                returnType = returnType.getComponentType();
            }
            return returnType.getMethod(methodName, new Class[0]);
        }
        catch (Exception ex) {
            return null;
        }
    }
}

