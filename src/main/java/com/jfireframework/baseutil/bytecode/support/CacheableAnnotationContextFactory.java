package com.jfireframework.baseutil.bytecode.support;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public abstract class CacheableAnnotationContextFactory implements AnnotationContextFactory
{

    protected IdentityHashMap<Method, AnnotationContext> methodAnnotationContextStore       = new IdentityHashMap<Method, AnnotationContext>();
    protected Map<String, AnnotationContext>             resourceNameAnnotationContextStore = new HashMap<String, AnnotationContext>();

    protected abstract AnnotationContext build(String resourceName, ClassLoader classLoader);

    protected abstract AnnotationContext build(Method method, ClassLoader classLoader);

    @Override
    public AnnotationContext get(Class<?> ckass, ClassLoader classLoader)
    {
        return get(ckass.getName().replace('.', '/'), classLoader);
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

    @Override
    public AnnotationContext get(String resourceName, ClassLoader classLoader)
    {
        AnnotationContext annotationContext = resourceNameAnnotationContextStore.get(resourceName);
        if (annotationContext != null)
        {
            return annotationContext;
        }
        annotationContext = build(resourceName, classLoader);
        resourceNameAnnotationContextStore.put(resourceName, annotationContext);
        return annotationContext;
    }
}
