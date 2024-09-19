package com.jfirer.baseutil.reflect;

import io.github.karlatemp.unsafeaccessor.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

public final class ReflectUtil
{
    public static final Unsafe               UNSAFE           = Unsafe.getUnsafe();
    public static final int                  PRIMITIVE_BYTE   = 1;
    public static final int                  PRIMITIVE_INT    = 2;
    public static final int                  PRIMITIVE_SHORT  = 3;
    public static final int                  PRIMITIVE_LONG   = 4;
    public static final int                  PRIMITIVE_FLOAT  = 5;
    public static final int                  PRIMITIVE_DOUBLE = 6;
    public static final int                  PRIMITIVE_CHAR   = 7;
    public static final int                  PRIMITIVE_BOOL   = 8;
    public static final int                  PRIMITIVE_VOID   = 9;
    public static final int                  CLASS_BYTE       = 10;
    public static final int                  CLASS_INT        = 11;
    public static final int                  CLASS_SHORT      = 12;
    public static final int                  CLASS_LONG       = 13;
    public static final int                  CLASS_FLOAT      = 14;
    public static final int                  CLASS_DOUBLE     = 15;
    public static final int                  CLASS_CHAR       = 16;
    public static final int                  CLASS_BOOL       = 17;
    public static final int                  CLASS_VOID       = 18;
    public static final int                  CLASS_STRING     = 19;
    public static final int                  CLASS_BIGDECIMAL = 20;
    public static final int                  CLASS_OBJECT     = 99;
    public static final MethodHandles.Lookup TRUSTED_LOOKUP;

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

    public static Class getBoxedTypeOrOrigin(Class clazz)
    {
        if (clazz == int.class)
        {
            return Integer.class;
        }
        else if (clazz == boolean.class)
        {
            return Boolean.class;
        }
        else if (clazz == byte.class)
        {
            return Byte.class;
        }
        else if (clazz == short.class)
        {
            return Short.class;
        }
        else if (clazz == long.class)
        {
            return Long.class;
        }
        else if (clazz == char.class)
        {
            return Character.class;
        }
        else if (clazz == float.class)
        {
            return Float.class;
        }
        else if (clazz == double.class)
        {
            return Double.class;
        }
        else if (clazz == void.class)
        {
            return Void.class;
        }
        else
        {
            return clazz;
        }
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

    public static boolean isNumber(Class<?> ckass)
    {
        int classId = getClassId(ckass);
        return (classId >= PRIMITIVE_BYTE && classId <= PRIMITIVE_DOUBLE) || (classId >= CLASS_BYTE && classId <= CLASS_DOUBLE);
    }

    public static boolean isNumber(int classId)
    {
        return (classId >= PRIMITIVE_BYTE && classId <= PRIMITIVE_DOUBLE) || (classId >= CLASS_BYTE && classId <= CLASS_DOUBLE);
    }

    public static boolean isNumberOrBigDecimal(Class<?> clazz)
    {
        int classId = getClassId(clazz);
        return (classId >= PRIMITIVE_BYTE && classId <= PRIMITIVE_DOUBLE) || (classId >= CLASS_BYTE && classId <= CLASS_DOUBLE) || classId == CLASS_BIGDECIMAL;
    }

    public static boolean isNumberOrBigDecimal(int classId)
    {
        return (classId >= PRIMITIVE_BYTE && classId <= PRIMITIVE_DOUBLE) || (classId >= CLASS_BYTE && classId <= CLASS_DOUBLE) || classId == CLASS_BIGDECIMAL;
    }

    public static boolean isBooleanOrBooleanBox(Class<?> ckass)
    {
        int classId = getClassId(ckass);
        return classId == PRIMITIVE_BOOL || classId == CLASS_BOOL;
    }

    public static boolean isCharOrCharBox(Class<?> ckass)
    {
        int classId = getClassId(ckass);
        return classId == PRIMITIVE_CHAR || classId == CLASS_CHAR;
    }
}
