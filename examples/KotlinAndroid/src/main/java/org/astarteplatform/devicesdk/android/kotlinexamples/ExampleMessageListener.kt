package org.astarteplatform.devicesdk.android.kotlinexamples

import android.util.Log
import org.astarteplatform.devicesdk.AstarteMessageListener

class ExampleMessageListener(activity: MainActivity?) : AstarteMessageListener {
    private val TAG = "ExampleMessageListener"
    private val mActivity: MainActivity? = activity

    override fun onConnected() {
        /*
        * This function gets called when the device establishes the connection
        * with the broker.
        *
        * When the connection is established, we enable the "Send ping" button.
        */
        Log.i(TAG, "Device connected")
        mActivity?.enablePingButton(true)
    }

    override fun onDisconnected(cause: Throwable?) {
        /*
        * This function gets called when the device loses the connection with the
        * broker.
        *
        * If the connection is lost, we disable the "Send ping" button.
        */
        Log.i(TAG, "Device disconnected: " + cause?.message)
        mActivity?.enablePingButton(false)
    }

    override fun onFailure(cause: Throwable) {
        /*
        * This function gets called when the device encounters an error during its
        * lifetime.
        */
        Log.w(TAG, "Device failure: " + cause.message)
    }
}
