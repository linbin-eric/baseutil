package com.jfireframework.baseutil.reflect;

import com.jfireframework.baseutil.smc.SmcHelper;
import com.jfireframework.baseutil.smc.compiler.CompileHelper;
import com.jfireframework.baseutil.smc.model.ClassModel;
import com.jfireframework.baseutil.smc.model.MethodModel;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

public class ValueAccessor
{
    private static final int           _UNSAFE       = 0;
    private static final int           _FIELD        = 1;
    private static final int           INT           = 1;
    private static final int           BYTE          = 2;
    private static final int           CHAR          = 3;
    private static final int           BOOLEAN       = 4;
    private static final int           SHORT         = 5;
    private static final int           LONG          = 6;
    private static final int           FLOAT         = 7;
    private static final int           DOUBLE        = 8;
    private final        int           accessType;
    private              Field         field;
    private              long          offset;
    private              boolean       primitive;
    private              int           primitiveType = 0;
    private static final AtomicInteger count         = new AtomicInteger();

    static String toMethodName(Field field)
    {
        return field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
    }

    public ValueAccessor()
    {
        accessType = Integer.MIN_VALUE;
    }

    public static ValueAccessor create(Field field, CompileHelper compileHelper)
    {
        ClassModel classModel = new ClassModel("ValueAccessor_" + count.getAndIncrement(), ValueAccessor.class);
        Class<?>   type       = field.getType();
        if (type == int.class || type == Integer.class)
        {
            return build(field, compileHelper, classModel, "getInt", int.class, Integer.class);
        }
        else if (type == short.class || type == Short.class)
        {
            return build(field, compileHelper, classModel, "getShort", short.class, Short.class);
        }
        else if (type == long.class || type == Long.class)
        {
            return build(field, compileHelper, classModel, "getLong", long.class, Long.class);
        }
        else if (type == float.class || type == Float.class)
        {
            return build(field, compileHelper, classModel, "getFloat", float.class, Float.class);
        }
        else if (type == double.class || type == Double.class)
        {
            return build(field, compileHelper, classModel, "getDouble", double.class, Double.class);
        }
        else if (type == boolean.class || type == Boolean.class)
        {
            return build(field, compileHelper, classModel, "getBoolean", boolean.class, Boolean.class);
        }
        else if (type == byte.class || type == Byte.class)
        {
            return build(field, compileHelper, classModel, "getByte", byte.class, Byte.class);
        }
        else if (type == char.class || type == Character.class)
        {
            return build(field, compileHelper, classModel, "getChar", char.class, Character.class);
        }
        else
        {
            try
            {
                Method      method      = ValueAccessor.class.getDeclaredMethod("get", Object.class);
                MethodModel methodModel = new MethodModel(method, classModel);
                methodModel.setBody("return ((" + SmcHelper.getReferenceName(field.getDeclaringClass(), classModel) + ")$0).get" + toMethodName(field) + "();");
                classModel.putMethodModel(methodModel);
                method = ValueAccessor.class.getDeclaredMethod("setObject", Object.class, Object.class);
                methodModel = new MethodModel(method, classModel);
                methodModel.setBody("((" + SmcHelper.getReferenceName(field.getDeclaringClass(), classModel) + ")$0).set" + toMethodName(field) + "((" + SmcHelper.getReferenceName(field.getType(), classModel) + ")$1);");
                classModel.putMethodModel(methodModel);
                return (ValueAccessor) compileHelper.compile(classModel).newInstance();
            }
            catch (Exception e)
            {
                ReflectUtil.throwException(e);
                return null;
            }
        }
    }

    private static ValueAccessor build(Field field, CompileHelper compileHelper, ClassModel classModel, String get, Class<?> C1, Class<?> C2)
    {
        try
        {
            Method      method      = ValueAccessor.class.getDeclaredMethod(get, Object.class);
            MethodModel methodModel = new MethodModel(method, classModel);
            if (field.getType() != boolean.class)
            {
                methodModel.setBody("return ((" + SmcHelper.getReferenceName(field.getDeclaringClass(), classModel) + ")$0).get" + toMethodName(field) + "();");
            }
            else
            {
                methodModel.setBody("return ((" + SmcHelper.getReferenceName(field.getDeclaringClass(), classModel) + ")$0).is" + toMethodName(field) + "();");
            }
            classModel.putMethodModel(methodModel);
            Method getIntObject = ValueAccessor.class.getDeclaredMethod(get + "Object", Object.class);
            methodModel = new MethodModel(getIntObject, classModel);
            if (field.getType() != boolean.class)
            {
                methodModel.setBody("return ((" + SmcHelper.getReferenceName(field.getDeclaringClass(), classModel) + ")$0).get" + toMethodName(field) + "();");
            }
            else
            {
                methodModel.setBody("return ((" + SmcHelper.getReferenceName(field.getDeclaringClass(), classModel) + ")$0).is" + toMethodName(field) + "();");
            }
            classModel.putMethodModel(methodModel);
            method = ValueAccessor.class.getDeclaredMethod("set", Object.class, C1);
            methodModel = new MethodModel(method, classModel);
            methodModel.setBody("((" + SmcHelper.getReferenceName(field.getDeclaringClass(), classModel) + ")$0).set" + toMethodName(field) + "($1);");
            classModel.putMethodModel(methodModel);
            method = ValueAccessor.class.getDeclaredMethod("set", Object.class, C2);
            methodModel = new MethodModel(method, classModel);
            methodModel.setBody("((" + SmcHelper.getReferenceName(field.getDeclaringClass(), classModel) + ")$0).set" + toMethodName(field) + "($1);");
            classModel.putMethodModel(methodModel);
            return (ValueAccessor) compileHelper.compile(classModel).newInstance();
        }
        catch (Exception e)
        {
            ReflectUtil.throwException(e);
        }
        return null;
    }

    public interface ObjectValue
    {
        Object get(Object entity) throws IllegalAccessException;

        void set(Object entity, Object value) throws IllegalAccessException;
    }

    public interface BooleanValue
    {
        boolean get(Object entity) throws IllegalAccessException;

        Boolean getObject(Object entity) throws IllegalAccessException;

        void set(Object entity, boolean value) throws IllegalAccessException;

        void set(Object entity, Boolean value) throws IllegalAccessException;
    }

    public interface DoubleValue
    {
        double get(Object entity) throws IllegalAccessException;

        Double getObject(Object entity) throws IllegalAccessException;

        void set(Object entity, double value) throws IllegalAccessException;

        void set(Object entity, Double value) throws IllegalAccessException;
    }

    public interface IntValue
    {
        int get(Object entity) throws IllegalAccessException;

        Integer getObject(Object entity) throws IllegalAccessException;

        void set(Object entity, int value) throws IllegalAccessException;

        void set(Object entity, Integer value) throws IllegalAccessException;
    }

    public interface CharValue
    {
        char get(Object entity) throws IllegalAccessException;

        Character getObject(Object entity) throws IllegalAccessException;

        void set(Object entity, char value) throws IllegalAccessException;

        void set(Object entity, Character value) throws IllegalAccessException;
    }

    public interface FloatValue
    {
        float get(Object entity) throws IllegalAccessException;

        Float getObject(Object entity) throws IllegalAccessException;

        void set(Object entity, float value) throws IllegalAccessException;

        void set(Object entity, Float value) throws IllegalAccessException;
    }

    public interface LongValue
    {
        long get(Object entity) throws IllegalAccessException;

        Long getObject(Object entity) throws IllegalAccessException;

        void set(Object entity, long value) throws IllegalAccessException;

        void set(Object entity, Long value) throws IllegalAccessException;
    }

    public interface ShortValue
    {
        short get(Object entity) throws IllegalAccessException;

        Short getObject(Object entity) throws IllegalAccessException;

        void set(Object entity, short value) throws IllegalAccessException;

        void set(Object entity, Short value) throws IllegalAccessException;
    }

    public interface ByteValue
    {
        byte get(Object entity) throws IllegalAccessException;

        Byte getObject(Object entity) throws IllegalAccessException;

        void set(Object entity, byte value) throws IllegalAccessException;

        void set(Object entity, Byte value) throws IllegalAccessException;
    }

    class UnsafeObjectValue implements ObjectValue
    {

        @Override
        public Object get(Object entity)
        {
            return UNSAFE.getObject(entity, offset);
        }

        @Override
        public void set(Object entity, Object value)
        {
            UNSAFE.putObject(entity, offset, value);
        }
    }

    class FieldObjectValue implements ObjectValue
    {

        @Override
        public Object get(Object entity) throws IllegalAccessException
        {
            return field.get(entity);
        }

        @Override
        public void set(Object entity, Object value) throws IllegalAccessException
        {
            field.set(entity, value);
        }
    }

    class FieldBooleanValue implements BooleanValue
    {

        @Override
        public boolean get(Object entity) throws IllegalAccessException
        {
            if (primitive)
            {
                return field.getBoolean(entity);
            }
            else
            {
                return ((Boolean) field.get(entity));
            }
        }

        @Override
        public Boolean getObject(Object entity) throws IllegalAccessException
        {
            if (primitive)
            {
                return field.getBoolean(entity);
            }
            else
            {
                return ((Boolean) field.get(entity));
            }
        }

        @Override
        public void set(Object entity, boolean value) throws IllegalAccessException
        {
            if (primitive)
            {
                field.setBoolean(entity, value);
            }
            else
            {
                field.set(entity, value);
            }
        }

        @Override
        public void set(Object entity, Boolean value) throws IllegalAccessException
        {
            if (primitive)
            {
                field.setBoolean(entity, value);
            }
            else
            {
                field.set(entity, value);
            }
        }
    }

    class UnsafeBooleanValue implements BooleanValue
    {

        @Override
        public boolean get(Object entity)
        {
            if (primitive)
            {
                return UNSAFE.getBoolean(entity, offset);
            }
            else
            {
                return ((Boolean) UNSAFE.getObject(entity, offset));
            }
        }

        @Override
        public Boolean getObject(Object entity)
        {
            if (primitive)
            {
                return UNSAFE.getBoolean(entity, offset);
            }
            else
            {
                return ((Boolean) UNSAFE.getObject(entity, offset));
            }
        }

        @Override
        public void set(Object entity, boolean value)
        {
            if (primitive)
            {
                UNSAFE.putBoolean(entity, offset, value);
            }
            else
            {
                UNSAFE.putObject(entity, offset, value);
            }
        }

        @Override
        public void set(Object entity, Boolean value)
        {
            if (primitive)
            {
                UNSAFE.putBoolean(entity, offset, value);
            }
            else
            {
                UNSAFE.putObject(entity, offset, value);
            }
        }
    }

    class FieldIntValue implements IntValue
    {

        @Override
        public int get(Object entity) throws IllegalAccessException
        {
            if (primitive)
            {
                return field.getInt(entity);
            }
            else
            {
                return (Integer) field.get(entity);
            }
        }

        @Override
        public Integer getObject(Object entity) throws IllegalAccessException
        {
            if (primitive)
            {
                return field.getInt(entity);
            }
            else
            {
                return (Integer) field.get(entity);
            }
        }

        @Override
        public void set(Object entity, int value) throws IllegalAccessException
        {
            if (primitive)
            {
                field.setInt(entity, value);
            }
            else
            {
                field.set(entity, value);
            }
        }

        @Override
        public void set(Object entity, Integer value) throws IllegalAccessException
        {
            if (primitive)
            {
                field.setInt(entity, value);
            }
            else
            {
                field.set(entity, value);
            }
        }
    }

    class UnsafeIntValue implements IntValue
    {

        @Override
        public int get(Object entity)
        {
            if (primitive)
            {
                return UNSAFE.getInt(entity, offset);
            }
            else
            {
                return ((Integer) UNSAFE.getObject(entity, offset));
            }
        }

        @Override
        public Integer getObject(Object entity)
        {
            if (primitive)
            {
                return UNSAFE.getInt(entity, offset);
            }
            else
            {
                return ((Integer) UNSAFE.getObject(entity, offset));
            }
        }

        @Override
        public void set(Object entity, int value)
        {
            if (primitive)
            {
                UNSAFE.putInt(entity, offset, value);
            }
            else
            {
                UNSAFE.putObject(entity, offset, Integer.valueOf(value));
            }
        }

        @Override
        public void set(Object entity, Integer value)
        {
            if (primitive)
            {
                UNSAFE.putInt(entity, offset, value);
            }
            else
            {
                UNSAFE.putObject(entity, offset, value);
            }
        }
    }

    class FieldByteValue implements ByteValue
    {

        @Override
        public byte get(Object entity) throws IllegalAccessException
        {
            if (primitive)
            {
                return field.getByte(entity);
            }
            else
            {
                return ((Byte) field.get(entity));
            }
        }

        @Override
        public Byte getObject(Object entity) throws IllegalAccessException
        {
            if (primitive)
            {
                return field.getByte(entity);
            }
            else
            {
                return ((Byte) field.get(entity));
            }
        }

        @Override
        public void set(Object entity, byte value) throws IllegalAccessException
        {
            if (primitive)
            {
                field.setByte(entity, value);
            }
            else
            {
                field.set(entity, value);
            }
        }

        @Override
        public void set(Object entity, Byte value) throws IllegalAccessException
        {
            if (primitive)
            {
                field.setByte(entity, value);
            }
            else
            {
                field.set(entity, value);
            }
        }
    }

    class UnsafeByteValue implements ByteValue
    {

        @Override
        public byte get(Object entity)
        {
            if (primitive)
            {
                return UNSAFE.getByte(entity, offset);
            }
            else
            {
                return ((Byte) UNSAFE.getObject(entity, offset)).byteValue();
            }
        }

        @Override
        public Byte getObject(Object entity)
        {
            if (primitive)
            {
                return UNSAFE.getByte(entity, offset);
            }
            else
            {
                return ((Byte) UNSAFE.getObject(entity, offset));
            }
        }

        @Override
        public void set(Object entity, byte value)
        {
            if (primitive)
            {
                UNSAFE.putByte(entity, offset, value);
            }
            else
            {
                UNSAFE.putObject(entity, offset, Byte.valueOf(value));
            }
        }

        @Override
        public void set(Object entity, Byte value)
        {
            if (primitive)
            {
                UNSAFE.putByte(entity, offset, value.byteValue());
            }
            else
            {
                UNSAFE.putObject(entity, offset, value);
            }
        }
    }

    class FieldLongValue implements LongValue
    {

        @Override
        public long get(Object entity) throws IllegalAccessException
        {
            if (primitive)
            {
                return field.getLong(entity);
            }
            else
            {
                return ((Long) field.get(entity));
            }
        }

        @Override
        public Long getObject(Object entity) throws IllegalAccessException
        {
            if (primitive)
            {
                return field.getLong(entity);
            }
            else
            {
                return ((Long) field.get(entity));
            }
        }

        @Override
        public void set(Object entity, long value) throws IllegalAccessException
        {
            if (primitive)
            {
                field.setLong(entity, value);
            }
            else
            {
                field.set(entity, value);
            }
        }

        @Override
        public void set(Object entity, Long value) throws IllegalAccessException
        {
            if (primitive)
            {
                field.setLong(entity, value);
            }
            else
            {
                field.set(entity, value);
            }
        }
    }

    class UnsafeLongValue implements LongValue
    {

        @Override
        public long get(Object entity)
        {
            if (primitive)
            {
                return UNSAFE.getLong(entity, offset);
            }
            else
            {
                return ((Long) UNSAFE.getObject(entity, offset)).longValue();
            }
        }

        @Override
        public Long getObject(Object entity)
        {
            if (primitive)
            {
                return UNSAFE.getLong(entity, offset);
            }
            else
            {
                return ((Long) UNSAFE.getObject(entity, offset));
            }
        }

        @Override
        public void set(Object entity, long value)
        {
            if (primitive)
            {
                UNSAFE.putLong(entity, offset, value);
            }
            else
            {
                UNSAFE.putObject(entity, offset, Long.valueOf(value));
            }
        }

        @Override
        public void set(Object entity, Long value)
        {
            if (primitive)
            {
                UNSAFE.putLong(entity, offset, value.longValue());
            }
            else
            {
                UNSAFE.putObject(entity, offset, value);
            }
        }
    }

    class FieldFloatValue implements FloatValue
    {

        @Override
        public float get(Object entity) throws IllegalAccessException
        {
            if (primitive)
            {
                return field.getFloat(entity);
            }
            else
            {
                return ((Float) field.get(entity));
            }
        }

        @Override
        public Float getObject(Object entity) throws IllegalAccessException
        {
            if (primitive)
            {
                return field.getFloat(entity);
            }
            else
            {
                return ((Float) field.get(entity));
            }
        }

        @Override
        public void set(Object entity, float value) throws IllegalAccessException
        {
            if (primitive)
            {
                field.setFloat(entity, value);
            }
            else
            {
                field.set(entity, value);
            }
        }

        @Override
        public void set(Object entity, Float value) throws IllegalAccessException
        {
            if (primitive)
            {
                field.setFloat(entity, value);
            }
            else
            {
                field.set(entity, value);
            }
        }
    }

    class UnsafeFloatValue implements FloatValue
    {

        @Override
        public float get(Object entity)
        {
            if (primitive)
            {
                return UNSAFE.getFloat(entity, offset);
            }
            else
            {
                return ((Float) UNSAFE.getObject(entity, offset)).floatValue();
            }
        }

        @Override
        public Float getObject(Object entity)
        {
            if (primitive)
            {
                return UNSAFE.getFloat(entity, offset);
            }
            else
            {
                return ((Float) UNSAFE.getObject(entity, offset));
            }
        }

        @Override
        public void set(Object entity, float value)
        {
            if (primitive)
            {
                UNSAFE.putFloat(entity, offset, value);
            }
            else
            {
                UNSAFE.putObject(entity, offset, Float.valueOf(value));
            }
        }

        @Override
        public void set(Object entity, Float value)
        {
            if (primitive)
            {
                UNSAFE.putFloat(entity, offset, value.floatValue());
            }
            else
            {
                UNSAFE.putObject(entity, offset, value);
            }
        }
    }

    class FieldDoubleValue implements DoubleValue
    {

        @Override
        public double get(Object entity) throws IllegalAccessException
        {
            if (primitive)
            {
                return field.getDouble(entity);
            }
            else
            {
                return ((Double) field.get(entity));
            }
        }

        @Override
        public Double getObject(Object entity) throws IllegalAccessException
        {
            if (primitive)
            {
                return field.getDouble(entity);
            }
            else
            {
                return ((Double) field.get(entity));
            }
        }

        @Override
        public void set(Object entity, double value) throws IllegalAccessException
        {
            if (primitive)
            {
                field.setDouble(entity, value);
            }
            else
            {
                field.set(entity, value);
            }
        }

        @Override
        public void set(Object entity, Double value) throws IllegalAccessException
        {
            if (primitive)
            {
                field.setDouble(entity, value);
            }
            else
            {
                field.set(entity, value);
            }
        }
    }

    class UnsafeDoubleValue implements DoubleValue
    {

        @Override
        public double get(Object entity)
        {
            if (primitive)
            {
                return UNSAFE.getDouble(entity, offset);
            }
            else
            {
                return ((Double) UNSAFE.getObject(entity, offset)).doubleValue();
            }
        }

        @Override
        public Double getObject(Object entity)
        {
            if (primitive)
            {
                return UNSAFE.getDouble(entity, offset);
            }
            else
            {
                return ((Double) UNSAFE.getObject(entity, offset));
            }
        }

        @Override
        public void set(Object entity, double value)
        {
            if (primitive)
            {
                UNSAFE.putDouble(entity, offset, value);
            }
            else
            {
                UNSAFE.putObject(entity, offset, Double.valueOf(value));
            }
        }

        @Override
        public void set(Object entity, Double value)
        {
            if (primitive)
            {
                UNSAFE.putDouble(entity, offset, value.doubleValue());
            }
            else
            {
                UNSAFE.putObject(entity, offset, value);
            }
        }
    }

    class FieldShortValue implements ShortValue
    {

        @Override
        public short get(Object entity) throws IllegalAccessException
        {
            if (primitive)
            {
                return field.getShort(entity);
            }
            else
            {
                return ((Short) field.get(entity));
            }
        }

        @Override
        public Short getObject(Object entity) throws IllegalAccessException
        {
            if (primitive)
            {
                return field.getShort(entity);
            }
            else
            {
                return ((Short) field.get(entity));
            }
        }

        @Override
        public void set(Object entity, short value) throws IllegalAccessException
        {
            if (primitive)
            {
                field.setShort(entity, value);
            }
            else
            {
                field.set(entity, value);
            }
        }

        @Override
        public void set(Object entity, Short value) throws IllegalAccessException
        {
            if (primitive)
            {
                field.setShort(entity, value);
            }
            else
            {
                field.set(entity, value);
            }
        }
    }

    class UnsafeShortValue implements ShortValue
    {

        @Override
        public short get(Object entity)
        {
            if (primitive)
            {
                return UNSAFE.getShort(entity, offset);
            }
            else
            {
                return ((Short) UNSAFE.getObject(entity, offset));
            }
        }

        @Override
        public Short getObject(Object entity)
        {
            if (primitive)
            {
                return UNSAFE.getShort(entity, offset);
            }
            else
            {
                return ((Short) UNSAFE.getObject(entity, offset));
            }
        }

        @Override
        public void set(Object entity, short value)
        {
            if (primitive)
            {
                UNSAFE.putShort(entity, offset, value);
            }
            else
            {
                UNSAFE.putObject(entity, offset, value);
            }
        }

        @Override
        public void set(Object entity, Short value)
        {
            if (primitive)
            {
                UNSAFE.putShort(entity, offset, value);
            }
            else
            {
                UNSAFE.putObject(entity, offset, value);
            }
        }
    }

    class FieldCharValue implements CharValue
    {

        @Override
        public char get(Object entity) throws IllegalAccessException
        {
            if (primitive)
            {
                return field.getChar(entity);
            }
            else
            {
                return ((Character) field.get(entity));
            }
        }

        @Override
        public Character getObject(Object entity) throws IllegalAccessException
        {
            if (primitive)
            {
                return field.getChar(entity);
            }
            else
            {
                return ((Character) field.get(entity));
            }
        }

        @Override
        public void set(Object entity, char value) throws IllegalAccessException
        {
            if (primitive)
            {
                field.setChar(entity, value);
            }
            else
            {
                field.set(entity, value);
            }
        }

        @Override
        public void set(Object entity, Character value) throws IllegalAccessException
        {
            if (primitive)
            {
                field.setChar(entity, value);
            }
            else
            {
                field.set(entity, value);
            }
        }
    }

    class UnsafeCharValue implements CharValue
    {

        @Override
        public char get(Object entity)
        {
            if (primitive)
            {
                return UNSAFE.getChar(entity, offset);
            }
            else
            {
                return ((Character) UNSAFE.getObject(entity, offset)).charValue();
            }
        }

        @Override
        public Character getObject(Object entity)
        {
            if (primitive)
            {
                return UNSAFE.getChar(entity, offset);
            }
            else
            {
                return ((Character) UNSAFE.getObject(entity, offset));
            }
        }

        @Override
        public void set(Object entity, char value)
        {
            if (primitive)
            {
                UNSAFE.putChar(entity, offset, value);
            }
            else
            {
                UNSAFE.putObject(entity, offset, Character.valueOf(value));
            }
        }

        @Override
        public void set(Object entity, Character value)
        {
            if (primitive)
            {
                UNSAFE.putChar(entity, offset, value.charValue());
            }
            else
            {
                UNSAFE.putObject(entity, offset, value);
            }
        }
    }

    private static final AtomicInteger count = new AtomicInteger();

    private String toMethodName(Field field)
    {
        return field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
    }

    public ValueAccessor(Field field, CompileHelper compileHelper)
    {
        Class<?> type = field.getType();
        if (type == int.class || type == Integer.class)
        {
            intValue = buildValueInstance(IntValue.class, field, compileHelper, int.class, Integer.class);
        }
        else if (type == short.class || type == Short.class)
        {
            shortValue = buildValueInstance(ShortValue.class, field, compileHelper, short.class, Short.class);
        }
        else if (type == long.class || type == Long.class)
        {
            longValue = buildValueInstance(LongValue.class, field, compileHelper, long.class, Long.class);
        }
        else if (type == float.class || type == Float.class)
        {
            floatValue = buildValueInstance(FloatValue.class, field, compileHelper, float.class, Float.class);
        }
        else if (type == double.class || type == Double.class)
        {
            doubleValue = buildValueInstance(DoubleValue.class, field, compileHelper, double.class, Double.class);
        }
        else if (type == boolean.class || type == Boolean.class)
        {
            booleanValue = buildValueInstance(BooleanValue.class, field, compileHelper, boolean.class, Boolean.class);
        }
        else if (type == byte.class || type == Byte.class)
        {
            byteValue = buildValueInstance(ByteValue.class, field, compileHelper, byte.class, Byte.class);
        }
        else if (type == char.class || type == Character.class)
        {
            charValue = buildValueInstance(CharValue.class, field, compileHelper, char.class, Character.class);
        }
        else
        {
            try
            {
                ClassModel classModel = new ClassModel("IntValue_" + count.getAndIncrement());
                classModel.addInterface(ObjectValue.class);
                Method      get         = ObjectValue.class.getDeclaredMethod("get", Object.class);
                Method      set         = ObjectValue.class.getDeclaredMethod("set", Object.class, Object.class);
                MethodModel methodModel = new MethodModel(get, classModel);
                methodModel.setBody("return ((" + SmcHelper.getReferenceName(field.getDeclaringClass(), classModel) + ")$0).get" + toMethodName(field) + "();");
                classModel.putMethodModel(methodModel);
                methodModel = new MethodModel(set, classModel);
                methodModel.setBody(" ((" + SmcHelper.getReferenceName(field.getDeclaringClass(), classModel) + ")$0).set" + toMethodName(field) + "(("+SmcHelper.getReferenceName(field.getType(),classModel)+")$1);");
                classModel.putMethodModel(methodModel);
                Class<?> compile = compileHelper.compile(classModel);
                objectValue = (ObjectValue) compile.newInstance();
            }
            catch (Exception e)
            {
                ReflectUtil.throwException(e);
            }
        }
    }

    private <T> T buildValueInstance(Class<T> tClass, Field field, CompileHelper compileHelper, Class c1, Class c2)
    {
        try
        {
            ClassModel classModel = new ClassModel("IntValue_" + count.getAndIncrement());
            classModel.addInterface(tClass);
            Method      get         = tClass.getDeclaredMethod("get", Object.class);
            Method      getObject   = tClass.getDeclaredMethod("getObject", Object.class);
            Method      set         = tClass.getDeclaredMethod("set", Object.class, c1);
            Method      set1        = tClass.getDeclaredMethod("set", Object.class, c2);
            MethodModel methodModel = new MethodModel(get, classModel);
            if (field.getType() == boolean.class)
            {
                methodModel.setBody("return ((" + SmcHelper.getReferenceName(field.getDeclaringClass(), classModel) + ")$0).is" + toMethodName(field) + "();");
            }
            else
            {
                methodModel.setBody("return ((" + SmcHelper.getReferenceName(field.getDeclaringClass(), classModel) + ")$0).get" + toMethodName(field) + "();");
            }
            classModel.putMethodModel(methodModel);
            methodModel = new MethodModel(getObject, classModel);
            if (field.getType() == boolean.class)
            {
                methodModel.setBody("return ((" + SmcHelper.getReferenceName(field.getDeclaringClass(), classModel) + ")$0).is" + toMethodName(field) + "();");
            }
            else
            {
                methodModel.setBody("return ((" + SmcHelper.getReferenceName(field.getDeclaringClass(), classModel) + ")$0).get" + toMethodName(field) + "();");
            }
            classModel.putMethodModel(methodModel);
            methodModel = new MethodModel(set, classModel);
            methodModel.setBody(" ((" + SmcHelper.getReferenceName(field.getDeclaringClass(), classModel) + ")$0).set" + toMethodName(field) + "($1);");
            classModel.putMethodModel(methodModel);
            methodModel = new MethodModel(set1, classModel);
            methodModel.setBody(" ((" + SmcHelper.getReferenceName(field.getDeclaringClass(), classModel) + ")$0).set" + toMethodName(field) + "($1);");
            classModel.putMethodModel(methodModel);
            Class<?> compile = compileHelper.compile(classModel);
            return (T) compile.newInstance();
        }
        catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return null;
        }
    }

    public ValueAccessor(Field field)
    {
        this.field = field;
        primitive = field.getType().isPrimitive();
        if (accessType == _UNSAFE)
        {
            offset = UNSAFE.objectFieldOffset(field);
        }
        else
        {
            field.setAccessible(true);
        }
        Class<?> type = field.getType();
        if (type == int.class || type == Integer.class)
        {
            if (accessType == _UNSAFE)
            {
                intValue = new UnsafeIntValue();
            }
            else
            {
                intValue = new FieldIntValue();
            }
        }
        else if (type == short.class || type == Short.class)
        {
            if (accessType == _UNSAFE)
            {
                shortValue = new UnsafeShortValue();
            }
            else
            {
                shortValue = new FieldShortValue();
            }
        }
        else if (type == long.class || type == Long.class)
        {
            if (accessType == _UNSAFE)
            {
                longValue = new UnsafeLongValue();
            }
            else
            {
                longValue = new FieldLongValue();
            }
        }
        else if (type == float.class || type == Float.class)
        {
            if (accessType == _UNSAFE)
            {
                floatValue = new UnsafeFloatValue();
            }
            else
            {
                floatValue = new FieldFloatValue();
            }
        }
        else if (type == double.class || type == Double.class)
        {
            if (accessType == _UNSAFE)
            {
                doubleValue = new UnsafeDoubleValue();
            }
            else
            {
                doubleValue = new FieldDoubleValue();
            }
        }
        else if (type == boolean.class || type == Boolean.class)
        {
            if (accessType == _UNSAFE)
            {
                booleanValue = new UnsafeBooleanValue();
            }
            else
            {
                booleanValue = new FieldBooleanValue();
            }
        }
        else if (type == byte.class || type == Byte.class)
        {
            if (accessType == _UNSAFE)
            {
                byteValue = new UnsafeByteValue();
            }
            else
            {
                byteValue = new FieldByteValue();
            }
        }
        else if (type == char.class || type == Character.class)
        {
            if (accessType == _UNSAFE)
            {
                charValue = new UnsafeCharValue();
            }
            else
            {
                charValue = new FieldCharValue();
            }
        }
        else
        {
            if (accessType == _UNSAFE)
            {
                objectValue = new UnsafeObjectValue();
            }
            else
            {
                objectValue = new FieldObjectValue();
            }
        }
    }

    public void set(Object entity, int value)
    {
        try
        {
            intValue.set(entity, value);
        }
        catch (IllegalAccessException e)
        {
            ReflectUtil.throwException(e);
        }
    }

    public void set(Object entity, Integer value)
    {
        try
        {
            intValue.set(entity, value);
        }
        catch (IllegalAccessException e)
        {
            ReflectUtil.throwException(e);
        }
    }

    public void set(Object entity, short value)
    {
        try
        {
            shortValue.set(entity, value);
        }
        catch (IllegalAccessException e)
        {
            ReflectUtil.throwException(e);
        }
    }

    public void set(Object entity, Short value)
    {
        try
        {
            shortValue.set(entity, value);
        }
        catch (IllegalAccessException e)
        {
            ReflectUtil.throwException(e);
        }
    }

    public void set(Object entity, long value)
    {
        try
        {
            longValue.set(entity, value);
        }
        catch (IllegalAccessException e)
        {
            ReflectUtil.throwException(e);
        }
    }

    public void set(Object entity, Long value)
    {
        try
        {
            longValue.set(entity, value);
        }
        catch (IllegalAccessException e)
        {
            ReflectUtil.throwException(e);
        }
    }

    public void set(Object entity, char value)
    {
        try
        {
            charValue.set(entity, value);
        }
        catch (IllegalAccessException e)
        {
            ReflectUtil.throwException(e);
        }
    }

    public void set(Object entity, Character value)
    {
        try
        {
            charValue.set(entity, value);
        }
        catch (IllegalAccessException e)
        {
            ReflectUtil.throwException(e);
        }
    }

    public void set(Object entity, byte value)
    {
        try
        {
            byteValue.set(entity, value);
        }
        catch (IllegalAccessException e)
        {
            ReflectUtil.throwException(e);
        }
    }

    public void set(Object entity, Byte value)
    {
        try
        {
            byteValue.set(entity, value);
        }
        catch (IllegalAccessException e)
        {
            ReflectUtil.throwException(e);
        }
    }

    public void set(Object entity, boolean value)
    {
        try
        {
            booleanValue.set(entity, value);
        }
        catch (IllegalAccessException e)
        {
            ReflectUtil.throwException(e);
        }
    }

    public void set(Object entity, Boolean value)
    {
        try
        {
            booleanValue.set(entity, value);
        }
        catch (IllegalAccessException e)
        {
            ReflectUtil.throwException(e);
        }
    }

    public void set(Object entity, float value)
    {
        try
        {
            floatValue.set(entity, value);
        }
        catch (IllegalAccessException e)
        {
            ReflectUtil.throwException(e);
        }
    }

    public void set(Object entity, Float value)
    {
        try
        {
            floatValue.set(entity, value);
        }
        catch (IllegalAccessException e)
        {
            ReflectUtil.throwException(e);
        }
    }

    public void set(Object entity, double value)
    {
        try
        {
            doubleValue.set(entity, value);
        }
        catch (IllegalAccessException e)
        {
            ReflectUtil.throwException(e);
        }
    }

    public void set(Object entity, Double value)
    {
        try
        {
            doubleValue.set(entity, value);
        }
        catch (IllegalAccessException e)
        {
            ReflectUtil.throwException(e);
        }
    }

    public void setObject(Object entity, Object value)
    {
        try
        {
            objectValue.set(entity, value);
        }
        catch (IllegalAccessException e)
        {
            ReflectUtil.throwException(e);
        }
    }

    public int getInt(Object entity)
    {
        try
        {
            return intValue.get(entity);
        }
        catch (IllegalAccessException e)
        {
            ReflectUtil.throwException(e);
            return 0;
        }
    }

    public Integer getIntObject(Object entity)
    {
        try
        {
            return intValue.getObject(entity);
        }
        catch (IllegalAccessException e)
        {
            ReflectUtil.throwException(e);
            return 0;
        }
    }

    public short getShort(Object entity)
    {
        try
        {
            return shortValue.get(entity);
        }
        catch (IllegalAccessException e)
        {
            ReflectUtil.throwException(e);
            return 0;
        }
    }

    public Short getShortObject(Object entity)
    {
        try
        {
            return shortValue.getObject(entity);
        }
        catch (IllegalAccessException e)
        {
            ReflectUtil.throwException(e);
            return 0;
        }
    }

    public boolean getBoolean(Object entity)
    {
        try
        {
            return booleanValue.get(entity);
        }
        catch (IllegalAccessException e)
        {
            ReflectUtil.throwException(e);
            return false;
        }
    }

    public Boolean getBooleanObject(Object entity)
    {
        try
        {
            return booleanValue.getObject(entity);
        }
        catch (IllegalAccessException e)
        {
            ReflectUtil.throwException(e);
            return false;
        }
    }

    public long getLong(Object entity)
    {
        try
        {
            return longValue.get(entity);
        }
        catch (IllegalAccessException e)
        {
            ReflectUtil.throwException(e);
            return 0;
        }
    }

    public Long getLongObject(Object entity)
    {
        try
        {
            return longValue.getObject(entity);
        }
        catch (IllegalAccessException e)
        {
            ReflectUtil.throwException(e);
            return 0l;
        }
    }

    public byte getByte(Object entity)
    {
        try
        {
            return byteValue.get(entity);
        }
        catch (IllegalAccessException e)
        {
            ReflectUtil.throwException(e);
            return 0;
        }
    }

    public Byte getByteObject(Object entity)
    {
        try
        {
            return byteValue.getObject(entity);
        }
        catch (IllegalAccessException e)
        {
            ReflectUtil.throwException(e);
            return 0;
        }
    }

    public char getChar(Object entity)
    {
        try
        {
            return charValue.get(entity);
        }
        catch (IllegalAccessException e)
        {
            ReflectUtil.throwException(e);
            return 0;
        }
    }

    public Character getCharObject(Object entity)
    {
        try
        {
            return charValue.getObject(entity);
        }
        catch (IllegalAccessException e)
        {
            ReflectUtil.throwException(e);
            return 0;
        }
    }

    public float getFloat(Object entity)
    {
        try
        {
            return floatValue.get(entity);
        }
        catch (IllegalAccessException e)
        {
            ReflectUtil.throwException(e);
            return 0;
        }
    }

    public Float getFloatObject(Object entity)
    {
        try
        {
            return floatValue.getObject(entity);
        }
        catch (IllegalAccessException e)
        {
            ReflectUtil.throwException(e);
            return 0f;
        }
    }

    public double getDouble(Object entity)
    {
        try
        {
            return doubleValue.get(entity);
        }
        catch (IllegalAccessException e)
        {
            ReflectUtil.throwException(e);
            return 0;
        }
    }

    public Double getDoubleObject(Object entity)
    {
        try
        {
            return doubleValue.getObject(entity);
        }
        catch (IllegalAccessException e)
        {
            ReflectUtil.throwException(e);
            return 0d;
        }
    }

    public Object get(Object entity)
    {
        try
        {
            return objectValue.get(entity);
        }
        catch (IllegalAccessException e)
        {
            ReflectUtil.throwException(e);
            return null;
        }
    }

    public Field getField()
    {
        return field;
    }
}
