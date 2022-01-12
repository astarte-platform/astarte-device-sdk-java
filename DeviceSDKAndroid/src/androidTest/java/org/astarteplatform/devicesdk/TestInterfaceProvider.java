package org.astarteplatform.devicesdk;

import java.util.Collection;
import java.util.HashSet;
import org.json.JSONObject;

public class TestInterfaceProvider implements AstarteInterfaceProvider {
  private final String m_astarteValuesInterface =
      "{\n"
          + "    \"interface_name\": \"org.astarte-platform.sdktest.DeviceDatastream\",\n"
          + "    \"version_major\": 0,\n"
          + "    \"version_minor\": 1,\n"
          + "    \"type\": \"datastream\",\n"
          + "    \"ownership\": \"device\",\n"
          + "    \"mappings\": [\n"
          + "        {\n"
          + "            \"endpoint\": \"/discardValue\",\n"
          + "            \"type\": \"double\",\n"
          + "            \"database_retention_policy\": \"use_ttl\",\n"
          + "            \"database_retention_ttl\": 300\n"
          + "        },\n"
          + "        {\n"
          + "            \"endpoint\": \"/storedValue\",\n"
          + "            \"type\": \"double\",\n"
          + "            \"retention\": \"stored\",\n"
          + "            \"database_retention_policy\": \"use_ttl\",\n"
          + "            \"database_retention_ttl\": 300\n"
          + "        },\n"
          + "        {\n"
          + "            \"endpoint\": \"/volatileValue\",\n"
          + "            \"type\": \"double\",\n"
          + "            \"retention\": \"volatile\",\n"
          + "            \"database_retention_policy\": \"use_ttl\",\n"
          + "            \"database_retention_ttl\": 300\n"
          + "        }\n"
          + "    ]\n"
          + "}";

  @Override
  public Collection<JSONObject> loadAllInterfaces() {
    Collection<JSONObject> interfaces = new HashSet<>();
    try {
      interfaces.add(new JSONObject(m_astarteValuesInterface));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return interfaces;
  }

  @Override
  public JSONObject loadInterface(String interfaceName) {
    return null;
  }
}
