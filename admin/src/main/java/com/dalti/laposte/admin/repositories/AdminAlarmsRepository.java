package com.dalti.laposte.admin.repositories;

import androidx.annotation.AnyThread;
import androidx.annotation.Nullable;

import com.dalti.laposte.core.repositories.AdminAlarm;
import com.dalti.laposte.core.repositories.AdminAlarmDAO;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Lazy;
import dz.jsoftware95.silverbox.android.backend.LoadableRepository;

@Singleton
@AnyThread
public class AdminAlarmsRepository extends LoadableRepository<AdminAlarm, AdminAlarm, AdminAlarmDAO> {

    @Inject
    @AnyThread
    public AdminAlarmsRepository(Lazy<AdminAlarmDAO> dao) {
        super(dao);
    }

    @Override
    @Nullable
    protected AdminAlarm loadFrom(@Nullable AdminAlarm data) {
        return data;
    }
}
