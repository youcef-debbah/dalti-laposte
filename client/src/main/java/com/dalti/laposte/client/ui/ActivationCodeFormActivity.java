package com.dalti.laposte.client.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.dalti.laposte.client.BR;
import com.dalti.laposte.client.R;
import com.dalti.laposte.client.model.ActivationFormModel;
import com.dalti.laposte.core.repositories.InputProperty;
import com.dalti.laposte.core.repositories.Teller;
import com.dalti.laposte.core.ui.AbstractQueueActivity;
import com.dalti.laposte.core.ui.Form;
import com.dalti.laposte.core.ui.Request;
import com.dalti.laposte.core.ui.scanner.ScannerActivity;
import com.dalti.laposte.core.util.QueueUtils;

import org.jetbrains.annotations.NotNull;

import dagger.hilt.android.AndroidEntryPoint;
import dz.jsoftware95.queue.common.GlobalConf;
import dz.jsoftware95.silverbox.android.backend.BackendEvent;
import dz.jsoftware95.silverbox.android.common.StringUtil;
import dz.jsoftware95.silverbox.android.middleware.BindingUtil;
import dz.jsoftware95.silverbox.android.middleware.ContextUtils;
import dz.jsoftware95.silverbox.android.observers.MainObserver;

@AndroidEntryPoint
public class ActivationCodeFormActivity extends AbstractQueueActivity implements Form {

    @Nullable
    private TextView codeInput;

    private ActivationFormModel model;
    private MainObserver<BackendEvent> observer;
    private ActivityResultLauncher<Intent> activationCodeReader;
    private LiveData<Integer> currentActivationCodeVisibility;

    @Override
    protected Integer getActionBarID() {
        return R.id.action_bar;
    }

    @Override
    protected void setupLayout(Bundle savedState) {
        model = getViewModel(ActivationFormModel.class);
        model.addDataObserver(observer = newSuccessListener(this));
        ViewDataBinding binding = BindingUtil.setContentView(this, R.layout.activity_activation_code_form);
        binding.setVariable(BR.activity, this);
    }

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        codeInput = findViewById(R.id.code_input);
        QueueUtils.setEditorAction(codeInput, this::submitActivationCode, "activation_code_input");
        activationCodeReader = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), newActivationCodeHandler());
        currentActivationCodeVisibility = Transformations.map(model.getCurrentActivatedCode(), code -> StringUtil.isBlank(code) ? ContextUtils.VIEW_GONE : ContextUtils.VIEW_VISIBLE);
    }

    @NotNull
    private ActivityResultCallback<ActivityResult> newActivationCodeHandler() {
        return result -> {
            if (result.getResultCode() == RESULT_OK && codeInput != null) {
                String code = StringUtil.getString(result.getData(), InputProperty.ACTIVATION_CODE.name());
                if (!StringUtil.isBlank(code)) {
                    codeInput.setText(code);
                    model.submitActivationCode(code.trim(), getStringExtra(Teller.ACTIVATION_SOURCE));
                } else
                    codeInput.setText("");
            }
        };
    }

    public void startCodeScan(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
            startScanner();
        else
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, Request.REQUEST_CAMERA_PERMISSION.ordinal());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Request.REQUEST_CAMERA_PERMISSION.ordinal()) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                startScanner();
            else {
                QueueUtils.showToast(this, R.string.manual_activation_code_input_needed);
                Teller.logPermissionDenied(Manifest.permission.CAMERA);
            }
        } else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void startScanner() {
        activationCodeReader.launch(new Intent(this, ScannerActivity.class));
    }

    public void openActivationInfo(View v) {
        startActivity(ClientActivationInfoActivity.class);
    }

    @Override
    public void submit() {
        if (codeInput != null)
            submitActivationCode(codeInput);
    }

    private boolean submitActivationCode(TextView codeInput) {
        String code = StringUtil.toString(codeInput.getText());
        if (!StringUtil.isBlank(code)) {
            int codeLength = code.length();
            if (codeLength < getActivationCodeLength())
                codeInput.setError(getString(R.string.activation_code_too_short));
            else if (codeLength > getActivationCodeLength())
                codeInput.setError(getString(R.string.activation_code_too_long));
            else if (!GlobalConf.ACTIVATION_CODE_PATTERN.matcher(code).matches())
                codeInput.setError(getString(R.string.only_numbers_are_allowed));
            else {
                codeInput.setError(null);
                model.submitActivationCode(code.trim(), getStringExtra(Teller.ACTIVATION_SOURCE));
                return false;
            }
        } else
            codeInput.setError(getString(R.string.enter_activation_code_first));

        return true;
    }

    public int getActivationCodeLength() {
        return GlobalConf.ACTIVATION_CODE_LENGTH;
    }

    public LiveData<String> getActivationCodeInput() {
        return model.getActivationCodeInput();
    }

    public LiveData<String> getCurrentActivatedCode() {
        return model.getCurrentActivatedCode();
    }

    public LiveData<Integer> getCurrentActivationCodeVisibility() {
        return currentActivationCodeVisibility;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        observer = null;
    }

    public String getNamespace() {
        return "activation_activity";
    }
}
