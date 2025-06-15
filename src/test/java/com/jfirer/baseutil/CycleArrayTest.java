package com.jfirer.baseutil;

import com.jfirer.baseutil.concurrent.CycleArray;
import com.jfirer.baseutil.concurrent.StrictReadCycleArray;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class CycleArrayTest
{
    private          CycleArray<AtomicInteger> cycleArray = new StrictReadCycleArray(16);
    private volatile boolean                   pass       = true;

    public AtomicInteger fetch()
    {
        AtomicInteger take = (AtomicInteger) cycleArray.take();
        if (take == null)
        {
            take = new AtomicInteger();
            take.incrementAndGet();
        }
        else
        {
            if (take.incrementAndGet() != 1)
            {
                pass = false;
                throw new IllegalStateException("current:" + take.get());
//                throw new IllegalStateException();
            }
        }
        return take;
    }

    public boolean put(AtomicInteger take)
    {
        take.decrementAndGet();
        return cycleArray.put(take);
    }

    @SneakyThrows
    @Test
    public void test()
    {
        int            numThread = 20;
        CountDownLatch latch     = new CountDownLatch(numThread);
        for (int j = 0; j < numThread; j++)
        {
            Thread.startVirtualThread(() -> {
                try
                {
                    for (int i = 0; i < 1000000; i++)
                    {
                        AtomicInteger fetch = fetch();

                        put(fetch);
                    }
                }
                catch (Throwable e)
                {
                    e.printStackTrace();
                }
                latch.countDown();
            });
        }
        latch.await();
        Assert.assertTrue(pass);
    }
}
