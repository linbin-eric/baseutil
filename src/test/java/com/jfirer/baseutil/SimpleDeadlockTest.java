package com.jfirer.baseutil;

import com.jfirer.baseutil.concurrent.StrictReadCycleArray;

public class SimpleDeadlockTest {
    
    public static void main(String[] args) throws InterruptedException {
        StrictReadCycleArray<Integer> array = new StrictReadCycleArray<>(2);
        
        // 简单的单生产者-单消费者测试
        Thread producer = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                while (!array.cycAdd(i)) {
                    Thread.yield(); // 如果队列满了就等待
                }
                if (i % 100 == 0) {
                    System.out.println("Put: " + i);
                }
            }
            System.out.println("Producer finished");
        });
        
        Thread consumer = new Thread(() -> {
            int count = 0;
            while (count < 1000) {
                Integer value = array.cycTake();
                if (value != null) {
                    count++;
                    if (count % 100 == 0) {
                        System.out.println("Take: " + value + ", count: " + count);
                    }
                } else {
                    Thread.yield(); // 如果队列空了就等待
                }
            }
            System.out.println("Consumer finished");
        });
        
        producer.start();
        consumer.start();
        
        // 等待最多5秒
        producer.join(5000);
        consumer.join(5000);
        
        if (producer.isAlive() || consumer.isAlive()) {
            System.out.println("Threads still alive - possible deadlock!");
            producer.interrupt();
            consumer.interrupt();
        }
    }
} 