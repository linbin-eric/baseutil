package com.jfirer.baseutil.reflect;

import com.jfirer.baseutil.reflect.valueaccessor.ValueAccessor;
import com.jfirer.baseutil.reflect.valueaccessor.impl.UnsafeValueAccessorImpl;
import com.jfirer.baseutil.smc.SmcHelper;
import com.jfirer.baseutil.smc.compiler.CompileHelper;
import com.jfirer.baseutil.smc.model.ClassModel;
import com.jfirer.baseutil.smc.model.MethodModel;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

public class CompileValueAccessor
{
    protected static final AtomicInteger count = new AtomicInteger();

    static String toMethodName(Field field)
    {
        return field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
    }

    public static ValueAccessor create(Field field, CompileHelper compileHelper) throws NoSuchMethodException, IOException, ClassNotFoundException
    {
        ClassModel classModel = new ClassModel("ValueAccessor_" + field.getName() + "_" + count.getAndIncrement());
        classModel.addInterface(ValueAccessor.class);
        classModel.addImport(field.getDeclaringClass());
        int    classId       = ReflectUtil.getClassId(field.getType());
        String getMethodName = ReflectUtil.isBooleanOrBooleanBox(field.getType()) ? "is" + toMethodName(field) : "get" + toMethodName(field);
        String setMethodName = "set" + toMethodName(field);
        field.getDeclaringClass().getMethod(getMethodName);
        field.getDeclaringClass().getMethod(setMethodName, field.getType());
        if (ReflectUtil.isPrimitive(field.getType()))
        {
            MethodModel methodModel = switch (classId)
            {
                case ReflectUtil.PRIMITIVE_BYTE -> new MethodModel(ValueAccessor.class.getDeclaredMethod("getByte", Object.class), classModel);
                case ReflectUtil.PRIMITIVE_CHAR -> new MethodModel(ValueAccessor.class.getDeclaredMethod("getChar", Object.class), classModel);
                case ReflectUtil.PRIMITIVE_DOUBLE -> new MethodModel(ValueAccessor.class.getDeclaredMethod("getDouble", Object.class), classModel);
                case ReflectUtil.PRIMITIVE_FLOAT -> new MethodModel(ValueAccessor.class.getDeclaredMethod("getFloat", Object.class), classModel);
                case ReflectUtil.PRIMITIVE_INT -> new MethodModel(ValueAccessor.class.getDeclaredMethod("getInt", Object.class), classModel);
                case ReflectUtil.PRIMITIVE_LONG -> new MethodModel(ValueAccessor.class.getDeclaredMethod("getLong", Object.class), classModel);
                case ReflectUtil.PRIMITIVE_SHORT -> new MethodModel(ValueAccessor.class.getDeclaredMethod("getShort", Object.class), classModel);
                case ReflectUtil.PRIMITIVE_BOOL -> new MethodModel(ValueAccessor.class.getDeclaredMethod("getBoolean", Object.class), classModel);
                default -> throw new IllegalStateException("Unexpected value: " + classId);
            };
            methodModel.setParamterNames("entity");
            methodModel.setBody("return ("+SmcHelper.getReferenceName(field.getDeclaringClass(), classModel)+  " entity)." + getMethodName + "();");
            classModel.putMethodModel(methodModel);
            MethodModel setMethodModel = switch (classId)
            {
                case ReflectUtil.PRIMITIVE_BYTE -> new MethodModel(ValueAccessor.class.getDeclaredMethod("set", Object.class, byte.class), classModel);
                case ReflectUtil.PRIMITIVE_CHAR -> new MethodModel(ValueAccessor.class.getDeclaredMethod("set", Object.class, char.class), classModel);
                case ReflectUtil.PRIMITIVE_DOUBLE -> new MethodModel(ValueAccessor.class.getDeclaredMethod("set", Object.class, double.class), classModel);
                case ReflectUtil.PRIMITIVE_FLOAT -> new MethodModel(ValueAccessor.class.getDeclaredMethod("set", Object.class, float.class), classModel);
                case ReflectUtil.PRIMITIVE_INT -> new MethodModel(ValueAccessor.class.getDeclaredMethod("set", Object.class, int.class), classModel);
                case ReflectUtil.PRIMITIVE_LONG -> new MethodModel(ValueAccessor.class.getDeclaredMethod("set", Object.class, long.class), classModel);
                case ReflectUtil.PRIMITIVE_SHORT -> new MethodModel(ValueAccessor.class.getDeclaredMethod("set", Object.class, short.class), classModel);
                case ReflectUtil.PRIMITIVE_BOOL -> new MethodModel(ValueAccessor.class.getDeclaredMethod("set", Object.class, boolean.class), classModel);
                default -> throw new IllegalStateException("Unexpected value: " + classId);
            };
            setMethodModel.setParamterNames("entity", "value");
            setMethodModel.setBody("entity." + setMethodName + "(value);");
            classModel.putMethodModel(methodModel);
        }
        MethodModel getMethodModel = new MethodModel(ValueAccessor.class.getDeclaredMethod("get", Object.class), classModel);
        getMethodModel.setParamterNames("entity");
        getMethodModel.setBody("return entity." + getMethodName + "();");
        classModel.putMethodModel(getMethodModel);
        MethodModel getRefenceMethodModel = new MethodModel(ValueAccessor.class.getDeclaredMethod("getReference", Object.class), classModel);
        getRefenceMethodModel.setParamterNames("entity");
        getRefenceMethodModel.setBody("return entity." + getMethodName + "();");
        classModel.putMethodModel(getRefenceMethodModel);
        MethodModel setMethodModel = new MethodModel(ValueAccessor.class.getDeclaredMethod("set", Object.class, Object.class), classModel);
        setMethodModel.setParamterNames("entity", "value");
        setMethodModel.setBody("entity." + setMethodName + "(value);");
        classModel.putMethodModel(setMethodModel);
        MethodModel setReferenceMethodModel = new MethodModel(ValueAccessor.class.getDeclaredMethod("setReference", Object.class, Object.class), classModel);
        setReferenceMethodModel.setParamterNames("entity", "value");
        setReferenceMethodModel.setBody("entity." + setMethodName + "(value);");
        classModel.putMethodModel(setReferenceMethodModel);
     compileHelper.compile(classModel);
    }

    private static ValueAccessor build(Field field, CompileHelper compileHelper, ClassModel classModel, String getMethodName, Class<?> C1, Class<?> C2)
    {
        try
        {
            overrideGetMethod(field, classModel, getMethodName);
            overrideGetMethod(field, classModel, getMethodName + "Object");
            overrideGetMethod(field, classModel, "get");
            overrideSetMethod(field, classModel, "set", C1);
            overrideSetMethod(field, classModel, "set", C2);
            overrideSetMethod(field, classModel, "setObject", Object.class);
            return (ValueAccessor) compileHelper.compile(classModel).getDeclaredConstructor().newInstance();
        }
        catch (Exception e)
        {
            ReflectUtil.throwException(e);
        }
        return null;
    }

    private static void overrideSetMethod(Field field, ClassModel classModel, String setMethodName, Class paramType) throws NoSuchMethodException
    {
        Method      method      = UnsafeValueAccessorImpl.class.getDeclaredMethod(setMethodName, Object.class, paramType);
        MethodModel methodModel = new MethodModel(method, classModel);
        if (paramType == Object.class)
        {
            methodModel.setBody("((" + SmcHelper.getReferenceName(field.getDeclaringClass(), classModel) + ")$0).set" + toMethodName(field) + "((" + SmcHelper.getReferenceName(field.getType(), classModel) + ")$1);");
        }
        else
        {
            methodModel.setBody("((" + SmcHelper.getReferenceName(field.getDeclaringClass(), classModel) + ")$0).set" + toMethodName(field) + "($1);");
        }
        classModel.putMethodModel(methodModel);
    }

    private static void overrideGetMethod(Field field, ClassModel classModel, String getMethodName) throws NoSuchMethodException
    {
        Method      method      = UnsafeValueAccessorImpl.class.getDeclaredMethod(getMethodName, Object.class);
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
    }

    @Override
    public void set(Object entity, int value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(Object entity, Integer value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(Object entity, short value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(Object entity, Short value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(Object entity, long value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(Object entity, Long value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(Object entity, char value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(Object entity, Character value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(Object entity, byte value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(Object entity, Byte value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(Object entity, boolean value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(Object entity, Boolean value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(Object entity, float value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(Object entity, Float value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(Object entity, double value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(Object entity, Double value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setObject(Object entity, Object value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getInt(Object entity)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Integer getIntObject(Object entity)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public short getShort(Object entity)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Short getShortObject(Object entity)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getBoolean(Object entity)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Boolean getBooleanObject(Object entity)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getLong(Object entity)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long getLongObject(Object entity)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte getByte(Object entity)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Byte getByteObject(Object entity)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public char getChar(Object entity)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Character getCharObject(Object entity)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public float getFloat(Object entity)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Float getFloatObject(Object entity)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getDouble(Object entity)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Double getDoubleObject(Object entity)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object get(Object entity)
    {
        throw new UnsupportedOperationException();
    }
}
