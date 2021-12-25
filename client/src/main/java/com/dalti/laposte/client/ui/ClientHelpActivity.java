package com.dalti.laposte.client.ui;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;

import androidx.databinding.ViewDataBinding;

import com.dalti.laposte.client.BR;
import com.dalti.laposte.client.R;
import com.dalti.laposte.core.repositories.AppConfig;
import com.dalti.laposte.core.repositories.StringSetting;
import com.dalti.laposte.core.ui.AbstractDocActivity;
import com.dalti.laposte.core.ui.AbstractQueueApplication;
import com.dalti.laposte.core.util.QueueUtils;

import dagger.hilt.android.AndroidEntryPoint;
import dz.jsoftware95.silverbox.android.middleware.BindingUtil;

@AndroidEntryPoint
public class ClientHelpActivity extends AbstractDocActivity {

    @Override
    protected Integer getActionBarID() {
        return R.id.action_bar;
    }

    @Override
    protected void setupLayout(Bundle savedState) {
        final ViewDataBinding binding = BindingUtil.setContentView(this, R.layout.activity_client_help);
        binding.setVariable(BR.activity, this);
    }

    public void privacyPolicy(View v) {
        openActivity(AbstractQueueApplication.requireInstance().getPrivacyPolicyActivity());
    }

    public void sendFeedback(View v) {
        QueueUtils.sendFeedback(this);
    }

    public void rateUs(View v) {
        openRatingDialog();
    }

    public void howToGetActivationCode(View v) {
        startActivity(ClientActivationInfoActivity.class);
    }

    public String getEmail() {
        return AppConfig.getInstance().get(StringSetting.EMAIL);
    }

    public ColorStateList getTicketCardColor() {
        return QueueUtils.getColorStateList(R.color.alternative_surface_color_selector);

    }

    public ColorStateList getTicketColor() {
        return QueueUtils.getColorStateList(R.color.on_alternative_surface_color_selector);
    }

    public String getNamespace() {
        return "client_help_activity";
    }
}
