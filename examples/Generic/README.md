# Astarte Device SDK Java Example

This directory contains a minimal example that shows how to use `AstarteGenericDevice`.

The device is instantiated in `ExampleDevice`, the code is commented to highlight all the
things needed to create your Astarte Device.


## Downloading

Add to "dependencies":

``` groovy
dependencies {
    implementation 'org.astarte-platform:devicesdk-generic:$version'
}
```

## Usage

### 1. Device registration and credentials secret emission

*If the device is already registered, skip this section.*

The device must be registered beforehand to obtain its `credentials-secret`.
There are three options to obtain such credentials:
- Using the astartectl command [`astartectl`](https://github.com/astarte-platform/astartectl).
- Using the [`Astarte Dashboard`](https://docs.astarte-platform.org/snapshot/015-astarte_dashboard.html), which is located at `https://dashboard.<your-astarte-domain>`.
- Using `AstartePairingService` class contained in sdk (see following paragraph for details).

#### 1.1 Programmatically Generate an Astarte Device ID

Generate an Astarte device id either randomly or in a deterministic fashion, using a UUID namespace and some unique data.

##### 1.1.1 Random

``` java
String deviceId = AstarteDeviceIdUtils.generateId();
```

##### 1.1.2 Deterministic (UUIDv5)

``` java
UUID uuidNamespace = UUID.fromString("f79ad91f-c638-4889-ae74-9d001a3b4cf8");
String macAddress = "98:75:a8:0d:96:db";
deviceId = AstarteDeviceIdUtils.generateId(uuidNamespace, macAddress);
```

#### 1.2. Register device

Register a device, and obtain its credentials secret. To access the registration API, a valid JWT must be passed.

``` java
String credentialsSecret = new AstartePairingService(pairingUrl, realm).registerDevice(jwt, deviceId);
```

### 2. Astarte device creation

In this example h2 uses persistence in memory but any DB compatible 
with JDBC can be used.

``` java
JdbcConnectionSource connectionSource = new JdbcConnectionSource("jdbc:h2:mem:testDb");
AstarteDevice device =
    new AstarteGenericDevice(
        deviceId,
        realm,
        credentialsSecret,
        new ExampleInterfaceProvider(),
        pairingUrl,
        connectionSource);
```

Check `ExampleInterfaceProvider` to see which interfaces are supported by the device
and how to add more interfaces to your examples.

### 3. Connection listeners

See `ExampleMessageListener` for further details about listening for device connection, disconnection and failure.

``` java
device.setAstarteMessageListener(new ExampleMessageListener());

```

See `ExampleGlobalEventListener` for further details about listening for incoming data from Astarte.


``` java 
device.addGlobalEventListener(new ExampleGlobalEventListener());
```   

### 4. Start the connection

``` java
device.connect();
```

Wait for device to connect. This can be handled asynchronously in the Message Listener.

``` java
while (!device.isConnected()) {
    Thread.sleep(100);
}
```

### 5. Publish

#### 5.1 Publish on a property interface

Retrieve the interface from the device and call `setProperty` on it.

``` java
AstarteDevicePropertyInterface availableSensorsInterface = (AstarteDevicePropertyInterface) device.getInterface(availableSensorsInterfaceName);

availableSensorsInterface.setProperty(String.format("/%s/name", sensorUuid), "randomThermometer");
availableSensorsInterface.setProperty(String.format("/%s/unit", sensorUuid), "Â°C");
```

#### 5.2 Publishing on a datastream interface with individual aggregation

Retrieve the interface from the device and call `streamData` on it.

``` java 
AstarteDeviceDatastreamInterface valuesInterface = (AstarteDeviceDatastreamInterface) device.getInterface(valuesInterfaceName);

Random r = new Random();
String path = String.format("/%s/value", sensorUuid);

double value = 20 + 10 * r.nextDouble();
System.out.println("Streaming value: " + value);
try {
  valuesInterface.streamData(path, value, DateTime.now());
} catch (AstarteTransportException e) {
  e.printStackTrace();
}    
```

### 6. Run examples

#### 6.1 Using an unregistered device

Run the code from the root of the repo with

```
./gradlew run --args="-r <realm> -t <jwt> \
 -p <pairing-url>"
```

where `pairing-url` is the URL to reach Pairing API in your Astarte instance, usually `https://api.<your-astarte-domain>/pairing`.

#### 6.1 Using a registered device

Run the code from the root of the repo with

```
./gradlew run --args="-r <realm> -d <device-id> \
  -c <credentials-secret> -p <pairing-url>"
```

where `pairing-url` is the URL to reach Pairing API in your Astarte instance, usually `https://api.<your-astarte-domain>/pairing`.