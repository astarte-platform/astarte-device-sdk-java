package org.astarteplatform.devicesdk.transport;

public class AstarteTransportException extends Exception {
  public AstarteTransportException(String message) {
    super(message);
  }

  public AstarteTransportException(String message, Throwable cause) {
    super(message, cause);
  }

  public AstarteTransportException(Throwable cause) {
    super(cause);
  }
}
