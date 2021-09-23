package org.astarteplatform.devicesdk.protocol;

import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;
import org.astarteplatform.devicesdk.AstartePropertyStorage;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class AstarteDeviceAggregateDatastreamInterfaceTest {
  AstarteDeviceAggregateDatastreamInterface aInterface;
  AstarteDeviceAggregateDatastreamInterface aInterfaceWArray;

  @Before
  public void init() throws AstarteInvalidInterfaceException {
    JSONObject json =
        new JSONObject(
            "{\n"
                + "    \"interface_name\": \"com.astarte.Test\",\n"
                + "    \"version_major\": 0,\n"
                + "    \"version_minor\": 1,\n"
                + "    \"type\": \"datastream\",\n"
                + "    \"ownership\": \"device\",\n"
                + "    \"aggregation\": \"object\",\n"
                + "    \"mappings\": [\n"
                + "        {\n"
                + "            \"endpoint\": \"/test/one\",\n"
                + "            \"type\": \"integer\",\n"
                + "            \"database_retention_policy\": \"use_ttl\",\n"
                + "            \"database_retention_ttl\": 31536000, \n"
                + "            \"explicit_timestamp\": true\n"
                + "        },\n"
                + "        {\n"
                + "            \"endpoint\": \"/test/two\",\n"
                + "            \"type\": \"integer\",\n"
                + "            \"database_retention_policy\": \"use_ttl\",\n"
                + "            \"database_retention_ttl\": 31536000, \n"
                + "            \"explicit_timestamp\": true\n"
                + "        }\n"
                + "    ]\n"
                + "}");

    JSONObject jsonWithArrayValues =
        new JSONObject(
            "{\n"
                + "    \"interface_name\": \"com.astarte.ArrayTest\",\n"
                + "    \"version_major\": 0,\n"
                + "    \"version_minor\": 1,\n"
                + "    \"type\": \"datastream\",\n"
                + "    \"ownership\": \"device\",\n"
                + "    \"aggregation\": \"object\",\n"
                + "    \"mappings\": [\n"
                + "        {\n"
                + "            \"endpoint\": \"/test/int\",\n"
                + "            \"type\": \"integer\",\n"
                + "            \"database_retention_policy\": \"use_ttl\",\n"
                + "            \"database_retention_ttl\": 31536000, \n"
                + "            \"explicit_timestamp\": true\n"
                + "        },\n"
                + "        {\n"
                + "            \"endpoint\": \"/test/intArray\",\n"
                + "            \"type\": \"integerarray\",\n"
                + "            \"database_retention_policy\": \"use_ttl\",\n"
                + "            \"database_retention_ttl\": 31536000, \n"
                + "            \"explicit_timestamp\": true\n"
                + "        },\n"
                + "        {\n"
                + "            \"endpoint\": \"/test/doubleArray\",\n"
                + "            \"type\": \"doublearray\",\n"
                + "            \"database_retention_policy\": \"use_ttl\",\n"
                + "            \"database_retention_ttl\": 31536000, \n"
                + "            \"explicit_timestamp\": true\n"
                + "        }\n"
                + "    ]\n"
                + "}");

    AstartePropertyStorage storage = mock(AstartePropertyStorage.class);
    aInterface =
        (AstarteDeviceAggregateDatastreamInterface) AstarteInterface.fromJSON(json, storage);
    aInterfaceWArray =
        (AstarteDeviceAggregateDatastreamInterface)
            AstarteInterface.fromJSON(jsonWithArrayValues, storage);
  }

  @Test
  public void validateAggregateTest()
      throws AstarteInvalidValueException, AstarteInterfaceMappingNotFoundException {
    Map<String, Object> payload = new HashMap<>();
    payload.put("one", 1);
    payload.put("two", 2);
    aInterface.validatePayload("/test", payload, DateTime.now());
  }

  @Test
  public void validateAggregateLTooFewPayloadTest()
      throws AstarteInterfaceMappingNotFoundException {
    Map<String, Object> payload = new HashMap<>();
    payload.put("one", 1);
    try {
      aInterface.validatePayload("/test", payload, DateTime.now());
    } catch (AstarteInvalidValueException ex) {
      System.out.println(ex.getMessage());
    }
  }

  @Test
  public void validateAggregateTooMuchPayloadTest() throws AstarteInvalidValueException {
    Map<String, Object> payload = new HashMap<>();
    payload.put("one", 1);
    payload.put("two", 2);
    payload.put("three", 3);
    try {
      aInterface.validatePayload("/test", payload, DateTime.now());
    } catch (AstarteInterfaceMappingNotFoundException ex) {
      System.out.println(ex.getMessage());
    }
  }

  @Test
  public void validateAggregateTimestampRequiredTest()
      throws AstarteInvalidValueException, AstarteInterfaceMappingNotFoundException {
    Map<String, Object> payload = new HashMap<>();
    payload.put("uno", 1);
    payload.put("due", 2);
    try {
      aInterface.validatePayload("/test", payload, null);
    } catch (AstarteInvalidValueException ex) {
      System.out.println(ex.getMessage());
    }
  }

  @Test
  public void validateAggregateWithArraysTest()
      throws AstarteInvalidValueException, AstarteInterfaceMappingNotFoundException {
    Map<String, Object> payload = new HashMap<>();
    payload.put("int", 1);
    payload.put("intArray", new Integer[] {2, 3, 4});
    payload.put("doubleArray", new Double[] {2.0, 3.0, -4.5});
    aInterfaceWArray.validatePayload("/test", payload, DateTime.now());
  }

  @Test
  public void validateAggregateWithBadArraysTest()
      throws AstarteInvalidValueException, AstarteInterfaceMappingNotFoundException {
    Map<String, Object> payload = new HashMap<>();
    payload.put("int", 1);
    payload.put("intArray", new Integer[] {2, 3, 4});
    payload.put("doubleArray", new Integer[] {2, 3, -4});
    try {
      aInterfaceWArray.validatePayload("/test", payload, DateTime.now());
    } catch (AstarteInvalidValueException ex) {
      System.out.println(ex.getMessage());
    }
  }
}
