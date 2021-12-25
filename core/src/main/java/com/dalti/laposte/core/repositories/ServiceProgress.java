package com.dalti.laposte.core.repositories;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.Collections;
import java.util.List;

public class ServiceProgress {
    @Embedded
    private Service service;

    @Relation(parentColumn = Service.ID, entityColumn = Progress.SERVICE_ID)
    private List<Progress> progresses;

    public ServiceProgress() {
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public List<Progress> getProgresses() {
        return progresses;
    }

    public void setProgresses(List<Progress> progresses) {
        this.progresses = progresses;
    }

    @Override
    @NonNull
    public String toString() {
        return "Selection{" +
                "service=" + service +
                ", progresses=" + progresses +
                '}';
    }

    public static Service getService(ServiceProgress serviceProgress) {
        return serviceProgress != null ? serviceProgress.getService() : null;
    }

    public static List<Progress> getProgresses(ServiceProgress serviceProgress) {
        if (serviceProgress != null) {
            List<Progress> progresses = serviceProgress.getProgresses();
            if (progresses != null)
                return progresses;
        }

        return Collections.emptyList();
    }
}
