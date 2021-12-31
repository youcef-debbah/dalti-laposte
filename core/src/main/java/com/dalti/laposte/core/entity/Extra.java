package com.dalti.laposte.core.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.dalti.laposte.core.repositories.Property;

import org.jetbrains.annotations.NotNull;

import dz.jsoftware95.silverbox.android.backend.Item;
import dz.jsoftware95.silverbox.android.common.Check;

@Entity(tableName = Extra.TABLE_NAME)
public class Extra implements Item {
    public static final String TABLE_NAME = "extra";
    public static final String ID = "extra_id";

    @PrimaryKey
    @ColumnInfo(name = ID)
    private long id;
    private String value;

    public Extra() {
    }

    @Ignore
    public Extra(long id, String value) {
        this.id = id;
        this.value = Check.nonNull(value);
    }

    @Ignore
    public Extra(Property property, String value) {
        this(property.key(), value);
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public long getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean isValid() {
        return value != null;
    }

    @Override
    public boolean equals(Object o) {
        ensurePersisted();
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Extra extra = (Extra) o;

        return id == extra.id;
    }

    @Override
    public int hashCode() {
        return idHashcode();
    }

    @Override
    @NotNull
    public String toString() {
        return "Extra{" +
                "id=" + id +
                ", value='" + value + '\'' +
                '}';
    }
}
