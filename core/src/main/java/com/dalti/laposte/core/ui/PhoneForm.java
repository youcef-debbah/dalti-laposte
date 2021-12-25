package com.dalti.laposte.core.ui;

import android.widget.TextView;

import androidx.lifecycle.LiveData;

public interface PhoneForm extends Form, PhoneService.VerificationCodeCallback {

    LiveData<String> getPhone();

    void finish();

    Integer getDescription();

    boolean updatePhone(TextView view);

    LiveData<String> getStateRepresentation();

    LiveData<Integer> getCodeInputVisibility();

    default String getNamespace() {
        return "phone_form_activity";
    }
}
