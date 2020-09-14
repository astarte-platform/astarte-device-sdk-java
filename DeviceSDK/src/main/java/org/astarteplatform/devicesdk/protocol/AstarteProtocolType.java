package org.astarteplatform.devicesdk.protocol;

public enum AstarteProtocolType {
  UNKNOWN_PROTOCOL(""),
  ASTARTE_MQTT_V1("astarte_mqtt_v1");

  private final String value;

  AstarteProtocolType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  public static AstarteProtocolType fromString(String text) {
    for (AstarteProtocolType t : AstarteProtocolType.values()) {
      if (t.value.equalsIgnoreCase(text)) {
        return t;
      }
    }
    return null;
  }
}
