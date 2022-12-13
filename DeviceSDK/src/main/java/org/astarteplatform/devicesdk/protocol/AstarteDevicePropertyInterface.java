package org.astarteplatform.devicesdk.protocol;

import org.astarteplatform.devicesdk.AstartePropertyStorage;
import org.astarteplatform.devicesdk.AstartePropertyStorageException;
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
    validatePayload(path, payload, null);

    AstarteTransport transport = getAstarteTransport();
    if (transport == null) {
      throw new AstarteTransportException("No available transport");
    }

    Object storedValue = null;

    if (mPropertyStorage != null) {
      try {
        storedValue = mPropertyStorage.getStoredValue(this, path);
      } catch (AstartePropertyStorageException e) {
        e.printStackTrace();
      }
    }

    // If the property changed send it
    if (!payload.equals(storedValue)) {
      transport.sendIndividualValue(this, path, payload);
    }

    // Store it
    if (mPropertyStorage != null) {
      try {
        mPropertyStorage.setStoredValue(getInterfaceName(), path, payload);
      } catch (AstartePropertyStorageException e) {
        throw new AstarteTransportException("Property storage failure", e);
      }
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
      try {
        mPropertyStorage.removeStoredPath(getInterfaceName(), path);
      } catch (AstartePropertyStorageException e) {
        throw new AstarteTransportException("Property storage failure", e);
      }
    }
  }
}
