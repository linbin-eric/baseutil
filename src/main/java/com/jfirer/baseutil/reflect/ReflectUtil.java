package com.jfirer.baseutil.reflect;

import io.github.karlatemp.unsafeaccessor.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

public final class ReflectUtil
{
    private static final Unsafe               UNSAFE           = Unsafe.getUnsafe();
    public static final  int                  PRIMITIVE_INT    = 1;
    public static final  int                  PRIMITIVE_BOOL   = 2;
    public static final  int                  PRIMITIVE_BYTE   = 3;
    public static final  int                  PRIMITIVE_SHORT  = 4;
    public static final  int                  PRIMITIVE_LONG   = 5;
    public static final  int                  PRIMITIVE_CHAR   = 6;
    public static final  int                  PRIMITIVE_FLOAT  = 7;
    public static final  int                  PRIMITIVE_DOUBLE = 8;
    public static final  int                  PRIMITIVE_VOID   = 9;
    public static final  int                  CLASS_INT        = 10;
    public static final  int                  CLASS_BOOL       = 11;
    public static final  int                  CLASS_BYTE       = 12;
    public static final  int                  CLASS_SHORT      = 13;
    public static final  int                  CLASS_LONG       = 14;
    public static final  int                  CLASS_CHAR       = 15;
    public static final  int                  CLASS_FLOAT      = 16;
    public static final  int                  CLASS_DOUBLE     = 17;
    public static final  int                  CLASS_STRING     = 18;
    public static final  int                  CLASS_OBJECT     = 19;
    public static final  int                  CLASS_VOID       = 19;
    public static final  MethodHandles.Lookup TRUSTED_LOOKUP;

    static
    {
        long  fieldOffset = 0;
        Class lookupClass = MethodHandles.Lookup.class;
        try
        {
            Field implLookup = lookupClass.getDeclaredField("IMPL_LOOKUP");
            fieldOffset = UNSAFE.staticFieldOffset(implLookup);
        }
        catch (Throwable e)
        {
            ReflectUtil.throwException(e);
        }
        TRUSTED_LOOKUP = (MethodHandles.Lookup) UNSAFE.getReference(lookupClass, fieldOffset);
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

    public static boolean isPrimitive(Class clazz)
    {
        int classId = getClassId(clazz);
        return classId <= 8;
    }

    public static boolean isPrimitiveBox(Class clazz)
    {
        int classId = getClassId(clazz);
        return classId >= 10 && classId <= 17;
    }

    public static int getClassId(Class clazz)
    {
        if (clazz == int.class)
        {
            return PRIMITIVE_INT;
        }
        else if (clazz == boolean.class)
        {
            return PRIMITIVE_BOOL;
        }
        else if (clazz == byte.class)
        {
            return PRIMITIVE_BYTE;
        }
        else if (clazz == short.class)
        {
            return PRIMITIVE_SHORT;
        }
        else if (clazz == long.class)
        {
            return PRIMITIVE_LONG;
        }
        else if (clazz == char.class)
        {
            return PRIMITIVE_CHAR;
        }
        else if (clazz == float.class)
        {
            return PRIMITIVE_FLOAT;
        }
        else if (clazz == double.class)
        {
            return PRIMITIVE_DOUBLE;
        }
        else if (clazz == void.class)
        {
            return PRIMITIVE_VOID;
        }
        else if (clazz == Integer.class)
        {
            return CLASS_INT;
        }
        else if (clazz == Boolean.class)
        {
            return CLASS_BOOL;
        }
        else if (clazz == Byte.class)
        {
            return CLASS_BYTE;
        }
        else if (clazz == Short.class)
        {
            return CLASS_SHORT;
        }
        else if (clazz == Long.class)
        {
            return CLASS_LONG;
        }
        else if (clazz == Character.class)
        {
            return CLASS_CHAR;
        }
        else if (clazz == Float.class)
        {
            return CLASS_FLOAT;
        }
        else if (clazz == Double.class)
        {
            return CLASS_DOUBLE;
        }
        else if (clazz == String.class)
        {
            return CLASS_STRING;
        }
        else if (clazz == Void.class)
        {
            return CLASS_VOID;
        }
        else
        {
            return CLASS_OBJECT;
        }
    }
}
