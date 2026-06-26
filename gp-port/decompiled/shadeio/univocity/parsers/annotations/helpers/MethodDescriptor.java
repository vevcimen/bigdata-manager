/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.annotations.helpers;

import java.lang.reflect.Method;

public final class MethodDescriptor {
    private final String prefixedName;
    private final String name;
    private final String prefix;
    private final Class<?> parameterType;
    private final Class<?> returnType;
    private final String string;

    private MethodDescriptor(String name, Class<?> parameterType, Class<?> returnType) {
        this.prefixedName = name;
        int lastDot = name.lastIndexOf(46);
        if (lastDot == -1) {
            this.name = name;
            this.prefix = "";
        } else {
            this.name = name.substring(lastDot + 1);
            this.prefix = name.substring(0, lastDot);
        }
        this.parameterType = parameterType;
        this.returnType = returnType;
        this.string = this.generateString();
    }

    private MethodDescriptor(String prefix, String name, Class<?> parameterType, Class<?> returnType) {
        this.prefixedName = prefix + '.' + name;
        this.name = name;
        this.prefix = prefix;
        this.parameterType = parameterType;
        this.returnType = returnType;
        this.string = this.generateString();
    }

    private String generateString() {
        StringBuilder out = new StringBuilder("method ");
        if (this.returnType != null) {
            out.append(this.returnType.getName());
            out.append(' ');
        }
        if (this.prefix.isEmpty()) {
            out.append(this.name);
        } else {
            out.append(this.prefix);
            out.append('.');
            out.append(this.name);
        }
        if (this.parameterType != null) {
            out.append('(');
            out.append(this.parameterType.getName());
            out.append(')');
        } else {
            out.append("()");
        }
        return out.toString();
    }

    public static MethodDescriptor setter(String name, Class<?> parameterType) {
        return new MethodDescriptor(name, parameterType, null);
    }

    public static MethodDescriptor getter(String name, Class<?> returnType) {
        return new MethodDescriptor(name, null, returnType);
    }

    static MethodDescriptor setter(String prefix, Method method) {
        return new MethodDescriptor(prefix, method.getName(), method.getParameterTypes()[0], null);
    }

    static MethodDescriptor getter(String prefix, Method method) {
        return new MethodDescriptor(prefix, method.getName(), null, method.getReturnType());
    }

    public String getName() {
        return this.name;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public Class<?> getParameterType() {
        return this.parameterType;
    }

    public Class<?> getReturnType() {
        return this.returnType;
    }

    public String getPrefixedName() {
        return this.prefixedName;
    }

    public String toString() {
        return this.string;
    }

    public boolean equals(Object o) {
        if (o == null || o.getClass() != this.getClass()) {
            return false;
        }
        return this.string.equals(o.toString());
    }

    public int hashCode() {
        return this.string.hashCode();
    }
}

