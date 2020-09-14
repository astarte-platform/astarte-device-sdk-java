package org.astarteplatform.devicesdk.protocol;

import org.joda.time.DateTime;

public class AstarteDatastreamEvent extends AstarteGenericIndividualEvent {
  private final DateTime mTimestamp;

  public AstarteDatastreamEvent(
      String interfaceName, String path, Object value, DateTime timestamp) {
    super(interfaceName, path, value);
    mTimestamp = timestamp;
  }

  public AstarteDatastreamEvent(String interfaceName, String path, Object value) {
    super(interfaceName, path, value);
    mTimestamp = null;
  }

  public DateTime getTimestamp() {
    return mTimestamp;
  }
}
