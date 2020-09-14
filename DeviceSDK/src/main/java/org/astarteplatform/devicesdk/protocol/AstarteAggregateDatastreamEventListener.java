package org.astarteplatform.devicesdk.protocol;

public interface AstarteAggregateDatastreamEventListener {
  void valueReceived(AstarteAggregateDatastreamEvent e);
}
