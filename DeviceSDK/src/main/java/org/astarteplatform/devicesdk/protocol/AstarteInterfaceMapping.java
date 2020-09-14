package org.astarteplatform.devicesdk.protocol;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

public class AstarteInterfaceMapping {
  private String path;
  private Type type;

  private static final Collection<Collection<Type>> mTypeCompatibilityList = new HashSet<>();

  static {
    Collection<Type> numberCompatibilityList = new HashSet<>();
    numberCompatibilityList.add(int.class);
    numberCompatibilityList.add(long.class);
    numberCompatibilityList.add(float.class);
    numberCompatibilityList.add(double.class);
    numberCompatibilityList.add(Integer.class);
    numberCompatibilityList.add(Long.class);
    numberCompatibilityList.add(BigInteger.class);
    numberCompatibilityList.add(Float.class);
    numberCompatibilityList.add(Double.class);

    Collection<Type> booleanCompatibilityList = new HashSet<>();
    booleanCompatibilityList.add(boolean.class);
    booleanCompatibilityList.add(Boolean.class);

    Collection<Type> stringCompatibilityList = new HashSet<>();
    booleanCompatibilityList.add(String.class);

    Collection<Type> dateTimeCompatibilityList = new HashSet<>();
    dateTimeCompatibilityList.add(DateTime.class);

    Collection<Type> byteArrayCompatibilityList = new HashSet<>();
    byteArrayCompatibilityList.add(byte[].class);
    byteArrayCompatibilityList.add(Byte[].class);

    mTypeCompatibilityList.add(numberCompatibilityList);
    mTypeCompatibilityList.add(booleanCompatibilityList);
    mTypeCompatibilityList.add(stringCompatibilityList);
    mTypeCompatibilityList.add(dateTimeCompatibilityList);
    mTypeCompatibilityList.add(byteArrayCompatibilityList);
  }
  ;

  static AstarteInterfaceMapping fromJSON(JSONObject astarteMappingObject) throws JSONException {
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

  boolean isTypeCompatible(Type otherType) {
    // Here is our long and boring compatibility map.
    for (Collection<Type> compatibilityList : mTypeCompatibilityList) {
      if (compatibilityList.contains(otherType) && compatibilityList.contains(getType())) {
        return true;
      }
    }
    return false;
  }

  boolean isTypeCompatible(String astarteType) {
    // Here is our long and boring compatibility map.
    for (Collection<Type> compatibilityList : mTypeCompatibilityList) {
      if (compatibilityList.contains(stringToJavaType(astarteType))
          && compatibilityList.contains(getType())) {
        return true;
      }
    }
    return false;
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
