package com.jfireframework.baseutil.aliasanno;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface AliasFor
{
    public String value();
    
    /**
     * 该属性是否用于继承。<br/>
     * 注意，只有数组属性才支持继承
     * 
     * @return
     */
    public boolean isExtends() default false;
    
    Class<? extends Annotation> annotation();
}
