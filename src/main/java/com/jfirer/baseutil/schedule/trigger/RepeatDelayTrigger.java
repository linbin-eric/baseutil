package com.jfirer.baseutil.schedule.trigger;

import java.util.concurrent.TimeUnit;

public class RepeatDelayTrigger extends BaseTrigger
{
    private final long delay;

    public RepeatDelayTrigger(Runnable timetask, long delay, TimeUnit unit)
    {
        super(timetask);
        this.delay = unit.toMillis(delay);
        calNext();
    }

    @Override
    public long deadline()
    {
        return deadline;
    }

    @Override
    public boolean calNext()
    {
        deadline = System.currentTimeMillis() + delay;
        return true;
    }
}
