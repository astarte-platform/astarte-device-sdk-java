package org.astarteplatform.devicesdk.tests.e2etest.utilities;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import org.astarteplatform.devicesdk.AstarteDevice;
import org.astarteplatform.devicesdk.generic.AstarteGenericDevice;

public class GenericDeviceSingleton {
  private static AstarteDevice astarteDevice;

  private GenericDeviceSingleton() {}

  public static AstarteDevice getInstance() throws Exception {

    if (astarteDevice == null) {

      JdbcConnectionSource connectionSource = null;

      connectionSource = new JdbcConnectionSource("jdbc:h2:mem:testDb");

      astarteDevice =
          new AstarteGenericDevice(
              GenericMockDevice.getDeviceId(),
              GenericMockDevice.getRealm(),
              GenericMockDevice.getCredentialsSecret(),
              new GenericInterfaceProvider(),
              GenericMockDevice.getPairingUrl(),
              connectionSource);

      astarteDevice.setAstarteMessageListener(new GenericMessageListener());

      astarteDevice.setAlwaysReconnect(true);

      astarteDevice.connect();

      while (!astarteDevice.isConnected()) {
        Thread.sleep(100);
      }

      return astarteDevice;
    }
    return astarteDevice;
  }
}
