package org.astarteplatform.devicesdk.android.kotlinexamples

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import org.astarteplatform.devicesdk.android.kotlinexamples.databinding.ActivityMainBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    /*
    * You should populate all this variables with the values you're using in your Astarte instance.
    *
    * You can register a device and obtain its credentials secret with astartectl or using the
    * Astarte dashboard.
    */
    private val mDeviceId = "<device-id>"
    private val mRealm = "<realm>"
    private val mCredentialsSecret = "<credentials-secret>"
    private val mPairingUrl = "<pairing-url>"

    private var mDeviceService: AstarteDeviceService? = null

    private val mExecutorService: ExecutorService = Executors.newFixedThreadPool(4)
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.contentMain.publishButton.isEnabled = false

        /*
        * AstarteDevice can't be executed in the UI thread since it's using Rooms for its persistence.
        *
        * Here we make a separate class and we pass it a thread pool to execute its code on, but you
        * are free to use any other Android abstraction you see fit (IntentService, WorkManager etc).
        */
        mDeviceService = AstarteDeviceService(
            this,
            mExecutorService
        )

        mDeviceService?.run(mRealm, mDeviceId, mCredentialsSecret, mPairingUrl)
    }


    /*
    * We expose this function to make the handlers able to enable or disable the ping button when the
    * device connects or disconnects.
    *
    * The code is executed using View.post since it must be executed on the UI thread and we are
    * calling this function from a different thread.
    */
    fun enablePingButton(enabled: Boolean) {
        binding.contentMain.publishButton.post {
            binding.contentMain.publishButton.isEnabled = enabled
        }
    }


    /*
    * We expose this function to make the handlers able to set the last received command text.
    *
    * The code is executed using View.post since it must be executed on the UI thread and we are
    * calling this function from a different thread.
    */
    fun setCommandsText(commandsText: String?) {
        binding.contentMain.cmdTextView.post { binding.contentMain.cmdTextView.text = commandsText }
    }

    /*
    * Send ping button handler
    */
    fun sendPing(v: View?) {
        mDeviceService?.sendPing()
    }

    /*
    * This callback gets called when the device is correctly initialized
    */
    fun onAstarteServiceInitialized() {
        Log.i(TAG, "Astarte Device initialized")
        try {
            mDeviceService?.connect()
        } catch (e: Exception) {
            Log.w(TAG, "Connection failed")
            e.printStackTrace()
        }
    }

    /*
    * This callback gets called if there's an error in the Astarte Service.
    *
    * This just prints the stack trace, errors should be handled more gracefully in a production
    * setup.
    */
    fun onAstarteServiceError(e: Exception) {
        Log.w(TAG, "Astarte Device error")
        e.printStackTrace()
    }

}
