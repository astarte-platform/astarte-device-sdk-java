package org.astarteplatform.devicesdk.generic;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.table.TableUtils;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Deque;
import org.astarteplatform.devicesdk.transport.AstarteFailedMessage;
import org.astarteplatform.devicesdk.transport.AstarteFailedMessageStorage;
import org.astarteplatform.devicesdk.transport.AstarteTransportException;

public class AstarteGenericFailedMessageStorage implements AstarteFailedMessageStorage {
  private Deque<AstarteGenericFailedMessage> mFailedMessageQueue;
  private Dao<AstarteGenericFailedMessage, Long> mFailedMessageDao;

  public AstarteGenericFailedMessageStorage(Dao<AstarteGenericFailedMessage, Long> failedMessageDao)
      throws SQLException {
    TableUtils.createTableIfNotExists(
        failedMessageDao.getConnectionSource(), AstarteGenericFailedMessage.class);
    mFailedMessageDao = failedMessageDao;
    mFailedMessageQueue =
        new ArrayDeque<>(
            mFailedMessageDao.query(
                mFailedMessageDao.queryBuilder().orderBy("storageId", true).prepare()));
  }

  @Override
  public void insertVolatile(String topic, byte[] payload, int qos) {
    AstarteGenericFailedMessage m = new AstarteGenericFailedMessage(topic, payload, qos);
    mFailedMessageQueue.add(m);
  }

  @Override
  public void insertVolatile(String topic, byte[] payload, int qos, int relativeExpiry) {
    AstarteGenericFailedMessage m =
        new AstarteGenericFailedMessage(topic, payload, qos, relativeExpiry);
    mFailedMessageQueue.add(m);
  }

  @Override
  public void insertStored(String topic, byte[] payload, int qos) throws AstarteTransportException {
    AstarteGenericFailedMessage m = new AstarteGenericFailedMessage(topic, payload, qos);
    try {
      storeAndAddMessage(m);
    } catch (SQLException e) {
      throw new AstarteTransportException("Cannot store failed message", e);
    }
  }

  @Override
  public void insertStored(String topic, byte[] payload, int qos, int relativeExpiry)
      throws AstarteTransportException {
    AstarteGenericFailedMessage m =
        new AstarteGenericFailedMessage(topic, payload, qos, relativeExpiry);
    try {
      storeAndAddMessage(m);
    } catch (SQLException e) {
      throw new AstarteTransportException("Cannot store failed message", e);
    }
  }

  private void storeAndAddMessage(AstarteGenericFailedMessage message) throws SQLException {
    mFailedMessageDao.create(message);
    mFailedMessageQueue.add(message);
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
    AstarteGenericFailedMessage m = mFailedMessageQueue.pop();
    if (m.getStorageId() > 0) {
      try {
        mFailedMessageDao.delete(m);
      } catch (SQLException e) {
        // TODO: handle failed deletion
        e.printStackTrace();
      }
    }
  }

  @Override
  public void rejectFirst() {
    // This is the same as ackFirst in this implementation, but the two are separate to allow
    // additional actions when a message is removed without it being successfully delivered
    AstarteGenericFailedMessage m = mFailedMessageQueue.pop();
    if (m.getStorageId() > 0) {
      try {
        mFailedMessageDao.delete(m);
      } catch (SQLException e) {
        // TODO: handle failed deletion
        e.printStackTrace();
      }
    }
  }
}
