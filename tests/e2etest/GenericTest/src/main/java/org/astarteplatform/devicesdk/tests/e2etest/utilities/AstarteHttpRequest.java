package org.astarteplatform.devicesdk.tests.e2etest.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class AstarteHttpRequest {

  private String m_appengineToken;

  public AstarteHttpRequest() throws Exception {
    m_appengineToken = GenericMockDevice.getAppEngineToken();
  }

  public JSONObject getServerInterface(String interfaceName) throws IOException {

    String httpQuery =
        GenericMockDevice.getApiUrl()
            + "/appengine/v1/"
            + GenericMockDevice.getRealm()
            + "/devices/"
            + GenericMockDevice.getDeviceId()
            + "/interfaces/"
            + interfaceName;

    URL obj = new URL(httpQuery);

    HttpURLConnection con = (HttpURLConnection) obj.openConnection();

    con.setRequestProperty("Authorization", "Bearer " + m_appengineToken);

    con.setRequestProperty("Content-Type", "application/json");
    con.setRequestMethod("GET");

    int responseCode = con.getResponseCode();

    System.out.println("GET Response Code :: " + responseCode);
    if (responseCode == HttpURLConnection.HTTP_OK) {
      BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
      String inputLine;
      StringBuffer response = new StringBuffer();

      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
      in.close();

      return new JSONObject(response.toString());

    } else {
      System.out.println("GET request did not work.");
    }

    return new JSONObject(0);
  }

  public void postServerInterface(String interfaceName, String endpoint, Object payload)
      throws IOException {
    String httpQuery =
        GenericMockDevice.getApiUrl()
            + "/appengine/v1/"
            + GenericMockDevice.getRealm()
            + "/devices/"
            + GenericMockDevice.getDeviceId()
            + "/interfaces/"
            + interfaceName
            + endpoint;

    URL obj = new URL(httpQuery);

    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
    con.setRequestProperty("Authorization", "Bearer " + m_appengineToken);
    con.setRequestProperty("Content-Type", "application/json");
    con.setRequestMethod("POST");

    con.setDoOutput(true);

    JSONObject request = new JSONObject();

    request.put("data", payload);

    OutputStream os = con.getOutputStream();
    os.write(request.toString().getBytes("UTF-8"));
    os.close();

    int responseCode = con.getResponseCode();
    String message = con.getResponseMessage();
    System.out.println("POST Response Code :: " + responseCode);

    if (responseCode == 422) {
      System.out.println(payload);
    }

    if (responseCode == HttpURLConnection.HTTP_OK) {
      BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
      String inputLine;
      StringBuffer response = new StringBuffer();

      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
      in.close();

      System.out.println(response.toString());
    } else {
      System.out.println("POST request did not work.");
    }
  }

  public void deleteServerInterfaceAsync(String interfaceName, String endpoint) throws IOException {
    String httpQuery =
        GenericMockDevice.getApiUrl()
            + "/appengine/v1/"
            + GenericMockDevice.getRealm()
            + "/devices/"
            + GenericMockDevice.getDeviceId()
            + "/interfaces/"
            + interfaceName
            + endpoint;

    URL obj = new URL(httpQuery);

    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
    con.setRequestProperty("Authorization", "Bearer " + m_appengineToken);
    con.setRequestProperty("Content-Type", "application/json");
    con.setRequestMethod("DELETE");

    int responseCode = con.getResponseCode();
    System.out.println("DELETE Response Code :: " + responseCode);

    if (responseCode == HttpURLConnection.HTTP_OK) {
      BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
      String inputLine;
      StringBuffer response = new StringBuffer();

      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
      in.close();

      System.out.println(response.toString());
    } else {
      System.out.println("DELETE request did not work.");
    }
  }
}
