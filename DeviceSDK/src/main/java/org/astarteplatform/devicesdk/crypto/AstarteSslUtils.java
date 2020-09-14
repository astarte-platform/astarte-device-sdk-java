package org.astarteplatform.devicesdk.crypto;

import java.io.StringWriter;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import javax.xml.bind.DatatypeConverter;

class AstarteSslUtils {
  public static String certificateToPEM(Certificate cert) {
    StringWriter sw = new StringWriter();
    try {
      sw.write("-----BEGIN CERTIFICATE-----\n");
      sw.write(
          DatatypeConverter.printBase64Binary(cert.getEncoded()).replaceAll("(.{64})", "$1\n"));
      sw.write("\n-----END CERTIFICATE-----\n");
    } catch (CertificateEncodingException e) {
      e.printStackTrace();
    }
    return sw.toString();
  }
}
