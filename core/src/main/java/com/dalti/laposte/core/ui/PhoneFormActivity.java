package com.dalti.laposte.core.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.lifecycle.LiveData;

import com.dalti.laposte.R;
import com.dalti.laposte.core.repositories.Event;
import com.dalti.laposte.core.repositories.Teller;
import com.dalti.laposte.core.util.QueueUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import dz.jsoftware95.queue.common.GlobalUtil;
import dz.jsoftware95.silverbox.android.common.StringUtil;

public abstract class PhoneFormActivity extends AbstractQueueActivity
        implements PhoneForm, PhoneService.VerificationCodeCallback, DialogInterface.OnClickListener {

    public static final String DESCRIPTION_KEY = "description";
    private static final String SMS_CODE_INPUT_LABEL = "sms_verification_code_input";

    protected PhoneService phoneModel;

    @Nullable
    protected AlertDialog dialog;
    @Nullable
    protected EditText input;

    @StringRes
    protected Integer description;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        this.phoneModel = getModel();
        this.description = getIntegerExtra(DESCRIPTION_KEY, R.string.enter_your_phone_number);
        setupDialog();
        QueueUtils.setEditorAction(findViewById(R.id.phone_input), this::updatePhone, "phone_form_input");
    }

    protected abstract PhoneService getModel();

    public Integer getDescription() {
        return description;
    }

    protected void setupDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setCustomTitle(InputDialog.newTitleView(this, R.string.enter_verification_code));
        LinearLayoutCompat layout = new LinearLayoutCompat(this);
        getLayoutInflater().inflate(R.layout.dialog_sms_code_input, layout, true);
        builder.setView(layout);
        builder.setPositiveButton(R.string.ok, this);
        builder.setNegativeButton(R.string.cancel, this);

        this.input = layout.findViewById(R.id.input);
        this.dialog = builder.create();
        QueueUtils.setEditorAction(input, input -> {
            onClick(dialog, DialogInterface.BUTTON_POSITIVE);
            return false;
        }, SMS_CODE_INPUT_LABEL);
    }

    @Override
    protected void onStart() {
        super.onStart();
        phoneModel.setAsCurrentCallback(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        phoneModel.removeCurrentCallback();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
        input = null;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            String input = StringUtil.extractString(this.input);
            Teller.log(Event.SmsVerificationCodeManualInput.NAME,
                    Event.SmsVerificationCodeManualInput.Param.INPUT_LENGTH, String.valueOf(GlobalUtil.length(input)));
            phoneModel.enterVerificationCode(input);
        } else if (which == DialogInterface.BUTTON_NEGATIVE)
            phoneModel.cancelVerification();

        hideVerificationCodeDialog();
    }

    @Override
    public void showVerificationCodeDialog() {
        if (dialog != null) {
            Teller.log(Event.ShowDialog.NAME, Event.ShowDialog.Param.DIALOG_NAME, SMS_CODE_INPUT_LABEL);
            dialog.show();
        }
    }

    @Override
    public void hideVerificationCodeDialog() {
        if (dialog != null) {
            Teller.log(Event.HideDialog.NAME, Event.HideDialog.Param.DIALOG_NAME, SMS_CODE_INPUT_LABEL);
            dialog.cancel();
        }
    }

    @NonNull
    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void verificationSuccessful() {
        setResult(RESULT_OK);
        Intent intent = new Intent();
        finish();
    }

    @Override
    public void submit() {
        updatePhone(input);
    }

    public abstract boolean updatePhone(TextView input);

    public abstract LiveData<String> getPhone();

    public LiveData<String> getStateRepresentation() {
        return phoneModel.getStateRepresentation();
    }

    public LiveData<Integer> getCodeInputVisibility() {
        return phoneModel.getCodeInputVisibility();
    }
}
