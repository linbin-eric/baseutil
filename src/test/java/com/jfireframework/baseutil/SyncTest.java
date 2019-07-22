package com.jfireframework.baseutil;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import com.jfireframework.baseutil.concurrent.Sync;
import org.junit.Assert;
import org.junit.Test;

public class SyncTest
{
    class ConcurrentObj
    {
        private volatile int           i;
        private volatile boolean       flag = false;
        private          Sync<Integer> sync = new Sync<Integer>() {
                                          
                                          @Override
                                          protected Integer get()
                                          {
                                              if (flag == false)
                                              {
                                                  return null;
                                              }
                                              else
                                              {
                                                  i += 1;
                                                  flag = false;
                                                  return i;
                                              }
                                          }
                                      };
        
        public void add()
        {
            while (flag)
                ;
            flag = true;
            sync.signal();
        }
        
        public int take()
        {
            return sync.take();
        }
        
        public int value()
        {
            return i;
        }
    }
    
    @Test
    public void test2() throws InterruptedException
    {
        final ConcurrentObj obj = new ConcurrentObj();
        final int sum = 1000000;
        int consumerSum = 3;
        for (int i = 0; i < consumerSum; i++)
        {
            new Thread(new Runnable() {
                
                @Override
                public void run()
                {
                    for (int i = 0; i < sum; i++)
                    {
                        obj.take();
                    }
                }
            }).start();
        }
        Thread t = new Thread(new Runnable() {
            
            @Override
            public void run()
            {
                for (int i = 0; i < sum; i++)
                {
                    obj.add();
                }
            }
        });
        t.start();
        t.join();
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(100));
        Assert.assertEquals(sum, obj.value());
    }
    
}
