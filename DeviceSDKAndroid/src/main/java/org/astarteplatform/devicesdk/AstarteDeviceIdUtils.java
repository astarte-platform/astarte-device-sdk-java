package org.astarteplatform.devicesdk;

import android.util.Base64;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.astarteplatform.devicesdk.util.UUID5;

/** The class helps to generate an unique Astarte deviceId */
public class AstarteDeviceIdUtils {

  /**
   * Generate a device Id in a random way based on UUIDv4.
   *
   * @return the generated device Id, using the standard Astarte Device ID encoding (base64
   *     urlencoding without padding).
   */
  public static String generateId() {
    UUID randomUUID = UUID.randomUUID();
    return Base64.encodeToString(
        randomUUID.toString().getBytes(StandardCharsets.UTF_8),
        Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
  }

  /**
   * Generate a device Id based on UUID namespace identifier and a uniqueData.
   *
   * @return the generated device Id, using the standard Astarte Device ID encoding (base64
   *     urlencoding without padding).
   */
  public static String generateId(UUID namespace, String uniqueData) {
    UUID uuid5FromName = UUID5.nameUUIDFromNamespaceAndString(namespace, uniqueData);
    ByteBuffer buffer =
        ByteBuffer.allocate(16)
            .putLong(uuid5FromName.getMostSignificantBits())
            .putLong(uuid5FromName.getLeastSignificantBits());
    return Base64.encodeToString(
        buffer.array(), Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
  }
}
