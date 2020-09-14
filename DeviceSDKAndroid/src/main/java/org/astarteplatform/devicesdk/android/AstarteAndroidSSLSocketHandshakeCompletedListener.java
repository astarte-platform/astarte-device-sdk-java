package org.astarteplatform.devicesdk.android;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;

final class AstarteAndroidSSLSocketHandshakeCompletedListener
    implements HandshakeCompletedListener {
  @Override
  public void handshakeCompleted(HandshakeCompletedEvent event) {
    // Invalidate the session as soon as the handshake is completed. This is a workaround to
    // avoid session resumption, that leads to HAProxy not resending the CN with Proxy V2 protocol
    event.getSession().invalidate();
  }
}
