package com.dalti.laposte.core.model;

import android.app.Application;

import com.dalti.laposte.core.repositories.ServicesRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dz.jsoftware95.silverbox.android.middleware.RepositoryModel;

@HiltViewModel
public class LoadedServiceModel extends RepositoryModel<ServicesRepository> {

    @Inject
    public LoadedServiceModel(Application application,
                              ServicesRepository repository) {
        super(application, repository);
    }
}
