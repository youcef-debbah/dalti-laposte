package com.dalti.laposte.core.repositories;

import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RoomDatabase;

import com.dalti.laposte.core.entity.ShortMessage;

import java.util.List;

import dz.jsoftware95.silverbox.android.backend.PageableDAO;


@Dao
@WorkerThread
public abstract class ShortMessageDAO extends PageableDAO<ShortMessage> {


    protected final QueueDatabase database;

    public ShortMessageDAO(QueueDatabase database) {
        this.database = database;
    }

    @Override
    public RoomDatabase getDatabase() {
        return database;
    }

    /**
     * Returns the list of tables and views to observe for changes.
     */
    @NonNull
    @Override
    @AnyThread
    public String[] getTables() {
        return new String[]{ShortMessage.TABLE_NAME};
    }

    @Override
    @Query("SELECT count(short_message_id) FROM short_message")
    public abstract int count();

    @Override
    @AnyThread
    @Query("SELECT * FROM short_message ORDER BY creationTime DESC LIMIT :start, :count")
    public abstract List<ShortMessage> getRange(final long start, final long count);

    @MainThread
    @Query("SELECT * FROM short_message where short_message_id = :id")
    @Override
    public abstract LiveData<ShortMessage> load(long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void save(ShortMessage entity);

    @Query("update short_message set confirmationTime = :now, state = :code where short_message_id = :id")
    public abstract void markAsConfirmed(long id, long now, int code);

    @Query("update short_message set deliveryTime = :now where short_message_id = :id")
    public abstract void markAsDelivered(long id, long now);

    @Query("select count(short_message_id) from short_message")
    public abstract int countMessages();

    @Query("select count(short_message_id) from short_message where phone like '+2135%'")
    public abstract int countOoredooMessages();

    @Query("select count(short_message_id) from short_message where phone like '+2136%'")
    public abstract int countMobilisMessages();

    @Query("select count(short_message_id) from short_message where phone like '+2137%'")
    public abstract int countDjezzyMessages();

    @Query("select count(short_message_id) from short_message where state = :code")
    public abstract int countMessages(int code);

    @Query("select count(short_message_id) from short_message where state = :code and phone like '+2135%'")
    public abstract int countOoredooMessages(int code);

    @Query("select count(short_message_id) from short_message where state = :code and phone like '+2136%'")
    public abstract int countMobilisMessages(int code);

    @Query("select count(short_message_id) from short_message where state = :code and phone like '+2137%'")
    public abstract int countDjezzyMessages(int code);
}
