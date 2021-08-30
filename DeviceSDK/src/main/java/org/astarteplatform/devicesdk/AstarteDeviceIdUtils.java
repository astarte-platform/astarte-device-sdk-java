package org.astarteplatform.devicesdk;

import java.nio.ByteBuffer;
import java.util.Base64;
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
    Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
    return encoder.encodeToString(randomUUID.toString().getBytes());
  }

  /**
   * Generate a device Id based on UUID namespace identifier and a uniqueData.
   *
   * @return the generated device Id, using the standard Astarte Device ID encoding (base64
   *     urlencoding without padding).
   */
  public static String generateId(UUID namespace, String uniqueData) {
    UUID uuid5FromName = UUID5.nameUUIDFromNamespaceAndString(namespace, uniqueData);
    Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
    ByteBuffer buffer =
        ByteBuffer.allocate(16)
            .putLong(uuid5FromName.getMostSignificantBits())
            .putLong(uuid5FromName.getLeastSignificantBits());
    return encoder.encodeToString(buffer.array());
  }
}
