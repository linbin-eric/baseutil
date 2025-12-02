package xin.nb1.baseutil.schedule.trigger;

import xin.nb1.baseutil.schedule.api.Trigger;

public abstract class BaseTrigger implements Trigger
{
    protected final    Runnable task;
    protected volatile boolean  canceled = false;
    protected volatile long     deadline;

    public BaseTrigger(Runnable task)
    {
        this.task = task;
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
    public Runnable attach()
    {
        return task;
    }
}
