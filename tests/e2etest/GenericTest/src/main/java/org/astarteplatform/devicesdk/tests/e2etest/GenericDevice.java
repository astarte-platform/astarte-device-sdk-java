package org.astarteplatform.devicesdk.tests.e2etest;

import org.astarteplatform.devicesdk.AstarteDevice;
import org.astarteplatform.devicesdk.tests.e2etest.utilities.GenericDeviceMockData;
import org.astarteplatform.devicesdk.tests.e2etest.utilities.GenericDeviceMockDataFactory;
import org.astarteplatform.devicesdk.tests.e2etest.utilities.GenericMockDevice;

public class GenericDevice {

  private GenericDeviceMockData m_deviceMockData;
  private AstarteDevice m_astarteGenericDevice;
  private GenericMockDevice m_mockDevice;

  public GenericDevice() throws Exception {
    m_deviceMockData = new GenericDeviceMockDataFactory();
    m_astarteGenericDevice = m_deviceMockData.getAstarteDeviceSingleton();
    m_mockDevice = m_deviceMockData.getMockData();
  }

  public static void main(String[] args) throws Exception {

    GenericPropertyDevice propertyDevice = new GenericPropertyDevice();
    propertyDevice.propertiesFromDeviceToServer();
    propertyDevice.propertiesFromServerToDevice();

    Thread.sleep(3000);

    GenericDatastreamDevice genericDatastreamDevice = new GenericDatastreamDevice();
    genericDatastreamDevice.datastreamFromDeviceToServer();
    genericDatastreamDevice.datastreamFromServerToDevice();

    Thread.sleep(3000);

    GenericAggregateDevice aggregateDevice = new GenericAggregateDevice();
    aggregateDevice.aggregateFromDeviceToServer();
    aggregateDevice.aggregateFromServerToDevice();

    Thread.sleep(3000);

    System.exit(0);
  }
}
