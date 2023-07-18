package org.astarteplatform.devicesdk.protocol;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.bson.BSONObject;
import org.joda.time.DateTime;

public class AstarteServerAggregateDatastreamInterface extends AstarteAggregateDatastreamInterface
    implements AstarteServerValueBuilder, AstarteServerValuePublisher {
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

  @Override
  public AstarteServerValue build(String interfacePath, Object serverValue, DateTime timestamp) {
    if (serverValue == null) {
      return null;
    }

    BSONObject astartePayload = (BSONObject) serverValue;
    Map<String, Object> astarteAggregate = new HashMap<>();
    // Build the map, and normalize payload where needed
    for (String key : astartePayload.keySet()) {
      for (Map.Entry<String, AstarteInterfaceMapping> m : getMappings().entrySet()) {
        if (AstarteInterface.isPathCompatibleWithMapping(
            interfacePath + "/" + key, m.getValue().getPath())) {
          if (m.getValue().getType() == DateTime.class) {
            // Replace the value
            astarteAggregate.put(key, new DateTime(astartePayload.get(key)));
          } else {
            astarteAggregate.put(key, astartePayload.get(key));
          }
        }
      }
    }

    return new AstarteServerValue.AstarteServerValueBuilder(astarteAggregate)
        .interfacePath(interfacePath)
        .build();
  }

  @Override
  public void publish(AstarteServerValue payload) {
    AstarteAggregateDatastreamEvent e =
        new AstarteAggregateDatastreamEvent(
            getInterfaceName(),
            payload.getMapValue(),
            payload.getTimestamp(),
            payload.getInterfacePath());
    for (AstarteAggregateDatastreamEventListener listener : mListeners) {
      listener.valueReceived(e);
    }
  }
}
