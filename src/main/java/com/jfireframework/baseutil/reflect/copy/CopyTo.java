package com.jfireframework.baseutil.reflect.copy;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 注解在拷贝的源头类上。代表着拷贝到目的类的哪一个属性
 * 
 * @author linbin
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface CopyTo
{
	String name();
	
	Class<?> to();
}
