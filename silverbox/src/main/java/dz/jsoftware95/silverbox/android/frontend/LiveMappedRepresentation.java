package dz.jsoftware95.silverbox.android.frontend;

import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import dz.jsoftware95.silverbox.android.observers.DuoMainObserver;
import dz.jsoftware95.silverbox.android.observers.MainObserver;

public class LiveMappedRepresentation extends MediatorLiveData<String> implements LiveRepresentation {

    public LiveMappedRepresentation(final @NonNull Resources resources,
                                    final @NonNull LiveData<? extends Representable> liveData) {
        addSource(liveData, newDataObserver(this, resources));
    }

    private MainObserver<Representable> newDataObserver(MutableLiveData<String> liveData, Resources resources) {
        return new DuoMainObserver<MutableLiveData<String>, Resources, Representable>(liveData, resources) {
            @Override
            protected void onUpdate(@NonNull MutableLiveData<String> liveData,
                                    @NonNull Resources resources,
                                    @Nullable Representable data) {
                if (data != null)
                    liveData.setValue(resources.getString(data.getStringRes()));
                else
                    liveData.setValue(null);
            }
        };
    }
}
