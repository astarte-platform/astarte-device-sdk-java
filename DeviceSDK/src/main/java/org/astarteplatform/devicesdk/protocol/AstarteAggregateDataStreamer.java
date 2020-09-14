package org.astarteplatform.devicesdk.protocol;

import java.util.Map;
import org.astarteplatform.devicesdk.transport.AstarteTransportException;
import org.joda.time.DateTime;

public interface AstarteAggregateDataStreamer {
  void streamData(String path, Map<String, Object> payload)
      throws AstarteTransportException, AstarteInvalidValueException,
          AstarteInterfaceMappingNotFoundException;

  void streamData(String path, Map<String, Object> payload, DateTime timestamp)
      throws AstarteTransportException, AstarteInvalidValueException,
          AstarteInterfaceMappingNotFoundException;
}
