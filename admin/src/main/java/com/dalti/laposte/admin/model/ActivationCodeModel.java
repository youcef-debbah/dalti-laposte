package com.dalti.laposte.admin.model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.dalti.laposte.admin.repositories.ActivationsListRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dz.jsoftware95.queue.api.Pair;
import dz.jsoftware95.silverbox.android.middleware.RepositoryModel;

@HiltViewModel
public class ActivationCodeModel extends RepositoryModel<ActivationsListRepository> {

    private final MutableLiveData<Pair<Long, Integer>> selectedActivationID;
    private final LiveData<SelectedActivation> selectedActivation;

    @Inject
    public ActivationCodeModel(@NonNull Application application,
                               @NonNull final ActivationsListRepository activationsListRepository) {
        super(application, activationsListRepository);
        MutableLiveData<Pair<Long, Integer>> selectedActivationID = new MutableLiveData<>(null);
        this.selectedActivation = Transformations.switchMap(selectedActivationID, activationsListRepository::getSelectedActivation);
        this.selectedActivationID = selectedActivationID;
    }

    public void selectActivation(Long activationID, Integer imageSize) {
        selectedActivationID.setValue(new Pair<>(activationID, imageSize));
    }

    public LiveData<SelectedActivation> getSelectedActivation() {
        return selectedActivation;
    }
}
