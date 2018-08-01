package com.jfireframework.baseutil.reflect.copy;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
public @interface CopyIgnore
{
    Class<?> from() default Object.class;
    
    Class<?> to() default Object.class;
    
    @Target({ METHOD, FIELD })
    @Retention(RUNTIME)
    @Documented
    @interface List
    {
        CopyIgnore[] value();
    }
}
