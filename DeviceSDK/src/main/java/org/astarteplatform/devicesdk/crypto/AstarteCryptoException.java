package org.astarteplatform.devicesdk.crypto;

public class AstarteCryptoException extends Exception {
  public AstarteCryptoException(String message) {
    super(message);
  }

  public AstarteCryptoException(String message, Throwable cause) {
    super(message, cause);
  }

  public AstarteCryptoException(Throwable cause) {
    super(cause);
  }
}
