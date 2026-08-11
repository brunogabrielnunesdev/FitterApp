package com.fitterapp.analytics.service;

public record MetricEventDecision(
    boolean recordEvent, boolean uniqueEvent, String idempotencyKeyHash) {

  static MetricEventDecision skipped(String idempotencyKeyHash) {
    return new MetricEventDecision(false, false, idempotencyKeyHash);
  }

  static MetricEventDecision record(boolean uniqueEvent, String idempotencyKeyHash) {
    return new MetricEventDecision(true, uniqueEvent, idempotencyKeyHash);
  }
}
