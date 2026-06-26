/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.annotations.helpers;

import java.lang.reflect.Method;
import shadeio.univocity.parsers.annotations.helpers.MethodDescriptor;

public enum MethodFilter {
    ONLY_GETTERS(new Filter(){

        @Override
        public boolean reject(Method method) {
            return method.getReturnType() == Void.TYPE || method.getParameterTypes().length != 0;
        }
    }),
    ONLY_SETTERS(new Filter(){

        @Override
        public boolean reject(Method method) {
            return method.getParameterTypes().length != 1;
        }
    });

    private Filter filter;

    private MethodFilter(Filter filter) {
        this.filter = filter;
    }

    public boolean reject(Method method) {
        return this.filter.reject(method);
    }

    public MethodDescriptor toDescriptor(String prefix, Method method) {
        if (this.reject(method)) {
            return null;
        }
        if (this == ONLY_SETTERS) {
            return MethodDescriptor.setter(prefix, method);
        }
        return MethodDescriptor.getter(prefix, method);
    }

    private static interface Filter {
        public boolean reject(Method var1);
    }
}

