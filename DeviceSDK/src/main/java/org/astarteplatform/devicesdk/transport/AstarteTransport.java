package org.astarteplatform.devicesdk.transport;

import java.util.Map;
import org.astarteplatform.devicesdk.AstarteDevice;
import org.astarteplatform.devicesdk.AstarteMessageListener;
import org.astarteplatform.devicesdk.AstartePropertyStorage;
import org.astarteplatform.devicesdk.crypto.AstarteCryptoException;
import org.astarteplatform.devicesdk.protocol.AstarteAggregateDatastreamInterface;
import org.astarteplatform.devicesdk.protocol.AstarteInterface;
import org.astarteplatform.devicesdk.protocol.AstarteProtocol;
import org.astarteplatform.devicesdk.protocol.AstarteProtocolType;

public abstract class AstarteTransport implements AstarteProtocol {
  private final AstarteProtocolType m_astarteProtocolType;
  private AstarteDevice mDevice;
  protected boolean m_introspectionSent = false;
  protected AstartePropertyStorage m_propertyStorage = null;
  protected AstarteFailedMessageStorage m_failedMessageStorage = null;
  protected AstarteMessageListener m_messageListener = null;
  protected AstarteTransportEventListener m_astarteTransportEventListener = null;

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
}
