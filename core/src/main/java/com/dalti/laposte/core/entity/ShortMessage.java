package com.dalti.laposte.core.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import com.dalti.laposte.R;
import com.dalti.laposte.core.repositories.SmsRepository;

import dz.jsoftware95.silverbox.android.backend.VisualItem;
import dz.jsoftware95.silverbox.android.middleware.TimeUtils;

@Entity(tableName = ShortMessage.TABLE_NAME)
public class ShortMessage implements VisualItem {
    public static final int PAGE_SIZE = 64;
    public static final String TABLE_NAME = "short_message";
    public static final String ID = "short_message_id";

    public static final int SENDING_STATE = Integer.MAX_VALUE;
    public static final int NULL_STATE = 0;
    public static final int OK_STATE = -1;
    public static final int LOCAL_ERROR = -2;

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ID)
    private long id;
    private String phone;
    private String smsToken;
    private String textContent;
    private int parts;
    private int state;
    private long creationTime;
    private Long confirmationTime;
    private Long deliveryTime;

    public ShortMessage() {
    }

    @Ignore
    public ShortMessage(long id, long time, String phone, String smsToken, String textContent, int parts) {
        this.id = id;
        this.creationTime = time;
        this.phone = phone;
        this.smsToken = smsToken;
        this.textContent = textContent;
        this.parts = parts;
        this.state = SENDING_STATE;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSmsToken() {
        return smsToken;
    }

    public void setSmsToken(String smsToken) {
        this.smsToken = smsToken;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public int getParts() {
        return parts;
    }

    public void setParts(int parts) {
        this.parts = parts;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public Long getConfirmationTime() {
        return confirmationTime;
    }

    public void setConfirmationTime(Long confirmationTime) {
        this.confirmationTime = confirmationTime;
    }

    public Long getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(Long deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    @Override
    public boolean equals(Object o) {
        ensurePersisted();
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ShortMessage entity = (ShortMessage) o;
        return id == entity.id;
    }

    @Override
    public int hashCode() {
        return idHashcode();
    }

    @Override
    public boolean areContentsTheSame(@NonNull @NotNull VisualItem other) {
        if (this == other) return true;
        if (!(other instanceof ShortMessage)) return false;
        ShortMessage message = (ShortMessage) other;
        return getParts() == message.getParts() &&
                getState() == message.getState() &&
                getCreationTime() == message.getCreationTime() &&
                Objects.equals(getPhone(), message.getPhone()) &&
                Objects.equals(getSmsToken(), message.getSmsToken()) &&
                Objects.equals(getTextContent(), message.getTextContent()) &&
                Objects.equals(getConfirmationTime(), message.getConfirmationTime()) &&
                Objects.equals(getDeliveryTime(), message.getDeliveryTime());
    }

    public int getIcon() {
        if (state == SENDING_STATE)
            return R.drawable.ic_baseline_pending_24;
        else if (state == OK_STATE)
            return R.drawable.ic_baseline_sms_24;
        else
            return R.drawable.ic_baseline_sms_failed_24;
    }

    public int getIconColor() {
        if (state == SENDING_STATE)
            return R.color.on_surface_color_selector;
        else if (state == OK_STATE)
            return R.color.primary_color_selector;
        else
            return R.color.secondary_color_selector;
    }

    public String getFormattedDate() {
        return TimeUtils.formatAsDate(creationTime);
    }

    public String getFormattedCreationTime() {
        return TimeUtils.formatAsTime(creationTime);
    }

    public String getFormattedConfirmationTime() {
        return TimeUtils.formatAsTime(confirmationTime);
    }

    public String getFormattedDeliveryTime() {
        return TimeUtils.formatAsTime(deliveryTime);
    }

    public String getStateOutcome() {
        return SmsRepository.getOutcome(state);
    }
}
