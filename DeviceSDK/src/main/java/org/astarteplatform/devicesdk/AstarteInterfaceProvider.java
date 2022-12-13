package org.astarteplatform.devicesdk;

import java.util.Collection;
import org.json.JSONObject;

public interface AstarteInterfaceProvider {
  Collection<JSONObject> loadAllInterfaces();

  JSONObject loadInterface(String interfaceName);
}
