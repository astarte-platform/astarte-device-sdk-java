package org.astarteplatform.devicesdk.protocol;

import org.astarteplatform.devicesdk.AstartePropertyStorage;
import org.astarteplatform.devicesdk.transport.AstarteTransport;
import org.astarteplatform.devicesdk.transport.AstarteTransportException;

public class AstarteDevicePropertyInterface extends AstartePropertyInterface
    implements AstartePropertySetter {
  AstarteDevicePropertyInterface(AstartePropertyStorage propertyStorage) {
    super(propertyStorage);
  }

  @Override
  public void setProperty(String path, Object payload)
      throws AstarteTransportException, AstarteInvalidValueException,
          AstarteInterfaceMappingNotFoundException {
    validatePayload(this, path, payload, null);

    AstarteTransport transport = getAstarteTransport();
    if (transport == null) {
      throw new AstarteTransportException("No available transport");
    }

    transport.sendIndividualValue(this, path, payload);
    // Store it
    if (mPropertyStorage != null) {
      mPropertyStorage.setStoredValue(getInterfaceName(), path, payload);
    }
  }

  @Override
  public void unsetProperty(String path)
      throws AstarteTransportException, AstarteInterfaceMappingNotFoundException {

    AstarteTransport transport = getAstarteTransport();
    if (transport == null) {
      throw new AstarteTransportException("No available transport");
    }

    // Send an empty payload
    transport.sendIndividualValue(this, path, null);
    // Store it
    if (mPropertyStorage != null) {
      mPropertyStorage.removeStoredPath(getInterfaceName(), path);
    }
  }
}
