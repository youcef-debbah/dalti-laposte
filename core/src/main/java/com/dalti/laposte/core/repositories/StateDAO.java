package com.dalti.laposte.core.repositories;

import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import dz.jsoftware95.queue.common.IdentityManager;
import dz.jsoftware95.silverbox.android.middleware.ContextUtils;


@Dao
@WorkerThread
public abstract class StateDAO {

    protected static final String SELECT_CURRENT_SERVICE_ID = "(select value from state where state_id = " + StateEntry.CURRENT_SERVICE_ID + ")";

    protected final QueueDatabase database;

    public StateDAO(QueueDatabase database) {
        this.database = database;
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void saveState(StateEntry stateEntry);

    @Query("select value from state where state_id = " + StateEntry.CURRENT_SERVICE_ID)
    public abstract LiveData<Long> getCurrentServiceID();

    @Query("select value from state where state_id = " + StateEntry.CURRENT_SERVICE_ID)
    public abstract Long getCurrentServiceIdValue();

    @Query("select value from state where state_id = " + StateEntry.CURRENT_SITUATION_VERSION)
    public abstract Long getSituationVersion();

    public long getCurrentSituationVersion() {
        Long version = getSituationVersion();
        return version != null ? version : AppConfig.DEFAULT_CLIENT_SITUATION;
    }

    @Transaction
    public long incSituationVersion(long id) {
        ContextUtils.AUTO_INFO_REFRESH_CACHE.remove(IdentityManager.getServiceID(id));
        return incSituationVersion();
    }

    @Transaction
    public long incSituationVersion() {
        long version = getCurrentSituationVersion() + 1;
        saveState(new StateEntry(StateEntry.CURRENT_SITUATION_VERSION, version));
        AppConfig.getInstance().setClientSituation(version);
        return version;
    }

    @Transaction
    public void invalidateCache() {
        ContextUtils.AUTO_INFO_REFRESH_CACHE.clear();
        incSituationVersion();
        AppConfig.getInstance().setSchemaVersion(AppConfig.NULL_SCHEMA);

        ExtraDAO extraDAO = database.extraDAO();
        extraDAO.remove(SimpleProperty.LAST_ADMIN_ALARM_UPDATE_TIME.key());
        extraDAO.remove(SimpleProperty.LAST_ACTIVATIONS_UPDATE_TIME.key());
        extraDAO.remove(SimpleProperty.LAST_SERVICES_UPDATE_TIME.key());

        database.adminAlarmDAO().deleteAll();
        database.activationDAO().deleteAll();
    }

}
