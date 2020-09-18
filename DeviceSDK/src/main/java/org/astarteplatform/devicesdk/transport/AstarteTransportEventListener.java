package org.astarteplatform.devicesdk.transport;

public interface AstarteTransportEventListener {
  void onTransportConnected();

  void onTransportConnectionInitializationError(Throwable cause);

  void onTransportConnectionError(Throwable cause);

  void onTransportDisconnected();
}
