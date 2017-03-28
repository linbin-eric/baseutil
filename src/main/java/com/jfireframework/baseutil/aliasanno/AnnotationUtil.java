package com.jfireframework.baseutil.aliasanno;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.exception.UnSupportException;

public class AnnotationUtil
{
    private static final Map<Annotation, AnnoContext> aliasMap = new ConcurrentHashMap<Annotation, AnnoContext>(256);
    
    public static boolean isPresent(Class<? extends Annotation> annoType, Field field)
    {
        if (field.isAnnotationPresent(annoType))
        {
            return true;
        }
        return getAnnotation(annoType, field) != null;
    }
    
    public static boolean isPresent(Class<? extends Annotation> annoType, Class<?> target)
    {
        if (target.isAnnotationPresent(annoType) && target.isAnnotation() == false)
        {
            return true;
        }
        return getAnnotation(annoType, target) != null;
    }
    
    public static boolean isPresent(Class<? extends Annotation> annoType, Method method)
    {
        if (method.isAnnotationPresent(annoType))
        {
            return true;
        }
        return getAnnotation(annoType, method) != null;
    }
    
    public static <T extends Annotation> T getAnnotation(Class<T> annoType, Method method)
    {
        T anno = null;
        anno = method.getAnnotation(annoType);
        if (anno != null)
        {
            return anno;
        }
        return getAnnotation(annoType, method.getAnnotations());
    }
    
    public static <T extends Annotation> T getAnnotation(Class<T> annoType, Field field)
    {
        T anno = null;
        anno = field.getAnnotation(annoType);
        if (anno != null)
        {
            return anno;
        }
        return getAnnotation(annoType, field.getAnnotations());
    }
    
    private static <T extends Annotation> T getAnnotation(Class<T> annoType, Annotation[] annotations)
    {
        for (Annotation each : annotations)
        {
            AnnoContext annoContext = aliasMap.get(each);
            if (annoContext == null)
            {
                annoContext = new AnnoContext(each);
                aliasMap.put(each, annoContext);
            }
            if (annoContext.isPresent(annoType))
            {
                return annoContext.getAnno(annoType);
            }
        }
        return null;
    }
    
    public static <T extends Annotation> T getAnnotation(Class<T> annotationType, Class<?> target)
    {
        if (target.isAnnotation())
        {
            return null;
        }
        T anno;
        anno = target.getAnnotation(annotationType);
        if (anno != null)
        {
            return anno;
        }
        return getAnnotation(annotationType, target.getAnnotations());
    }
    
    static class AnnoContext
    {
        private final Map<String, ValueProxy>          valueMap = new HashMap<String, ValueProxy>();
        private final Set<Class<? extends Annotation>> types    = new HashSet<Class<? extends Annotation>>();
        private final ClassLoader                      classLoader;
        
        enum schema
        {
            alias, extendsfor, origin
        }
        
        class ValueProxy
        {
            private final Object value;
            private final schema schema;
            
            public ValueProxy(Object value, schema schema)
            {
                this.value = value;
                this.schema = schema;
            }
        }
        
        public AnnoContext(Annotation annotation)
        {
            classLoader = annotation.annotationType().getClassLoader();
            resolveAliasValues(annotation);
        }
        
        private void resolveAliasValues(Annotation annotation)
        {
            types.add(annotation.annotationType());
            for (Method each : annotation.annotationType().getMethods())
            {
                if (each.getParameterTypes().length != 0 || each.getDeclaringClass() == Annotation.class)
                {
                    continue;
                }
                String name = null;
                Object value = null;
                ValueProxy valueProxy;
                if (each.isAnnotationPresent(AliasFor.class))
                {
                    AliasFor aliasFor = each.getAnnotation(AliasFor.class);
                    try
                    {
                        name = aliasFor.annotation().getName() + "." + aliasFor.annotation().getMethod(aliasFor.value()).getName();
                    }
                    catch (Exception e)
                    {
                        throw new UnSupportException(StringUtil.format("别名注解的属性不存在，请检查{}.{}中的别名是否拼写错误", each.getDeclaringClass().getName(), each.getName()), e);
                    }
                    try
                    {
                        value = each.invoke(annotation);
                    }
                    catch (Exception e)
                    {
                        throw new JustThrowException(e);
                    }
                    valueProxy = new ValueProxy(value, schema.alias);
                }
                else if (each.isAnnotationPresent(ExtendsFor.class))
                {
                    ExtendsFor extendsFor = each.getAnnotation(ExtendsFor.class);
                    try
                    {
                        if (each.getReturnType() != extendsFor.annotation().getMethod(extendsFor.value()).getReturnType())
                        {
                            throw new UnSupportException(StringUtil.format("需要继承的属性与本注解属性不一致，请检查{}.{}", each.getDeclaringClass().getName(), each.getName()));
                        }
                        if (each.getReturnType().isArray() == false)
                        {
                            throw new UnSupportException(StringUtil.format("只有数组才能继承，请检查{}.{}", each.getDeclaringClass().getName(), each.getName()));
                        }
                        name = extendsFor.annotation().getName() + "." + extendsFor.annotation().getMethod(extendsFor.value()).getName();
                    }
                    catch (Exception e)
                    {
                        throw new UnSupportException(StringUtil.format("别名注解的属性不存在，请检查{}.{}中的别名是否拼写错误", each.getDeclaringClass().getName(), each.getName()), e);
                    }
                    try
                    {
                        value = each.invoke(annotation);
                    }
                    catch (Exception e)
                    {
                        throw new JustThrowException(e);
                    }
                    valueProxy = new ValueProxy(value, schema.extendsfor);
                }
                else
                {
                    name = each.getDeclaringClass().getName() + '.' + each.getName();
                    try
                    {
                        value = each.invoke(annotation);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        throw new JustThrowException(e);
                    }
                    valueProxy = new ValueProxy(value, schema.origin);
                }
                String originName = each.getDeclaringClass().getName() + '.' + each.getName();
                // if判断为真，意味着该属性的值被更上层的注解指定过
                if (valueMap.containsKey(originName))
                {
                    ValueProxy pred = valueMap.get(originName);
                    switch (pred.schema)
                    {
                        case origin:
                            // 在属性值已经被上层注解注定的情况下，上层的注解的类型肯定不是origin
                            throw new UnSupportException("程序逻辑异常，代码不应该可以走到这个地方");
                        case alias:
                            valueProxy = new ValueProxy(pred.value, valueProxy.schema);
                            valueMap.put(name, valueProxy);
                            break;
                        case extendsfor:
                            Class<?> predType = pred.value.getClass();
                            int predArrayLength = Array.getLength(pred.value);
                            int arrayLength = Array.getLength(valueProxy.value);
                            Object newArray = Array.newInstance(predType.getComponentType(), predArrayLength + arrayLength);
                            System.arraycopy(valueProxy.value, 0, newArray, 0, arrayLength);
                            System.arraycopy(pred.value, 0, newArray, arrayLength, predArrayLength);
                            valueProxy = new ValueProxy(newArray, schema.origin);
                            valueMap.put(name, valueProxy);
                            break;
                        default:
                            break;
                    }
                }
                // 出现这种情况意味着本属性要指定的值已经被上层的注解指定，此时应该忽略
                else if (valueMap.containsKey(name))
                {
                }
                else
                {
                    valueMap.put(name, valueProxy);
                }
                
            }
            for (Annotation anno : annotation.annotationType().getDeclaredAnnotations())
            {
                if (anno instanceof Documented || anno instanceof Target || anno instanceof Retention || anno instanceof Inherited)
                {
                    continue;
                }
                resolveAliasValues(anno);
            }
        }
        
        public boolean isPresent(Class<? extends Annotation> type)
        {
            return types.contains(type);
        }
        
        @SuppressWarnings("unchecked")
        public <T extends Annotation> T getAnno(Class<T> type)
        {
            return (T) Proxy.newProxyInstance(classLoader, new Class<?>[] { type }, new aliasInvocationHandler());
        }
        
        class aliasInvocationHandler implements InvocationHandler
        {
            
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
            {
                String name = method.getDeclaringClass().getName() + '.' + method.getName();
                return valueMap.get(name).value;
            }
        }
    }
    
}
