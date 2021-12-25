package com.dalti.laposte.core.ui;

import android.app.Activity;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.dalti.laposte.core.repositories.StringPreference;

public interface PhoneService {

    void setAsCurrentCallback(@NonNull VerificationCodeCallback newCallback);

    void removeCurrentCallback();

    void enterVerificationCode(String code);

    void cancelVerification();

    LiveData<String> getStateRepresentation();

    LiveData<Integer> getCodeInputVisibility();

    <T extends Activity & VerificationCodeCallback> boolean savePhone(T activity,
                                                                      TextView input,
                                                                      StringPreference alarmPhonePreference);

    interface VerificationCodeCallback {
        void showVerificationCodeDialog();

        void hideVerificationCodeDialog();

        @NonNull
        Activity getActivity();

        void verificationSuccessful();
    }
}
