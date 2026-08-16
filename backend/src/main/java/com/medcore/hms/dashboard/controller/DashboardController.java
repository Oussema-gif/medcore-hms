package com.medcore.hms.dashboard.controller;

import com.medcore.hms.common.api.ApiResponse;
import com.medcore.hms.dashboard.dto.DashboardSummaryResponse;
import com.medcore.hms.dashboard.service.DashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DOCTOR', 'RECEPTIONIST')")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ApiResponse<DashboardSummaryResponse> getSummary() {
        return ApiResponse.success(
                dashboardService.getSummary(),
                "Dashboard summary retrieved successfully"
        );
    }
}
