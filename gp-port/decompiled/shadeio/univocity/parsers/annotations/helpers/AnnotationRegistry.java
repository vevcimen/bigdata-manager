/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.annotations.helpers;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

public class AnnotationRegistry {
    private static final Map<AnnotatedElement, FieldAnnotations> modifiedAnnotations = new HashMap<AnnotatedElement, FieldAnnotations>();

    static final synchronized void setValue(AnnotatedElement annotatedElement, Annotation annotation, String attribute, Object newValue) {
        FieldAnnotations attributes = modifiedAnnotations.get(annotatedElement);
        if (attributes == null) {
            attributes = new FieldAnnotations();
            modifiedAnnotations.put(annotatedElement, attributes);
        }
        attributes.setValue(annotation, attribute, newValue);
    }

    public static final synchronized <T> T getValue(AnnotatedElement annotatedElement, Annotation annotation, String attribute, T valueIfNull) {
        if (annotatedElement == null) {
            return valueIfNull;
        }
        Object value = AnnotationRegistry.getValue(annotatedElement, annotation, attribute);
        if (value == null) {
            return valueIfNull;
        }
        return (T)value;
    }

    static final synchronized Object getValue(AnnotatedElement annotatedElement, Annotation annotation, String attribute) {
        FieldAnnotations attributes = modifiedAnnotations.get(annotatedElement);
        if (attributes == null) {
            return null;
        }
        return attributes.getValue(annotation, attribute);
    }

    public static final void reset() {
        modifiedAnnotations.clear();
    }

    private static class AnnotationAttributes {
        private Map<String, Object> attributes = new HashMap<String, Object>();

        private AnnotationAttributes() {
        }

        private void setAttribute(String attribute, Object newValue) {
            if (!this.attributes.containsKey(attribute)) {
                this.attributes.put(attribute, newValue);
            } else {
                Class<?> newClass;
                Object existingValue = this.attributes.get(attribute);
                if (existingValue == null || newValue == null) {
                    return;
                }
                Class<?> originalClass = existingValue.getClass();
                if (originalClass != (newClass = newValue.getClass()) && newClass.isArray() && newClass.getComponentType() == existingValue.getClass()) {
                    Object array = Array.newInstance(originalClass, 1);
                    Array.set(array, 0, existingValue);
                    this.attributes.put(attribute, array);
                }
            }
        }

        private Object getAttribute(String attribute) {
            return this.attributes.get(attribute);
        }
    }

    private static class FieldAnnotations {
        private Map<Annotation, AnnotationAttributes> annotations = new HashMap<Annotation, AnnotationAttributes>();

        private FieldAnnotations() {
        }

        private void setValue(Annotation annotation, String attribute, Object newValue) {
            AnnotationAttributes attributes = this.annotations.get(annotation);
            if (attributes == null) {
                attributes = new AnnotationAttributes();
                this.annotations.put(annotation, attributes);
            }
            attributes.setAttribute(attribute, newValue);
        }

        private Object getValue(Annotation annotation, String attribute) {
            AnnotationAttributes attributes = this.annotations.get(annotation);
            if (attributes == null) {
                return null;
            }
            return attributes.getAttribute(attribute);
        }
    }
}

