package org.astarteplatform.devicesdk;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.astarteplatform.devicesdk.crypto.AstarteCryptoException;
import org.astarteplatform.devicesdk.protocol.AstarteGlobalEventListener;
import org.astarteplatform.devicesdk.protocol.AstarteInterface;
import org.astarteplatform.devicesdk.protocol.AstarteInvalidInterfaceException;
import org.astarteplatform.devicesdk.protocol.AstarteServerAggregateDatastreamInterface;
import org.astarteplatform.devicesdk.protocol.AstarteServerDatastreamInterface;
import org.astarteplatform.devicesdk.protocol.AstarteServerPropertyInterface;
import org.astarteplatform.devicesdk.transport.AstarteFailedMessageStorage;
import org.astarteplatform.devicesdk.transport.AstarteTransportException;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class AstarteDevice {
  private final Map<String, AstarteInterface> mAstarteInterfaces;
  protected final AstartePropertyStorage mPropertyStorage;
  protected final AstarteFailedMessageStorage mFailedMessageStorage;
  protected boolean mAlwaysReconnect = false;

  AstarteDevice(
      AstarteInterfaceProvider astarteInterfaceProvider,
      AstartePropertyStorage propertyStorage,
      AstarteFailedMessageStorage failedMessageStorage)
      throws JSONException, AstarteInvalidInterfaceException {
    mPropertyStorage = propertyStorage;
    mFailedMessageStorage = failedMessageStorage;
    Collection<JSONObject> astarteInterfaces = astarteInterfaceProvider.loadAllInterfaces();
    mAstarteInterfaces = new HashMap<>();
    for (JSONObject astarteInterface : astarteInterfaces) {
      AstarteInterface theInterface = AstarteInterface.fromJSON(astarteInterface, mPropertyStorage);
      mAstarteInterfaces.put(theInterface.getInterfaceName(), theInterface);
    }
  }

  public boolean hasInterface(String interfaceName) {
    return mAstarteInterfaces.containsKey(interfaceName);
  }

  public Collection<String> getAllInterfaceNames() {
    return mAstarteInterfaces.keySet();
  }

  public Collection<AstarteInterface> getAllInterfaces() {
    return mAstarteInterfaces.values();
  }

  public AstarteInterface getInterface(String interfaceName) {
    return mAstarteInterfaces.get(interfaceName);
  }

  public void addGlobalEventListener(AstarteGlobalEventListener eventListener) {
    for (Map.Entry<String, AstarteInterface> interfaceEntry : mAstarteInterfaces.entrySet()) {
      AstarteInterface astarteInterface = interfaceEntry.getValue();
      if (astarteInterface instanceof AstarteServerPropertyInterface) {
        ((AstarteServerPropertyInterface) astarteInterface).addListener(eventListener);
      } else if (astarteInterface instanceof AstarteServerDatastreamInterface) {
        ((AstarteServerDatastreamInterface) astarteInterface).addListener(eventListener);
      } else if (astarteInterface instanceof AstarteServerAggregateDatastreamInterface) {
        ((AstarteServerAggregateDatastreamInterface) astarteInterface).addListener(eventListener);
      }
    }
  }

  public void removeGlobalListener(AstarteGlobalEventListener eventListener) {
    for (Map.Entry<String, AstarteInterface> interfaceEntry : mAstarteInterfaces.entrySet()) {
      AstarteInterface astarteInterface = interfaceEntry.getValue();
      if (astarteInterface instanceof AstarteServerPropertyInterface) {
        ((AstarteServerPropertyInterface) astarteInterface).removeListener(eventListener);
      } else if (astarteInterface instanceof AstarteServerDatastreamInterface) {
        ((AstarteServerDatastreamInterface) astarteInterface).removeListener(eventListener);
      } else if (astarteInterface instanceof AstarteServerAggregateDatastreamInterface) {
        ((AstarteServerAggregateDatastreamInterface) astarteInterface)
            .removeListener(eventListener);
      }
    }
  }

  public abstract String getDeviceId();

  public abstract String getAstarteRealm();

  public abstract AstarteMessageListener getAstarteMessageListener();

  public abstract void setAstarteMessageListener(AstarteMessageListener astarteMessageListener);

  public abstract void connect()
      throws AstarteTransportException, AstarteCryptoException, AstartePairingException;

  public abstract void disconnect() throws AstarteTransportException;

  public abstract boolean isConnected();

  public boolean alwaysReconnects() {
    return mAlwaysReconnect;
  }

  public void setAlwaysReconnect(boolean alwaysReconnect) {
    this.mAlwaysReconnect = alwaysReconnect;
  }
}
