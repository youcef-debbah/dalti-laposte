package com.dalti.laposte.admin.ui;

import android.app.Application;

import androidx.annotation.NonNull;

import com.dalti.laposte.core.model.PhoneModel;
import com.dalti.laposte.core.repositories.ExtraRepository;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AdminPhoneModel extends PhoneModel {

    @Inject
    public AdminPhoneModel(@NotNull Application application,
                           @NonNull ExtraRepository extraRepository) {
        super(application, extraRepository);
    }
}
