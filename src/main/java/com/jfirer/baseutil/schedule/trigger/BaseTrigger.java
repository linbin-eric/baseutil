package com.jfirer.baseutil.schedule.trigger;

import com.jfirer.baseutil.schedule.api.Timetask;
import com.jfirer.baseutil.schedule.api.Trigger;

public abstract class BaseTrigger implements Trigger
{
    protected volatile boolean canceled = false;
    protected final Timetask   timetask;
    protected volatile long    deadline;
    
    public BaseTrigger(Timetask timetask)
    {
        this.timetask = timetask;
    }
    
    @Override
    public void cancel()
    {
        canceled = true;
    }
    
    @Override
    public boolean isCanceled()
    {
        return canceled;
    }
    
    @Override
    public long deadline()
    {
        return deadline;
    }
    
    @Override
    public Timetask timetask()
    {
        return timetask;
    }
    
}
