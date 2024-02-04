package com.jfirer.baseutil.schedule.trigger;

import java.util.concurrent.TimeUnit;
import com.jfirer.baseutil.schedule.api.Timetask;

/**
 * 指定每一次触发间隔的触发器
 * 
 * @author 林斌
 *
 */
public class RelaxTrigger extends BaseTrigger
{
    private final int[] relaxTimes;
    private int         index = 0;
    
    public RelaxTrigger(Timetask timetask, int[] relaxTimes)
    {
        super(timetask);
        this.relaxTimes = relaxTimes;
        calNext();
    }
    
    @Override
    public void calNext()
    {
        if (timetask.isCanceled() == false)
        {
            if (index == relaxTimes.length)
            {
                if (timetask.isCanceled())
                {
                    cancel();
                    return;
                }
                index -= 1;
            }
            deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(relaxTimes[index]);
            index += 1;
        }
        else
        {
            cancel();
        }
    }
    
    @Override
    public void cancel()
    {
        super.cancel();
    }
}
