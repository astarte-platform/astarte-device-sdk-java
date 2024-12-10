package org.astarteplatform.devicesdk.tests.e2etest;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.astarteplatform.devicesdk.AstarteDevice;
import org.astarteplatform.devicesdk.protocol.*;
import org.astarteplatform.devicesdk.tests.e2etest.utilities.AstarteHttpRequest;
import org.astarteplatform.devicesdk.tests.e2etest.utilities.GenericDeviceSingleton;
import org.astarteplatform.devicesdk.tests.e2etest.utilities.GenericMockDevice;
import org.astarteplatform.devicesdk.transport.AstarteTransportException;
import org.joda.time.DateTime;
import org.json.JSONObject;

public class GenericDatastreamDevice implements AstarteDatastreamEventListener {

  private AstarteDevice m_astarteGenericDevice;
  private AstarteHttpRequest m_astarteHttpRequest;

  public GenericDatastreamDevice() throws Exception {
    m_astarteGenericDevice = GenericDeviceSingleton.getInstance();
    m_astarteHttpRequest = new AstarteHttpRequest();
  }

  public void datastreamFromDeviceToServer()
      throws AstarteInvalidValueException, AstarteInterfaceMappingNotFoundException,
          AstarteTransportException, IOException, InterruptedException {

    System.out.println("Test for individual datastreams in the direction from device to server.");

    String interfaceName = GenericMockDevice.INTERFACE_DEVICE_DATA;

    AstarteDeviceDatastreamInterface astarteDatastreamInterface =
        (AstarteDeviceDatastreamInterface) m_astarteGenericDevice.getInterface(interfaceName);

    for (Map.Entry<String, Object> entry : GenericMockDevice.getMockDataDictionary().entrySet()) {
      astarteDatastreamInterface.streamData("/" + entry.getKey(), entry.getValue(), DateTime.now());
      Thread.sleep(500);
    }

    JSONObject response = m_astarteHttpRequest.getServerInterface(interfaceName);

    if (response.isEmpty()) {
      System.exit(1);
    }

    JSONObject dataMap = response.getJSONObject("data");

    Map<String, Object> object = new HashMap<>();
    Map<String, Object> map = dataMap.toMap();

    for (Map.Entry<String, Object> entry : map.entrySet()) {
      HashMap<String, Object> temp = (HashMap<String, Object>) entry.getValue();
      String key = entry.getKey();
      for (Map.Entry<String, Object> e : temp.entrySet()) {
        if (e.getKey().equals("value")) {
          object.put(key, e.getValue());
        }
      }
    }
    if (GenericMockDevice.checkEqualityOfHashMaps(object)) {
      System.exit(1);
    }
  }

  public void datastreamFromServerToDevice() throws IOException {

    System.out.println("Test for individual datastreams in the direction from server to device.");

    String interfaceName = GenericMockDevice.INTERFACE_SERVER_DATA;

    AstarteServerDatastreamInterface astarteServerDatastreamInterface =
        (AstarteServerDatastreamInterface) m_astarteGenericDevice.getInterface(interfaceName);

    astarteServerDatastreamInterface.addListener(this);

    for (Map.Entry<String, Object> entry : GenericMockDevice.getMockDataDictionary().entrySet()) {
      m_astarteHttpRequest.postServerInterface(
          interfaceName, "/" + entry.getKey(), entry.getValue());
    }
  }

  @Override
  public void valueReceived(AstarteDatastreamEvent e) {
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
}
