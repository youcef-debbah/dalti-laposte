package com.dalti.laposte.admin.model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.PagedList;

import com.dalti.laposte.admin.repositories.AdminAlarmsListRepository;
import com.dalti.laposte.core.repositories.AdminAlarm;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dz.jsoftware95.silverbox.android.middleware.ContextUtils;
import dz.jsoftware95.silverbox.android.middleware.RepositoryModel;

@HiltViewModel
public class AdminAlarmsListModel extends RepositoryModel<AdminAlarmsListRepository> {

    @NonNull
    private static final PagedList.Config PAGING_CONFIG = new PagedList.Config.Builder()
            .setPageSize(AdminAlarm.PAGE_SIZE)
            .setPrefetchDistance(AdminAlarm.PAGE_SIZE)
            .setInitialLoadSizeHint(AdminAlarm.PAGE_SIZE * 2)
            .build();

    private static final Integer VISIBLE = ContextUtils.VIEW_VISIBLE;
    private static final Integer INVISIBLE = ContextUtils.VIEW_INVISIBLE;

    private final LiveData<PagedList<AdminAlarm>> currentAdminAlarms;
    private final MutableLiveData<Integer> noDataIconVisibility;

    @Inject
    public AdminAlarmsListModel(@NonNull Application application,
                                @NonNull final AdminAlarmsListRepository adminAlarmsListRepository) {
        super(application, adminAlarmsListRepository);
        this.noDataIconVisibility = new MutableLiveData<>(INVISIBLE);
        this.currentAdminAlarms = adminAlarmsListRepository.getLiveList(PAGING_CONFIG);
        this.currentAdminAlarms.observe(this, this::updateNoDataIconVisibility);
        adminAlarmsListRepository.autoRefresh();
    }

    public LiveData<PagedList<AdminAlarm>> getCurrentAdminAlarms() {
        return currentAdminAlarms;
    }

    public LiveData<Integer> getNoDataIconVisibility() {
        return noDataIconVisibility;
    }

    private void updateNoDataIconVisibility(PagedList<AdminAlarm> data) {
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
        updateNoDataIconVisibility(currentAdminAlarms.getValue());
    }
}