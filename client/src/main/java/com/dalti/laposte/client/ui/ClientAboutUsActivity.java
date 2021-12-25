package com.dalti.laposte.client.ui;

import android.os.Bundle;
import android.view.View;

import androidx.databinding.ViewDataBinding;

import com.dalti.laposte.client.BR;
import com.dalti.laposte.client.R;
import com.dalti.laposte.core.ui.AbstractDocActivity;
import com.dalti.laposte.core.ui.AbstractQueueApplication;
import com.dalti.laposte.core.util.BuildConfiguration;
import com.dalti.laposte.core.util.QueueUtils;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import dz.jsoftware95.queue.common.GlobalConf;
import dz.jsoftware95.silverbox.android.common.StringUtil;
import dz.jsoftware95.silverbox.android.middleware.BindingUtil;
import dz.jsoftware95.silverbox.android.middleware.TimeUtils;

@AndroidEntryPoint
public class ClientAboutUsActivity extends AbstractDocActivity {

    @Inject
    BuildConfiguration buildConfiguration;

    @Override
    protected Integer getActionBarID() {
        return R.id.action_bar;
    }

    @Override
    protected void setupLayout(Bundle savedState) {
        final ViewDataBinding binding = BindingUtil.setContentView(this, R.layout.activity_client_about_us);
        binding.setVariable(BR.activity, this);
    }

    public String getBuildVersion() {
        final String value = buildConfiguration.getFullVersionName();
        return getString(R.string.build_version, value != null ? value : GlobalConf.EMPTY_TOKEN);
    }

    public String getBuildTime() {
        String value = TimeUtils.formatAsDateTime(StringUtil.parseLong(getString(R.string.build_epoch)));
        return getString(R.string.build_time, value != null ? value : getString(R.string.unknown_symbol));
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

    public String getNamespace() {
        return "client_about_us_activity";
    }
}
