package com.jfirer.baseutil.schedule.handler;

import com.jfirer.baseutil.schedule.api.ExpireHandler;
import com.jfirer.baseutil.schedule.api.Trigger;

public class SimpleExpireHandler implements ExpireHandler
{

    @Override
    public void expire(Trigger trigger)
    {
        trigger.timetask().invoke();
    }
}
