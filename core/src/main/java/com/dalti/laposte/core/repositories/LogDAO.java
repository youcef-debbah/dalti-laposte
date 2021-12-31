package com.dalti.laposte.core.repositories;

import androidx.annotation.WorkerThread;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.dalti.laposte.core.entity.LoggedEvent;

import java.util.List;

@Dao
@WorkerThread
public abstract class LogDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void log(LoggedEvent log);

    @Query("select * from logged_event order by logged_event_id ASC")
    public abstract List<LoggedEvent> getAll();

    @Delete
    public abstract void deleteAll(List<LoggedEvent> events);
}
