package org.astarteplatform.devicesdk.android;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.test.platform.app.InstrumentationRegistry;
import org.astarteplatform.devicesdk.protocol.AstarteInterfaceMapping;
import org.astarteplatform.devicesdk.util.AstartePayload;
import org.bson.BSONCallback;
import org.bson.BSONDecoder;
import org.bson.BasicBSONCallback;
import org.bson.BasicBSONDecoder;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AstarteAndroidPropertyStorageTest {
  private final BSONDecoder mBSONDecoder = new BasicBSONDecoder();
  private final BSONCallback mBSONCallback = new BasicBSONCallback();
  private final String key = "/test";
  private Context context;

  @Mock AstarteInterfaceMapping mapping;

  @Before
  public void init() {
    this.context = InstrumentationRegistry.getInstrumentation().getContext();
    when(mapping.getPath()).thenReturn("/test");
  }

  @Test
  public void testSerializeInt32() {
    when(mapping.getType()).thenReturn(Integer.class);
    Integer value = 3;
    final SharedPreferences preferences =
        this.context.getSharedPreferences("astarte.property_store.test", Context.MODE_PRIVATE);
    AstarteAndroidPropertyStorage.put(preferences, key, value);
    final Object decoded =
        AstarteAndroidPropertyStorage.get(preferences, mapping, key, mBSONDecoder, mBSONCallback);
    assertEquals(value, decoded);
  }

  @Test
  public void testSerializeIntArray() {
    when(mapping.getType()).thenReturn(Integer[].class);
    Integer[] value = {1, 2};
    final SharedPreferences preferences =
        this.context.getSharedPreferences("astarte.property_store.test", Context.MODE_PRIVATE);
    AstarteAndroidPropertyStorage.put(preferences, key, value);
    final Object decoded =
        AstarteAndroidPropertyStorage.get(preferences, mapping, key, mBSONDecoder, mBSONCallback);

    assertTrue("validate deserialized Integer Array", decoded instanceof Integer[]);
    assertArrayEquals(value, (Integer[]) decoded);
  }

  @Test
  public void testSerializeBsonArray() {
    when(mapping.getType()).thenReturn(byte[][].class);
    byte[][] value = {AstartePayload.serialize("1", null), AstartePayload.serialize(2, null)};
    final SharedPreferences preferences =
        this.context.getSharedPreferences("astarte.property_store.test", Context.MODE_PRIVATE);
    AstarteAndroidPropertyStorage.put(preferences, key, value);
    final Object decoded =
        AstarteAndroidPropertyStorage.get(preferences, mapping, key, mBSONDecoder, mBSONCallback);

    assertTrue("validate deserialized Integer Array", decoded instanceof byte[][]);
    assertArrayEquals(value, (byte[][]) decoded);
  }

  @Test
  public void testSerializeDateTime() {
    when(mapping.getType()).thenReturn(DateTime.class);
    DateTime value = new DateTime();
    final SharedPreferences preferences =
        this.context.getSharedPreferences("astarte.property_store.test", Context.MODE_PRIVATE);
    AstarteAndroidPropertyStorage.put(preferences, key, value);
    final Object decoded =
        AstarteAndroidPropertyStorage.get(preferences, mapping, key, mBSONDecoder, mBSONCallback);

    assertTrue("validate deserialized Integer Array", decoded instanceof DateTime);
    assertEquals(value, decoded);
  }
}
