package org.astarteplatform.devicesdk.protocol;

import java.util.Collection;
import java.util.HashSet;
import org.astarteplatform.devicesdk.AstartePropertyStorage;

public class AstarteServerPropertyInterface extends AstartePropertyInterface {
  private final Collection<AstartePropertyEventListener> mListeners;

  AstarteServerPropertyInterface(AstartePropertyStorage propertyStorage) {
    super(propertyStorage);
    mListeners = new HashSet<>();
  }

  public void addListener(AstartePropertyEventListener listener) {
    mListeners.add(listener);
  }

  public void removeListener(AstartePropertyEventListener listener) {
    mListeners.remove(listener);
  }

  public Collection<AstartePropertyEventListener> getAllListeners() {
    return mListeners;
  }
}
