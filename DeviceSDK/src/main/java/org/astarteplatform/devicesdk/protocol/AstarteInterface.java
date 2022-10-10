package org.astarteplatform.devicesdk.protocol;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.astarteplatform.devicesdk.AstartePropertyStorage;
import org.astarteplatform.devicesdk.transport.AstarteTransport;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class AstarteInterface {
  /* These are always required */
  private String interfaceName;
  private int majorVersion;
  private int minorVersion;

  private Map<String, AstarteInterfaceMapping> mappings;
  private AstarteTransport mAstarteTransport;

  public AstarteTransport getAstarteTransport() {
    return mAstarteTransport;
  }

  public void setAstarteTransport(AstarteTransport astarteTransport) {
    this.mAstarteTransport = astarteTransport;
  }

  public static AstarteInterface fromJSON(
      JSONObject astarteInterfaceObject, AstartePropertyStorage propertyStorage)
      throws JSONException, AstarteInvalidInterfaceException {
    // Create depending on types
    String astarteInterfaceType = astarteInterfaceObject.getString("type");
    String astarteInterfaceOwnership = astarteInterfaceObject.getString("ownership");
    String astarteInterfaceAggregation;
    try {
      astarteInterfaceAggregation = astarteInterfaceObject.getString("aggregation");
    } catch (JSONException e) {
      // Use default
      astarteInterfaceAggregation = "individual";
    }
    boolean astarteInterfaceExplicitTimestamp;
    try {
      astarteInterfaceExplicitTimestamp = astarteInterfaceObject.getBoolean("explicit_timestamp");
    } catch (JSONException e) {
      // Use default
      astarteInterfaceExplicitTimestamp = false;
    }

    AstarteInterface astarteInterface = null;
    if (astarteInterfaceType.equals("properties")) {
      if (astarteInterfaceOwnership.equals("device")) {
        astarteInterface = new AstarteDevicePropertyInterface(propertyStorage);
      } else if (astarteInterfaceOwnership.equals("server")) {
        astarteInterface = new AstarteServerPropertyInterface(propertyStorage);
      }
    } else if (astarteInterfaceType.equals("datastream")) {
      if (astarteInterfaceOwnership.equals("device")) {
        if (astarteInterfaceAggregation.equals("individual")) {
          astarteInterface = new AstarteDeviceDatastreamInterface();
        } else if (astarteInterfaceAggregation.equals("object")) {
          AstarteAggregateDatastreamInterface aggregateDatastreamInterface =
              new AstarteDeviceAggregateDatastreamInterface();
          aggregateDatastreamInterface.explicitTimestamp = astarteInterfaceExplicitTimestamp;
          astarteInterface = aggregateDatastreamInterface;
        }
      } else if (astarteInterfaceOwnership.equals("server")) {
        if (astarteInterfaceAggregation.equals("individual")) {
          astarteInterface = new AstarteServerDatastreamInterface();
        } else if (astarteInterfaceAggregation.equals("object")) {
          AstarteAggregateDatastreamInterface aggregateDatastreamInterface =
              new AstarteServerAggregateDatastreamInterface();
          aggregateDatastreamInterface.explicitTimestamp = astarteInterfaceExplicitTimestamp;
          astarteInterface = aggregateDatastreamInterface;
        }
      }
    }

    if (astarteInterface == null) {
      // Something went really wrong
      throw new AstarteInvalidInterfaceException("Couldn't parse the interface");
    }

    astarteInterface.interfaceName = astarteInterfaceObject.getString("interface_name");
    astarteInterface.majorVersion = astarteInterfaceObject.getInt("version_major");
    astarteInterface.minorVersion = astarteInterfaceObject.getInt("version_minor");

    if (astarteInterface.majorVersion == 0 && astarteInterface.minorVersion == 0) {
      // Invalid Major and Minor
      throw new AstarteInvalidInterfaceException(
          String.format(
              "Both Major and Minor version are 0 on interface %s",
              astarteInterface.getInterfaceName()));
    }

    // Get and create mappings
    astarteInterface.mappings = new HashMap<>();
    JSONArray jsonMappings = astarteInterfaceObject.getJSONArray("mappings");
    for (int i = 0; i < jsonMappings.length(); i++) {
      if (Objects.equals(astarteInterfaceType, "datastream")) {
        astarteInterface.mappings.put(
            jsonMappings.getJSONObject(i).getString("endpoint"),
            AstarteInterfaceDatastreamMapping.fromJSON(jsonMappings.getJSONObject(i)));
      } else {
        astarteInterface.mappings.put(
            jsonMappings.getJSONObject(i).getString("endpoint"),
            AstarteInterfaceMapping.fromJSON(jsonMappings.getJSONObject(i)));
      }
    }

    return astarteInterface;
  }

  public AstarteInterfaceMapping findMappingInInterface(String path)
      throws AstarteInterfaceMappingNotFoundException {

    for (Map.Entry<String, AstarteInterfaceMapping> mappingEntry : getMappings().entrySet()) {
      if (isPathCompatibleWithMapping(path, mappingEntry.getKey())) {
        return mappingEntry.getValue();
      }
    }

    throw new AstarteInterfaceMappingNotFoundException(
        "Mapping " + path + " not found in interface " + this);
  }

  public void validatePayload(String path, Object payload, DateTime timestamp)
      throws AstarteInvalidValueException, AstarteInterfaceMappingNotFoundException {
    findMappingInInterface(path).validatePayload(payload, timestamp);
  }

  public static boolean isPathCompatibleWithMapping(String path, String mapping) {
    // Tokenize and handle paths, to ensure we match parametric interfaces.
    String[] mappingTokens = mapping.split("/");
    String[] pathTokens = path.split("/");
    if (mappingTokens.length != pathTokens.length) {
      return false;
    }

    boolean matches = true;
    for (int k = 0; k < mappingTokens.length; k++) {
      if (!mappingTokens[k].contains("%{")) {
        if (!Objects.equals(mappingTokens[k], pathTokens[k])) {
          matches = false;
          break;
        }
      }
    }

    return matches;
  }

  public String getInterfaceName() {
    return interfaceName;
  }

  public int getMajorVersion() {
    return majorVersion;
  }

  public int getMinorVersion() {
    return minorVersion;
  }

  public Map<String, AstarteInterfaceMapping> getMappings() {
    return mappings;
  }
}
