package org.astarteplatform.devicesdk.protocol;

import java.util.Collection;
import java.util.HashSet;

public class AstarteServerAggregateDatastreamInterface extends AstarteAggregateDatastreamInterface {
  private final Collection<AstarteAggregateDatastreamEventListener> mListeners;

  AstarteServerAggregateDatastreamInterface() {
    mListeners = new HashSet<>();
  }

  public void addListener(AstarteAggregateDatastreamEventListener listener) {
    mListeners.add(listener);
  }

  public void removeListener(AstarteAggregateDatastreamEventListener listener) {
    mListeners.remove(listener);
  }

  public Collection<AstarteAggregateDatastreamEventListener> getAllListeners() {
    return mListeners;
  }
}
