package com.jfireframework.baseutil.bytecode.support;

import java.lang.reflect.Method;
import java.util.IdentityHashMap;

public abstract class CacheableAnnotationContextFactory implements AnnotationContextFactory
{

    protected IdentityHashMap<Class<?>, AnnotationContext> classAnnotationContextStore  = new IdentityHashMap<Class<?>, AnnotationContext>();
    protected IdentityHashMap<Method, AnnotationContext>   methodAnnotationContextStore = new IdentityHashMap<Method, AnnotationContext>();

    protected abstract AnnotationContext build(Class<?> ckass, ClassLoader classLoader);

    protected abstract AnnotationContext build(Method method, ClassLoader classLoader);

    @Override
    public AnnotationContext get(Class<?> ckass, ClassLoader classLoader)
    {
        AnnotationContext annotationContext = classAnnotationContextStore.get(ckass);
        if (annotationContext != null)
        {
            return annotationContext;
        }
        annotationContext = build(ckass, classLoader);
        classAnnotationContextStore.put(ckass, annotationContext);
        return annotationContext;
    }

    @Override
    public AnnotationContext get(Method method, ClassLoader classLoader)
    {
        AnnotationContext annotationContext = methodAnnotationContextStore.get(method);
        if (annotationContext != null)
        {
            return annotationContext;
        }
        annotationContext = build(method, classLoader);
        methodAnnotationContextStore.put(method, annotationContext);
        return annotationContext;
    }
}
