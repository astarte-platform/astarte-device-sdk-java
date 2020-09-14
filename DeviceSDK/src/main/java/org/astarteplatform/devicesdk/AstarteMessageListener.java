package org.astarteplatform.devicesdk;

public interface AstarteMessageListener {
  void onConnected();

  void onDisconnected(Throwable cause);

  void onFailure(Throwable cause);
}
