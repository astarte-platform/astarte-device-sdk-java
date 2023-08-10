package org.astarteplatform.devicesdk.crypto;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLSocketFactory;
import org.bouncycastle.operator.OperatorCreationException;

public interface AstarteCryptoStore {
  void clearKeyStore();

  PublicKey getPublicKey();

  Certificate getCertificate();

  void setAstarteCertificate(Certificate astarteCertificate);

  String generateCSR(String directoryString)
      throws IOException, OperatorCreationException, InvalidAlgorithmParameterException,
          NoSuchAlgorithmException, NoSuchProviderException;

  SSLSocketFactory getSSLSocketFactory()
      throws KeyManagementException, NoSuchAlgorithmException, CertificateException,
          KeyStoreException, IOException;
}
