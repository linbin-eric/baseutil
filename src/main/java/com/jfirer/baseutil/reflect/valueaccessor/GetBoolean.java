package com.jfirer.baseutil.reflect.valueaccessor;

@FunctionalInterface
public interface GetBoolean<T>
{
    boolean get(T obj);
}
