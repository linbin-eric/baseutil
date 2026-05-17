package cc.jfire.baseutil.reflect.type;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 这个类的核心目标是从一个明确的子类或者类型出发，不断向上解析整个链路的泛型信息。注意，只解析类继承路径上的泛型信息。
 * 通过方法：resolveTypeArguments 建立类型变量到类型的定义
 * 通过方法：resolveType 将定义的类型，特别是参数类型，将里面的实际参数明确出来。
 */
public class TypeResolver
{
    private final Map<TypeVariable<?>, Type> store = new HashMap<>();

    private void _init(Type source)
    {
        Type current = source;
        while (current != null)
        {
            Class<?> currentClass;
            if (current instanceof ParameterizedType pt)
            {
                currentClass = (Class<?>) pt.getRawType();
                TypeVariable<?>[] vars = currentClass.getTypeParameters();
                Type[]            args = pt.getActualTypeArguments();
                for (int i = 0; i < vars.length; i++)
                {
                    store.put(vars[i], args[i]);
                }
                for (Field declaredField : currentClass.getDeclaredFields())
                {
                    Type genericType = declaredField.getGenericType();
                    if (genericType instanceof ParameterizedType)
                    {
                        _init(genericType);
                    }
                }
            }
            else if (current instanceof Class<?> clazz)
            {
                currentClass = clazz;
            }
            else
            {
                break;
            }
            if (currentClass == Object.class)
            {
                break;
            }
            current = currentClass.getGenericSuperclass();
        }
    }

    public TypeResolver(Type source)
    {
        _init(source);
    }

    public Type resolveType(Type type)
    {
        if (type instanceof TypeVariable<?> tv)
        {
            Type target = store.get(tv);
            if (target == null)
            {
                return tv;
            }
            // 注意这里继续递归,因为出来的还有可能仍然是TypeVariable，通过递归的方式，最终明确掉。
            return resolveType(target);
        }
        if (type instanceof ParameterizedType pt)
        {
            Type[] args         = pt.getActualTypeArguments();
            Type[] resolvedArgs = new Type[args.length];
            for (int i = 0; i < args.length; i++)
            {
                resolvedArgs[i] = resolveType(args[i]);
            }
            return new ParameterizedTypeImplByJfire(pt.getOwnerType(), pt.getRawType(), resolvedArgs);
        }
        else if (type instanceof GenericArrayType gat)
        {
            Type genericComponentType = gat.getGenericComponentType();
            Type componentType        = resolveType(genericComponentType);
            if (componentType == genericComponentType)
            {
                return gat;
            }
            if (componentType instanceof Class<?> componentClass)
            {
                return Array.newInstance(componentClass, 0).getClass();
            }
            return new GenericArrayTypeImplByJfire(componentType);
        }
        else if (type instanceof WildcardType wildcardType)
        {
            Type[] lowerBounds = wildcardType.getLowerBounds();
            if (lowerBounds.length != 0)
            {
                return Object.class;
            }
            Type[] upperBounds = wildcardType.getUpperBounds();
            if (upperBounds.length == 0)
            {
                return Object.class;
            }
            return resolveType(upperBounds[0]);
        }
        else if (type instanceof Class<?>)
        {
            return type;
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }
}
