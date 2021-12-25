package com.dalti.laposte.admin.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LiveData;

import com.dalti.laposte.admin.BR;
import com.dalti.laposte.admin.R;
import com.dalti.laposte.core.repositories.ExtraRepository;
import com.dalti.laposte.core.repositories.StringSetting;
import com.dalti.laposte.core.ui.PhoneFormActivity;
import com.dalti.laposte.core.ui.PhoneService;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import dz.jsoftware95.silverbox.android.middleware.BindingUtil;

@AndroidEntryPoint
public class AdminPhoneFormActivity extends PhoneFormActivity {

    @Inject
    ExtraRepository extraRepository;

    @Override
    protected PhoneService getModel() {
        return getViewModel(AdminPhoneModel.class);
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

    @SuppressLint("SetTextI18n")
    public boolean updatePhone(TextView input) {
        if (input != null) {
            return phoneModel.savePhone(this, input, StringSetting.CONTACT_PHONE);
        } else
            return false;
    }

    @Override
    public LiveData<String> getPhone() {
        return extraRepository.getString(StringSetting.CONTACT_PHONE);
    }
}
