package com.jfirer.baseutil;

import com.jfirer.baseutil.concurrent.BitmapObjectPool;
import com.jfirer.baseutil.concurrent.CycleArray;
import com.jfirer.baseutil.concurrent.IndexReadCycleArray;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
public class ReUsePoolBenchMark
{
    @Param({"4096","10000","20000"})
    private int arraySize;

    class TestV implements BitmapObjectPool.Poolable
    {
        private final int bitmapIndex;

        public TestV(int bitmapIndex)
        {
            this.bitmapIndex = bitmapIndex;
        }

        @Override
        public int getBitmapIndex()
        {
            return bitmapIndex;
        }
    }

    private CycleArray<Integer>     cycleArray;
    private BitmapObjectPool<TestV> bitmapObjectPool;
    private Integer                 testValue;

    @Setup(Level.Trial)
    public void setup()
    {
        testValue  = 1000; // 统一的测试对象，避免在测试方法中创建对象
        cycleArray = new IndexReadCycleArray<>(arraySize);
        for (int i = 0; i < arraySize; i++)
        {
            cycleArray.add(testValue);
        }
        bitmapObjectPool = new BitmapObjectPool<>(TestV::new, arraySize);
    }

    @Benchmark
    @Group("zindexArray")
    @GroupThreads(8)
    public void indexArrayTake()
    {
        Integer poll = cycleArray.poll();
        if (poll != null)
        {
            cycleArray.add(poll);
        }
    }

    @Benchmark
    @Group("bitmapArray")
    @GroupThreads(8)
    public void bitmapArrayPut()
    {
        TestV acquire = bitmapObjectPool.acquire();
        if (acquire != null)
        {
            bitmapObjectPool.release(acquire);
        }
    }
    public static void main(String[] args) throws RunnerException
    {
        Options opt = new OptionsBuilder()
                .include(ReUsePoolBenchMark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
