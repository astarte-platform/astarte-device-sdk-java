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
      "org.astarte-platform.genericsensors.SamplingRate"
    };
    Collection<JSONObject> interfaces = new HashSet<>();

    for (String interfaceName : interfaceNames) {
      try {
        interfaces.add(loadInterface(interfaceName));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return interfaces;
  }

  @Override
  public JSONObject loadInterface(String interfaceName) {
    /*
     * loadInterface must return the interface witch the given interface name.
     *
     * Here we load the interface from JSON files that is in the resource folder.
     *
     */
    InputStream is =
        ClassLoader.getSystemClassLoader()
            .getResourceAsStream("standard-interfaces/" + interfaceName + ".json");
    if (is == null) {
      return null;
    }
    JSONTokener tokener = new JSONTokener(is);
    return new JSONObject(tokener);
  }
}
