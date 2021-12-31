package com.dalti.laposte.core.ui;

import android.os.Bundle;
import android.view.View;

import androidx.databinding.ViewDataBinding;

import com.dalti.laposte.BR;
import com.dalti.laposte.R;
import com.dalti.laposte.core.entity.Service;
import com.dalti.laposte.core.repositories.ServicesListRepository;
import com.dalti.laposte.core.repositories.Teller;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import dz.jsoftware95.silverbox.android.middleware.BindingUtil;

@AndroidEntryPoint
public class ServicesListActivity extends AbstractListActivity {

    @Inject
    ServicesListRepository repository;

    @Override
    public Integer getActionBarID() {
        return R.id.action_bar;
    }

    @Override
    protected void setupLayout(Bundle savedState) {
        ViewDataBinding binding = BindingUtil.setContentView(this, R.layout.activity_services_list);
        binding.setVariable(BR.activity, this);
    }

    @Override
    protected void onRefresh() {
        repository.refresh(true);
    }

    public void clearSelection(View v) {
        repository.setCurrentService(null);
        Teller.logSelectContentEvent("cleared", Service.TABLE_NAME);
        cancelResultAndFinish();
    }

    public String getNamespace() {
        return "services_list_activity";
    }
}
