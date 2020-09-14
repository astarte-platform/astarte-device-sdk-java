package org.astarteplatform.devicesdk.protocol;

public class AstarteInterfaceNotFoundException extends Exception {
  public AstarteInterfaceNotFoundException(String message) {
    super(message);
  }

  public AstarteInterfaceNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public AstarteInterfaceNotFoundException(Throwable cause) {
    super(cause);
  }
}
