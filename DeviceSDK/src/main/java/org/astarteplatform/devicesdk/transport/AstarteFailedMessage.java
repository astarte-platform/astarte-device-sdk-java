package org.astarteplatform.devicesdk.transport;

public interface AstarteFailedMessage {
  String getTopic();

  byte[] getPayload();

  int getQos();

  boolean isExpired();
}
