package com.dalti.laposte.client.ui;

import android.os.Bundle;

import com.dalti.laposte.client.R;
import com.dalti.laposte.core.model.WebPageModel;
import com.dalti.laposte.core.ui.AbstractQueueActivity;
import com.dalti.laposte.core.ui.WebPageDetails;

import dagger.hilt.android.AndroidEntryPoint;
import dz.jsoftware95.silverbox.android.middleware.BindingUtil;

@AndroidEntryPoint
public class ClientPrivacyPolicyActivity extends AbstractQueueActivity {

    @Override
    protected Integer getActionBarID() {
        return R.id.action_bar;
    }

    @Override
    protected void setupLayout(Bundle savedState) {
        BindingUtil.setContentView(this, R.layout.activity_client_privacy_policy);
        getViewModel(WebPageModel.class).setPage(WebPageDetails.CLIENT_PRIVACY_POLICY.getPageName(this));
    }
}
