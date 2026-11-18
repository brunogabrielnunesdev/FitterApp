package com.fitterapp.auth.service.password;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitterapp.auth.service.verification.EmailDeliveryException;
import java.io.ByteArrayOutputStream;
import java.net.http.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.concurrent.Flow;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ResendPasswordResetEmailSenderTests {
  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void sendsEscapedLinkAndCorrectExpiry() throws Exception {
    HttpClient client = mock(HttpClient.class);
    @SuppressWarnings("unchecked")
    HttpResponse<String> response = mock(HttpResponse.class);
    when(response.statusCode()).thenReturn(202);
    when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(response);
    var sender =
        new ResendPasswordResetEmailSender(
            client,
            mapper,
            "re_test",
            "FitterApp <from@example.com>",
            "https://example.com/reset-password?source=mail");
    sender.send("person@example.com", "<Person>", "a+b/=");
    var captor = ArgumentCaptor.forClass(HttpRequest.class);
    verify(client).send(captor.capture(), any(HttpResponse.BodyHandler.class));
    var request = captor.getValue();
    assertThat(request.uri().toString()).isEqualTo("https://api.resend.com/emails");
    assertThat(request.headers().firstValue("Authorization")).contains("Bearer re_test");
    var body = mapper.readTree(readBody(request));
    assertThat(body.path("to").get(0).asText()).isEqualTo("person@example.com");
    assertThat(body.path("html").asText())
        .contains("&lt;Person&gt;", "source=mail&amp;token=a%2Bb%2F%3D", "30 minutos");
  }

  @Test
  void rejectsProviderFailure() throws Exception {
    HttpClient client = mock(HttpClient.class);
    @SuppressWarnings("unchecked")
    HttpResponse<String> response = mock(HttpResponse.class);
    when(response.statusCode()).thenReturn(503);
    when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(response);
    var sender =
        new ResendPasswordResetEmailSender(
            client, mapper, "key", "from@example.com", "https://example.com/reset");
    assertThatThrownBy(() -> sender.send("person@example.com", "Person", "token"))
        .isInstanceOf(EmailDeliveryException.class);
  }

  @Test
  void requiresProviderKey() {
    var sender =
        new ResendPasswordResetEmailSender(
            mock(HttpClient.class), mapper, "", "from@example.com", "https://example.com/reset");
    assertThatThrownBy(() -> sender.send("person@example.com", "Person", "token"))
        .isInstanceOf(EmailDeliveryException.class);
  }

  private String readBody(HttpRequest request) throws Exception {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    CountDownLatch done = new CountDownLatch(1);
    request
        .bodyPublisher()
        .orElseThrow()
        .subscribe(
            new Flow.Subscriber<ByteBuffer>() {
              public void onSubscribe(Flow.Subscription s) {
                s.request(Long.MAX_VALUE);
              }

              public void onNext(ByteBuffer value) {
                byte[] data = new byte[value.remaining()];
                value.get(data);
                bytes.writeBytes(data);
              }

              public void onError(Throwable error) {
                done.countDown();
              }

              public void onComplete() {
                done.countDown();
              }
            });
    assertThat(done.await(5, TimeUnit.SECONDS)).isTrue();
    return bytes.toString(StandardCharsets.UTF_8);
  }
}
