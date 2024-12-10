package org.astarteplatform.devicesdk.tests.e2etest;

import java.io.IOException;
import java.util.*;
import org.astarteplatform.devicesdk.AstarteDevice;
import org.astarteplatform.devicesdk.protocol.*;
import org.astarteplatform.devicesdk.tests.e2etest.utilities.AstarteHttpRequest;
import org.astarteplatform.devicesdk.tests.e2etest.utilities.GenericDeviceSingleton;
import org.astarteplatform.devicesdk.tests.e2etest.utilities.GenericMockDevice;
import org.astarteplatform.devicesdk.transport.AstarteTransportException;
import org.joda.time.DateTime;

public class GenericAggregateDevice implements AstarteAggregateDatastreamEventListener {
  private final AstarteDevice m_astarteGenericDevice;

  private final AstarteHttpRequest astarteHttpRequest;

  public GenericAggregateDevice() throws Exception {
    m_astarteGenericDevice = GenericDeviceSingleton.getInstance();
    astarteHttpRequest = new AstarteHttpRequest();
  }

  public void aggregateFromDeviceToServer()
      throws AstarteInvalidValueException, AstarteInterfaceMappingNotFoundException,
          AstarteTransportException, IOException, InterruptedException {

    System.out.println(
        "Test for aggregated object datastreams" + " in the direction from device to server.");
    String interfaceName = GenericMockDevice.INTERFACE_DEVICE_AGGR;

    AstarteDeviceAggregateDatastreamInterface astarteDeviceAggregateDatastream =
        (AstarteDeviceAggregateDatastreamInterface)
            m_astarteGenericDevice.getInterface(interfaceName);

    astarteDeviceAggregateDatastream.streamData(
        "/sensor_id", GenericMockDevice.getMockDataDictionary(), DateTime.now());

    Thread.sleep(1000);

    List<Object> response =
        astarteHttpRequest
            .getServerInterface(interfaceName)
            .getJSONObject("data")
            .getJSONArray("sensor_id")
            .toList();

    if (response.isEmpty()) {
      System.exit(1);
    }

    Map<String, Object> object = (Map<String, Object>) response.get(0);

    object.remove("timestamp");

    if (GenericMockDevice.checkEqualityOfHashMaps(object)) {
      System.exit(1);
    }
  }

  public void aggregateFromServerToDevice() throws IOException {
    System.out.println(
        "Test for aggregated object datastreams" + " in the direction from server to device.");
    String interfaceName = GenericMockDevice.INTERFACE_SERVER_AGGR;

    AstarteServerAggregateDatastreamInterface astarteServerAggregateDatastream =
        (AstarteServerAggregateDatastreamInterface)
            m_astarteGenericDevice.getInterface(interfaceName);

    astarteServerAggregateDatastream.addListener(this);

    Map<String, Object> data = GenericMockDevice.getMockDataDictionary();

    astarteHttpRequest.postServerInterface(interfaceName, "/sensor_id", data);
  }

  @Override
  public void valueReceived(AstarteAggregateDatastreamEvent e) {

    System.out.println(
        "Received aggregate datastream value on interface "
            + e.getInterfaceName()
            + ", values: "
            + e.getValues());

    if (GenericMockDevice.checkEqualityOfHashMaps(e.getValues())) {
      System.exit(1);
    }
  }
}
