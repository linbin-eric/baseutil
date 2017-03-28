package com.jfireframework.baseutil;

import java.util.Queue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.LinkedTransferQueue;
import org.junit.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import com.jfireframework.baseutil.concurrent.MPMCQueue;

@State(value = Scope.Benchmark)
public class NewBenchmark
{
    
    Queue<String> queue = new LinkedTransferQueue<String>();
    
    @Setup
    public void before()
    {
        for (int i = 0; i < 3638232; i++)
        {
            queue.offer("nihao");
        }
    }
    
    @Benchmark
    public void test() throws InterruptedException, BrokenBarrierException
    {
        queue.poll();
    }
    
    @Test
    public void test1()
    {
        Options opt = new OptionsBuilder().include(NewBenchmark.class.getSimpleName())//
                .warmupIterations(10)//
                .warmupBatchSize(20)//
                .measurementBatchSize(20)//
                .measurementIterations(20)//
                .forks(1).build();
        try
        {
            new Runner(opt).run();
        }
        catch (RunnerException e)
        {
            e.printStackTrace();
        }
    }
    
}
