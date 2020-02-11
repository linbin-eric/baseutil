package com.jfirer.baseutil;

import com.jfirer.baseutil.reflect.ValueAccessor;
import com.jfirer.baseutil.smc.compiler.CompileHelper;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 3)
@Measurement(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@Threads(1)
@Fork(1)
@OutputTimeUnit(TimeUnit.SECONDS)
public class ValueAccessorBenchmark
{
    static ValueAccessorTest test   = new ValueAccessorTest();
    static ValueAccessorTest test_2 = new ValueAccessorTest();
    static ValueAccessor     valueAccessor;
    static ValueAccessor     valueAccessor_compile;

    static
    {
        try
        {
            valueAccessor = new ValueAccessor(ValueAccessorTest.class.getDeclaredField("a"));
            valueAccessor_compile = ValueAccessor.create(ValueAccessorTest.class.getDeclaredField("a"), new CompileHelper());
        }
        catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        }
    }

    @Benchmark
    public void testOld()
    {
        valueAccessor.set(test, 2);
    }

    @Benchmark
    public void testNew()
    {
        valueAccessor_compile.set(test_2, 2);
    }

    public static void main(String[] args) throws RunnerException
    {
        Options opt = new OptionsBuilder().include(ValueAccessorBenchmark.class.getSimpleName()).build();
        new Runner(opt).run();
    }
}
