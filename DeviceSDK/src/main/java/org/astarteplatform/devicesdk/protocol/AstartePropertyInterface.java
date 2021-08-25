package org.astarteplatform.devicesdk.protocol;

import java.util.Map;
import org.astarteplatform.devicesdk.AstartePropertyStorage;
import org.joda.time.DateTime;

public abstract class AstartePropertyInterface extends AstarteInterface {
  final AstartePropertyStorage mPropertyStorage;

  AstartePropertyInterface(AstartePropertyStorage propertyStorage) {
    mPropertyStorage = propertyStorage;
  }

  public Map<String, Object> getAllValues() {
    if (mPropertyStorage == null) {
      return null;
    }

    return mPropertyStorage.getStoredValuesForInterface(this);
  }

  private <T> T getPropertyValue(String path, Class<T> clazz) {
    if (mPropertyStorage == null) {
      return null;
    }

    Map<String, Object> storedPaths = mPropertyStorage.getStoredValuesForInterface(this);
    if (!storedPaths.containsKey(path)) {
      return null;
    }

    Object value = storedPaths.get(path);
    if (value == null) {
      return null;
    }

    return clazz.cast(value);
  }

  /** Returns the value mapped by {@code path} if it exists or null if no such mapping exists. */
  public String getPropertyValueString(String path) {
    return getPropertyValue(path, String.class);
  }

  /** Returns the value mapped by {@code path} if it exists or null if no such mapping exists. */
  public Boolean getPropertyValueBoolean(String path) {
    return getPropertyValue(path, Boolean.class);
  }

  /** Returns the value mapped by {@code path} if it exists or null if no such mapping exists. */
  public Integer getPropertyValueInt(String path) {
    return getPropertyValue(path, Integer.class);
  }

  /** Returns the value mapped by {@code path} if it exists or null if no such mapping exists. */
  public Long getPropertyValueLong(String path) {
    return getPropertyValue(path, Long.class);
  }

  /** Returns the value mapped by {@code path} if it exists or null if no such mapping exists. */
  public Double getPropertyValueDouble(String path) {
    return getPropertyValue(path, Double.class);
  }

  /** Returns the value mapped by {@code path} if it exists or null if no such mapping exists. */
  public byte[] getPropertyValueByteArray(String path) {
    return getPropertyValue(path, byte[].class);
  }

  /** Returns the value mapped by {@code path} if it exists or null if no such mapping exists. */
  public DateTime getPropertyValueDateTime(String path) {
    return getPropertyValue(path, DateTime.class);
  }
}
