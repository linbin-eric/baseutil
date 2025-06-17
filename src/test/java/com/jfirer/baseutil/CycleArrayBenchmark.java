package com.jfirer.baseutil;

import com.jfirer.baseutil.concurrent.CycleArray;
import com.jfirer.baseutil.concurrent.RelaxationReadCycleArray;
import com.jfirer.baseutil.concurrent.StrictReadCycleArray;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
public class CycleArrayBenchmark {

    @Param({"1024"})
    private int arraySize;

    private CycleArray<Integer> strictArray;
    private CycleArray<Integer> relaxationArray;
    private Integer testValue;

    @Setup(Level.Trial)
    public void setup() {
        strictArray = new StrictReadCycleArray<>(arraySize);
        relaxationArray = new RelaxationReadCycleArray<>(arraySize);
        testValue = 1000; // 统一的测试对象，避免在测试方法中创建对象
    }

    @Benchmark
    @Group("strictArray")
    @GroupThreads(2)
    public void strictArrayPut() {
        strictArray.cycAdd(testValue);
    }

    @Benchmark
    @Group("strictArray")
    @GroupThreads(2)
    public void strictArrayTake() {
        strictArray.cycTake();
    }

    @Benchmark
    @Group("relaxationArray")
    @GroupThreads(2)
    public void relaxationArrayPut() {
        relaxationArray.cycAdd(testValue);
    }

    @Benchmark
    @Group("relaxationArray")
    @GroupThreads(2)
    public void relaxationArrayTake() {
        relaxationArray.cycTake();
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(CycleArrayBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
} 