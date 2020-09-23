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
    validateAggregate(this, path, payload, timestamp);

    AstarteTransport transport = getAstarteTransport();
    if (transport == null) {
      throw new AstarteTransportException("No available transport");
    }

    transport.sendAggregate(this, path, payload, timestamp);
  }
}
