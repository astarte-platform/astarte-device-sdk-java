package org.astarteplatform.devicesdk.tests.e2etest.utilities;

import org.astarteplatform.devicesdk.AstarteMessageListener;

public class GenericMessageListener implements AstarteMessageListener {
  public void onConnected() {
    /*
     * This function gets called when the device establishes the connection
     * with the broker.
     */
    System.out.println("Device connected");
  }

  public void onDisconnected(Throwable cause) {
    /*
     * This function gets called when the device loses the connection with the
     * broker.
     */
    System.out.println("Device disconnected: " + cause.getMessage());
  }

  public void onFailure(Throwable cause) {
    /*
     * This function gets called when the device encounters an error during its
     * lifetime.
     */
    System.out.println("Device failure: " + cause.getMessage());
  }
}
