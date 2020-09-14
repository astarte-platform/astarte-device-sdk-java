package org.astarteplatform.devicesdk.protocol;

import java.util.Map;
import org.joda.time.DateTime;

public class AstarteAggregateDatastreamEvent extends AstarteGenericAggregateEvent {
  private final DateTime mTimestamp;

  public AstarteAggregateDatastreamEvent(
      String interfaceName, Map<String, Object> values, DateTime timestamp) {
    super(interfaceName, values);
    mTimestamp = timestamp;
  }

  public AstarteAggregateDatastreamEvent(String interfaceName, Map<String, Object> values) {
    super(interfaceName, values);
    mTimestamp = null;
  }

  public DateTime getTimestamp() {
    return mTimestamp;
  }
}
