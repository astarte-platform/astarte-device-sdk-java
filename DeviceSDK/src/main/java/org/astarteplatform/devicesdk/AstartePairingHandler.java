package org.astarteplatform.devicesdk;

import java.security.cert.Certificate;
import java.util.List;
import org.astarteplatform.devicesdk.crypto.AstarteCryptoStore;
import org.astarteplatform.devicesdk.transport.AstarteTransport;

public class AstartePairingHandler {
  private final AstartePairingService m_AstartePairingService;
  private final String m_astarteRealm;
  private final String m_deviceId;
  private final String m_credentialSecret;
  final AstarteCryptoStore m_cryptoStore;
  private List<AstarteTransport> m_transports;

  private Certificate m_certificate;

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

    m_AstartePairingService = new AstartePairingService(pairingUrl, astarteRealm);

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
    m_transports =
        m_AstartePairingService.reloadTransports(m_credentialSecret, m_cryptoStore, m_deviceId);
  }

  public void requestNewCertificate() throws AstartePairingException {
    m_certificate =
        m_AstartePairingService.requestNewCertificate(
            m_credentialSecret, m_cryptoStore, m_deviceId);
  }
}
