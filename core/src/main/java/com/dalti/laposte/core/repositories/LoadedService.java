package com.dalti.laposte.core.repositories;

import android.graphics.drawable.Drawable;

import androidx.annotation.WorkerThread;

import com.dalti.laposte.core.ui.VectorDrawableUtil;

import java.util.List;
import java.util.Map;

import dz.jsoftware95.silverbox.android.common.Check;
import dz.jsoftware95.silverbox.android.common.StringUtil;
import dz.jsoftware95.silverbox.android.middleware.ContextUtils;

public class LoadedService {

    private final long id;

    private final int wilaya;

    private final String wilayaName;

    private final int extra;

    private final Integer availability;

    private final String address;

    private final String name;

    private final String description;

    private final int postalCode;

    private final Map<Integer, List<Service.DayEvent>> schedule;

    private final Drawable map;

    private final Drawable thumbnails;

    @WorkerThread
    private LoadedService(Service service) {
        Check.isNotMainThread();
        this.id = service.getId();
        this.wilaya = service.getWilaya();
        this.wilayaName = service.getWilayaName();
        this.extra = service.getExtra();
        this.availability = service.getAvailability();
        this.address = service.getAddress();
        this.name = service.getName();
        this.description = service.getDescription();
        this.postalCode = service.getPostalCode();
        this.schedule = Service.parseSchedule(service.getScheduleData());
        this.map = VectorDrawableUtil.getDrawable(ContextUtils.decodeData(service.getMap()));
        this.thumbnails = VectorDrawableUtil.getDrawable(service.getThumbnails());
    }

    public long getId() {
        return id;
    }

    public int getWilaya() {
        return wilaya;
    }

    public String getWilayaName() {
        return wilayaName;
    }

    public int getExtra() {
        return extra;
    }

    public Integer getAvailability() {
        return availability;
    }

    public String getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Integer getPostalCode() {
        return postalCode;
    }

    public Map<Integer, List<Service.DayEvent>> getSchedule() {
        return schedule;
    }

    public Drawable getMap() {
        return map;
    }

    public Drawable getThumbnails() {
        return thumbnails;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoadedService that = (LoadedService) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return StringUtil.hash(id);
    }

    @WorkerThread
    public static LoadedService from(Service service) {
        return service != null ? new LoadedService(service) : null;
    }

    public static String toDescription(LoadedService loadedService) {
        return loadedService != null? loadedService.getDescription() : null;
    }
}
