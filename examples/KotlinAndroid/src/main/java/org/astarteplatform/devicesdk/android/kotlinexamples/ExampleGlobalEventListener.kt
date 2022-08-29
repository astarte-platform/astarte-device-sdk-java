package org.astarteplatform.devicesdk.android.kotlinexamples

import android.util.Log
import org.astarteplatform.devicesdk.protocol.AstarteAggregateDatastreamEvent
import org.astarteplatform.devicesdk.protocol.AstarteDatastreamEvent
import org.astarteplatform.devicesdk.protocol.AstarteGlobalEventListener
import org.astarteplatform.devicesdk.protocol.AstartePropertyEvent

class ExampleGlobalEventListener(activity: MainActivity?) : AstarteGlobalEventListener() {
    private val TAG = "ExampleGlobalEventsListener"
    private val COMMANDS_INTERFACE = "org.astarte-platform.genericcommands.ServerCommands"
    private val mActivity: MainActivity? = activity

    override fun valueReceived(e: AstarteDatastreamEvent) {
        /*
        * This function gets called when the device receives data on a server owned
        * datastream interface with individual aggregation.
        *
        * We handle data coming from org.astarte-platform.genericcommands.ServerCommands/command
        * as special case, displaying it in the commands TextView.
        */
        if (e.interfaceName == COMMANDS_INTERFACE && e.path == "/command") {
            mActivity?.setCommandsText(e.valueString)
        } else {
            /*
       * Otherwise, we just print what we receive
       */
            Log.i(
                TAG,
                "Received datastream value on interface "
                        + e.interfaceName
                        + ", path: "
                        + e.path
                        + ", value: "
                        + e.value
            )
        }
    }

    override fun valueReceived(e: AstarteAggregateDatastreamEvent) {
        /*
        * This function gets called when the device receives data on a server owned
        * datastream interface with object aggregation.
        */
        Log.i(
            TAG,
            ("Received aggregate datastream value on interface "
                    + e.interfaceName
                    + ", values: "
                    + e.values)
        )
    }

    override fun propertyReceived(e: AstartePropertyEvent) {
        /*
        * This function gets called when the device receives data on a server owned
        * properties interface.
        */
        Log.i(
            TAG,
            ("Received property on interface "
                    + e.interfaceName
                    + ", path: "
                    + e.path
                    + ", value: "
                    + e.value)
        )
    }

    override fun propertyUnset(e: AstartePropertyEvent) {
        /*
        * This function gets called when the device receives an unset on a server owned
        * properties interface.
        */
        Log.i(TAG, "Received unset on interface " + e.interfaceName + ", path: " + e.path)
    }
}
