package com.dalti.laposte.admin.ui;

import android.os.Bundle;

import androidx.databinding.ViewDataBinding;

import com.dalti.laposte.admin.R;
import com.dalti.laposte.admin.repositories.ActivationsListRepository;
import com.dalti.laposte.core.ui.AbstractListActivity;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import dz.jsoftware95.silverbox.android.middleware.BindingUtil;

@AndroidEntryPoint
public class ActivationsListActivity extends AbstractListActivity {

    @Inject
    ActivationsListRepository repository;

    @Override
    protected void onRefresh() {
        repository.refresh(true);
    }

    @Override
    protected Integer getActionBarID() {
        return R.id.action_bar;
    }

    @Override
    protected void setupLayout(Bundle savedState) {
        ViewDataBinding binding = BindingUtil.setContentView(this, R.layout.activity_activations_list);
    }
}
