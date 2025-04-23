package com.jfirer.baseutil;

import lombok.SneakyThrows;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class VirtualThreadUtil
{
    @SneakyThrows
    public static void start(List<Runnable> runnables)
    {
        CountDownLatch latch = new CountDownLatch(runnables.size());
        for (Runnable runnable : runnables)
        {
            Thread.startVirtualThread(() -> {
                try
                {
                    runnable.run();
                }
                finally
                {
                    latch.countDown();
                }
            });
        }
        latch.await();
    }

    @SneakyThrows
    public static void start(List<Runnable> runnables, long timeout, TimeUnit unit)
    {
        CountDownLatch latch = new CountDownLatch(runnables.size());
        for (Runnable runnable : runnables)
        {
            Thread.startVirtualThread(() -> {
                try
                {
                    runnable.run();
                }
                finally
                {
                    latch.countDown();
                }
            });
        }
        latch.await(timeout, unit);
    }
}
