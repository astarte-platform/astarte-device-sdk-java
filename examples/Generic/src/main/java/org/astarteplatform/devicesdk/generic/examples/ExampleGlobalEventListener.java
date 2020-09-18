package org.astarteplatform.devicesdk.generic.examples;

import org.astarteplatform.devicesdk.protocol.AstarteAggregateDatastreamEvent;
import org.astarteplatform.devicesdk.protocol.AstarteDatastreamEvent;
import org.astarteplatform.devicesdk.protocol.AstarteGlobalEventListener;
import org.astarteplatform.devicesdk.protocol.AstartePropertyEvent;

class ExampleGlobalEventListener extends AstarteGlobalEventListener {
  public void propertyReceived(AstartePropertyEvent e) {
    /*
     * This function gets called when the device receives data on a server owned
     * properties interface.
     */
    System.out.println(
        "Received property on interface "
            + e.getInterfaceName()
            + ", path: "
            + e.getPath()
            + ", value: "
            + e.getValue());
  }

  public void propertyUnset(AstartePropertyEvent e) {
    /*
     * This function gets called when the device receives an unset on a server owned
     * properties interface.
     */
    System.out.println(
        "Received unset on interface " + e.getInterfaceName() + ", path: " + e.getPath());
  }

  public void valueReceived(AstarteDatastreamEvent e) {
    /*
     * This function gets called when the device receives data on a server owned
     * datastream interface with individual aggregation.
     */
    System.out.println(
        "Received datastream value on interface "
            + e.getInterfaceName()
            + ", path: "
            + e.getPath()
            + ", value: "
            + e.getValue());
  }

  public void valueReceived(AstarteAggregateDatastreamEvent e) {
    /*
     * This function gets called when the device receives data on a server owned
     * datastream interface with object aggregation.
     */
    System.out.println(
        "Received aggregate datastream value on interface "
            + e.getInterfaceName()
            + ", values: "
            + e.getValues());
  }
}
