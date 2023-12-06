package org.astarteplatform.devicesdk.tests.e2etest;

import java.io.IOException;
import java.util.*;
import org.astarteplatform.devicesdk.AstarteDevice;
import org.astarteplatform.devicesdk.protocol.*;
import org.astarteplatform.devicesdk.tests.e2etest.utilities.AstarteHttpRequest;
import org.astarteplatform.devicesdk.tests.e2etest.utilities.GenericDeviceMockData;
import org.astarteplatform.devicesdk.tests.e2etest.utilities.GenericDeviceMockDataFactory;
import org.astarteplatform.devicesdk.tests.e2etest.utilities.GenericMockDevice;
import org.astarteplatform.devicesdk.transport.AstarteTransportException;
import org.joda.time.DateTime;

public class GenericAggregateDevice implements AstarteAggregateDatastreamEventListener {
  private final GenericDeviceMockData m_deviceMockData;
  private final GenericMockDevice m_mockDevice;
  private final AstarteDevice m_astarteGenericDevice;

  private final AstarteHttpRequest astarteHttpRequest;

  public GenericAggregateDevice() throws Exception {
    m_deviceMockData = new GenericDeviceMockDataFactory();
    m_astarteGenericDevice = m_deviceMockData.getAstarteDeviceSingleton();
    m_mockDevice = m_deviceMockData.getMockData();
    astarteHttpRequest = new AstarteHttpRequest();
  }

  public void aggregateFromDeviceToServer()
      throws AstarteInvalidValueException, AstarteInterfaceMappingNotFoundException,
          AstarteTransportException, IOException, InterruptedException {

    System.out.println(
        "Test for aggregated object datastreams" + " in the direction from device to server.");
    String interfaceName = m_mockDevice.getInterfaceDeviceAggr();

    AstarteDeviceAggregateDatastreamInterface astarteDeviceAggregateDatastream =
        (AstarteDeviceAggregateDatastreamInterface)
            m_astarteGenericDevice.getInterface(interfaceName);

    astarteDeviceAggregateDatastream.streamData(
        "/sensor_id", m_mockDevice.getMockDataDictionary(), DateTime.now());

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

    if (m_mockDevice.checkEqualityOfHashMaps(object)) {
      System.exit(1);
    }
  }

  public void aggregateFromServerToDevice() throws IOException {
    System.out.println(
        "Test for aggregated object datastreams" + " in the direction from server to device.");
    String interfaceName = m_mockDevice.getInterfaceServerAggr();

    AstarteServerAggregateDatastreamInterface astarteServerAggregateDatastream =
        (AstarteServerAggregateDatastreamInterface)
            m_astarteGenericDevice.getInterface(interfaceName);

    astarteServerAggregateDatastream.addListener(this);

    Map<String, Object> data = m_mockDevice.getMockDataDictionary();

    astarteHttpRequest.postServerInterface(interfaceName, "/sensor_id", data);
  }

  @Override
  public void valueReceived(AstarteAggregateDatastreamEvent e) {

    System.out.println(
        "Received aggregate datastream value on interface "
            + e.getInterfaceName()
            + ", values: "
            + e.getValues());

    if (m_mockDevice.checkEqualityOfHashMaps(e.getValues())) {
      System.exit(1);
    }
  }
}
