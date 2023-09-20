package org.astarteplatform.devicesdk.android;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedKeyManager;

class AstarteAndroidMutualSSLSocketFactory extends SSLSocketFactory {
  private SSLSocketFactory internalSSLSocketFactory;

  public AstarteAndroidMutualSSLSocketFactory()
      throws KeyManagementException, NoSuchAlgorithmException, CertificateException,
          KeyStoreException, IOException {
    // CA certificate is used to authenticate server
    TrustManagerFactory trustManagerFactory =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    KeyStore caStore = KeyStore.getInstance("AndroidCAStore");
    caStore.load(null);
    trustManagerFactory.init(caStore);

    // client key and certificates are sent to server so it can authenticate us
    final KeyStore androidKeyStore = KeyStore.getInstance("AndroidKeyStore");
    androidKeyStore.load(null);

    X509ExtendedKeyManager keyManager =
        new X509ExtendedKeyManager() {
          @Override
          public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
            return "AstarteCertificate";
          }

          @Override
          public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
            return null; // different if you're validating the server's cert
          }

          @Override
          public X509Certificate[] getCertificateChain(String alias) {
            try {
              X509Certificate signedClientCertificate =
                  (X509Certificate) androidKeyStore.getCertificate("AstarteTrustedCertificate");
              return new X509Certificate[] {signedClientCertificate};
            } catch (Exception e) {
              e.printStackTrace();
            }
            return null;
          }

          @Override
          public String[] getClientAliases(String keyType, Principal[] issuers) {
            return new String[] {"AstarteCertificate"};
          }

          @Override
          public String[] getServerAliases(String keyType, Principal[] issuers) {
            return null; // different if you're validating server's cert
          }

          @Override
          public PrivateKey getPrivateKey(String alias) {
            try {
              KeyStore.PrivateKeyEntry privateKeyEntry =
                  (KeyStore.PrivateKeyEntry) androidKeyStore.getEntry("AstarteCertificate", null);
              return privateKeyEntry.getPrivateKey();
            } catch (Exception e) {
              // This thing here fails, but everything's good.
            }
            return null;
          }
        };

    // finally, create SSL socket factory
    SSLContext context = SSLContext.getInstance("TLSv1.2");
    context.init(new KeyManager[] {keyManager}, trustManagerFactory.getTrustManagers(), null);

    internalSSLSocketFactory = context.getSocketFactory();
  }

  @Override
  public String[] getDefaultCipherSuites() {
    return internalSSLSocketFactory.getDefaultCipherSuites();
  }

  @Override
  public String[] getSupportedCipherSuites() {
    return internalSSLSocketFactory.getSupportedCipherSuites();
  }

  @Override
  public Socket createSocket() throws IOException {
    return enableTLSOnSocket(internalSSLSocketFactory.createSocket());
  }

  @Override
  public Socket createSocket(Socket s, String host, int port, boolean autoClose)
      throws IOException {
    return enableTLSOnSocket(internalSSLSocketFactory.createSocket(s, host, port, autoClose));
  }

  @Override
  public Socket createSocket(String host, int port) throws IOException {
    return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port));
  }

  @Override
  public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
      throws IOException {
    return enableTLSOnSocket(
        internalSSLSocketFactory.createSocket(host, port, localHost, localPort));
  }

  @Override
  public Socket createSocket(InetAddress host, int port) throws IOException {
    return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port));
  }

  @Override
  public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
      throws IOException {
    return enableTLSOnSocket(
        internalSSLSocketFactory.createSocket(address, port, localAddress, localPort));
  }

  private Socket enableTLSOnSocket(Socket socket) {
    if (socket instanceof SSLSocket) {
      // Support only TLSv1.2
      ((SSLSocket) socket).setEnabledProtocols(new String[] {"TLSv1.2"});
      ((SSLSocket) socket)
          .addHandshakeCompletedListener(new AstarteAndroidSSLSocketHandshakeCompletedListener());
    }
    try {
      socket.setReuseAddress(false);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return socket;
  }
}
