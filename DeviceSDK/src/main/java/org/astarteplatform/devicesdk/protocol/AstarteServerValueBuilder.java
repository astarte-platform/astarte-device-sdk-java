package org.astarteplatform.devicesdk.protocol;

import org.joda.time.DateTime;

public interface AstarteServerValueBuilder {
  AstarteServerValue build(String interfacePath, Object serverValue, DateTime timestamp);
}
