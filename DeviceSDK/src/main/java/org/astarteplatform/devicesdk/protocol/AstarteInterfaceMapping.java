package org.astarteplatform.devicesdk.protocol;

import java.lang.reflect.Type;
import java.util.Objects;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

public class AstarteInterfaceMapping {
  private String path;
  private Type type;

  protected static AstarteInterfaceMapping fromJSON(JSONObject astarteMappingObject)
      throws JSONException {
    AstarteInterfaceMapping astarteInterfaceMapping = new AstarteInterfaceMapping();
    astarteInterfaceMapping.parseMappingFromJSON(astarteMappingObject);
    return astarteInterfaceMapping;
  }

  protected void parseMappingFromJSON(JSONObject astarteMappingObject) throws JSONException {
    path = astarteMappingObject.getString("endpoint");
    type = stringToJavaType(astarteMappingObject.getString("type"));
  }

  public String getPath() {
    return path;
  }

  public Type getType() {
    return type;
  }

  protected boolean isTypeCompatible(Type otherType) {
    return (getType() == otherType);
  }

  public void validatePayload(Object payload) throws AstarteInvalidValueException {
    if (!isTypeCompatible(payload.getClass())) {
      throw new AstarteInvalidValueException(
          String.format(
              "Value incompatible with parameter type for %s: %s expected, %s found",
              getPath(), getType(), payload.getClass()));
    }
    if (payload instanceof Double && !Double.isFinite((Double) payload)) {
      throw new AstarteInvalidValueException(
          String.format("Value per %s cannot be NaN", getPath()));
    }
  }

  public void validatePayload(Object payload, DateTime timestamp)
      throws AstarteInvalidValueException {
    validatePayload(payload);
  }

  private static Type stringToJavaType(String typeString) {
    if (Objects.equals(typeString, "string")) {
      return String.class;
    } else if (Objects.equals(typeString, "integer")) {
      return Integer.class;
    } else if (Objects.equals(typeString, "double")) {
      return Double.class;
    } else if (Objects.equals(typeString, "longinteger")) {
      return Long.class;
    } else if (Objects.equals(typeString, "boolean")) {
      return Boolean.class;
    } else if (Objects.equals(typeString, "binaryblob")) {
      return Byte[].class;
    } else if (Objects.equals(typeString, "datetime")) {
      return DateTime.class;
    }

    return Object.class;
  }
}
