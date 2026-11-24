package com.ledgepro.concurrency;

import com.ledgepro.dao.ProductDAO;
import com.ledgepro.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Background monitoring service demonstrating:
 * - Thread & Runnable
 * - Thread-safe collections (CopyOnWriteArrayList)
 * - Synchronization
 * - Periodic polling & inter-thread alert sharing
 */
@Service
public class LowStockMonitorService implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(LowStockMonitorService.class);

    private final ProductDAO productDAO;
    private final List<Product> activeLowStockAlerts = new CopyOnWriteArrayList<>();
    private volatile boolean running = true;
    private Thread monitorThread;

    public LowStockMonitorService(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    @PostConstruct
    public void startMonitoring() {
        monitorThread = new Thread(this, "LedgePro-LowStockMonitorThread");
        monitorThread.setDaemon(true);
        monitorThread.start();
        logger.info("Low Stock Monitoring background daemon thread started.");
    }

    @Override
    public void run() {
        // Initial delay to let database schema and seed data initialize
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            return;
        }

        while (running) {
            try {
                // Poll business 1 (default store) every 30 seconds
                List<Product> lowStock = productDAO.findLowStock(1L);
                synchronized (this) {
                    activeLowStockAlerts.clear();
                    activeLowStockAlerts.addAll(lowStock);
                }
                if (!lowStock.isEmpty()) {
                    logger.debug("Low Stock Alert: {} products are currently below threshold.", lowStock.size());
                }

                // Sleep for 30 seconds
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                logger.info("LowStockMonitorThread interrupted, stopping.");
                running = false;
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                logger.warn("LowStockMonitorThread encountered non-fatal error: {}", e.getMessage());
            }
        }
    }

    public synchronized List<Product> getActiveLowStockAlerts() {
        return Collections.unmodifiableList(new ArrayList<>(activeLowStockAlerts));
    }

    @PreDestroy
    public void stopMonitoring() {
        running = false;
        if (monitorThread != null) {
            monitorThread.interrupt();
        }
    }
}
