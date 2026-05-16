package cc.jfire.baseutil.reflect;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 这个类的核心目标是从一个明确的子类或者类型出发，不断向上解析整个链路的泛型信息。注意，只解析类继承路径上的泛型信息。
 * 通过方法：resolveTypeArguments 建立类型变量到类型的定义
 * 通过方法：resolveType 将定义的类型，特别是参数类型，将里面的实际参数明确出来。
 */
public class TypeResolver
{
    public static ConcurrentMap<TypeVariable<?>, Type> resolveTypeArguments(Type sourceType)
    {
        ConcurrentMap<TypeVariable<?>, Type> resolved = new ConcurrentHashMap<>();
        Type                                 current  = sourceType;
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
                    resolved.put(vars[i], args[i]);
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
        return resolved;
    }

    public static Type resolveType(Type type, Map<TypeVariable<?>, Type> resolved)
    {
        if (type instanceof TypeVariable<?> tv)
        {
            Type target = resolved.get(tv);
            if (target == null)
            {
                return tv;
            }
            // 注意这里继续递归,因为出来的还有可能仍然是TypeVariable，通过递归的方式，最终明确掉。
            return resolveType(target, resolved);
        }
        if (type instanceof ParameterizedType pt)
        {
            Type[]  args         = pt.getActualTypeArguments();
            Type[]  resolvedArgs = new Type[args.length];
            boolean changed      = false;
            for (int i = 0; i < args.length; i++)
            {
                resolvedArgs[i] = resolveType(args[i], resolved);
                if (resolvedArgs[i] != args[i])
                {
                    changed = true;
                }
            }
            Type ownerType         = pt.getOwnerType();
            Type resolvedOwnerType = ownerType == null ? null : resolveType(ownerType, resolved);
            if (resolvedOwnerType != ownerType)
            {
                changed = true;
            }
            if (changed == false)
            {
                return pt;
            }
            return new ParameterizedTypeImpl(resolvedOwnerType, pt.getRawType(), resolvedArgs);
        }
        else if (type instanceof GenericArrayType gat)
        {
            Type genericComponentType = gat.getGenericComponentType();
            Type componentType        = resolveType(genericComponentType, resolved);
            if (componentType == genericComponentType)
            {
                return gat;
            }
            if (componentType instanceof Class<?> componentClass)
            {
                return Array.newInstance(componentClass, 0).getClass();
            }
            return new GenericArrayTypeImpl(componentType);
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
            return resolveType(upperBounds[0], resolved);
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

    static class ParameterizedTypeImpl implements ParameterizedType
    {
        private final Type   ownerType;
        private final Type   rawType;
        private final Type[] actualTypeArguments;

        ParameterizedTypeImpl(Type ownerType, Type rawType, Type[] actualTypeArguments)
        {
            this.ownerType           = ownerType;
            this.rawType             = rawType;
            this.actualTypeArguments = actualTypeArguments.clone();
        }

        @Override
        public Type[] getActualTypeArguments()
        {
            return actualTypeArguments.clone();
        }

        @Override
        public Type getRawType()
        {
            return rawType;
        }

        @Override
        public Type getOwnerType()
        {
            return ownerType;
        }

        @Override
        public boolean equals(Object other)
        {
            return other instanceof ParameterizedType that && Objects.equals(ownerType, that.getOwnerType()) && Objects.equals(rawType, that.getRawType()) && Arrays.equals(actualTypeArguments, that.getActualTypeArguments());
        }

        @Override
        public int hashCode()
        {
            return Arrays.hashCode(actualTypeArguments) ^ Objects.hashCode(ownerType) ^ Objects.hashCode(rawType);
        }

        @Override
        public String toString()
        {
            StringBuilder builder = new StringBuilder(rawType.getTypeName());
            if (actualTypeArguments.length != 0)
            {
                builder.append("<");
                for (int i = 0; i < actualTypeArguments.length; i++)
                {
                    if (i != 0)
                    {
                        builder.append(", ");
                    }
                    builder.append(actualTypeArguments[i].getTypeName());
                }
                builder.append(">");
            }
            return builder.toString();
        }
    }

    static class GenericArrayTypeImpl implements GenericArrayType
    {
        private final Type genericComponentType;

        GenericArrayTypeImpl(Type genericComponentType)
        {
            this.genericComponentType = genericComponentType;
        }

        @Override
        public Type getGenericComponentType()
        {
            return genericComponentType;
        }

        @Override
        public boolean equals(Object other)
        {
            return other instanceof GenericArrayType that && genericComponentType.equals(that.getGenericComponentType());
        }

        @Override
        public int hashCode()
        {
            return genericComponentType.hashCode();
        }

        @Override
        public String toString()
        {
            return genericComponentType.getTypeName() + "[]";
        }
    }
}
