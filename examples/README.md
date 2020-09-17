# Astarte Device SDK Java Example

This directory contains a minimal example that shows how to use `AstarteGenericDevice`.

The device is instantiated in `ExampleDevice.java`, the code is commented to highlight all the
things needeed to create your Astarte Device.

You can run the code from the root of the repo with
```
./gradlew run --args="-r <realm> -d <device-id> \
  -c <credentials-secret> -p <pairing-url>"
```
to make the example stream to your Astarte instance.

`pairing-url` is the URL to reach Pairing API in your Astarte instance, which is normally
`https://api.astarte.example.com/pairing` if your Astarte instance is served on
`https://api.astarte.example.com`.

Your must register the device beforehand to obtain its `credentials-secret`. To do that and to see
which data it's sending you can either use
[`astartectl`](https://github.com/astarte-platform/astartectl) or the Astarte Dashboard of your
instance, which is located at `https://dashboard.<your-astarte-domain>`.
