package com.jfirer.baseutil.schedule.trigger;

import java.util.concurrent.TimeUnit;

public class OnceDelayTrigger extends BaseTrigger
{
    public OnceDelayTrigger(Runnable timetask, long delay, TimeUnit unit)
    {
        super(timetask);
        deadline = System.currentTimeMillis() + unit.toMillis(delay);
    }

    @Override
    public long deadline()
    {
        return deadline;
    }

    @Override
    public boolean calNext()
    {
        return false;
    }
}
