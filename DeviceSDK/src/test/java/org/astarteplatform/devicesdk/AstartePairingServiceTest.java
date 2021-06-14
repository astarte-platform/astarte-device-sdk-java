package org.astarteplatform.devicesdk;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Before;
import org.junit.Test;

public class AstartePairingServiceTest {

  private MockWebServer mockWebServer;
  private AstartePairingService astartePairingService;

  @Before
  public void init() {
    this.mockWebServer = new MockWebServer();
    this.astartePairingService =
        new AstartePairingService(mockWebServer.url("pairing").toString(), "test");
  }

  @Test(expected = AstartePairingException.class)
  public void testThrowExceptionOnUnsuccessfulRegister()
      throws IOException, AstartePairingException {
    mockWebServer.enqueue(
        new MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setResponseCode(500));

    astartePairingService.registerDevice("token ", "0011778222");
  }

  @Test
  public void testSuccessfulRegister()
      throws IOException, AstartePairingException, InterruptedException {
    String expectedCredentialSecret = "TTkd5OgB13X/3qU0LXU7OCxyTXz5QHM2NY1IgidtPOs=";
    String expectedRegisterPath = "/pairing/v1/test/agent/devices";
    String deviceId = "YHjKs3SMTgqq09eD7fzm6w";

    String expectedRequestBody = "{\"data\":{\"hw_id\":\"" + deviceId + "\"}}";

    String responseBody =
        "{\n"
            + "  \"data\": {\n"
            + "    \"credentials_secret\": \""
            + expectedCredentialSecret
            + "\"\n"
            + "  }\n"
            + "}";

    mockWebServer.enqueue(
        new MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setBody(responseBody)
            .setResponseCode(200));

    String credentialSecret = astartePairingService.registerDevice("token ", deviceId);
    RecordedRequest recordedRequest = mockWebServer.takeRequest();
    String registerPath = recordedRequest.getPath();
    String requestBody = recordedRequest.getBody().readUtf8();

    assertEquals("validate request body", expectedRequestBody, requestBody);
    assertEquals("validate register path", expectedRegisterPath, registerPath);
    assertEquals("validate credentials secret", expectedCredentialSecret, credentialSecret);
  }
}
