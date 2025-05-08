package com.lifelover.dome.core.report;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import com.lifelover.dome.core.config.ConfigLoader;


public abstract class AbstractEventReporter implements EventReporter {

    private static final int THREAD_POOL_SIZE = 5;
    private static final int QUEUE_CAPACITY = 250;
    

    private final ExecutorService executorService;

    public AbstractEventReporter() {
        this.executorService = new ThreadPoolExecutor(
            THREAD_POOL_SIZE, 
            THREAD_POOL_SIZE, 
            0L, 
            TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(QUEUE_CAPACITY)
        );
    }

    

    @Override
    public void asyncReport(MetricsEvent metricsEvent) {
        List<MetricsEvent> lst = new ArrayList<>(1);
        lst.add(metricsEvent);
        asyncReport(lst);
    }



    @Override
    public void asyncReport(List<MetricsEvent> lst) {
        if (lst == null || lst.isEmpty()) {
            return;
        }
        // String reportUrl = ConfigLoader.getAgentConfig().getCollectorAddr();
        // if (reportUrl == null || reportUrl.isBlank()) {
        //     System.out.println("收集器地址为空");
        //     return;
        // }
        for (MetricsEvent event : lst) {
            executorService.submit(() -> {
                try {
                    handle(event);
                } catch (Exception e) {
                    // 可以添加日志记录错误
                    System.err.println("事件上报失败: " + e.getMessage());
                }
            });
        }
    }

    /**
     * 
     * @param metricsEvent
     */
    protected abstract void handle(MetricsEvent metricsEvent);

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
