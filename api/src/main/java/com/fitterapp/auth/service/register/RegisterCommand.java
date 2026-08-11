package com.fitterapp.auth.service.register;

import com.fitterapp.analytics.entity.event.EventSource;

public record RegisterCommand(
    String fullName, String email, String phoneNumber, String password, EventSource source) {
  public RegisterCommand(String fullName, String email, String phoneNumber, String password) {
    this(fullName, email, phoneNumber, password, EventSource.MOBILE_APP);
  }
}
