package com.fitterapp.auth.service.verification;

public interface VerificationEmailSender {

    void send(String recipient, String recipientName, String rawToken);
}
