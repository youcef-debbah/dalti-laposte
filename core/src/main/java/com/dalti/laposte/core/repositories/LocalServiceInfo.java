package com.dalti.laposte.core.repositories;


import com.dalti.laposte.core.util.QueueUtils;

import org.jetbrains.annotations.NotNull;

import dz.jsoftware95.queue.common.PostOfficeAvailability;
import dz.jsoftware95.silverbox.android.common.StringUtil;

public class LocalServiceInfo {
    private long id;
    private int wilaya;
    private int extra;
    private Integer availability;
    private String descriptionEng;
    private String descriptionFre;
    private String descriptionArb;
    private boolean unknown;

    public LocalServiceInfo() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getWilaya() {
        return wilaya;
    }

    public void setWilaya(int wilaya) {
        this.wilaya = wilaya;
    }

    public int getExtra() {
        return extra;
    }

    public void setExtra(int extra) {
        this.extra = extra;
    }

    public Integer getAvailability() {
        return availability;
    }

    public void setAvailability(Integer availability) {
        this.availability = availability;
    }

    public String getDescriptionEng() {
        return descriptionEng;
    }

    public void setDescriptionEng(String descriptionEng) {
        this.descriptionEng = descriptionEng;
    }

    public String getDescriptionFre() {
        return descriptionFre;
    }

    public void setDescriptionFre(String descriptionFre) {
        this.descriptionFre = descriptionFre;
    }

    public String getDescriptionArb() {
        return descriptionArb;
    }

    public void setDescriptionArb(String descriptionArb) {
        this.descriptionArb = descriptionArb;
    }

    public boolean isUnknown() {
        return unknown;
    }

    public void setUnknown(boolean unknown) {
        this.unknown = unknown;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        LocalServiceInfo service = (LocalServiceInfo) o;

        return id == service.id;
    }

    @Override
    public int hashCode() {
        return StringUtil.hash(id);
    }

    @Override
    public @NotNull String toString() {
        return "LocalServiceInfo{" +
                "id=" + id +
                ", " + PostOfficeAvailability.from(availability) +
                ", extra=" + extra +
                ", unknown=" + unknown +
                '}';
    }

    public String getDescription() {
        return QueueUtils.getString(descriptionEng, descriptionFre, descriptionArb);
    }
}
