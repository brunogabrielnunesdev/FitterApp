package com.fitterapp.auth.service.password;

import com.fitterapp.auth.service.verification.EmailDeliveryException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordResetEmailListener {
  private final PasswordResetEmailSender sender;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onPasswordResetRequested(PasswordResetRequested event) {
    try {
      sender.send(event.email(), event.fullName(), event.rawToken());
    } catch (EmailDeliveryException exception) {
      // Do not expose account existence or put the address/token in logs.
      log.error("Password reset email delivery failed: {}", exception.getMessage());
    }
  }
}
