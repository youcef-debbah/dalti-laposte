package com.dalti.laposte.core.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import dz.jsoftware95.silverbox.android.backend.Item;

@Entity(tableName = LoggedEvent.NAME)
public class LoggedEvent implements Item {

    public static final String NAME = "logged_event";
    public static final String ID = "logged_event_id";

    @PrimaryKey
    @ColumnInfo(name = ID)
    private long id;
    private String name;
    private String parameters;

    public LoggedEvent() {
    }

    @Ignore
    public LoggedEvent(long id, String name, String parameters) {
        this.id = id;
        this.name = name;
        this.parameters = parameters;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    @Override
    public boolean equals(Object other) {
        ensurePersisted();
        if (this == other)
            return true;
        if (other == null || getClass() != other.getClass())
            return false;
        LoggedEvent otherItem = (LoggedEvent) other;

        return id == otherItem.id;
    }

    @Override
    public int hashCode() {
        return idHashcode();
    }

    @Override
    @NotNull
    public String toString() {
        return "LoggedEvent{" +
                "id=" + id +
                ", name=" + name +
                ", parameters=" + parameters +
                '}';
    }
}
