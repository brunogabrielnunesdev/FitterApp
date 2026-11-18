package com.fitterapp.auth.service.password;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

import com.fitterapp.auth.service.verification.EmailDeliveryException;
import org.junit.jupiter.api.Test;

class PasswordResetEmailListenerTests {
  @Test
  void forwardsEventToSender() {
    var sender = mock(PasswordResetEmailSender.class);
    new PasswordResetEmailListener(sender)
        .onPasswordResetRequested(
            new PasswordResetRequested("user@example.com", "User", "raw-token"));
    verify(sender).send("user@example.com", "User", "raw-token");
  }

  @Test
  void handlesDeliveryFailureWithoutExposingAccount() {
    var sender = mock(PasswordResetEmailSender.class);
    doThrow(new EmailDeliveryException("delivery failed"))
        .when(sender)
        .send(anyString(), anyString(), anyString());
    assertThatCode(
            () ->
                new PasswordResetEmailListener(sender)
                    .onPasswordResetRequested(
                        new PasswordResetRequested("user@example.com", "User", "raw-token")))
        .doesNotThrowAnyException();
  }
}
