package com.github.dadiyang.autologging.aop.util;

import org.junit.Test;

import java.util.Set;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;

public class SnowFlakeIdUtilsTest {

    @Test
    public void next() throws InterruptedException {
        Set<Long> nums = new ConcurrentSkipListSet<>();
        int tn = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(tn);
        CountDownLatch countDownLatch = new CountDownLatch(tn);
        for (int i = 0; i < tn; i++) {
            executorService.submit(() -> {
                try {
                    System.out.println("await");
                    countDownLatch.countDown();
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("go");
                for (int j = 0; j < 1024; j++) {
                    nums.add(SnowFlakeIdUtils.next());
                }
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);
        // 能通过测试，说明没有重复的id
        assertEquals(1024 * tn, nums.size());
    }

    @Test
    public void nextId() {
        System.out.println(SnowFlakeIdUtils.next());
        System.out.println(SnowFlakeIdUtils.next());

    }
}