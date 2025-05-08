package com.lifelover.dome.core.report;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import com.lifelover.dome.core.config.ConfigLoader;


public class LocalQueueAsyncEventReporter implements EventReporter {

    private static final int THREAD_POOL_SIZE = 5;
    private static final int QUEUE_CAPACITY = 250;
    private static final int REQUEST_TIMEOUT_MS = 150;

    private final ExecutorService executorService;

    public LocalQueueAsyncEventReporter() {
        this.executorService = new ThreadPoolExecutor(
            THREAD_POOL_SIZE, 
            THREAD_POOL_SIZE, 
            0L, 
            TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(QUEUE_CAPACITY)
        );
    }

    @Override
    public void report(List<MetricsEvent> lst) {
        if (lst == null || lst.isEmpty()) {
            return;
        }
        String reportUrl = ConfigLoader.getAgentConfig().getCollectorAddr();
        if (reportUrl == null || reportUrl.isBlank()) {
            System.out.println("收集器地址为空");
            return;
        }
        for (MetricsEvent event : lst) {
            executorService.submit(() -> {
                try {
                    HttpUtil.post(reportUrl, event.jsonStr(), REQUEST_TIMEOUT_MS);
                } catch (Exception e) {
                    // 可以添加日志记录错误
                    System.err.println("事件上报失败: " + e.getMessage());
                }
            });
        }
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
