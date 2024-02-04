package com.jfirer.baseutil;

import com.jfirer.baseutil.schedule.api.Timer;
import com.jfirer.baseutil.schedule.api.Timetask;
import com.jfirer.baseutil.schedule.handler.SimpleExpireHandler;
import com.jfirer.baseutil.schedule.timer.FixedCapacityWheelTimer;
import com.jfirer.baseutil.schedule.trigger.FixDayTimeTrigger;
import com.jfirer.baseutil.schedule.trigger.RepeatDelayTrigger;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class FixScheduleTest
{
    @Test
    public void test()
    {
        Timer timer = new FixedCapacityWheelTimer(1024, new SimpleExpireHandler(), 1, TimeUnit.MILLISECONDS);
        timer.add(new RepeatDelayTrigger(new Timetask()
        {
            long t0 = System.currentTimeMillis();
            int times = 10;

            @Override
            public void invoke()
            {
                System.out.println(Thread.currentThread().getName() + ":" + (System.currentTimeMillis() - t0));
                t0 = System.currentTimeMillis();
                times--;
            }

            @Override
            public boolean isCanceled()
            {
                return times <= 0;
            }
        }, 1, TimeUnit.SECONDS));
        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(10000));
    }

    @Test
    public void test2()
    {
        Timer timer = new FixedCapacityWheelTimer(16, new SimpleExpireHandler(), 1000, TimeUnit.MILLISECONDS);
        timer.add(new FixDayTimeTrigger(new Timetask()
        {
            long t0 = System.currentTimeMillis();
            int times = 10;

            @Override
            public void invoke()
            {
                System.out.println(System.currentTimeMillis() - t0);
                t0 = System.currentTimeMillis();
            }

            @Override
            public boolean isCanceled()
            {
                return (times--) == 0;
            }
        }, 19, 39, 30));
        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(10));
    }
}
