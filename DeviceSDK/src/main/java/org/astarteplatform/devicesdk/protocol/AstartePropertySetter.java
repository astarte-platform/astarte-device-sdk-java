package org.astarteplatform.devicesdk.protocol;

import org.astarteplatform.devicesdk.transport.AstarteTransportException;

public interface AstartePropertySetter {
  void setProperty(String path, Object payload)
      throws AstarteTransportException, AstarteInvalidValueException,
          AstarteInterfaceMappingNotFoundException;

  void unsetProperty(String path)
      throws AstarteTransportException, AstarteInterfaceMappingNotFoundException;
}
