package com.dalti.laposte.admin.model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.PagedList;

import com.dalti.laposte.core.repositories.ShortMessage;
import com.dalti.laposte.core.repositories.SmsRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dz.jsoftware95.silverbox.android.middleware.ContextUtils;
import dz.jsoftware95.silverbox.android.middleware.RepositoryModel;

@HiltViewModel
public class ShortMessagesHistoryModel extends RepositoryModel<SmsRepository> {

    @NonNull
    private static final PagedList.Config PAGING_CONFIG = new PagedList.Config.Builder()
            .setPageSize(ShortMessage.PAGE_SIZE)
            .setPrefetchDistance(ShortMessage.PAGE_SIZE)
            .setInitialLoadSizeHint(ShortMessage.PAGE_SIZE * 2)
            .build();

    private static final Integer VISIBLE = ContextUtils.VIEW_VISIBLE;
    private static final Integer INVISIBLE = ContextUtils.VIEW_INVISIBLE;

    private final LiveData<PagedList<ShortMessage>> currentList;
    private final MutableLiveData<Integer> noDataIconVisibility;

    @Inject
    public ShortMessagesHistoryModel(@NonNull Application application,
                                     @NonNull final SmsRepository repository) {
        super(application, repository);
        this.noDataIconVisibility = new MutableLiveData<>(INVISIBLE);
        this.currentList = repository.getLiveList(PAGING_CONFIG);
        this.currentList.observe(this, this::updateNoDataIconVisibility);
        repository.autoRefresh();
    }

    public LiveData<PagedList<ShortMessage>> getCurrentList() {
        return currentList;
    }

    public LiveData<Integer> getNoDataIconVisibility() {
        return noDataIconVisibility;
    }

    private void updateNoDataIconVisibility(PagedList<?> data) {
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
        updateNoDataIconVisibility(currentList.getValue());
    }
}