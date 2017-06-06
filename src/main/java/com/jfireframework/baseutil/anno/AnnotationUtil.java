package com.jfireframework.baseutil.anno;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.exception.UnSupportException;
import com.jfireframework.baseutil.verify.Verify;

public class AnnotationUtil
{
    private final IdentityHashMap<Annotation, AnnotationTree> treeMap = new IdentityHashMap<Annotation, AnnotationTree>(256);
    
    public boolean isPresent(Class<? extends Annotation> annoType, Field field)
    {
        if (field.isAnnotationPresent(annoType))
        {
            return true;
        }
        if (field.getAnnotation(annoType) != null)
        {
            return true;
        }
        return isPresent(annoType, field.getAnnotations());
    }
    
    public boolean isPresent(Class<? extends Annotation> annoType, Annotation... annotations)
    {
        for (Annotation each : annotations)
        {
            AnnotationTree annotationTree = treeMap.get(each);
            if (annotationTree == null)
            {
                annotationTree = new AnnotationTreeImpl(each);
                treeMap.put(each, annotationTree);
            }
            if (annotationTree.isPresent(annoType))
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
    
    public <T extends Annotation> T getAnnotation(Class<T> annoType, Annotation[] annotations)
    {
        for (Annotation each : annotations)
        {
            AnnotationTree annotationTree = treeMap.get(each);
            if (annotationTree == null)
            {
                annotationTree = new AnnotationTreeImpl(each);
                treeMap.put(each, annotationTree);
            }
            if (annotationTree.isPresent(annoType))
            {
                return annotationTree.getAnnotation(annoType);
            }
        }
        return null;
    }
    
    public <T extends Annotation> T[] getAnnotations(Class<T> annoType, Field field)
    {
        return getAnnotations(annoType, field.getAnnotations());
    }
    
    public <T extends Annotation> T[] getAnnotations(Class<T> annoType, Method method)
    {
        return getAnnotations(annoType, method.getAnnotations());
    }
    
    public <T extends Annotation> T[] getAnnotations(Class<T> annoType, Class<?> ckass)
    {
        return getAnnotations(annoType, ckass.getAnnotations());
    }
    
    @SuppressWarnings("unchecked")
    public <T extends Annotation> T[] getAnnotations(Class<T> annoType, Annotation[] annotations)
    {
        List<T> contexts = new ArrayList<T>();
        for (Annotation each : annotations)
        {
            AnnotationTree annotationTree = treeMap.get(each);
            if (annotationTree == null)
            {
                annotationTree = new AnnotationTreeImpl(each);
                treeMap.put(each, annotationTree);
            }
            if (annotationTree.isPresent(annoType))
            {
                for (T annotation : annotationTree.getAnnotations(annoType))
                {
                    contexts.add(annotation);
                }
            }
        }
        T[] array = (T[]) Array.newInstance(annoType, contexts.size());
        return contexts.toArray(array);
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
    
    /**
     * 从annotations及其注解这些注解的所有注解中，寻找被annotation所注解的注解实例
     * 
     * @param <T>
     * 
     * @param annotation
     * @param annotations
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T getMetaAnnotation(Annotation annotation, Annotation... annotations)
    {
        for (Annotation each : annotations)
        {
            AnnotationTree candidate = treeMap.get(each);
            T t = (T) candidate.getMetaAnnotation(annotation);
            if (t != null)
            {
                return t;
            }
        }
        return null;
    }
    
    /**
     * 注解树。代表着一个根注解所展开的整个树信息
     * 
     * @author 林斌
     *
     */
    interface AnnotationTree
    {
        boolean isPresent(Class<? extends Annotation> type);
        
        <T extends Annotation> T getAnnotation(Class<T> type);
        
        <T extends Annotation> T[] getAnnotations(Class<T> type);
        
        /**
         * 返回被入参注解所注解的注解
         * 
         * @param annotation
         * @return
         */
        Annotation getMetaAnnotation(Annotation annotation);
    }
    
    class AnnotationTreeImpl implements AnnotationTree
    {
        /**
         * V 是被Annotation K 注解的Annotation
         */
        private IdentityHashMap<Annotation, Annotation> metaAnnotation = new IdentityHashMap<Annotation, Annotation>();
        private Set<Class<? extends Annotation>>        types               = new HashSet<Class<? extends Annotation>>();
        private Map<String, Object>                     aliasValue          = new HashMap<String, Object>();
        private Map<String, Object>                     extendValue         = new HashMap<String, Object>();
        private Set<Annotation>                         annotations         = new HashSet<Annotation>();
        private Map<Class<Annotation>, Annotation[]>    resultMap           = new HashMap<Class<Annotation>, Annotation[]>();
        private ClassLoader                             classLoader;
        
        @SuppressWarnings("unchecked")
        public AnnotationTreeImpl(Annotation annotation)
        {
            classLoader = annotation.annotationType().getClassLoader();
            resolveAliasValues(annotation);
            List<Annotation> tmp = new ArrayList<Annotation>();
            for (Class<?> annotationType : types)
            {
                tmp.clear();
                for (Annotation instance : annotations)
                {
                    if (instance.annotationType() == annotationType)
                    {
                        tmp.add(instance);
                    }
                }
                resultMap.put((Class<Annotation>) annotationType, tmp.toArray(new Annotation[tmp.size()]));
            }
        }
        
        private Annotation resolveAliasValues(Annotation annotation)
        {
            types.add(annotation.annotationType());
            Map<String, Object> valueMap = new HashMap<String, Object>();
            for (Method each : annotation.annotationType().getMethods())
            {
                try
                {
                    if (each.getParameterTypes().length != 0 //
                            || each.getDeclaringClass() == Annotation.class//
                            || Modifier.isPublic(each.getModifiers()) == false)
                    {
                        continue;
                    }
                    String name = each.getDeclaringClass().getName() + "." + each.getName();
                    if (aliasValue.containsKey(name))
                    {
                        valueMap.put(name, aliasValue.get(name));
                        continue;
                    }
                    else if (extendValue.containsKey(name))
                    {
                        Object value = each.invoke(annotation);
                        Object extend = extendValue.get(name);
                        int valueLength = Array.getLength(value);
                        int extendLength = Array.getLength(extend);
                        Object newArray = Array.newInstance(value.getClass().getComponentType(), valueLength + extendLength);
                        System.arraycopy(value, 0, newArray, 0, valueLength);
                        System.arraycopy(extend, 0, newArray, valueLength, extendLength);
                        valueMap.put(name, newArray);
                        continue;
                    }
                    valueMap.put(name, each.invoke(annotation));
                    if (each.isAnnotationPresent(AliasFor.class))
                    {
                        AliasFor aliasFor = each.getAnnotation(AliasFor.class);
                        Verify.True(annotation.annotationType().isAnnotationPresent(aliasFor.annotation()), "注解别名只能针对直接注解在本注解上的注解生效，请检查{}的属性:{}", annotation.annotationType().getName(), each.getName());
                        Method originAnnoMethod;
                        try
                        {
                            originAnnoMethod = aliasFor.annotation().getMethod(aliasFor.value());
                        }
                        catch (Exception e)
                        {
                            throw new UnSupportException(StringUtil.format("别名注解的属性不存在，请检查{}.{}中的别名是否拼写错误", each.getDeclaringClass().getName(), each.getName()), e);
                        }
                        name = aliasFor.annotation().getName() + "." + aliasFor.value();
                        if (aliasFor.isExtends() == false)
                        {
                            try
                            {
                                aliasValue.put(name, each.invoke(annotation));
                            }
                            catch (Exception e)
                            {
                                throw new JustThrowException(e);
                            }
                        }
                        else
                        {
                            if (each.getReturnType() != originAnnoMethod.getReturnType())
                            {
                                throw new UnSupportException(StringUtil.format("需要继承的属性与本注解属性不一致，请检查{}.{}", each.getDeclaringClass().getName(), each.getName()));
                            }
                            if (each.getReturnType().isArray() == false)
                            {
                                throw new UnSupportException(StringUtil.format("只有数组才能继承，请检查{}.{}", each.getDeclaringClass().getName(), each.getName()));
                            }
                            try
                            {
                                extendValue.put(name, each.invoke(annotation));
                            }
                            catch (Exception e)
                            {
                                throw new JustThrowException(e);
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    throw new JustThrowException(e);
                }
            }
            Annotation result = (Annotation) Proxy.newProxyInstance(classLoader, new Class<?>[] { annotation.annotationType() }, new aliasInvocationHandler(valueMap, annotation));
            annotations.add(result);
            for (Annotation anno : annotation.annotationType().getDeclaredAnnotations())
            {
                if (anno instanceof Documented || anno instanceof Target || anno instanceof Retention || anno instanceof Inherited)
                {
                    continue;
                }
                metaAnnotation.put(resolveAliasValues(anno), result);
            }
            return result;
        }
        
        class aliasInvocationHandler implements InvocationHandler
        {
            private final Map<String, Object> valueMap;
            private final Annotation          host;
            
            public aliasInvocationHandler(Map<String, Object> valueMap, Annotation host)
            {
                this.valueMap = valueMap;
                this.host = host;
            }
            
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
            {
                String name = method.getDeclaringClass().getName() + '.' + method.getName();
                Object value = valueMap.get(name);
                return value == null ? method.invoke(host, args) : value;
            }
        }
        
        public boolean isPresent(Class<? extends Annotation> type)
        {
            return types.contains(type);
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public <T extends Annotation> T getAnnotation(Class<T> type)
        {
            Annotation[] annotations = resultMap.get(type);
            return (T) (annotations != null ? annotations[0] : null);
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public <T extends Annotation> T[] getAnnotations(Class<T> type)
        {
            Annotation[] annotations = resultMap.get(type);
            if (annotations == null)
            {
                return null;
            }
            else
            {
                T[] array = (T[]) Array.newInstance(type, annotations.length);
                System.arraycopy(annotations, 0, array, 0, annotations.length);
                return array;
            }
        }
        
        @Override
        public Annotation getMetaAnnotation(Annotation annotation)
        {
            return metaAnnotation.get(annotation);
        }
        
    }
}
