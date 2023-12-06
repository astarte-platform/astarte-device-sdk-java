package org.astarteplatform.devicesdk.tests.e2etest.utilities;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.DateTime;

public class GenericMockDevice {
  private String realm;
  private String deviceId;
  private String credentialsSecret;
  private String apiUrl;
  private String appEngineToken;
  private String pairingUrl;
  private String interfacesDir;

  private String interfaceServerData;
  private String interfaceDeviceData;
  private String interfaceServerAggr;
  private String interfaceDeviceAggr;
  private String interfaceServerProp;
  private String interfaceDeviceProp;

  private DateTime dateTime = new DateTime("2023-09-06T08:39:45.618-08:00");

  private Map<String, Object> mockDataDictionary;

  public GenericMockDevice() throws Exception {
    realm = System.getenv("E2E_REALM");
    deviceId = System.getenv("E2E_DEVICE_ID");
    credentialsSecret = System.getenv("E2E_CREDENTIALS_SECRET");
    apiUrl = System.getenv("E2E_API_URL");
    appEngineToken = System.getenv("E2E_TOKEN");

    if (isNullOrBlank(realm)
        || isNullOrBlank(deviceId)
        || isNullOrBlank(credentialsSecret)
        || isNullOrBlank(apiUrl)
        || isNullOrBlank(appEngineToken)) {
      System.out.println(
          "Real: "
              + realm
              + " - device: "
              + deviceId
              + " - credentials secret: "
              + credentialsSecret
              + " - api url: "
              + apiUrl
              + " - token: "
              + appEngineToken);
      throw new Exception("Missing one of the environment variables");
    }

    interfacesDir = Paths.get("build/resources/standard-interfaces").toAbsolutePath().toString();

    pairingUrl = apiUrl + "/pairing";

    interfaceServerData = "org.astarte-platform.java.e2etest.ServerDatastream";
    interfaceDeviceData = "org.astarte-platform.java.e2etest.DeviceDatastream";
    interfaceServerAggr = "org.astarte-platform.java.e2etest.ServerAggregate";
    interfaceDeviceAggr = "org.astarte-platform.java.e2etest.DeviceAggregate";
    interfaceServerProp = "org.astarte-platform.java.e2etest.ServerProperty";
    interfaceDeviceProp = "org.astarte-platform.java.e2etest.DeviceProperty";

    byte[] mockByte = new byte[] {104, 101, 108, 108, 111};

    DateTime dateTime1 = new DateTime("2023-09-13T21:39:45.618-08:00");

    mockDataDictionary = new HashMap<>();
    mockDataDictionary.put("double_endpoint", 5.4);
    mockDataDictionary.put("integer_endpoint", 42);
    mockDataDictionary.put("boolean_endpoint", true);
    mockDataDictionary.put("longinteger_endpoint", 45543543534L);
    mockDataDictionary.put("string_endpoint", "hello");
    mockDataDictionary.put("binaryblob_endpoint", mockByte);
    mockDataDictionary.put("datetime_endpoint", dateTime);
    mockDataDictionary.put("doublearray_endpoint", new double[] {22.2, 322.22, 12.3, 0.1});
    mockDataDictionary.put("integerarray_endpoint", new int[] {22, 322, 0, 10});
    mockDataDictionary.put("booleanarray_endpoint", new boolean[] {true, false, true, false});
    mockDataDictionary.put(
        "longintegerarray_endpoint",
        new long[] {45543543534L, 45543543534L, 45543543534L, 45543543534L});
    mockDataDictionary.put("stringarray_endpoint", new String[] {"hello", " world"});
    mockDataDictionary.put("binaryblobarray_endpoint", new byte[][] {mockByte, mockByte});
    mockDataDictionary.put("datetimearray_endpoint", new DateTime[] {dateTime, dateTime1});
  }

  public String getRealm() {
    return realm;
  }

  public String getDeviceId() {
    return deviceId;
  }

  public String getCredentialsSecret() {
    return credentialsSecret;
  }

  public String getApiUrl() {
    return apiUrl;
  }

  public String getAppEngineToken() {
    return appEngineToken;
  }

  public String getPairingUrl() {
    return pairingUrl;
  }

  public String getInterfaceServerData() {
    return interfaceServerData;
  }

  public String getInterfaceDeviceData() {
    return interfaceDeviceData;
  }

  public String getInterfaceServerAggr() {
    return interfaceServerAggr;
  }

  public String getInterfaceDeviceAggr() {
    return interfaceDeviceAggr;
  }

  public String getInterfaceServerProp() {
    return interfaceServerProp;
  }

  public String getInterfaceDeviceProp() {
    return interfaceDeviceProp;
  }

  public Map<String, Object> getMockDataDictionary() {
    return mockDataDictionary;
  }

  public boolean checkEqualityOfHashMaps(Map<String, Object> deviceProperty) {

    boolean isEqual = false;

    if (deviceProperty.size() != this.getMockDataDictionary().size()) {
      return true;
    }

    for (Map.Entry<String, Object> entry : this.getMockDataDictionary().entrySet()) {

      if (entry.getValue().getClass().isArray()) {
        if (entry.getValue() instanceof int[]) {
          isEqual = this.getMockDataDictionary().get(entry.getKey()).equals(entry.getValue());
        } else if (entry.getValue() instanceof double[]) {
          isEqual = this.getMockDataDictionary().get(entry.getKey()).equals(entry.getValue());
        } else if (entry.getValue() instanceof long[]) {
          isEqual = this.getMockDataDictionary().get(entry.getKey()).equals(entry.getValue());
        } else if (entry.getValue() instanceof boolean[]) {
          isEqual = this.getMockDataDictionary().get(entry.getKey()).equals(entry.getValue());
        } else if (entry.getValue() instanceof String[]) {
          isEqual = this.getMockDataDictionary().get(entry.getKey()).equals(entry.getValue());
        } else if (entry.getValue() instanceof DateTime[]) {
          isEqual = this.getMockDataDictionary().get(entry.getKey()).equals(entry.getValue());
        } else if (entry.getValue() instanceof byte[]) {
          isEqual = this.getMockDataDictionary().get(entry.getKey()).equals(entry.getValue());
        } else if (entry.getValue() instanceof byte[][]) {
          isEqual = this.getMockDataDictionary().get(entry.getKey()).equals(entry.getValue());
        }
      } else if (entry.getValue() instanceof DateTime) {
        isEqual = this.getMockDataDictionary().get(entry.getKey()).equals(entry.getValue());
      } else {
        if (entry.getValue() instanceof Long) {
          isEqual = this.getMockDataDictionary().get(entry.getKey()).equals(entry.getValue());
        } else {
          isEqual =
              deviceProperty.entrySet().stream()
                  .filter(f -> f.getKey().equals(entry.getKey()))
                  .anyMatch(n -> n.getValue().hashCode() == entry.getValue().hashCode());
        }
      }
      if (!isEqual) {
        return true;
      }
    }

    return !isEqual;
  }

  public boolean compareDates(DateTime entry) {
    return (entry.getMillisOfSecond() != dateTime.getMillisOfSecond());
  }

  private static boolean isNullOrBlank(String param) {
    return param == null || param.trim().isEmpty();
  }
}
