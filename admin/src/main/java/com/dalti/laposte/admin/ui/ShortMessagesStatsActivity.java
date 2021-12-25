package com.dalti.laposte.admin.ui;

import android.os.Bundle;
import android.view.View;

import androidx.databinding.ViewDataBinding;

import com.dalti.laposte.admin.BR;
import com.dalti.laposte.admin.R;
import com.dalti.laposte.admin.model.ShortMessagesStatsModel;
import com.dalti.laposte.core.ui.AbstractQueueActivity;

import dagger.hilt.android.AndroidEntryPoint;
import dz.jsoftware95.silverbox.android.middleware.BindingUtil;

@AndroidEntryPoint
public class ShortMessagesStatsActivity extends AbstractQueueActivity {

    @Override
    protected Integer getActionBarID() {
        return R.id.action_bar;
    }

    @Override
    protected void setupLayout(Bundle savedState) {
        ViewDataBinding binding = BindingUtil.setContentView(this, R.layout.activity_short_messages_stats);
        binding.setVariable(BR.activity, this);
        binding.setVariable(BR.model, getViewModel(ShortMessagesStatsModel.class));
    }

    public void openMessagesHistory(View v) {
        startActivity(ShortMessagesHistoryActivity.class);
    }

    public String getNamespace() {
        return "short_messages_stats_activity";
    }
}
