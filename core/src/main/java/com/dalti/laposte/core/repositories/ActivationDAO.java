package com.dalti.laposte.core.repositories;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.Collection;
import java.util.List;

import dz.jsoftware95.silverbox.android.backend.PageableDAO;
import dz.jsoftware95.silverbox.android.common.StringUtil;

@Dao
@WorkerThread
public abstract class ActivationDAO extends PageableDAO<Activation> {

    protected final QueueDatabase database;

    public ActivationDAO(QueueDatabase database) {
        this.database = database;
    }

    @Override
    public QueueDatabase getDatabase() {
        return database;
    }

    /**
     * Returns the list of tables and views to observe for changes.
     */
    @NonNull
    @Override
    @AnyThread
    public String[] getTables() {
        return new String[]{Activation.TABLE_NAME};
    }

    @Override
    @Query("SELECT count(code) FROM activation")
    public abstract int count();

    @Override
    @AnyThread
    @Query("SELECT * FROM activation ORDER BY activation_id ASC LIMIT :start, :count")
    public abstract List<Activation> getRange(final long start, final long count);

    @Query("DELETE FROM activation")
    public abstract void deleteAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void updateAll(Collection<Activation> activations);

    @Transaction
    public void replaceAll(Collection<Activation> activations) {
        deleteAll();
        updateAll(activations);
        database.extraDAO().put(new Extra(SimpleProperty.LAST_ACTIVATIONS_UPDATE_TIME.key(), String.valueOf(System.currentTimeMillis())));
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void update(Activation activation);

    public Long getLastActivationsUpdate() {
        return StringUtil.parseLong(database.extraDAO().get(SimpleProperty.LAST_ACTIVATIONS_UPDATE_TIME.key()));
    }

    @Query("SELECT * FROM activation WHERE activation_id = :id")
    public abstract LiveData<Activation> load(long id);
}
