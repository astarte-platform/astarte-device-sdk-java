package org.astarteplatform.devicesdk.protocol;

import java.util.Collection;
import java.util.HashSet;

public class AstarteServerDatastreamInterface extends AstarteDatastreamInterface {
  private final Collection<AstarteDatastreamEventListener> mListeners;

  AstarteServerDatastreamInterface() {
    mListeners = new HashSet<>();
  }

  public void addListener(AstarteDatastreamEventListener listener) {
    mListeners.add(listener);
  }

  public void removeListener(AstarteDatastreamEventListener listener) {
    mListeners.remove(listener);
  }

  public Collection<AstarteDatastreamEventListener> getAllListeners() {
    return mListeners;
  }
}
