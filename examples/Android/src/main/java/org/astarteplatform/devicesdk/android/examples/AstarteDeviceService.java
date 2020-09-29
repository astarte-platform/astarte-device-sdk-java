package org.astarteplatform.devicesdk.android.examples;

import java.util.concurrent.Executor;
import org.astarteplatform.devicesdk.AstarteDevice;
import org.astarteplatform.devicesdk.AstartePairingException;
import org.astarteplatform.devicesdk.android.AstarteAndroidDevice;
import org.astarteplatform.devicesdk.crypto.AstarteCryptoException;
import org.astarteplatform.devicesdk.protocol.AstarteDeviceDatastreamInterface;
import org.astarteplatform.devicesdk.transport.AstarteTransportException;
import org.joda.time.DateTime;

public class AstarteDeviceService {
  private static final String EVENTS_INTERFACE = "org.astarte-platform.genericevents.DeviceEvents";
  private final Executor mExecutor;
  private final MainActivity mActivity;
  private AstarteDevice mDevice;

  /*
   * We keep a reference to the activity so we can pass it to the handlers, that will call some
   * callbacks on it.
   *
   * The call passes in an executor, all the Astarte Device code will be executed on it, since
   * it uses Rooms for its persistence, which can't be called in the UI thread. This is just an
   * example and you're free to use whatever Android threading abstraction you like, as long as
   * the Astarte Device code is not executed on the UI thread.
   */
  public AstarteDeviceService(MainActivity activity, Executor executor) {
    mActivity = activity;
    mExecutor = executor;
  }

  public void init(
      final String realm,
      final String deviceId,
      final String credentialsSecret,
      final String pairingUrl) {
    mExecutor.execute(
        new Runnable() {
          public void run() {
            try {
              /*
               * Astarte device creation
               *
               * The interfaces supported by the device are populated by
               * ExampleInterfaceProvider, see that class for more details
               */
              mDevice =
                  new AstarteAndroidDevice(
                      deviceId,
                      realm,
                      credentialsSecret,
                      new ExampleInterfaceProvider(mActivity),
                      pairingUrl,
                      mActivity.getApplicationContext());
            } catch (Exception e) {
              mActivity.onAstarteServiceError(e);
              return;
            }

            /*
             * Connect listeners
             *
             * See ExampleMessageListener to listen for device connection, disconnection
             * and failure.
             * See ExampleGlobalEventListener to listen for incoming data pushed from
             * Astarte.
             */
            mDevice.addGlobalEventListener(new ExampleGlobalEventListener(mActivity));
            mDevice.setAstarteMessageListener(new ExampleMessageListener(mActivity));

            /*
             * Set this if you want to let AstarteDevice take care of the reconnection.
             * The default is false, which means that the application is responsible of
             * reconnecting in case of failures
             */
            mDevice.setAlwaysReconnect(true);

            /*
             * Notify the activity that the device is ready to use
             */
            mActivity.onAstarteServiceInitialized();
          }
        });
  }

  public void connect()
      throws AstartePairingException, AstarteCryptoException, AstarteTransportException {
    mExecutor.execute(
        new Runnable() {
          @Override
          public void run() {
            try {
              /*
               * Start the connection. ExampleMessageListener will notify when the
               * connection is completed.
               */
              mDevice.connect();
            } catch (Exception e) {
              mActivity.onAstarteServiceError(e);
            }
          }
        });
  }

  public void sendPing() {
    mExecutor.execute(
        new Runnable() {
          @Override
          public void run() {
            try {
              /*
               * Publish on a datastream interface
               *
               * Retrieve the interface from the device and call streamData on it.
               */
              ((AstarteDeviceDatastreamInterface) mDevice.getInterface(EVENTS_INTERFACE))
                  .streamData("/event", "ping", DateTime.now());
            } catch (Exception e) {
              mActivity.onAstarteServiceError(e);
            }
          }
        });
  }
}
