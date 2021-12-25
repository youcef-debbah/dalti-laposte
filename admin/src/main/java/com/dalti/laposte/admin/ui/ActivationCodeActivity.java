package com.dalti.laposte.admin.ui;

import android.content.ClipboardManager;
import android.os.Bundle;
import android.view.View;

import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LiveData;

import com.dalti.laposte.admin.BR;
import com.dalti.laposte.admin.R;
import com.dalti.laposte.admin.model.ActivationCodeModel;
import com.dalti.laposte.admin.model.SelectedActivation;
import com.dalti.laposte.core.repositories.Activation;
import com.dalti.laposte.core.ui.AbstractQueueActivity;
import com.dalti.laposte.core.util.QueueUtils;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import dz.jsoftware95.silverbox.android.backend.Item;
import dz.jsoftware95.silverbox.android.middleware.BindingUtil;

@AndroidEntryPoint
public class ActivationCodeActivity extends AbstractQueueActivity {

    private ActivationCodeModel model;

    @Inject
    ClipboardManager clipboard;

    @Override
    protected Integer getActionBarID() {
        return R.id.action_bar;
    }

    @Override
    protected void setupLayout(Bundle savedState) {
        ViewDataBinding binding = BindingUtil.setContentView(this, R.layout.activity_activation_code);
        binding.setVariable(BR.activity, this);
    }

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        model = getViewModel(ActivationCodeModel.class);
        model.selectActivation(getLongExtra(Activation.ID, Item.AUTO_ID), QueueUtils.getSmallestDisplaySize(this));
    }

    public void copyToClipboard(View v) {
        String code;
        SelectedActivation activation = getSelectedActivation().getValue();
        if (activation != null && (code = activation.getActivationCode()) != null)
            QueueUtils.copyToClipboard(clipboard, code, R.string.activation_code);
    }

    public LiveData<SelectedActivation> getSelectedActivation() {
        return model.getSelectedActivation();
    }

    public String getNamespace() {
        return "activation_code_activity";
    }
}
