package com.dalti.laposte.client.model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.dalti.laposte.core.repositories.AbstractActivationRepository;
import com.dalti.laposte.core.repositories.ExtraRepository;
import com.dalti.laposte.core.repositories.InputProperty;
import com.dalti.laposte.core.repositories.SimpleProperty;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dz.jsoftware95.silverbox.android.middleware.RepositoryModel;

@HiltViewModel
public class ActivationFormModel extends RepositoryModel<AbstractActivationRepository> {

    private final ExtraRepository extraRepository;

    @Inject
    public ActivationFormModel(@NonNull Application application,
                               @NonNull AbstractActivationRepository repository,
                               @NonNull ExtraRepository extraRepository) {
        super(application, repository);
        this.extraRepository = extraRepository;
    }

    public void submitActivationCode(String code, String source) {
        extraRepository.put(InputProperty.ACTIVATION_CODE, code);
        getRepository().activateApplication(true, source);
    }

    public LiveData<String> getActivationCodeInput() {
        return extraRepository.get(InputProperty.ACTIVATION_CODE);
    }

    public LiveData<String> getCurrentActivatedCode() {
        return extraRepository.get(SimpleProperty.CURRENT_ACTIVATED_CODE);
    }
}
