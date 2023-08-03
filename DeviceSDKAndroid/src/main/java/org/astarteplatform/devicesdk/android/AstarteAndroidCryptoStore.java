package org.astarteplatform.devicesdk.android;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;
import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLSocketFactory;
import org.astarteplatform.devicesdk.crypto.AstarteCryptoStore;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;

class AstarteAndroidCryptoStore implements AstarteCryptoStore {
  private PublicKey m_publicKey;
  private KeyPair m_keyPair = null;
  private Certificate m_certificate;
  private static final String TAG = "AndroidCryptoStore";

  public AstarteAndroidCryptoStore() {
    // See if we have something in our Android Keystore already
    try {
      java.security.KeyStore ks = java.security.KeyStore.getInstance("AndroidKeyStore");
      ks.load(null);

      // Load the key pair from the Android Key Store
      KeyStore.Entry entry = ks.getEntry("AstarteCertificate", null);
      KeyStore.Entry trustedEntry = ks.getEntry("AstarteTrustedCertificate", null);

      if (entry == null) {
        Log.w(TAG, "No key pair found!");
        return;
      }

      if (trustedEntry instanceof KeyStore.TrustedCertificateEntry) {
        Log.i(TAG, "Found a valid, trusted Certificate for Astarte");
        m_certificate = ((KeyStore.TrustedCertificateEntry) trustedEntry).getTrustedCertificate();
        Log.i(TAG, "Certificate successfully loaded!");
      } else if (entry instanceof KeyStore.PrivateKeyEntry) {
        m_publicKey = ((KeyStore.PrivateKeyEntry) entry).getCertificate().getPublicKey();
        Log.i(TAG, "Found the base Keypair.");
      }
    } catch (KeyStoreException e) {
      Log.e(TAG, "Could not load keystore! KeyStoreException: " + e);
    } catch (Exception e) {
      Log.e(TAG, "Could not load keystore! KeyStoreException: " + e);
    }
  }

  @Override
  public void clearKeyStore() {
    try {
      java.security.KeyStore ks = java.security.KeyStore.getInstance("AndroidKeyStore");
      ks.load(null);

      ks.deleteEntry("AstarteCertificate");
      ks.deleteEntry("AstarteTrustedCertificate");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public PublicKey getPublicKey() {
    return m_publicKey;
  }

  @Override
  public Certificate getCertificate() {
    return m_certificate;
  }

  @Override
  public void setAstarteCertificate(Certificate astarteCertificate) {
    try {
      java.security.KeyStore ks = java.security.KeyStore.getInstance("AndroidKeyStore");
      ks.load(null);

      KeyStore.TrustedCertificateEntry entry =
          new KeyStore.TrustedCertificateEntry(astarteCertificate);
      ks.setEntry("AstarteTrustedCertificate", entry, null);
    } catch (KeyStoreException e) {
      e.printStackTrace();
      Log.e(TAG, "Could not load keystore! KeyStoreException: " + e);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void generateKeyPair() throws Exception {
    // Clear the KeyStore first.
    clearKeyStore();

    KeyPairGenerator keyGen =
        KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
    keyGen.initialize(
        new KeyGenParameterSpec.Builder(
                "AstarteCertificate", KeyProperties.PURPOSE_VERIFY | KeyProperties.PURPOSE_SIGN)
            .setDigests(
                KeyProperties.DIGEST_NONE, KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build());

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
    JcaContentSignerBuilder csBuilder = new JcaContentSignerBuilder("SHA256withRSA");
    ContentSigner signer = csBuilder.build(m_keyPair.getPrivate());
    PKCS10CertificationRequest request = kpGen.build(signer);

    return csrToString(request);
  }

  @Override
  public SSLSocketFactory getSSLSocketFactory()
      throws KeyManagementException, NoSuchAlgorithmException, CertificateException,
          KeyStoreException, IOException {
    return new AstarteAndroidMutualSSLSocketFactory();
  }

  private String csrToString(PKCS10CertificationRequest csr) throws IOException {
    StringWriter w = new StringWriter();
    JcaPEMWriter p = new JcaPEMWriter(w);
    p.writeObject(csr);
    p.close();
    return w.toString();
  }
}
