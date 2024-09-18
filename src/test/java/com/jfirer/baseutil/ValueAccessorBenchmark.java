package com.jfirer.baseutil;

import com.jfirer.baseutil.reflect.valueaccessor.GetInt;
import com.jfirer.baseutil.reflect.valueaccessor.ValueAccessor;
import com.jfirer.baseutil.reflect.valueaccessor.impl.LambdaAccessorImpl;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 2, time = 2)
@Measurement(iterations = 3, time = 3)
@Threads(1)
@Fork(1)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class ValueAccessorBenchmark
{
    ValueAccessorTest         test = new ValueAccessorTest();
    ValueAccessor             valueAccessor;
    ValueAccessor             valueAccessor_compile;
    ValueAccessor             valueAccessor_lambda;
    GetInt<ValueAccessorTest> accessorTestApplyInt;
    GetInt                    factoryLambda;
    TestLambda                testLambda;

    public ValueAccessorBenchmark()
    {
        try
        {
            String name  = "a";
            Field  field = ValueAccessorTest.class.getDeclaredField(name);
            valueAccessor         = ValueAccessor.normal(ValueAccessorTest.class.getDeclaredField(name));
            valueAccessor_compile = ValueAccessor.compile(ValueAccessorTest.class.getDeclaredField(name));
            valueAccessor_lambda  = new LambdaAccessorImpl(field);
            testLambda            = new TestLambda(field);
            accessorTestApplyInt  = ValueAccessorTest::getA;
            factoryLambda         = ValueAccessor.buildGetInt(field);
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    public void testUnsafe()
    {
        valueAccessor.getInt(test);
    }

    public void testCompile()
    {
        valueAccessor_compile.getInt(test);
    }

    public void testlambda()
    {
        testLambda.getInt(test);
    }

    @Benchmark
    public void AtestValueAccessorLambda()
    {
        valueAccessor_lambda.getInt(test);
    }

    public void testOrigin()
    {
        accessorTestApplyInt.get(test);
    }

    public void factoryLambda()
    {
        factoryLambda.get(test);
    }

    public static void main(String[] args) throws RunnerException
    {
        Options opt = new OptionsBuilder().include(ValueAccessorBenchmark.class.getSimpleName()).build();
        new Runner(opt).run();
    }
}
