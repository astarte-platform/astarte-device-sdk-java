package org.astarteplatform.devicesdk.util;

import org.joda.time.DateTime;

public class DecodedMessage {
  private Object payload;
  private DateTime timestamp;

  public Object getPayload() {
    return payload;
  }

  public void setPayload(Object payload) {
    this.payload = payload;
  }

  public DateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(DateTime timestamp) {
    this.timestamp = timestamp;
  }
}
