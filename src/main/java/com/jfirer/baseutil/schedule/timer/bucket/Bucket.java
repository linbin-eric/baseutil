package com.jfirer.baseutil.schedule.timer.bucket;

import com.jfirer.baseutil.schedule.api.Trigger;

public interface Bucket
{
    void add(Trigger trigger);
    
    void expire();
}
