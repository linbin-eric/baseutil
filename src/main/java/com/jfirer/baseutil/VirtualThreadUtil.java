package com.jfirer.baseutil;

import lombok.SneakyThrows;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;

public class VirtualThreadUtil
{
    @SneakyThrows
    public static void start(Collection<Runnable> runnables, int parallelSize)
    {
        start(runnables, parallelSize, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    @SneakyThrows
    public static void start(Collection<Runnable> runnables, int parallelSize, long timeout, TimeUnit unit)
    {
        CountDownLatch  latch = new CountDownLatch(parallelSize);
        Queue<Runnable> queue = new LinkedTransferQueue<>(runnables);
        for (int i = 0; i < parallelSize; i++)
        {
            Thread.startVirtualThread(() -> {
                try
                {
                    do
                    {
                        Runnable poll = queue.poll();
                        if (poll != null)
                        {
                            poll.run();
                        }
                        else
                        {
                            return;
                        }
                    } while (true);
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
