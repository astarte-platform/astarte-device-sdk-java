package org.astarteplatform.devicesdk.protocol;

import org.astarteplatform.devicesdk.transport.AstarteTransport;
import org.astarteplatform.devicesdk.transport.AstarteTransportException;
import org.joda.time.DateTime;

public class AstarteDeviceDatastreamInterface extends AstarteDatastreamInterface
    implements AstarteDataStreamer {

  @Override
  public void streamData(String path, Object payload)
      throws AstarteTransportException, AstarteInvalidValueException,
          AstarteInterfaceMappingNotFoundException {
    streamData(path, payload, null);
  }

  @Override
  public void streamData(String path, Object payload, DateTime timestamp)
      throws AstarteTransportException, AstarteInvalidValueException,
          AstarteInterfaceMappingNotFoundException {
    validatePayload(path, payload, timestamp);

    AstarteTransport transport = getAstarteTransport();
    if (transport == null) {
      throw new AstarteTransportException("No available transport");
    }

    transport.sendIndividualValue(this, path, payload, timestamp);
  }
}
