package org.astarteplatform.devicesdk.generic;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.*;

class AstarteGenericMutualSSLSocketFactory extends SSLSocketFactory {
  private SSLSocketFactory internalSSLSocketFactory;
  private AstarteGenericCryptoStore mCryptoStore;

  public AstarteGenericMutualSSLSocketFactory(
      AstarteGenericCryptoStore cryptoStore, boolean ignoreSSLErrors)
      throws KeyManagementException, NoSuchAlgorithmException, CertificateException,
          KeyStoreException, IOException {
    TrustManager[] trustManagers;
    if (ignoreSSLErrors) {
      TrustManager[] trustAllCerts =
          new TrustManager[] {
            new X509TrustManager() {
              @Override
              public void checkClientTrusted(
                  java.security.cert.X509Certificate[] chain, String authType) {}

              @Override
              public void checkServerTrusted(
                  java.security.cert.X509Certificate[] chain, String authType) {}

              @Override
              public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[] {};
              }
            }
          };
      trustManagers = trustAllCerts;
    } else {
      // CA certificate is used to authenticate server
      String caFile = System.getProperty("java.home") + "/lib/security/cacerts";
      KeyStore caStore = KeyStore.getInstance(KeyStore.getDefaultType());
      try (InputStream is = Files.newInputStream(Paths.get(caFile))) {
        caStore.load(is, null);
      }

      TrustManagerFactory trustManagerFactory =
          TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init(caStore);
      trustManagers = trustManagerFactory.getTrustManagers();
    }

    mCryptoStore = cryptoStore;

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
            Certificate cert = mCryptoStore.getCertificate();
            if (cert != null) {
              return new X509Certificate[] {(X509Certificate) cert};
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
            PrivateKey privateKey = mCryptoStore.getPrivateKey();
            return privateKey;
          }
        };

    // finally, create SSL socket factory
    SSLContext context = SSLContext.getInstance("TLSv1.2");
    context.init(new KeyManager[] {keyManager}, trustManagers, null);

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
          .addHandshakeCompletedListener(new AstarteGenericSSLSocketHandshakeCompletedListener());
    }
    try {
      socket.setReuseAddress(false);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return socket;
  }
}
