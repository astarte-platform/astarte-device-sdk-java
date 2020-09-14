package org.astarteplatform.devicesdk.android;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
    entities = {AstarteAndroidFailedMessage.class},
    version = 1)
public abstract class AstarteAndroidRoomDatabase extends RoomDatabase {

  public abstract AstarteAndroidFailedMessageDao astarteFailedMessageDao();

  private static volatile AstarteAndroidRoomDatabase INSTANCE;
  private static final int NUMBER_OF_THREADS = 4;
  static final ExecutorService databaseWriteExecutor =
      Executors.newFixedThreadPool(NUMBER_OF_THREADS);

  static AstarteAndroidRoomDatabase getDatabase(final Context context) {
    if (INSTANCE == null) {
      synchronized (AstarteAndroidRoomDatabase.class) {
        if (INSTANCE == null) {
          INSTANCE =
              Room.databaseBuilder(
                      context.getApplicationContext(),
                      AstarteAndroidRoomDatabase.class,
                      "astarte_sdk_database")
                  .build();
        }
      }
    }
    return INSTANCE;
  }
}
