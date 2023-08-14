package org.astarteplatform.devicesdk.tests.e2etest.utilities;

import org.astarteplatform.devicesdk.AstarteDevice;

public class GenericDeviceMockDataFactory implements GenericDeviceMockData {
  @Override
  public AstarteDevice getAstarteDeviceSingleton() throws Exception {
    return GenericDeviceSingleton.getInstance();
  }

  @Override
  public GenericMockDevice getMockData() throws Exception {
    return new GenericMockDevice();
  }
}
