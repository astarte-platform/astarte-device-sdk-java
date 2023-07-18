package org.astarteplatform.devicesdk.protocol;

import java.util.Map;
import org.joda.time.DateTime;

public class AstarteAggregateDatastreamEvent extends AstarteGenericAggregateEvent {
  private final DateTime mTimestamp;

  public AstarteAggregateDatastreamEvent(
      String interfaceName, Map<String, Object> values, DateTime timestamp, String interfacePath) {
    super(interfaceName, values, interfacePath);
    mTimestamp = timestamp;
  }

  public AstarteAggregateDatastreamEvent(
      String interfaceName, Map<String, Object> values, String interfacePath) {
    super(interfaceName, values, interfacePath);
    mTimestamp = null;
  }

  public DateTime getTimestamp() {
    return mTimestamp;
  }
}
