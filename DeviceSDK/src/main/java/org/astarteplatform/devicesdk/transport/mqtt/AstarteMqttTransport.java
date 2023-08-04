package org.astarteplatform.devicesdk.transport.mqtt;

import java.util.logging.Logger;
import javax.net.ssl.SSLHandshakeException;
import org.astarteplatform.devicesdk.AstarteMessageListener;
import org.astarteplatform.devicesdk.crypto.AstarteCryptoException;
import org.astarteplatform.devicesdk.protocol.AstarteProtocolType;
import org.astarteplatform.devicesdk.transport.AstarteTransport;
import org.astarteplatform.devicesdk.transport.AstarteTransportException;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;

public abstract class AstarteMqttTransport extends AstarteTransport implements IMqttActionListener {
  protected MqttAsyncClient m_client;
  private final MqttConnectionInfo m_connectionInfo;
  private MqttCallback mMqttCallback;
  private static Logger logger = Logger.getLogger(AstarteMqttTransport.class.getName());
  private final IMqttActionListener mMqttActionListener =
      new IMqttActionListener() {
        public void onSuccess(IMqttToken asyncActionToken) {
          try {
            onConnected(asyncActionToken);
          } catch (AstarteTransportException e) {
            if (m_astarteTransportEventListener != null) {
              m_astarteTransportEventListener.onTransportConnectionInitializationError(e);
            }
          }
        }

        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
          Throwable derivedException = null;
          if (exception.getCause() instanceof NullPointerException) {
            // This is most likely due to a missing Certificate
            derivedException = new AstarteCryptoException("Missing Device Certificate", exception);
          } else if (exception.getCause() instanceof SSLHandshakeException) {
            // This
            derivedException = new AstarteCryptoException("Invalid Device Certificate", exception);
          } else {
            derivedException = new AstarteTransportException("Error while connecting", exception);
          }

          if (m_astarteTransportEventListener != null) {
            m_astarteTransportEventListener.onTransportConnectionError(derivedException);
          }
          AstarteMessageListener listener = getMessageListener();
          if (listener != null) {
            listener.onFailure(derivedException);
          } else {
            logger.severe(derivedException.getMessage());
          }
          logger.severe(exception.getMessage());
        }
      };

  protected AstarteMqttTransport(AstarteProtocolType type, MqttConnectionInfo connectionInfo) {
    super(type);
    m_connectionInfo = connectionInfo;
  }

  protected void setCallback(MqttCallback callback) {
    mMqttCallback = callback;
  }

  public MqttConnectionInfo getConnectionInfo() {
    return m_connectionInfo;
  }

  private void initClient() {
    if (m_client != null) {
      try {
        m_client.close();
      } catch (Exception e) {
        logger.severe(e.getMessage());
      }
    }

    String brokerUrl = m_connectionInfo.getBrokerUrl();
    if (brokerUrl.startsWith("mqtts")) {
      // This is a special case - Astarte often uses mqtts to indicate MQTT+SSL, but
      // paho doesn't understand it. So, we manually replace it.
      brokerUrl = brokerUrl.replace("mqtts", "ssl");
    }

    try {
      m_client = new MqttAsyncClient(brokerUrl, m_connectionInfo.getClientId(), null);
    } catch (MqttException e) {
      logger.severe(e.getMessage());
    }
    m_client.setCallback(mMqttCallback);
  }

  @Override
  public void connect() throws AstarteTransportException, AstarteCryptoException {
    try {
      if (m_client != null) {
        if (m_client.isConnected()) {
          return;
        }
      } else {
        initClient();
      }

      m_client.connect(m_connectionInfo.getMqttConnectOptions(), null, mMqttActionListener);
    } catch (MqttException e) {
      if (e.getCause() instanceof NullPointerException) {
        // This is most likely due to a missing Certificate
        throw new AstarteCryptoException("Missing Device Certificate", e);
      } else if (e.getCause() instanceof SSLHandshakeException) {
        // This
        throw new AstarteCryptoException("Invalid Device Certificate", e);
      }
      throw new AstarteTransportException(e);
    }
  }

  @Override
  public void disconnect() throws AstarteTransportException {
    try {
      if (m_client.isConnected()) {
        m_client.disconnect();
      }
    } catch (MqttException e) {
      throw new AstarteTransportException(e);
    }
  }

  @Override
  public boolean isConnected() {
    if (m_client == null) {
      return false;
    }

    return m_client.isConnected();
  }

  @Override
  public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
    Exception failureCause = null;
    // Called when connection has failed
    if (exception.getCause() instanceof NullPointerException) {
      // This is most likely due to a missing Certificate
      failureCause = new AstarteCryptoException("Missing Device Certificate", exception);
    } else if (exception.getCause() instanceof SSLHandshakeException) {
      // This
      failureCause = new AstarteCryptoException("Invalid Device Certificate", exception);
    } else {
      failureCause = new AstarteTransportException(exception);
    }

    if (m_astarteTransportEventListener == null) {
      logger.severe(failureCause.getMessage());
      return;
    }

    m_astarteTransportEventListener.onTransportConnectionError(failureCause);
  }

  protected abstract void onConnected(IMqttToken asyncActionToken) throws AstarteTransportException;
}
