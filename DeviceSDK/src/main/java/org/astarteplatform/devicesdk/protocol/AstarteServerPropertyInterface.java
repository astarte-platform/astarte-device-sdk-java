package org.astarteplatform.devicesdk.protocol;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import org.astarteplatform.devicesdk.AstartePropertyStorage;
import org.joda.time.DateTime;

public class AstarteServerPropertyInterface extends AstartePropertyInterface
    implements AstarteServerValueBuilder, AstarteServerValuePublisher {
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

  @Override
  public AstarteServerValue build(String interfacePath, Object serverValue, DateTime timestamp) {
    AstarteInterfaceMapping targetMapping = null;
    AstarteServerValue astarteServerValue;

    for (Map.Entry<String, AstarteInterfaceMapping> entry : getMappings().entrySet()) {
      if (AstarteInterface.isPathCompatibleWithMapping(interfacePath, entry.getKey())) {
        targetMapping = entry.getValue();
        break;
      }
    }

    if (targetMapping != null) {
      Object astarteValue = serverValue;
      if (targetMapping.getType() == DateTime.class) {
        // Convert manually
        astarteValue = new DateTime(serverValue);
      }

      astarteServerValue =
          new AstarteServerValue.AstarteServerValueBuilder(astarteValue)
              .interfacePath(interfacePath)
              .build();
    } else {
      // Couldn't find the mapping
      System.err.printf(
          "Got an unexpected path %s for interface %s!%n", interfacePath, getInterfaceName());

      astarteServerValue = null;
    }

    return astarteServerValue;
  }

  @Override
  public void publish(AstarteServerValue payload) {
    AstartePropertyEvent e =
        new AstartePropertyEvent(
            getInterfaceName(), payload.getInterfacePath(), payload.getValue());
    if (payload.getValue() == null) {
      for (AstartePropertyEventListener listener : mListeners) {
        listener.propertyUnset(e);
      }
    } else {
      for (AstartePropertyEventListener listener : mListeners) {
        listener.propertyReceived(e);
      }
    }
  }
}
