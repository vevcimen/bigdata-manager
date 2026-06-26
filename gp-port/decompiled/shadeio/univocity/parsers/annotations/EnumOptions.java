/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import shadeio.univocity.parsers.conversions.EnumSelector;

@Retention(value=RetentionPolicy.RUNTIME)
@Inherited
@Target(value={ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface EnumOptions {
    public EnumSelector[] selectors() default {EnumSelector.NAME, EnumSelector.ORDINAL, EnumSelector.STRING};

    public String customElement() default "";
}

