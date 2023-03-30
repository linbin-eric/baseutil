package com.jfirer.baseutil.reflect;

import io.github.karlatemp.unsafeaccessor.Unsafe;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public final class ReflectUtil
{
    private static final Unsafe UNSAFE = Unsafe.getUnsafe();

    public enum Primitive
    {
        INT,
        BOOL,
        BYTE,
        SHORT,
        LONG,
        CHAR,
        FLOAT,
        DOUBLE,
        STRING,
        UNKONW
    }

    public static Primitive ofPrimitive(Class<?> type)
    {
        switch (type.getName())
        {
            case "int", "java.lang.Integer" ->
            {
                return Primitive.INT;
            }
            case "boolean", "java.lang.Boolean" ->
            {
                return Primitive.BOOL;
            }
            case "byte", "java.lang.Byte" ->
            {
                return Primitive.BYTE;
            }
            case "short", "java.lang.Short" ->
            {
                return Primitive.SHORT;
            }
            case "long", "java.lang.Long" ->
            {
                return Primitive.LONG;
            }
            case "char", "java.lang.Character" ->
            {
                return Primitive.CHAR;
            }
            case "float", "java.lang.Float" ->
            {
                return Primitive.FLOAT;
            }
            case "double", "java.lang.Double" ->
            {
                return Primitive.DOUBLE;
            }
            case "java.lang.String" ->
            {
                return Primitive.STRING;
            }
            default ->
            {
                return Primitive.UNKONW;
            }
        }
    }

    public static Class<?> wrapPrimitive(Class<?> type)
    {
        if (type.isPrimitive() == false)
        {
            throw new IllegalArgumentException();
        }
        if (type == int.class)
        {
            return Integer.class;
        }
        else if (type == short.class)
        {
            return Short.class;
        }
        else if (type == long.class)
        {
            return Long.class;
        }
        else if (type == float.class)
        {
            return Float.class;
        }
        else if (type == double.class)
        {
            return Double.class;
        }
        else if (type == boolean.class)
        {
            return Boolean.class;
        }
        else if (type == byte.class)
        {
            return Byte.class;
        }
        else if (type == char.class)
        {
            return Character.class;
        }
        else
        {
            return null;
        }
    }

    public static void throwException(Throwable t)
    {
        if (t == null)
        {
            throw new NullPointerException("传入的参数为null");
        }
        else
        {
            UNSAFE.throwException(t);
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void throwException0(Throwable t) throws E
    {
        throw (E) t;
    }
}
