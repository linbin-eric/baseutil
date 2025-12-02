package xin.nb1.baseutil.bytecode.support;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class CacheableAnnotationContextFactory implements AnnotationContextFactory
{
    protected Map<Method, AnnotationContext> methodAnnotationContextStore       = new ConcurrentHashMap<>();
    protected Map<String, AnnotationContext> resourceNameAnnotationContextStore = new ConcurrentHashMap<>();
    protected Map<Field, AnnotationContext>  fieldAnnotationContextStore        = new ConcurrentHashMap<>();

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
        return methodAnnotationContextStore.computeIfAbsent(method, m -> build(m, classLoader));
    }

    @Override
    public AnnotationContext get(String resourceName, ClassLoader classLoader)
    {
        return resourceNameAnnotationContextStore.computeIfAbsent(resourceName, value -> build(value, classLoader));
    }

    @Override
    public AnnotationContext get(Field field, ClassLoader classLoader)
    {
        return fieldAnnotationContextStore.computeIfAbsent(field, f -> build(f, classLoader));
    }

    protected abstract AnnotationContext build(Field field, ClassLoader classLoader);

    @Override
    public AnnotationContext get(Class<?> ckass)
    {
        return get(ckass, Thread.currentThread().getContextClassLoader());
    }

    @Override
    public AnnotationContext get(String resourceName)
    {
        return get(resourceName, Thread.currentThread().getContextClassLoader());
    }

    @Override
    public AnnotationContext get(Method method)
    {
        return get(method, Thread.currentThread().getContextClassLoader());
    }

    @Override
    public AnnotationContext get(Field field)
    {
        return get(field, Thread.currentThread().getContextClassLoader());
    }

    @Override
    public AnnotationContext get(AnnotatedElement annotatedElement)
    {
        if (annotatedElement instanceof Class ckass)
        {
            return get(ckass);
        }
        else if (annotatedElement instanceof Method method)
        {
            return get(method);
        }
        else if (annotatedElement instanceof Field field)
        {
            return get(field);
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }
}
