package com.dalti.laposte.core.model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.PagedList;

import com.dalti.laposte.core.entity.Service;
import com.dalti.laposte.core.repositories.ServicesListRepository;
import com.dalti.laposte.core.repositories.Teller;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dz.jsoftware95.silverbox.android.middleware.ContextUtils;
import dz.jsoftware95.silverbox.android.middleware.RepositoryModel;

@HiltViewModel
public class ServicesListModel extends RepositoryModel<ServicesListRepository> {

    @NonNull
    private static final PagedList.Config PAGING_CONFIG = new PagedList.Config.Builder()
            .setPageSize(Service.PAGE_SIZE)
            .setPrefetchDistance(Service.PAGE_SIZE)
            .setInitialLoadSizeHint(Service.PAGE_SIZE * 2)
            .build();

    private static final Integer VISIBLE = ContextUtils.VIEW_VISIBLE;
    private static final Integer INVISIBLE = ContextUtils.VIEW_INVISIBLE;

    private final LiveData<PagedList<Service>> currentServices;
    private final MutableLiveData<Integer> noDataIconVisibility;

    /**
     * Creates a new Refreshable View Model with an empty refresh observers list
     *
     * @param application the "application instance to be used by this model
     */
    @Inject
    public ServicesListModel(@NonNull Application application,
                             @NonNull final ServicesListRepository servicesListRepository) {
        super(application, servicesListRepository);
        this.noDataIconVisibility = new MutableLiveData<>(INVISIBLE);
        this.currentServices = servicesListRepository.getLiveList(PAGING_CONFIG);
        this.currentServices.observe(this, this::updateNoDataIconVisibility);
        servicesListRepository.autoRefresh();
    }

    @NonNull
    public LiveData<PagedList<Service>> getCurrentServices() {
        return currentServices;
    }

    public void setCurrentService(Long serviceID) {
        getRepository().setCurrentService(serviceID);
        Teller.logSelectContentEvent(String.valueOf(serviceID), Service.TABLE_NAME);
    }

    public LiveData<Integer> getNoDataIconVisibility() {
        return noDataIconVisibility;
    }

    private void updateNoDataIconVisibility(PagedList<Service> data) {
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
        updateNoDataIconVisibility(currentServices.getValue());
    }
}
