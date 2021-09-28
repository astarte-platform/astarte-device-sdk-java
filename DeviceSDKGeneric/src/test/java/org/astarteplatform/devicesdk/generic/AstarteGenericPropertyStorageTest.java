package org.astarteplatform.devicesdk.generic;

import static org.junit.Assert.*;

import java.util.Date;
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

    byte[] serialized = AstarteGenericPropertyStorage.serialize(intExpected);
    Object intDes =
        AstarteGenericPropertyStorage.deserialize(serialized, mBSONCallback, mBSONDecoder);

    assertEquals("validate deserialized int", intExpected, intDes);
  }

  @Test
  public void testSerializeInt64() {
    long longExpected = 32;

    byte[] serialized = AstarteGenericPropertyStorage.serialize(longExpected);
    Object longDes =
        AstarteGenericPropertyStorage.deserialize(serialized, mBSONCallback, mBSONDecoder);

    assertEquals("validate deserialized long", longExpected, longDes);
  }

  @Test
  public void testSerializeString() {
    String strExpected = "hello world";

    byte[] serialized = AstarteGenericPropertyStorage.serialize(strExpected);
    Object str = AstarteGenericPropertyStorage.deserialize(serialized, mBSONCallback, mBSONDecoder);

    assertEquals("validate deserialized string ", strExpected, str);
  }

  @Test
  public void testSerializeDecimal128() {
    Decimal128 decimal128Expected = new Decimal128(128);

    byte[] serialized = AstarteGenericPropertyStorage.serialize(decimal128Expected);
    Object decimal128Des =
        AstarteGenericPropertyStorage.deserialize(serialized, mBSONCallback, mBSONDecoder);

    assertEquals("validate deserialized decimal 128", decimal128Expected, decimal128Des);
  }

  @Test
  public void testSerializeDouble() {
    double doubleExpected = 10.8;

    byte[] serialized = AstarteGenericPropertyStorage.serialize(doubleExpected);
    Object doubleDes =
        AstarteGenericPropertyStorage.deserialize(serialized, mBSONCallback, mBSONDecoder);

    assertEquals("validate deserialized double", doubleExpected, doubleDes);
  }

  @Test
  public void testSerializeBoolean() {
    boolean boolExpected = true;

    byte[] serialized = AstarteGenericPropertyStorage.serialize(boolExpected);
    Object booleanDes =
        AstarteGenericPropertyStorage.deserialize(serialized, mBSONCallback, mBSONDecoder);

    assertEquals("validate deserialized boolean", boolExpected, booleanDes);
  }

  @Test
  public void testSerializeDate() {
    Date dateExpected = new DateTime(System.currentTimeMillis()).toDate();

    byte[] serialized = AstarteGenericPropertyStorage.serialize(dateExpected);
    Object dateDes =
        AstarteGenericPropertyStorage.deserialize(serialized, mBSONCallback, mBSONDecoder);

    assertEquals("validate deserialized date", dateExpected, dateDes);
  }

  @Test
  public void testSerializeBinary() {
    byte[] binaryExpected = {0x1, 0x2, 0x3, 0x4};

    byte[] serialized = AstarteGenericPropertyStorage.serialize(binaryExpected);
    Object binaryDes =
        AstarteGenericPropertyStorage.deserialize(serialized, mBSONCallback, mBSONDecoder);

    assertTrue("validate deserialized binary ", binaryDes instanceof byte[]);
    assertArrayEquals("validate deserialized binary", binaryExpected, (byte[]) binaryDes);
  }
}
