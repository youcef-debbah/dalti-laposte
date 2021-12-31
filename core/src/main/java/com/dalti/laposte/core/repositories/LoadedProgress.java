package com.dalti.laposte.core.repositories;

import androidx.annotation.NonNull;

import com.dalti.laposte.core.entity.Progress;
import com.dalti.laposte.core.entity.Service;

import org.jetbrains.annotations.Nullable;

import dz.jsoftware95.queue.common.IdentityManager;

public class LoadedProgress {

    private final long id;

    private final String serviceName;
    private final String serviceDescription;
    private final long serviceID;

    private final Integer currentToken;
    private final Integer waiting;
    private final Long potentialTime;
    private final Long timestamp;

    private final Integer ticket;

    private final Long remainingTime;
    private final Long averageServingTime;
    private final Integer progressIcon;

    private LoadedProgress(long id, String serviceName, String serviceDescription, long serviceID, Integer currentToken, Integer waiting, Long potentialTime, Long timestamp, Integer ticket, Long remainingTime, Long averageServingTime) {
        this.id = id;
        this.serviceName = serviceName;
        this.serviceDescription = serviceDescription;
        this.serviceID = serviceID;
        this.currentToken = currentToken;
        this.waiting = waiting;
        this.potentialTime = potentialTime;
        this.timestamp = timestamp;
        this.ticket = ticket;
        this.remainingTime = remainingTime;
        this.averageServingTime = averageServingTime;
        this.progressIcon = Progress.getProgressIcon(IdentityManager.getProgressRank(id));
    }

    @Nullable
    public static LoadedProgress from(Service s, Progress p) {
        if (s == null || p == null)
            return null;
        else
            return new LoadedProgress(p.getId(), s.getName(), s.getDescription(),
                    p.getServiceID(), p.getCurrentToken(), p.getWaiting(),
                    p.getPotentialTime(), p.getTimestamp(), p.getTicket(),
                    p.getRemainingTime(), p.getAverageServingTime());
    }

    public long getId() {
        return id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getServiceDescription() {
        return serviceDescription;
    }

    public long getServiceID() {
        return serviceID;
    }

    public Integer getCurrentToken() {
        return currentToken;
    }

    public Integer getWaiting() {
        return waiting;
    }

    public Long getPotentialTime() {
        return potentialTime;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public Integer getTicket() {
        return ticket;
    }

    public Long getRemainingTime() {
        return remainingTime;
    }

    public Long getAverageServingTime() {
        return averageServingTime;
    }

    public Integer getProgressIcon() {
        return progressIcon;
    }

    @Override
    @NonNull
    public String toString() {
        return serviceDescription;
    }

    public static String toServiceDescription(LoadedProgress loadedProgress) {
        return loadedProgress != null ? loadedProgress.getServiceDescription() : null;
    }
}
