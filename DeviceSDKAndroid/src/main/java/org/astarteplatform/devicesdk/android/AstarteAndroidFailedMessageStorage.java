package org.astarteplatform.devicesdk.android;

import java.util.ArrayDeque;
import java.util.Deque;
import org.astarteplatform.devicesdk.transport.AstarteFailedMessage;
import org.astarteplatform.devicesdk.transport.AstarteFailedMessageStorage;
import org.astarteplatform.devicesdk.transport.AstarteTransportException;

public class AstarteAndroidFailedMessageStorage implements AstarteFailedMessageStorage {
  private Deque<AstarteAndroidFailedMessage> mFailedMessageQueue;
  private AstarteAndroidFailedMessageDao mFailedMessageDao;

  public AstarteAndroidFailedMessageStorage(AstarteAndroidFailedMessageDao failedMessageDao) {
    mFailedMessageDao = failedMessageDao;
    // Load messages from the storage
    mFailedMessageQueue = new ArrayDeque<>(mFailedMessageDao.getAll());
  }

  @Override
  public void insertVolatile(String topic, byte[] payload, int qos) {
    AstarteAndroidFailedMessage m = new AstarteAndroidFailedMessage(topic, payload, qos);
    mFailedMessageQueue.push(m);
  }

  @Override
  public void insertVolatile(String topic, byte[] payload, int qos, int relativeExpiry) {
    AstarteAndroidFailedMessage m =
        new AstarteAndroidFailedMessage(topic, payload, qos, relativeExpiry);
    mFailedMessageQueue.push(m);
  }

  @Override
  public void insertStored(String topic, byte[] payload, int qos) throws AstarteTransportException {
    AstarteAndroidFailedMessage m = new AstarteAndroidFailedMessage(topic, payload, qos);
    try {
      storeAndPushMessage(m);
    } catch (Exception e) {
      throw new AstarteTransportException("Cannot store failed message", e);
    }
  }

  @Override
  public void insertStored(String topic, byte[] payload, int qos, int relativeExpiry)
      throws AstarteTransportException {
    AstarteAndroidFailedMessage m =
        new AstarteAndroidFailedMessage(topic, payload, qos, relativeExpiry);
    try {
      storeAndPushMessage(m);
    } catch (Exception e) {
      throw new AstarteTransportException("Cannot store failed message", e);
    }
  }

  private void storeAndPushMessage(AstarteAndroidFailedMessage message) {
    long storageId = mFailedMessageDao.insert(message);
    message.setStorageId(storageId);
    mFailedMessageQueue.push(message);
  }

  @Override
  public boolean isEmpty() {
    return mFailedMessageQueue.isEmpty();
  }

  @Override
  public AstarteFailedMessage peekFirst() {
    return mFailedMessageQueue.peekFirst();
  }

  @Override
  public void ackFirst() {
    AstarteAndroidFailedMessage m = mFailedMessageQueue.pop();
    if (m.getStorageId() > 0) {
      mFailedMessageDao.delete(m);
    }
  }

  @Override
  public void rejectFirst() {
    // This is the same as ackFirst in this implementation, but the two are separate to allow
    // additional actions when a message is removed without it being successfully delivered
    AstarteAndroidFailedMessage m = mFailedMessageQueue.pop();
    if (m.getStorageId() > 0) {
      mFailedMessageDao.delete(m);
    }
  }
}
