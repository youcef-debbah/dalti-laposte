package com.dalti.laposte.core.repositories;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Objects;

import dz.jsoftware95.silverbox.android.backend.Item;

@Entity(tableName = StateEntry.TABLE_NAME)
public class StateEntry implements Item {
    public static final String TABLE_NAME = "state";
    public static final String ID = "state_id";

    public static final long CURRENT_SERVICE_ID = 2;
    public static final long CURRENT_SITUATION_VERSION = 3;

    @PrimaryKey
    @ColumnInfo(name = ID)
    private long id;

    private Long value;

    public StateEntry() {
    }

    @Ignore
    public StateEntry(long id, Long value) {
        this.id = id;
        this.value = value;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        StateEntry that = (StateEntry) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    @NonNull
    public String toString() {
        return "StateEntry{" +
                "id=" + id +
                ", value=" + value +
                '}';
    }
}
