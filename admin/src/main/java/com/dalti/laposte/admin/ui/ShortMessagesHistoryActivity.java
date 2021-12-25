package com.dalti.laposte.admin.ui;

import android.os.Bundle;

import com.dalti.laposte.admin.R;
import com.dalti.laposte.core.ui.AbstractQueueActivity;

import dagger.hilt.android.AndroidEntryPoint;
import dz.jsoftware95.silverbox.android.middleware.BindingUtil;

@AndroidEntryPoint
public class ShortMessagesHistoryActivity extends AbstractQueueActivity {

    @Override
    protected Integer getActionBarID() {
        return R.id.action_bar;
    }

    @Override
    protected void setupLayout(Bundle savedState) {
        BindingUtil.setContentView(this, R.layout.activity_short_messages_history);
    }
}
