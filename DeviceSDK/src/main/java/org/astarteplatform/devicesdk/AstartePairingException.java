package org.astarteplatform.devicesdk;

public class AstartePairingException extends Exception {
  AstartePairingException(String message) {
    super(message);
  }

  AstartePairingException(String message, Throwable cause) {
    super(message, cause);
  }
}
