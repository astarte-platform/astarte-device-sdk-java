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
    final Map<String, AstarteInterfaceMapping> mappings = getMappings();

    // Check path with mapping prefix
    String firstMappingPath = mappings.entrySet().iterator().next().getValue().getPath();
    String prefix = firstMappingPath.substring(0, firstMappingPath.lastIndexOf("/"));
    String[] splitMappingPath = prefix.split("/");
    String[] splitPath = path.split("/");
    if (splitPath.length != splitMappingPath.length) {
      throw new AstarteInterfaceMappingNotFoundException(
          String.format("%s not found in interface", path));
    }
    for (int i = 0; i < splitPath.length; i++) {
      if (!splitPath[i].equals(splitMappingPath[i]) && !splitMappingPath[i].startsWith("%{")) {
        throw new AstarteInterfaceMappingNotFoundException(
            String.format("%s not found in interface", path));
      }
    }

    String formattedPath = prefix + "/";

    for (Map.Entry<String, AstarteInterfaceMapping> interfaceMappingEntry : mappings.entrySet()) {
      final AstarteInterfaceMapping astarteInterfaceMapping = interfaceMappingEntry.getValue();
      if (!payload.containsKey(
          astarteInterfaceMapping.getPath().substring((formattedPath).length()))) {
        throw new AstarteInvalidValueException(
            String.format("Value not found for %s", astarteInterfaceMapping.getPath()));
      }
    }

    for (Map.Entry<String, Object> data : payload.entrySet()) {
      if (mappings.containsKey(formattedPath + data.getKey())) {
        findMappingInInterface(formattedPath + data.getKey())
            .validatePayload(data.getValue(), timestamp);
      } else {
        throw new AstarteInterfaceMappingNotFoundException(
            String.format("%s not found in interface", formattedPath + data.getKey()));
      }
    }
  }
}
