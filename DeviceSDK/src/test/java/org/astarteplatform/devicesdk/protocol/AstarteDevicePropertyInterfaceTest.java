package org.astarteplatform.devicesdk.protocol;

import static org.mockito.Mockito.mock;

import org.astarteplatform.devicesdk.AstartePropertyStorage;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class AstarteDevicePropertyInterfaceTest {
  AstarteDevicePropertyInterface aInterface;

  @Before
  public void init() throws AstarteInvalidInterfaceException {
    JSONObject json =
        new JSONObject(
            "{\n"
                + "    \"interface_name\": \"com.astarte.Test\",\n"
                + "    \"version_major\": 0,\n"
                + "    \"version_minor\": 1,\n"
                + "    \"type\": \"properties\",\n"
                + "    \"ownership\": \"device\",\n"
                + "    \"mappings\": [\n"
                + "        {\n"
                + "            \"endpoint\": \"/test/uno\",\n"
                + "            \"type\": \"integer\",\n"
                + "            \"database_retention_policy\": \"use_ttl\",\n"
                + "            \"database_retention_ttl\": 31536000 \n"
                + "        },\n"
                + "        {\n"
                + "            \"endpoint\": \"/test/due\",\n"
                + "            \"type\": \"integer\",\n"
                + "            \"database_retention_policy\": \"use_ttl\",\n"
                + "            \"database_retention_ttl\": 31536000 \n"
                + "        }\n"
                + "    ]\n"
                + "}");
    AstartePropertyStorage storage = mock(AstartePropertyStorage.class);
    aInterface = (AstarteDevicePropertyInterface) AstarteInterface.fromJSON(json, storage);
  }

  @Test
  public void validatePropertyTest()
      throws AstarteInvalidValueException, AstarteInterfaceMappingNotFoundException {
    aInterface.validatePayload("/test/uno", 1, null);
  }

  @Test
  public void validatePropertyInvalidValueTest() throws AstarteInterfaceMappingNotFoundException {
    try {
      aInterface.validatePayload("/test/uno", 1.0, null);
    } catch (AstarteInvalidValueException ex) {
      System.out.println(ex.getMessage());
    }
  }

  @Test
  public void validateAbsentPropertyTest() throws AstarteInvalidValueException {
    try {
      aInterface.validatePayload("/test/tre", 3, null);
    } catch (AstarteInterfaceMappingNotFoundException e) {
      System.out.println(e.getMessage());
    }
  }

  @Test
  public void validatePropertyTimestampTest() throws AstarteInterfaceMappingNotFoundException {
    try {
      aInterface.validatePayload("/test/uno", 1, DateTime.now());
    } catch (AstarteInvalidValueException ex) {
      System.out.println(ex.getMessage());
    }
  }
}
