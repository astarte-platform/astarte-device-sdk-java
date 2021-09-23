package org.astarteplatform.devicesdk.protocol;

import java.lang.reflect.Type;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

public class AstarteInterfaceMapping {
  private String path;
  private Type type;
  private Type primitiveArrayType;

  protected static AstarteInterfaceMapping fromJSON(JSONObject astarteMappingObject)
      throws JSONException {
    AstarteInterfaceMapping astarteInterfaceMapping = new AstarteInterfaceMapping();
    astarteInterfaceMapping.parseMappingFromJSON(astarteMappingObject);
    return astarteInterfaceMapping;
  }

  protected void parseMappingFromJSON(JSONObject astarteMappingObject) throws JSONException {
    path = astarteMappingObject.getString("endpoint");
    type = stringToJavaType(astarteMappingObject.getString("type"));
    primitiveArrayType = stringToPrimitiveArrayJavaType(astarteMappingObject.getString("type"));
  }

  public String getPath() {
    return path;
  }

  public Type getType() {
    return type;
  }

  protected boolean isTypeCompatible(Type otherType) {
    return otherType == this.type || otherType == this.primitiveArrayType;
  }

  public void validatePayload(Object payload) throws AstarteInvalidValueException {
    if (!isTypeCompatible(payload.getClass())) {
      throw new AstarteInvalidValueException(
          String.format(
              "Value incompatible with parameter type for %s: %s expected, %s found",
              getPath(), getType(), payload.getClass()));
    }
    if (payload instanceof Double && !isFinite((Double) payload)) {
      throw new AstarteInvalidValueException(
          String.format("Value per %s cannot be NaN", getPath()));
    }
    if (payload instanceof Double[]) {
      final Double[] arrayPayload = (Double[]) payload;
      for (Double value : arrayPayload) {
        if (!isFinite(value)) {
          throw new AstarteInvalidValueException(
              String.format("Value per %s cannot be NaN", getPath()));
        }
      }
    }
  }

  /**
   * Replacement for Double.isFinite function for better compatibility with Android SDK < 24
   *
   * @param value The number to check
   * @return true if value is a finite number
   */
  private static boolean isFinite(Double value) {
    return !(value.isInfinite() || value.isNaN());
  }

  public void validatePayload(Object payload, DateTime timestamp)
      throws AstarteInvalidValueException {
    validatePayload(payload);
  }

  private static Type stringToJavaType(String typeString) {
    switch (typeString) {
      case "string":
        return String.class;
      case "integer":
        return Integer.class;
      case "double":
        return Double.class;
      case "longinteger":
        return Long.class;
      case "boolean":
        return Boolean.class;
      case "binaryblob":
        return Byte[].class;
      case "datetime":
        return DateTime.class;
      case "stringarray":
        return String[].class;
      case "integerarray":
        return Integer[].class;
      case "doublearray":
        return Double[].class;
      case "longintegerarray":
        return Long[].class;
      case "booleanarray":
        return Boolean[].class;
      case "binaryblobarray":
        return Byte[][].class;
      case "datetimearray":
        return DateTime[].class;
      default:
        return Object.class;
    }
  }

  private static Type stringToPrimitiveArrayJavaType(String typeString) {
    switch (typeString) {
      case "binaryblob":
        return byte[].class;
      case "integerarray":
        return int[].class;
      case "doublearray":
        return double[].class;
      case "longintegerarray":
        return long[].class;
      case "booleanarray":
        return boolean[].class;
      case "binaryblobarray":
        return byte[][].class;
      default:
        return null;
    }
  }
}
