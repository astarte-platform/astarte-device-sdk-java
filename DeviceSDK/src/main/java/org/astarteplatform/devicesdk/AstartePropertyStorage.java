package org.astarteplatform.devicesdk;

import java.util.List;
import java.util.Map;
import org.astarteplatform.devicesdk.protocol.AstarteInterface;

public interface AstartePropertyStorage {
  Map<String, Object> getStoredValuesForInterface(AstarteInterface astarteInterface)
      throws AstartePropertyStorageException;

  List<String> getStoredPathsForInterface(String interfaceName)
      throws AstartePropertyStorageException;

  Object getStoredValue(AstarteInterface astarteInterface, String path)
      throws AstartePropertyStorageException;

  void setStoredValue(String interfaceName, String path, Object value)
      throws AstartePropertyStorageException;

  void removeStoredPath(String interfaceName, String path) throws AstartePropertyStorageException;

  void purgeProperties(Map<String, List<String>> availableProperties)
      throws AstartePropertyStorageException;
}
