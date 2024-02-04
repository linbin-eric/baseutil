package com.jfirer.baseutil.schedule.timer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.jfirer.baseutil.Verify;
import com.jfirer.baseutil.schedule.api.ExpireHandler;
import com.jfirer.baseutil.schedule.api.Trigger;
import com.jfirer.baseutil.schedule.timer.bucket.Bucket;
import com.jfirer.baseutil.schedule.timer.bucket.impl.BucketImpl;

public class FixedCapacityWheelTimer extends BaseTimer
{
    private final Bucket[]        buckets;
    private final int             mask;
    private final ExecutorService pool;
    protected final long          tickDuration;
    
    public FixedCapacityWheelTimer(int tickCount, ExpireHandler expireHandler, ExecutorService pool, long tickDuration, TimeUnit unit)
    {
        super(expireHandler, tickDuration, unit);
        this.tickDuration = unit.toNanos(tickDuration);
        this.pool = pool;
        int tmp = 1;
        while (tmp < tickCount && tmp > 0)
        {
            tmp = tmp << 1;
        }
        Verify.True(tmp > 0, "please check the tickCount. It is too large");
        tickCount = tmp;
        mask = tickCount - 1;
        buckets = new Bucket[tickCount];
        for (int i = 0; i < buckets.length; i++)
        {
            buckets[i] = new BucketImpl(expireHandler, this, this.tickDuration);
        }
        new Thread(this, "FixedCapacityWheelTimer").start();
    }
    
    public FixedCapacityWheelTimer(int tickCount, ExpireHandler expireHandler, long tickDuration, TimeUnit unit)
    {
        this(tickCount, expireHandler, Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 4, new ThreadFactory() {
            int i = 1;
            
            @Override
            public Thread newThread(Runnable r)
            {
                Thread thread = new Thread(r, "timer-thread-" + i);
                i++;
                return thread;
            }
        }), tickDuration, unit);
    }
    
    @Override
    public void run()
    {
        long tickNow = 0;
        while (state == STARTED)
        {
            waitToNextTick(tickNow);
            final Bucket bucket = buckets[(int) (tickNow & mask)];
            pool.execute(new Runnable() {
                @Override
                public void run()
                {
                    bucket.expire();
                }
            });
            tickNow += 1;
        }
        
    }
    
    @Override
    public void add(Trigger trigger)
    {
        if (trigger.isCanceled())
        {
            return;
        }
        long left = trigger.deadline() - startTime;
        long posi = left / tickDuration;
        posi = posi <= 0 ? 0 : posi;
        int index = (int) (posi & mask);
        buckets[index].add(trigger);
    }
    
}
