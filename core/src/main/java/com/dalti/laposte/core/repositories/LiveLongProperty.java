package com.dalti.laposte.core.repositories;

import androidx.lifecycle.MutableLiveData;

import org.jetbrains.annotations.NotNull;

import dz.jsoftware95.queue.common.Function;
import dz.jsoftware95.silverbox.android.concurrent.AppWorker;
import dz.jsoftware95.silverbox.android.concurrent.DuoJob;
import dz.jsoftware95.silverbox.android.concurrent.Job;
import dz.jsoftware95.silverbox.android.frontend.LiveRepresentation;

public class LiveLongProperty extends MutableLiveData<Long> implements LiveRepresentation {

    private final LongPreference preference;

    public LiveLongProperty(LongPreference longPreference) {
        super(AppConfig.getInstance().getLong(longPreference));
        preference = longPreference;
    }

    public LongPreference getPreference() {
        return preference;
    }

    public void increment() {
        update(oldValue -> oldValue != null ? oldValue + 1 : preference.getDefaultLong() + 1);
    }

    public void decrement() {
        update(oldValue -> oldValue != null ? oldValue - 1 : preference.getDefaultLong() - 1);
    }

    public void reset() {
        update(oldValue -> preference.getDefaultLong());
    }

    public void set(long value) {
        update(oldValue -> value);
    }

    public void update(Function<Long, Long> mapper) {
        if (mapper != null)
            newUpdateJob(preference, this, mapper).execute();
    }

    @NotNull
    private static Job newUpdateJob(LongPreference preference, LiveLongProperty liveData, Function<Long, Long> mapper) {
        return new DuoJob<LiveLongProperty, Function<Long, Long>>(AppWorker.BACKGROUND, liveData, mapper) {
            @Override
            protected void doFromBackground(@NotNull LiveLongProperty liveData,
                                            @NotNull Function<Long, Long> mapper) {
                Long mappedValue = mapper.apply(AppConfig.getInstance().getLong(preference));
                long newValue = mappedValue != null ? mappedValue : preference.getDefaultLong();
                AppConfig.getInstance().put(preference, newValue);
                liveData.postValue(mappedValue);
            }
        };
    }
}
