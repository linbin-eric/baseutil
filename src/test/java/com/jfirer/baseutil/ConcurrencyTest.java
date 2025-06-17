package com.jfirer.baseutil;

import com.jfirer.baseutil.concurrent.StrictReadCycleArray;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrencyTest {
    
    public static void main(String[] args) throws InterruptedException {
        testStrictReadCycleArray();
    }
    
    public static void testStrictReadCycleArray() throws InterruptedException {
        StrictReadCycleArray<Integer> array = new StrictReadCycleArray<>(4); // 小容量
        
        AtomicInteger putCount = new AtomicInteger(0);
        AtomicInteger takeCount = new AtomicInteger(0);
        AtomicInteger putFailCount = new AtomicInteger(0);
        AtomicInteger takeNullCount = new AtomicInteger(0);
        
        int writerThreads = 2;
        int readerThreads = 4;
        int operationsPerThread = 10000;
        
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(writerThreads + readerThreads);
        
        // 启动写线程
        for (int i = 0; i < writerThreads; i++) {
            final int threadId = i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < operationsPerThread; j++) {
                        if (array.cycAdd(threadId * operationsPerThread + j)) {
                            putCount.incrementAndGet();
                        } else {
                            putFailCount.incrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            }).start();
        }
        
        // 启动读线程
        for (int i = 0; i < readerThreads; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < operationsPerThread; j++) {
                        Integer result = array.cycTake();
                        if (result != null) {
                            takeCount.incrementAndGet();
                        } else {
                            takeNullCount.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    endLatch.countDown();
                }
            }).start();
        }
        
        System.out.println("Starting test...");
        long startTime = System.currentTimeMillis();
        startLatch.countDown();
        
        // 等待最多10秒
        boolean finished = endLatch.await(10, java.util.concurrent.TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();
        
        System.out.println("Test finished: " + finished);
        System.out.println("Time taken: " + (endTime - startTime) + "ms");
        System.out.println("Put success: " + putCount.get());
        System.out.println("Put failed: " + putFailCount.get());
        System.out.println("Take success: " + takeCount.get());
        System.out.println("Take null: " + takeNullCount.get());
        
        if (!finished) {
            System.out.println("Test timed out - possible deadlock/livelock!");
        }
    }
} 