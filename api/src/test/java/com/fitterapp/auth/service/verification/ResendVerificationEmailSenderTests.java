package com.fitterapp.auth.service.verification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class ResendVerificationEmailSenderTests {

  @Test
  void sendsHtmlEmailThroughResendApi() throws Exception {
    HttpClient httpClient = Mockito.mock(HttpClient.class);
    @SuppressWarnings("unchecked")
    HttpResponse<String> response = Mockito.mock(HttpResponse.class);
    when(response.statusCode()).thenReturn(200);
    when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(response);
    ResendVerificationEmailSender sender =
        new ResendVerificationEmailSender(
            httpClient,
            "re_test_key",
            "FitterApp <onboarding@resend.dev>",
            "http://localhost:5173/confirmar-email");

    sender.send("bruno@fitterapp.com", "Bruno Gabriel", "token+with+symbols/=");

    ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
    verify(httpClient).send(requestCaptor.capture(), any(HttpResponse.BodyHandler.class));
    HttpRequest request = requestCaptor.getValue();
    assertThat(request.uri().toString()).isEqualTo("https://api.resend.com/emails");
    assertThat(request.headers().firstValue("Authorization")).contains("Bearer re_test_key");
  }
}
