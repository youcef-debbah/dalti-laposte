package com.dalti.laposte.client.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LiveData;

import com.dalti.laposte.client.BR;
import com.dalti.laposte.client.R;
import com.dalti.laposte.core.repositories.AlarmPhonePreference;
import com.dalti.laposte.core.repositories.ExtraRepository;
import com.dalti.laposte.core.entity.TurnAlarm;
import com.dalti.laposte.core.ui.PhoneFormActivity;
import com.dalti.laposte.core.ui.PhoneService;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import dz.jsoftware95.silverbox.android.backend.Item;
import dz.jsoftware95.silverbox.android.middleware.BindingUtil;

@AndroidEntryPoint
public class ClientPhoneFormActivity extends PhoneFormActivity {

    @Inject
    ExtraRepository extraRepository;

    @Override
    protected PhoneService getModel() {
        return getViewModel(ClientPhoneModel.class);
    }

    @Override
    protected Integer getActionBarID() {
        return R.id.action_bar;
    }

    @Override
    protected void setupLayout(Bundle savedState) {
        ViewDataBinding binding = BindingUtil.setContentView(this, R.layout.activity_phone_form);
        binding.setVariable(BR.activity, this);
    }

    @Override
    public LiveData<String> getPhone() {
        long alarmID = getLongExtra(TurnAlarm.ID, Item.AUTO_ID);
        if (alarmID > Item.AUTO_ID)
            return extraRepository.getString(new AlarmPhonePreference(alarmID));
        else
            return null;
    }

    @SuppressLint("SetTextI18n")
    public boolean updatePhone(TextView input) {
        long alarmID = getLongExtra(TurnAlarm.ID, Item.AUTO_ID);
        if (alarmID > Item.AUTO_ID && input != null) {
            return phoneModel.savePhone(this, input, new AlarmPhonePreference(alarmID));
        } else
            return false;
    }
}
