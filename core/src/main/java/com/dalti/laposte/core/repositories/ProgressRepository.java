package com.dalti.laposte.core.repositories;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dalti.laposte.core.entity.Progress;
import com.dalti.laposte.core.ui.AbstractQueueApplication;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Lazy;
import dz.jsoftware95.queue.common.GlobalConf;
import dz.jsoftware95.queue.common.GlobalUtil;
import dz.jsoftware95.queue.common.IdentityManager;
import dz.jsoftware95.queue.common.PostOfficeAvailability;
import dz.jsoftware95.silverbox.android.backend.Item;
import dz.jsoftware95.silverbox.android.backend.LoadableRepository;
import dz.jsoftware95.silverbox.android.common.StringUtil;
import dz.jsoftware95.silverbox.android.concurrent.DuoDatabaseJob;
import dz.jsoftware95.silverbox.android.concurrent.Job;
import dz.jsoftware95.silverbox.android.concurrent.UnDatabaseJob;

@Singleton
@AnyThread
public class ProgressRepository extends LoadableRepository<LoadedProgress, LoadedProgress, ProgressDAO> {

    @Inject
    @AnyThread
    public ProgressRepository(Lazy<ProgressDAO> dao) {
        super(dao);
    }

    @Override
    @Nullable
    protected LoadedProgress loadFrom(@Nullable LoadedProgress data) {
        return data;
    }

    public void showUnknownAvailabilityNotificationWork(long serviceID) {
        execute(newShowUnknownAvailabilityNotificationJob(this, serviceID));
    }

    private Job newShowUnknownAvailabilityNotificationJob(ProgressRepository progressRepository, long serviceID) {
        return new UnDatabaseJob<ProgressRepository>(progressRepository) {
            @Override
            protected void doFromBackground(@NonNull @NotNull ProgressRepository repository) {
                NotificationUtils.cancelLaunchedAlarms(alarm -> serviceID == IdentityManager.getServiceID(alarm.getProgressID()));
                AppConfig appConfig = AppConfig.getInstance();
                if (appConfig.getActivationState().isActive()
                        && appConfig.get(BooleanSetting.UNKNOWN_AVAILABILITY_NOTIFICATION)) {
                    LocalServiceInfo info = repository.requireDAO().markServiceAsUnknown(serviceID);
                    if (info != null)
                        NotificationUtils.startUnknownAvailabilityAlert(info.getId(), info.getDescription());
                }
            }
        };
    }

    public void hideUnknownAvailabilityNotificationWork(long serviceID, Map<String, String> data) {
        execute(hideUnknownAvailabilityNotificationJob(this, data, serviceID));
    }

    private Job hideUnknownAvailabilityNotificationJob(ProgressRepository progressRepository, Map<String, String> data, long serviceID) {
        return new DuoDatabaseJob<ProgressRepository, Map<String, String>>(progressRepository, data) {
            @Override
            protected void doFromBackground(@NotNull ProgressRepository repository,
                                            @NotNull Map<String, String> data) {
                NotificationUtils.handleTurnAlarmNotification(data);
                LocalServiceInfo info = repository.requireDAO().markServiceAsKnown(serviceID);
                if (info != null)
                    NotificationUtils.cancelUnknownAvailabilityAlert(info.getId());
            }
        };
    }

    public void clearOldLaunchedTurnAlarms(UUID workID) throws InterruptedException {
        executeAndWait(newClearTurnAlarmsNotifications(this, workID));
    }

    @NotNull
    public static UnDatabaseJob<ProgressRepository> newClearTurnAlarmsNotifications(final @NonNull ProgressRepository progressRepository, UUID workID) {
        return new UnDatabaseJob<ProgressRepository>(progressRepository) {
            @Override
            protected void doFromBackground(@NonNull ProgressRepository repository) {
                AppConfig appConfig = AppConfig.getInstance();
                Set<String> data = appConfig.get(SetSetting.LAUNCHED_TURN_ALARMS);
                if (GlobalUtil.notEmpty(data)) {
                    ProgressDAO progressDAO = repository.requireDAO();
                    HashSet<String> canceledAlarms = new HashSet<>(2);
                    long now = System.currentTimeMillis();
                    for (String alarmData : data) {
                        LaunchedTurnAlarm launchedTurnAlarm = LaunchedTurnAlarm.decode(alarmData);
                        if (launchedTurnAlarm != null) {
                            Long remainingTime = Progress.getRemainingTime(progressDAO.getProgressValue(launchedTurnAlarm.getProgressID()));
                            if ((now - launchedTurnAlarm.getLaunchTime() > NotificationUtils.ALARM_NOTIFICATION_TTL)
                                    && (remainingTime == null || remainingTime > launchedTurnAlarm.getAlarmTargetTime())) {
                                NotificationUtils.cancelTurnAlarmNotification(launchedTurnAlarm.getProgressID());
                                canceledAlarms.add(launchedTurnAlarm.toString());
                            }
                        }
                    }
                    appConfig.remove(SetSetting.LAUNCHED_TURN_ALARMS, canceledAlarms);
                } else
                    AbstractQueueApplication.getWorkManager().cancelWorkById(workID);
            }
        };
    }

    public void handleClientNotifications(long serviceID, @Nullable Map<String, String> data) {
        Integer availability = StringUtil.parseInteger(StringUtil.getString(data, GlobalConf.AVAILABILITY_KEY));
        if (availability != null) {
            if (PostOfficeAvailability.isKnown(availability)) {
                hideUnknownAvailabilityNotificationWork(serviceID, data);
            } else {
                showUnknownAvailabilityNotificationWork(serviceID);
            }
        }
    }

    public void showIdleAdminWarning(Long serviceID) {
        if (serviceID != null && serviceID > Item.AUTO_ID)
            execute(newIdleStaleAdminWarning(this, serviceID));
    }

    private static Job newIdleStaleAdminWarning(ProgressRepository repository, Long serviceID) {
        return new UnDatabaseJob<ProgressRepository>(repository) {
            @Override
            protected void doFromBackground(@NonNull ProgressRepository repository) {
                LocalServiceInfo info = repository.requireDAO().getServiceInfo(serviceID);
                if (info != null)
                    NotificationUtils.startIdleAdminNotification(info.getId(), info.getDescription());
            }
        };
    }

    public void hideIdleAdminWarning(Long serviceID) {
        if (serviceID != null)
            execute(newHideIdleAdminWarning(this, serviceID));
    }

    private static Job newHideIdleAdminWarning(ProgressRepository repository, Long serviceID) {
        return new UnDatabaseJob<ProgressRepository>(repository) {
            @Override
            protected void doFromBackground(@NonNull ProgressRepository repository) {
                NotificationUtils.cancelIdleAdminAlert(serviceID);
            }
        };
    }
}
