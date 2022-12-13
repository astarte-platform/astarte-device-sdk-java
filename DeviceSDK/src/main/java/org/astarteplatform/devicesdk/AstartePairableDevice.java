package org.astarteplatform.devicesdk;

import org.astarteplatform.devicesdk.crypto.AstarteCryptoException;
import org.astarteplatform.devicesdk.crypto.AstarteCryptoStore;
import org.astarteplatform.devicesdk.protocol.AstarteInterface;
import org.astarteplatform.devicesdk.protocol.AstarteInterfaceAlreadyPresentException;
import org.astarteplatform.devicesdk.protocol.AstarteInterfaceNotFoundException;
import org.astarteplatform.devicesdk.protocol.AstarteInvalidInterfaceException;
import org.astarteplatform.devicesdk.transport.AstarteFailedMessageStorage;
import org.astarteplatform.devicesdk.transport.AstarteTransport;
import org.astarteplatform.devicesdk.transport.AstarteTransportEventListener;
import org.astarteplatform.devicesdk.transport.AstarteTransportException;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class AstartePairableDevice extends AstarteDevice
    implements AstarteTransportEventListener {
  private AstartePairingHandler mPairingHandler;
  private AstarteTransport mAstarteTransport;
  private AstarteMessageListener mAstarteMessageListener;
  private boolean mInitialized;
  private boolean mExplicitDisconnectionRequest;
  private java.util.Timer mReconnectTimer;

  protected AstartePairableDevice(
      AstartePairingHandler pairingHandler,
      AstartePropertyStorage propertyStorage,
      AstarteFailedMessageStorage failedMessageStorage,
      AstarteInterfaceProvider interfaceProvider)
      throws JSONException, AstarteInvalidInterfaceException {
    super(interfaceProvider, propertyStorage, failedMessageStorage);
    mPairingHandler = pairingHandler;
  }

  private void init() throws AstartePairingException {
    mPairingHandler.init();
    // Get and configure the first available transport
    setFirstTransportFromPairingHandler();
  }

  @Override
  public String getDeviceId() {
    return mPairingHandler.getDeviceId();
  }

  @Override
  public String getAstarteRealm() {
    return mPairingHandler.getAstarteRealm();
  }

  public AstarteCryptoStore getCryptoStore() {
    return mPairingHandler.m_cryptoStore;
  }

  public AstarteTransport getAstarteTransport() {
    return mAstarteTransport;
  }

  public void setAstarteTransport(AstarteTransport astarteTransport) {
    this.mAstarteTransport = astarteTransport;
    configureTransport();
  }

  @Override
  public AstarteMessageListener getAstarteMessageListener() {
    return mAstarteMessageListener;
  }

  @Override
  public void setAstarteMessageListener(AstarteMessageListener astarteMessageListener) {
    this.mAstarteMessageListener = astarteMessageListener;
    if (mAstarteTransport != null && astarteMessageListener != null) {
      mAstarteTransport.setMessageListener(astarteMessageListener);
    }
  }

  private boolean eventuallyReconnect() {
    synchronized (this) {
      if (alwaysReconnects() && mReconnectTimer == null) {
        mReconnectTimer = new java.util.Timer();
        // Retry in 5 seconds, and after 15 seconds
        mReconnectTimer.schedule(
            new java.util.TimerTask() {
              @Override
              public void run() {
                try {
                  connect();
                } catch (Exception e) {
                  mAstarteMessageListener.onFailure(e);
                }
              }
            },
            5000,
            15000);
        return true;
      }

      mExplicitDisconnectionRequest = false;
      return false;
    }
  }

  @Override
  public void connect()
      throws AstarteTransportException, AstarteCryptoException, AstartePairingException {
    synchronized (this) {
      if (!mInitialized) {
        try {
          init();
        } catch (Exception e) {
          if (!eventuallyReconnect()) {
            throw e;
          }
          return;
        }
        mInitialized = true;
      }

      if (isConnected()) {
        return;
      }

      if (!mPairingHandler.isCertificateAvailable()) {
        mPairingHandler.requestNewCertificate();
      }

      try {
        mAstarteTransport.connect();
      } catch (AstarteCryptoException e) {
        System.err.println("Regenerating the cert");
        // Generate the certificate and try again
        try {
          mPairingHandler.requestNewCertificate();
        } catch (AstartePairingException ex) {
          onTransportConnectionError(ex);
          return;
        }
        if (!eventuallyReconnect()) {
          // Try connecting again (don't catch this time)
          mAstarteTransport.connect();
        }
      }
    }
  }

  @Override
  public void disconnect() throws AstarteTransportException {
    synchronized (this) {
      if (mReconnectTimer != null) {
        mExplicitDisconnectionRequest = true;
      }

      if (!isConnected()) {
        return;
      }

      mExplicitDisconnectionRequest = true;
      mAstarteTransport.disconnect();
    }
  }

  @Override
  public boolean isConnected() {
    boolean connected;
    if (mAstarteTransport != null) {
      connected = mAstarteTransport.isConnected();
    } else {
      connected = false;
    }
    return connected;
  }

  @Override
  public void onTransportConnected() {
    synchronized (this) {
      if (mAstarteMessageListener != null) {
        mAstarteMessageListener.onConnected();
      }
      if (mReconnectTimer != null) {
        mReconnectTimer.cancel();
        mReconnectTimer = null;
      }
    }
  }

  @Override
  public void onTransportConnectionInitializationError(Throwable cause) {
    synchronized (this) {
      if (mAstarteMessageListener != null) {
        mAstarteMessageListener.onFailure(cause);
      } else {
        cause.printStackTrace();
      }

      // Disconnect and reconnect in a separate thread since we can't call
      // disconnect() in a callback.
      new Thread(
              new Runnable() {
                public void run() {
                  try {
                    disconnect();
                    // Manually call this since we explicitly disconnect, in case the user
                    // is handling reconnection manually
                    if (mAstarteMessageListener != null) {
                      mAstarteMessageListener.onDisconnected(cause);
                    }
                  } catch (AstarteTransportException e) {
                    // Not much that we can do here, we are reconnecting below
                    e.printStackTrace();
                  }
                  eventuallyReconnect();
                }
              })
          .start();
    }
  }

  @Override
  public void onTransportConnectionError(Throwable cause) {
    synchronized (this) {
      if (cause instanceof AstarteCryptoException) {
        System.err.println("Regenerating the cert");
        // Generate the certificate and try again
        try {
          mPairingHandler.requestNewCertificate();

          setFirstTransportFromPairingHandler();
        } catch (AstartePairingException e) {
          if (!eventuallyReconnect()) {
            mAstarteMessageListener.onFailure(e);
            e.printStackTrace();
          }
          return;
        }

        // If we got this, connect again anyway
        try {
          mAstarteTransport.connect();
        } catch (Exception e) {
          // Unrecoverable
          if (mAstarteMessageListener != null) {
            mAstarteMessageListener.onFailure(e);
          } else {
            e.printStackTrace();
          }
        }
      } else {
        if (!eventuallyReconnect()) {
          if (mAstarteMessageListener != null) {
            mAstarteMessageListener.onFailure(cause);
          }
        }
      }
    }
  }

  @Override
  public void onTransportDisconnected() {
    synchronized (this) {
      if (mAstarteMessageListener != null) {
        mAstarteMessageListener.onDisconnected(new AstarteTransportException("Connection lost"));
      }

      if (alwaysReconnects() && !mExplicitDisconnectionRequest) {
        // Reconnect
        eventuallyReconnect();
      }

      mExplicitDisconnectionRequest = false;
    }
  }

  private void setFirstTransportFromPairingHandler() throws AstartePairingException {
    mAstarteTransport = mPairingHandler.getTransports().get(0);
    if (mAstarteTransport == null) {
      throw new AstartePairingException(
          "Astarte returned no supported transports " + "for the Device!");
    }
    configureTransport();
  }

  private void configureTransport() {
    mAstarteTransport.setDevice(this);
    mAstarteTransport.setPropertyStorage(mPropertyStorage);
    mAstarteTransport.setFailedMessageStorage(mFailedMessageStorage);
    mAstarteTransport.setAstarteTransportEventListener(this);
    if (mAstarteMessageListener != null) {
      mAstarteTransport.setMessageListener(mAstarteMessageListener);
    }

    // Set transport on all interfaces
    for (AstarteInterface astarteInterface : getAllInterfaces()) {
      astarteInterface.setAstarteTransport(mAstarteTransport);
    }
  }

  /**
   * Method that adds an interface dynamically to the introspection. If the device is connected the
   * updated introspection is sent to the server.
   *
   * @param astarteInterfaceObject the JSONObject representation of the interface
   * @throws AstarteInvalidInterfaceException when the JSON Object does not represent an interface
   *     correctly
   * @throws AstarteInterfaceAlreadyPresentException when an interface with the same name, major and
   *     minor is already present
   */
  @Override
  public void addInterface(JSONObject astarteInterfaceObject)
      throws AstarteInvalidInterfaceException, AstarteInterfaceAlreadyPresentException {
    super.addInterface(astarteInterfaceObject);
    if (isConnected()) {
      String interfaceName = astarteInterfaceObject.getString("interface_name");
      AstarteInterface astarteInterface = getInterface(interfaceName);
      astarteInterface.setAstarteTransport(mAstarteTransport);
      try {
        mAstarteTransport.sendIntrospection();
      } catch (AstarteTransportException e) {
        onTransportConnectionInitializationError(e);
      }
    }
  }

  /**
   * Method that dynamically removes an interface from the introspection. If the device is
   * connected, the updated introspection is sent to the server.
   *
   * @param interfaceName The name of the interface to remove
   * @throws AstarteInterfaceNotFoundException when no interface is found with the given name
   */
  @Override
  public void removeInterface(String interfaceName) throws AstarteInterfaceNotFoundException {
    super.removeInterface(interfaceName);
    if (isConnected()) {
      try {
        mAstarteTransport.sendIntrospection();
      } catch (AstarteTransportException e) {
        onTransportConnectionInitializationError(e);
      }
    }
  }
}
