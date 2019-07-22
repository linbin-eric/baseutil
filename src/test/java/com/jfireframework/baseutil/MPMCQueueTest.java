package com.jfireframework.baseutil;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import com.jfireframework.baseutil.concurrent.MPMCQueue;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by linbin on 2016/9/19.
 */
public class MPMCQueueTest
{
    
    @Test
    public void test2() throws InterruptedException
    {
        final MPMCQueue<String>                 queue          = new MPMCQueue<String>(false);
        final ConcurrentHashMap<String, String> set            = new ConcurrentHashMap<String, String>(15000);
        final int                               count          = 10000;
        final int                               countThreadSum = 5;
        final CountDownLatch                    latch          = new CountDownLatch(countThreadSum);
        for (int i = 0; i < countThreadSum; i++)
        {
            Thread countThread = new Thread(new Runnable() {
                @Override
                public void run()
                {
                    for (int i = 0; i < count; i++)
                    {
                        String result = queue.take();
                        if (set.putIfAbsent(result, "") != null)
                        {
                            System.err.println("错误");
                        }
                    }
                    latch.countDown();
                }
            }, "take-" + i);
            countThread.start();
        }
        for (int i = 0; i < countThreadSum; i++)
        {
            final String name = "thread" + i;
            Thread thread = new Thread(new Runnable() {
                
                @Override
                public void run()
                {
                    for (int i = 0; i < count; i++)
                    {
                        queue.offerAndSignal(name + i);
                    }
                }
            }, "insert-");
            thread.start();
        }
        latch.await();
        Assert.assertTrue(queue.isEmpty());
    }
    
    @Test
    public void test() throws InterruptedException
    {
        final MPMCQueue<Integer> queue = new MPMCQueue<Integer>();
        final int count = 1000000;
        final int threadSum = 4;
        final int countThreadSum = 3;
        for (int i = 0; i < threadSum; i++)
        {
            new Thread(new Runnable() {
                @Override
                public void run()
                {
                    for (int i = 0; i < count; i++)
                    {
                        queue.offerAndSignal(i);
                    }
                    System.out.println(Thread.currentThread().getName() + "输出完毕");
                }
            }, "insert-" + i).start();
        }
        final CountDownLatch latch = new CountDownLatch(count * threadSum);
        for (int i = 0; i < countThreadSum; i++)
        {
            Thread countThread = new Thread(new Runnable() {
                @Override
                public void run()
                {
                    int sum = count * threadSum;
                    for (int i = 0; i < sum; i++)
                    {
                        queue.take();
                        latch.countDown();
                    }
                    if (queue.poll() == null)
                    {
                        System.out.println("完成");
                    }
                    else
                    {
                        System.err.println("异常");
                    }
                }
            }, "take-" + i);
            countThread.start();
        }
        latch.await();
        System.out.println("完成");
    }
}
