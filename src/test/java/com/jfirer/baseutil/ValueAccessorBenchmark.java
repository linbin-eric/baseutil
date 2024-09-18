package com.jfirer.baseutil;

import com.jfirer.baseutil.reflect.CompileValueAccessor;
import com.jfirer.baseutil.reflect.LambdaValueAccessor;
import com.jfirer.baseutil.reflect.valueaccessor.ValueAccessor;
import com.jfirer.baseutil.reflect.valueaccessor.impl.UnsafeValueAccessorImpl;
import com.jfirer.baseutil.smc.compiler.CompileHelper;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 1, time = 3)
@Measurement(iterations = 3, time = 3, timeUnit = TimeUnit.SECONDS)
@Threads(1)
@Fork(1)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class ValueAccessorBenchmark
{
    ValueAccessorTest   test = new ValueAccessorTest();
    ValueAccessor       valueAccessor;
    ValueAccessor       valueAccessor_compile;
    LambdaValueAccessor valueAccessor_lambda;
    ApplyInt<ValueAccessorTest> accessorTestApplyInt;

    interface ApplyInt<T>
    {
        int apply(T t);
    }

    public ValueAccessorBenchmark()
    {
        try
        {
            String name = "a";
            valueAccessor = new UnsafeValueAccessorImpl(ValueAccessorTest.class.getDeclaredField(name));
            valueAccessor_compile = CompileValueAccessor.create(ValueAccessorTest.class.getDeclaredField(name), new CompileHelper());
            valueAccessor_lambda = new LambdaValueAccessor(ValueAccessorTest.class.getDeclaredField(name));
            accessorTestApplyInt = ValueAccessorTest::getA;
        }
        catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        }
    }

    @Benchmark
    public void testUnsafe()
    {
//        valueAccessor.setObject(test, "sadas");
        valueAccessor.getInt(test);
    }

    @Benchmark
    public void testCompile()
    {
//        valueAccessor_compile.setObject(test_2, "sadas");
        valueAccessor_compile.getInt(test);
    }

    @Benchmark
    public void testLambda()
    {
        valueAccessor_lambda.getInt(test);
    }

    @Benchmark
    public void testOrigin()
    {
        accessorTestApplyInt.apply(test);
    }

    public static void main(String[] args) throws RunnerException
    {
        Options opt = new OptionsBuilder().include(ValueAccessorBenchmark.class.getSimpleName()).build();
        new Runner(opt).run();
    }
}
