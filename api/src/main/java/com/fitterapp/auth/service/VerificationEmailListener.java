package com.fitterapp.auth.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class VerificationEmailListener {

    private final VerificationEmailSender emailSender;

    public VerificationEmailListener(VerificationEmailSender emailSender) {
        this.emailSender = emailSender;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onVerificationEmailRequested(VerificationEmailRequested event) {
        emailSender.send(event.email(), event.fullName(), event.rawToken());
    }
}
