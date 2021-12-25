package com.dalti.laposte.core.repositories;

import androidx.lifecycle.MutableLiveData;

import org.jetbrains.annotations.NotNull;

import dz.jsoftware95.queue.common.Function;
import dz.jsoftware95.silverbox.android.concurrent.AppWorker;
import dz.jsoftware95.silverbox.android.concurrent.DuoJob;
import dz.jsoftware95.silverbox.android.concurrent.Job;
import dz.jsoftware95.silverbox.android.frontend.LiveRepresentation;

public class LiveStringProperty extends MutableLiveData<String> implements LiveRepresentation {

    private final StringPreference preference;

    public LiveStringProperty(StringPreference preference) {
        super(AppConfig.getInstance().get(preference));
        this.preference = preference;
    }

    public StringPreference getPreference() {
        return preference;
    }

    public void reset() {
        update(oldValue -> preference.getDefaultString());
    }

    public void set(String value) {
        update(oldValue -> value);
    }

    public void update(Function<String, String> mapper) {
        if (mapper != null)
            newUpdateJob(preference, this, mapper).execute();
    }

    @NotNull
    private static Job newUpdateJob(StringPreference preference, LiveStringProperty liveData, Function<String, String> mapper) {
        return new DuoJob<LiveStringProperty, Function<String, String>>(AppWorker.BACKGROUND, liveData, mapper) {
            @Override
            protected void doFromBackground(@NotNull LiveStringProperty liveData,
                                            @NotNull Function<String, String> mapper) {
                String mappedValue = mapper.apply(AppConfig.getInstance().get(preference));
                String newValue = mappedValue != null ? mappedValue : preference.getDefaultString();
                AppConfig.getInstance().put(preference, newValue);
                liveData.postValue(mappedValue);
            }
        };
    }
}
