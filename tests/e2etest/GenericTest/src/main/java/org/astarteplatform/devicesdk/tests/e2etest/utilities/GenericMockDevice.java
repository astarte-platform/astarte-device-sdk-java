package org.astarteplatform.devicesdk.tests.e2etest.utilities;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.DateTime;

public class GenericMockDevice {
  private static String realm;
  private static String deviceId;
  private static String credentialsSecret;
  private static String apiUrl;
  private static String appEngineToken;
  private static String pairingUrl;
  private static String interfacesDir;

  public static final String INTERFACE_SERVER_DATA =
      "org.astarte-platform.java.e2etest.ServerDatastream";
  public static final String INTERFACE_DEVICE_DATA =
      "org.astarte-platform.java.e2etest.DeviceDatastream";
  public static final String INTERFACE_SERVER_AGGR =
      "org.astarte-platform.java.e2etest.ServerAggregate";
  public static final String INTERFACE_DEVICE_AGGR =
      "org.astarte-platform.java.e2etest.DeviceAggregate";
  public static final String INTERFACE_SERVER_PROP =
      "org.astarte-platform.java.e2etest.ServerProperty";
  public static final String INTERFACE_DEVICE_PROP =
      "org.astarte-platform.java.e2etest.DeviceProperty";

  private static DateTime dateTime = new DateTime("2023-09-06T08:39:45.618-08:00");

  private static Map<String, Object> mockDataDictionary;

  static {
    try {
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
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static String getRealm() {
    return realm;
  }

  public static String getDeviceId() {
    return deviceId;
  }

  public static String getCredentialsSecret() {
    return credentialsSecret;
  }

  public static String getApiUrl() {
    return apiUrl;
  }

  public static String getAppEngineToken() {
    return appEngineToken;
  }

  public static String getPairingUrl() {
    return pairingUrl;
  }

  public static String[] getInterfaceNames() {
    return new String[] {
      INTERFACE_SERVER_DATA,
      INTERFACE_DEVICE_DATA,
      INTERFACE_SERVER_AGGR,
      INTERFACE_DEVICE_AGGR,
      INTERFACE_SERVER_PROP,
      INTERFACE_DEVICE_PROP
    };
  }

  public static Map<String, Object> getMockDataDictionary() {
    return mockDataDictionary;
  }

  public static boolean checkEqualityOfHashMaps(Map<String, Object> deviceProperty) {

    boolean isEqual = false;

    if (deviceProperty.size() != getMockDataDictionary().size()) {
      return true;
    }

    for (Map.Entry<String, Object> entry : getMockDataDictionary().entrySet()) {

      if (entry.getValue().getClass().isArray()) {
        if (entry.getValue() instanceof int[]) {
          isEqual = getMockDataDictionary().get(entry.getKey()).equals(entry.getValue());
        } else if (entry.getValue() instanceof double[]) {
          isEqual = getMockDataDictionary().get(entry.getKey()).equals(entry.getValue());
        } else if (entry.getValue() instanceof long[]) {
          isEqual = getMockDataDictionary().get(entry.getKey()).equals(entry.getValue());
        } else if (entry.getValue() instanceof boolean[]) {
          isEqual = getMockDataDictionary().get(entry.getKey()).equals(entry.getValue());
        } else if (entry.getValue() instanceof String[]) {
          isEqual = getMockDataDictionary().get(entry.getKey()).equals(entry.getValue());
        } else if (entry.getValue() instanceof DateTime[]) {
          isEqual = getMockDataDictionary().get(entry.getKey()).equals(entry.getValue());
        } else if (entry.getValue() instanceof byte[]) {
          isEqual = getMockDataDictionary().get(entry.getKey()).equals(entry.getValue());
        } else if (entry.getValue() instanceof byte[][]) {
          isEqual = getMockDataDictionary().get(entry.getKey()).equals(entry.getValue());
        }
      } else if (entry.getValue() instanceof DateTime) {
        isEqual = getMockDataDictionary().get(entry.getKey()).equals(entry.getValue());
      } else {
        if (entry.getValue() instanceof Long) {
          isEqual = getMockDataDictionary().get(entry.getKey()).equals(entry.getValue());
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

  public static boolean compareDates(DateTime entry) {
    return (entry.getMillisOfSecond() != dateTime.getMillisOfSecond());
  }

  private static boolean isNullOrBlank(String param) {
    return param == null || param.trim().isEmpty();
  }
}
