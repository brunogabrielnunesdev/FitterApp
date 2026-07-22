package com.fitterapp.auth.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class SmtpVerificationEmailSender implements VerificationEmailSender {

    private final JavaMailSender mailSender;
    private final String senderAddress;
    private final String confirmationUrl;

    public SmtpVerificationEmailSender(
            JavaMailSender mailSender,
            @Value("${fitterapp.email.from}") String senderAddress,
            @Value("${fitterapp.email.confirmation-url}") String confirmationUrl) {
        this.mailSender = mailSender;
        this.senderAddress = senderAddress;
        this.confirmationUrl = confirmationUrl;
    }

    @Override
    public void send(String recipient, String recipientName, String rawToken) {
        String encodedToken = URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
        String link = confirmationUrl + "?token=" + encodedToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderAddress);
        message.setTo(recipient);
        message.setSubject("Confirme seu e-mail no FitterApp");
        message.setText("""
                Olá, %s!

                Confirme seu e-mail para ativar sua conta no FitterApp:

                %s

                Este link expira em 24 horas e pode ser utilizado apenas uma vez.

                Se você não criou esta conta, ignore esta mensagem.
                """.formatted(recipientName, link));

        mailSender.send(message);
    }
}
