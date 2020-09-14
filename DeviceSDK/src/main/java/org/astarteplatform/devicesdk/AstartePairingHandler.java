package org.astarteplatform.devicesdk;

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

public class AstartePairingHandler {
  private HttpUrl m_pairingUrl;
  private final String m_astarteRealm;
  private final String m_deviceId;
  private final String m_credentialSecret;
  final AstarteCryptoStore m_cryptoStore;
  private List<AstarteTransport> m_transports;

  private Certificate m_certificate;

  private final OkHttpClient m_httpClient = new OkHttpClient();
  private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

  public AstartePairingHandler(
      String pairingUrl,
      String astarteRealm,
      String deviceId,
      String credentialSecret,
      AstarteCryptoStore cryptoStore) {
    m_astarteRealm = astarteRealm;
    m_deviceId = deviceId;
    m_credentialSecret = credentialSecret;
    m_cryptoStore = cryptoStore;

    m_pairingUrl = HttpUrl.parse(pairingUrl);

    HttpUrl.Builder builder = m_pairingUrl.newBuilder().addPathSegments("v1");
    m_pairingUrl = builder.build();

    m_certificate = m_cryptoStore.getCertificate();
  }

  public void init() throws AstartePairingException {
    reloadTransports();
  }

  public List<AstarteTransport> getTransports() {
    return m_transports;
  }

  public String getAstarteRealm() {
    return m_astarteRealm;
  }

  public String getDeviceId() {
    return m_deviceId;
  }

  public Certificate getCertificate() {
    return m_certificate;
  }

  public boolean isCertificateAvailable() {
    return m_cryptoStore.getCertificate() != null;
  }

  private void reloadTransports() throws AstartePairingException {
    // Build the request URL for Astarte MQTT v1
    HttpUrl requestUrl;
    try {
      HttpUrl.Builder builder =
          m_pairingUrl
              .newBuilder()
              .addPathSegment(m_astarteRealm)
              .addPathSegment("devices")
              .addPathSegment(m_deviceId);
      requestUrl = builder.build();
    } catch (Exception e) {
      throw new AstartePairingException("Could not build Pairing URL", e);
    }

    Request request =
        new Request.Builder()
            .url(requestUrl)
            .header("Authorization", "Bearer " + m_credentialSecret)
            .get()
            .build();

    JSONObject transports;
    try (Response response = m_httpClient.newCall(request).execute()) {
      String responseBody = response.body().string();
      System.out.println(responseBody);
      JSONObject responseJson = new JSONObject(responseBody);
      transports = responseJson.getJSONObject("data").getJSONObject("protocols");
    } catch (NullPointerException e) {
      throw new AstartePairingException(
          "Null Pointer exception - probably got a " + "wrong payload?", e);
    } catch (Exception e) {
      throw new AstartePairingException(
          "Failure in calling Pairing API to " + requestUrl.toString(), e);
    }

    // Iterate Transports and make them available
    Iterator<String> keys = transports.keys();
    m_transports = new LinkedList<>();

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
                m_deviceId,
                transports.getJSONObject(key),
                m_cryptoStore);
        m_transports.add(supportedTransport);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    if (m_transports.isEmpty()) {
      throw new AstartePairingException("Pairing did not return any supported Transport.");
    }
  }

  public void requestNewCertificate() throws AstartePairingException {
    // Get the CSR from Crypto
    String csr;
    try {
      csr = m_cryptoStore.generateCSR("CN=" + m_astarteRealm + "/" + m_deviceId);
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
              .addPathSegment(m_deviceId)
              .addPathSegments("protocols/astarte_mqtt_v1/credentials");
      requestUrl = builder.build();
    } catch (Exception e) {
      throw new AstartePairingException("Could not build Pairing URL", e);
    }

    RequestBody body = RequestBody.create(payload.toString(), JSON);
    Request request =
        new Request.Builder()
            .url(requestUrl)
            .header("Authorization", "Bearer " + m_credentialSecret)
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

      m_cryptoStore.setAstarteCertificate(astarteCertificate);
      m_certificate = astarteCertificate;
    } catch (NullPointerException e) {
      throw new AstartePairingException(
          "Null Pointer exception - probably got a " + "wrong payload?", e);
    } catch (Exception e) {
      throw new AstartePairingException("Failure in calling Pairing API", e);
    }
  }
}
