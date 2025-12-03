package cc.jfire.baseutil.schedule.trigger;

import java.util.Calendar;

/**
 * 在一天之中的固定时间触发
 */
public class FixDayTimeTrigger extends BaseTrigger
{
    private final int hour;
    private final int minute;
    private final int second;

    public FixDayTimeTrigger(Runnable timetask, int hour, int minute, int second)
    {
        super(timetask);
        this.hour   = hour;
        this.minute = minute;
        this.second = second;
        calNext();
    }

    @Override
    public boolean calNext()
    {
        Calendar now    = Calendar.getInstance();
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
        deadline = target.getTimeInMillis();
        return true;
    }
}
