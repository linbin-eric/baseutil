package com.jfirer.baseutil.schedule.timer.bucket.impl;

import com.jfirer.baseutil.schedule.api.ExpireHandler;
import com.jfirer.baseutil.schedule.api.Timer;
import com.jfirer.baseutil.schedule.api.Trigger;
import com.jfirer.baseutil.schedule.timer.bucket.Bucket;
import org.jctools.queues.MpscLinkedQueue;
import org.jctools.util.PaddedAtomicLong;

import java.util.LinkedList;
import java.util.List;

public class BucketImpl implements Bucket
{
    protected final MpscLinkedQueue<Trigger> triggers = new MpscLinkedQueue<>();
    protected final ExpireHandler            expireHandler;
    protected final Timer                    timer;
    protected final long                     tickDuration;
    private         Trigger[]                array    = new Trigger[16];
    private         List<Trigger>            tmpStore = new LinkedList<Trigger>();
    private         PaddedAtomicLong         ctl      = new PaddedAtomicLong(0);

    public BucketImpl(ExpireHandler expireHandler, Timer timer, long tickDuration)
    {
        this.expireHandler = expireHandler;
        this.timer         = timer;
        this.tickDuration  = tickDuration;
    }

    @Override
    public void add(Trigger trigger)
    {
        if (trigger.isCanceled())
        {
            return;
        }
        triggers.offer(trigger);
    }

    @Override
    public void expire()
    {
        long now = ctl.longValue();
        // 如果出现并发冲突的情况，意味着bucket还没有处理完毕的情况就再次被触发了。那么可能是因为内部的expire处理耗时太多。
        // 此时不需要做额外的处理。否则代码会很复杂。这种情况下出现触发不稳是可以理解的。
        if (now == 0 && ctl.compareAndSet(0, 1))
        {
            int drained;
            do
            {
                drained = triggers.drain(trigger -> {
                    if (trigger.isCanceled() || trigger.timetask().isCanceled())
                    {
                        ;
                    }
                    else if ((trigger.deadline() - System.nanoTime()) < tickDuration)
                    {
                        try
                        {
                            //执行触发逻辑，最终会找到timetask并且执行
                            expireHandler.expire(trigger);
                        }
                        catch (Throwable e)
                        {
                            ;
                        }
                        //计算下一次被调用的时间，由于timetask内部是异步线程，导致此时的timetask的isCancel返回false。于是调度器就再次被放到池子里
                        trigger.calNext();
                        if (trigger.isCanceled() == false)
                        {
                            timer.add(trigger);
                        }
                    }
                    else
                    {
                        tmpStore.add(trigger);
                    }
                });
            } while (drained > 0);
            triggers.addAll(tmpStore);
            tmpStore.clear();
            ctl.set(0);
        }
    }
}
