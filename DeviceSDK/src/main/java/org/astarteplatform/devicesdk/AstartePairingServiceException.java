package org.astarteplatform.devicesdk;

public class AstartePairingServiceException extends RuntimeException {
  AstartePairingServiceException(String message) {
    super(message);
  }

  AstartePairingServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}
