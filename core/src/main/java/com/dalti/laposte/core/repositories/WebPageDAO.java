package com.dalti.laposte.core.repositories;

import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
@WorkerThread
public abstract class WebPageDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void save(WebPage page);

    @Query("select * from web_page where web_page_name = :name")
    public abstract LiveData<WebPage> load(String name);
}
