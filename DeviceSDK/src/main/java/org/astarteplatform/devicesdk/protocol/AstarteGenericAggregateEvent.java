package org.astarteplatform.devicesdk.protocol;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import org.joda.time.DateTime;

public abstract class AstarteGenericAggregateEvent {
  private final String mInterfaceName;
  private final Map<String, Object> mValues;

  AstarteGenericAggregateEvent(String interfaceName, Map<String, Object> values) {
    mInterfaceName = interfaceName;
    mValues = values;
  }

  public Type valueType() {
    return mValues.getClass();
  }

  public String getInterfaceName() {
    return mInterfaceName;
  }

  public Collection<String> getPaths() {
    return mValues.keySet();
  }

  public boolean hasPath(String path) {
    return mValues.containsKey(path);
  }

  public Map<String, Object> getValues() {
    return mValues;
  }

  public String getValueString(String path) {
    return (String) mValues.get(path);
  }

  public boolean getValueBoolean(String path) {
    return (boolean) mValues.get(path);
  }

  public int getValueInt(String path) {
    return (int) mValues.get(path);
  }

  public long getValueLong(String path) {
    return (long) mValues.get(path);
  }

  public double getValueDouble(String path) {
    return (double) mValues.get(path);
  }

  public byte[] getValueByteArray(String path) {
    return (byte[]) mValues.get(path);
  }

  public DateTime getValueDateTime(String path) {
    return (DateTime) mValues.get(path);
  }
}
