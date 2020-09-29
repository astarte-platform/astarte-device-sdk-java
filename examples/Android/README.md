# Astarte Device SDK Android Example

This directory contains a minimal example that shows how to use `AstarteAndroidDevice`.

The device is instantiated in `AstarteDeviceService.java`, which is instantiated in
`MainActivity.java`. The code is commented to highlight all the things needeed to create your
Astarte Device.

To run the code, you have to populate the private variables at the beginning of `MainActivity.java`.

Your must register the device beforehand to obtain its `credentials-secret`. To do that and to see
which data it's sending you can either use
[`astartectl`](https://github.com/astarte-platform/astartectl) or the Astarte Dashboard of your
instance, which is located at `https://dashboard.<your-astarte-domain>`.

`pairing-url` is the URL to reach Pairing API in your Astarte instance, which is normally
`https://api.astarte.example.com/pairing` if your Astarte instance is served on
`https://api.astarte.example.com`.

To make the example work, the interfaces contained in `src/main/assets/standard-interfaces` have to
be installed in your realm.

To test the communication from the device towards Astarte, pressing the button. This will send a
`ping` payload on the `/event` path in the `org.astarte-platform.genericevents.DeviceEvents`
interface. You can check live data flowing from the device using the Astarte Dashboard.

To send data from Astarte towards the device, you can publish (using `astartectl` or calling
AppEngine API manually) a string on the `/command` path of the
`org.astarte-platform.genericevents.DeviceEvents` interface. That string will be shown in the
`TextEdit` above the button.
