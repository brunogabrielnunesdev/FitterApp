package com.fitterapp.analytics.dto.dashboard;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record DashboardPeriodDto(
    LocalDate from,
    LocalDate to,
    String timezone,
    OffsetDateTime startInclusive,
    OffsetDateTime endExclusive) {}
