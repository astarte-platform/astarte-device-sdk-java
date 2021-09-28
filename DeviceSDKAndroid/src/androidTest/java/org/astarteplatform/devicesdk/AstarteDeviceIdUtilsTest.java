package org.astarteplatform.devicesdk;

import static org.junit.Assert.*;

import android.util.Base64;
import java.util.UUID;
import org.junit.Test;

public class AstarteDeviceIdUtilsTest {

  @Test
  public void testGenerateIdFromUUIDDefault() {
    UUID nameUUID = UUID.fromString("f79ad91f-c638-4889-ae74-9d001a3b4cf8");
    String hardwareId = "myidentifierdata";

    String expectedDeviceId = "AJInS0w3VpWpuOqkXhgZdA";
    String deviceId = AstarteDeviceIdUtils.generateId(nameUUID, hardwareId);
    assertEquals(
        "Compare with result from astartectl compute-from-string", expectedDeviceId, deviceId);
  }

  @Test
  public void testGenerateIdFromUUID() {
    UUID nameUUID = UUID.fromString("b068931c-c450-342b-a3f5-b3d276ea4297");
    String hardwareId = "0099112233";

    String expectedDeviceId = "dvt9mLDaWb2vW7bdBJwKCg";
    String deviceId = AstarteDeviceIdUtils.generateId(nameUUID, hardwareId);
    assertEquals(
        "Compare with result from astartectl compute-from-string", expectedDeviceId, deviceId);
  }

  @Test
  public void testGenerateUUID() {
    String astarteDeviceId = AstarteDeviceIdUtils.generateId();
    byte[] astarteDecodedId;
    try {
      astarteDecodedId =
          Base64.decode(astarteDeviceId, Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
    } catch (IllegalArgumentException e) {
      astarteDecodedId = null;
    }

    assertNotNull("Id decoded != NULL", astarteDecodedId);
    assertTrue("Id decoded not empty", astarteDecodedId.length > 0);
  }
}
