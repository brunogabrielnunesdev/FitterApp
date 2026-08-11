package com.fitterapp.analytics.dto.dashboard;

public record FunnelMetricsDto(
    long accountsCompleted,
    long profilesStarted,
    long profilesSubmitted,
    long profilesApproved,
    long profilesRejected) {}
