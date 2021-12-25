package com.dalti.laposte.core.ui;

import android.os.Bundle;

import com.dalti.laposte.core.model.WebPageModel;

import dagger.hilt.android.AndroidEntryPoint;
import com.dalti.laposte.R;
import dz.jsoftware95.silverbox.android.middleware.BindingUtil;

@AndroidEntryPoint
public class HowToFixGoogleServicesActivity extends AbstractQueueActivity {
    @Override
    protected Integer getActionBarID() {
        return R.id.action_bar;
    }

    @Override
    protected void setupLayout(Bundle savedState) {
        BindingUtil.setContentView(this, R.layout.activity_fix_google_services);
        getViewModel(WebPageModel.class).setPage(WebPageDetails.HOW_TO_FIX_GOOGLE_SERVICES.getPageName(this));
    }
}
