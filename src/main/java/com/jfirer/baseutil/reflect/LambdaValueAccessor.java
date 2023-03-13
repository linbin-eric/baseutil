package com.jfirer.baseutil.reflect;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;

public class LambdaValueAccessor extends ValueAccessor
{
    interface IntGet<R>
    {
        int apply(R r);
    }

    private IntGet intGet;

    public LambdaValueAccessor(Field field)
    {
        super(field);
        try
        {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            switch (primitiveType)
            {
                case INT, BYTE, SHORT ->
                {
                    String       mehtodName   = "get" + field.getName().toUpperCase().charAt(0) + field.getName().substring(1);
                    MethodHandle methodHandle = lookup.findVirtual(field.getDeclaringClass(), mehtodName, MethodType.methodType(field.getType()));
                    intGet = (IntGet) LambdaMetafactory.metafactory(lookup, "apply", MethodType.methodType(IntGet.class), MethodType.methodType(int.class, Object.class), methodHandle, MethodType.methodType(int.class, field.getDeclaringClass())).getTarget().invoke();
                }
            }
        }
        catch (Throwable e)
        {
            ReflectUtil.throwException(e);
        }
    }

    @Override
    public int getInt(Object entity)
    {
        return intGet.apply(entity);
    }
}
