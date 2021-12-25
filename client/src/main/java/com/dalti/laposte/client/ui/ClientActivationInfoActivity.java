package com.dalti.laposte.client.ui;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.UiThread;
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

@UiThread
@AndroidEntryPoint
public class ClientActivationInfoActivity extends AbstractDocActivity {

    @Override
    protected Integer getActionBarID() {
        return R.id.action_bar;
    }

    @Override
    protected void setupLayout(Bundle savedState) {
        final ViewDataBinding binding = BindingUtil.setContentView(this, R.layout.activity_client_activation_info);
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

    public void help(View v) {
        startActivity(ClientHelpActivity.class);
    }

    public String getActivationOfferText() {
        final AppConfig appConfig = AppConfig.getInstance();
        final String price = appConfig.get(StringSetting.CHEAPEST_ACTIVATION_PRICE_DZD);
        final String duration = appConfig.get(StringSetting.CHEAPEST_ACTIVATION_DURATION_DAYS);
        if (price != null && duration != null)
            return getString(R.string.help_activation_offer_price_duration, price, duration);
        else if (price != null)
            return getString(R.string.help_activation_offer_price, price);
        else
            return getString(R.string.help_activation_offer);
    }

    public String getNamespace() {
        return "activation_info_activity";
    }
}
