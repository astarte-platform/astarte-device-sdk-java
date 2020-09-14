package org.astarteplatform.devicesdk.protocol;

import java.util.Map;
import org.astarteplatform.devicesdk.transport.AstarteTransportException;
import org.joda.time.DateTime;

public interface AstarteProtocol {
  AstarteProtocolType getAstarteProtocolType();

  void sendIntrospection() throws AstarteTransportException;

  void sendEmptyCache() throws AstarteTransportException;

  void resendAllProperties() throws AstarteTransportException;

  void sendIndividualValue(AstarteInterface astarteInterface, String path, Object value)
      throws AstarteTransportException;

  void sendIndividualValue(
      AstarteInterface astarteInterface, String path, Object value, DateTime timestamp)
      throws AstarteTransportException;

  void sendAggregate(
      AstarteAggregateDatastreamInterface astarteInterface, String path, Map<String, Object> value)
      throws AstarteTransportException;

  void sendAggregate(
      AstarteAggregateDatastreamInterface astarteInterface,
      String path,
      Map<String, Object> value,
      DateTime timestamp)
      throws AstarteTransportException;
}
