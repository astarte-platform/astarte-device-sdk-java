package org.astarteplatform.devicesdk.protocol;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import org.joda.time.DateTime;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class AstarteServerPropertyInterfaceTest {
  private static final String interfaceName = "org.test.Values";
  private static final String prInterface =
      "{\n"
          + "    \"interface_name\": \""
          + interfaceName
          + "\",\n"
          + "    \"version_major\": 0,\n"
          + "    \"version_minor\": 1,\n"
          + "    \"type\": \"properties\",\n"
          + "    \"ownership\": \"server\",\n"
          + "    \"mappings\": [\n"
          + "        {\n"
          + "            \"endpoint\": \"/enable\",\n"
          + "            \"type\": \"boolean\",\n"
          + "        }\n"
          + "    ]\n"
          + "}\n";

  private AstarteServerPropertyInterface propertyInterface;

  @Before
  public void init() throws AstarteInvalidInterfaceException {
    JSONObject jsonInterface = new JSONObject(prInterface);
    propertyInterface =
        (AstarteServerPropertyInterface) AstarteInterface.fromJSON(jsonInterface, null);
  }

  @Test
  public void buildSuccessful() {
    AstarteServerValue astarteServerValue =
        propertyInterface.build("/enable", true, new DateTime());

    assertNotNull("Astarte server value != NULL", astarteServerValue);
    assertEquals("Compare interface path", astarteServerValue.getInterfacePath(), "/enable");
    assertEquals("Compare interface path", astarteServerValue.getValue(), true);
  }

  @Test
  public void buildNotSuccessful() {
    AstarteServerValue astarteServerValue =
        propertyInterface.build("/enable1", true, new DateTime());

    assertNull("Astarte server value == NULL", astarteServerValue);
  }

  @Test
  public void publishPropertyReceivedSuccessful() {
    AstartePropertyEventListener listener =
        new AstartePropertyEventListener() {
          @Override
          public void propertyReceived(AstartePropertyEvent e) {
            assertEquals("Compare interface path", e.getPath(), "/enable");
            assertEquals("Compare interface name", e.getInterfaceName(), "org.test.Values");
            assertEquals("Compare value", e.getValue(), true);
          }

          @Override
          public void propertyUnset(AstartePropertyEvent e) {
            fail("function propertyUnset called");
          }
        };

    propertyInterface.addListener(listener);
    AstarteServerValue astarteServerValue =
        propertyInterface.build("/enable", true, new DateTime());

    propertyInterface.publish(astarteServerValue);
    propertyInterface.removeListener(listener);
  }

  @Test
  public void publishPropertyUnsetSuccessful() {
    AstartePropertyEventListener listener =
        new AstartePropertyEventListener() {
          @Override
          public void propertyReceived(AstartePropertyEvent e) {
            fail("function propertyReceived called");
          }

          @Override
          public void propertyUnset(AstartePropertyEvent e) {
            assertEquals("Compare interface path", e.getPath(), "/enable");
            assertEquals("Compare interface name", e.getInterfaceName(), "org.test.Values");
            assertNull("Compare value", e.getValue());
          }
        };

    propertyInterface.addListener(listener);
    AstarteServerValue astarteServerValue =
        propertyInterface.build("/enable", null, new DateTime());

    propertyInterface.publish(astarteServerValue);
    propertyInterface.removeListener(listener);
  }
}
