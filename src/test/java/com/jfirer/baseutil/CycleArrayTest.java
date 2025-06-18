package com.jfirer.baseutil;

import com.jfirer.baseutil.concurrent.CycleArray;
import com.jfirer.baseutil.concurrent.RoundReadCycleArray;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class CycleArrayTest
{
    private          CycleArray<AtomicInteger> cycleArray = new RoundReadCycleArray<>(1024);
    private volatile boolean                   pass       = true;

    public AtomicInteger fetch()
    {
        AtomicInteger take = (AtomicInteger) cycleArray.poll();
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

    public boolean add(AtomicInteger take)
    {
        take.decrementAndGet();
        return cycleArray.add(take);
    }

    @SneakyThrows
    @Test
    public void test()
    {
        int            numThread = 10;
        CountDownLatch latch     = new CountDownLatch(numThread);
        for (int j = 0; j < numThread; j++)
        {
            Thread.startVirtualThread(() -> {
                try
                {
                    for (int i = 0; i < 100; i++)
                    {
                        AtomicInteger fetch = fetch();

                        add(fetch);
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
