package org.astarteplatform.devicesdk.transport.mqtt;

import static org.eclipse.paho.client.mqttv3.MqttException.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.zip.InflaterInputStream;
import org.astarteplatform.devicesdk.AstartePropertyStorageException;
import org.astarteplatform.devicesdk.protocol.*;
import org.astarteplatform.devicesdk.transport.AstarteFailedMessage;
import org.astarteplatform.devicesdk.transport.AstarteFailedMessageStorage;
import org.astarteplatform.devicesdk.transport.AstarteTransportException;
import org.astarteplatform.devicesdk.util.AstartePayload;
import org.astarteplatform.devicesdk.util.DecodedMessage;
import org.bson.BSONCallback;
import org.bson.BSONDecoder;
import org.bson.BasicBSONCallback;
import org.bson.BasicBSONDecoder;
import org.eclipse.paho.client.mqttv3.*;
import org.joda.time.DateTime;

public class AstarteMqttV1Transport extends AstarteMqttTransport implements MqttCallbackExtended {
  private final String m_baseTopic;
  private final BSONDecoder mBSONDecoder = new BasicBSONDecoder();
  private final BSONCallback mBSONCallback = new BasicBSONCallback();
  private static Logger logger = Logger.getLogger(AstarteMqttV1Transport.class.getName());

  @Override
  public void connectComplete(boolean reconnect, String serverURI) {
    if (reconnect) {
      logger.info("Reconnected to : " + serverURI);
    } else {
      logger.info("Connected to : " + serverURI);
    }
  }

  @Override
  public void connectionLost(Throwable cause) {
    if (m_astarteTransportEventListener != null) {
      m_astarteTransportEventListener.onTransportDisconnected();
    } else {
      logger.info("The Connection was lost.");
    }
  }

  @Override
  public void messageArrived(String topic, MqttMessage message) throws AstarteTransportException {
    logger.info("Incoming message: " + new String(message.getPayload()));
    if (!topic.contains(m_baseTopic) || m_messageListener == null) {
      return;
    }

    String path = topic.replace(m_baseTopic + "/", "");

    // Is it a control message?
    if (path.startsWith("control")) {
      if (Objects.equals(path, "control/consumer/properties")) {
        handlePurgeProperties(message.getPayload());
      } else {
        logger.warning("Unhandled control message!" + path);
      }
      return;
    }

    String astarteInterface = path.split("/")[0];
    String interfacePath = path.replace(astarteInterface, "");

    // Identify in our introspection whether the interface exists
    if (!getDevice().hasInterface(astarteInterface)) {
      logger.warning("Got an unexpected interface! " + astarteInterface);
      return;
    }

    Object payload;
    DateTime timestamp = null;
    if (message.getPayload().length == 0) {
      // This is a property unset
      payload = null;
    } else {
      final DecodedMessage decodedMessage =
          AstartePayload.deserialize(message.getPayload(), mBSONDecoder, mBSONCallback);
      payload = decodedMessage.getPayload();
      timestamp = decodedMessage.getTimestamp();
    }

    AstarteInterface targetInterface = getDevice().getInterface(astarteInterface);
    if (!(targetInterface instanceof AstarteServerValueBuilder)) {
      return;
    }

    AstarteServerValueBuilder astarteServerValueBuilder =
        (AstarteServerValueBuilder) targetInterface;
    AstarteServerValue astarteServerValue =
        astarteServerValueBuilder.build(interfacePath, payload, timestamp);

    if (astarteServerValue == null) {
      return;
    }

    if (targetInterface instanceof AstarteServerPropertyInterface) {
      try {
        if (astarteServerValue.getValue() != null) {
          savePropertyToStorage(astarteInterface, interfacePath, astarteServerValue.getValue());
        } else {
          removePropertyFromStorage(astarteInterface, interfacePath);
        }
      } catch (AstartePropertyStorageException e) {
        logger.severe("Caching won't work " + e.getMessage());
      }
    }

    if (!(targetInterface instanceof AstarteServerValuePublisher)) {
      return;
    }

    AstarteServerValuePublisher astarteServerValuePublisher =
        (AstarteServerValuePublisher) targetInterface;
    astarteServerValuePublisher.publish(astarteServerValue);
  }

  @Override
  public void deliveryComplete(IMqttDeliveryToken token) {}

  public AstarteMqttV1Transport(MutualSSLAuthenticationMqttConnectionInfo connectionInfo) {
    super(AstarteProtocolType.ASTARTE_MQTT_V1, connectionInfo);
    m_baseTopic = connectionInfo.getClientId();

    // Add callbacks here
    setCallback(this);
  }

  @Override
  public void setFailedMessageStorage(AstarteFailedMessageStorage failedMessageStorage) {
    super.setFailedMessageStorage(failedMessageStorage);
  }

  @Override
  public void sendIntrospection() throws AstarteTransportException {
    // Create the introspection String
    StringBuilder introspectionStringBuilder = new StringBuilder();
    for (AstarteInterface astarteInterface : getDevice().getAllInterfaces()) {
      introspectionStringBuilder.append(astarteInterface.getInterfaceName());
      introspectionStringBuilder.append(':');
      introspectionStringBuilder.append(astarteInterface.getMajorVersion());
      introspectionStringBuilder.append(':');
      introspectionStringBuilder.append(astarteInterface.getMinorVersion());
      introspectionStringBuilder.append(';');
    }
    // Remove last ';'
    introspectionStringBuilder.deleteCharAt(introspectionStringBuilder.length() - 1);
    String introspection = introspectionStringBuilder.toString();

    MqttMessage introspectionMessage = new MqttMessage();
    introspectionMessage.setPayload(introspection.getBytes());
    introspectionMessage.setRetained(false);
    introspectionMessage.setQos(2);

    try {
      m_client.publish(m_baseTopic, introspectionMessage);
    } catch (MqttException e) {
      throw new AstarteTransportException(e);
    }
  }

  @Override
  public void sendEmptyCache() throws AstarteTransportException {
    MqttMessage emptyCacheMessage = new MqttMessage();
    emptyCacheMessage.setPayload("1".getBytes());
    emptyCacheMessage.setRetained(false);
    emptyCacheMessage.setQos(2);
    // Send an Empty Cache
    try {
      m_client.publish(m_baseTopic + "/control/emptyCache", emptyCacheMessage);
    } catch (MqttException e) {
      throw new AstarteTransportException(e);
    }
  }

  @Override
  public void resendAllProperties() throws AstarteTransportException {
    // Iterate and send
    if (m_propertyStorage == null) {
      return;
    }

    for (AstarteInterface astarteInterface : getDevice().getAllInterfaces()) {
      if (astarteInterface instanceof AstarteDevicePropertyInterface) {
        Map<String, Object> storedPaths = null;
        try {
          storedPaths = m_propertyStorage.getStoredValuesForInterface(astarteInterface);
        } catch (AstartePropertyStorageException e) {
          throw new AstarteTransportException("Failed to resend properties", e);
        }
        if (storedPaths == null) {
          continue;
        }

        for (Map.Entry<String, Object> entry : storedPaths.entrySet()) {
          sendIndividualValue(astarteInterface, entry.getKey(), entry.getValue());
        }
      }
    }
  }

  public void retryFailedMessages() throws AstarteTransportException {
    while (!m_failedMessageStorage.isEmpty()) {
      AstarteFailedMessage failedMessage = m_failedMessageStorage.peekFirst();
      if (failedMessage.isExpired()) {
        // No need to send this anymore, drop it
        m_failedMessageStorage.rejectFirst();
        continue;
      }

      try {
        doSendMqttMessage(failedMessage);
      } catch (MqttException e) {
        // We just break here to avoid sending out of order data
        throw new AstarteTransportException(e);
      }
      m_failedMessageStorage.ackFirst();
    }
  }

  @Override
  public void sendIndividualValue(
      AstarteInterface astarteInterface, String path, Object value, DateTime timestamp)
      throws AstarteTransportException {
    int qos = 2;
    AstarteInterfaceDatastreamMapping mapping = null;
    if (astarteInterface instanceof AstarteDatastreamInterface) {
      try {
        // Find a matching mapping
        mapping = (AstarteInterfaceDatastreamMapping) astarteInterface.findMappingInInterface(path);
      } catch (AstarteInterfaceMappingNotFoundException e) {
        throw new AstarteTransportException("Mapping not found", e);
      }
      qos = qosFromReliability(mapping);
    }

    String topic = m_baseTopic + "/" + astarteInterface.getInterfaceName() + path;
    byte[] payload =
        AstartePayload.serialize(value, (timestamp != null) ? timestamp.toDate() : null);

    try {
      doSendMqttMessage(topic, payload, qos);
    } catch (MqttException e) {
      if (astarteInterface instanceof AstarteDatastreamInterface) {
        handleDatastreamFailedPublish(e, mapping, topic, payload, qos);
      } else {
        handlePropertiesFailedPublish(e, topic, payload, qos);
      }
    }
  }

  @Override
  public void sendAggregate(
      AstarteAggregateDatastreamInterface astarteInterface,
      String path,
      Map<String, Object> value,
      DateTime timestamp)
      throws AstarteTransportException {
    int qos;
    AstarteInterfaceDatastreamMapping mapping;
    try {
      // Find a matching mapping
      mapping =
          (AstarteInterfaceDatastreamMapping) astarteInterface.getMappings().values().toArray()[0];
      qos = qosFromReliability(mapping);
    } catch (Exception e) {
      throw new AstarteTransportException("Mapping not found", e);
    }

    String topic = m_baseTopic + "/" + astarteInterface.getInterfaceName() + path;
    byte[] payload = AstartePayload.serialize(value, timestamp.toDate());

    try {
      doSendMqttMessage(topic, payload, qos);
    } catch (MqttException e) {
      // Aggregate can only be Datastream
      handleDatastreamFailedPublish(e, mapping, topic, payload, qos);
    }
  }

  private void handlePropertiesFailedPublish(MqttException e, String topic, byte[] payload, int qos)
      throws AstarteTransportException {
    if (!isTemporaryException(e)) {
      // Not a temporary exception, so it can't be solved by resending
      throw new AstarteTransportException(e);
    }

    // Properties are always stored and never expire
    m_failedMessageStorage.insertStored(topic, payload, qos);
  }

  private void handleDatastreamFailedPublish(
      MqttException e,
      AstarteInterfaceDatastreamMapping mapping,
      String topic,
      byte[] payload,
      int qos)
      throws AstarteTransportException {
    if (!isTemporaryException(e)) {
      // Not a temporary exception, so it can't be solved by resending
      throw new AstarteTransportException(e);
    }

    int expiry = mapping.getExpiry();
    switch (mapping.getRetention()) {
      case DISCARD:
        // Message won't be retried, so we throw to notify the user
        throw new AstarteTransportException("Cannot send value", e);

      case VOLATILE:
        {
          if (expiry > 0) {
            m_failedMessageStorage.insertVolatile(topic, payload, qos, expiry);
          } else {
            m_failedMessageStorage.insertVolatile(topic, payload, qos);
          }
          break;
        }

      case STORED:
        {
          if (expiry > 0) {
            m_failedMessageStorage.insertStored(topic, payload, qos, expiry);
          } else {
            m_failedMessageStorage.insertStored(topic, payload, qos);
          }
          break;
        }
    }
  }

  private boolean isTemporaryException(MqttException e) {
    switch (e.getReasonCode()) {
        // These the reason code that can be reasonably thought of as temporary.
        // See
        // https://www.eclipse.org/paho/files/javadoc/org/eclipse/paho/client/mqttv3/MqttException.html
      case REASON_CODE_BROKER_UNAVAILABLE:
      case REASON_CODE_CLIENT_CLOSED:
      case REASON_CODE_CLIENT_DISCONNECTING:
      case REASON_CODE_CLIENT_EXCEPTION:
      case REASON_CODE_CLIENT_NOT_CONNECTED:
      case REASON_CODE_CLIENT_TIMEOUT:
      case REASON_CODE_CONNECTION_LOST:
      case REASON_CODE_MAX_INFLIGHT:
      case REASON_CODE_WRITE_TIMEOUT:
        return true;
      default:
        return false;
    }
  }

  private int qosFromReliability(AstarteInterfaceDatastreamMapping mapping) {
    switch (mapping.getReliability()) {
      case UNIQUE:
        return 2;
      case GUARANTEED:
        return 1;
      case UNRELIABLE:
        return 0;
    }

    return 0;
  }

  private void doSendMqttMessage(AstarteFailedMessage failedMessage) throws MqttException {
    doSendMqttMessage(failedMessage.getTopic(), failedMessage.getPayload(), failedMessage.getQos());
  }

  private void doSendMqttMessage(String topic, byte[] payload, int qos) throws MqttException {
    MqttMessage mqttMessage = new MqttMessage();
    mqttMessage.setPayload(payload);
    mqttMessage.setRetained(false);
    mqttMessage.setQos(qos);

    doSendMqttMessage(topic, mqttMessage);
  }

  private void doSendMqttMessage(String topic, MqttMessage mqttMessage) throws MqttException {
    m_client.publish(topic, mqttMessage);
  }

  @Override
  public void onSuccess(IMqttToken asyncActionToken) {}

  private void setupSubscriptions() {
    try {
      m_client.subscribe(m_baseTopic + "/control/consumer/properties", 2);

      for (AstarteInterface astarteInterface : getDevice().getAllInterfaces()) {
        if ((astarteInterface instanceof AstarteServerAggregateDatastreamInterface)
            || (astarteInterface instanceof AstarteServerDatastreamInterface)
            || (astarteInterface instanceof AstarteServerPropertyInterface)) {
          m_client.subscribe(m_baseTopic + "/" + astarteInterface.getInterfaceName() + "/#", 2);
        }
      }
    } catch (MqttException e) {
      logger.severe("Error while setting up subscriptions: " + e.getMessage());
    }
  }

  private void handlePurgeProperties(byte[] payload) throws AstarteTransportException {
    // Remove first 4 bytes
    byte[] deflated = new byte[payload.length - 4];
    System.arraycopy(payload, 4, deflated, 0, payload.length - 4);
    // Get the deflated payload
    ByteArrayInputStream bais = new ByteArrayInputStream(deflated);
    InflaterInputStream iis = new InflaterInputStream(bais);

    StringBuilder result = new StringBuilder();
    byte[] buf = new byte[256];
    int rlen = -1;
    try {
      while ((rlen = iis.read(buf)) != -1) {
        result.append(new String(Arrays.copyOf(buf, rlen)));
      }
    } catch (IOException e) {
      logger.severe("Error while handling purge properties: " + e.getMessage());
    }

    String purgePropertiesPayload = result.toString();
    if (m_propertyStorage != null) {
      Map<String, List<String>> availableProperties = new HashMap<>();
      // Build all entries based on introspection
      for (AstarteInterface astarteInterface : getDevice().getAllInterfaces()) {
        // Add an entry in case it's a server property interface
        if (astarteInterface instanceof AstarteServerPropertyInterface) {
          availableProperties.put(astarteInterface.getInterfaceName(), new ArrayList<String>());
        }
      }

      String[] allProperties = purgePropertiesPayload.split(";");
      for (String property : allProperties) {
        String[] propertyTokens = property.split("/", 2);
        if (propertyTokens.length != 2) {
          // This is somehow invalid
          continue;
        }
        List<String> pathList = availableProperties.get(propertyTokens[0]);
        if (pathList == null) {
          // This should not happen, but just in case.
          continue;
        }
        if (!pathList.add("/" + propertyTokens[1])) {
          // TODO: take any action?
          continue;
        }
        availableProperties.put(propertyTokens[0], pathList);
      }

      // Perform purge on the storage
      try {
        m_propertyStorage.purgeProperties(availableProperties);
      } catch (AstartePropertyStorageException e) {
        throw new AstarteTransportException("Failed to purge properties", e);
      }
    }
  }

  @Override
  protected void onConnected(IMqttToken asyncActionToken) throws AstarteTransportException {
    // Called when connection has succeeded
    // Send introspection in case it's needed
    if (!asyncActionToken.getSessionPresent() || !m_introspectionSent) {
      // Set up subscriptions
      setupSubscriptions();
      // Prepare introspection and all
      sendIntrospection();
      sendEmptyCache();
      m_introspectionSent = true;
      // Send all properties
      resendAllProperties();
    }

    // Try to resend previously failed messages
    try {
      retryFailedMessages();
    } catch (AstarteTransportException e) {
      throw new AstarteTransportException("Message redelivery failed", e);
    }

    if (m_astarteTransportEventListener != null) {
      m_astarteTransportEventListener.onTransportConnected();
    } else {
      logger.info("Transport Connected");
    }
  }
}
