package org.astarteplatform.devicesdk.protocol;

public interface AstartePropertyEventListener {
  void propertyReceived(AstartePropertyEvent e);

  void propertyUnset(AstartePropertyEvent e);
}
