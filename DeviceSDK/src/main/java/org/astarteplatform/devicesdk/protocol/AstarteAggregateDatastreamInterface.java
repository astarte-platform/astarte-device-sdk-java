package org.astarteplatform.devicesdk.protocol;

public abstract class AstarteAggregateDatastreamInterface extends AstarteDatastreamInterface {
  boolean explicitTimestamp;

  public boolean isExplicitTimestamp() {
    return explicitTimestamp;
  }
}
