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

  public static AstarteInterfaceMapping findMappingInInterface(
      AstarteInterface astarteInterface, String path)
      throws AstarteInterfaceMappingNotFoundException {

    for (Map.Entry<String, AstarteInterfaceMapping> mappingEntry :
        astarteInterface.getMappings().entrySet()) {
      if (isPathCompatibleWithMapping(path, mappingEntry.getKey())) {
        return mappingEntry.getValue();
      }
    }

    throw new AstarteInterfaceMappingNotFoundException(
        "Mapping " + path + " not found in interface " + astarteInterface);
  }

  public static void validatePayload(AstarteInterface astarteInterface, String path, Object payload)
      throws AstarteInvalidValueException, AstarteInterfaceMappingNotFoundException {
    validatePayload(astarteInterface, path, payload, null);
  }

  public static void validatePayload(
      AstarteInterface astarteInterface, String path, Object payload, DateTime timestamp)
      throws AstarteInvalidValueException, AstarteInterfaceMappingNotFoundException {
    validatePayload(findMappingInInterface(astarteInterface, path), payload, timestamp);
  }

  public static void validatePayload(AstarteInterfaceMapping mapping, Object payload)
      throws AstarteInvalidValueException {
    if (!mapping.isTypeCompatible(payload.getClass())) {
      throw new AstarteInvalidValueException(
          "Payload type "
              + payload.getClass()
              + " is incompatible with mapping type "
              + mapping.getType());
    }
  }

  public static void validatePayload(
      AstarteInterfaceMapping mapping, Object payload, DateTime timestamp)
      throws AstarteInvalidValueException {
    validatePayload(mapping, payload);

    // Act differently depending on the mapping type
    if (mapping instanceof AstarteInterfaceDatastreamMapping) {
      AstarteInterfaceDatastreamMapping datastreamMapping =
          (AstarteInterfaceDatastreamMapping) mapping;
      if (datastreamMapping.isExplicitTimestamp() && timestamp == null) {
        throw new AstarteInvalidValueException(
            "This mapping has an explicit timestamp, " + "but no timestamp has been specified.");
      }
    } else if (timestamp != null) {
      // FIXME: Maybe go easier, and just throw a warning?
      throw new AstarteInvalidValueException(
          "When sending a property, explicit timestamp " + "is always ignored");
    }
  }

  public static void validateAggregate(
      AstarteInterface astarteInterface,
      String pathPrefix,
      Map<String, Object> payload,
      DateTime timestamp)
      throws AstarteInvalidValueException, AstarteInterfaceMappingNotFoundException {
    // We need to ensure the path list matches
    if (astarteInterface.getMappings().size() != payload.size()) {
      throw new AstarteInterfaceMappingNotFoundException(
          "The interface mapping and the payload don't match.");
    }
    for (Map.Entry<String, Object> payloadEntry : payload.entrySet()) {
      String path = pathPrefix + "/" + payloadEntry.getKey();
      validatePayload(
          findMappingInInterface(astarteInterface, path), payloadEntry.getValue(), timestamp);
    }
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
