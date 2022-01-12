package org.astarteplatform.devicesdk;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.astarteplatform.devicesdk.crypto.AstarteCryptoException;
import org.astarteplatform.devicesdk.protocol.*;
import org.astarteplatform.devicesdk.transport.AstarteFailedMessageStorage;
import org.astarteplatform.devicesdk.transport.AstarteTransportException;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class AstarteDevice {
  private final Map<String, AstarteInterface> mAstarteInterfaces;
  protected final AstartePropertyStorage mPropertyStorage;
  protected final AstarteFailedMessageStorage mFailedMessageStorage;
  protected boolean mAlwaysReconnect;

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

  /**
   * Method that adds an interface dynamically to the introspection
   *
   * @param astarteInterfaceObject the JSONObject representation of the interface
   * @throws AstarteInvalidInterfaceException when the JSON Object does not represent an interface
   *     correctly
   * @throws AstarteInterfaceAlreadyPresentException when an interface with the same name, major and
   *     minor is already present
   */
  public void addInterface(JSONObject astarteInterfaceObject)
      throws AstarteInvalidInterfaceException, AstarteInterfaceAlreadyPresentException {
    AstarteInterface newInterface =
        AstarteInterface.fromJSON(astarteInterfaceObject, mPropertyStorage);
    AstarteInterface formerInterface = getInterface(newInterface.getInterfaceName());
    if (formerInterface != null
        && formerInterface.getMajorVersion() == newInterface.getMajorVersion()) {
      if (formerInterface.getMinorVersion() == newInterface.getMinorVersion()) {
        throw new AstarteInterfaceAlreadyPresentException("Interface already present in mapping");
      }
      if (formerInterface.getMinorVersion() > newInterface.getMinorVersion()) {
        throw new AstarteInvalidInterfaceException("Can't downgrade an interface at runtime");
      }
    }
    mAstarteInterfaces.put(newInterface.getInterfaceName(), newInterface);
  }

  /**
   * Method that dynamically removes an interface from the introspection
   *
   * @param interfaceName The name of the interface to remove
   * @throws AstarteInterfaceNotFoundException when no interface is found with the given name
   */
  public void removeInterface(String interfaceName) throws AstarteInterfaceNotFoundException {
    AstarteInterface formerInterface = getInterface(interfaceName);
    if (formerInterface == null) {
      throw new AstarteInterfaceNotFoundException("Interface " + interfaceName + " not found");
    }
    mAstarteInterfaces.remove(interfaceName);
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
