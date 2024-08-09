package org.astarteplatform.devicesdk.transport.unknown;

public class UnknownTransportConnectionInfo {
  private final String m_clientId;

  public UnknownTransportConnectionInfo(String astarteRealm, String deviceId) {
    m_clientId = astarteRealm + "/" + deviceId;
  }

  public String getClientId() {
    return m_clientId;
  }
}
