package org.astarteplatform.devicesdk.protocol;

import java.util.Map;
import org.astarteplatform.devicesdk.transport.AstarteTransport;
import org.astarteplatform.devicesdk.transport.AstarteTransportException;
import org.joda.time.DateTime;

public class AstarteDeviceAggregateDatastreamInterface extends AstarteAggregateDatastreamInterface
    implements AstarteAggregateDataStreamer {

  @Override
  public void streamData(String path, Map<String, Object> payload)
      throws AstarteTransportException, AstarteInvalidValueException,
          AstarteInterfaceMappingNotFoundException {
    streamData(path, payload, null);
  }

  @Override
  public void streamData(String path, Map<String, Object> payload, DateTime timestamp)
      throws AstarteTransportException, AstarteInvalidValueException,
          AstarteInterfaceMappingNotFoundException {
    validatePayload(path, payload, timestamp);

    AstarteTransport transport = getAstarteTransport();
    if (transport == null) {
      throw new AstarteTransportException("No available transport");
    }

    transport.sendAggregate(this, path, payload, timestamp);
  }

  public void validatePayload(String path, Map<String, Object> payload, DateTime timestamp)
      throws AstarteInvalidValueException, AstarteInterfaceMappingNotFoundException {
    String fomattedPath = path + "/";
    final Map<String, AstarteInterfaceMapping> mappings = getMappings();

    for (Map.Entry<String, AstarteInterfaceMapping> interfaceMappingEntry : mappings.entrySet()) {
      final AstarteInterfaceMapping astarteInterfaceMapping = interfaceMappingEntry.getValue();
      if (!payload.containsKey(
          astarteInterfaceMapping.getPath().substring((fomattedPath).length()))) {
        throw new AstarteInvalidValueException(
            String.format("Value not found for %s", astarteInterfaceMapping.getPath()));
      }
    }

    for (Map.Entry<String, Object> data : payload.entrySet()) {
      if (mappings.containsKey(fomattedPath + data.getKey())) {
        findMappingInInterface(fomattedPath + data.getKey())
            .validatePayload(data.getValue(), timestamp);
      } else {
        throw new AstarteInterfaceMappingNotFoundException(
            String.format("%s not found in interface", fomattedPath + data.getKey()));
      }
    }
  }
}
