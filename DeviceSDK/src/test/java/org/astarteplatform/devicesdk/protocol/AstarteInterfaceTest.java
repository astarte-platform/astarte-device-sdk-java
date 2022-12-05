package org.astarteplatform.devicesdk.protocol;

import static org.junit.Assert.assertEquals;

import org.json.JSONObject;
import org.junit.Test;

public class AstarteInterfaceTest {
  @Test
  public void testSuccessfulFromJson() throws AstarteInvalidInterfaceException {
    String iface =
        "{\n"
            + "    \"interface_name\": \"org.astarte-platform.genericsensors.AvailableSensors\",\n"
            + "    \"version_major\": 0,\n"
            + "    \"version_minor\": 1,\n"
            + "    \"type\": \"properties\",\n"
            + "    \"ownership\": \"device\",\n"
            + "    \"description\": \"Describes available generic sensors.\",\n"
            + "    \"doc\": \"This interface allows to describe available sensors and their attributes such as name and sampled data measurement unit. Sensors are identified by their sensor_id. See also org.astarte-platform.genericsensors.AvailableSensors.\",\n"
            + "    \"mappings\": [\n"
            + "        {\n"
            + "            \"endpoint\": \"/%{sensor_id}/name\",\n"
            + "            \"type\": \"string\",\n"
            + "            \"description\": \"Sensor name.\",\n"
            + "            \"doc\": \"An arbitrary sensor name.\"\n"
            + "        },\n"
            + "        {\n"
            + "            \"endpoint\": \"/%{sensor_id}/unit\",\n"
            + "            \"type\": \"string\",\n"
            + "            \"description\": \"Sample data measurement unit.\",\n"
            + "            \"doc\": \"SI unit such as m, kg, K, etc...\"\n"
            + "        }\n"
            + "    ]\n"
            + "}\n";
    JSONObject obj = new JSONObject(iface);
    final AstarteInterface astarteInterface = AstarteInterface.fromJSON(obj, null);
    assertEquals(
        astarteInterface.getInterfaceName(),
        "org.astarte-platform.genericsensors.AvailableSensors");
    assertEquals(astarteInterface.getMajorVersion(), 0);
    assertEquals(astarteInterface.getMinorVersion(), 1);
  }

  @Test(expected = AstarteInvalidInterfaceException.class)
  public void testThroeExceptionFromJson() throws AstarteInvalidInterfaceException {
    String iface =
        "{\n"
            + "    \"interface_name\": \"org.astarte-platform.genericsensors.AvailableSensors\",\n"
            + "    \"version_major\": 0,\n"
            + "    \"version_minor\": 0,\n"
            + "    \"type\": \"properties\",\n"
            + "    \"ownership\": \"device\",\n"
            + "    \"description\": \"Describes available generic sensors.\",\n"
            + "    \"doc\": \"This interface allows to describe available sensors and their attributes such as name and sampled data measurement unit. Sensors are identified by their sensor_id. See also org.astarte-platform.genericsensors.AvailableSensors.\",\n"
            + "    \"mappings\": []\n"
            + "}\n";
    JSONObject obj = new JSONObject(iface);
    AstarteInterface.fromJSON(obj, null);
  }
}
