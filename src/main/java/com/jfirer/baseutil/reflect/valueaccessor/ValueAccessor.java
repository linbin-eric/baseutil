package com.jfirer.baseutil.reflect.valueaccessor;

import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.baseutil.smc.SmcHelper;
import com.jfirer.baseutil.smc.compiler.CompileHelper;
import com.jfirer.baseutil.smc.model.ClassModel;
import com.jfirer.baseutil.smc.model.MethodModel;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

public interface ValueAccessor
{
    default void set(Object entity, int value)
    {
        throw new IllegalStateException("not impl method!");
    }

    default void set(Object entity, short value)
    {
        throw new IllegalStateException("not impl method!");
    }

    default void set(Object entity, long value)
    {
        throw new IllegalStateException("not impl method!");
    }

    default void set(Object entity, char value)
    {
        throw new IllegalStateException("not impl method!");
    }

    default void set(Object entity, byte value)
    {
        throw new IllegalStateException("not impl method!");
    }

    default void set(Object entity, boolean value)
    {
        throw new IllegalStateException("not impl method!");
    }

    default void set(Object entity, float value)
    {
        throw new IllegalStateException("not impl method!");
    }

    default void set(Object entity, double value)
    {
        throw new IllegalStateException("not impl method!");
    }

    default void setReference(Object entity, Object value)
    {
        throw new IllegalStateException("not impl method!");
    }

    default void setObject(Object entity, Object value)
    {
        throw new IllegalStateException("not impl method!");
    }

    default int getInt(Object entity)
    {
        throw new IllegalStateException("not impl method!");
    }

    default short getShort(Object entity)
    {
        throw new IllegalStateException("not impl method!");
    }

    default boolean getBoolean(Object entity)
    {
        throw new IllegalStateException("not impl method!");
    }

    default long getLong(Object entity)
    {
        throw new IllegalStateException("not impl method!");
    }

    default byte getByte(Object entity)
    {
        throw new IllegalStateException("not impl method!");
    }

    default char getChar(Object entity)
    {
        throw new IllegalStateException("not impl method!");
    }

    default float getFloat(Object entity)
    {
        throw new IllegalStateException("not impl method!");
    }

    default double getDouble(Object entity)
    {
        throw new IllegalStateException("not impl method!");
    }

    default Object get(Object entity)
    {
        throw new IllegalStateException("not impl method!");
    }

    default Object getReference(Object entity)
    {
        throw new IllegalStateException("not impl method!");
    }

    AtomicInteger count = new AtomicInteger();

    static String toMethodName(Field field)
    {
        return field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
    }

    static ValueAccessor create(Field field, CompileHelper compileHelper)
    {
        try
        {
            ClassModel classModel = new ClassModel("ValueAccessor_" + field.getName() + "_" + count.getAndIncrement());
            classModel.addInterface(ValueAccessor.class);
            classModel.addImport(field.getDeclaringClass());
            int    classId       = ReflectUtil.getClassId(field.getType());
            String getMethodName = field.getType() == boolean.class ? "is" + toMethodName(field) : "get" + toMethodName(field);
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
                methodModel.setBody("return ((" + SmcHelper.getReferenceName(field.getDeclaringClass(), classModel) + " )entity)." + getMethodName + "();");
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
                setMethodModel.setBody("((" + SmcHelper.getReferenceName(field.getDeclaringClass(), classModel) + "entity)." + setMethodName + "(value);");
                classModel.putMethodModel(methodModel);
            }
            MethodModel getMethodModel = new MethodModel(ValueAccessor.class.getDeclaredMethod("get", Object.class), classModel);
            getMethodModel.setParamterNames("entity");
            getMethodModel.setBody("return ((" + SmcHelper.getReferenceName(field.getDeclaringClass(), classModel) + " )entity)." + getMethodName + "();");
            classModel.putMethodModel(getMethodModel);
            MethodModel getRefenceMethodModel = new MethodModel(ValueAccessor.class.getDeclaredMethod("getReference", Object.class), classModel);
            getRefenceMethodModel.setParamterNames("entity");
            getRefenceMethodModel.setBody("return ((" + SmcHelper.getReferenceName(field.getDeclaringClass(), classModel) + " )entity)." + getMethodName + "();");
            classModel.putMethodModel(getRefenceMethodModel);
            MethodModel setMethodModel = new MethodModel(ValueAccessor.class.getDeclaredMethod("set", Object.class, Object.class), classModel);
            setMethodModel.setParamterNames("entity", "value");
            setMethodModel.setBody("((" + SmcHelper.getReferenceName(field.getDeclaringClass(), classModel) + "entity)." + setMethodName + "(value);");
            classModel.putMethodModel(setMethodModel);
            MethodModel setReferenceMethodModel = new MethodModel(ValueAccessor.class.getDeclaredMethod("setReference", Object.class, Object.class), classModel);
            setReferenceMethodModel.setParamterNames("entity", "value");
            setReferenceMethodModel.setBody("((" + SmcHelper.getReferenceName(field.getDeclaringClass(), classModel) + "entity)." + setMethodName + "(value);");
            classModel.putMethodModel(setReferenceMethodModel);
            Class<?> compile = compileHelper.compile(classModel);
            return (ValueAccessor) compile.getConstructor().newInstance();
        }
        catch (Throwable e)
        {
            ReflectUtil.throwException(e);
            return null;
        }
    }
}
