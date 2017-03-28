package com.jfireframework.baseutil;

import java.util.Queue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.junit.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import com.jfireframework.baseutil.concurrent.MPSCQueue;

@State(Scope.Benchmark)
public class BenchMarkForMpsc
{
    Queue<String> queue          = new ConcurrentLinkedQueue<String>();
    int           threads        = 40;
    TimeValue     time           = TimeValue.milliseconds(300);
    int           iterations     = 20;
    int           warmIterations = 10;
    
    @Benchmark
    public void test() throws InterruptedException, BrokenBarrierException
    {
        queue.offer("sa");
    }
    
    @Test
    public void test1()
    {
        
        Options opt = new OptionsBuilder().include(NewBenchmark.class.getSimpleName())//
                .warmupIterations(warmIterations)//
                .warmupTime(time)//
                .threads(threads)//
                .measurementIterations(iterations)//
                .measurementTime(time)//
                .shouldDoGC(true)//
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
