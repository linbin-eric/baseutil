package com.jfirer.baseutil.schedule.trigger;

import java.util.Calendar;
import com.jfirer.baseutil.schedule.api.Timetask;

public class FixDayTimeTrigger extends BaseTrigger
{
    private final int hour;
    private final int minute;
    private final int second;
    
    public FixDayTimeTrigger(Timetask timetask, int hour, int minute, int second)
    {
        super(timetask);
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        calNext();
    }
    
    @Override
    public void calNext()
    {
        if (timetask.isCanceled() == false)
        {
            Calendar now = Calendar.getInstance();
            Calendar target = Calendar.getInstance();
            target.set(Calendar.HOUR_OF_DAY, hour);
            target.set(Calendar.MINUTE, minute);
            target.set(Calendar.SECOND, second);
            if (target.after(now) == false)
            {
                target.add(Calendar.DAY_OF_YEAR, 1);
            }
            else
            {
                ;
            }
            deadline = (target.getTimeInMillis() - System.currentTimeMillis()) * 1000000 + System.nanoTime();
        }
        else
        {
            cancel();
        }
    }
    
}
