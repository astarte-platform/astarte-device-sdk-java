package org.astarteplatform.devicesdk.transport.mqtt;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

public interface MqttConnectionInfo {
  String getBrokerUrl();

  String getClientId();

  MqttConnectOptions getMqttConnectOptions();
}
