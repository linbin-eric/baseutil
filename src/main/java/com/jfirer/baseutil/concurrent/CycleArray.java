package com.jfirer.baseutil.concurrent;

public interface CycleArray<T>
{
    boolean cycAdd(T t);

    T cycTake();
}
