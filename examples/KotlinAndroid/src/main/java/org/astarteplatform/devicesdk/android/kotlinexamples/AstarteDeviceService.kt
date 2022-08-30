package org.astarteplatform.devicesdk.android.kotlinexamples

import org.astarteplatform.devicesdk.AstarteDevice
import org.astarteplatform.devicesdk.AstartePairingException
import org.astarteplatform.devicesdk.android.AstarteAndroidDevice
import org.astarteplatform.devicesdk.crypto.AstarteCryptoException
import org.astarteplatform.devicesdk.protocol.AstarteDeviceDatastreamInterface
import org.astarteplatform.devicesdk.transport.AstarteTransportException
import org.joda.time.DateTime
import java.util.concurrent.Executor

class AstarteDeviceService(
    activity: MainActivity,
    executor: Executor
) {
    private val EVENTS_INTERFACE = "org.astarte-platform.genericevents.DeviceEvents"

    /*
    * We keep a reference to the activity so we can pass it to the handlers, that will call some
    * callbacks on it.
    *
    * The call passes in an executor, all the Astarte Device code will be executed on it, since
    * it uses Rooms for its persistence, which can't be called in the UI thread. This is just an
    * example and you're free to use whatever Android threading abstraction you like, as long as
    * the Astarte Device code is not executed on the UI thread.
    */
    private var mExecutor: Executor? = executor
    private var mActivity: MainActivity? = activity

    private var mDevice: AstarteDevice? = null

    fun run(realm: String, deviceId: String, credentialsSecret: String, pairingUrl: String) {
        mExecutor?.execute(
            Runnable {
                mDevice = try {
                    /*
                     * Astarte device creation
                     *
                     * The interfaces supported by the device are populated by
                     * ExampleInterfaceProvider, see that class for more details
                     */
                    AstarteAndroidDevice(
                        deviceId,
                        realm,
                        credentialsSecret,
                        ExampleInterfaceProvider(
                            mActivity
                        ),
                        pairingUrl,
                        mActivity!!.applicationContext
                    )
                } catch (e: Exception) {
                    mActivity?.onAstarteServiceError(e)
                    return@Runnable
                }

                /*
                * Connect listeners
                *
                * See ExampleMessageListener to listen for device connection, disconnection
                * and failure.
                * See ExampleGlobalEventListener to listen for incoming data pushed from
                * Astarte.
                */
                mDevice?.addGlobalEventListener(
                    ExampleGlobalEventListener(
                        mActivity
                    )
                )
                mDevice?.astarteMessageListener = ExampleMessageListener(
                    mActivity
                )

                /*
                * Set this if you want to let AstarteDevice take care of the reconnection.
                * The default is false, which means that the application is responsible of
                * reconnecting in case of failures
                */
                mDevice?.setAlwaysReconnect(true)

                /*
                * Notify the activity that the device is ready to use
                */
                mActivity?.onAstarteServiceInitialized()
            })
    }

    @Throws(
        AstartePairingException::class,
        AstarteCryptoException::class,
        AstarteTransportException::class
    )
    fun connect() {
        mExecutor!!.execute {
            try {
                /*
                * Start the connection. ExampleMessageListener will notify when the
                * connection is completed.
                */
                mDevice?.connect()
            } catch (e: java.lang.Exception) {
                mActivity?.onAstarteServiceError(e)
            }
        }
    }

    fun sendPing() {
        mExecutor!!.execute {
            try {
                /*
                * Publish on a datastream interface
                *
                * Retrieve the interface from the device and call streamData on it.
                */
                (mDevice?.getInterface(EVENTS_INTERFACE) as AstarteDeviceDatastreamInterface)
                    .streamData("/event", "ping", DateTime.now())
            } catch (e: java.lang.Exception) {
                mActivity?.onAstarteServiceError(e)
            }
        }
    }
}
