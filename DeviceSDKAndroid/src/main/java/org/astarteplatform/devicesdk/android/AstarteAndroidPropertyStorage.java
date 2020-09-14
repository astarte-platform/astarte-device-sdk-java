package org.astarteplatform.devicesdk.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.astarteplatform.devicesdk.AstartePropertyStorage;
import org.astarteplatform.devicesdk.protocol.AstarteInterface;
import org.astarteplatform.devicesdk.protocol.AstarteInterfaceMapping;
import org.joda.time.DateTime;

class AstarteAndroidPropertyStorage implements AstartePropertyStorage {
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

          if (entry.getValue().getType() == String.class) {
            returnedPaths.put(
                preferencesEntry.getKey(),
                interfaceSharedPreference.getString(preferencesEntry.getKey(), null));
          } else if (entry.getValue().getType() == Integer.class) {
            returnedPaths.put(
                preferencesEntry.getKey(),
                interfaceSharedPreference.getInt(preferencesEntry.getKey(), 0));
          } else if (entry.getValue().getType() == Long.class) {
            returnedPaths.put(
                preferencesEntry.getKey(),
                interfaceSharedPreference.getLong(preferencesEntry.getKey(), 0));
          } else if (entry.getValue().getType() == Boolean.class) {
            returnedPaths.put(
                preferencesEntry.getKey(),
                interfaceSharedPreference.getBoolean(preferencesEntry.getKey(), false));
          } else if (entry.getValue().getType() == Double.class) {
            returnedPaths.put(
                preferencesEntry.getKey(),
                (double) interfaceSharedPreference.getFloat(preferencesEntry.getKey(), 0));
          } else if (entry.getValue().getType() == Byte[].class) {
            String encodedValue =
                interfaceSharedPreference.getString(preferencesEntry.getKey(), null);
            returnedPaths.put(
                preferencesEntry.getKey(), Base64.decode(encodedValue, Base64.DEFAULT));
          } else if (entry.getValue().getType() == DateTime.class) {
            String dtAsString =
                interfaceSharedPreference.getString(preferencesEntry.getKey(), null);
            DateTime dt = DateTime.parse(dtAsString);
            returnedPaths.put(preferencesEntry.getKey(), dt);
          }
        }
      }

      return returnedPaths;
    }
  }

  @Override
  public void setStoredValue(String interfaceName, String path, Object value) {
    synchronized (this) {
      SharedPreferences interfaceSharedPreference = getSharedPreferencesFor(interfaceName);

      SharedPreferences.Editor editor = interfaceSharedPreference.edit();
      if (value instanceof String) {
        editor.putString(path, (String) value);
      } else if (value.getClass() == int.class) {
        editor.putInt(path, (int) value);
      } else if (value.getClass() == long.class) {
        editor.putLong(path, (long) value);
      } else if (value.getClass() == boolean.class) {
        editor.putBoolean(path, (boolean) value);
      } else if (value.getClass() == double.class) {
        editor.putFloat(path, (float) value);
      } else if (value instanceof byte[]) {
        byte[] byteArray = (byte[]) value;
        editor.putString(path, Base64.encodeToString(byteArray, Base64.DEFAULT));
      } else if (value instanceof DateTime) {
        editor.putString(path, value.toString());
      }

      editor.apply();
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

  private SharedPreferences getSharedPreferencesFor(String astarteInterfaceName) {
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
}
