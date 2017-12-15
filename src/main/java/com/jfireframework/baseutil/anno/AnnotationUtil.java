package com.jfireframework.baseutil.anno;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

public class AnnotationUtil
{
    private final IdentityHashMap<Annotation, CompositionAnnotation> treeMap = new IdentityHashMap<Annotation, CompositionAnnotation>();
    
    public boolean isPresent(Class<? extends Annotation> annoType, Field field)
    {
        if (field.isAnnotationPresent(annoType))
        {
            return true;
        }
        return isPresent(annoType, field.getAnnotations());
    }
    
    private CompositionAnnotation getCompositionAnnotation(Annotation annotation)
    {
        CompositionAnnotation compositionAnnotation = treeMap.get(annotation);
        if (compositionAnnotation == null)
        {
            compositionAnnotation = new CompositionAnnotationImpl(annotation);
            treeMap.put(annotation, compositionAnnotation);
        }
        return compositionAnnotation;
    }
    
    public boolean isPresent(Class<? extends Annotation> annoType, Annotation... annotations)
    {
        for (Annotation each : annotations)
        {
            if (isPresent(annoType, getCompositionAnnotation(each)))
            {
                return true;
            }
        }
        return false;
    }
    
    private boolean isPresent(Class<? extends Annotation> annoType, CompositionAnnotation compositionAnnotation)
    {
        if (compositionAnnotation.getAnnotation().annotationType().isAnnotationPresent(annoType))
        {
            return true;
        }
        for (CompositionAnnotation each : compositionAnnotation.getCompositionAnnotations())
        {
            if (isPresent(annoType, each))
            {
                return true;
            }
        }
        return false;
    }
    
    public boolean isPresent(Class<? extends Annotation> annoType, Class<?> target)
    {
        if (target.isAnnotationPresent(annoType) && target.isAnnotation() == false)
        {
            return true;
        }
        return isPresent(annoType, target.getAnnotations());
    }
    
    public boolean isPresent(Class<? extends Annotation> annoType, Method method)
    {
        if (method.isAnnotationPresent(annoType))
        {
            return true;
        }
        return isPresent(annoType, method.getAnnotations());
    }
    
    public <T extends Annotation> T getAnnotation(Class<T> annoType, Method method)
    {
        T result = method.getAnnotation(annoType);
        if (result != null)
        {
            return result;
        }
        return getAnnotation(annoType, method.getAnnotations());
    }
    
    public <T extends Annotation> T getAnnotation(Class<T> annoType, Field field)
    {
        T result = field.getAnnotation(annoType);
        if (result != null)
        {
            return result;
        }
        return getAnnotation(annoType, field.getAnnotations());
    }
    
    public <T extends Annotation> T getAnnotation(Class<T> annoType, Annotation... annotations)
    {
        for (Annotation each : annotations)
        {
            T result = getAnnotation(annoType, getCompositionAnnotation(each));
            if (result != null)
            {
                return result;
            }
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private <T extends Annotation> T getAnnotation(Class<T> type, CompositionAnnotation compositionAnnotation)
    {
        if (compositionAnnotation.getAnnotation().annotationType() == type)
        {
            return (T) compositionAnnotation.getAnnotation();
        }
        for (CompositionAnnotation each : compositionAnnotation.getCompositionAnnotations())
        {
            T annotation = getAnnotation(type, each);
            if (annotation != null)
            {
                return annotation;
            }
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private <T extends Annotation> void getAnnotations(Class<T> type, CompositionAnnotation compositionAnnotation, List<T> list)
    {
        if (compositionAnnotation.getAnnotation().annotationType() == type)
        {
            list.add((T) compositionAnnotation.getAnnotation());
        }
        for (CompositionAnnotation each : compositionAnnotation.getCompositionAnnotations())
        {
            getAnnotations(type, each, list);
        }
    }
    
    public <T extends Annotation> List<T> getAnnotations(Class<T> annoType, Field field)
    {
        return getAnnotations(annoType, field.getAnnotations());
    }
    
    public <T extends Annotation> List<T> getAnnotations(Class<T> annoType, Method method)
    {
        return getAnnotations(annoType, method.getAnnotations());
    }
    
    public <T extends Annotation> List<T> getAnnotations(Class<T> annoType, Class<?> ckass)
    {
        return getAnnotations(annoType, ckass.getAnnotations());
    }
    
    public <T extends Annotation> List<T> getAnnotations(Class<T> annoType, Annotation... annotations)
    {
        List<T> list = new ArrayList<T>();
        for (Annotation each : annotations)
        {
            getAnnotations(annoType, getCompositionAnnotation(each), list);
        }
        return list;
    }
    
    public <T extends Annotation> T getAnnotation(Class<T> annotationType, Class<?> target)
    {
        if (target.isAnnotation())
        {
            return null;
        }
        T result = target.getAnnotation(annotationType);
        if (result != null)
        {
            return result;
        }
        return getAnnotation(annotationType, target.getAnnotations());
    }
    
}
