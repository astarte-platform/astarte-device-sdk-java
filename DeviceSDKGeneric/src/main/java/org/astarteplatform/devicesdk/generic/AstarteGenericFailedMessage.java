package org.astarteplatform.devicesdk.generic;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.util.concurrent.TimeUnit;
import org.astarteplatform.devicesdk.transport.AstarteFailedMessage;

@DatabaseTable(tableName = "failed_messages")
public class AstarteGenericFailedMessage implements AstarteFailedMessage {
  @DatabaseField(generatedId = true, canBeNull = false)
  private long storageId;

  @DatabaseField(canBeNull = false)
  protected long absoluteExpiry;

  @DatabaseField(canBeNull = false)
  private String topic;

  @DatabaseField(canBeNull = false, dataType = DataType.BYTE_ARRAY)
  private byte[] payload;

  @DatabaseField(canBeNull = false)
  private int qos;

  AstarteGenericFailedMessage() {
    // Needed by ORMLite
  }

  public AstarteGenericFailedMessage(String topic, byte[] payload, int qos) {
    this.absoluteExpiry = 0;
    this.topic = topic;
    this.payload = payload;
    this.qos = qos;
  }

  public AstarteGenericFailedMessage(String topic, byte[] payload, int qos, int relativeExpiry) {
    this.absoluteExpiry = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(relativeExpiry);
    this.topic = topic;
    this.payload = payload;
    this.qos = qos;
  }

  @Override
  public String getTopic() {
    return topic;
  }

  @Override
  public byte[] getPayload() {
    return payload;
  }

  @Override
  public int getQos() {
    return qos;
  }

  @Override
  public boolean isExpired() {
    if (absoluteExpiry <= 0) {
      return false;
    }

    return absoluteExpiry < System.currentTimeMillis();
  }

  public long getStorageId() {
    return storageId;
  }
}
