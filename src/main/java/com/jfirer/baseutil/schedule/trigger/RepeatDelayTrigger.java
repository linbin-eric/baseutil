package com.jfirer.baseutil.schedule.trigger;

import java.util.concurrent.TimeUnit;
import com.jfirer.baseutil.schedule.api.Timetask;

public class RepeatDelayTrigger extends BaseTrigger
{
    private final long delay;
    
    public RepeatDelayTrigger(Timetask timetask, long delay, TimeUnit unit)
    {
        super(timetask);
        this.delay = unit.toNanos(delay);
        calNext();
    }
    
    @Override
    public long deadline()
    {
        return deadline;
    }
    
    @Override
    public void calNext()
    {
        if (timetask.isCanceled() == false)
        {
            deadline = System.nanoTime() + delay;
        }
        else
        {
            cancel();
        }
    }
    
}
