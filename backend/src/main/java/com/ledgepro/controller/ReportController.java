package com.ledgepro.controller;

import com.ledgepro.service.ReportAnalyticsService;
import com.ledgepro.util.GenericResponse;
import com.ledgepro.util.TenantUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportAnalyticsService analyticsService;

    public ReportController(ReportAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/top-products")
    public GenericResponse<List<Map<String, Object>>> getTopSellingProducts(
            @RequestHeader(value = "X-Business-Id", required = false) String headerBizId,
            @RequestParam(required = false) Long businessId,
            @RequestParam(defaultValue = "5") int limit) {
        Long targetBizId = TenantUtil.resolveBusinessId(businessId, headerBizId);
        return GenericResponse.success(analyticsService.getTopSellingProducts(targetBizId, limit));
    }

    @GetMapping("/payment-methods")
    public GenericResponse<Map<String, Double>> getSalesByPaymentMethod(
            @RequestHeader(value = "X-Business-Id", required = false) String headerBizId,
            @RequestParam(required = false) Long businessId) {
        Long targetBizId = TenantUtil.resolveBusinessId(businessId, headerBizId);
        return GenericResponse.success(analyticsService.getSalesByPaymentMethod(targetBizId));
    }

    @GetMapping("/inventory-health")
    public GenericResponse<Map<String, Object>> getInventoryHealth(
            @RequestHeader(value = "X-Business-Id", required = false) String headerBizId,
            @RequestParam(required = false) Long businessId) {
        Long targetBizId = TenantUtil.resolveBusinessId(businessId, headerBizId);
        return GenericResponse.success(analyticsService.getInventoryHealthMetrics(targetBizId));
    }

    @GetMapping("/financial-summary")
    public GenericResponse<Map<String, Object>> getFinancialSummary(
            @RequestHeader(value = "X-Business-Id", required = false) String headerBizId,
            @RequestParam(required = false) Long businessId) {
        Long targetBizId = TenantUtil.resolveBusinessId(businessId, headerBizId);
        return GenericResponse.success(analyticsService.getFinancialSummary(targetBizId));
    }

    @GetMapping("/expenses-by-category")
    public GenericResponse<Map<String, Double>> getExpensesByCategory(
            @RequestHeader(value = "X-Business-Id", required = false) String headerBizId,
            @RequestParam(required = false) Long businessId) {
        Long targetBizId = TenantUtil.resolveBusinessId(businessId, headerBizId);
        return GenericResponse.success(analyticsService.getExpensesByCategory(targetBizId));
    }
}
