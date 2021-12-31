package com.dalti.laposte.admin.model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.PagedList;

import com.dalti.laposte.admin.repositories.ActivationsListRepository;
import com.dalti.laposte.core.entity.Activation;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dz.jsoftware95.silverbox.android.middleware.ContextUtils;
import dz.jsoftware95.silverbox.android.middleware.RepositoryModel;

@HiltViewModel
public class ActivationsListModel extends RepositoryModel<ActivationsListRepository> {

    @NonNull
    private static final PagedList.Config PAGING_CONFIG = new PagedList.Config.Builder()
            .setPageSize(Activation.PAGE_SIZE)
            .setPrefetchDistance(Activation.PAGE_SIZE)
            .setInitialLoadSizeHint(Activation.PAGE_SIZE * 2)
            .build();

    private static final Integer VISIBLE = ContextUtils.VIEW_VISIBLE;
    private static final Integer INVISIBLE = ContextUtils.VIEW_INVISIBLE;

    private final LiveData<PagedList<Activation>> currentActivations;
    private final MutableLiveData<Integer> noDataIconVisibility;

    @Inject
    public ActivationsListModel(@NonNull Application application,
                                @NonNull final ActivationsListRepository activationsListRepository) {
        super(application, activationsListRepository);
        this.noDataIconVisibility = new MutableLiveData<>(INVISIBLE);
        this.currentActivations = activationsListRepository.getLiveList(PAGING_CONFIG);
        this.currentActivations.observe(this, this::updateNoDataIconVisibility);
        activationsListRepository.autoRefresh();
    }

    public LiveData<PagedList<Activation>> getCurrentActivations() {
        return currentActivations;
    }

    public LiveData<Integer> getNoDataIconVisibility() {
        return noDataIconVisibility;
    }

    private void updateNoDataIconVisibility(PagedList<Activation> data) {
        if (isRefreshing() || (data != null && !data.isEmpty()))
            noDataIconVisibility.setValue(INVISIBLE);
        else
            noDataIconVisibility.setValue(VISIBLE);
    }

    @Override
    public void markAsRefreshing() {
        super.markAsRefreshing();
        noDataIconVisibility.setValue(INVISIBLE);
    }

    @Override
    public void markAsRefreshDone() {
        super.markAsRefreshDone();
        updateNoDataIconVisibility(currentActivations.getValue());
    }
}