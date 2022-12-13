package org.astarteplatform.devicesdk.android.examples;

import android.content.Context;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import org.astarteplatform.devicesdk.AstarteInterfaceProvider;
import org.json.JSONException;
import org.json.JSONObject;

public class ExampleInterfaceProvider implements AstarteInterfaceProvider {
  private final Context mContext;

  public ExampleInterfaceProvider(Context context) {
    mContext = context;
  }

  @Override
  public Collection<JSONObject> loadAllInterfaces() {
    /*
     * loadAllInterfaces must return all the interfaces supported by this device.
     *
     * Here we load the interfaces from JSON files that are in the resources folder.
     *
     * The interfaces used in this example are the Astarte standard-interfaces present in
     * the main Astarte repository.
     */
    String[] interfaceNames = {
      "org.astarte-platform.genericevents.DeviceEvents",
      "org.astarte-platform.genericcommands.ServerCommands",
    };
    Collection<JSONObject> interfaces = new HashSet<>();
    for (String interfaceName : interfaceNames) {
      try {
        JSONObject obj = new JSONObject(loadJSONFromAsset(interfaceName));
        interfaces.add(obj);
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
    return interfaces;
  }

  @Override
  public JSONObject loadInterface(String interfaceName) {
    return null;
  }

  private String loadJSONFromAsset(String interfaceName) {
    String json = null;
    try {
      InputStream is = mContext.getAssets().open("standard-interfaces/" + interfaceName + ".json");
      int size = is.available();
      byte[] buffer = new byte[size];
      is.read(buffer);
      is.close();
      json = new String(buffer, "UTF-8");
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
    return json;
  }
}
