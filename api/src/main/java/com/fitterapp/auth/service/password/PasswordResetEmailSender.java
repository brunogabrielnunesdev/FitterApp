package com.fitterapp.auth.service.password;

public interface PasswordResetEmailSender {
  void send(String recipient, String recipientName, String rawToken);
}
