package org.astarteplatform.devicesdk.protocol;

import java.util.Map;
import org.joda.time.DateTime;

public class AstarteServerValue {
  private final Map<String, Object> mapValue;
  private final Object value;
  private final String interfacePath;
  private final DateTime timestamp;

  private AstarteServerValue(AstarteServerValueBuilder builder) {
    this.mapValue = builder.mapValue;
    this.value = builder.value;
    this.interfacePath = builder.interfacePath;
    this.timestamp = builder.timestamp;
  }

  public Map<String, Object> getMapValue() {
    return mapValue;
  }

  public Object getValue() {
    return value;
  }

  public String getInterfacePath() {
    return interfacePath;
  }

  public DateTime getTimestamp() {
    return timestamp;
  }

  public static class AstarteServerValueBuilder {
    private final Map<String, Object> mapValue;
    private final Object value;
    private String interfacePath;
    private DateTime timestamp;

    public AstarteServerValueBuilder(Object value) {
      this.value = value;
      this.mapValue = null;
    }

    public AstarteServerValueBuilder(Map<String, Object> mapValue) {
      this.value = null;
      this.mapValue = mapValue;
    }

    public AstarteServerValueBuilder interfacePath(String interfacePath) {
      this.interfacePath = interfacePath;
      return this;
    }

    public AstarteServerValueBuilder timestamp(DateTime timestamp) {
      this.timestamp = timestamp;
      return this;
    }

    public AstarteServerValue build() {
      return new AstarteServerValue(this);
    }
  }
}
