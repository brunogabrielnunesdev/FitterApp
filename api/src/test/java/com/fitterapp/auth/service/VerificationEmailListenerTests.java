package com.fitterapp.auth.service;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VerificationEmailListenerTests {

    @Mock
    private VerificationEmailSender emailSender;

    @Test
    void sendsTheRequestedVerificationEmail() {
        VerificationEmailListener listener = new VerificationEmailListener(emailSender);

        listener.onVerificationEmailRequested(new VerificationEmailRequested(
                "bruno@fitterapp.com",
                "Bruno Gabriel",
                "raw-token"));

        verify(emailSender).send("bruno@fitterapp.com", "Bruno Gabriel", "raw-token");
    }
}
