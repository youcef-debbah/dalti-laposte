package com.dalti.laposte.admin.model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.dalti.laposte.core.repositories.AbstractActivationRepository;
import com.dalti.laposte.core.repositories.ExtraRepository;
import com.dalti.laposte.core.repositories.InputProperty;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dz.jsoftware95.silverbox.android.middleware.RepositoryModel;

@HiltViewModel
public class ProfileModel extends RepositoryModel<AbstractActivationRepository> {

    @NonNull
    private final ExtraRepository extraRepository;

    private final LiveData<String> activeUsername;

    @Inject
    public ProfileModel(@NonNull Application application,
                           @NonNull AbstractActivationRepository activationRepository,
                           @NonNull ExtraRepository extraRepository) {
        super(application, activationRepository);
        this.extraRepository = extraRepository;
        this.activeUsername = activationRepository.getActiveUsername();
    }

    public LiveData<String> getActiveUsername() {
        return activeUsername;
    }

    public LiveData<String> getPrincipalName() {
        return extraRepository.get(InputProperty.PRINCIPAL_NAME);
    }


    public void setPrincipalName(String name) {
        extraRepository.put(InputProperty.PRINCIPAL_NAME, name);
    }

    public LiveData<String> getPrincipalPassword() {
        return extraRepository.get(InputProperty.PRINCIPAL_PASSWORD);
    }

    public void setPrincipalPassword(String password) {
        extraRepository.put(InputProperty.PRINCIPAL_PASSWORD, password);
    }

    public void activateApplication(String source) {
        getRepository().activateApplication(true, source);
    }
}
