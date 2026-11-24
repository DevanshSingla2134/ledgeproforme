package com.ledgepro.controller;

import com.ledgepro.concurrency.LowStockMonitorService;
import com.ledgepro.dao.DashboardDAO;
import com.ledgepro.model.DashboardStats;
import com.ledgepro.model.Product;
import com.ledgepro.util.GenericResponse;
import com.ledgepro.util.TenantUtil;
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
    public GenericResponse<DashboardStats> getDashboardStats(
            @RequestHeader(value = "X-Business-Id", required = false) String headerBizId,
            @RequestParam(required = false) Long businessId) {
        Long targetBizId = TenantUtil.resolveBusinessId(businessId, headerBizId);
        DashboardStats stats = dashboardDAO.getDashboardStats(targetBizId);
        return GenericResponse.success(stats, "Dashboard statistics retrieved successfully");
    }

    @GetMapping("/alerts/low-stock")
    public GenericResponse<List<Product>> getLiveLowStockAlerts(
            @RequestHeader(value = "X-Business-Id", required = false) String headerBizId,
            @RequestParam(required = false) Long businessId) {
        Long targetBizId = TenantUtil.resolveBusinessId(businessId, headerBizId);
        List<Product> alerts = lowStockMonitorService.getActiveLowStockAlerts()
                .stream()
                .filter(p -> p.getBusinessId() != null && p.getBusinessId().equals(targetBizId))
                .toList();
        return GenericResponse.success(alerts, "Live low stock alerts retrieved from background monitor thread");
    }
}
