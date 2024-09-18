package com.jfirer.baseutil.reflect.valueaccessor;

@FunctionalInterface
public interface GetInt<T>
{
    int get(T obj);
}
