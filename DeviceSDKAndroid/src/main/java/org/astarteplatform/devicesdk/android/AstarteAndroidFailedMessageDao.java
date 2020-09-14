package org.astarteplatform.devicesdk.android;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface AstarteAndroidFailedMessageDao {
  @Insert
  long insert(AstarteAndroidFailedMessage message);

  @Delete
  void delete(AstarteAndroidFailedMessage message);

  @Query("SELECT * from failed_messages ORDER BY id ASC")
  List<AstarteAndroidFailedMessage> getAll();
}
