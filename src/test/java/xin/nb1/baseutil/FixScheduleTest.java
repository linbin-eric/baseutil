package xin.nb1.baseutil;

import xin.nb1.baseutil.schedule.api.Timer;
import xin.nb1.baseutil.schedule.timer.SimpleWheelTimer;
import xin.nb1.baseutil.schedule.trigger.RepeatDelayTrigger;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class FixScheduleTest
{
    @Test
    @Ignore
    public void test()
    {
        Timer timer = new SimpleWheelTimer(Executors.newCachedThreadPool(), 100);
        System.out.println("开始");
        timer.add(new RepeatDelayTrigger(new Runnable()
        {
            long t0    = System.currentTimeMillis();
            int  times = 10;

            @Override
            public void run()
            {
                System.out.println(Thread.currentThread().getName() + ":" + (System.currentTimeMillis() - t0));
                t0 = System.currentTimeMillis();
                times--;
            }
        }, 3, TimeUnit.SECONDS));
        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(100));
    }
}
