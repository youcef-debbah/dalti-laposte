package com.dalti.laposte.core.repositories;

import androidx.annotation.WorkerThread;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.dalti.laposte.core.entity.Extra;

@Dao
@WorkerThread
public abstract class ExtraDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void put(Extra extra);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract void putIfAbsent(Extra extra);

    @Query("select value from Extra where extra_id = :key")
    public abstract String get(long key);

    @Query("delete from Extra where extra_id = :key")
    public abstract void remove(long key);
}
