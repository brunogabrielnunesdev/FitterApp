package com.fitterapp.analytics.repository;

public record DashboardMetricSnapshot(
    long accountsCompleted,
    long profilesStarted,
    long profilesSubmitted,
    long profilesApproved,
    long profilesRejected,
    long searchesRaw,
    long searchesUnique,
    long profileViewsRaw,
    long profileViewsUnique,
    long whatsappContactsRaw,
    long whatsappContactsUnique) {}
