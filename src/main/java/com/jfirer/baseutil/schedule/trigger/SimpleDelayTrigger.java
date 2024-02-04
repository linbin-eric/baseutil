package com.jfirer.baseutil.schedule.trigger;

import java.util.concurrent.TimeUnit;
import com.jfirer.baseutil.schedule.api.Timetask;

public class SimpleDelayTrigger extends BaseTrigger
{
    
    public SimpleDelayTrigger(Timetask timetask, long delay, TimeUnit unit)
    {
        super(timetask);
        deadline = System.nanoTime() + unit.toNanos(delay);
    }
    
    @Override
    public long deadline()
    {
        return deadline;
    }
    
    @Override
    public void calNext()
    {
        cancel();
    }
    
}
