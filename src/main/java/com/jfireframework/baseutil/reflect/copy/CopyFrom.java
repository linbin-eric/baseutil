package com.jfireframework.baseutil.reflect.copy;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 注解在拷贝的目的类上，代表从该属性从源头类的哪一个属性拷贝
 * 
 * @author linbin
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface CopyFrom
{
	String name();
	
	Class<?> from();
	
}
