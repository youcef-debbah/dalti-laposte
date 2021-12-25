package com.dalti.laposte.core.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import dz.jsoftware95.silverbox.android.middleware.ContextUtils;

public class Estimation {

    private final Integer remainingTime;
    private final Integer remainingTimeLabel;

    private final Integer potentialTime;
    private final Integer potentialTimeLabel;

    public Estimation(Integer remainingTime, Integer remainingTimeLabel,
                      Integer potentialTime, Integer potentialTimeLabel,
                      Integer ticket) {
        if (ticket == null) {
            this.remainingTime = null;
            this.remainingTimeLabel = null;

            this.potentialTime = potentialTime;
            this.potentialTimeLabel = potentialTimeLabel;
        } else {
            this.remainingTime = remainingTime;
            this.remainingTimeLabel = remainingTimeLabel;

            this.potentialTime = null;
            this.potentialTimeLabel = null;
        }
    }

    public static LiveData<Estimation> liveDataFrom(LiveData<Progress> progressData) {
        MediatorLiveData<Estimation> liveData = new MediatorLiveData<>();
        liveData.addSource(progressData, progress -> {
            if (progress != null) {
                liveData.setValue(new Estimation(progress.calcRemainingTime(), progress.calcRemainingTimeLabel(),
                        progress.calcPotentialTime(), progress.calcPotentialTimeLabel(), progress.getTicket()));
            } else
                liveData.setValue(null);
        });
        return liveData;
    }

    private boolean remainingTimeInfoVisible() {
        return remainingTime != null || remainingTimeLabel != null;
    }

    private boolean potentialTimeInfoVisible() {
        return potentialTime != null || potentialTimeLabel != null;
    }

    public Integer getEstimatedTime() {
        return remainingTimeInfoVisible() ? remainingTime : potentialTime;
    }

    public Integer getEstimatedLabel() {
        return remainingTimeInfoVisible() ? remainingTimeLabel : potentialTimeLabel;
    }

    public Integer getVisibility() {
        return remainingTimeInfoVisible() || potentialTimeInfoVisible() ?
                ContextUtils.VIEW_VISIBLE : ContextUtils.VIEW_GONE;
    }
}
