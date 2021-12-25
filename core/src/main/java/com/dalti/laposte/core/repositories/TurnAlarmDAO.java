package com.dalti.laposte.core.repositories;

import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RoomDatabase;
import androidx.room.Transaction;

import java.util.List;

import dz.jsoftware95.queue.response.ResponseConfig;
import dz.jsoftware95.silverbox.android.middleware.ContextUtils;

@Dao
@WorkerThread
public abstract class TurnAlarmDAO {

    private static final int MAX_ALARMS_COUNT = 5;
    protected final QueueDatabase database;

    public TurnAlarmDAO(QueueDatabase database) {
        this.database = database;
    }

    public RoomDatabase getDatabase() {
        return database;
    }

    @Query("select * from turn_alarm order by creationTime desc")
    public abstract LiveData<List<TurnAlarm>> getTurnAlarmValues();

    @Query("select * from turn_alarm where turn_alarm_id = :id")
    public abstract TurnAlarm getTurnAlarmValue(long id);

    // cause situation version to increment
    @Query("update turn_alarm set priority = :value, lastUpdate = :now where turn_alarm_id = :id and priority <> :value")
    public abstract int updatePriority_helper(long id, int value, long now);

    public int updatePriority(long id, int value) {
        int result = updatePriority_helper(id, value, System.currentTimeMillis());
        if (result > 0) {
            ContextUtils.AUTO_INFO_REFRESH_CACHE.clear();
            database.stateDAO().incSituationVersion();
        }
        return result;
    }

    @Query("update turn_alarm set ringtone = :value, lastUpdate = :now where turn_alarm_id = :id and ringtone <> :value")
    public abstract int updateRingtone_helper(long id, int value, long now);

    public int updateRingtone(long id, int value) {
        int result = updateRingtone_helper(id, value, System.currentTimeMillis());
        if (result > 0) {
            ContextUtils.AUTO_INFO_REFRESH_CACHE.clear();
            database.stateDAO().incSituationVersion();
        }
        return result;
    }

    @Query("update turn_alarm set snooze = :value, lastUpdate = :now where turn_alarm_id = :id and snooze <> :value")
    public abstract int updateSnooze_helper(long id, int value, long now);

    public int updateSnooze(long id, int value) {
        int result = updateSnooze_helper(id, value, System.currentTimeMillis());
        if (result > 0) {
            ContextUtils.AUTO_INFO_REFRESH_CACHE.clear();
            database.stateDAO().incSituationVersion();
        }
        return result;
    }

    @Query("update turn_alarm set vibrate = :value, lastUpdate = :now where turn_alarm_id = :id and vibrate <> :value")
    public abstract int updateVibrate_helper(long id, boolean value, long now);

    public int updateVibrate(long id, boolean value) {
        int result = updateVibrate_helper(id, value, System.currentTimeMillis());
        if (result > 0) {
            ContextUtils.AUTO_INFO_REFRESH_CACHE.clear();
            database.stateDAO().incSituationVersion();
        }
        return result;
    }

    @Query("update turn_alarm set enabled = :value, lastUpdate = :now where turn_alarm_id = :id and enabled <> :value")
    public abstract int updateEnabled_helper(long id, boolean value, long now);

    @Transaction
    public int updateEnabled(long id, boolean value) {
        int result = updateEnabled_helper(id, value, System.currentTimeMillis());
        if (result > 0) {
            ContextUtils.AUTO_INFO_REFRESH_CACHE.clear();
            database.stateDAO().incSituationVersion();
        }
        return result;
    }

    @Query("update turn_alarm set duration = :value, lastUpdate = :now where turn_alarm_id = :id and duration <> :value")
    public abstract int updateBeforehandDuration_helper(long id, long value, long now);

    @Transaction
    public int updateBeforehandDuration(long id, long value) {
        int result = updateBeforehandDuration_helper(id, value, System.currentTimeMillis());
        if (result > 0) {
            ContextUtils.AUTO_INFO_REFRESH_CACHE.clear();
            database.stateDAO().incSituationVersion();
        }
        return result;
    }

    @Query("update turn_alarm set queue = :value, lastUpdate = :now where turn_alarm_id = :id and queue <> :value")
    public abstract int updateMaxQueueLength_helper(long id, int value, long now);

    @Transaction
    public int updateMaxQueueLength(long id, int value) {
        int result = updateMaxQueueLength_helper(id, value, System.currentTimeMillis());
        if (result > 0) {
            ContextUtils.AUTO_INFO_REFRESH_CACHE.clear();
            database.stateDAO().incSituationVersion();
        }
        return result;
    }

    @Query("update turn_alarm set liquidity = :value, lastUpdate = :now where turn_alarm_id = :id and liquidity <> :value")
    public abstract int updateMinLiquidity_helper(long id, int value, long now);

    @Transaction
    public int updateMinLiquidity(long id, int value) {
        int result = updateMinLiquidity_helper(id, value, System.currentTimeMillis());
        if (result > 0) {
            ContextUtils.AUTO_INFO_REFRESH_CACHE.clear();
            database.stateDAO().incSituationVersion();
        }
        return result;
    }

    @Query("update turn_alarm set phone = :value, lastUpdate = :now where turn_alarm_id = :id and (phone is null or phone <> :value)")
    public abstract int updatePhone_helper(long id, String value, long now);

    @Transaction
    public int updatePhone(long id, String value) {
        int result = updatePhone_helper(id, value, System.currentTimeMillis());
        if (result > 0) {
            ContextUtils.AUTO_INFO_REFRESH_CACHE.clear();
            database.stateDAO().incSituationVersion();
        }
        return result;
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void save_helper(TurnAlarm turnAlarm);

    @Query("select count(turn_alarm_id) from turn_alarm")
    public abstract int alarmsCount();

    @Query("select count(turn_alarm_id) from turn_alarm where duration == :targetDuration")
    public abstract int countSimilarAlarms(long targetDuration);

    @Query("select count(turn_alarm_id) from turn_alarm")
    public abstract int countAlarms();

    @Transaction
    public String save(TurnAlarm turnAlarm) {
        int count = countSimilarAlarms(turnAlarm.getBeforehandDuration());
        if (count == 0) {
            if (countAlarms() >= MAX_ALARMS_COUNT)
                return ResponseConfig.UPDATE_ERROR;
            save_helper(turnAlarm);
            ContextUtils.AUTO_INFO_REFRESH_CACHE.clear();
            database.stateDAO().incSituationVersion();
            return ResponseConfig.ENTRY_SAVED;
        } else
            return ResponseConfig.ENTRY_EXIST;
    }

    @Query("delete from turn_alarm where turn_alarm_id = :id")
    public abstract int delete_helper(long id);

    @Transaction
    public int delete(long id) {
        AppConfig.getInstance().removeCommit(new AlarmPhonePreference(id));
        int result = delete_helper(id);
        if (result > 0) {
            ContextUtils.AUTO_INFO_REFRESH_CACHE.clear();
            database.stateDAO().incSituationVersion();
        }
        return result;
    }
}
