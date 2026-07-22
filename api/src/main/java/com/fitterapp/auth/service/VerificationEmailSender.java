package com.fitterapp.auth.service;

public interface VerificationEmailSender {

    void send(String recipient, String recipientName, String rawToken);
}
