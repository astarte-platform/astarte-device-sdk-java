package org.astarteplatform.devicesdk.android.examples;

import android.util.Log;
import org.astarteplatform.devicesdk.AstarteMessageListener;

public class ExampleMessageListener implements AstarteMessageListener {
  private String TAG = "ExampleMessageListener";
  private MainActivity mActivity;

  public ExampleMessageListener(MainActivity activity) {
    mActivity = activity;
  }

  @Override
  public void onConnected() {
    /*
     * This function gets called when the device establishes the connection
     * with the broker.
     *
     * When the connection is established, we enable the "Send ping" button.
     */
    Log.i(TAG, "Device connected");
    mActivity.enablePingButton(true);
  }

  @Override
  public void onDisconnected(Throwable cause) {
    /*
     * This function gets called when the device loses the connection with the
     * broker.
     *
     * If the connection is lost, we disable the "Send ping" button.
     */
    Log.i(TAG, "Device disconnected: " + cause.getMessage());
    mActivity.enablePingButton(false);
  }

  @Override
  public void onFailure(Throwable cause) {
    /*
     * This function gets called when the device encounters an error during its
     * lifetime.
     */
    Log.w(TAG, "Device failure: " + cause.getMessage());
  }
}
