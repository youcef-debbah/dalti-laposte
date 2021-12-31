package com.dalti.laposte.core.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.dalti.laposte.R;
import com.dalti.laposte.core.util.QueueUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

import dz.jsoftware95.queue.api.AlarmData;
import dz.jsoftware95.queue.api.AlarmInfo;
import dz.jsoftware95.queue.common.SmsState;
import dz.jsoftware95.silverbox.android.backend.VisualItem;
import dz.jsoftware95.silverbox.android.middleware.TimeUtils;

@Entity(tableName = AdminAlarm.TABLE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdminAlarm implements VisualItem, Serializable, AlarmData {
    public static final int PAGE_SIZE = 64;
    public static final String TABLE_NAME = "admin_alarm";
    public static final String ID = "admin_alarm_id";

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ID)
    private long id;

    private String identifier;

    private Boolean enabled;
    private Long creationTime;
    private Long lastLaunch;

    private Long progress;
    private Integer ticket;
    private Long duration;
    private Integer queue;
    private Integer liquidity;
    private String phone;
    private Integer lastSendingState;
    private Long lastSendingTime;
    private Long sentMessagesCount;

    public AdminAlarm() {
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Long creationTime) {
        this.creationTime = creationTime;
    }

    public Long getLastLaunch() {
        return lastLaunch;
    }

    public void setLastLaunch(Long lastLaunch) {
        this.lastLaunch = lastLaunch;
    }

    @Override
    public Long getProgress() {
        return progress;
    }

    public void setProgress(Long progress) {
        this.progress = progress;
    }

    @Override
    public Integer getTicket() {
        return ticket;
    }

    public void setTicket(Integer ticket) {
        this.ticket = ticket;
    }

    @Override
    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    @Override
    public Integer getQueue() {
        return queue;
    }

    public void setQueue(Integer queue) {
        this.queue = queue;
    }

    @Override
    public Integer getLiquidity() {
        return liquidity;
    }

    public void setLiquidity(Integer liquidity) {
        this.liquidity = liquidity;
    }

    @Override
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getLastSendingState() {
        return lastSendingState;
    }

    public void setLastSendingState(Integer lastSendingState) {
        this.lastSendingState = lastSendingState;
    }

    public Long getLastSendingTime() {
        return lastSendingTime;
    }

    public void setLastSendingTime(Long lastSendingTime) {
        this.lastSendingTime = lastSendingTime;
    }

    public Long getSentMessagesCount() {
        return sentMessagesCount;
    }

    public void setSentMessagesCount(Long sentMessagesCount) {
        this.sentMessagesCount = sentMessagesCount;
    }

    @Override
    public Long getLastUpdate() {
        return creationTime;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    @NotNull
    public String toString() {
        return "AdminAlarm{" +
                "progress=" + progress +
                ", ticket=" + ticket +
                ", phone='" + phone + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdminAlarm that = (AdminAlarm) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return idHashcode();
    }

    @Override
    public boolean areContentsTheSame(@NonNull VisualItem o) {
        if (this == o) return true;
        if (getClass() != o.getClass()) return false;
        AdminAlarm that = (AdminAlarm) o;
        return isSimilarTo(that)
                && Objects.equals(getEnabled(), that.getEnabled())
                && Objects.equals(getCreationTime(), that.getCreationTime())
                && Objects.equals(getLastLaunch(), that.getLastLaunch())
                && Objects.equals(getLastSendingState(), that.getLastSendingState())
                && Objects.equals(getLastSendingTime(), that.getLastSendingTime())
                && Objects.equals(getSentMessagesCount(), that.getSentMessagesCount())
                ;
    }

    public AlarmInfo getInfo() {
        AlarmInfo info = new AlarmInfo();
        info.setEnabled(getEnabled());
        info.setProgress(getProgress());
        info.setTicket(getTicket());
        info.setDuration(getDuration());
        info.setQueue(getQueue());
        info.setLiquidity(getLiquidity());
        info.setPhone(getPhone());
        return info;
    }

    public String getFormattedDuration() {
        return QueueUtils.formatAsDurationOfSecMin(duration);
    }

    public String getFormattedCreationTime() {
        return TimeUtils.formatAsDateTime(creationTime);
    }

    public String getFormattedLastLaunch() {
        return QueueUtils.formatAsTimeNewLineDate(lastLaunch);
    }

    public String getFormattedLastSendingTime() {
        return QueueUtils.formatAsTimeNewLineDate(lastSendingTime);
    }

    public String getFormattedLastSendingState() {
        return QueueUtils.getString(getLastSendingStateStringRes(lastSendingState));
    }

    public String getFormattedLiquidity() {
        return QueueUtils.getString(liquidity, R.array.alarm_liquidity_options);
    }

    private static int getLastSendingStateStringRes(Integer state) {
        if (state == null)
            return R.string.unknown_symbol;
        else if (state == SmsState.SENT_SUCCESSFULLY)
            return R.string.sent_successfully;
        else if (state == SmsState.CANCELED_BY_CLIENT_CONFIRMATION)
            return R.string.canceled_by_client_confirmation;
        else if (state == SmsState.CANCELED_BY_CLIENT_UNREGISTRATION)
            return R.string.canceled_by_client_unregistration;
        else if (state == SmsState.CANCELED_NO_OPERATORS)
            return R.string.canceled_no_more_operators;
        else if (state == SmsState.CANCELED_OPERATORS_FAILED)
            return R.string.canceled_operators_failed;
        else if (state == SmsState.CANCELED_AUTOMATICALLY)
            return R.string.canceled_automatically;
        else
            return R.string.unknown_state;
    }
}