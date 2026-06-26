/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.annotations;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public abstract class HeaderTransformer {
    public final String transformName(AnnotatedElement element, String name) {
        if (element instanceof Field) {
            return this.transformName((Field)element, name);
        }
        return this.transformName((Method)element, name);
    }

    public final int transformIndex(AnnotatedElement element, int index) {
        if (element instanceof Field) {
            return this.transformIndex((Field)element, index);
        }
        return this.transformIndex((Method)element, index);
    }

    public String transformName(Field field, String name) {
        return name;
    }

    public int transformIndex(Field field, int index) {
        return index;
    }

    public String transformName(Method method, String name) {
        return name;
    }

    public int transformIndex(Method method, int index) {
        return index;
    }
}

