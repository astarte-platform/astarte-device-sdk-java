package org.astarteplatform.devicesdk.generic;

import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import java.sql.SQLException;
import org.astarteplatform.devicesdk.AstarteInterfaceProvider;
import org.astarteplatform.devicesdk.AstartePairableDevice;
import org.astarteplatform.devicesdk.AstartePairingHandler;
import org.astarteplatform.devicesdk.AstartePropertyStorage;
import org.astarteplatform.devicesdk.protocol.AstarteInvalidInterfaceException;
import org.astarteplatform.devicesdk.transport.AstarteFailedMessageStorage;
import org.json.JSONException;

public class AstarteGenericDevice extends AstartePairableDevice {
  public AstarteGenericDevice(
      String deviceId,
      String astarteRealm,
      String credentialSecret,
      AstarteInterfaceProvider interfaceProvider,
      String pairingBaseUrl,
      ConnectionSource connectionSource)
      throws JSONException, AstarteInvalidInterfaceException, SQLException {
    this(
        deviceId,
        astarteRealm,
        credentialSecret,
        interfaceProvider,
        pairingBaseUrl,
        new AstarteGenericPropertyStorage(
            DaoManager.createDao(connectionSource, AstarteGenericPropertyEntry.class)),
        new AstarteGenericFailedMessageStorage(
            DaoManager.createDao(connectionSource, AstarteGenericFailedMessage.class)));
  }

  public AstarteGenericDevice(
      String deviceId,
      String astarteRealm,
      String credentialSecret,
      AstarteInterfaceProvider interfaceProvider,
      String pairingBaseUrl,
      AstartePropertyStorage propertyStorage,
      AstarteFailedMessageStorage failedMessageStorage)
      throws JSONException, AstarteInvalidInterfaceException {
    super(
        new AstartePairingHandler(
            pairingBaseUrl,
            astarteRealm,
            deviceId,
            credentialSecret,
            new AstarteGenericCryptoStore()),
        propertyStorage,
        failedMessageStorage,
        interfaceProvider);
  }
}
