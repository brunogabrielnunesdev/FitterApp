package com.fitterapp.auth.service.verification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class VerificationEmailListener {

  private final VerificationEmailSender emailSender;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onVerificationEmailRequested(VerificationEmailRequested event) {
    emailSender.send(event.email(), event.fullName(), event.rawToken());
  }
}
