package org.astarteplatform.devicesdk.transport.mqtt;

import javax.net.ssl.SSLSocketFactory;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

public class MutualSSLAuthenticationMqttConnectionInfo implements MqttConnectionInfo {
  private final String m_brokerUrl;
  private final MqttConnectOptions m_mqttConnectOptions;
  private final String m_clientId;

  public MutualSSLAuthenticationMqttConnectionInfo(
      String brokerUrl, String astarteRealm, String deviceId, SSLSocketFactory sslSocketFactory) {
    m_brokerUrl = brokerUrl;
    m_mqttConnectOptions = new MqttConnectOptions();
    m_mqttConnectOptions.setConnectionTimeout(60);
    m_mqttConnectOptions.setKeepAliveInterval(60);
    // 128 is a sane default. 10 is definitely too low, especially when enqueuing properties
    m_mqttConnectOptions.setMaxInflight(128);
    // We handle this at a different level.
    m_mqttConnectOptions.setAutomaticReconnect(false);
    m_mqttConnectOptions.setCleanSession(false);
    try {
      m_mqttConnectOptions.setSocketFactory(sslSocketFactory);
    } catch (Exception e) {
      e.printStackTrace();
    }
    m_clientId = astarteRealm + "/" + deviceId;
  }

  @Override
  public String getBrokerUrl() {
    return m_brokerUrl;
  }

  @Override
  public String getClientId() {
    return m_clientId;
  }

  @Override
  public MqttConnectOptions getMqttConnectOptions() {
    return m_mqttConnectOptions;
  }
}
