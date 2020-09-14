package org.astarteplatform.devicesdk.protocol;

import java.lang.reflect.Type;
import org.joda.time.DateTime;

abstract class AstarteGenericIndividualEvent {
  private final String mInterfaceName;
  private final String mPath;
  private final Object mValue;

  AstarteGenericIndividualEvent(String interfaceName, String path, Object value) {
    mInterfaceName = interfaceName;
    mPath = path;
    mValue = value;
  }

  public Type valueType() {
    return mValue.getClass();
  }

  public String getInterfaceName() {
    return mInterfaceName;
  }

  public String getPath() {
    return mPath;
  }

  public Object getValue() {
    return mValue;
  }

  public String getValueString() {
    return (String) mValue;
  }

  public boolean getValueBoolean() {
    return (boolean) mValue;
  }

  public int getValueInt() {
    return (int) mValue;
  }

  public long getValueLong() {
    return (long) mValue;
  }

  public double getValueDouble() {
    return (double) mValue;
  }

  public byte[] getValueByteArray() {
    return (byte[]) mValue;
  }

  public DateTime getValueDateTime() {
    return (DateTime) mValue;
  }
}
