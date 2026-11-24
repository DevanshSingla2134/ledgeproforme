package com.ledgepro.concurrency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.util.concurrent.*;

/**
 * Thread Pool Manager demonstrating:
 * - Multithreading
 * - ExecutorService
 * - Callable & Future
 * - Synchronization and thread safety
 * - Thread lifecycle management
 */
@Component
public class ReportThreadPoolManager {
    private static final Logger logger = LoggerFactory.getLogger(ReportThreadPoolManager.class);

    private final ExecutorService executorService;
    private final Object statsLock = new Object();
    private int completedTaskCount = 0;

    public ReportThreadPoolManager(
            @Value("${ledgepro.concurrency.core-pool-size:4}") int corePoolSize,
            @Value("${ledgepro.concurrency.max-pool-size:8}") int maxPoolSize,
            @Value("${ledgepro.concurrency.queue-capacity:100}") int queueCapacity) {

        this.executorService = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                new ThreadFactory() {
                    private int count = 1;
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r, "LedgePro-WorkerPool-" + count++);
                        t.setDaemon(true);
                        return t;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        logger.info("Initialized LedgePro Worker Thread Pool (Core: {}, Max: {})", corePoolSize, maxPoolSize);
    }

    /**
     * Submits a Callable task to the worker pool and returns a Future,
     * demonstrating Java Callable<T> and Future<T> concurrency.
     */
    public <T> Future<T> submitTask(Callable<T> task) {
        return executorService.submit(() -> {
            try {
                return task.call();
            } finally {
                synchronized (statsLock) {
                    completedTaskCount++;
                }
            }
        });
    }

    /**
     * Submits an asynchronous Runnable task
     */
    public void executeAsync(Runnable task) {
        executorService.execute(() -> {
            try {
                task.run();
            } finally {
                synchronized (statsLock) {
                    completedTaskCount++;
                }
            }
        });
    }

    public int getCompletedTaskCount() {
        synchronized (statsLock) {
            return completedTaskCount;
        }
    }

    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down LedgePro Concurrency Thread Pool...");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
