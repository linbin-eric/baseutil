package com.jfirer.baseutil;

import com.jfirer.baseutil.reflect.valueaccessor.GetInt;
import com.jfirer.baseutil.reflect.valueaccessor.ValueAccessor;
import com.jfirer.baseutil.reflect.valueaccessor.impl.CompileAccessorVersion2Impl;
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
    ValueAccessor             valueAccessor_compile2;
    GetInt<ValueAccessorTest> accessorTestApplyInt;
    GetInt                    factoryLambda;
    TestLambda                testLambda;
    GetInt                    compileGetInt;

    public ValueAccessorBenchmark()
    {
        try
        {
            String name  = "a";
            Field  field = ValueAccessorTest.class.getDeclaredField(name);
            valueAccessor          = ValueAccessor.normal(ValueAccessorTest.class.getDeclaredField(name));
            valueAccessor_compile  = ValueAccessor.compile(ValueAccessorTest.class.getDeclaredField(name));
            valueAccessor_lambda   = new LambdaAccessorImpl(field);
            valueAccessor_compile2 = new CompileAccessorVersion2Impl(field);
            testLambda             = new TestLambda(field);
            accessorTestApplyInt   = ValueAccessorTest::getA;
            factoryLambda          = ValueAccessor.buildGetInt(field);
            compileGetInt          = ValueAccessor.buildCompileGetInt(field);
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    @Benchmark
    public void testUnsafe()
    {
        valueAccessor.getInt(test);
    }

    @Benchmark
    public void testCompile()
    {
        valueAccessor_compile.getInt(test);
    }
//
//    @Benchmark
//    public void testlambda()
//    {
//        testLambda.getInt(test);
//    }

    @Benchmark
    public void testCompileGetint()
    {
        compileGetInt.get(test);
    }

    @Benchmark
    public void testlambda()
    {
        valueAccessor_lambda.getInt(test);
    }

    @Benchmark
    public void testOrigin()
    {
        accessorTestApplyInt.get(test);
    }

    @Benchmark
    public void factoryLambda()
    {
        factoryLambda.get(test);
    }

    @Benchmark
    public void testCompile2()
    {
        valueAccessor_compile2.getInt(test);
    }

    public static void main(String[] args) throws RunnerException
    {
        Options opt = new OptionsBuilder().include(ValueAccessorBenchmark.class.getSimpleName()).build();
        new Runner(opt).run();
    }
}
