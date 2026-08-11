package com.fitterapp.analytics.dto.dashboard;

public record AdminDashboardDto(
    DashboardPeriodDto period,
    FunnelMetricsDto funnel,
    EventMetricsDto searches,
    EventMetricsDto profileViews,
    EventMetricsDto whatsappContacts) {}
