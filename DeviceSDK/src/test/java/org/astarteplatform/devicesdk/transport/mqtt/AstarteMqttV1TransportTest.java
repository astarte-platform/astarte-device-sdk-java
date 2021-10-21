package org.astarteplatform.devicesdk.transport.mqtt;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import org.astarteplatform.devicesdk.util.AstartePayload;
import org.astarteplatform.devicesdk.util.DecodedMessage;
import org.bson.*;
import org.joda.time.DateTime;
import org.junit.Test;

public class AstarteMqttV1TransportTest {
  private final BSONDecoder mBSONDecoder = new BasicBSONDecoder();
  private final BSONCallback mBSONCallback = new BasicBSONCallback();

  @Test
  public void integerToEncodedBSONTest() {
    Integer i = 3;
    final byte[] encodedPayload = AstartePayload.serialize(i, null);
    final DecodedMessage decodedMessage =
        AstartePayload.deserialize(encodedPayload, mBSONDecoder, mBSONCallback);
    assertEquals(i, decodedMessage.getPayload());
  }

  @Test
  public void integerWTimestampToEncodedBSONTest() {
    Integer i = 3;
    final byte[] encodedPayload =
        AstartePayload.serialize(i, new DateTime(System.currentTimeMillis()).toDate());
    final DecodedMessage decodedMessage =
        AstartePayload.deserialize(encodedPayload, mBSONDecoder, mBSONCallback);
    assertEquals(i, decodedMessage.getPayload());
  }

  @Test
  public void mapToEncodedBSONTest() {
    Map<String, Object> m = new HashMap<>();
    m.put("/int", 1);
    m.put("/double", 1.0);
    m.put("/string", "s");
    final byte[] encodedPayload =
        AstartePayload.serialize(m, new DateTime(System.currentTimeMillis()).toDate());
    final DecodedMessage decodedMessage =
        AstartePayload.deserialize(encodedPayload, mBSONDecoder, mBSONCallback);
    assertEquals(m, decodedMessage.getPayload());
  }

  @Test
  public void arrayToEncodedBSONTest() {
    Integer[] i = {1, 2, 3};
    final byte[] encodedPayload =
        AstartePayload.serialize(i, new DateTime(System.currentTimeMillis()).toDate());
    final DecodedMessage decodedMessage =
        AstartePayload.deserialize(encodedPayload, mBSONDecoder, mBSONCallback);
    assertTrue("validate deserialized Integer ", decodedMessage.getPayload() instanceof Integer[]);
    assertArrayEquals(i, (Integer[]) decodedMessage.getPayload());
  }

  @Test
  public void mapArrayToEncodedBSONTest() {
    Map<String, Object> m = new HashMap<>();
    Integer[] intarray = {1, 2, 3};
    DateTime now = new DateTime();
    DateTime[] dateTimeArray = {now, now};
    m.put("/intarray", intarray);
    m.put("/double", 1.0);
    m.put("/string", "s");
    m.put("/datetime", now);
    m.put("/datetimearray", dateTimeArray);
    final byte[] encodedPayload =
        AstartePayload.serialize(m, new DateTime(System.currentTimeMillis()).toDate());
    final DecodedMessage decodedMessage =
        AstartePayload.deserialize(encodedPayload, mBSONDecoder, mBSONCallback);
    final Map<String, Object> decodedMessagePayload =
        (Map<String, Object>) decodedMessage.getPayload();
    Object decodedIntArray = decodedMessagePayload.get("/intarray");
    Object decodedDateTimeArray = decodedMessagePayload.get("/datetimearray");
    Object decodedDateTime = decodedMessagePayload.get("/datetime");
    assertTrue("validate deserialized Integer ", decodedIntArray instanceof Integer[]);
    assertArrayEquals((Integer[]) m.get("/intarray"), (Integer[]) decodedIntArray);
    assertArrayEquals((DateTime[]) m.get("/datetimearray"), (DateTime[]) decodedDateTimeArray);
    assertEquals(m.get("/datetime"), decodedDateTime);
  }

  @Test
  public void bsonToEncodedBSONTest() {
    final BasicBSONObject bsonObject = new BasicBSONObject();
    bsonObject.append("first", "one");
    byte[] b = new BasicBSONEncoder().encode(bsonObject);
    final byte[] encodedPayload = AstartePayload.serialize(b, null);
    final DecodedMessage decodedMessage =
        AstartePayload.deserialize(encodedPayload, mBSONDecoder, mBSONCallback);
    assertTrue("validate deserialized bson ", decodedMessage.getPayload() instanceof byte[]);
    assertArrayEquals(b, (byte[]) decodedMessage.getPayload());
  }

  @Test
  public void bsonArrayToEncodedBSONTest() {
    final BasicBSONObject bsonObject = new BasicBSONObject();
    bsonObject.append("first", "one");
    byte[][] b = {
      new BasicBSONEncoder().encode(bsonObject), new BasicBSONEncoder().encode(bsonObject)
    };
    final byte[] encodedPayload = AstartePayload.serialize(b, null);
    final DecodedMessage decodedMessage =
        AstartePayload.deserialize(encodedPayload, mBSONDecoder, mBSONCallback);
    assertTrue("validate deserialized bsonArray ", decodedMessage.getPayload() instanceof byte[][]);
    assertArrayEquals(b, (byte[][]) decodedMessage.getPayload());
  }

  @Test
  public void datetimeToEncodedBSONTest() {
    final DateTime d = new DateTime();
    final byte[] encodedPayload = AstartePayload.serialize(d, null);
    final DecodedMessage decodedMessage =
        AstartePayload.deserialize(encodedPayload, mBSONDecoder, mBSONCallback);
    assertTrue("validate deserialized Datetime ", decodedMessage.getPayload() instanceof DateTime);
    assertEquals(d, (DateTime) decodedMessage.getPayload());
  }

  @Test
  public void datetimeArrayToEncodedBSONTest() {
    final DateTime[] d = {new DateTime(), new DateTime()};
    final byte[] encodedPayload = AstartePayload.serialize(d, null);
    final DecodedMessage decodedMessage =
        AstartePayload.deserialize(encodedPayload, mBSONDecoder, mBSONCallback);
    assertTrue(
        "validate deserialized Datetime Array ", decodedMessage.getPayload() instanceof DateTime[]);
    assertArrayEquals(d, (DateTime[]) decodedMessage.getPayload());
  }
}
