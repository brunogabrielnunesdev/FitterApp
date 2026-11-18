package com.fitterapp.auth.service.password;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitterapp.auth.service.verification.EmailDeliveryException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class ResendPasswordResetEmailSender implements PasswordResetEmailSender {
  private static final URI EMAILS_URI = URI.create("https://api.resend.com/emails");
  private final HttpClient client;
  private final ObjectMapper objectMapper;
  private final String apiKey;
  private final String senderAddress;
  private final String resetUrl;

  @Autowired
  public ResendPasswordResetEmailSender(
      ObjectMapper objectMapper,
      @Value("${fitterapp.resend.api-key}") String apiKey,
      @Value("${fitterapp.email.from}") String senderAddress,
      @Value("${fitterapp.email.password-reset-url}") String resetUrl) {
    this(
        HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build(),
        objectMapper,
        apiKey,
        senderAddress,
        resetUrl);
  }

  ResendPasswordResetEmailSender(
      HttpClient client,
      ObjectMapper objectMapper,
      String apiKey,
      String senderAddress,
      String resetUrl) {
    this.client = client;
    this.objectMapper = objectMapper;
    this.apiKey = apiKey;
    this.senderAddress = senderAddress;
    this.resetUrl = resetUrl;
  }

  @Override
  public void send(String recipient, String recipientName, String rawToken) {
    if (apiKey == null || apiKey.isBlank())
      throw new EmailDeliveryException("RESEND_API_KEY is not configured");
    String link =
        UriComponentsBuilder.fromUriString(resetUrl)
            .queryParam("token", "{token}")
            .encode()
            .buildAndExpand(rawToken)
            .toUriString();
    String html =
        """
        <p>Olá, %s!</p>
        <p>Recebemos uma solicitação para redefinir sua senha no FitterApp.</p>
        <p><a href="%s">Redefinir minha senha</a></p>
        <p>Este link expira em 30 minutos e pode ser usado apenas uma vez.</p>
        <p>Se você não solicitou a redefinição, ignore esta mensagem.</p>
        """
            .formatted(HtmlUtils.htmlEscape(recipientName), HtmlUtils.htmlEscape(link));
    String body;
    try {
      body =
          objectMapper.writeValueAsString(
              Map.of(
                  "from",
                  senderAddress,
                  "to",
                  List.of(recipient),
                  "subject",
                  "Redefina sua senha no FitterApp",
                  "html",
                  html));
    } catch (JsonProcessingException exception) {
      throw new EmailDeliveryException("Could not prepare password reset email", exception);
    }
    HttpRequest request =
        HttpRequest.newBuilder(EMAILS_URI)
            .timeout(Duration.ofSeconds(15))
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
    try {
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() < 200 || response.statusCode() >= 300)
        throw new EmailDeliveryException(
            "Resend rejected email delivery: HTTP " + response.statusCode());
    } catch (IOException exception) {
      throw new EmailDeliveryException(
          "Could not deliver password reset email through Resend", exception);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new EmailDeliveryException("Password reset email delivery was interrupted", exception);
    }
  }
}
