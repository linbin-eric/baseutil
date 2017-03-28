package com.jfireframework.baseutil;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.jfireframework.baseutil.concurrent.MPSCQueue;
import com.jfireframework.baseutil.concurrent.SpscQueue;

public class MpScTest
{
    Set<String>                           set       = new HashSet<String>();
    int                                   count     = 100000;
    private ConcurrentLinkedQueue<String> source    = new ConcurrentLinkedQueue<String>();
    int                                   threadNum = 3;
    AtomicInteger                         sum       = new AtomicInteger(0);
    CountDownLatch                        latch     = new CountDownLatch(1);
    Queue<String>                         testQueue = new MPSCQueue<String>();
    
    @Before
    public void before()
    {
        for (int i = 0; i < count; i++)
        {
            source.offer(String.valueOf(i));
        }
    }
    
    @Test
    public void test()
    {
        new Thread(new Runnable() {
            
            @Override
            public void run()
            {
                int total = 0;
                do
                {
                    String value = testQueue.poll();
                    if (value != null && ((set.add(value) == false) || (total += 1) == count))
                    {
                        break;
                    }
                } while (true);
                latch.countDown();
            }
        }).start();
        for (int i = 0; i < threadNum; i++)
        {
            new Thread(new Runnable() {
                
                @Override
                public void run()
                {
                    do
                    {
                        String value = source.poll();
                        if (value != null)
                        {
                            testQueue.offer(value);
                        }
                        else
                        {
                            break;
                        }
                    } while (true);
                }
            }).start();
        }
        try
        {
            latch.await();
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Assert.assertEquals(count, set.size());
    }
}
