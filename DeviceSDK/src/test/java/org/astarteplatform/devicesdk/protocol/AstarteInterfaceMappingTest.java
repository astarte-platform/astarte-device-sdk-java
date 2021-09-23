package org.astarteplatform.devicesdk.protocol;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.joda.time.DateTime;
import org.json.JSONObject;
import org.junit.Test;

public class AstarteInterfaceMappingTest {

  @Test
  public void typeInt() {
    String interf =
        "{\n" + "     \"endpoint\": \"/integer\",\n" + "     \"type\": \"integer\",\n" + "   }\n";

    JSONObject jsonInterface = new JSONObject(interf);
    AstarteInterfaceMapping astarteInterfaceMapping =
        AstarteInterfaceMapping.fromJSON(jsonInterface);
    assertTrue("mapping type integer", astarteInterfaceMapping.isTypeCompatible(Integer.class));
  }

  @Test
  public void typeBadInt() {
    String interf =
        "{\n" + "     \"endpoint\": \"/integer\",\n" + "     \"type\": \"integer\",\n" + "   }\n";

    JSONObject jsonInterface = new JSONObject(interf);
    AstarteInterfaceMapping astarteInterfaceMapping =
        AstarteInterfaceMapping.fromJSON(jsonInterface);
    assertFalse(
        "mapping type integer with wrong type",
        astarteInterfaceMapping.isTypeCompatible(String.class));
  }

  @Test
  public void typeString() {
    String interf =
        "{\n" + "     \"endpoint\": \"/string\",\n" + "     \"type\": \"string\",\n" + "   }\n";

    JSONObject jsonInterface = new JSONObject(interf);
    AstarteInterfaceMapping astarteInterfaceMapping =
        AstarteInterfaceMapping.fromJSON(jsonInterface);
    assertTrue("mapping type string", astarteInterfaceMapping.isTypeCompatible(String.class));
  }

  @Test
  public void typeBadString() {
    String interf =
        "{\n" + "     \"endpoint\": \"/string\",\n" + "     \"type\": \"string\",\n" + "   }\n";

    JSONObject jsonInterface = new JSONObject(interf);
    AstarteInterfaceMapping astarteInterfaceMapping =
        AstarteInterfaceMapping.fromJSON(jsonInterface);
    assertFalse(
        "mapping type string with wrong type",
        astarteInterfaceMapping.isTypeCompatible(Integer.class));
  }

  @Test
  public void typeDouble() {
    String interf =
        "{\n" + "     \"endpoint\": \"/double\",\n" + "     \"type\": \"double\",\n" + "   }\n";

    JSONObject jsonInterface = new JSONObject(interf);
    AstarteInterfaceMapping astarteInterfaceMapping =
        AstarteInterfaceMapping.fromJSON(jsonInterface);
    assertTrue("mapping type double", astarteInterfaceMapping.isTypeCompatible(Double.class));
  }

  @Test
  public void typeBadDouble() {
    String interf =
        "{\n" + "     \"endpoint\": \"/double\",\n" + "     \"type\": \"double\",\n" + "   }\n";

    JSONObject jsonInterface = new JSONObject(interf);
    AstarteInterfaceMapping astarteInterfaceMapping =
        AstarteInterfaceMapping.fromJSON(jsonInterface);
    assertFalse(
        "mapping type double with wrong type",
        astarteInterfaceMapping.isTypeCompatible(String.class));
  }

  @Test
  public void typeLongInteger() {
    String interf =
        "{\n"
            + "     \"endpoint\": \"/longinteger\",\n"
            + "     \"type\": \"longinteger\",\n"
            + "   }\n";

    JSONObject jsonInterface = new JSONObject(interf);
    AstarteInterfaceMapping astarteInterfaceMapping =
        AstarteInterfaceMapping.fromJSON(jsonInterface);
    assertTrue("mapping type Long", astarteInterfaceMapping.isTypeCompatible(Long.class));
  }

  @Test
  public void typeBadLongInteger() {
    String interf =
        "{\n"
            + "     \"endpoint\": \"/longinteger\",\n"
            + "     \"type\": \"longinteger\",\n"
            + "   }\n";

    JSONObject jsonInterface = new JSONObject(interf);
    AstarteInterfaceMapping astarteInterfaceMapping =
        AstarteInterfaceMapping.fromJSON(jsonInterface);
    assertFalse(
        "mapping type Long with wrong type",
        astarteInterfaceMapping.isTypeCompatible(String.class));
  }

  @Test
  public void typeBoolean() {
    String interf =
        "{\n" + "     \"endpoint\": \"/boolean\",\n" + "     \"type\": \"boolean\",\n" + "   }\n";

    JSONObject jsonInterface = new JSONObject(interf);
    AstarteInterfaceMapping astarteInterfaceMapping =
        AstarteInterfaceMapping.fromJSON(jsonInterface);
    assertTrue("mapping type boolean", astarteInterfaceMapping.isTypeCompatible(Boolean.class));
  }

  @Test
  public void typeBadBoolean() {
    String interf =
        "{\n" + "     \"endpoint\": \"/boolean\",\n" + "     \"type\": \"boolean\",\n" + "   }\n";

    JSONObject jsonInterface = new JSONObject(interf);
    AstarteInterfaceMapping astarteInterfaceMapping =
        AstarteInterfaceMapping.fromJSON(jsonInterface);
    assertFalse(
        "mapping type boolean with wrong type",
        astarteInterfaceMapping.isTypeCompatible(String.class));
  }

  @Test
  public void typeBinaryBlob() {
    String interf =
        "{\n"
            + "     \"endpoint\": \"/binaryblob\",\n"
            + "     \"type\": \"binaryblob\",\n"
            + "   }\n";

    JSONObject jsonInterface = new JSONObject(interf);
    AstarteInterfaceMapping astarteInterfaceMapping =
        AstarteInterfaceMapping.fromJSON(jsonInterface);
    assertTrue("mapping type binaryblob", astarteInterfaceMapping.isTypeCompatible(Byte[].class));
  }

  @Test
  public void typeBadBinaryBlob() {
    String interf =
        "{\n"
            + "     \"endpoint\": \"/binaryblob\",\n"
            + "     \"type\": \"binaryblob\",\n"
            + "   }\n";

    JSONObject jsonInterface = new JSONObject(interf);
    AstarteInterfaceMapping astarteInterfaceMapping =
        AstarteInterfaceMapping.fromJSON(jsonInterface);
    assertFalse(
        "mapping type binaryblob with wrong type",
        astarteInterfaceMapping.isTypeCompatible(String.class));
  }

  @Test
  public void typeDatetime() {
    String interf =
        "{\n" + "     \"endpoint\": \"/datetime\",\n" + "     \"type\": \"datetime\",\n" + "   }\n";

    JSONObject jsonInterface = new JSONObject(interf);
    AstarteInterfaceMapping astarteInterfaceMapping =
        AstarteInterfaceMapping.fromJSON(jsonInterface);
    assertTrue("mapping type datetime", astarteInterfaceMapping.isTypeCompatible(DateTime.class));
  }

  @Test
  public void typeBadDatetime() {
    String interf =
        "{\n" + "     \"endpoint\": \"/datetime\",\n" + "     \"type\": \"datetime\",\n" + "   }\n";

    JSONObject jsonInterface = new JSONObject(interf);
    AstarteInterfaceMapping astarteInterfaceMapping =
        AstarteInterfaceMapping.fromJSON(jsonInterface);
    assertFalse(
        "mapping type datetime with wrong type",
        astarteInterfaceMapping.isTypeCompatible(String.class));
  }

  @Test
  public void ValidateDouble() throws AstarteInvalidValueException {
    String interf =
        "{\n" + "     \"endpoint\": \"/double\",\n" + "     \"type\": \"double\",\n" + "   }\n";

    JSONObject jsonInterface = new JSONObject(interf);
    AstarteInterfaceMapping astarteInterfaceMapping =
        AstarteInterfaceMapping.fromJSON(jsonInterface);
    astarteInterfaceMapping.validatePayload(3.0);
  }

  @Test
  public void ValidateNaNDouble() {
    String interf =
        "{\n" + "     \"endpoint\": \"/double\",\n" + "     \"type\": \"double\",\n" + "   }\n";

    JSONObject jsonInterface = new JSONObject(interf);
    AstarteInterfaceMapping astarteInterfaceMapping =
        AstarteInterfaceMapping.fromJSON(jsonInterface);
    try {
      astarteInterfaceMapping.validatePayload(Double.NaN);
    } catch (AstarteInvalidValueException e) {
      System.out.println(e.getMessage());
    }
  }

  @Test
  public void typeIntArray() {
    String interf =
        "{\n"
            + "     \"endpoint\": \"/integer\",\n"
            + "     \"type\": \"integerarray\",\n"
            + "   }\n";

    JSONObject jsonInterface = new JSONObject(interf);
    AstarteInterfaceMapping astarteInterfaceMapping =
        AstarteInterfaceMapping.fromJSON(jsonInterface);
    assertTrue("mapping type integer", astarteInterfaceMapping.isTypeCompatible(Integer[].class));
  }

  @Test
  public void typeBadIntArray() {
    String interf =
        "{\n"
            + "     \"endpoint\": \"/integer\",\n"
            + "     \"type\": \"integerarray\",\n"
            + "   }\n";

    JSONObject jsonInterface = new JSONObject(interf);
    AstarteInterfaceMapping astarteInterfaceMapping =
        AstarteInterfaceMapping.fromJSON(jsonInterface);
    assertFalse(
        "mapping type integer with wrong type",
        astarteInterfaceMapping.isTypeCompatible(Integer.class));
  }

  @Test
  public void typeStringArray() {
    String interf =
        "{\n"
            + "     \"endpoint\": \"/string\",\n"
            + "     \"type\": \"stringarray\",\n"
            + "   }\n";

    JSONObject jsonInterface = new JSONObject(interf);
    AstarteInterfaceMapping astarteInterfaceMapping =
        AstarteInterfaceMapping.fromJSON(jsonInterface);
    assertTrue("mapping type string", astarteInterfaceMapping.isTypeCompatible(String[].class));
  }

  @Test
  public void typeBadStringArray() {
    String interf =
        "{\n"
            + "     \"endpoint\": \"/string\",\n"
            + "     \"type\": \"stringarray\",\n"
            + "   }\n";

    JSONObject jsonInterface = new JSONObject(interf);
    AstarteInterfaceMapping astarteInterfaceMapping =
        AstarteInterfaceMapping.fromJSON(jsonInterface);
    assertFalse(
        "mapping type string with wrong type",
        astarteInterfaceMapping.isTypeCompatible(String.class));
  }

  @Test
  public void typeDoubleArray() {
    String interf =
        "{\n"
            + "     \"endpoint\": \"/double\",\n"
            + "     \"type\": \"doublearray\",\n"
            + "   }\n";

    JSONObject jsonInterface = new JSONObject(interf);
    AstarteInterfaceMapping astarteInterfaceMapping =
        AstarteInterfaceMapping.fromJSON(jsonInterface);
    assertTrue("mapping type double", astarteInterfaceMapping.isTypeCompatible(Double[].class));
  }

  @Test
  public void typeBadDoubleArray() {
    String interf =
        "{\n"
            + "     \"endpoint\": \"/double\",\n"
            + "     \"type\": \"doublearray\",\n"
            + "   }\n";

    JSONObject jsonInterface = new JSONObject(interf);
    AstarteInterfaceMapping astarteInterfaceMapping =
        AstarteInterfaceMapping.fromJSON(jsonInterface);
    assertFalse(
        "mapping type double with wrong type",
        astarteInterfaceMapping.isTypeCompatible(Double.class));
  }

  @Test
  public void typeLongIntegerArray() {
    String interf =
        "{\n"
            + "     \"endpoint\": \"/longinteger\",\n"
            + "     \"type\": \"longintegerarray\",\n"
            + "   }\n";

    JSONObject jsonInterface = new JSONObject(interf);
    AstarteInterfaceMapping astarteInterfaceMapping =
        AstarteInterfaceMapping.fromJSON(jsonInterface);
    assertTrue("mapping type Long", astarteInterfaceMapping.isTypeCompatible(Long[].class));
  }

  @Test
  public void typeBadLongIntegerArray() {
    String interf =
        "{\n"
            + "     \"endpoint\": \"/longinteger\",\n"
            + "     \"type\": \"longintegerarray\",\n"
            + "   }\n";

    JSONObject jsonInterface = new JSONObject(interf);
    AstarteInterfaceMapping astarteInterfaceMapping =
        AstarteInterfaceMapping.fromJSON(jsonInterface);
    assertFalse(
        "mapping type Long with wrong type", astarteInterfaceMapping.isTypeCompatible(Long.class));
  }

  @Test
  public void typeBooleanArray() {
    String interf =
        "{\n"
            + "     \"endpoint\": \"/boolean\",\n"
            + "     \"type\": \"booleanarray\",\n"
            + "   }\n";

    JSONObject jsonInterface = new JSONObject(interf);
    AstarteInterfaceMapping astarteInterfaceMapping =
        AstarteInterfaceMapping.fromJSON(jsonInterface);
    assertTrue("mapping type boolean", astarteInterfaceMapping.isTypeCompatible(Boolean[].class));
  }

  @Test
  public void typeBadBooleanArray() {
    String interf =
        "{\n"
            + "     \"endpoint\": \"/boolean\",\n"
            + "     \"type\": \"booleanarray\",\n"
            + "   }\n";

    JSONObject jsonInterface = new JSONObject(interf);
    AstarteInterfaceMapping astarteInterfaceMapping =
        AstarteInterfaceMapping.fromJSON(jsonInterface);
    assertFalse(
        "mapping type boolean with wrong type",
        astarteInterfaceMapping.isTypeCompatible(Boolean.class));
  }

  @Test
  public void typeBinaryBlobArray() {
    String interf =
        "{\n"
            + "     \"endpoint\": \"/binaryblob\",\n"
            + "     \"type\": \"binaryblobarray\",\n"
            + "   }\n";

    JSONObject jsonInterface = new JSONObject(interf);
    AstarteInterfaceMapping astarteInterfaceMapping =
        AstarteInterfaceMapping.fromJSON(jsonInterface);
    assertTrue("mapping type binaryblob", astarteInterfaceMapping.isTypeCompatible(Byte[][].class));
  }

  @Test
  public void typeBadBinaryBlobArray() {
    String interf =
        "{\n"
            + "     \"endpoint\": \"/binaryblob\",\n"
            + "     \"type\": \"binaryblobarray\",\n"
            + "   }\n";

    JSONObject jsonInterface = new JSONObject(interf);
    AstarteInterfaceMapping astarteInterfaceMapping =
        AstarteInterfaceMapping.fromJSON(jsonInterface);
    assertFalse(
        "mapping type binaryblob with wrong type",
        astarteInterfaceMapping.isTypeCompatible(Byte[].class));
  }

  @Test
  public void typeDatetimeArray() {
    String interf =
        "{\n"
            + "     \"endpoint\": \"/datetime\",\n"
            + "     \"type\": \"datetimearray\",\n"
            + "   }\n";

    JSONObject jsonInterface = new JSONObject(interf);
    AstarteInterfaceMapping astarteInterfaceMapping =
        AstarteInterfaceMapping.fromJSON(jsonInterface);
    assertTrue("mapping type datetime", astarteInterfaceMapping.isTypeCompatible(DateTime[].class));
  }

  @Test
  public void typeBadDatetimeArray() {
    String interf =
        "{\n"
            + "     \"endpoint\": \"/datetime\",\n"
            + "     \"type\": \"datetimearray\",\n"
            + "   }\n";

    JSONObject jsonInterface = new JSONObject(interf);
    AstarteInterfaceMapping astarteInterfaceMapping =
        AstarteInterfaceMapping.fromJSON(jsonInterface);
    assertFalse(
        "mapping type datetime with wrong type",
        astarteInterfaceMapping.isTypeCompatible(DateTime.class));
  }

  @Test
  public void ValidateDoubleArray() throws AstarteInvalidValueException {
    String interf =
        "{\n"
            + "     \"endpoint\": \"/double\",\n"
            + "     \"type\": \"doublearray\",\n"
            + "   }\n";

    JSONObject jsonInterface = new JSONObject(interf);
    AstarteInterfaceMapping astarteInterfaceMapping =
        AstarteInterfaceMapping.fromJSON(jsonInterface);
    Object payload = new Double[] {3.0, 1.0};
    astarteInterfaceMapping.validatePayload(payload);
  }

  @Test
  public void ValidateNaNDoubleArray() {
    String interf =
        "{\n"
            + "     \"endpoint\": \"/double\",\n"
            + "     \"type\": \"doublearray\",\n"
            + "   }\n";

    JSONObject jsonInterface = new JSONObject(interf);
    AstarteInterfaceMapping astarteInterfaceMapping =
        AstarteInterfaceMapping.fromJSON(jsonInterface);
    try {
      Object payload = new Double[] {1.0, Double.NaN};
      astarteInterfaceMapping.validatePayload(payload);
    } catch (AstarteInvalidValueException e) {
      System.out.println(e.getMessage());
    }
  }
}
