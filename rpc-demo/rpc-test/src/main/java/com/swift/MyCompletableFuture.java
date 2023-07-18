package com.swift;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author sunGuoNan
 * @version 1.0
 */
public class MyCompletableFuture {
    public static void main(String[] args) {


        /*
         * 可以获取子线程中的返回，过程中的结果，并可以在主线程中阻塞等待其完成
         */
        CompletableFuture<Integer> integerCompletableFuture = new CompletableFuture<>();

        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            int i = 8;
            integerCompletableFuture.complete(i);
        }).start();


        Integer integer;
        try {
            // get方法是一个阻塞的方法
            integer = integerCompletableFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        System.out.println(integer);


    }
}
