package com.dalti.laposte.client.repository;

import android.os.Bundle;

import androidx.annotation.AnyThread;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.dalti.laposte.R;
import com.dalti.laposte.core.repositories.AppConfig;
import com.dalti.laposte.core.repositories.BooleanSetting;
import com.dalti.laposte.core.repositories.Event;
import com.dalti.laposte.core.repositories.Teller;
import com.dalti.laposte.core.repositories.TurnAlarm;
import com.dalti.laposte.core.repositories.TurnAlarmDAO;
import com.dalti.laposte.core.util.QueueUtils;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Lazy;
import dz.jsoftware95.queue.common.BiFunction;
import dz.jsoftware95.queue.response.ResponseConfig;
import dz.jsoftware95.silverbox.android.backend.DataEvent;
import dz.jsoftware95.silverbox.android.backend.Item;
import dz.jsoftware95.silverbox.android.backend.LazyRepository;
import dz.jsoftware95.silverbox.android.concurrent.DuoDatabaseJob;
import dz.jsoftware95.silverbox.android.concurrent.Job;
import dz.jsoftware95.silverbox.android.concurrent.UnDatabaseJob;
import dz.jsoftware95.silverbox.android.observers.AddLiveDataSourceJob;

@Singleton
@AnyThread
public class TurnAlarmRepository extends LazyRepository<TurnAlarmDAO> {

    private static final BiFunction<String, Integer, Integer> INC_INTEGER = (k, v) -> v == null ? 1 : v + 1;
    private static final BiFunction<String, Integer, Integer> DEC_INTEGER = (k, v) -> (v == null || v < 2) ? null : v - 1;

    private final MediatorLiveData<List<TurnAlarm>> turnAlarms;

    private final Map<Long, Map<String, Integer>> currentAlarmUpdates = new HashMap<>(2);

    @Inject
    @AnyThread
    public TurnAlarmRepository(Lazy<TurnAlarmDAO> daoSupplier) {
        super(daoSupplier);
        this.turnAlarms = new MediatorLiveData<>();
        this.turnAlarms.postValue(null);
    }

    @Override
    protected void ontInitialize() {
        new AddLiveDataSourceJob<>(requireDAO().getTurnAlarmValues(), turnAlarms).execute();
        if (AppConfig.getInstance().getAndSet(BooleanSetting.INIT_TURN_ALARMS, false)
                && requireDAO().alarmsCount() == 0)
            addAlarm(30);
    }

    public LiveData<List<TurnAlarm>> getTurnAlarms() {
        initializeIfNeeded();
        return turnAlarms;
    }

    public void addAlarm(Integer minutes) {
        if (minutes != null)
            execute(newAddJob(this, minutes));
    }

    private static Job newAddJob(TurnAlarmRepository repository, int minutes) {
        return new UnDatabaseJob<TurnAlarmRepository>(repository) {
            @Override
            protected void doFromBackground(@NotNull TurnAlarmRepository repository) {
                final TurnAlarm turnAlarm = new TurnAlarm(minutes);
                String result = repository.requireDAO().save(turnAlarm);
                if (Objects.equals(result, ResponseConfig.ENTRY_EXIST)) {
                    QueueUtils.toast(R.string.similar_alarm_exist);
                    Teller.log(Event.NewTurnAlarmExist.NAME);
                } else if (Objects.equals(result, ResponseConfig.UPDATE_ERROR)) {
                    QueueUtils.toast(R.string.too_many_alarms);
                    Teller.log(Event.NewTurnAlarmLimited.NAME);
                } else
                    Teller.log(Event.NewTurnAlarm.NAME, Event.NewTurnAlarm.Param.NEW_VALUE, TimeUnit.MILLISECONDS.toMinutes(turnAlarm.getBeforehandDuration()));
            }
        };
    }

    public void deleteAlarm(long id) {
        execute(newDeleteJob(this, id));
    }

    private static Job newDeleteJob(TurnAlarmRepository repository, long id) {
        return new UnDatabaseJob<TurnAlarmRepository>(repository) {
            @Override
            protected void doFromBackground(@NonNull TurnAlarmRepository repository) {
                if (repository.requireDAO().delete(id) != 0)
                    Teller.log(Event.DeleteTurnAlarmPhone.NAME, Event.DeleteTurnAlarmPhone.Param.TURN_ALARM_ID, String.valueOf(id));
            }
        };
    }

    private synchronized void markUpdateStarted(long id, String property) {
        Map<String, Integer> alarmProperties = currentAlarmUpdates.get(id);
        if (alarmProperties == null)
            currentAlarmUpdates.put(id, alarmProperties = new HashMap<>(2));

        alarmProperties.put(property, INC_INTEGER.apply(property, alarmProperties.get(property)));
    }

    private synchronized void markUpdateEnd(long id, String property) {
        Map<String, Integer> alarmProperties = currentAlarmUpdates.get(id);
        if (alarmProperties != null) {
            Integer currentUpdatesCount = DEC_INTEGER.apply(property, alarmProperties.get(property));
            if (currentUpdatesCount != null)
                alarmProperties.put(property, currentUpdatesCount);
            else {
                alarmProperties.remove(property);
                if (alarmProperties.isEmpty())
                    currentAlarmUpdates.remove(id);
            }
        }
    }

    public synchronized boolean hasPendingUpdate(long id) {
        return currentAlarmUpdates.containsKey(id);
    }

    public synchronized boolean isActive(Long id, String property) {
        if (id == null || property == null)
            return false;
        else {
            Integer updates = getCurrentUpdatesCount(id, property);
            return updates == null || updates < 1;
        }
    }

    private Integer getCurrentUpdatesCount(Long id, String property) {
        Map<String, Integer> alarmProperties = currentAlarmUpdates.get(id);
        if (alarmProperties != null)
            return alarmProperties.get(property);
        return null;
    }

    public void updateEnabled(long id, boolean value) {
        execute(newUpdateEnabledJob(this, id, value));
    }

    private static Job newUpdateEnabledJob(TurnAlarmRepository repository, long id, boolean value) {
        return new AlarmUpdate(repository, id, TurnAlarm.ENABLED) {
            @Override
            protected void doFromBackground(@NonNull TurnAlarmRepository repository,
                                            @NonNull Long id) {
                super.doFromBackground(repository, id);
                if (repository.requireDAO().updateEnabled(id, value) == 0)
                    repository.postPublish(DataEvent.SUCCESSFUL_UPDATE);
                else {
                    Bundle params = new Bundle();
                    params.putString(Event.UpdateTurnAlarmEnabled.Param.TURN_ALARM_ID, String.valueOf(id));
                    params.putString(Event.UpdateTurnAlarmEnabled.Param.NEW_VALUE, String.valueOf(value));
                    Teller.logEvent(Event.UpdateTurnAlarmEnabled.NAME, params);
                }
            }
        };
    }

    public void updateBeforehandDuration(long id, long value) {
        execute(newUpdateBeforehandDurationJob(this, id, value));
    }

    private static Job newUpdateBeforehandDurationJob(TurnAlarmRepository repository, long id, long value) {
        return new AlarmUpdate(repository, id, TurnAlarm.BEFOREHAND_DURATION) {
            @Override
            protected void doFromBackground(@NonNull TurnAlarmRepository repository,
                                            @NonNull Long id) {
                super.doFromBackground(repository, id);
                if (repository.requireDAO().updateBeforehandDuration(id, value) == 0)
                    repository.postPublish(DataEvent.SUCCESSFUL_UPDATE);
                else {
                    Bundle params = new Bundle();
                    params.putString(Event.UpdateTurnAlarmDuration.Param.TURN_ALARM_ID, String.valueOf(id));
                    params.putString(Event.UpdateTurnAlarmDuration.Param.NEW_VALUE, String.valueOf(value));
                    Teller.logEvent(Event.UpdateTurnAlarmDuration.NAME, params);
                }
            }
        };
    }

    public void updateMaxQueueLength(long id, int value) {
        execute(newUpdateMaxQueueLengthJob(this, id, value));
    }

    private static Job newUpdateMaxQueueLengthJob(TurnAlarmRepository repository, long id, int value) {
        return new AlarmUpdate(repository, id, TurnAlarm.MAX_QUEUE_LENGTH) {
            @Override
            protected void doFromBackground(@NonNull TurnAlarmRepository repository,
                                            @NonNull Long id) {
                super.doFromBackground(repository, id);
                if (repository.requireDAO().updateMaxQueueLength(id, value) == 0)
                    repository.postPublish(DataEvent.SUCCESSFUL_UPDATE);
                else {
                    Bundle params = new Bundle();
                    params.putString(Event.UpdateTurnAlarmMaxQueueLength.Param.TURN_ALARM_ID, String.valueOf(id));
                    params.putString(Event.UpdateTurnAlarmMaxQueueLength.Param.NEW_VALUE, String.valueOf(value));
                    Teller.logEvent(Event.UpdateTurnAlarmMaxQueueLength.NAME, params);
                }
            }
        };
    }

    public void updateMinLiquidity(long id, int value) {
        execute(newUpdateMinLiquidityJob(this, id, value));
    }

    private static Job newUpdateMinLiquidityJob(TurnAlarmRepository repository, long id, int value) {
        return new AlarmUpdate(repository, id, TurnAlarm.MIN_LIQUIDITY) {
            @Override
            protected void doFromBackground(@NonNull TurnAlarmRepository repository,
                                            @NonNull Long id) {
                super.doFromBackground(repository, id);
                if (repository.requireDAO().updateMinLiquidity(id, value) == 0)
                    repository.postPublish(DataEvent.SUCCESSFUL_UPDATE);
                else {
                    Bundle params = new Bundle();
                    params.putString(Event.UpdateTurnAlarmMinLiquidity.Param.TURN_ALARM_ID, String.valueOf(id));
                    params.putString(Event.UpdateTurnAlarmMinLiquidity.Param.NEW_VALUE, String.valueOf(value));
                    Teller.logEvent(Event.UpdateTurnAlarmMinLiquidity.NAME, params);
                }
            }
        };
    }

    public void updatePriority(long id, int value) {
        execute(newUpdatePriorityJob(this, id, value));
    }

    private static Job newUpdatePriorityJob(TurnAlarmRepository repository, long id, int value) {
        return new AlarmUpdate(repository, id, TurnAlarm.PRIORITY) {
            @Override
            protected void doFromBackground(@NonNull TurnAlarmRepository repository,
                                            @NonNull Long id) {
                super.doFromBackground(repository, id);
                if (repository.requireDAO().updatePriority(id, value) == 0)
                    repository.postPublish(DataEvent.SUCCESSFUL_UPDATE);
                else {
                    Bundle params = new Bundle();
                    params.putString(Event.UpdateTurnAlarmPriority.Param.TURN_ALARM_ID, String.valueOf(id));
                    params.putString(Event.UpdateTurnAlarmPriority.Param.NEW_VALUE, String.valueOf(value));
                    Teller.logEvent(Event.UpdateTurnAlarmPriority.NAME, params);
                }
            }
        };
    }

    public void updateRingtone(long id, int value) {
        execute(newUpdateRingtoneJob(this, id, value));
    }

    private static Job newUpdateRingtoneJob(TurnAlarmRepository repository, long id, int value) {
        return new AlarmUpdate(repository, id, TurnAlarm.RINGTONE) {
            @Override
            protected void doFromBackground(@NonNull TurnAlarmRepository repository,
                                            @NonNull Long id) {
                super.doFromBackground(repository, id);
                if (repository.requireDAO().updateRingtone(id, value) == 0)
                    repository.postPublish(DataEvent.SUCCESSFUL_UPDATE);
                else {
                    Bundle params = new Bundle();
                    params.putString(Event.UpdateTurnAlarmRingtone.Param.TURN_ALARM_ID, String.valueOf(id));
                    params.putString(Event.UpdateTurnAlarmRingtone.Param.NEW_VALUE, String.valueOf(value));
                    Teller.logEvent(Event.UpdateTurnAlarmRingtone.NAME, params);
                }
            }
        };
    }

    public void updateSnooze(long id, int value) {
        execute(newUpdateSnoozeJob(this, id, value));
    }

    private static Job newUpdateSnoozeJob(TurnAlarmRepository repository, long id, int value) {
        return new AlarmUpdate(repository, id, TurnAlarm.SNOOZE) {
            @Override
            protected void doFromBackground(@NonNull TurnAlarmRepository repository,
                                            @NonNull Long id) {
                super.doFromBackground(repository, id);
                if (repository.requireDAO().updateSnooze(id, value) == 0)
                    repository.postPublish(DataEvent.SUCCESSFUL_UPDATE);
                else {
                    Bundle params = new Bundle();
                    params.putString(Event.UpdateTurnAlarmSnooze.Param.TURN_ALARM_ID, String.valueOf(id));
                    params.putString(Event.UpdateTurnAlarmSnooze.Param.NEW_VALUE, String.valueOf(value));
                    Teller.logEvent(Event.UpdateTurnAlarmSnooze.NAME, params);
                }
            }
        };
    }

    public void updateVibrate(long id, boolean value) {
        execute(newUpdateVibrateJob(this, id, value));
    }

    private static Job newUpdateVibrateJob(TurnAlarmRepository repository, long id, boolean value) {
        return new AlarmUpdate(repository, id, TurnAlarm.VIBRATE) {
            @Override
            protected void doFromBackground(@NonNull TurnAlarmRepository repository,
                                            @NonNull Long id) {
                super.doFromBackground(repository, id);
                if (repository.requireDAO().updateVibrate(id, value) == 0)
                    repository.postPublish(DataEvent.SUCCESSFUL_UPDATE);
                else {
                    Bundle params = new Bundle();
                    params.putString(Event.UpdateTurnAlarmVibrate.Param.TURN_ALARM_ID, String.valueOf(id));
                    params.putString(Event.UpdateTurnAlarmVibrate.Param.NEW_VALUE, String.valueOf(value));
                    Teller.logEvent(Event.UpdateTurnAlarmVibrate.NAME, params);
                }
            }
        };
    }

    public void updatePhone(Long id, String value) {
        if (id != null && id > Item.AUTO_ID && value != null)
            execute(newUpdatePhoneJob(this, id, value));
        else
            Teller.logUnexpectedNull(id, value);
    }

    private static Job newUpdatePhoneJob(TurnAlarmRepository repository, long id, String value) {
        return new AlarmUpdate(repository, id, TurnAlarm.PHONE) {
            @Override
            protected void doFromBackground(@NonNull TurnAlarmRepository repository,
                                            @NonNull Long id) {
                super.doFromBackground(repository, id);
                if (repository.requireDAO().updatePhone(id, value) == 0)
                    repository.postPublish(DataEvent.SUCCESSFUL_UPDATE);
                else {
                    Bundle params = new Bundle();
                    params.putString(Event.UpdateTurnAlarmPhone.Param.TURN_ALARM_ID, String.valueOf(id));
                    params.putString(Event.UpdateTurnAlarmPhone.Param.NEW_VALUE, String.valueOf(value));
                    Teller.logEvent(Event.UpdateTurnAlarmPhone.NAME, params);
                }
            }
        };
    }

    private static class AlarmUpdate extends DuoDatabaseJob<TurnAlarmRepository, Long> {

        private final String property;

        protected AlarmUpdate(@NonNull TurnAlarmRepository repository,
                              long entityId,
                              @NonNull String property) {
            super(repository, entityId);
            this.property = Objects.requireNonNull(property);
            repository.markUpdateStarted(entityId, property);
        }

        @Override
        @CallSuper
        protected void doFromBackground(@NonNull TurnAlarmRepository repository,
                                        @NonNull Long entityId) {
            repository.markUpdateEnd(entityId, property);
        }

        @Override
        @CallSuper
        protected void closeFromBackground(@NotNull TurnAlarmRepository repository,
                                           @NonNull Long entityId) {
        }
    }
}
