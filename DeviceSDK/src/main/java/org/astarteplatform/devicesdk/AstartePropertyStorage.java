package org.astarteplatform.devicesdk;

import java.util.List;
import java.util.Map;
import org.astarteplatform.devicesdk.protocol.AstarteInterface;

public interface AstartePropertyStorage {
  Map<String, Object> getStoredValuesForInterface(AstarteInterface astarteInterface);

  List<String> getStoredPathsForInterface(String interfaceName);

  void setStoredValue(String interfaceName, String path, Object value);

  void removeStoredPath(String interfaceName, String path);

  void purgeProperties(Map<String, List<String>> availableProperties);
}
