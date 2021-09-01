package org.astarteplatform.devicesdk.protocol;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import org.joda.time.DateTime;

public class AstarteServerDatastreamInterface extends AstarteDatastreamInterface
    implements AstarteServerValueBuilder, AstarteServerValuePublisher {
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
              .timestamp(timestamp)
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
    AstarteDatastreamEvent e =
        new AstarteDatastreamEvent(
            getInterfaceName(),
            payload.getInterfacePath(),
            payload.getValue(),
            payload.getTimestamp());

    for (AstarteDatastreamEventListener listener : mListeners) {
      listener.valueReceived(e);
    }
  }
}
