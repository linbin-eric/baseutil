package com.jfirer.baseutil.schedule.timer;

import com.jfirer.baseutil.schedule.api.Timer;
import com.jfirer.baseutil.schedule.api.Trigger;
import org.jctools.queues.MpscLinkedQueue;
import org.jctools.util.PaddedAtomicLong;

import java.util.Queue;
import java.util.concurrent.ExecutorService;

public class Bucket
{
    private volatile Queue<Trigger>   triggers          = new MpscLinkedQueue<>();
    private volatile Queue<Trigger>   nextRoundTriggers = new MpscLinkedQueue<>();
    protected final  long             tickDuration;
    private final    ExecutorService  executorService;
    private final    Timer            timer;
    private          PaddedAtomicLong ctl               = new PaddedAtomicLong(0);

    public Bucket(long tickDuration, ExecutorService executorService, Timer timer)
    {
        this.tickDuration    = tickDuration;
        this.executorService = executorService;
        this.timer           = timer;
    }

    public void add(Trigger trigger)
    {
        if (trigger.isCanceled())
        {
            return;
        }
        triggers.offer(trigger);
    }

    public void expire()
    {
        long now = ctl.longValue();
        // 如果出现并发冲突的情况，意味着bucket还没有处理完毕的情况就再次被触发了。那么可能是因为内部的expire处理耗时太多。
        // 此时不需要做额外的处理。否则代码会很复杂。这种情况下出现触发不稳是可以理解的。
        if (now == 0 && ctl.compareAndSet(0, 1))
        {
            Queue<Trigger> tmp = triggers;
            triggers          = nextRoundTriggers;
            nextRoundTriggers = tmp;
            Trigger poll;
            long    currentTimeMillis = System.currentTimeMillis();
            while ((poll = nextRoundTriggers.poll()) != null)
            {
                if (poll.deadline() - currentTimeMillis < tickDuration)
                {
                    executorService.submit(poll.attach());
                    if (poll.calNext())
                    {
                        timer.add(poll);
                    }
                }
                else
                {
                    triggers.add(poll);
                }
            }
            ctl.set(0);
        }
    }
}
