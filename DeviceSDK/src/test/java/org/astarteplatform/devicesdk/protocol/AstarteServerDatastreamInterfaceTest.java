package org.astarteplatform.devicesdk.protocol;

import static org.junit.Assert.*;

import org.joda.time.DateTime;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class AstarteServerDatastreamInterfaceTest {
  private static final String interfaceName = "org.test.Values";
  private static final String dtInterface =
      "{\n"
          + "    \"interface_name\": \""
          + interfaceName
          + "\",\n"
          + "    \"version_major\": 0,\n"
          + "    \"version_minor\": 1,\n"
          + "    \"type\": \"datastream\",\n"
          + "    \"ownership\": \"server\",\n"
          + "    \"mappings\": [\n"
          + "        {\n"
          + "            \"endpoint\": \"/value\",\n"
          + "            \"type\": \"double\",\n"
          + "            \"explicit_timestamp\": true,\n"
          + "        }\n"
          + "    ]\n"
          + "}\n";

  private AstarteServerDatastreamInterface datastreamInterface;

  @Before
  public void init() throws AstarteInvalidInterfaceException {
    JSONObject jsonInterface = new JSONObject(dtInterface);
    datastreamInterface =
        (AstarteServerDatastreamInterface) AstarteInterface.fromJSON(jsonInterface, null);
  }

  @Test
  public void buildSuccessful() {
    AstarteServerValue astarteServerValue =
        datastreamInterface.build("/value", 10.6, new DateTime());

    assertNotNull("Astarte server value != NULL", astarteServerValue);
    assertEquals("Compare interface path", astarteServerValue.getInterfacePath(), "/value");
    assertEquals("Compare interface path", astarteServerValue.getValue(), 10.6);
  }

  @Test
  public void buildNotSuccessful() {
    AstarteServerValue astarteServerValue =
        datastreamInterface.build("/value2", 10.6, new DateTime());

    assertNull("Astarte server value == NULL", astarteServerValue);
  }

  @Test
  public void publishSuccessful() {
    AstarteDatastreamEventListener listener =
        e -> {
          assertEquals("Compare interface path", e.getPath(), "/value");
          assertEquals("Compare interface name", e.getInterfaceName(), "org.test.Values");
          assertEquals("Compare value", e.getValue(), 10.6);
        };
    datastreamInterface.addListener(listener);
    AstarteServerValue astarteServerValue =
        datastreamInterface.build("/value", 10.6, new DateTime());

    datastreamInterface.publish(astarteServerValue);
    datastreamInterface.removeListener(listener);
  }
}
