package com.dalti.laposte.admin.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LiveData;

import com.dalti.laposte.admin.BR;
import com.dalti.laposte.admin.R;
import com.dalti.laposte.admin.model.ProfileModel;
import com.dalti.laposte.core.repositories.ExtraRepository;
import com.dalti.laposte.core.repositories.StringSetting;
import com.dalti.laposte.core.repositories.Teller;
import com.dalti.laposte.core.ui.AbstractQueueActivity;
import com.dalti.laposte.core.ui.Form;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import dz.jsoftware95.silverbox.android.backend.BackendEvent;
import dz.jsoftware95.silverbox.android.common.StringUtil;
import dz.jsoftware95.silverbox.android.middleware.BindingUtil;
import dz.jsoftware95.silverbox.android.observers.UnMainObserver;

@AndroidEntryPoint
public class ProfileActivity extends AbstractQueueActivity implements Form {

    private ProfileModel profileModel;

    private TextView phoneInput;
    private TextView nameInput;
    private TextView passwordInput;
    private UnMainObserver<Activity, BackendEvent> observer;

    @Inject
    ExtraRepository extraRepository;

    @Override
    protected Integer getActionBarID() {
        return R.id.action_bar;
    }

    @Override
    protected void setupLayout(Bundle savedState) {
        ViewDataBinding binding = BindingUtil.setContentView(this, R.layout.activity_profile);
        binding.setVariable(BR.activity, this);
    }

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        profileModel = getViewModel(ProfileModel.class);
        profileModel.addDataObserver(observer = newSuccessListener(this));

        phoneInput = findViewById(R.id.phone_input);
        nameInput = findViewById(R.id.name_input);
        passwordInput = findViewById(R.id.password_input);
    }

    public LiveData<String> getPrincipalPhone() {
        return extraRepository.getString(StringSetting.CONTACT_PHONE);
    }

    public void openPhoneForm(View view) {
        startActivity(AdminPhoneFormActivity.class);
    }

    public LiveData<String> getActiveUsername() {
        return profileModel.getActiveUsername();
    }

    public LiveData<String> getPrincipalName() {
        return profileModel.getPrincipalName();
    }

    public LiveData<String> getPrincipalPassword() {
        return profileModel.getPrincipalPassword();
    }

    @Override
    public void submit() {
        if (nameInput != null && nameInput.getError() == null)
            profileModel.setPrincipalName(StringUtil.toString(nameInput.getText()));

        if (passwordInput != null && passwordInput.getError() == null)
            profileModel.setPrincipalPassword(StringUtil.toString(passwordInput.getText()));

        if (phoneInput != null && phoneInput.getError() == null && passwordInput.getError() == null)
            profileModel.activateApplication(getStringExtra(Teller.ACTIVATION_SOURCE));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        observer = null;
    }

    public String getNamespace() {
        return "profile_activity";
    }
}
