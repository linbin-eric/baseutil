package cc.jfire.baseutil.schedule.timer;

import cc.jfire.baseutil.schedule.api.Trigger;

import java.util.concurrent.ExecutorService;

public class SimpleWheelTimer extends BaseTimer
{
    private final   Bucket[]        buckets;
    private final   int             mask = 1023;
    private final   ExecutorService pool;
    protected final long            tickDurationMillSeconds;

    public SimpleWheelTimer(ExecutorService pool, long tickDurationMillSeconds)
    {
        super(tickDurationMillSeconds);
        this.pool                    = pool;
        this.tickDurationMillSeconds = tickDurationMillSeconds;
        buckets                      = new Bucket[1024];
        for (int i = 0; i < buckets.length; i++)
        {
            buckets[i] = new Bucket(tickDurationMillSeconds, pool, this);
        }
        new Thread(this, "SimpleWheelTimer").start();
    }

    @Override
    public void run()
    {
        long tickNow = 0;
        while (state == STARTED)
        {
            waitToNextTick(tickNow);
            final Bucket bucket = buckets[(int) (tickNow & mask)];
            pool.execute(() -> bucket.expire());
            tickNow += 1;
        }
    }

    @Override
    public void add(Trigger trigger)
    {
        if (trigger.isCanceled())
        {
            return;
        }
        long left = trigger.deadline() - startTime;
        long posi = left / tickDurationMillSeconds;
        posi = posi <= 0 ? 0 : posi;
        int index = (int) (posi & mask);
        buckets[index].add(trigger);
    }
}
