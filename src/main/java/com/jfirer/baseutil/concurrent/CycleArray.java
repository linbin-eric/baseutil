package com.jfirer.baseutil.concurrent;

public interface CycleArray<T>
{
    boolean add(T t);

    T poll();

    void pushBusyWait(T t);

    boolean isEmpty();
}
