package xin.nb1.baseutil.reflect;

import io.github.karlatemp.unsafeaccessor.Unsafe;
import lombok.SneakyThrows;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public final class ReflectUtil
{
    public static final Unsafe               UNSAFE                  = Unsafe.getUnsafe();
    public static final int                  PRIMITIVE_BYTE          = 1;
    public static final int                  PRIMITIVE_INT           = 2;
    public static final int                  PRIMITIVE_SHORT         = 3;
    public static final int                  PRIMITIVE_LONG          = 4;
    public static final int                  PRIMITIVE_FLOAT         = 5;
    public static final int                  PRIMITIVE_DOUBLE        = 6;
    public static final int                  PRIMITIVE_CHAR          = 7;
    public static final int                  PRIMITIVE_BOOL          = 8;
    public static final int                  PRIMITIVE_VOID          = 9;
    public static final int                  CLASS_BYTE              = 10;
    public static final int                  CLASS_INT               = 11;
    public static final int                  CLASS_SHORT             = 12;
    public static final int                  CLASS_LONG              = 13;
    public static final int                  CLASS_FLOAT             = 14;
    public static final int                  CLASS_DOUBLE            = 15;
    public static final int                  CLASS_CHAR              = 16;
    public static final int                  CLASS_BOOL              = 17;
    public static final int                  CLASS_VOID              = 18;
    public static final int                  CLASS_STRING            = 19;
    public static final int                  CLASS_BIGDECIMAL        = 20;
    public static final int                  CLASS_BLOB              = 21;
    public static final int                  CLASS_CLOB              = 22;
    public static final int                  CLASS_TIMESTAMP         = 23;
    public static final int                  CLASS_CALENDAR          = 24;
    public static final int                  CLASS_DATE              = 25;
    public static final int                  CLASS_SQL_DATE          = 26;
    public static final int                  CLASS_TIME              = 27;
    public static final int                  PRIMITIVE_BYTE_ARRAY    = 31;
    public static final int                  PRIMITIVE_INT_ARRAY     = 32;
    public static final int                  PRIMITIVE_SHORT_ARRAY   = 33;
    public static final int                  PRIMITIVE_LONG_ARRAY    = 34;
    public static final int                  PRIMITIVE_FLOAT_ARRAY   = 35;
    public static final int                  PRIMITIVE_DOUBLE_ARRAY  = 36;
    public static final int                  PRIMITIVE_CHAR_ARRAY    = 37;
    public static final int                  PRIMITIVE_BOOLEAN_ARRAY = 38;
    public static final int                  CLASS_ENUM              = 98;
    public static final int                  CLASS_OBJECT            = 99;
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
        else if (clazz == BigDecimal.class)
        {
            return CLASS_BIGDECIMAL;
        }
        else if (clazz == Blob.class)
        {
            return CLASS_BLOB;
        }
        else if (clazz == Clob.class)
        {
            return CLASS_CLOB;
        }
        else if (clazz == Timestamp.class)
        {
            return CLASS_TIMESTAMP;
        }
        else if (clazz == int[].class)
        {
            return PRIMITIVE_INT_ARRAY;
        }
        else if (clazz == byte[].class)
        {
            return PRIMITIVE_BYTE_ARRAY;
        }
        else if (clazz == short[].class)
        {
            return PRIMITIVE_SHORT_ARRAY;
        }
        else if (clazz == long[].class)
        {
            return PRIMITIVE_LONG_ARRAY;
        }
        else if (clazz == float[].class)
        {
            return PRIMITIVE_FLOAT_ARRAY;
        }
        else if (clazz == double[].class)
        {
            return PRIMITIVE_DOUBLE_ARRAY;
        }
        else if (clazz == char[].class)
        {
            return PRIMITIVE_CHAR_ARRAY;
        }
        else if (clazz == boolean[].class)
        {
            return PRIMITIVE_BOOLEAN_ARRAY;
        }
        else if (clazz == Calendar.class)
        {
            return CLASS_CALENDAR;
        }
        else if (clazz == Date.class)
        {
            return CLASS_DATE;
        }
        else if (clazz == java.sql.Date.class)
        {
            return CLASS_SQL_DATE;
        }
        else if (clazz == Time.class)
        {
            return CLASS_TIME;
        }
        else if (clazz.isEnum())
        {
            return CLASS_ENUM;
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

    public static boolean isNonBoxedObject(Class<?> clazz)
    {
        return getClassId(clazz) > CLASS_VOID;
    }

    public static boolean isNonBoxedObject(int classId)
    {
        return classId > CLASS_VOID;
    }

    public static String parseBeanGetMethodName(Field field)
    {
        if (field.getType() == boolean.class)
        {
            return "is" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
        }
        else
        {
            return "get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
        }
    }

    public static String parseBeanSetMethodName(Field field)
    {
        return "set" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
    }

    public static Field[] findPojoBeanSetFields(Class clazz)
    {
        List<Field> fields = new LinkedList<>();
        while (clazz != Object.class)
        {
            for (Field each : clazz.getDeclaredFields())
            {
                try
                {
                    clazz.getDeclaredMethod(parseBeanSetMethodName(each), each.getType());
                    fields.add(each);
                }
                catch (NoSuchMethodException e)
                {
                    ;
                }
            }
            clazz = clazz.getSuperclass();
        }
        return fields.toArray(new Field[0]);
    }

    public static Field[] findPojoBeanGetFields(Class clazz)
    {
        List<Field> fields = new LinkedList<>();
        while (clazz != Object.class)
        {
            for (Field each : clazz.getDeclaredFields())
            {
                try
                {
                    clazz.getDeclaredMethod(parseBeanGetMethodName(each));
                    fields.add(each);
                }
                catch (NoSuchMethodException e)
                {
                    ;
                }
            }
            clazz = clazz.getSuperclass();
        }
        return fields.toArray(new Field[0]);
    }

    @SneakyThrows
    public static Method getMethodWithoutCheck(Class<?> clazz, String methodName, Class<?>... parameterTypes)
    {
        return clazz.getDeclaredMethod(methodName, parameterTypes);
    }
}
