package com.dalti.laposte.client.ui;

import android.app.Application;

import androidx.annotation.NonNull;

import com.dalti.laposte.client.repository.TurnAlarmRepository;
import com.dalti.laposte.core.model.PhoneModel;
import com.dalti.laposte.core.repositories.AlarmPhonePreference;
import com.dalti.laposte.core.repositories.AppConfig;
import com.dalti.laposte.core.repositories.ExtraRepository;
import com.dalti.laposte.core.repositories.StringPreference;
import com.dalti.laposte.core.repositories.StringSetting;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ClientPhoneModel extends PhoneModel {

    protected final TurnAlarmRepository alarmRepository;

    @Inject
    public ClientPhoneModel(@NotNull Application application,
                            @NotNull TurnAlarmRepository alarmRepository,
                            @NonNull ExtraRepository extraRepository) {
        super(application, extraRepository);
        this.alarmRepository = alarmRepository;
    }

    @Override
    protected void onUpdate(StringPreference preference, String phone) {
        if (preference instanceof AlarmPhonePreference && phone != null) {
            alarmRepository.updatePhone(((AlarmPhonePreference) preference).getAlarmID(), phone);
            AppConfig.getInstance().put(StringSetting.LATEST_TURN_ALARM_PHONE, phone);
        }
    }
}
