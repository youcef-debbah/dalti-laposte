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
public abstract class AdminAlarmDAO extends PageableDAO<AdminAlarm> {

    protected final QueueDatabase database;

    public AdminAlarmDAO(QueueDatabase database) {
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
        return new String[]{AdminAlarm.TABLE_NAME};
    }

    @Query("SELECT * FROM admin_alarm WHERE admin_alarm_id = :id")
    public abstract LiveData<AdminAlarm> load(long id);

    @Override
    @Query("SELECT count(admin_alarm_id) FROM admin_alarm")
    public abstract int count();

    @Override
    @AnyThread
    @Query("SELECT * FROM admin_alarm ORDER BY admin_alarm_id ASC LIMIT :start, :count")
    public abstract List<AdminAlarm> getRange(final long start, final long count);

    @Query("DELETE FROM admin_alarm")
    public abstract void deleteAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void saveAll(Collection<AdminAlarm> alarms);

    @Transaction
    public void replaceAll(List<AdminAlarm> newAlarms) {
        deleteAll();
        saveAll(newAlarms);
        database.extraDAO().put(new Extra(SimpleProperty.LAST_ADMIN_ALARM_UPDATE_TIME.key(), String.valueOf(System.currentTimeMillis())));
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void update(AdminAlarm alarm);

    public Long getLastAdminAlarmsUpdate() {
        return StringUtil.parseLong(database.extraDAO().get(SimpleProperty.LAST_ADMIN_ALARM_UPDATE_TIME.key()));
    }

    @Transaction
    @Query("SELECT * FROM admin_alarm WHERE admin_alarm_id = :id")
    public abstract AdminAlarm getAdminAlarmValue(Long id);
}
