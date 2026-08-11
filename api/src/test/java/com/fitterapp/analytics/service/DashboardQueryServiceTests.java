package com.fitterapp.analytics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fitterapp.analytics.exception.InvalidDashboardPeriodException;
import com.fitterapp.analytics.repository.DashboardMetricSnapshot;
import com.fitterapp.analytics.repository.DashboardMetricsRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardQueryServiceTests {
  @Mock private DashboardMetricsRepository repository;
  @InjectMocks private DashboardQueryService service;

  @Test
  void aggregatesTheInclusiveLocalDatePeriodUsingTheRequestedTimezone() {
    OffsetDateTime start = OffsetDateTime.parse("2026-08-01T00:00:00-03:00");
    OffsetDateTime end = OffsetDateTime.parse("2026-09-01T00:00:00-03:00");
    when(repository.aggregate(start, end))
        .thenReturn(new DashboardMetricSnapshot(10, 8, 7, 5, 2, 30, 20, 25, 18, 9, 6));

    var result =
        service.query(
            LocalDate.parse("2026-08-01"),
            LocalDate.parse("2026-08-31"),
            "America/Sao_Paulo");

    assertThat(result.period().startInclusive()).isEqualTo(start);
    assertThat(result.period().endExclusive()).isEqualTo(end);
    assertThat(result.period().timezone()).isEqualTo("America/Sao_Paulo");
    assertThat(result.funnel().accountsCompleted()).isEqualTo(10);
    assertThat(result.funnel().profilesApproved()).isEqualTo(5);
    assertThat(result.searches().raw()).isEqualTo(30);
    assertThat(result.searches().unique()).isEqualTo(20);
    assertThat(result.profileViews().unique()).isEqualTo(18);
    assertThat(result.whatsappContacts().unique()).isEqualTo(6);
    verify(repository).aggregate(start, end);
  }

  @Test
  void honorsDaylightSavingTransitionsAtPeriodBoundaries() {
    OffsetDateTime start = OffsetDateTime.parse("2026-03-08T00:00:00-05:00");
    OffsetDateTime end = OffsetDateTime.parse("2026-03-09T00:00:00-04:00");
    when(repository.aggregate(start, end))
        .thenReturn(new DashboardMetricSnapshot(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));

    var result =
        service.query(
            LocalDate.parse("2026-03-08"),
            LocalDate.parse("2026-03-08"),
            "America/New_York");

    assertThat(result.period().startInclusive()).isEqualTo(start);
    assertThat(result.period().endExclusive()).isEqualTo(end);
  }

  @Test
  void rejectsAnInvertedPeriod() {
    assertThatThrownBy(
            () ->
                service.query(
                    LocalDate.parse("2026-08-31"),
                    LocalDate.parse("2026-08-01"),
                    "America/Sao_Paulo"))
        .isInstanceOf(InvalidDashboardPeriodException.class)
        .hasMessageContaining("start");
  }

  @Test
  void rejectsAnInvalidTimezone() {
    assertThatThrownBy(
            () ->
                service.query(
                    LocalDate.parse("2026-08-01"),
                    LocalDate.parse("2026-08-31"),
                    "Mars/Olympus"))
        .isInstanceOf(InvalidDashboardPeriodException.class)
        .hasMessageContaining("timezone");
  }
}
