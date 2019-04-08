package com.jfireframework.baseutil.bytecode.support;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public interface AnnotationContextFactory
{
    AnnotationContext get(Class<?> ckass, ClassLoader classLoader);

    AnnotationContext get(String resourceName, ClassLoader classLoader);

    AnnotationContext get(Method method, ClassLoader classLoader);

    AnnotationContext get(Field field, ClassLoader classLoader);
}
