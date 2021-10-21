package org.astarteplatform.devicesdk.generic;

import static org.junit.Assert.*;

import org.astarteplatform.devicesdk.util.AstartePayload;
import org.bson.*;
import org.bson.types.Decimal128;
import org.joda.time.DateTime;
import org.junit.Test;

public class AstarteGenericPropertyStorageTest {
  private final BSONDecoder mBSONDecoder = new BasicBSONDecoder();
  private final BSONCallback mBSONCallback = new BasicBSONCallback();

  @Test
  public void testSerializeInt32() {
    int intExpected = 32;

    byte[] serialized = AstartePayload.serialize(intExpected, null);
    Object intDes =
        AstartePayload.deserialize(serialized, mBSONDecoder, mBSONCallback).getPayload();

    assertEquals("validate deserialized int", intExpected, intDes);
  }

  @Test
  public void testSerializeInt64() {
    long longExpected = 32;

    byte[] serialized = AstartePayload.serialize(longExpected, null);
    Object longDes =
        AstartePayload.deserialize(serialized, mBSONDecoder, mBSONCallback).getPayload();

    assertEquals("validate deserialized long", longExpected, longDes);
  }

  @Test
  public void testSerializeString() {
    String strExpected = "hello world";

    byte[] serialized = AstartePayload.serialize(strExpected, null);
    Object str = AstartePayload.deserialize(serialized, mBSONDecoder, mBSONCallback).getPayload();

    assertEquals("validate deserialized string ", strExpected, str);
  }

  @Test
  public void testSerializeDecimal128() {
    Decimal128 decimal128Expected = new Decimal128(128);

    byte[] serialized = AstartePayload.serialize(decimal128Expected, null);
    Object decimal128Des =
        AstartePayload.deserialize(serialized, mBSONDecoder, mBSONCallback).getPayload();

    assertEquals("validate deserialized decimal 128", decimal128Expected, decimal128Des);
  }

  @Test
  public void testSerializeDouble() {
    double doubleExpected = 10.8;

    byte[] serialized = AstartePayload.serialize(doubleExpected, null);
    Object doubleDes =
        AstartePayload.deserialize(serialized, mBSONDecoder, mBSONCallback).getPayload();

    assertEquals("validate deserialized double", doubleExpected, doubleDes);
  }

  @Test
  public void testSerializeBoolean() {
    boolean boolExpected = true;

    byte[] serialized = AstartePayload.serialize(boolExpected, null);
    Object booleanDes =
        AstartePayload.deserialize(serialized, mBSONDecoder, mBSONCallback).getPayload();

    assertEquals("validate deserialized boolean", boolExpected, booleanDes);
  }

  @Test
  public void testSerializeDate() {
    DateTime dateExpected = new DateTime(System.currentTimeMillis());

    byte[] serialized = AstartePayload.serialize(dateExpected, null);
    Object dateDes =
        AstartePayload.deserialize(serialized, mBSONDecoder, mBSONCallback).getPayload();

    assertEquals("validate deserialized date", dateExpected, dateDes);
  }

  @Test
  public void testSerializeBinary() {
    byte[] binaryExpected = {0x1, 0x2, 0x3, 0x4};

    byte[] serialized = AstartePayload.serialize(binaryExpected, null);
    Object binaryDes =
        AstartePayload.deserialize(serialized, mBSONDecoder, mBSONCallback).getPayload();

    assertTrue("validate deserialized binary ", binaryDes instanceof byte[]);
    assertArrayEquals("validate deserialized binary", binaryExpected, (byte[]) binaryDes);
  }

  @Test
  public void testSerializeIntegerArray() {
    Integer[] intExpected = {1, 2, 3};

    byte[] serialized = AstartePayload.serialize(intExpected, null);
    Object intDes =
        AstartePayload.deserialize(serialized, mBSONDecoder, mBSONCallback).getPayload();

    assertTrue("validate deserialized Integer ", intDes instanceof Integer[]);
    assertArrayEquals("validate deserialized Integer", intExpected, (Integer[]) intDes);
  }
}
