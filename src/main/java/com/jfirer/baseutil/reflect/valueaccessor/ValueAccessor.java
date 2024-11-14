package com.jfirer.baseutil.reflect.valueaccessor;

import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.baseutil.reflect.valueaccessor.impl.LambdaAccessorImpl;
import com.jfirer.baseutil.reflect.valueaccessor.impl.UnsafeValueAccessorImpl;
import com.jfirer.baseutil.smc.SmcHelper;
import com.jfirer.baseutil.smc.compiler.CompileHelper;
import com.jfirer.baseutil.smc.model.ClassModel;
import com.jfirer.baseutil.smc.model.ConstructorModel;
import com.jfirer.baseutil.smc.model.FieldModel;
import com.jfirer.baseutil.smc.model.MethodModel;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;

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

    AtomicInteger count         = new AtomicInteger();
    CompileHelper compileHelper = new CompileHelper();

    static String toMethodName(Field field)
    {
        return field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
    }

    static ValueAccessor standard(Field field)
    {
        return new UnsafeValueAccessorImpl(field);
    }

    static ValueAccessor lambda(Field field)
    {
        return new LambdaAccessorImpl(field);
    }

    static ValueAccessor compile(Field field)
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
                methodModel.setFinal(true);
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
                setMethodModel.setFinal(true);
                setMethodModel.setBody("((" + SmcHelper.getReferenceName(field.getDeclaringClass(), classModel) + ")entity)." + setMethodName + "(value);");
                classModel.putMethodModel(setMethodModel);
            }
            MethodModel getMethodModel = new MethodModel(ValueAccessor.class.getDeclaredMethod("get", Object.class), classModel);
            getMethodModel.setFinal(true);
            getMethodModel.setParamterNames("entity");
            getMethodModel.setBody("return ((" + SmcHelper.getReferenceName(field.getDeclaringClass(), classModel) + " )entity)." + getMethodName + "();");
            classModel.putMethodModel(getMethodModel);
            MethodModel getRefenceMethodModel = new MethodModel(ValueAccessor.class.getDeclaredMethod("getReference", Object.class), classModel);
            getRefenceMethodModel.setParamterNames("entity");
            getRefenceMethodModel.setFinal(true);
            getRefenceMethodModel.setBody("return ((" + SmcHelper.getReferenceName(field.getDeclaringClass(), classModel) + " )entity)." + getMethodName + "();");
            classModel.putMethodModel(getRefenceMethodModel);
            if (field.getType().isPrimitive())
            {
                MethodModel setMethodModel = new MethodModel(ValueAccessor.class.getDeclaredMethod("setObject", Object.class, Object.class), classModel);
                setMethodModel.setFinal(true);
                setMethodModel.setParamterNames("entity", "value");
                setMethodModel.setBody("((" + SmcHelper.getReferenceName(field.getDeclaringClass(), classModel) + ")entity)." + setMethodName + "((" + SmcHelper.getReferenceName(ReflectUtil.getBoxedTypeOrOrigin(field.getType()), classModel) + ")value);");
                classModel.putMethodModel(setMethodModel);
            }
            else
            {
                MethodModel setMethodModel = new MethodModel(ValueAccessor.class.getDeclaredMethod("setObject", Object.class, Object.class), classModel);
                setMethodModel.setFinal(true);
                setMethodModel.setParamterNames("entity", "value");
                setMethodModel.setBody("((" + SmcHelper.getReferenceName(field.getDeclaringClass(), classModel) + ")entity)." + setMethodName + "((" + SmcHelper.getReferenceName(field.getType(), classModel) + ")value);");
                classModel.putMethodModel(setMethodModel);
                MethodModel setReferenceMethodModel = new MethodModel(ValueAccessor.class.getDeclaredMethod("setReference", Object.class, Object.class), classModel);
                setReferenceMethodModel.setFinal(true);
                setReferenceMethodModel.setParamterNames("entity", "value");
                setReferenceMethodModel.setBody("((" + SmcHelper.getReferenceName(field.getDeclaringClass(), classModel) + ")entity)." + setMethodName + "((" + SmcHelper.getReferenceName(field.getType(), classModel) + ")value);");
                classModel.putMethodModel(setReferenceMethodModel);
            }
            Class<?> compile = compileHelper.compile(classModel);
            return (ValueAccessor) compile.getConstructor().newInstance();
        }
        catch (Throwable e)
        {
            ReflectUtil.throwException(e);
            return null;
        }
    }

    static GetInt buildGetInt(Field field)
    {
        if (field.getType() != int.class)
        {
            throw new IllegalArgumentException(field.getName() + " is not int");
        }
        MethodHandles.Lookup lookup        = MethodHandles.lookup();
        String               getMethodName = "get" + toMethodName(field);
        try
        {
            return (GetInt) LambdaMetafactory.metafactory(lookup, //固定参数
                                                          "get",//需要实现的函数式接口的方法名
                                                          MethodType.methodType(GetInt.class),//固定参数，本方法最终返回的函数式接口的类
                                                          MethodType.methodType(int.class, Object.class),// 函数式接口的方法签名，如果是泛型的，用 Object.class代替
                                                          lookup.findVirtual(field.getDeclaringClass(), getMethodName, MethodType.methodType(int.class)),//这个函数接口需要引用的类的方法
                                                          MethodType.methodType(int.class, field.getDeclaringClass()))//实际运行时候传入的参数类型，也就是泛型信息在运行的时候对应的实际的类型。
                                             .getTarget().invoke();
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    static SetInt buildSetInt(Field field)
    {
        if (field.getType() != int.class)
        {
            throw new IllegalArgumentException(field.getName() + " is not int");
        }
        MethodHandles.Lookup lookup        = MethodHandles.lookup();
        String               setMethodName = "set" + toMethodName(field);
        try
        {
            return (SetInt) LambdaMetafactory.metafactory(lookup, //固定参数
                                                          "set",//需要实现的函数式接口的方法名
                                                          MethodType.methodType(SetInt.class),////固定写法，中间参数是需要实现的函数接口类
                                                          MethodType.methodType(void.class, Object.class, int.class),// 函数式接口的方法签名
                                                          lookup.findVirtual(field.getDeclaringClass(), setMethodName, MethodType.methodType(void.class, field.getType())),//这个函数接口需要引用的类的实例方法
                                                          MethodType.methodType(void.class, field.getDeclaringClass(), int.class))//实际运行的时候，这个函数式接口的方法签名。也就是将泛型的信息补充上
                                             .getTarget().invoke();
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    static GetByte buildGetByte(Field field)
    {
        if (field.getType() != byte.class)
        {
            throw new IllegalArgumentException(field.getName() + " is not byte");
        }
        MethodHandles.Lookup lookup        = MethodHandles.lookup();
        String               getMethodName = "get" + toMethodName(field);
        try
        {
            return (GetByte) LambdaMetafactory.metafactory(lookup, //固定参数
                                                           "get",//需要实现的函数式接口的方法名
                                                           MethodType.methodType(GetByte.class),//固定参数，本方法最终返回的函数式接口的类
                                                           MethodType.methodType(byte.class, Object.class),// 函数式接口的方法签名，如果是泛型的，用 Object.class代替
                                                           lookup.findVirtual(field.getDeclaringClass(), getMethodName, MethodType.methodType(byte.class)),//这个函数接口需要引用的类的方法
                                                           MethodType.methodType(byte.class, field.getDeclaringClass()))//实际运行时候传入的参数类型，也就是泛型信息在运行的时候对应的实际的类型。
                                              .getTarget().invoke();
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    static SetByte buildSetByte(Field field)
    {
        if (field.getType() != byte.class)
        {
            throw new IllegalArgumentException(field.getName() + " is not byte");
        }
        MethodHandles.Lookup lookup        = MethodHandles.lookup();
        String               setMethodName = "set" + toMethodName(field);
        try
        {
            return (SetByte) LambdaMetafactory.metafactory(lookup, //固定参数
                                                           "set",//需要实现的函数式接口的方法名
                                                           MethodType.methodType(SetByte.class),////固定写法，中间参数是需要实现的函数接口类
                                                           MethodType.methodType(void.class, Object.class, byte.class),// 函数式接口的方法签名
                                                           lookup.findVirtual(field.getDeclaringClass(), setMethodName, MethodType.methodType(void.class, field.getType())),//这个函数接口需要引用的类的实例方法
                                                           MethodType.methodType(void.class, field.getDeclaringClass(), byte.class))//实际运行的时候，这个函数式接口的方法签名。也就是将泛型的信息补充上
                                              .getTarget().invoke();
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    static GetChar buildGetChar(Field field)
    {
        if (field.getType() != char.class)
        {
            throw new IllegalArgumentException(field.getName() + " is not char");
        }
        MethodHandles.Lookup lookup        = MethodHandles.lookup();
        String               getMethodName = "get" + toMethodName(field);
        try
        {
            return (GetChar) LambdaMetafactory.metafactory(lookup, //固定参数
                                                           "get",//需要实现的函数式接口的方法名
                                                           MethodType.methodType(GetChar.class),//固定参数，本方法最终返回的函数式接口的类
                                                           MethodType.methodType(char.class, Object.class),// 函数式接口的方法签名，如果是泛型的，用 Object.class代替
                                                           lookup.findVirtual(field.getDeclaringClass(), getMethodName, MethodType.methodType(char.class)),//这个函数接口需要引用的类的方法
                                                           MethodType.methodType(char.class, field.getDeclaringClass()))//实际运行时候传入的参数类型，也就是泛型信息在运行的时候对应的实际的类型。
                                              .getTarget().invoke();
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    static SetChar buildSetChar(Field field)
    {
        if (field.getType() != char.class)
        {
            throw new IllegalArgumentException(field.getName() + " is not char");
        }
        MethodHandles.Lookup lookup        = MethodHandles.lookup();
        String               setMethodName = "set" + toMethodName(field);
        try
        {
            return (SetChar) LambdaMetafactory.metafactory(lookup, //固定参数
                                                           "set",//需要实现的函数式接口的方法名
                                                           MethodType.methodType(SetChar.class),////固定写法，中间参数是需要实现的函数接口类
                                                           MethodType.methodType(void.class, Object.class, char.class),// 函数式接口的方法签名
                                                           lookup.findVirtual(field.getDeclaringClass(), setMethodName, MethodType.methodType(void.class, field.getType())),//这个函数接口需要引用的类的实例方法
                                                           MethodType.methodType(void.class, field.getDeclaringClass(), char.class))//实际运行的时候，这个函数式接口的方法签名。也就是将泛型的信息补充上
                                              .getTarget().invoke();
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    static GetDouble buildGetDouble(Field field)
    {
        if (field.getType() != double.class)
        {
            throw new IllegalArgumentException(field.getName() + " is not double");
        }
        MethodHandles.Lookup lookup        = MethodHandles.lookup();
        String               getMethodName = "get" + toMethodName(field);
        try
        {
            return (GetDouble) LambdaMetafactory.metafactory(lookup, //固定参数
                                                             "get",//需要实现的函数式接口的方法名
                                                             MethodType.methodType(GetDouble.class),//固定参数，本方法最终返回的函数式接口的类
                                                             MethodType.methodType(double.class, Object.class),// 函数式接口的方法签名，如果是泛型的，用 Object.class代替
                                                             lookup.findVirtual(field.getDeclaringClass(), getMethodName, MethodType.methodType(double.class)),//这个函数接口需要引用的类的方法
                                                             MethodType.methodType(double.class, field.getDeclaringClass()))//实际运行时候传入的参数类型，也就是泛型信息在运行的时候对应的实际的类型。
                                                .getTarget().invoke();
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    static SetDouble buildSetDouble(Field field)
    {
        if (field.getType() != double.class)
        {
            throw new IllegalArgumentException(field.getName() + " is not double");
        }
        MethodHandles.Lookup lookup        = MethodHandles.lookup();
        String               setMethodName = "set" + toMethodName(field);
        try
        {
            return (SetDouble) LambdaMetafactory.metafactory(lookup, //固定参数
                                                             "set",//需要实现的函数式接口的方法名
                                                             MethodType.methodType(SetDouble.class),////固定写法，中间参数是需要实现的函数接口类
                                                             MethodType.methodType(void.class, Object.class, double.class),// 函数式接口的方法签名
                                                             lookup.findVirtual(field.getDeclaringClass(), setMethodName, MethodType.methodType(void.class, field.getType())),//这个函数接口需要引用的类的实例方法
                                                             MethodType.methodType(void.class, field.getDeclaringClass(), double.class))//实际运行的时候，这个函数式接口的方法签名。也就是将泛型的信息补充上
                                                .getTarget().invoke();
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    static GetShort buildGetShort(Field field)
    {
        if (field.getType() != short.class)
        {
            throw new IllegalArgumentException(field.getName() + " is not short");
        }
        MethodHandles.Lookup lookup        = MethodHandles.lookup();
        String               getMethodName = "get" + toMethodName(field);
        try
        {
            return (GetShort) LambdaMetafactory.metafactory(lookup, //固定参数
                                                            "get",//需要实现的函数式接口的方法名
                                                            MethodType.methodType(GetShort.class),//固定参数，本方法最终返回的函数式接口的类
                                                            MethodType.methodType(short.class, Object.class),// 函数式接口的方法签名，如果是泛型的，用 Object.class代替
                                                            lookup.findVirtual(field.getDeclaringClass(), getMethodName, MethodType.methodType(short.class)),//这个函数接口需要引用的类的方法
                                                            MethodType.methodType(short.class, field.getDeclaringClass()))//实际运行时候传入的参数类型，也就是泛型信息在运行的时候对应的实际的类型。
                                               .getTarget().invoke();
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    static SetShort buildSetShort(Field field)
    {
        if (field.getType() != short.class)
        {
            throw new IllegalArgumentException(field.getName() + " is not short");
        }
        MethodHandles.Lookup lookup        = MethodHandles.lookup();
        String               setMethodName = "set" + toMethodName(field);
        try
        {
            return (SetShort) LambdaMetafactory.metafactory(lookup, //固定参数
                                                            "set",//需要实现的函数式接口的方法名
                                                            MethodType.methodType(SetShort.class),////固定写法，中间参数是需要实现的函数接口类
                                                            MethodType.methodType(void.class, Object.class, short.class),// 函数式接口的方法签名
                                                            lookup.findVirtual(field.getDeclaringClass(), setMethodName, MethodType.methodType(void.class, field.getType())),//这个函数接口需要引用的类的实例方法
                                                            MethodType.methodType(void.class, field.getDeclaringClass(), short.class))//实际运行的时候，这个函数式接口的方法签名。也就是将泛型的信息补充上
                                               .getTarget().invoke();
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    static GetBoolean buildGetBoolean(Field field)
    {
        if (field.getType() != boolean.class)
        {
            throw new IllegalArgumentException(field.getName() + " is not boolean");
        }
        MethodHandles.Lookup lookup        = MethodHandles.lookup();
        String               getMethodName = "is" + toMethodName(field);
        try
        {
            return (GetBoolean) LambdaMetafactory.metafactory(lookup, //固定参数
                                                              "get",//需要实现的函数式接口的方法名
                                                              MethodType.methodType(GetBoolean.class),//固定参数，本方法最终返回的函数式接口的类
                                                              MethodType.methodType(boolean.class, Object.class),// 函数式接口的方法签名，如果是泛型的，用 Object.class代替
                                                              lookup.findVirtual(field.getDeclaringClass(), getMethodName, MethodType.methodType(boolean.class)),//这个函数接口需要引用的类的方法
                                                              MethodType.methodType(boolean.class, field.getDeclaringClass()))//实际运行时候传入的参数类型，也就是泛型信息在运行的时候对应的实际的类型。
                                                 .getTarget().invoke();
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    static SetBoolean buildSetBoolean(Field field)
    {
        if (field.getType() != boolean.class)
        {
            throw new IllegalArgumentException(field.getName() + " is not boolean");
        }
        MethodHandles.Lookup lookup        = MethodHandles.lookup();
        String               setMethodName = "set" + toMethodName(field);
        try
        {
            return (SetBoolean) LambdaMetafactory.metafactory(lookup, //固定参数
                                                              "set",//需要实现的函数式接口的方法名
                                                              MethodType.methodType(SetBoolean.class),////固定写法，中间参数是需要实现的函数接口类
                                                              MethodType.methodType(void.class, Object.class, boolean.class),// 函数式接口的方法签名
                                                              lookup.findVirtual(field.getDeclaringClass(), setMethodName, MethodType.methodType(void.class, field.getType())),//这个函数接口需要引用的类的实例方法
                                                              MethodType.methodType(void.class, field.getDeclaringClass(), boolean.class))//实际运行的时候，这个函数式接口的方法签名。也就是将泛型的信息补充上
                                                 .getTarget().invoke();
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    static GetFloat buildGetFloat(Field field)
    {
        if (field.getType() != float.class)
        {
            throw new IllegalArgumentException(field.getName() + " is not float");
        }
        MethodHandles.Lookup lookup        = MethodHandles.lookup();
        String               getMethodName = "get" + toMethodName(field);
        try
        {
            return (GetFloat) LambdaMetafactory.metafactory(lookup, //固定参数
                                                            "get",//需要实现的函数式接口的方法名
                                                            MethodType.methodType(GetFloat.class),//固定参数，本方法最终返回的函数式接口的类
                                                            MethodType.methodType(float.class, Object.class),// 函数式接口的方法签名，如果是泛型的，用 Object.class代替
                                                            lookup.findVirtual(field.getDeclaringClass(), getMethodName, MethodType.methodType(float.class)),//这个函数接口需要引用的类的方法
                                                            MethodType.methodType(float.class, field.getDeclaringClass()))//实际运行时候传入的参数类型，也就是泛型信息在运行的时候对应的实际的类型。
                                               .getTarget().invoke();
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    static SetFloat buildSetFloat(Field field)
    {
        if (field.getType() != float.class)
        {
            throw new IllegalArgumentException(field.getName() + " is not float");
        }
        MethodHandles.Lookup lookup        = MethodHandles.lookup();
        String               setMethodName = "set" + toMethodName(field);
        try
        {
            return (SetFloat) LambdaMetafactory.metafactory(lookup, //固定参数
                                                            "set",//需要实现的函数式接口的方法名
                                                            MethodType.methodType(SetFloat.class),////固定写法，中间参数是需要实现的函数接口类
                                                            MethodType.methodType(void.class, Object.class, float.class),// 函数式接口的方法签名
                                                            lookup.findVirtual(field.getDeclaringClass(), setMethodName, MethodType.methodType(void.class, field.getType())),//这个函数接口需要引用的类的实例方法
                                                            MethodType.methodType(void.class, field.getDeclaringClass(), float.class))//实际运行的时候，这个函数式接口的方法签名。也就是将泛型的信息补充上
                                               .getTarget().invoke();
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    static GetLong buildGetLong(Field field)
    {
        if (field.getType() != long.class)
        {
            throw new IllegalArgumentException(field.getName() + " is not long");
        }
        MethodHandles.Lookup lookup        = MethodHandles.lookup();
        String               getMethodName = "get" + toMethodName(field);
        try
        {
            return (GetLong) LambdaMetafactory.metafactory(lookup, //固定参数
                                                           "get",//需要实现的函数式接口的方法名
                                                           MethodType.methodType(GetLong.class),//固定参数，本方法最终返回的函数式接口的类
                                                           MethodType.methodType(long.class, Object.class),// 函数式接口的方法签名，如果是泛型的，用 Object.class代替
                                                           lookup.findVirtual(field.getDeclaringClass(), getMethodName, MethodType.methodType(long.class)),//这个函数接口需要引用的类的方法
                                                           MethodType.methodType(long.class, field.getDeclaringClass()))//实际运行时候传入的参数类型，也就是泛型信息在运行的时候对应的实际的类型。
                                              .getTarget().invoke();
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    static SetLong buildSetLong(Field field)
    {
        if (field.getType() != long.class)
        {
            throw new IllegalArgumentException(field.getName() + " is not long");
        }
        MethodHandles.Lookup lookup        = MethodHandles.lookup();
        String               setMethodName = "set" + toMethodName(field);
        try
        {
            return (SetLong) LambdaMetafactory.metafactory(lookup, //固定参数
                                                           "set",//需要实现的函数式接口的方法名
                                                           MethodType.methodType(SetLong.class),////固定写法，中间参数是需要实现的函数接口类
                                                           MethodType.methodType(void.class, Object.class, long.class),// 函数式接口的方法签名
                                                           lookup.findVirtual(field.getDeclaringClass(), setMethodName, MethodType.methodType(void.class, field.getType())),//这个函数接口需要引用的类的实例方法
                                                           MethodType.methodType(void.class, field.getDeclaringClass(), long.class))//实际运行的时候，这个函数式接口的方法签名。也就是将泛型的信息补充上
                                              .getTarget().invoke();
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    static Function buildGetReference(Field field)
    {
        if (field.getType().isPrimitive())
        {
            throw new IllegalArgumentException(field.getName() + "is not Object。");
        }
        MethodHandles.Lookup lookup        = MethodHandles.lookup();
        String               getMethodName = "get" + toMethodName(field);
        try
        {
            return (Function<Object, Object>) LambdaMetafactory.metafactory(lookup,//
                                                                            "apply",//
                                                                            MethodType.methodType(Function.class),//
                                                                            MethodType.methodType(Object.class, Object.class),//
                                                                            lookup.findVirtual(field.getDeclaringClass(), getMethodName, MethodType.methodType(field.getType())),//
                                                                            MethodType.methodType(field.getType(), field.getDeclaringClass())).getTarget().invoke();
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    static BiConsumer buildSetReference(Field field)
    {
        if (field.getType().isPrimitive())
        {
            throw new IllegalArgumentException(field.getName() + "is not Object。");
        }
        MethodHandles.Lookup lookup        = MethodHandles.lookup();
        String               setMethodName = "set" + toMethodName(field);
        try
        {
            return (BiConsumer<Object, Object>) LambdaMetafactory.metafactory(lookup,//
                                                                              "accept",//
                                                                              MethodType.methodType(BiConsumer.class),//
                                                                              MethodType.methodType(void.class, Object.class, Object.class),//
                                                                              lookup.findVirtual(field.getDeclaringClass(), setMethodName, MethodType.methodType(void.class, field.getType())),//
                                                                              MethodType.methodType(void.class, field.getDeclaringClass(), field.getType())).getTarget().invoke();// )
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    static GetInt buildCompileGetInt(Field field)
    {
        if (field.getType() != int.class)
        {
            throw new IllegalArgumentException(field.getName() + " is not int");
        }
        try
        {
            ClassModel classModel = new ClassModel("GetInt_" + count.incrementAndGet());
            classModel.setFinal(true);
            classModel.addInterface(GetInt.class);
            MethodModel methodModel = new MethodModel(GetInt.class.getDeclaredMethod("get", Object.class), classModel);
            methodModel.setParamterNames("entity");
            String getMethodName = "get" + toMethodName(field);
            methodModel.setBody("return ((" + SmcHelper.getReferenceName(field.getDeclaringClass(), classModel) + ")entity)." + getMethodName + "();");
            classModel.putMethodModel(methodModel);
            methodModel.setFinal(true);
            Class<GetInt> compile = (Class<GetInt>) new CompileHelper(Thread.currentThread().getContextClassLoader()).compile(classModel);
            return compile.getConstructor().newInstance();
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }
}
