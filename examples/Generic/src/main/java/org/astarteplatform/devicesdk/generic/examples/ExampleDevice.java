package org.astarteplatform.devicesdk.generic.examples;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import java.util.Random;
import java.util.UUID;
import org.apache.commons.cli.*;
import org.astarteplatform.devicesdk.AstarteDevice;
import org.astarteplatform.devicesdk.AstarteDeviceIdUtils;
import org.astarteplatform.devicesdk.AstartePairingService;
import org.astarteplatform.devicesdk.generic.AstarteGenericDevice;
import org.astarteplatform.devicesdk.protocol.AstarteDeviceDatastreamInterface;
import org.astarteplatform.devicesdk.protocol.AstarteDevicePropertyInterface;
import org.astarteplatform.devicesdk.transport.AstarteTransportException;
import org.joda.time.DateTime;

public class ExampleDevice {
  private static final String availableSensorsInterfaceName =
      "org.astarte-platform.genericsensors.AvailableSensors";
  private static final String valuesInterfaceName = "org.astarte-platform.genericsensors.Values";
  private static final String sensorUuid = "b2c5a6ed-ebe4-4c5c-9d8a-6d2f114fc6e5";

  public static void main(String[] args) throws Exception {
    /*
     *  Initialization of needed parameters, reading them from the command line
     */
    Options options = new Options();

    Option realmOpt = new Option("r", "realm", true, "The target Astarte realm");
    realmOpt.setRequired(true);
    options.addOption(realmOpt);

    Option pairingUrlOpt =
        new Option(
            "p",
            "pairing-url",
            true,
            "The URL to reach Pairing API in the target Astarte instance");
    pairingUrlOpt.setRequired(true);
    options.addOption(pairingUrlOpt);

    Option jwtOpt = new Option("t", "jwt", true, "The jwt for the Astarte Register Device");
    options.addOption(jwtOpt);

    Option deviceIdOpt = new Option("d", "device-id", true, "The device id for the Astarte Device");
    options.addOption(deviceIdOpt);

    Option credentialsSecretOpt =
        new Option(
            "c", "credentials-secret", true, "The credentials secret for the Astarte Device");
    options.addOption(credentialsSecretOpt);

    CommandLineParser parser = new DefaultParser();
    HelpFormatter formatter = new HelpFormatter();
    CommandLine cmd = null;

    try {
      cmd = parser.parse(options, args);
    } catch (ParseException e) {
      System.out.println(e.getMessage());
      formatter.printHelp("Astarte SDK Example", options);

      System.exit(1);
    }

    String realm = cmd.getOptionValue("realm");
    String pairingUrl = cmd.getOptionValue("pairing-url");
    String deviceId = cmd.getOptionValue("device-id");
    String credentialsSecret = cmd.getOptionValue("credentials-secret");
    String jwt = cmd.getOptionValue("jwt");

    if (deviceId == null || deviceId.isEmpty()) {
      /*
       * Astarte device id creation
       */

      UUID uuidNamespace = UUID.fromString("f79ad91f-c638-4889-ae74-9d001a3b4cf8");
      String macAddress = "98:75:a8:0d:96:db";
      deviceId = AstarteDeviceIdUtils.generateId(uuidNamespace, macAddress);
      System.out.println("deviceId: " + deviceId);

      credentialsSecret =
          new AstartePairingService(pairingUrl, realm).registerDevice(jwt, deviceId);
    }

    /*
     * Astarte device creation
     *
     * We use h2 in memory as persistence in this example, but any DB compatible
     * with JDBC can be used.
     *
     * The interfaces supported by the device are populated by ExampleInterfaceProvider, see that
     * class for more details
     */
    JdbcConnectionSource connectionSource = new JdbcConnectionSource("jdbc:h2:mem:testDb");
    ExampleInterfaceProvider interfaceProvider = new ExampleInterfaceProvider();
    AstarteDevice device =
        new AstarteGenericDevice(
            deviceId, realm, credentialsSecret, interfaceProvider, pairingUrl, connectionSource);
    /*
     * Connect listeners
     *
     * See ExampleMessageListener to listen for device connection, disconnection and failure.
     * See ExampleGlobalEventListener to listen for incoming data pushed from Astarte.
     */
    device.setAstarteMessageListener(new ExampleMessageListener());
    device.addGlobalEventListener(new ExampleGlobalEventListener());

    /*
     * Set this if you want to let AstarteDevice take care of the reconnection. The default
     * is false, which means that the application is responsible of reconnecting in case of
     * failures
     */
    device.setAlwaysReconnect(true);

    /*
     * Start the connection
     */
    device.connect();

    /*
     * Wait for device connection
     *
     * This can be handled asynchronously in the Message Listener.
     */
    while (!device.isConnected()) {
      Thread.sleep(100);
    }

    /*
     * Publish on a properties interface
     *
     * Retrieve the interface from the device and call setProperty on it.
     */
    AstarteDevicePropertyInterface availableSensorsInterface =
        (AstarteDevicePropertyInterface) device.getInterface(availableSensorsInterfaceName);

    availableSensorsInterface.setProperty(
        String.format("/%s/name", sensorUuid), "randomThermometer");
    availableSensorsInterface.setProperty(String.format("/%s/unit", sensorUuid), "°C");

    /*
     * Publish on a datastream interface
     *
     * Retrieve the interface from the device and call streamData on it.
     */
    device.addInterface(interfaceProvider.loadInterface(valuesInterfaceName));

    AstarteDeviceDatastreamInterface valuesInterface =
        (AstarteDeviceDatastreamInterface) device.getInterface(valuesInterfaceName);

    Random r = new Random();
    String path = String.format("/%s/value", sensorUuid);

    while (true) {
      double value = 20 + 10 * r.nextDouble();
      System.out.println("Streaming value: " + value);
      try {
        valuesInterface.streamData(path, value, DateTime.now());
      } catch (AstarteTransportException e) {
        e.printStackTrace();
      }
      Thread.sleep(500);
    }
  }
}
