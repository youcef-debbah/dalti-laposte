package com.dalti.laposte.core.repositories;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.dalti.laposte.core.entity.Progress;
import com.dalti.laposte.core.entity.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import dz.jsoftware95.queue.api.AlarmInfo;
import dz.jsoftware95.queue.common.GlobalConf;
import dz.jsoftware95.queue.common.IdentityManager;
import dz.jsoftware95.queue.api.IntLongPair;
import dz.jsoftware95.silverbox.android.backend.DataLoader;
import dz.jsoftware95.silverbox.android.common.StringUtil;
import dz.jsoftware95.silverbox.android.observers.ObserversUtil;

@Dao
@WorkerThread
public abstract class ProgressDAO implements DataLoader<LoadedProgress> {

    protected final QueueDatabase database;

    public ProgressDAO(QueueDatabase database) {
        this.database = database;
    }

    @Nullable
    @Query("select * from progress where progress_id = :id")
    public abstract Progress getProgressValue(long id);

    public Progress requireProgressValue(long id) {
        Progress progress = getProgressValue(id);
        return progress != null ? progress : new Progress(id);
    }

    @Query("select * from progress where progress_id in (:ids)")
    protected abstract List<Progress> getProgressValuesIn(Collection<Long> ids);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    protected abstract void update_helper(List<Progress> progresses);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract void insert(Progress progress);

    @Nullable
    @Transaction
    public Set<Long> update(@Nullable Long serviceID, Map<String, String> data) {
        AppConfig.getInstance().setServerSituation(StringUtil.parseLong(data.get(GlobalConf.SITUATION_VERSION)));

        Set<Long> updatedIDs = new HashSet<>(4);
        Service serviceValue = serviceID != null ? getServiceValue(serviceID) : null;
        if (serviceValue != null) {
            Set<Long> toUpdateIDs = StringUtil.parseProgressesIDs(serviceID, data.get(GlobalConf.EXTRA_PROGRESS_RANKS_KEY));

            List<Progress> currentProgresses = getProgressValuesIn(toUpdateIDs);
            for (Progress progress : currentProgresses) {
                boolean updated = progress.update(data);
                if (updated)
                    updatedIDs.add(progress.getId());
                toUpdateIDs.remove(progress.getId());
            }

            for (Long progressID : toUpdateIDs) {
                insert(new Progress(progressID, data));
                updatedIDs.add(progressID);
            }
            update_helper(currentProgresses);

            boolean serviceUpdated = serviceValue.update(data);
            if (serviceUpdated) {
                database.serviceDAO().update(serviceValue);
                return null;
            }
        }
        return updatedIDs;
    }

    @Query("select count(progress_id) from progress where ticket is not null and service_id = :service")
    public abstract int countTickets(long service);

    @Query("update progress set ticket = :ticket, lastTicketUpdate = :now where progress_id = :id")
    public abstract void updateTicket_helper(Integer ticket, long now, long id);

    @Query("select ticket as 'primaryValue', lastTicketUpdate as 'secondaryValue' from progress where progress_id = :id")
    public abstract IntLongPair getTicket(long id);

    @Transaction
    public boolean updateTicket(Integer newTicket, long progressID) {
        IntLongPair currentTicket = getTicket(progressID);
        if (currentTicket == null || !Objects.equals(newTicket, currentTicket.getPrimaryValue())) {
            updateTicket_helper(newTicket, System.currentTimeMillis(), progressID);
            database.stateDAO().incSituationVersion(progressID);
            NotificationUtils.cancelLaunchedAlarms(alarm -> progressID == alarm.getProgressID());
            return true;
        } else
            return false;
    }

    @Query("delete from progress where timestamp is not null and timestamp < :minimum")
    public abstract void cleanOldProgresses(long minimum);

    @Query("delete from progress")
    public abstract void deleteAll();

    public LiveData<Long> getCurrentServiceID() {
        return database.stateDAO().getCurrentServiceID();
    }

    public LiveData<LoadedProgress> load(long progressID) {
        return ObserversUtil.merge(getService(IdentityManager.getServiceID(progressID)),
                getProgress(progressID),
                LoadedProgress::from);
    }

    @Query("select * from progress where progress_id = :progress")
    public abstract LiveData<Progress> getProgress(long progress);

    @Transaction
    public LocalServiceInfo markServiceAsUnknown(long service) {
        LocalServiceInfo localServiceInfo = getServiceInfo(service);
        if (localServiceInfo != null && !localServiceInfo.isUnknown()) {
            database.serviceDAO().updateServiceUnknownState(service, true);
            if (countTickets(service) > 0)
                return localServiceInfo;
        }

        return null;
    }

    @Transaction
    public LocalServiceInfo markServiceAsKnown(long service) {
        LocalServiceInfo localServiceInfo = getServiceInfo(service);
        if (localServiceInfo != null) {
            if (localServiceInfo.isUnknown())
                database.serviceDAO().updateServiceUnknownState(service, false);
            return localServiceInfo;
        }
        return null;
    }

    @Query("select ticket as 'primaryValue', lastTicketUpdate as 'secondaryValue' from progress where service_id = :service order by progress_id")
    public abstract List<IntLongPair> getTickets(long service);

    public LocalServiceInfo getServiceInfo(long service) {
        return database.serviceDAO().getServiceInfo(service);
    }

    public boolean exists(long serviceID) {
        return database.serviceDAO().exists(serviceID);
    }

    public Service getCurrentServiceValue() {
        return database.serviceDAO().getCurrentServiceValue();
    }

    public Long getCurrentServiceIdValue() {
        return database.stateDAO().getCurrentServiceIdValue();
    }

    public Service getServiceValue(long id) {
        return database.serviceDAO().getServiceValue(id);
    }

    public ServiceProgress getServiceProgressValue(long id) {
        return database.serviceDAO().getServiceProgressValue(id);
    }

    public LiveData<Service> getService(long id) {
        return database.serviceDAO().getService(id);
    }

    @Transaction
    @Query("select progress_id as 'progress', max(lastUpdate, lastTicketUpdate) as 'lastUpdate', ticket, enabled, duration, queue, liquidity, phone, vibrate, ringtone, priority, snooze from turn_alarm, progress where ticket not null and enabled")
    public abstract List<AlarmInfo> getAlarmsInfo();
}
