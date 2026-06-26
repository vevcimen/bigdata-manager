/*
 * Decompiled with CFR 0.152.
 */
package shadeio.univocity.parsers.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import shadeio.univocity.parsers.fixed.FieldAlignment;

@Retention(value=RetentionPolicy.RUNTIME)
@Inherited
@Target(value={ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface FixedWidth {
    public int value() default -1;

    public FieldAlignment alignment() default FieldAlignment.LEFT;

    public char padding() default 32;

    public boolean keepPadding() default false;

    public int from() default -1;

    public int to() default -1;
}

