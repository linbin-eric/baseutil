package com.jfireframework.baseutil.anno;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.jfireframework.baseutil.reflect.ReflectUtil;

public class CompositionAnnotationImpl implements CompositionAnnotation
{
    private Annotation                 annotation;
    private Set<CompositionAnnotation> compositionAnnotations;
    
    class Context
    {
        Map<Class<? extends Annotation>, Map<String, Object>> store = new HashMap<Class<? extends Annotation>, Map<String, Object>>();
        
        void put(Class<? extends Annotation> type, String name, Object value)
        {
            Map<String, Object> map = store.get(type);
            if (map == null)
            {
                map = new HashMap<String, Object>();
                store.put(type, map);
            }
            map.put(name, value);
        }
        
        Map<String, Object> get(Class<? extends Annotation> type)
        {
            return store.get(type);
        }
    }
    
    public CompositionAnnotationImpl(Annotation annotation)
    {
        this(annotation, new HashMap<String, Object>());
    }
    
    public CompositionAnnotationImpl(final Annotation annotation, final Map<String, Object> predOverrideAttributes)
    {
        try
        {
            if (predOverrideAttributes == null || predOverrideAttributes.isEmpty())
            {
                this.annotation = annotation;
            }
            else
            {
                this.annotation = (Annotation) Proxy.newProxyInstance(annotation.annotationType().getClassLoader(), new Class<?>[] { annotation.annotationType() }, //
                        new InvocationHandler() {
                            
                            @Override
                            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
                            {
                                Object value = predOverrideAttributes.get(method.getName());
                                return value == null ? method.invoke(annotation, args) : value;
                            }
                        });
            }
            Context context = new Context();
            Class<? extends Annotation> annotationType = annotation.annotationType();
            for (Method method : annotationType.getMethods())
            {
                if (method.getParameterTypes().length != 0 || method.isAnnotationPresent(OverridesAttribute.class) == false)
                {
                    continue;
                }
                method.setAccessible(true);
                OverridesAttribute overridesAttribute = method.getAnnotation(OverridesAttribute.class);
                Class<? extends Annotation> overrideAnnotationClass = overridesAttribute.annotation();
                String name = overridesAttribute.name();
                Object value = method.invoke(annotation);
                context.put(overrideAnnotationClass, name, value);
            }
            Set<CompositionAnnotation> set = new HashSet<CompositionAnnotation>();
            for (Annotation annotatedAnnotation : annotationType.getAnnotations())
            {
                Class<? extends Annotation> annotatedAnnotationType = annotatedAnnotation.annotationType();
                if (annotatedAnnotationType == Documented.class || annotatedAnnotationType == Target.class || annotatedAnnotationType == Retention.class)
                {
                    continue;
                }
                set.add(new CompositionAnnotationImpl(annotatedAnnotation, context.get(annotatedAnnotationType)));
            }
            compositionAnnotations = Collections.unmodifiableSet(set);
        }
        catch (Exception e)
        {
            ReflectUtil.throwException(e);
        }
    }
    
    @Override
    public Annotation getAnnotation()
    {
        return annotation;
    }
    
    @Override
    public Set<CompositionAnnotation> getCompositionAnnotations()
    {
        return compositionAnnotations;
    }
    
}
