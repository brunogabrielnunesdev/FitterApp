package com.fitterapp.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

class SmtpVerificationEmailSenderTests {

    @Test
    void sendsConfirmationLinkWithoutExposingTokenAnywhereElse() {
        JavaMailSender mailSender = org.mockito.Mockito.mock(JavaMailSender.class);
        SmtpVerificationEmailSender sender = new SmtpVerificationEmailSender(
                mailSender,
                "no-reply@fitterapp.local",
                "http://localhost:5173/confirmar-email");

        sender.send("student@example.com", "Bruno Gabriel", "token with symbols+/=");

        ArgumentCaptor<SimpleMailMessage> messageCaptor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage message = messageCaptor.getValue();
        assertThat(message.getFrom()).isEqualTo("no-reply@fitterapp.local");
        assertThat(message.getTo()).containsExactly("student@example.com");
        assertThat(message.getSubject()).isEqualTo("Confirme seu e-mail no FitterApp");
        assertThat(message.getText())
                .contains("Olá, Bruno Gabriel!")
                .contains("http://localhost:5173/confirmar-email?token=token+with+symbols%2B%2F%3D")
                .contains("expira em 24 horas");
    }
}
