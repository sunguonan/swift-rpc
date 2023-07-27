package com.swift.protection;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 最简单的熔断器的实现
 *
 * @author sunGuoNan
 * @version 1.0
 */
public class CircuitBreaker {

    // 理论上：标准的断路器应该有三种状态  open close half_open，我们为了简单只选取两种
    private volatile boolean isOpen = false;

    // 需要搜集指标  异常的数量   比例
    // 总的请求数
    private final AtomicInteger requestCount = new AtomicInteger(0);

    // 异常的请求数
    private final AtomicInteger errorRequest = new AtomicInteger(0);

    // 异常的阈值
    private final int maxErrorRequest;
    private final float maxErrorRate;

    public CircuitBreaker(int maxErrorRequest, float maxErrorRate) {
        this.maxErrorRequest = maxErrorRequest;
        this.maxErrorRate = maxErrorRate;
    }


    // 断路器的核心方法，判断是否开启
    public boolean isBreak() {
        // 优先返回，如果已经打开了，就直接返回true
        if (isOpen) {
            return true;
        }

        // 需要判断数据指标，是否满足当前的阈值
        if (errorRequest.get() > maxErrorRequest) {
            this.isOpen = true;
            return true;
        }

        if (errorRequest.get() > 0 && requestCount.get() > 0 &&
                errorRequest.get() / (float) requestCount.get() > maxErrorRate
        ) {
            this.isOpen = true;
            return true;
        }

        return false;
    }

    // 每次发生请求，获取发生异常应该进行记录
    public void recordRequest() {
        this.requestCount.getAndIncrement();
    }

    public void recordErrorRequest() {
        this.errorRequest.getAndIncrement();
    }

    /**
     * 重置熔断器
     */
    public void reset() {
        this.isOpen = false;
        this.requestCount.set(0);
        this.errorRequest.set(0);
    }


    public static void main(String[] args) {

        CircuitBreaker circuitBreaker = new CircuitBreaker(3, 1.1F);

        new Thread(() -> {
            for (int i = 0; i < 1000; i++) {

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                circuitBreaker.recordRequest();
                int num = new Random().nextInt(100);
                if (num > 70) {
                    circuitBreaker.recordErrorRequest();
                }

                boolean aBreak = circuitBreaker.isBreak();

                String result = aBreak ? "断路器阻塞了请求" : "断路器放行了请求";

                System.out.println(result);

            }
        }).start();


        new Thread(() -> {
            for (; ; ) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("-----------------------------------------");
                circuitBreaker.reset();
            }
        }).start();

        try {
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

}
