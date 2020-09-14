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

  Object getPropertyValue(String path) {
    if (mPropertyStorage == null) {
      return null;
    }

    Map<String, Object> storedPaths = mPropertyStorage.getStoredValuesForInterface(this);
    if (!storedPaths.containsKey(path)) {
      return null;
    }
    return storedPaths.get(path);
  }

  public String getPropertyValueString(String path) {
    return (String) getPropertyValue(path);
  }

  public boolean getPropertyValueBoolean(String path) {
    return (boolean) getPropertyValue(path);
  }

  public int getPropertyValueInt(String path) {
    return (int) getPropertyValue(path);
  }

  public long getPropertyValueLong(String path) {
    return (long) getPropertyValue(path);
  }

  public double getPropertyValueDouble(String path) {
    return (double) getPropertyValue(path);
  }

  public byte[] getPropertyValueByteArray(String path) {
    return (byte[]) getPropertyValue(path);
  }

  public DateTime getPropertyValueDateTime(String path) {
    return (DateTime) getPropertyValue(path);
  }
}
