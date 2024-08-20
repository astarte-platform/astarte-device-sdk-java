package org.astarteplatform.devicesdk.android;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.concurrent.TimeUnit;
import org.astarteplatform.devicesdk.transport.AstarteFailedMessage;

@Entity(tableName = "failed_messages")
public class AstarteAndroidFailedMessage implements AstarteFailedMessage {
  @PrimaryKey(autoGenerate = true)
  @ColumnInfo(name = "id")
  private long storageId;

  @ColumnInfo(name = "absolute_expiry")
  @NonNull
  protected long absoluteExpiry;

  @NonNull private String topic;

  @NonNull private byte[] payload;

  @NonNull private int qos;

  public AstarteAndroidFailedMessage(String topic, byte[] payload, int qos) {
    this.absoluteExpiry = 0;
    this.topic = topic;
    this.payload = payload;
    this.qos = qos;
  }

  public AstarteAndroidFailedMessage(String topic, byte[] payload, int qos, int relativeExpiry) {
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

  public long getAbsoluteExpiry() {
    return absoluteExpiry;
  }

  public void setAbsoluteExpiry(long absoluteExpiry) {
    this.absoluteExpiry = absoluteExpiry;
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

  public void setStorageId(long id) {
    this.storageId = id;
  }
}
