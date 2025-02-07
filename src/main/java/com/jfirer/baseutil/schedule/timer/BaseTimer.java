package com.jfirer.baseutil.schedule.timer;

import com.jfirer.baseutil.schedule.api.Timer;

import java.util.concurrent.locks.LockSupport;

public abstract class BaseTimer implements Timer
{
    public static final int  STARTED     = 1;
    public static final int  termination = -1;
    protected volatile  int  state       = STARTED;
    protected final     long startTime   = System.currentTimeMillis();
    protected final     long durationMillSeconds;

    public BaseTimer(long durationMillSeconds)
    {
        this.durationMillSeconds = durationMillSeconds;
    }

    /**
     * 要使用这种方式等待的原因很简单。因为系统的等待并不是完美的按照约定的时间进行，有可能会比我们的参数时间要多一些。
     * 而且其余的代码执行也会有时间上的损耗。所以每次都要计算是否确实可以等待
     */
    protected void waitToNextTick(long tick)
    {
        long deadline = (tick + 1) * durationMillSeconds + startTime;
        long now      = System.currentTimeMillis();
        long left;
        if ((left = deadline - now) < 0)
        {
            ;
        }
        else
        {
            LockSupport.parkNanos(left * 1000000);
        }
    }

    @Override
    public void stop()
    {
        state = termination;
    }
}
