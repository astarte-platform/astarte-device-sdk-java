package org.astarteplatform.devicesdk.android.examples;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
  private String TAG = "MainActivity";
  /*
   * You should populate all this variables with the values you're using in your Astarte instance.
   *
   * You can register a device and obtain its credentials secret with astartectl or using the
   * Astarte dashboard.
   */
  private String mDeviceId = "<device-id>";
  private String mRealm = "<realm>";
  private String mCredentialsSecret = "<credentials-secret>";
  private String mPairingUrl = "<pairing-url>";

  private AstarteDeviceService mDeviceService;
  private Button mPublishButton;
  private TextView mCommandTextView;
  ExecutorService mExecutorService = Executors.newFixedThreadPool(4);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    // Save the publish button, and disable it at startup
    mPublishButton = findViewById(R.id.publishButton);
    mPublishButton.setEnabled(false);
    mCommandTextView = findViewById(R.id.cmdTextView);
    /*
     * AstarteDevice can't be executed in the UI thread since it's using Rooms for its persistence.
     *
     * Here we make a separate class and we pass it a thread pool to execute its code on, but you
     * are free to use any other Android abstraction you see fit (IntentService, WorkManager etc).
     */
    mDeviceService = new AstarteDeviceService(this, mExecutorService);
    mDeviceService.init(mRealm, mDeviceId, mCredentialsSecret, mPairingUrl);
  }

  /*
   * We expose this function to make the handlers able to enable or disable the ping button when the
   * device connects or disconnects.
   *
   * The code is executed using View.post since it must be executed on the UI thread and we are
   * calling this function from a different thread.
   */
  public void enablePingButton(final boolean enabled) {
    mPublishButton.post(
        new Runnable() {
          @Override
          public void run() {
            mPublishButton.setEnabled(enabled);
          }
        });
  }

  /*
   * We expose this function to make the handlers able to set the last received command text.
   *
   * The code is executed using View.post since it must be executed on the UI thread and we are
   * calling this function from a different thread.
   */
  public void setCommandsText(final String commandsText) {
    mCommandTextView.post(
        new Runnable() {
          @Override
          public void run() {
            mCommandTextView.setText(commandsText);
          }
        });
  }

  /*
   * Send ping button handler
   */
  public void sendPing(View v) {
    mDeviceService.sendPing();
  }

  /*
   * This callback gets called when the device is correctly initialized
   */
  public void onAstarteServiceInitialized() {
    Log.i(TAG, "Astarte Device initialized");
    try {
      mDeviceService.connect();
    } catch (Exception e) {
      Log.w(TAG, "Connection failed");
      e.printStackTrace();
    }
  }

  /*
   * This callback gets called if there's an error in the Astarte Service.
   *
   * This just prints the stack trace, errors should be handled more gracefully in a production
   * setup.
   */
  public void onAstarteServiceError(Exception e) {
    Log.w(TAG, "Astarte Device error");
    e.printStackTrace();
  }
}
