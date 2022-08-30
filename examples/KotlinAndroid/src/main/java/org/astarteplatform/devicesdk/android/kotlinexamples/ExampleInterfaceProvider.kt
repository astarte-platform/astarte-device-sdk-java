package org.astarteplatform.devicesdk.android.kotlinexamples

import android.content.Context
import org.astarteplatform.devicesdk.AstarteInterfaceProvider
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class ExampleInterfaceProvider(context: Context?) : AstarteInterfaceProvider {
    private val mContext: Context? = context;

    override fun loadAllInterfaces(): MutableCollection<JSONObject> {
        /*
        * loadAllInterfaces must return all the interfaces supported by this device.
        *
        * Here we load the interfaces from JSON files that are in the resources folder.
        *
        * The interfaces used in this example are the Astarte standard-interfaces present in
        * the main Astarte repository.
        */
        val interfaceNames = arrayOf(
            "org.astarte-platform.genericevents.DeviceEvents",
            "org.astarte-platform.genericcommands.ServerCommands"
        )
        val interfaces: MutableCollection<JSONObject> = HashSet()

        for (interfaceName in interfaceNames) {
            try {
                val jsonStr = loadJSONFromAsset(interfaceName)

                if (jsonStr == null || jsonStr.isBlank()) {
                    continue
                }

                val obj = JSONObject(jsonStr)
                interfaces.add(obj)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        return interfaces
    }

    private fun loadJSONFromAsset(interfaceName: String): String? {
        return try {
            val inputStream = mContext?.assets?.open("standard-interfaces/$interfaceName.json")
            val size = inputStream?.available() ?: 0
            val buffer = ByteArray(size)
            inputStream?.read(buffer)
            inputStream?.close()
            String(buffer, Charsets.UTF_8)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}
