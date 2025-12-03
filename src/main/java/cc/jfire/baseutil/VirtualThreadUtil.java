package cc.jfire.baseutil;

import lombok.SneakyThrows;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class VirtualThreadUtil
{
    @SneakyThrows
    public static void start(Collection<? extends Runnable> runnables, int parallelSize)
    {
        start(runnables, parallelSize, Long.MAX_VALUE, TimeUnit.MILLISECONDS, r -> {});
    }

    @SneakyThrows
    public static <R extends Runnable> void start(Collection<R> runnables, int parallelSize, Consumer<R> consumer)
    {
        start(runnables, parallelSize, Long.MAX_VALUE, TimeUnit.SECONDS, consumer);
    }

    @SneakyThrows
    public static <R extends Runnable> void start(Collection<R> runnables, int parallelSize, long timeout, TimeUnit unit, Consumer<R> consumer)
    {
        CountDownLatch latch = new CountDownLatch(parallelSize);
        Queue<R>       queue = new LinkedTransferQueue<>(runnables);
        for (int i = 0; i < parallelSize; i++)
        {
            Thread.startVirtualThread(() -> {
                try
                {
                    do
                    {
                        R poll = queue.poll();
                        if (poll != null)
                        {
                            poll.run();
                            consumer.accept(poll);
                        }
                        else
                        {
                            return;
                        }
                    } while (true);
                }
                finally
                {
                    latch.countDown();
                }
            });
        }
        latch.await(timeout, unit);
    }
}
