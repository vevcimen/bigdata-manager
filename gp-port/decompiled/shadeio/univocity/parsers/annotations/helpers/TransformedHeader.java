/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.annotations.helpers;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import shadeio.univocity.parsers.annotations.HeaderTransformer;
import shadeio.univocity.parsers.annotations.Parsed;
import shadeio.univocity.parsers.annotations.helpers.AnnotationHelper;
import shadeio.univocity.parsers.annotations.helpers.AnnotationRegistry;

public class TransformedHeader {
    private final AnnotatedElement target;
    private final Field field;
    private final Method method;
    private final HeaderTransformer transformer;
    private int index = -2;

    public TransformedHeader(AnnotatedElement target, HeaderTransformer transformer) {
        if (target instanceof Field) {
            this.field = (Field)target;
            this.method = null;
        } else {
            this.method = (Method)target;
            this.field = null;
        }
        this.target = target;
        this.transformer = transformer;
    }

    public String getHeaderName() {
        String[] field;
        if (this.target == null) {
            return null;
        }
        String name = null;
        Parsed annotation = AnnotationHelper.findAnnotation(this.target, Parsed.class);
        if (annotation != null && (name = (field = AnnotationRegistry.getValue(this.target, annotation, "field", annotation.field())).length == 0 ? this.getTargetName() : field[0]).length() == 0) {
            name = this.getTargetName();
        }
        if (this.transformer != null) {
            if (this.field != null) {
                return this.transformer.transformName(this.field, name);
            }
            return this.transformer.transformName(this.method, name);
        }
        return name;
    }

    public int getHeaderIndex() {
        if (this.index == -2) {
            Parsed annotation = AnnotationHelper.findAnnotation(this.target, Parsed.class);
            if (annotation != null) {
                this.index = AnnotationRegistry.getValue(this.target, annotation, "index", annotation.index());
                if (this.index != -1 && this.transformer != null) {
                    this.index = this.field != null ? this.transformer.transformIndex(this.field, this.index) : this.transformer.transformIndex(this.method, this.index);
                }
            } else {
                this.index = -1;
            }
        }
        return this.index;
    }

    public String getTargetName() {
        if (this.target == null) {
            return null;
        }
        if (this.field != null) {
            return this.field.getName();
        }
        return this.method.getName();
    }

    public AnnotatedElement getTarget() {
        return this.target;
    }

    public boolean isWriteOnly() {
        if (this.method != null) {
            return this.method.getParameterTypes().length != 0;
        }
        return false;
    }

    public boolean isReadOly() {
        if (this.method != null) {
            return this.method.getParameterTypes().length == 0 && this.method.getReturnType() != Void.TYPE;
        }
        return false;
    }

    public String describe() {
        return AnnotationHelper.describeElement(this.target);
    }
}

