package org.astarteplatform.devicesdk.transport.mqtt;

import static org.eclipse.paho.client.mqttv3.MqttException.REASON_CODE_BROKER_UNAVAILABLE;
import static org.eclipse.paho.client.mqttv3.MqttException.REASON_CODE_CLIENT_CLOSED;
import static org.eclipse.paho.client.mqttv3.MqttException.REASON_CODE_CLIENT_DISCONNECTING;
import static org.eclipse.paho.client.mqttv3.MqttException.REASON_CODE_CLIENT_EXCEPTION;
import static org.eclipse.paho.client.mqttv3.MqttException.REASON_CODE_CLIENT_NOT_CONNECTED;
import static org.eclipse.paho.client.mqttv3.MqttException.REASON_CODE_CLIENT_TIMEOUT;
import static org.eclipse.paho.client.mqttv3.MqttException.REASON_CODE_CONNECTION_LOST;
import static org.eclipse.paho.client.mqttv3.MqttException.REASON_CODE_MAX_INFLIGHT;
import static org.eclipse.paho.client.mqttv3.MqttException.REASON_CODE_WRITE_TIMEOUT;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.InflaterInputStream;
import org.astarteplatform.devicesdk.protocol.AstarteAggregateDatastreamEvent;
import org.astarteplatform.devicesdk.protocol.AstarteAggregateDatastreamEventListener;
import org.astarteplatform.devicesdk.protocol.AstarteAggregateDatastreamInterface;
import org.astarteplatform.devicesdk.protocol.AstarteDatastreamEvent;
import org.astarteplatform.devicesdk.protocol.AstarteDatastreamEventListener;
import org.astarteplatform.devicesdk.protocol.AstarteDatastreamInterface;
import org.astarteplatform.devicesdk.protocol.AstarteDevicePropertyInterface;
import org.astarteplatform.devicesdk.protocol.AstarteInterface;
import org.astarteplatform.devicesdk.protocol.AstarteInterfaceDatastreamMapping;
import org.astarteplatform.devicesdk.protocol.AstarteInterfaceMapping;
import org.astarteplatform.devicesdk.protocol.AstartePropertyEvent;
import org.astarteplatform.devicesdk.protocol.AstartePropertyEventListener;
import org.astarteplatform.devicesdk.protocol.AstarteProtocolType;
import org.astarteplatform.devicesdk.protocol.AstarteServerAggregateDatastreamInterface;
import org.astarteplatform.devicesdk.protocol.AstarteServerDatastreamInterface;
import org.astarteplatform.devicesdk.protocol.AstarteServerPropertyInterface;
import org.astarteplatform.devicesdk.transport.AstarteFailedMessage;
import org.astarteplatform.devicesdk.transport.AstarteFailedMessageStorage;
import org.astarteplatform.devicesdk.transport.AstarteTransportException;
import org.bson.BSONCallback;
import org.bson.BSONDecoder;
import org.bson.BSONObject;
import org.bson.BasicBSONCallback;
import org.bson.BasicBSONDecoder;
import org.bson.BsonBinaryWriter;
import org.bson.Document;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.EncoderContext;
import org.bson.io.BasicOutputBuffer;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.joda.time.DateTime;

public class AstarteMqttV1Transport extends AstarteMqttTransport {
  private final String m_baseTopic;
  private ArrayDeque<AstarteFailedMessage> mFailedMessages;
  private final BSONDecoder mBSONDecoder = new BasicBSONDecoder();
  private final BSONCallback mBSONCallback = new BasicBSONCallback();
  private final MqttCallback mMqttCallback =
      new MqttCallbackExtended() {
        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
          if (reconnect) {
            System.out.println("Reconnected to : " + serverURI);
          } else {
            System.out.println("Connected to: " + serverURI);
          }
        }

        @Override
        public void connectionLost(Throwable cause) {
          if (m_astarteTransportEventListener != null) {
            m_astarteTransportEventListener.onTransportDisconnected();
          } else {
            System.out.println("The Connection was lost.");
          }
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
          System.out.println("Incoming message: " + new String(message.getPayload()));
          if (!topic.contains(m_baseTopic) || m_messageListener == null) {
            return;
          }

          String path = topic.replace(m_baseTopic + "/", "");

          // Is it a control message?
          if (path.startsWith("control")) {
            if (Objects.equals(path, "control/consumer/properties")) {
              handlePurgeProperties(message.getPayload());
            } else {
              System.err.println("Unhandled control message!" + path);
            }
            return;
          }

          String astarteInterface = path.split("/")[0];
          String interfacePath = path.replace(astarteInterface, "");

          // Identify in our introspection whether the interface exists
          if (!getDevice().hasInterface(astarteInterface)) {
            System.err.println("Got an unexpected interface! " + astarteInterface);
            return;
          }

          Object payload;
          DateTime timestamp = null;
          if (message.getPayload().length == 0) {
            // This is a property unset
            payload = null;
          } else {
            // Parse the BSON payload
            mBSONCallback.reset();
            mBSONDecoder.decode(message.getPayload(), mBSONCallback);
            BSONObject astartePayload = (BSONObject) mBSONCallback.get();
            // Parse the BSON value
            payload = astartePayload.get("v");
            if (astartePayload.containsField("t")) {
              timestamp = new DateTime(astartePayload.get("t"));
            }
          }

          AstarteInterface targetInterface = getDevice().getInterface(astarteInterface);
          if (targetInterface instanceof AstarteServerAggregateDatastreamInterface) {
            // Handle as an aggregate
            if (payload == null) {
              return;
            }
            BSONObject astartePayload = (BSONObject) payload;
            Map<String, Object> astarteAggregate = new HashMap<>();
            // Build the map, and normalize payload where needed
            for (String key : astartePayload.keySet()) {
              for (Map.Entry<String, AstarteInterfaceMapping> m :
                  targetInterface.getMappings().entrySet()) {
                if (AstarteInterface.isPathCompatibleWithMapping(
                    interfacePath + "/" + key, m.getValue().getPath())) {
                  if (m.getValue().getType() == DateTime.class) {
                    // Replace the value
                    astarteAggregate.put(key, new DateTime(astartePayload.get(key)));
                  } else {
                    astarteAggregate.put(key, astartePayload.get(key));
                  }
                }
              }
            }

            // Generate and stream the right event
            AstarteServerAggregateDatastreamInterface realInterface =
                (AstarteServerAggregateDatastreamInterface) targetInterface;
            AstarteAggregateDatastreamEvent e =
                new AstarteAggregateDatastreamEvent(astarteInterface, astarteAggregate, timestamp);
            for (AstarteAggregateDatastreamEventListener listener :
                realInterface.getAllListeners()) {
              listener.valueReceived(e);
            }
          } else {
            AstarteInterfaceMapping targetMapping = null;
            for (Map.Entry<String, AstarteInterfaceMapping> entry :
                targetInterface.getMappings().entrySet()) {
              if (AstarteInterface.isPathCompatibleWithMapping(interfacePath, entry.getKey())) {
                targetMapping = entry.getValue();
                break;
              }
            }
            if (targetMapping == null) {
              // Couldn't find the mapping
              System.err.println(
                  String.format(
                      "Got an unexpected path %s for interface %s!",
                      interfacePath, targetInterface.getInterfaceName()));
              return;
            }

            Object astarteValue = payload;
            if (targetMapping.getType() == DateTime.class) {
              // Convert manually
              astarteValue = new DateTime(payload);
            }

            // Generate and stream the right event
            if (targetInterface instanceof AstarteServerDatastreamInterface) {
              AstarteServerDatastreamInterface realInterface =
                  (AstarteServerDatastreamInterface) targetInterface;
              AstarteDatastreamEvent e =
                  new AstarteDatastreamEvent(
                      astarteInterface, interfacePath, astarteValue, timestamp);

              for (AstarteDatastreamEventListener listener :
                  ((AstarteServerDatastreamInterface) targetInterface).getAllListeners()) {
                listener.valueReceived(e);
              }
            } else if (targetInterface instanceof AstarteServerPropertyInterface) {
              {
                AstarteServerPropertyInterface realInterface =
                    (AstarteServerPropertyInterface) targetInterface;

                AstartePropertyEvent e =
                    new AstartePropertyEvent(astarteInterface, interfacePath, astarteValue);

                // Is it an unset?
                if (astarteValue == null) {
                  for (AstartePropertyEventListener listener :
                      ((AstarteServerPropertyInterface) targetInterface).getAllListeners()) {
                    listener.propertyUnset(e);
                  }
                } else {
                  for (AstartePropertyEventListener listener :
                      ((AstarteServerPropertyInterface) targetInterface).getAllListeners()) {
                    listener.propertyReceived(e);
                  }
                }
              }
            }
          }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {}
      };

  public AstarteMqttV1Transport(MutualSSLAuthenticationMqttConnectionInfo connectionInfo) {
    super(AstarteProtocolType.ASTARTE_MQTT_V1, connectionInfo);
    m_baseTopic = connectionInfo.getClientId();

    // Add callbacks here
    setCallback(mMqttCallback);
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
        Map<String, Object> storedPaths =
            m_propertyStorage.getStoredValuesForInterface(astarteInterface);
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
    AstarteInterfaceDatastreamMapping mapping;
    try {
      // Find a matching mapping
      mapping =
          (AstarteInterfaceDatastreamMapping)
              AstarteInterface.findMappingInInterface(astarteInterface, path);
    } catch (Exception e) {
      throw new AstarteTransportException("Mapping not found", e);
    }
    if (astarteInterface instanceof AstarteDatastreamInterface) {
      qos = qosFromReliability(mapping);
    }

    String topic = m_baseTopic + "/" + astarteInterface.getInterfaceName() + path;
    byte[] payload = objectToEncodedBSON(value, timestamp);

    try {
      doSendMqttMessage(topic, payload, qos);
    } catch (MqttException e) {
      handleFailedPublish(e, mapping, topic, payload, qos);
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
    byte[] payload = objectToEncodedBSON(value, timestamp);

    try {
      doSendMqttMessage(topic, payload, qos);
    } catch (MqttException e) {
      handleFailedPublish(e, mapping, topic, payload, qos);
    }
  }

  private void handleFailedPublish(
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

  private byte[] objectToEncodedBSON(Object o, DateTime t) {
    if (o == null) {
      // When handling unsets in Astarte MQTT v1, send an empty payload
      return new byte[] {};
    }

    HashMap<String, Object> bsonJavaObject = new HashMap<>();

    if (o instanceof DateTime) {
      // Special case for DateTime
      bsonJavaObject.put("v", ((DateTime) o).toDate());
    } else if (o instanceof Map) {
      // Check if the Map contains Date objects and replace them
      @SuppressWarnings("unchecked")
      Map<String, Object> aggregate = (Map) o;
      for (Map.Entry<String, Object> entry : aggregate.entrySet()) {
        if (entry.getValue() instanceof DateTime) {
          entry.setValue(((DateTime) entry.getValue()).toDate());
        }
      }
      bsonJavaObject.put("v", aggregate);
    } else {
      bsonJavaObject.put("v", o);
    }
    if (t != null) {
      bsonJavaObject.put("t", t.toDate());
    }
    Document bsonDocument = new Document(bsonJavaObject);

    BasicOutputBuffer out = new BasicOutputBuffer();
    byte[] documentAsByteArray = null;

    try (BsonBinaryWriter w = new BsonBinaryWriter(out)) {
      new DocumentCodec().encode(w, bsonDocument, EncoderContext.builder().build());
      documentAsByteArray = out.toByteArray();
    }

    return documentAsByteArray;
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
      e.printStackTrace();
    }
  }

  private void handlePurgeProperties(byte[] payload) {
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
      e.printStackTrace();
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
      m_propertyStorage.purgeProperties(availableProperties);
    }
  }

  @Override
  protected void onConnected(IMqttToken asyncActionToken) {
    // Called when connection has succeeded
    // Send introspection in case it's needed
    if (!asyncActionToken.getSessionPresent() || !m_introspectionSent) {
      try {
        // Set up subscriptions
        setupSubscriptions();
        // Prepare introspection and all
        sendIntrospection();
        sendEmptyCache();
        m_introspectionSent = true;
        // Send all properties
        resendAllProperties();
      } catch (AstarteTransportException e) {
        e.printStackTrace();
      }
    }

    // Try to resend previously failed messages
    try {
      retryFailedMessages();
    } catch (AstarteTransportException e) {
      // TODO: what should we do here?
      e.printStackTrace();
    }

    if (m_astarteTransportEventListener != null) {
      m_astarteTransportEventListener.onTransportConnected();
    } else {
      System.out.println("Transport Connected");
    }
  }
}
