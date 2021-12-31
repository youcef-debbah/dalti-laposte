package com.dalti.laposte.core.ui;

import android.os.Bundle;

import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LiveData;

import com.dalti.laposte.BR;
import com.dalti.laposte.R;
import com.dalti.laposte.core.repositories.LoadedService;
import com.dalti.laposte.core.entity.Service;
import com.dalti.laposte.core.repositories.ServicesRepository;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import dz.jsoftware95.silverbox.android.middleware.BindingUtil;

@AndroidEntryPoint
public class MapActivity extends AbstractQueueActivity {

    private LiveData<LoadedService> service;

    private ServicesRepository servicesRepository;

    @Inject
    public void setModel(ServicesRepository servicesRepository) {
        this.servicesRepository = servicesRepository;
    }

    @Override
    protected Integer getActionBarID() {
        return R.id.action_bar;
    }

    @Override
    protected void setupLayout(Bundle savedState) {
        ViewDataBinding binding = BindingUtil.setContentView(this, R.layout.activity_map);
        binding.setVariable(BR.activity, this);
    }

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        this.service = servicesRepository.getLoadedData(getLongExtra(Service.ID, 0));
    }

    public LiveData<LoadedService> getService() {
        return service;
    }
}
