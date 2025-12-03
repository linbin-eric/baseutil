package cc.jfire.baseutil.benchmarkforhandle;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.lang.invoke.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 1, time = 3)
@Measurement(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
@Threads(1)
@Fork(1)
@OutputTimeUnit(TimeUnit.SECONDS)
public class Demo
{
    @State(Scope.Benchmark)
    public static class Provider
    {
        Data                    data = new Data();
        Method                  method;
        Function<Data, Integer> function;
        MethodHandle            handle;

        public Provider()
        {
            try
            {
                method = Data.class.getDeclaredMethod("getAge");
                data.setAge(12);
                MethodType           mt     = MethodType.methodType(Integer.class, Data.class);
                MethodHandles.Lookup lookup = MethodHandles.lookup();
                handle = lookup.findVirtual(Data.class, "getAge", MethodType.methodType(Integer.class));
                CallSite     apply  = LambdaMetafactory.metafactory(lookup, "apply", MethodType.methodType(Function.class), MethodType.methodType(Object.class, Object.class), handle, mt);
                MethodHandle target = apply.getTarget();
                function = (Function<Data, Integer>) target.invoke();
            }
            catch (Throwable e)
            {
                ;
            }
        }
    }

    @Benchmark
    public void testForOriginal(Provider provider)
    {
        provider.data.getAge();
    }

    @Benchmark
    public void testForReflect(Provider provider)
    {
        try
        {
            provider.method.invoke(provider.data);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
        catch (InvocationTargetException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Benchmark
    public void testForLambda(Provider provider)
    {
        provider.function.apply(provider.data);
    }

    @Benchmark
    public void testForHandler(Provider provider) throws Throwable
    {
        provider.handle.invoke(provider.data);
    }

    public static void main(String[] args) throws Throwable
    {
        Options opt = new OptionsBuilder().include(Demo.class.getSimpleName()).build();
        new Runner(opt).run();
    }
}
