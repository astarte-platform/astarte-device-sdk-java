package org.astarteplatform.devicesdk.android;

import android.content.Context;
import org.astarteplatform.devicesdk.AstarteInterfaceProvider;
import org.astarteplatform.devicesdk.AstartePairableDevice;
import org.astarteplatform.devicesdk.AstartePairingHandler;
import org.astarteplatform.devicesdk.protocol.AstarteInvalidInterfaceException;
import org.json.JSONException;

public class AstarteAndroidDevice extends AstartePairableDevice {
  public AstarteAndroidDevice(
      String deviceId,
      String astarteRealm,
      String credentialSecret,
      AstarteInterfaceProvider interfaceProvider,
      String pairingBaseUrl,
      Context context)
      throws JSONException, AstarteInvalidInterfaceException {
    this(
        deviceId,
        astarteRealm,
        credentialSecret,
        interfaceProvider,
        pairingBaseUrl,
        context,
        false);
  }

  public AstarteAndroidDevice(
      String deviceId,
      String astarteRealm,
      String credentialSecret,
      AstarteInterfaceProvider interfaceProvider,
      String pairingBaseUrl,
      Context context,
      boolean ignoreSSLErrors)
      throws JSONException, AstarteInvalidInterfaceException {
    super(
        new AstartePairingHandler(
            pairingBaseUrl,
            astarteRealm,
            deviceId,
            credentialSecret,
            new AstarteAndroidCryptoStore(),
            ignoreSSLErrors),
        new AstarteAndroidPropertyStorage(context, "astarte.property_store." + deviceId),
        new AstarteAndroidFailedMessageStorage(
            AstarteAndroidRoomDatabase.getDatabase(context).astarteFailedMessageDao()),
        interfaceProvider);
  }
}
