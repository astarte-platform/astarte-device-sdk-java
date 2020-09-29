package org.astarteplatform.devicesdk.generic.examples;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import org.astarteplatform.devicesdk.AstarteInterfaceProvider;
import org.json.JSONObject;
import org.json.JSONTokener;

public class ExampleInterfaceProvider implements AstarteInterfaceProvider {
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
      "org.astarte-platform.genericsensors.AvailableSensors",
      "org.astarte-platform.genericsensors.SamplingRate",
      "org.astarte-platform.genericsensors.Values"
    };
    Collection<JSONObject> interfaces = new HashSet<>();

    for (String interfaceName : interfaceNames) {
      InputStream is =
          ClassLoader.getSystemClassLoader()
              .getResourceAsStream("standard-interfaces/" + interfaceName + ".json");
      JSONTokener tokener = new JSONTokener(is);

      try {
        interfaces.add(new JSONObject(tokener));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return interfaces;
  }
}
