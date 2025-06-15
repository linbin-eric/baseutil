package com.jfirer.baseutil.concurrent;

public interface CycleArray<T>
{
    boolean put(T t);

    T take();
}
