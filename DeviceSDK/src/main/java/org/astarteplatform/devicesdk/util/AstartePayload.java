package org.astarteplatform.devicesdk.util;

import java.lang.reflect.Array;
import java.util.*;
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

    o = prepareDateTimeValues(o);

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
    if (decodedObject instanceof Date) {
      decoded.setPayload(new DateTime(decodedObject));
    } else if (decodedObject instanceof BasicBSONList) {
      BasicBSONList list = (BasicBSONList) decodedObject;
      final Object[] valueArray = bsonListToArray(list);
      if (valueArray instanceof Date[]) {
        decoded.setPayload(dateArrayToDateTimeArray((Date[]) valueArray));
      } else {
        decoded.setPayload(valueArray);
      }
    } else {
      if (decodedObject instanceof BasicBSONObject) {
        Map<String, Object> map = (BasicBSONObject) decodedObject;
        for (String key : map.keySet()) {
          Object value = map.get(key);
          if (value instanceof Date) {
            map.put(key, new DateTime(value));
          } else if (value instanceof BasicBSONList) {
            final Object[] valueArray = bsonListToArray((BasicBSONList) value);
            if (valueArray instanceof Date[]) {
              map.put(key, dateArrayToDateTimeArray((Date[]) valueArray));
            } else {
              map.put(key, valueArray);
            }
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

  /**
   * Change all occurrences of DateTime Values to java.util.Date for easier serialization.
   *
   * @param o The Payload to prepare for serialization
   * @return The Payload ready to be serialized
   */
  private static Object prepareDateTimeValues(Object o) {
    if (o instanceof DateTime) {
      return ((DateTime) o).toDate();
    }
    if (o instanceof DateTime[]) {
      return dateTimesArrayToDateList((DateTime[]) o);
    }
    if (o instanceof Map) {
      Map<String, Object> aggregate = new HashMap<String, Object>((Map) o);
      for (Map.Entry<String, Object> entry : aggregate.entrySet()) {
        if (entry.getValue() instanceof DateTime) {
          entry.setValue(((DateTime) entry.getValue()).toDate());
        } else if (entry.getValue() instanceof DateTime[]) {
          entry.setValue(dateTimesArrayToDateList((DateTime[]) entry.getValue()));
        }
      }
      return aggregate;
    }
    return o;
  }

  private static List<Date> dateTimesArrayToDateList(DateTime[] in) {
    List<Date> out = new ArrayList<>();
    for (DateTime d : in) {
      out.add(d.toDate());
    }
    return out;
  }

  private static DateTime[] dateArrayToDateTimeArray(Date[] in) {
    DateTime[] out = new DateTime[in.length];
    for (int i = 0; i < in.length; i++) {
      out[i] = new DateTime(in[i]);
    }
    return out;
  }
}
