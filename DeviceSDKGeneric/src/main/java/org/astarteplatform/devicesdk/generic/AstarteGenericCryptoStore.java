package org.astarteplatform.devicesdk.generic;

import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.ECGenParameterSpec;
import javax.net.ssl.SSLSocketFactory;
import org.astarteplatform.devicesdk.crypto.AstarteCryptoStore;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;

class AstarteGenericCryptoStore implements AstarteCryptoStore {
  private KeyPair m_keyPair;
  private Certificate m_certificate;
  private AstarteGenericMutualSSLSocketFactory m_socketFactory;

  public AstarteGenericCryptoStore() {
    // TODO: this does not store credentials in a KeyStore
    // Should we pass a KeyStore instance from outside or accept a path to a file?
  }

  @Override
  public void clearKeyStore() {
    m_certificate = null;
    m_keyPair = null;
  }

  @Override
  public PublicKey getPublicKey() {
    if (m_keyPair != null) {
      return m_keyPair.getPublic();
    }

    return null;
  }

  @Override
  public Certificate getCertificate() {
    return m_certificate;
  }

  PrivateKey getPrivateKey() {
    if (m_keyPair != null) {
      return m_keyPair.getPrivate();
    }

    return null;
  }

  @Override
  public void setAstarteCertificate(Certificate astarteCertificate) {
    m_certificate = astarteCertificate;
  }

  private void generateKeyPair() throws Exception {
    // Clear the KeyStore first.
    clearKeyStore();

    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
    keyGen.initialize(new ECGenParameterSpec("secp256r1"), new SecureRandom());

    m_keyPair = keyGen.generateKeyPair();
  }

  @Override
  public String generateCSR(String directoryString) throws IOException, OperatorCreationException {
    if (m_keyPair == null) {
      // Generate the key first.
      try {
        generateKeyPair();
      } catch (Exception e) {
        e.printStackTrace();
        return "";
      }
    }
    X500Name subjectName = new X500Name(directoryString);
    JcaPKCS10CertificationRequestBuilder kpGen =
        new JcaPKCS10CertificationRequestBuilder(subjectName, m_keyPair.getPublic());
    JcaContentSignerBuilder csBuilder = new JcaContentSignerBuilder("SHA256withECDSA");
    ContentSigner signer = csBuilder.build(m_keyPair.getPrivate());
    PKCS10CertificationRequest request = kpGen.build(signer);

    return csrToString(request);
  }

  @Override
  public SSLSocketFactory getSSLSocketFactory(boolean ignoreSSLErrors)
      throws KeyManagementException, NoSuchAlgorithmException, CertificateException,
          KeyStoreException, IOException {
    if (m_socketFactory == null) {
      m_socketFactory = new AstarteGenericMutualSSLSocketFactory(this, ignoreSSLErrors);
    }

    return m_socketFactory;
  }

  private String csrToString(PKCS10CertificationRequest csr) throws IOException {
    StringWriter w = new StringWriter();
    JcaPEMWriter p = new JcaPEMWriter(w);
    p.writeObject(csr);
    p.close();
    return w.toString();
  }
}
