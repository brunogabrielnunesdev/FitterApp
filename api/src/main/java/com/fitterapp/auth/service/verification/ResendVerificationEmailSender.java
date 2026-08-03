package com.fitterapp.auth.service.verification;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ResendVerificationEmailSender implements VerificationEmailSender {

  private static final URI EMAILS_URI = URI.create("https://api.resend.com/emails");

  private final HttpClient httpClient;
  private final String apiKey;
  private final String senderAddress;
  private final String confirmationUrl;

  @Autowired
  public ResendVerificationEmailSender(
      @Value("${fitterapp.resend.api-key}") String apiKey,
      @Value("${fitterapp.email.from}") String senderAddress,
      @Value("${fitterapp.email.confirmation-url}") String confirmationUrl) {
    this(HttpClient.newHttpClient(), apiKey, senderAddress, confirmationUrl);
  }

  ResendVerificationEmailSender(
      HttpClient httpClient, String apiKey, String senderAddress, String confirmationUrl) {
    this.httpClient = httpClient;
    this.apiKey = apiKey;
    this.senderAddress = senderAddress;
    this.confirmationUrl = confirmationUrl;
  }

  @Override
  public void send(String recipient, String recipientName, String rawToken) {
    if (apiKey.isBlank()) {
      throw new EmailDeliveryException("RESEND_API_KEY is not configured");
    }

    String confirmationLink =
        confirmationUrl + "?token=" + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
    String body =
        """
        {"from":%s,"to":[%s],"subject":"Confirme seu e-mail no FitterApp","html":%s}
        """
            .formatted(
                json(senderAddress),
                json(recipient),
                json(emailHtml(recipientName, confirmationLink)));
    HttpRequest request =
        HttpRequest.newBuilder(EMAILS_URI)
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

    try {
      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        throw new EmailDeliveryException(
            "Resend rejected email delivery: HTTP " + response.statusCode());
      }
    } catch (IOException exception) {
      throw new EmailDeliveryException("Could not deliver email through Resend", exception);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new EmailDeliveryException("Email delivery through Resend was interrupted", exception);
    }
  }

  private String emailHtml(String recipientName, String confirmationLink) {
    return """
        <p>Olá, %s!</p>
        <p>Confirme seu e-mail para ativar sua conta no FitterApp:</p>
        <p><a href="%s">Confirmar meu e-mail</a></p>
        <p>Este link expira em 24 horas e pode ser utilizado apenas uma vez.</p>
        <p>Se você não criou esta conta, ignore esta mensagem.</p>
        """
        .formatted(escapeHtml(recipientName), confirmationLink);
  }

  private String json(String value) {
    return "\""
        + value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
        + "\"";
  }

  private String escapeHtml(String value) {
    return value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;");
  }
}
