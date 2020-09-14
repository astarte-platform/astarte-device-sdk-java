package org.astarteplatform.devicesdk.transport;

public interface AstarteTransportEventListener {
  void onTransportConnected();

  void onTransportConnectionError(Throwable cause);

  void onTransportDisconnected();
}
