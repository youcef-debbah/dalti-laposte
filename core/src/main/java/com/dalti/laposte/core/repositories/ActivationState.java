package com.dalti.laposte.core.repositories;

import org.jetbrains.annotations.NotNull;

import dz.jsoftware95.silverbox.android.middleware.ContextUtils;

public class ActivationState {

    private final boolean active;
    private final boolean notExpired;
    private final Long key;
    private final Long expirationDate;

    private final long clientSituation;
    private final long serverSituation;

    private final String applicationID;
    private final Long googleServicesVersion;

    public ActivationState(long key, long expirationDate,
                           long clientSituation, long serverSituation,
                           String applicationID, Long googleServicesVersion) {
        long now = System.currentTimeMillis();
        boolean validKey = key != 0;
        boolean notExpired = expirationDate > now;
        this.active = validKey && notExpired;
        this.notExpired = notExpired;
        this.key = validKey ? key : null;
        this.expirationDate = expirationDate > 0 ? expirationDate : null;

        this.clientSituation = clientSituation;
        this.serverSituation = serverSituation;

        this.applicationID = applicationID;
        this.googleServicesVersion = googleServicesVersion;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isNotActive() {
        return !active;
    }

    public boolean isExpired() {
        return key != null && !notExpired;
    }

    public Long getKey() {
        return key;
    }

    public Long getExpirationDate() {
        return expirationDate;
    }

    public long getClientSituation() {
        return clientSituation;
    }

    public long getServerSituation() {
        return serverSituation;
    }

    public String getApplicationID() {
        return applicationID;
    }

    public Long getGoogleServicesVersion() {
        return googleServicesVersion;
    }

    public boolean isSyncNeeded() {
        return clientSituation > serverSituation;
    }

    public int getWhenActivated() {
        return isActive() ? ContextUtils.VIEW_VISIBLE : ContextUtils.VIEW_GONE;
    }

    public int getWhenNotActivated() {
        return isActive() ? ContextUtils.VIEW_GONE : ContextUtils.VIEW_VISIBLE;
    }

    @Override
    @NotNull
    public String toString() {
        return "ActivationState{" +
                "active=" + active +
                ", notExpired=" + notExpired +
                ", keyPresent=" + (key != null) +
                ", expirationDate=" + expirationDate +
                ", clientSituation=" + clientSituation +
                ", serverSituation=" + serverSituation +
                '}';
    }
}
