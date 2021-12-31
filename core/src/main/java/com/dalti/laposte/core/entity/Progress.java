package com.dalti.laposte.core.entity;

import android.content.res.ColorStateList;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.dalti.laposte.R;
import com.dalti.laposte.core.repositories.AdminAction;
import com.dalti.laposte.core.util.QueueUtils;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import dz.jsoftware95.queue.common.Estimator;
import dz.jsoftware95.queue.common.GlobalConf;
import dz.jsoftware95.queue.common.IdentityManager;
import dz.jsoftware95.silverbox.android.backend.Item;
import dz.jsoftware95.silverbox.android.common.Check;
import dz.jsoftware95.silverbox.android.common.StringUtil;
import dz.jsoftware95.silverbox.android.middleware.ContextUtils;
import dz.jsoftware95.silverbox.android.observers.Chronological;

@Entity(
        tableName = Progress.TABLE_NAME,
        foreignKeys = @ForeignKey(entity = Service.class, parentColumns = Service.ID, childColumns = Progress.SERVICE_ID, onDelete = ForeignKey.CASCADE)
)
public class Progress implements Item, Chronological {
    public static final String TABLE_NAME = "progress";
    public static final String ID = "progress_id";
    public static final String SERVICE_ID = "service_id";

    @PrimaryKey
    @ColumnInfo(name = ID)
    private long id;
    @ColumnInfo(name = SERVICE_ID, index = true)
    private long serviceID;

    private Integer currentToken;
    private Integer waiting;
    private Long potentialTime;
    private Long timestamp;
    private String lastUpdater;

    private Integer ticket;
    private Long lastTicketUpdate;

    private Long remainingTime;
    private Long averageServingTime;

    public Progress() {
    }

    @Ignore
    public Progress(long id) {
        setId(id);
    }

    @Ignore
    public Progress(long id, Map<String, String> data) {
        this(id);
        update(data);
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
        this.serviceID = IdentityManager.getServiceID(id);
    }

    public long getServiceID() {
        return serviceID;
    }

    public void setServiceID(long serviceID) {
        this.serviceID = serviceID;
    }

    public Integer getCurrentToken() {
        return currentToken;
    }

    public void setCurrentToken(Integer currentToken) {
        this.currentToken = currentToken;
    }

    public int getCurrentTokenInt() {
        Integer currentToken = this.currentToken;
        return currentToken != null && currentToken > 0 ? currentToken : 0;
    }

    public Integer getWaiting() {
        return waiting;
    }

    public int getWaitingInt() {
        Integer waiting = this.waiting;
        return waiting != null && waiting > 0 ? waiting : 0;
    }

    public void setWaiting(Integer waiting) {
        this.waiting = waiting;
    }

    @Override
    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getLastUpdater() {
        return lastUpdater;
    }

    public void setLastUpdater(String lastUpdater) {
        this.lastUpdater = lastUpdater;
    }

    public Integer getTicket() {
        return ticket;
    }

    public void setTicket(Integer ticket) {
        this.ticket = ticket;
    }

    public Long getLastTicketUpdate() {
        return lastTicketUpdate;
    }

    public void setLastTicketUpdate(Long lastTicketUpdate) {
        this.lastTicketUpdate = lastTicketUpdate;
    }

    public Long getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(Long remainingTime) {
        this.remainingTime = remainingTime;
    }

    public Long getPotentialTime() {
        return potentialTime;
    }

    public void setPotentialTime(Long potentialTime) {
        this.potentialTime = potentialTime;
    }

    public Long getAverageServingTime() {
        return averageServingTime;
    }

    public void setAverageServingTime(Long averageServingTime) {
        this.averageServingTime = averageServingTime;
    }

    @Override
    public boolean isValid() {
        Integer currentToken = this.currentToken;
        Integer waiting = this.waiting;
        return (currentToken == null || currentToken >= 0)
                && (waiting == null || waiting >= 0)
                ;
    }

    public void update(AdminAction action) {
        action.apply(this);
        Check.isValid(this);
    }

    public boolean update(Map<String, String> data) {
        int rank = getRank();
        Long dataTimestamp = StringUtil.parseLong(data.get(GlobalConf.TIMESTAMP_PREFIX + rank));
        if (shouldUpdate(dataTimestamp)) {
            this.timestamp = dataTimestamp;
            this.lastUpdater = StringUtil.getString(data, GlobalConf.LAST_UPDATER_PREFIX + rank);
            this.currentToken = StringUtil.parsePositiveInteger(data.get(GlobalConf.CURRENT_TOKEN_PREFIX + rank));
            this.waiting = StringUtil.parsePositiveInteger(data.get(GlobalConf.WAITING_PREFIX + rank));
            this.potentialTime = StringUtil.parseLong(data.get(GlobalConf.POTENTIAL_WAIT_TIME_PREFIX + rank));
            this.averageServingTime = StringUtil.parseLong(data.get(GlobalConf.AVERAGE_SERVING_TIME_PREFIX + rank));

            Integer ranksCount = StringUtil.parsePositiveInteger(data.get(GlobalConf.PROGRESSES_COUNT));
            if (ranksCount != null && rank < ranksCount) {
                Integer ticket = StringUtil.parsePositiveInteger(data.get(GlobalConf.CURRENT_TICKET_PREFIX + rank));
                if (Objects.equals(ticket, this.ticket))
                    this.remainingTime = StringUtil.parseLong(data.get(GlobalConf.REMAINING_TIME_PREFIX + rank));
            }

            Check.isValid(this);
            return true;
        } else
            return false;
    }

    public int getRank() {
        return IdentityManager.getProgressRank(id);
    }

    private boolean shouldUpdate(Long dataTimestamp) {
        Long currentTimestamp = this.timestamp;
        return dataTimestamp == null || currentTimestamp == null
                || dataTimestamp >= currentTimestamp;
    }

    @Override
    public boolean equals(Object o) {
        ensurePersisted();
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Progress progress = (Progress) o;

        return id == progress.id;
    }

    @Override
    public int hashCode() {
        return idHashcode();
    }

    @NonNull
    @Override
    public String toString() {
        return "Progress#" + getRank() + "{" +
                "serviceID=" + serviceID +
                ", currentToken=" + currentToken +
                ", waiting=" + waiting +
                ", ticket=" + ticket +
                ", averageServingTime=" + averageServingTime +
                ", potentialTime=" + potentialTime +
                ", remainingTime=" + remainingTime +
                '}';
    }

    public static Map<Integer, Progress> asMap(List<Progress> progresses) {
        if (progresses != null && !progresses.isEmpty()) {
            Map<Integer, Progress> result = new HashMap<>(progresses.size());
            for (Progress progress : progresses)
                result.put(progress.getRank(), progress);
            return Collections.unmodifiableMap(result);
        } else
            return Collections.emptyMap();
    }

    // ui methods

    public CharSequence getCurrentTokenText() {
        return QueueUtils.formatNumber(currentToken);
    }

    public CharSequence getWaitingText() {
        return QueueUtils.formatNumber(waiting);
    }

    @DrawableRes
    public Integer getIcon() {
        return getProgressIcon(IdentityManager.getProgressRank(id));
    }

    @Nullable
    public static Integer getProgressIcon(int rank) {
        switch (rank) {
            case 0:
                return R.drawable.ic_padded_pleb_24;
            case 1:
                return R.drawable.ic_baseline_accessible_24;
            case 2:
                return R.drawable.ic_padded_businessman_24;
            default:
                return null;
        }
    }

    public Integer getTimestampVisibility() {
        return timestamp != null ? ContextUtils.VIEW_VISIBLE : ContextUtils.VIEW_GONE;
    }

    public Integer getDividerVisibility() {
        return IdentityManager.getProgressRank(id) > 0 ? ContextUtils.VIEW_VISIBLE : ContextUtils.VIEW_GONE;
    }

    public Integer getTicketInfoVisible() {
        return ticket != null ? ContextUtils.VIEW_VISIBLE : ContextUtils.VIEW_GONE;
    }

    public Integer getAddTicketButtonVisible() {
        return ticket == null ? ContextUtils.VIEW_VISIBLE : ContextUtils.VIEW_GONE;
    }

    private boolean hasRemainingTimeValue() {
        return remainingTime != null && remainingTime > 0 && remainingTime <= Estimator.MAX_ESTIMATION_VALUE;
    }

    @Nullable
    public Integer calcRemainingTime() {
        return hasRemainingTimeValue() ? remainingTime.intValue() : null;
    }

    public Integer calcPotentialTime() {
        if (IdentityManager.getProgressRank(id) == 0 && potentialTime != null
                && potentialTime > 0 && potentialTime <= Estimator.MAX_ESTIMATION_VALUE)
            return potentialTime.intValue();
        else
            return null;
    }

    public Integer calcRemainingTimeLabel() {
        if (remainingTime == null)
            return null;
        else if (remainingTime == Estimator.TICKET_TURN_PASSED)
            return R.string.turn_passed;
        else if (remainingTime == Estimator.TICKET_TURN_NOW)
            return R.string.turn_now;
        else if (remainingTime == Estimator.ESTIMATION_NOT_AVAILABLE)
            return R.string.estimation_not_available;
        else if (hasRemainingTimeValue())
            return R.string.remaining_time;
        else
            return null;
    }

    public Integer calcPotentialTimeLabel() {
        return calcPotentialTime() != null ? R.string.potential_waiting_time : null;
    }

    public ColorStateList getTicketCardColor() {
        return (hasOldTicket()) ?
                QueueUtils.getColorStateList(R.color.shadowed_surface_color_selector) : QueueUtils.getColorStateList(R.color.alternative_surface_color_selector);

    }

    public ColorStateList getTicketColor() {
        return hasOldTicket() ?
                QueueUtils.getColorStateList(R.color.on_surface_extra_light_color_selector) : QueueUtils.getColorStateList(R.color.on_alternative_surface_color_selector);

    }

    private boolean hasOldTicket() {
        return ticket != null && currentToken != null
                && ticket < currentToken - Estimator.MAX_TICKET_DELAY;
    }

    public String getLastUpdaterLabel() {
        return QueueUtils.getString(R.string.by_updater, lastUpdater != null ? lastUpdater : QueueUtils.getString(R.string.unknown_symbol));
    }

    public static Long getRemainingTime(Progress progress) {
        return progress == null ? null : progress.remainingTime;
    }
}
