package com.dalti.laposte.core.repositories;

import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RoomDatabase;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.Collection;
import java.util.List;

import dz.jsoftware95.silverbox.android.backend.PageableDAO;
import dz.jsoftware95.silverbox.android.common.StringUtil;

@Dao
@WorkerThread
public abstract class ServiceDAO extends PageableDAO<Service> {

    protected final QueueDatabase database;

    public ServiceDAO(QueueDatabase database) {
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
        return new String[]{Service.TABLE_NAME};
    }

    @Override
    @Query("SELECT count(service_id) FROM service")
    public abstract int count();

    @Override
    @AnyThread
    @Query("SELECT * FROM service ORDER BY service_id ASC LIMIT :start, :count")
    public abstract List<Service> getRange(final long start, final long count);

    @MainThread
    @Override
    @Query("SELECT * FROM service where service_id = :id")
    public abstract LiveData<Service> load(long id);

    // core API

    @Query("SELECT * FROM service where service_id = :id")
    public abstract Service getServiceValue(long id);

    @Query("DELETE FROM service")
    public abstract void deleteAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void updateAll(Collection<Service> services);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    public abstract void update(Service service);

    @Query("update service set unknown = :value where service_id = :service")
    public abstract void updateServiceUnknownState(long service, boolean value);

//    @Query("select topic from service where id = " + StateDAO.SELECT_CURRENT_SERVICE_ID)
//    public abstract String getCurrentTopicValue();

    @Transaction
    public void replaceAll(Collection<Service> services, int schema) {
        deleteAll();
        updateAll(services);
        database.extraDAO().put(new Extra(SimpleProperty.LAST_SERVICES_UPDATE_TIME, String.valueOf(System.currentTimeMillis())));
        AppConfig.getInstance().setSchemaVersion(schema);
    }

    public Long getLastServicesUpdate() {
        return StringUtil.parseLong(database.extraDAO().get(SimpleProperty.LAST_SERVICES_UPDATE_TIME.key()));
    }

    public void clearProgresses() {
        database.progressDAO().deleteAll();
    }

    @Transaction
    public void selectService(@Nullable Long serviceID) {
        database.stateDAO().saveState(new StateEntry(StateEntry.CURRENT_SERVICE_ID, serviceID));
    }

    // external use

    @Query("select service_id as 'id', wilaya, extra, availability, descriptionEng, descriptionFre, descriptionArb, unknown from service where service_id = :service")
    public abstract LocalServiceInfo getServiceInfo(long service);

    @Query("select exists(select service_id from service where service_id = :serviceID)")
    public abstract boolean exists(long serviceID);

    @Query("select * from service where service.service_id = " + StateDAO.SELECT_CURRENT_SERVICE_ID)
    public abstract Service getCurrentServiceValue();

    @Transaction
    @Query("SELECT * FROM Service WHERE service_id = :id")
    public abstract ServiceProgress getServiceProgressValue(long id);

    @Query("SELECT * FROM Service WHERE service_id = :id")
    public abstract LiveData<Service> getService(long id);
}
