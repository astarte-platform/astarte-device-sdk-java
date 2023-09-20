package org.astarteplatform.devicesdk;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import okhttp3.*;
import org.apache.commons.io.IOUtils;
import org.astarteplatform.devicesdk.crypto.AstarteCryptoStore;
import org.astarteplatform.devicesdk.protocol.AstarteProtocolType;
import org.astarteplatform.devicesdk.transport.AstarteTransport;
import org.astarteplatform.devicesdk.transport.AstarteTransportFactory;
import org.json.JSONException;
import org.json.JSONObject;

/** This is the class responsible for communicating with Astarte Pairing API. */
public final class AstartePairingService {
  private HttpUrl m_pairingUrl;
  private final String m_astarteRealm;
  private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
  private final OkHttpClient m_httpClient;

  public AstartePairingService(String pairingUrl, String astarteRealm) {
    m_astarteRealm = astarteRealm;
    m_pairingUrl = HttpUrl.parse(pairingUrl);

    if (m_pairingUrl == null) {
      throw new IllegalStateException();
    }

    m_pairingUrl = m_pairingUrl.newBuilder().addPathSegments("v1").build();

    m_httpClient = new OkHttpClient();
  }

  /**
   * Register a device, obtaining its credentials secret.
   *
   * @param jwtToken the token to access the agent API
   * @param deviceId the identify of device
   * @return the credentials secret
   * @throws AstartePairingServiceException if the initialization of the request has not been
   *     successful
   * @throws IOException if the execution of request has not been successful
   * @throws AstartePairingException if the response has not been successful
   */
  public String registerDevice(String jwtToken, String deviceId)
      throws IOException, AstartePairingException {
    String credentialsSecret;

    JSONObject payload = new JSONObject();
    try {
      JSONObject data = new JSONObject();
      data.put("hw_id", deviceId);
      payload.put("data", data);
    } catch (JSONException e) {
      throw new AstartePairingServiceException("Could not generate the JSON Request Payload", e);
    }

    HttpUrl registerUrl;
    try {
      HttpUrl.Builder builder =
          m_pairingUrl
              .newBuilder()
              .addPathSegment(m_astarteRealm)
              .addPathSegment("agent")
              .addPathSegment("devices");
      registerUrl = builder.build();
    } catch (Exception e) {
      throw new AstartePairingServiceException("Could not build Register Device URL", e);
    }

    RequestBody body = RequestBody.create(JSON, payload.toString());
    Request request =
        new Request.Builder()
            .url(registerUrl)
            .header("Authorization", "Bearer " + jwtToken)
            .post(body)
            .build();

    try (Response response = m_httpClient.newCall(request).execute()) {
      final ResponseBody responseBody = response.body();
      if (!response.isSuccessful() || responseBody == null) {
        throw new AstartePairingException(
            "Request to device register API failed with "
                + response.code()
                + ". Returned body is "
                + (responseBody != null ? responseBody.string() : "empty"));
      }

      String responseBodyString = responseBody.string();
      JSONObject responseJson = new JSONObject(responseBodyString);
      credentialsSecret = responseJson.optJSONObject("data").optString("credentials_secret");

      if (credentialsSecret.isEmpty()) {
        throw new AstartePairingException("Failure in calling device register API");
      }
    }
    return credentialsSecret;
  }

  protected List<AstarteTransport> reloadTransports(
      String credentialSecret, AstarteCryptoStore cryptoStore, String deviceId)
      throws AstartePairingException {
    // Build the request URL for Astarte MQTT v1
    HttpUrl requestUrl;
    try {
      HttpUrl.Builder builder =
          m_pairingUrl
              .newBuilder()
              .addPathSegment(m_astarteRealm)
              .addPathSegment("devices")
              .addPathSegment(deviceId);
      requestUrl = builder.build();
    } catch (Exception e) {
      throw new AstartePairingException("Could not build Pairing URL", e);
    }

    Request request =
        new Request.Builder()
            .url(requestUrl)
            .header("Authorization", "Bearer " + credentialSecret)
            .get()
            .build();

    JSONObject transportObjects;
    try (Response response = m_httpClient.newCall(request).execute()) {
      String responseBody = response.body().string();
      System.out.println(responseBody);
      JSONObject responseJson = new JSONObject(responseBody);
      transportObjects = responseJson.getJSONObject("data").getJSONObject("protocols");
    } catch (NullPointerException e) {
      throw new AstartePairingException(
          "Null Pointer exception - probably got a wrong payload?", e);
    } catch (Exception e) {
      throw new AstartePairingException(
          "Failure in calling Pairing API to " + requestUrl.toString(), e);
    }

    // Iterate Transports and make them available
    Iterator<String> keys = transportObjects.keys();
    List<AstarteTransport> transports = new LinkedList<>();

    while (keys.hasNext()) {
      String key = keys.next();
      AstarteProtocolType protocolType = AstarteProtocolType.fromString(key);
      if (protocolType == null) {
        System.out.println("Found unsupported protocol " + key);
        continue;
      }

      try {
        AstarteTransport supportedTransport =
            AstarteTransportFactory.createAstarteTransportFromPairing(
                protocolType,
                m_astarteRealm,
                deviceId,
                transportObjects.getJSONObject(key),
                cryptoStore);
        transports.add(supportedTransport);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    if (transports.isEmpty()) {
      throw new AstartePairingException("Pairing did not return any supported Transport.");
    }

    return transports;
  }

  protected Certificate requestNewCertificate(
      String credentialSecret, AstarteCryptoStore cryptoStore, String deviceId)
      throws AstartePairingException {
    // Get the CSR from Crypto
    String csr;
    Certificate certificate;
    try {
      csr = cryptoStore.generateCSR("CN=" + m_astarteRealm + "/" + deviceId);
    } catch (Exception e) {
      throw new AstartePairingException("Could not generate a CSR", e);
    }

    JSONObject payload = new JSONObject();
    try {
      JSONObject data = new JSONObject();
      data.put("csr", csr);
      payload.put("data", data);
    } catch (JSONException e) {
      throw new AstartePairingException("Could not generate the JSON Request Payload", e);
    }

    // Build the request URL for Astarte MQTT v1
    HttpUrl requestUrl;
    try {
      HttpUrl.Builder builder =
          m_pairingUrl
              .newBuilder()
              .addPathSegment(m_astarteRealm)
              .addPathSegment("devices")
              .addPathSegment(deviceId)
              .addPathSegments("protocols/astarte_mqtt_v1/credentials");
      requestUrl = builder.build();
    } catch (Exception e) {
      throw new AstartePairingException("Could not build Pairing URL", e);
    }

    RequestBody body = RequestBody.create(JSON, payload.toString());
    Request request =
        new Request.Builder()
            .url(requestUrl)
            .header("Authorization", "Bearer " + credentialSecret)
            .post(body)
            .build();

    try (Response response = m_httpClient.newCall(request).execute()) {
      if (!response.isSuccessful()) {
        throw new AstartePairingException(
            "Request to Pairing API failed with "
                + response.code()
                + ". Returned body is "
                + response.body().string());
      }

      String responseBody = response.body().string();
      JSONObject responseJson = new JSONObject(responseBody);
      CertificateFactory fact = CertificateFactory.getInstance("X.509");
      InputStream certificateStream =
          IOUtils.toInputStream(
              responseJson.getJSONObject("data").getString("client_crt"), Charset.defaultCharset());
      X509Certificate astarteCertificate =
          (X509Certificate) fact.generateCertificate(certificateStream);

      cryptoStore.setAstarteCertificate(astarteCertificate);
      certificate = astarteCertificate;
    } catch (NullPointerException e) {
      throw new AstartePairingException(
          "Null Pointer exception - probably got a " + "wrong payload?", e);
    } catch (Exception e) {
      throw new AstartePairingException("Failure in calling Pairing API", e);
    }

    return certificate;
  }
}
