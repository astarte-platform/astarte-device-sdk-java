package org.astarteplatform.devicesdk.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.astarteplatform.devicesdk.AstartePropertyStorage;
import org.astarteplatform.devicesdk.AstartePropertyStorageException;
import org.astarteplatform.devicesdk.protocol.AstarteInterface;
import org.astarteplatform.devicesdk.protocol.AstarteInterfaceMapping;
import org.astarteplatform.devicesdk.util.AstartePayload;
import org.astarteplatform.devicesdk.util.DecodedMessage;
import org.bson.BSONCallback;
import org.bson.BSONDecoder;
import org.bson.BasicBSONCallback;
import org.bson.BasicBSONDecoder;
import org.joda.time.DateTime;

class AstarteAndroidPropertyStorage implements AstartePropertyStorage {
  private final BSONDecoder mBSONDecoder = new BasicBSONDecoder();
  private final BSONCallback mBSONCallback = new BasicBSONCallback();
  private final Context rootContext;
  private final String sharedPreferencesKeyPrefix;
  private final Map<String, SharedPreferences> sharedPreferencesCache;

  AstarteAndroidPropertyStorage(Context rootContext, String sharedPreferencesKeyPrefix) {
    this.rootContext = rootContext;
    this.sharedPreferencesKeyPrefix = sharedPreferencesKeyPrefix;
    sharedPreferencesCache = new HashMap<>();
  }

  @Override
  public List<String> getStoredPathsForInterface(String astarteInterfaceName) {
    synchronized (this) {
      List<String> returnedPaths = new ArrayList<>();

      SharedPreferences interfaceSharedPreference = getSharedPreferencesFor(astarteInterfaceName);

      for (Map.Entry<String, ?> preferencesEntry : interfaceSharedPreference.getAll().entrySet()) {
        returnedPaths.add(preferencesEntry.getKey());
      }

      return returnedPaths;
    }
  }

  @Override
  public Object getStoredValue(AstarteInterface astarteInterface, String path)
      throws AstartePropertyStorageException {
    Map<String, Object> storedValues = getStoredValuesForInterface(astarteInterface);
    for (Map.Entry<String, Object> entry : storedValues.entrySet()) {
      if (!AstarteInterface.isPathCompatibleWithMapping(entry.getKey(), path)) {
        continue;
      }
      return entry.getValue();
    }
    return null;
  }

  @Override
  public Map<String, Object> getStoredValuesForInterface(AstarteInterface astarteInterface) {
    synchronized (this) {
      SharedPreferences interfaceSharedPreference =
          getSharedPreferencesFor(astarteInterface.getInterfaceName());

      Map<String, Object> returnedPaths = new HashMap<>();
      for (Map.Entry<String, ?> preferencesEntry : interfaceSharedPreference.getAll().entrySet()) {
        for (Map.Entry<String, AstarteInterfaceMapping> entry :
            astarteInterface.getMappings().entrySet()) {
          if (!AstarteInterface.isPathCompatibleWithMapping(
              preferencesEntry.getKey(), entry.getKey())) {
            continue;
          }
          String key = preferencesEntry.getKey();
          final AstarteInterfaceMapping mapping = entry.getValue();
          Object value = get(interfaceSharedPreference, mapping, key, mBSONDecoder, mBSONCallback);
          returnedPaths.put(key, value);
        }
      }

      return returnedPaths;
    }
  }

  @Override
  public void setStoredValue(String interfaceName, String path, Object value) {
    synchronized (this) {
      SharedPreferences interfaceSharedPreference = getSharedPreferencesFor(interfaceName);
      put(interfaceSharedPreference, path, value);
    }
  }

  @Override
  public void removeStoredPath(String interfaceName, String path) {
    synchronized (this) {
      SharedPreferences interfaceSharedPreference = getSharedPreferencesFor(interfaceName);

      SharedPreferences.Editor editor = interfaceSharedPreference.edit();
      editor.remove(path);

      editor.apply();
    }
  }

  @Override
  public void purgeProperties(Map<String, List<String>> availableProperties) {
    synchronized (this) {
      // When we get this, we want to clear all keys which aren't part of the received list.
      for (Map.Entry<String, List<String>> entry : availableProperties.entrySet()) {
        for (String storedPath : getStoredPathsForInterface(entry.getKey())) {
          if (!entry.getValue().contains(storedPath)) {
            // Purge!
            removeStoredPath(entry.getKey(), storedPath);
          }
        }
      }
    }
  }

  protected SharedPreferences getSharedPreferencesFor(String astarteInterfaceName) {
    SharedPreferences interfaceSharedPreference;
    if (!sharedPreferencesCache.containsKey(astarteInterfaceName)) {
      interfaceSharedPreference =
          rootContext.getSharedPreferences(
              sharedPreferencesKeyPrefix + "." + astarteInterfaceName, Context.MODE_PRIVATE);
      sharedPreferencesCache.put(astarteInterfaceName, interfaceSharedPreference);
    } else {
      interfaceSharedPreference = sharedPreferencesCache.get(astarteInterfaceName);
    }

    return interfaceSharedPreference;
  }

  protected static void put(
      SharedPreferences interfaceSharedPreference, String path, Object value) {
    SharedPreferences.Editor editor = interfaceSharedPreference.edit();
    if (value instanceof String) {
      editor.putString(path, (String) value);
    } else if (value.getClass() == Integer.class) {
      editor.putInt(path, (int) value);
    } else if (value.getClass() == Long.class) {
      editor.putLong(path, (long) value);
    } else if (value.getClass() == Boolean.class) {
      editor.putBoolean(path, (boolean) value);
    } else if (value.getClass() == Double.class) {
      editor.putFloat(path, (float) value);
    } else if (value.getClass().isArray()) {
      byte[] byteArray = AstartePayload.serialize(value, null);
      editor.putString(path, Base64.encodeToString(byteArray, Base64.DEFAULT));
    } else if (value instanceof DateTime) {
      editor.putString(path, value.toString());
    }

    editor.apply();
  }

  protected static Object get(
      SharedPreferences interfaceSharedPreference,
      AstarteInterfaceMapping mapping,
      String key,
      BSONDecoder mBSONDecoder,
      BSONCallback mBSONCallback) {
    if (mapping.getType() == String.class) {
      return interfaceSharedPreference.getString(key, null);
    } else if (mapping.getType() == Integer.class) {
      return interfaceSharedPreference.getInt(key, 0);
    } else if (mapping.getType() == Long.class) {
      return interfaceSharedPreference.getLong(key, 0);
    } else if (mapping.getType() == Boolean.class) {
      return interfaceSharedPreference.getBoolean(key, false);
    } else if (mapping.getType() == Double.class) {
      return (double) interfaceSharedPreference.getFloat(key, 0);
    } else if (((Class) mapping.getType()).isArray()) {
      String encodedValue = interfaceSharedPreference.getString(key, null);
      final byte[] value = Base64.decode(encodedValue, Base64.DEFAULT);
      final DecodedMessage decodedValue =
          AstartePayload.deserialize(value, mBSONDecoder, mBSONCallback);
      return decodedValue.getPayload();
    } else if (mapping.getType() == DateTime.class) {
      String dtAsString = interfaceSharedPreference.getString(key, null);
      DateTime dt = DateTime.parse(dtAsString);
      return dt;
    }
    return null;
  }
}
