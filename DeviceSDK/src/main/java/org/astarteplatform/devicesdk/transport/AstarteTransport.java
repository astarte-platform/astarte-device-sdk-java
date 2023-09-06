package org.astarteplatform.devicesdk.transport;

import java.util.Map;
import java.util.logging.Logger;
import org.astarteplatform.devicesdk.AstarteDevice;
import org.astarteplatform.devicesdk.AstarteMessageListener;
import org.astarteplatform.devicesdk.AstartePropertyStorage;
import org.astarteplatform.devicesdk.AstartePropertyStorageException;
import org.astarteplatform.devicesdk.crypto.AstarteCryptoException;
import org.astarteplatform.devicesdk.protocol.AstarteAggregateDatastreamInterface;
import org.astarteplatform.devicesdk.protocol.AstarteInterface;
import org.astarteplatform.devicesdk.protocol.AstarteProtocol;
import org.astarteplatform.devicesdk.protocol.AstarteProtocolType;

public abstract class AstarteTransport implements AstarteProtocol {
  private final AstarteProtocolType m_astarteProtocolType;
  private AstarteDevice mDevice;
  private static Logger logger = Logger.getLogger(AstarteTransport.class.getName());
  protected boolean m_introspectionSent;
  protected AstartePropertyStorage m_propertyStorage;
  protected AstarteFailedMessageStorage m_failedMessageStorage;
  protected AstarteMessageListener m_messageListener;
  protected AstarteTransportEventListener m_astarteTransportEventListener;

  protected AstarteTransport(AstarteProtocolType type) {
    m_astarteProtocolType = type;
  }

  @Override
  public AstarteProtocolType getAstarteProtocolType() {
    return m_astarteProtocolType;
  }

  public AstarteDevice getDevice() {
    return mDevice;
  }

  public void setDevice(AstarteDevice device) {
    mDevice = device;
  }

  public AstarteMessageListener getMessageListener() {
    return m_messageListener;
  }

  public void setMessageListener(AstarteMessageListener messageListener) {
    this.m_messageListener = messageListener;
  }

  public AstartePropertyStorage getPropertyStorage() {
    return m_propertyStorage;
  }

  public void setPropertyStorage(AstartePropertyStorage m_propertyStorage) {
    this.m_propertyStorage = m_propertyStorage;
  }

  public AstarteFailedMessageStorage getFailedMessageStorage() {
    return m_failedMessageStorage;
  }

  public void setFailedMessageStorage(AstarteFailedMessageStorage failedMessageStorage) {
    this.m_failedMessageStorage = failedMessageStorage;
  }

  public AstarteTransportEventListener getAstarteTransportEventListener() {
    return m_astarteTransportEventListener;
  }

  public void setAstarteTransportEventListener(
      AstarteTransportEventListener m_astarteTransportEventListener) {
    this.m_astarteTransportEventListener = m_astarteTransportEventListener;
  }

  public abstract void connect() throws AstarteTransportException, AstarteCryptoException;

  public abstract void disconnect() throws AstarteTransportException;

  public abstract boolean isConnected();

  @Override
  public void sendIndividualValue(AstarteInterface astarteInterface, String path, Object value)
      throws AstarteTransportException {
    sendIndividualValue(astarteInterface, path, value, null);
  }

  @Override
  public void sendAggregate(
      AstarteAggregateDatastreamInterface astarteInterface, String path, Map<String, Object> value)
      throws AstarteTransportException {
    sendAggregate(astarteInterface, path, value, null);
  }

  protected void savePropertyToStorage(String interfaceName, String path, Object value)
      throws AstartePropertyStorageException {
    if (m_propertyStorage != null) {
      m_propertyStorage.setStoredValue(interfaceName, path, value);
    } else {
      logger.severe("Property storage invalid! Caching won't work");
    }
  }

  protected void removePropertyFromStorage(String interfaceName, String path)
      throws AstartePropertyStorageException {
    if (m_propertyStorage != null) {
      m_propertyStorage.removeStoredPath(interfaceName, path);
    } else {
      logger.severe("Property storage invalid! Caching won't work");
    }
  }
}
