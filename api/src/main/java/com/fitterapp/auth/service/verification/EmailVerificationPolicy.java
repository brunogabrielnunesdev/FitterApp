package com.fitterapp.auth.service.verification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmailVerificationPolicy {

  private final boolean required;

  public EmailVerificationPolicy(
      @Value("${fitterapp.email.verification-required:true}") boolean required) {
    this.required = required;
  }

  public boolean isRequired() {
    return required;
  }
}
