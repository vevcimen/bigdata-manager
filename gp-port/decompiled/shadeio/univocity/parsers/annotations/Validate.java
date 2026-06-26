/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import shadeio.univocity.parsers.conversions.Validator;

@Retention(value=RetentionPolicy.RUNTIME)
@Inherited
@Target(value={ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface Validate {
    public boolean nullable() default false;

    public boolean allowBlanks() default false;

    public String matches() default "";

    public String[] oneOf() default {};

    public String[] noneOf() default {};

    public Class<? extends Validator>[] validators() default {};
}

