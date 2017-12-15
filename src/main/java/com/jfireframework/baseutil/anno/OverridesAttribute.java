package com.jfireframework.baseutil.anno;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface OverridesAttribute
{
    /**
     * 需要覆盖的注解
     * 
     * @return
     */
    Class<? extends Annotation> annotation();
    
    /**
     * 需要覆盖的属性名称
     * 
     * @return
     */
    String name();
    
}
