package com.dalti.laposte.client.model;

import android.app.Application;

import androidx.annotation.NonNull;

import com.dalti.laposte.client.repository.TurnAlarmRepository;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dz.jsoftware95.silverbox.android.middleware.RepositoryModel;

@HiltViewModel
public class TurnAlarmModel extends RepositoryModel<TurnAlarmRepository> {

    @Inject
    public TurnAlarmModel(@NonNull @NotNull Application application, @NonNull @NotNull TurnAlarmRepository repository) {
        super(application, repository);
    }

    @NonNull
    @NotNull
    @Override
    public TurnAlarmRepository getRepository() {
        return super.getRepository();
    }
}
