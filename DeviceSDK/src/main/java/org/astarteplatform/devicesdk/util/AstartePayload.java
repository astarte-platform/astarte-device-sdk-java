package org.astarteplatform.devicesdk.util;

import java.lang.reflect.Array;
import java.util.Date;
import java.util.Map;
import org.bson.*;
import org.bson.types.BasicBSONList;
import org.joda.time.DateTime;

/**
 * Utility class made to handle serialization and deserialization of BSON message either for
 * transportation and storage needs
 */
public class AstartePayload {
  public static byte[] serialize(Object o, Date t) {
    if (o == null) {
      // When handling unsets in Astarte MQTT v1, send an empty payload
      return new byte[] {};
    }

    final BasicBSONObject bsonObject = new BasicBSONObject();
    bsonObject.append("v", o);
    if (t != null) bsonObject.append("t", t);

    return new BasicBSONEncoder().encode(bsonObject);
  }

  public static DecodedMessage deserialize(
      byte[] mqttPayload, BSONDecoder mBSONDecoder, BSONCallback mBSONCallback) {
    DecodedMessage decoded = new DecodedMessage();
    // Parse the BSON payload
    mBSONCallback.reset();
    mBSONDecoder.decode(mqttPayload, mBSONCallback);
    BSONObject astartePayload = (BSONObject) mBSONCallback.get();
    // Parse the BSON value

    final Object decodedObject = astartePayload.get("v");
    if (decodedObject instanceof BasicBSONList) {
      BasicBSONList list = (BasicBSONList) decodedObject;
      decoded.setPayload(bsonListToArray(list));
    } else {
      if (decodedObject instanceof BasicBSONObject) {
        Map<String, Object> map = (BasicBSONObject) decodedObject;
        for (String key : map.keySet()) {
          Object value = map.get(key);
          if (value instanceof BasicBSONList) {
            map.put(key, bsonListToArray((BasicBSONList) value));
          }
        }
      }

      decoded.setPayload(decodedObject);
    }
    if (astartePayload.containsField("t")) {
      decoded.setTimestamp(new DateTime(astartePayload.get("t")));
    }
    return decoded;
  }

  private static Object[] bsonListToArray(BasicBSONList list) {
    if (!list.containsField("0")) {
      return new Object[0];
    }
    Class<?> clazz = list.get("0").getClass();
    int size = list.keySet().size();
    Object[] test = (Object[]) Array.newInstance(clazz, size);
    System.arraycopy(list.toArray(), 0, test, 0, size);
    return test;
  }
}
