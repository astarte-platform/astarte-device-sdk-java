package org.astarteplatform.devicesdk.tests.e2etest;

import org.astarteplatform.devicesdk.AstarteDevice;
import org.astarteplatform.devicesdk.tests.e2etest.utilities.GenericDeviceSingleton;

public class GenericDevice {

  private AstarteDevice m_astarteGenericDevice;

  public GenericDevice() throws Exception {
    m_astarteGenericDevice = GenericDeviceSingleton.getInstance();
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
