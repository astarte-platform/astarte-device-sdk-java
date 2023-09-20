package org.astarteplatform.devicesdk.transport;

import org.astarteplatform.devicesdk.crypto.AstarteCryptoStore;
import org.astarteplatform.devicesdk.protocol.AstarteProtocolType;
import org.astarteplatform.devicesdk.transport.mqtt.AstarteMqttV1Transport;
import org.astarteplatform.devicesdk.transport.mqtt.MutualSSLAuthenticationMqttConnectionInfo;
import org.json.JSONObject;

public class AstarteTransportFactory {
  public static AstarteTransport createAstarteTransportFromPairing(
      AstarteProtocolType protocolType,
      String astarteRealm,
      String deviceId,
      JSONObject protocolData,
      AstarteCryptoStore cryptoStore) {
    switch (protocolType) {
      case ASTARTE_MQTT_V1:
        try {
          String brokerUrl = protocolData.getString("broker_url");
          if (brokerUrl.endsWith("/")) {
            brokerUrl = brokerUrl.substring(0, brokerUrl.length() - 1);
          }
          return new AstarteMqttV1Transport(
              new MutualSSLAuthenticationMqttConnectionInfo(
                  brokerUrl, astarteRealm, deviceId, cryptoStore.getSSLSocketFactory()));
        } catch (Exception e) {
          e.printStackTrace();
          return null;
        }
      default:
        return null;
    }
  }
}
