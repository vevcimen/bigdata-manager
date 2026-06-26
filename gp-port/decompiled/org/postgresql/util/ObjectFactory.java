/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ObjectFactory {
    public static <T> T instantiate(Class<T> expectedClass, String classname, Properties info, boolean tryString, @Nullable String stringarg) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Object[] args = new Object[]{info};
        Constructor<T> ctor = null;
        Class<T> cls = Class.forName(classname).asSubclass(expectedClass);
        try {
            ctor = cls.getConstructor(Properties.class);
        }
        catch (NoSuchMethodException noSuchMethodException) {
            // empty catch block
        }
        if (tryString && ctor == null) {
            try {
                ctor = cls.getConstructor(String.class);
                args = new String[]{stringarg};
            }
            catch (NoSuchMethodException noSuchMethodException) {
                // empty catch block
            }
        }
        if (ctor == null) {
            ctor = cls.getConstructor(new Class[0]);
            args = new Object[]{};
        }
        return ctor.newInstance(args);
    }
}

