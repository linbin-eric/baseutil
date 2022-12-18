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
@Warmup(iterations = 1)
@Measurement(iterations = 10, time = 3, timeUnit = TimeUnit.SECONDS)
@Threads(1)
@Fork(1)
@OutputTimeUnit(TimeUnit.SECONDS)
public class ValueAccessorBenchmark
{
    static ValueAccessorTest test   = new ValueAccessorTest();
    static ValueAccessor     valueAccessor;
    static ValueAccessor     valueAccessor_compile;

    static
    {
        try
        {
            String name = "d1";
            valueAccessor = new ValueAccessor(ValueAccessorTest.class.getDeclaredField(name));
            valueAccessor_compile = ValueAccessor.create(ValueAccessorTest.class.getDeclaredField(name), new CompileHelper());
        }
        catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        }
    }

    @Benchmark
    public void testOld()
    {
//        valueAccessor.setObject(test, "sadas");
        valueAccessor.getDoubleObject(test);
    }

    @Benchmark
    public void testNew()
    {
//        valueAccessor_compile.setObject(test_2, "sadas");
        valueAccessor_compile.get(test);
    }

    public static void main(String[] args) throws RunnerException
    {
        Options opt = new OptionsBuilder().include(ValueAccessorBenchmark.class.getSimpleName()).build();
        new Runner(opt).run();
    }
}
