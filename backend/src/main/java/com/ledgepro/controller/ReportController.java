package com.ledgepro.controller;

import com.ledgepro.service.ReportAnalyticsService;
import com.ledgepro.util.GenericResponse;
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
            @RequestParam(defaultValue = "1") Long businessId,
            @RequestParam(defaultValue = "5") int limit) {
        return GenericResponse.success(analyticsService.getTopSellingProducts(businessId, limit));
    }

    @GetMapping("/payment-methods")
    public GenericResponse<Map<String, Double>> getSalesByPaymentMethod(@RequestParam(defaultValue = "1") Long businessId) {
        return GenericResponse.success(analyticsService.getSalesByPaymentMethod(businessId));
    }

    @GetMapping("/inventory-health")
    public GenericResponse<Map<String, Object>> getInventoryHealth(@RequestParam(defaultValue = "1") Long businessId) {
        return GenericResponse.success(analyticsService.getInventoryHealthMetrics(businessId));
    }

    @GetMapping("/financial-summary")
    public GenericResponse<Map<String, Object>> getFinancialSummary(@RequestParam(defaultValue = "1") Long businessId) {
        return GenericResponse.success(analyticsService.getFinancialSummary(businessId));
    }

    @GetMapping("/expenses-by-category")
    public GenericResponse<Map<String, Double>> getExpensesByCategory(@RequestParam(defaultValue = "1") Long businessId) {
        return GenericResponse.success(analyticsService.getExpensesByCategory(businessId));
    }
}
