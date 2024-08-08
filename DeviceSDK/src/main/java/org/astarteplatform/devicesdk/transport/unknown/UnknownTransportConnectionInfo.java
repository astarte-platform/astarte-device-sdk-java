package org.astarteplatform.devicesdk.transport.unknown;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import javax.net.ssl.SSLSocketFactory;

public class UnknownTransportConnectionInfo {
    private final String m_clientId;

    public UnknownTransportConnectionInfo(
            String astarteRealm, String deviceId) {
        m_clientId = astarteRealm + "/" + deviceId;
    }

    public String getClientId() {
        return m_clientId;
    }
}
