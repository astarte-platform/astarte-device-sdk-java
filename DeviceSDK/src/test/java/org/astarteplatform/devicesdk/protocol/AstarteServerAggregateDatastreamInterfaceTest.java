package org.astarteplatform.devicesdk.protocol;

import static org.junit.Assert.*;

import java.util.Map;
import org.bson.BasicBSONObject;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class AstarteServerAggregateDatastreamInterfaceTest {
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
          + "    \"aggregation\": \"object\",\n"
          + "    \"mappings\": [\n"
          + "        {\n"
          + "            \"endpoint\": \"/test/value\",\n"
          + "            \"type\": \"double\",\n"
          + "        },\n"
          + "        {\n"
          + "            \"endpoint\": \"/test/name\",\n"
          + "            \"type\": \"string\",\n"
          + "        }\n"
          + "    ]\n"
          + "}\n";

  private AstarteServerAggregateDatastreamInterface datastreamInterface;

  @Before
  public void init() throws AstarteInvalidInterfaceException {
    JSONObject jsonInterface = new JSONObject(dtInterface);
    datastreamInterface =
        (AstarteServerAggregateDatastreamInterface) AstarteInterface.fromJSON(jsonInterface, null);
  }

  @Test
  public void buildSuccessful() {
    BasicBSONObject expectedValues = new BasicBSONObject();
    expectedValues.put("value", 10.6);
    expectedValues.put("name", "build");
    AstarteServerValue astarteServerValue =
        datastreamInterface.build("/test", expectedValues, new DateTime());

    assertNotNull("Astarte server value != NULL", astarteServerValue);
    assertEquals("Compare interface path", astarteServerValue.getInterfacePath(), "/test");

    Map<String, Object> rValues = astarteServerValue.getMapValue();
    assertTrue("map value not empty", rValues.size() > 0);
    assertEquals("Compare value endpoint", rValues.get("value"), 10.6);
    assertEquals("Compare name endpoint", rValues.get("name"), "build");
  }

  @Test
  public void buildNotSuccessful() {
    AstarteServerValue astarteServerValue =
        datastreamInterface.build("/value2", null, new DateTime());

    assertNull("Astarte server value == NULL", astarteServerValue);
  }

  @Test
  public void publishSuccessful() {
    BasicBSONObject expectedValues = new BasicBSONObject();
    expectedValues.put("value", 10.6);
    expectedValues.put("name", "build");

    AstarteAggregateDatastreamEventListener listener =
        e -> {
          Map<String, Object> rValues = e.getValues();
          assertEquals("Compare interface name", e.getInterfaceName(), "org.test.Values");
          assertTrue("map value not empty", rValues.size() > 0);
          assertEquals("Compare value endpoint", rValues.get("value"), 10.6);
          assertEquals("Compare name endpoint", rValues.get("name"), "build");
        };
    datastreamInterface.addListener(listener);
    AstarteServerValue astarteServerValue =
        datastreamInterface.build("/test", expectedValues, new DateTime());

    datastreamInterface.publish(astarteServerValue);
    datastreamInterface.removeListener(listener);
  }
}
