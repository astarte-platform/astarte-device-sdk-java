package org.astarteplatform.devicesdk.tests.e2etest;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import org.astarteplatform.devicesdk.AstarteDevice;
import org.astarteplatform.devicesdk.protocol.*;
import org.astarteplatform.devicesdk.tests.e2etest.utilities.AstarteHttpRequest;
import org.astarteplatform.devicesdk.tests.e2etest.utilities.GenericDeviceSingleton;
import org.astarteplatform.devicesdk.tests.e2etest.utilities.GenericMockDevice;
import org.astarteplatform.devicesdk.transport.AstarteTransportException;
import org.joda.time.DateTime;
import org.json.JSONObject;

public class GenericPropertyDevice implements AstartePropertyEventListener {
  private final AstarteDevice m_astarteGenericDevice;

  private final AstarteHttpRequest m_astarteHttpRequest;

  public GenericPropertyDevice() throws Exception {
    m_astarteGenericDevice = GenericDeviceSingleton.getInstance();
    m_astarteHttpRequest = new AstarteHttpRequest();
  }

  public void propertiesFromDeviceToServer()
      throws AstarteInvalidValueException, AstarteInterfaceMappingNotFoundException,
          AstarteTransportException, IOException, InterruptedException {
    System.out.println("Test for individual properties in the direction from device to server.");

    AstarteDevicePropertyInterface astarteDeviceProperty =
        (AstarteDevicePropertyInterface)
            m_astarteGenericDevice.getInterface(GenericMockDevice.INTERFACE_DEVICE_PROP);

    for (Map.Entry<String, Object> entry : GenericMockDevice.getMockDataDictionary().entrySet()) {
      astarteDeviceProperty.setProperty("/sensorUuid/" + entry.getKey(), entry.getValue());
      Thread.sleep(1000);
    }

    JSONObject response =
        m_astarteHttpRequest.getServerInterface(GenericMockDevice.INTERFACE_DEVICE_PROP);

    if (response.isEmpty()) {
      System.exit(1);
    }

    Map<String, Object> deviceProperty =
        response.getJSONObject("data").getJSONObject("sensorUuid").toMap();

    if (GenericMockDevice.checkEqualityOfHashMaps(deviceProperty)) {
      System.exit(1);
    }
  }

  public void propertiesFromServerToDevice() throws IOException, InterruptedException {
    System.out.println("Test for individual properties in the direction from server to device.");

    AstarteServerPropertyInterface astarteServerDatastreamInterface =
        (AstarteServerPropertyInterface)
            m_astarteGenericDevice.getInterface(GenericMockDevice.INTERFACE_SERVER_PROP);

    astarteServerDatastreamInterface.addListener(this);

    for (Map.Entry<String, Object> entry : GenericMockDevice.getMockDataDictionary().entrySet()) {
      m_astarteHttpRequest.postServerInterface(
          GenericMockDevice.INTERFACE_SERVER_PROP,
          "/sensorUuid/" + entry.getKey(),
          entry.getValue());
      Thread.sleep(500);
    }

    for (Map.Entry<String, Object> entry : GenericMockDevice.getMockDataDictionary().entrySet()) {
      m_astarteHttpRequest.deleteServerInterfaceAsync(
          GenericMockDevice.INTERFACE_SERVER_PROP, "/sensorUuid/" + entry.getKey());
      Thread.sleep(500);
    }
  }

  @Override
  public void propertyReceived(AstartePropertyEvent e) {
    for (Map.Entry<String, Object> entry : GenericMockDevice.getMockDataDictionary().entrySet()) {
      if (entry.getKey().equals(e.getPath().replace("/sensorUuid/", ""))) {
        if (e.getValue().getClass().isArray()) {
          if (e.getValue() instanceof Integer[]) {
            Integer[] dest = new Integer[((Integer[]) e.getValue()).length];
            System.arraycopy(e.getValue(), 0, dest, 0, ((Integer[]) e.getValue()).length);
            if (!Arrays.equals(dest, (Integer[]) e.getValue())) {
              System.exit(1);
            }
          } else if (e.getValue() instanceof Double[]) {
            Double[] dest = new Double[((Double[]) e.getValue()).length];
            System.arraycopy(e.getValue(), 0, dest, 0, (((Double[]) e.getValue()).length));
            if (!Arrays.equals(dest, (Double[]) e.getValue())) {
              System.exit(1);
            }
          } else if (e.getValue() instanceof Long[]) {
            Long[] dest = new Long[((Long[]) e.getValue()).length];
            System.arraycopy(e.getValue(), 0, dest, 0, ((Long[]) e.getValue()).length);
            if (!Arrays.equals(dest, (Long[]) e.getValue())) {
              System.exit(1);
            }
          } else if (e.getValue() instanceof String[]) {
            String[] dest = new String[((String[]) e.getValue()).length];
            System.arraycopy(e.getValue(), 0, dest, 0, ((String[]) e.getValue()).length);
            if (!Arrays.equals(dest, (String[]) e.getValue())) {
              System.exit(1);
            }
          } else if (e.getValue() instanceof byte[]) {
            byte[] dest = new byte[((byte[]) e.getValue()).length];
            System.arraycopy(e.getValue(), 0, dest, 0, ((byte[]) e.getValue()).length);
            if (!Arrays.equals(dest, (byte[]) e.getValue())) {
              System.exit(1);
            }
          } else if (e.getValue() instanceof DateTime[]) {
            DateTime[] dest = new DateTime[((DateTime[]) e.getValue()).length];
            System.arraycopy(e.getValue(), 0, dest, 0, ((DateTime[]) e.getValue()).length);
            if (!Arrays.equals(dest, (DateTime[]) e.getValue())) {
              System.exit(1);
            }
          } else if (e.getValue() instanceof Boolean[]) {
            Boolean[] dest = new Boolean[((Boolean[]) e.getValue()).length];
            System.arraycopy(e.getValue(), 0, dest, 0, ((Boolean[]) e.getValue()).length);
            if (!Arrays.equals(dest, (Boolean[]) e.getValue())) {
              System.exit(1);
            }
          }
        } else if (e.getValue() instanceof DateTime) {
          if (GenericMockDevice.compareDates((DateTime) entry.getValue())) {
            System.exit(1);
          }
        } else {
          if (!e.getValue().equals(entry.getValue())) {
            System.exit(1);
          }
        }
      }
    }
  }

  @Override
  public void propertyUnset(AstartePropertyEvent e) {
    System.out.println(
        "Received unset on interface " + e.getInterfaceName() + ", path: " + e.getPath());
  }
}
