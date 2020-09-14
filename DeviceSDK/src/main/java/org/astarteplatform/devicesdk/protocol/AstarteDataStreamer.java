package org.astarteplatform.devicesdk.protocol;

import org.astarteplatform.devicesdk.transport.AstarteTransportException;
import org.joda.time.DateTime;

public interface AstarteDataStreamer {
  void streamData(String path, Object payload)
      throws AstarteTransportException, AstarteInvalidValueException,
          AstarteInterfaceMappingNotFoundException;

  void streamData(String path, Object payload, DateTime timestamp)
      throws AstarteTransportException, AstarteInvalidValueException,
          AstarteInterfaceMappingNotFoundException;
}
