package com.lifelover.dome.core.report;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public enum ThreadPoolSingleton{
    REPROTER_POOL;
    private static final int THREAD_POOL_SIZE = 5;
    private static final int QUEUE_CAPACITY = 250;

    private final ExecutorService executorService;

    ThreadPoolSingleton(){
        this.executorService = new ThreadPoolExecutor(
            THREAD_POOL_SIZE, 
            THREAD_POOL_SIZE, 
            0L, 
            TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(QUEUE_CAPACITY)
        );
    }

    public ExecutorService getThreadPool(){
        return executorService;
    }


    // 添加关闭线程池的方法，在应用程序关闭时调用
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }
}