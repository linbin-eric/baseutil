package com.jfirer.baseutil.reflect.valueaccessor.impl;

import com.jfirer.baseutil.reflect.valueaccessor.ValueAccessor;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.jfirer.baseutil.reflect.ReflectUtil.*;

public class LambdaValueAccessor implements ValueAccessor
{
    @FunctionalInterface
    public interface GetBoolean
    {
        boolean get(Object obj);
    }

    @FunctionalInterface
    public interface SetBoolean
    {
        void set(Object obj, boolean z);
    }

    @FunctionalInterface
    public interface GetByte
    {
        byte get(Object obj);
    }

    @FunctionalInterface
    public interface SetByte
    {
        void set(Object obj, byte b);
    }

    @FunctionalInterface
    public interface GetChar
    {
        char get(Object obj);
    }

    @FunctionalInterface
    public interface SetChar
    {
        void set(Object obj, char c);
    }

    @FunctionalInterface
    public interface GetShort
    {
        short get(Object obj);
    }

    @FunctionalInterface
    public interface SetShort
    {
        void set(Object obj, short s);
    }

    @FunctionalInterface
    public interface GetInt
    {
        int get(Object obj);
    }

    @FunctionalInterface
    public interface SetInt
    {
        void set(Object obj, int i);
    }

    @FunctionalInterface
    public interface GetLong
    {
        long get(Object obj);
    }

    @FunctionalInterface
    public interface SetLong
    {
        void set(Object obj, long l);
    }

    @FunctionalInterface
    public interface GetFloat
    {
        float get(Object obj);
    }

    @FunctionalInterface
    public interface SetFloat
    {
        void set(Object obj, float f);
    }

    @FunctionalInterface
    public interface GetDouble
    {
        double get(Object obj);
    }

    @FunctionalInterface
    public interface SetDouble
    {
        void set(Object obj, double d);
    }

    private Field                      field;
    private GetBoolean                 getBoolean;
    private SetBoolean                 setBoolean;
    private GetByte                    getByte;
    private SetByte                    setByte;
    private GetChar                    getChar;
    private SetChar                    setChar;
    private GetShort                   getShort;
    private SetShort                   setShort;
    private GetInt                     getInt;
    private SetInt                     setInt;
    private GetLong                    getLong;
    private SetLong                    setLong;
    private GetFloat                   getFloat;
    private SetFloat                   setFloat;
    private GetDouble                  getDouble;
    private SetDouble                  setDouble;
    private Function<Object, Object>   getObj;
    private BiConsumer<Object, Object> setObj;

    public LambdaValueAccessor(Field field)
    {
        try
        {
            String               getMethodName    = field.getType() == boolean.class ? "is" + ValueAccessor.toMethodName(field) : "get" + ValueAccessor.toMethodName(field);
            String               setMethodName    = "set" + ValueAccessor.toMethodName(field);
            MethodHandles.Lookup lookup           = MethodHandles.lookup();
            MethodHandle         getMethodHandler = lookup.findVirtual(field.getDeclaringClass(), getMethodName, MethodType.methodType(field.getType()));
            MethodHandle         setMethodHandler = lookup.findVirtual(field.getDeclaringClass(), setMethodName, MethodType.methodType(void.class, field.getType()));
            int                  classId          = getClassId(field.getType());
            switch (classId)
            {
                case PRIMITIVE_INT ->
                {
                    getInt = (GetInt) LambdaMetafactory.metafactory(lookup, //固定参数
                                                                    "get",//需要实现的函数式接口的方法名
                                                                    MethodType.methodType(GetInt.class),//固定参数，本方法最终返回的函数式接口的类
                                                                    MethodType.methodType(int.class, Object.class),// 函数式接口的方法签名，如果是泛型的，用 Object.class代替
                                                                    getMethodHandler,//这个函数接口需要引用的类的方法
                                                                    MethodType.methodType(int.class, field.getDeclaringClass()))//实际运行时候传入的参数类型，也就是泛型信息在运行的时候对应的实际的类型。
                                                       .getTarget().invoke();
                    setInt = (SetInt) LambdaMetafactory.metafactory(lookup, //固定参数
                                                                    "set",//需要实现的函数式接口的方法名
                                                                    MethodType.methodType(SetInt.class),////固定写法，中间参数是需要实现的函数接口类
                                                                    MethodType.methodType(void.class, Object.class, int.class),// 函数式接口的方法签名
                                                                    setMethodHandler,//这个函数接口需要引用的类的实例方法
                                                                    MethodType.methodType(void.class, field.getDeclaringClass(), int.class))//实际运行的时候，这个函数式接口的方法签名。也就是将泛型的信息补充上
                                                       .getTarget().invoke();
                }
                case PRIMITIVE_BYTE ->
                {
                    getByte = (GetByte) LambdaMetafactory.metafactory(lookup, //固定参数
                                                                      "get",//需要实现的函数式接口的方法名
                                                                      MethodType.methodType(GetByte.class),//固定参数，本方法最终返回的函数式接口的类
                                                                      MethodType.methodType(byte.class, Object.class),// 函数式接口的方法签名，如果是泛型的，用 Object.class代替
                                                                      getMethodHandler,//这个函数接口需要引用的类的方法
                                                                      MethodType.methodType(byte.class, field.getDeclaringClass()))//
                                                         .getTarget().invoke();
                    setByte = (SetByte) LambdaMetafactory.metafactory(lookup, //固定参数
                                                                      "set",//需要实现的函数式接口的方法名
                                                                      MethodType.methodType(SetByte.class),////固定写法，中间参数是需要实现的函数接口类
                                                                      MethodType.methodType(void.class, Object.class, byte.class),// 函数式接口的方法签名
                                                                      setMethodHandler,//这个函数接口需要引用的类的实例方法
                                                                      MethodType.methodType(void.class, field.getDeclaringClass(), byte.class))//
                                                         .getTarget().invoke();
                }
                case PRIMITIVE_SHORT ->
                {
                    getShort = (GetShort) LambdaMetafactory.metafactory(lookup, //固定参数
                                                                        "get",//需要实现的函数式接口的方法名
                                                                        MethodType.methodType(GetShort.class),//固定参数，本方法最终返回的函数式接口的类
                                                                        MethodType.methodType(short.class, Object.class),// 函数式接口的方法签名，如果是泛型的，用 Object.class代替
                                                                        getMethodHandler,//这个函数接口需要引用的类的方法
                                                                        MethodType.methodType(short.class, field.getDeclaringClass()))//
                                                           .getTarget().invoke();
                    setShort = (SetShort) LambdaMetafactory.metafactory(lookup, //固定参数
                                                                        "set",//需要实现的函数式接口的方法名
                                                                        MethodType.methodType(SetShort.class),////固定写法，中间参数是需要实现的函数接口类
                                                                        MethodType.methodType(void.class, Object.class, short.class),// 函数式接口的方法签名
                                                                        setMethodHandler,//这个函数接口需要引用的类的实例方法
                                                                        MethodType.methodType(void.class, field.getDeclaringClass(), short.class))//
                                                           .getTarget().invoke();
                }
                case PRIMITIVE_CHAR ->
                {
                    getChar = (GetChar) LambdaMetafactory.metafactory(lookup, //固定参数
                                                                      "get",//需要实现的函数式接口的方法名
                                                                      MethodType.methodType(GetChar.class),//固定参数，本方法最终返回的函数式接口的类
                                                                      MethodType.methodType(char.class, Object.class),// 函数式接口的方法签名，如果是泛型的，用 Object.class代替
                                                                      getMethodHandler,//这个函数接口需要引用的类的方法
                                                                      MethodType.methodType(char.class, field.getDeclaringClass()))//
                                                         .getTarget().invoke();
                    setChar = (SetChar) LambdaMetafactory.metafactory(lookup, //固定参数
                                                                      "set",//需要实现的函数式接口的方法名
                                                                      MethodType.methodType(SetChar.class),////固定写法，中间参数是需要实现的函数接口类
                                                                      MethodType.methodType(void.class, Object.class, char.class),// 函数式接口的方法签名
                                                                      setMethodHandler,//这个函数接口需要引用的类的实例方法
                                                                      MethodType.methodType(void.class, field.getDeclaringClass(), char.class))//
                                                         .getTarget().invoke();
                }
                case PRIMITIVE_FLOAT ->
                {
                    getFloat = (GetFloat) LambdaMetafactory.metafactory(lookup, //固定参数
                                                                        "get",//需要实现的函数式接口的方法名
                                                                        MethodType.methodType(GetFloat.class),//固定参数，本方法最终返回的函数式接口的类
                                                                        MethodType.methodType(float.class, Object.class),// 函数式接口的方法签名，如果是泛型的，用 Object.class代替
                                                                        getMethodHandler,//这个函数接口需要引用的类的方法
                                                                        MethodType.methodType(float.class, field.getDeclaringClass()))//
                                                           .getTarget().invoke();
                    setFloat = (SetFloat) LambdaMetafactory.metafactory(lookup, //固定参数
                                                                        "set",//需要实现的函数式接口的方法名
                                                                        MethodType.methodType(SetFloat.class),////固定写法，中间参数是需要实现的函数接口类
                                                                        MethodType.methodType(void.class, Object.class, float.class),// 函数式接口的方法签名
                                                                        setMethodHandler,//这个函数接口需要引用的类的实例方法
                                                                        MethodType.methodType(void.class, field.getDeclaringClass(), float.class))//
                                                           .getTarget().invoke();
                }
                case PRIMITIVE_DOUBLE ->
                {
                    getDouble = (GetDouble) LambdaMetafactory.metafactory(lookup, //固定参数
                                                                          "get",//需要实现的函数式接口的方法名
                                                                          MethodType.methodType(GetDouble.class),//固定参数，本方法最终返回的函数式接口的类
                                                                          MethodType.methodType(double.class, Object.class),// 函数式接口的方法签名，如果是泛型的，用 Object.class代替
                                                                          getMethodHandler,//这个函数接口需要引用的类的方法
                                                                          MethodType.methodType(double.class, field.getDeclaringClass()))//
                                                             .getTarget().invoke();
                    setDouble = (SetDouble) LambdaMetafactory.metafactory(lookup, //固定参数
                                                                          "set",//需要实现的函数式接口的方法名
                                                                          MethodType.methodType(SetDouble.class),////固定写法，中间参数是需要实现的函数接口类
                                                                          MethodType.methodType(void.class, Object.class, double.class),// 函数式接口的方法签名
                                                                          setMethodHandler,//这个函数接口需要引用的类的实例方法
                                                                          MethodType.methodType(void.class, field.getDeclaringClass(), double.class))//
                                                             .getTarget().invoke();
                }
                case PRIMITIVE_LONG ->
                {
                    getLong = (GetLong) LambdaMetafactory.metafactory(lookup, //固定参数
                                                                      "get",//需要实现的函数式接口的方法名
                                                                      MethodType.methodType(GetLong.class),//固定参数，本方法最终返回的函数式接口的类
                                                                      MethodType.methodType(long.class, Object.class),// 函数式接口的方法签名，如果是泛型的，用 Object.class代替
                                                                      getMethodHandler,//这个函数接口需要引用的类的方法
                                                                      MethodType.methodType(long.class, field.getDeclaringClass()))//
                                                         .getTarget().invoke();
                    setLong = (SetLong) LambdaMetafactory.metafactory(lookup, //固定参数
                                                                      "set",//需要实现的函数式接口的方法名
                                                                      MethodType.methodType(SetLong.class),////固定写法，中间参数是需要实现的函数接口类
                                                                      MethodType.methodType(void.class, Object.class, long.class),// 函数式接口的方法签名
                                                                      setMethodHandler,//这个函数接口需要引用的类的实例方法
                                                                      MethodType.methodType(void.class, field.getDeclaringClass(), long.class))//
                                                         .getTarget().invoke();
                }
                case PRIMITIVE_BOOL ->
                {
                    getBoolean = (GetBoolean) LambdaMetafactory.metafactory(lookup, //固定参数
                                                                            "get",//需要实现的函数式接口的方法名
                                                                            MethodType.methodType(GetBoolean.class),//固定参数，本方法最终返回的函数式接口的类
                                                                            MethodType.methodType(boolean.class, Object.class),// 函数式接口的方法签名，如果是泛型的，用 Object.class代替
                                                                            getMethodHandler,//这个函数接口需要引用的类的方法
                                                                            MethodType.methodType(boolean.class, field.getDeclaringClass()))//
                                                               .getTarget().invoke();
                    setBoolean = (SetBoolean) LambdaMetafactory.metafactory(lookup, //固定参数
                                                                            "set",//需要实现的函数式接口的方法名
                                                                            MethodType.methodType(SetBoolean.class),////固定写法，中间参数是需要实现的函数接口类
                                                                            MethodType.methodType(void.class, Object.class, boolean.class),// 函数式接口的方法签名
                                                                            setMethodHandler,//这个函数接口需要引用的类的实例方法
                                                                            MethodType.methodType(void.class, field.getDeclaringClass(), boolean.class))//
                                                               .getTarget().invoke();
                }
                default ->
                {
                    getObj = (Function<Object, Object>) LambdaMetafactory.metafactory(lookup,//
                                                                                      "apply",//
                                                                                      MethodType.methodType(Function.class),//
                                                                                      MethodType.methodType(Object.class, Object.class),//
                                                                                      getMethodHandler,//
                                                                                      MethodType.methodType(field.getType(), field.getDeclaringClass())).getTarget().invoke();
                    setObj = (BiConsumer<Object, Object>) LambdaMetafactory.metafactory(lookup,//
                                                                                        "accept",//
                                                                                        MethodType.methodType(BiConsumer.class),//
                                                                                        MethodType.methodType(void.class, Object.class, Object.class),//
                                                                                        setMethodHandler,//
                                                                                        MethodType.methodType(void.class, field.getDeclaringClass(), field.getType())).getTarget().invoke();// )
                }
            }
        }
        catch (Throwable e)
        {
            throwException(e);
        }
    }

    @Override
    public int getInt(Object entity)
    {
        return getInt.get(entity);
    }

    @Override
    public long getLong(Object entity)
    {
        return getLong.get(entity);
    }

    @Override
    public byte getByte(Object entity)
    {
        return getByte.get(entity);
    }

    @Override
    public short getShort(Object entity)
    {
        return getShort.get(entity);
    }

    @Override
    public float getFloat(Object entity)
    {
        return getFloat.get(entity);
    }

    @Override
    public double getDouble(Object entity)
    {
        return getDouble.get(entity);
    }

    @Override
    public boolean getBoolean(Object entity)
    {
        return getBoolean.get(entity);
    }

    @Override
    public char getChar(Object entity)
    {
        return getChar.get(entity);
    }

    @Override
    public void set(Object entity, int value)
    {
        setInt.set(entity, value);
    }

    @Override
    public void set(Object entity, byte value)
    {
        setByte.set(entity, value);
    }

    @Override
    public void set(Object entity, char value)
    {
        setChar.set(entity, value);
    }

    @Override
    public void set(Object entity, long value)
    {
        setLong.set(entity, value);
    }

    @Override
    public void set(Object entity, float value)
    {
        setFloat.set(entity, value);
    }
}
