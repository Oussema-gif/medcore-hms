package com.medcore.hms.dashboard.dto;

public record DashboardSummaryResponse(
        long activePatients,
        long todayAppointments,
        long pendingLaboratoryWork,
        String systemStatus
) {
}
