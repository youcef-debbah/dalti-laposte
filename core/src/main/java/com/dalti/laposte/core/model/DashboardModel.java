package com.dalti.laposte.core.model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.dalti.laposte.core.repositories.DashboardRepository;
import com.dalti.laposte.core.repositories.Estimation;
import com.dalti.laposte.core.repositories.ExtraRepository;
import com.dalti.laposte.core.repositories.Selection;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dz.jsoftware95.silverbox.android.middleware.RepositoryModel;

@HiltViewModel
public class DashboardModel extends RepositoryModel<DashboardRepository> {

    protected LiveData<Selection> selection;
    protected LiveData<Selection.Statistics> statistics;

    /**
     * Creates a new Refreshable View Model with an empty refresh observers list
     *
     * @param application the application instance to be used by this model
     */
    @Inject
    public DashboardModel(@NonNull Application application,
                          @NonNull DashboardRepository dashboardRepository,
                          @NonNull ExtraRepository extraRepository) {
        super(application, dashboardRepository);
        this.selection = dashboardRepository.getSelection();
        this.statistics = dashboardRepository.getStatistics();
    }

    public LiveData<Selection> getSelection() {
        return selection;
    }

    public LiveData<Selection.Statistics> getStatistics() {
        return statistics;
    }

    public void setTicket(Integer ticket, long progress) {
        getRepository().setTicket(ticket, progress);
    }

    public LiveData<Estimation> getEstimation(Long progressID) {
        return getRepository().getEstimation(progressID);
    }
}
