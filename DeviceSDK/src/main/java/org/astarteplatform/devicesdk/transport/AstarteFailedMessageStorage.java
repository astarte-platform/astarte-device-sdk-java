package org.astarteplatform.devicesdk.transport;

public interface AstarteFailedMessageStorage {
  void insertVolatile(String topic, byte[] payload, int qos);

  void insertVolatile(String topic, byte[] payload, int qos, int relativeExpiry);

  void insertStored(String topic, byte[] payload, int qos) throws AstarteTransportException;

  void insertStored(String topic, byte[] payload, int qos, int relativeExpiry)
      throws AstarteTransportException;

  boolean isEmpty();

  AstarteFailedMessage peekFirst();

  void ackFirst() throws AstarteTransportException;

  void rejectFirst() throws AstarteTransportException;
}
