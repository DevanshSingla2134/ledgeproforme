package com.ledgepro.controller;

import com.ledgepro.concurrency.LowStockMonitorService;
import com.ledgepro.dao.DashboardDAO;
import com.ledgepro.model.DashboardStats;
import com.ledgepro.model.Product;
import com.ledgepro.util.GenericResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DashboardDAO dashboardDAO;
    private final LowStockMonitorService lowStockMonitorService;

    public DashboardController(DashboardDAO dashboardDAO, LowStockMonitorService lowStockMonitorService) {
        this.dashboardDAO = dashboardDAO;
        this.lowStockMonitorService = lowStockMonitorService;
    }

    @GetMapping("/stats")
    public GenericResponse<DashboardStats> getDashboardStats(@RequestParam(defaultValue = "1") Long businessId) {
        DashboardStats stats = dashboardDAO.getDashboardStats(businessId);
        return GenericResponse.success(stats, "Dashboard statistics retrieved successfully");
    }

    @GetMapping("/alerts/low-stock")
    public GenericResponse<List<Product>> getLiveLowStockAlerts() {
        List<Product> alerts = lowStockMonitorService.getActiveLowStockAlerts();
        return GenericResponse.success(alerts, "Live low stock alerts retrieved from background monitor thread");
    }
}
