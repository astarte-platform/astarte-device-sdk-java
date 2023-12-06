package org.astarteplatform.devicesdk.tests.e2etest.utilities;

import org.astarteplatform.devicesdk.AstarteDevice;

public interface GenericDeviceMockData {

  public AstarteDevice getAstarteDeviceSingleton() throws Exception;

  public GenericMockDevice getMockData() throws Exception;
}
