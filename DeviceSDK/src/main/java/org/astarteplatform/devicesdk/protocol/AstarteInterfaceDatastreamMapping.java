package org.astarteplatform.devicesdk.protocol;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

public class AstarteInterfaceDatastreamMapping extends AstarteInterfaceMapping {
  public enum MappingReliability {
    UNRELIABLE("unreliable"),
    GUARANTEED("guaranteed"),
    UNIQUE("unique");

    public final String label;

    MappingReliability(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return String.valueOf(label);
    }

    public static MappingReliability fromString(String text) {
      for (MappingReliability t : MappingReliability.values()) {
        if (t.label.equalsIgnoreCase(text)) {
          return t;
        }
      }
      return null;
    }
  }

  public enum MappingRetention {
    DISCARD("discard"),
    VOLATILE("volatile"),
    STORED("stored");

    public final String label;

    MappingRetention(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return String.valueOf(label);
    }

    public static MappingRetention fromString(String text) {
      for (MappingRetention t : MappingRetention.values()) {
        if (t.label.equalsIgnoreCase(text)) {
          return t;
        }
      }
      return null;
    }
  }

  private boolean explicitTimestamp = false;
  private MappingReliability reliability = MappingReliability.UNRELIABLE;
  private MappingRetention retention = MappingRetention.DISCARD;
  private int expiry = 0;

  protected static AstarteInterfaceDatastreamMapping fromJSON(JSONObject astarteMappingObject)
      throws JSONException {
    AstarteInterfaceDatastreamMapping astarteInterfaceDatastreamMapping =
        new AstarteInterfaceDatastreamMapping();
    astarteInterfaceDatastreamMapping.parseMappingFromJSON(astarteMappingObject);
    try {
      astarteInterfaceDatastreamMapping.explicitTimestamp =
          astarteMappingObject.getBoolean("explicit_timestamp");
    } catch (JSONException ignored) {
    }
    try {
      astarteInterfaceDatastreamMapping.reliability =
          MappingReliability.fromString(astarteMappingObject.getString("reliability"));
    } catch (JSONException ignored) {
    }
    try {
      astarteInterfaceDatastreamMapping.retention =
          MappingRetention.fromString(astarteMappingObject.getString("retention"));
    } catch (JSONException ignored) {
    }
    try {
      astarteInterfaceDatastreamMapping.expiry = astarteMappingObject.getInt("expiry");
    } catch (JSONException ignored) {
    }

    return astarteInterfaceDatastreamMapping;
  }

  public boolean isExplicitTimestamp() {
    return explicitTimestamp;
  }

  public MappingReliability getReliability() {
    return reliability;
  }

  public MappingRetention getRetention() {
    return retention;
  }

  public int getExpiry() {
    return expiry;
  }

  public void validatePayload(Object payload, DateTime timestamp)
      throws AstarteInvalidValueException {
    validatePayload(payload);
    if (isExplicitTimestamp() && timestamp == null) {
      throw new AstarteInvalidValueException(
          "This mapping has an explicit timestamp, but no timestamp has been specified.");
    }
  }
}
