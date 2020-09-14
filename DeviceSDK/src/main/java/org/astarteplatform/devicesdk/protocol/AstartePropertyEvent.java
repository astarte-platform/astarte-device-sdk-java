package org.astarteplatform.devicesdk.protocol;

public class AstartePropertyEvent extends AstarteGenericIndividualEvent {
  public AstartePropertyEvent(String interfaceName, String path, Object value) {
    super(interfaceName, path, value);
  }
}
