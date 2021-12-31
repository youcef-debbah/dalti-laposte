package com.dalti.laposte.core.entity;

import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.dalti.laposte.R;
import com.dalti.laposte.core.repositories.AppConfig;
import com.dalti.laposte.core.repositories.LongPreference;
import com.dalti.laposte.core.ui.AbstractQueueApplication;
import com.dalti.laposte.core.util.QueueUtils;

import java.util.concurrent.TimeUnit;

import dz.jsoftware95.queue.common.GlobalUtil;
import dz.jsoftware95.queue.common.PostOfficeAvailability;
import dz.jsoftware95.silverbox.android.backend.Item;
import dz.jsoftware95.silverbox.android.common.StringUtil;
import dz.jsoftware95.silverbox.android.middleware.ContextUtils;
import dz.jsoftware95.silverbox.android.middleware.TimeUtils;

@Entity(tableName = TurnAlarm.TABLE_NAME)
public class TurnAlarm implements Item {
    public static final String TABLE_NAME = "turn_alarm";
    public static final String ID = "turn_alarm_id";
    public static final String BEFOREHAND_DURATION = "duration";
    public static final String MIN_LIQUIDITY = "liquidity";
    public static final String MAX_QUEUE_LENGTH = "queue";
    public static final String ENABLED = "enabled";
    public static final String VIBRATE = "vibrate";
    public static final String PRIORITY = "priority";
    public static final String RINGTONE = "ringtone";
    public static final String SNOOZE = "snooze";
    public static final String PHONE = "phone";

    @ArrayRes
    public static final int LIQUIDITY_OPTIONS = R.array.alarm_liquidity_options;
    @ArrayRes
    public static final int PRIORITY_OPTIONS = R.array.alarm_priority_options;
    @ArrayRes
    public static final int RINGTONE_OPTIONS = R.array.alarm_ringtone_options;

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ID)
    private long id;
    @ColumnInfo(name = BEFOREHAND_DURATION)
    private long beforehandDuration;
    @ColumnInfo(name = MIN_LIQUIDITY)
    private int minLiquidity;
    @ColumnInfo(name = MAX_QUEUE_LENGTH)
    private int maxQueueLength;
    @ColumnInfo(name = ENABLED)
    private boolean enabled;
    @ColumnInfo(name = VIBRATE)
    private boolean vibrate;
    @ColumnInfo(name = PRIORITY)
    private int priority;
    @ColumnInfo(name = RINGTONE)
    private int ringtone;
    @ColumnInfo(name = SNOOZE)
    private int snooze;
    @ColumnInfo(name = PHONE)
    private String phone;

    private long creationTime;
    private long lastUpdate;

    public TurnAlarm() {
    }

    @Ignore
    public TurnAlarm(long beforehandDurationInMinutes) {
        AppConfig appConfig = AppConfig.getInstance();

        long theoreticalQueueLength = beforehandDurationInMinutes * appConfig.getAsInt(Settings.DEFAULT_QUEUE_LENGTH_PER_MINUTE);
        this.maxQueueLength = (int) Math.min(theoreticalQueueLength, appConfig.getLong(Settings.MAX_QUEUE_LENGTH_INPUT));
        this.beforehandDuration = TimeUnit.MINUTES.toMillis(beforehandDurationInMinutes);

        long now = System.currentTimeMillis();
        this.creationTime = now;
        this.lastUpdate = now;

        this.minLiquidity = appConfig.getAsInt(Settings.DEFAULT_MIN_LIQUIDITY_VALUE);
        this.enabled = true;
        this.vibrate = true;
        this.priority = alarmPriority(appConfig, null);
        this.ringtone = 0;
        this.snooze = appConfig.getAsInt(Settings.DEFAULT_SNOOZE_VALUE);
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public long getBeforehandDuration() {
        return beforehandDuration;
    }

    public String getBeforehandDurationAsText() {
        AbstractQueueApplication context = AbstractQueueApplication.requireInstance();
        int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(beforehandDuration);
        return context.getResources().getQuantityString(R.plurals.minutes, minutes, minutes);
    }

    public void setBeforehandDuration(long beforehandDuration) {
        this.beforehandDuration = beforehandDuration;
    }

    public int getMinLiquidity() {
        return minLiquidity;
    }

    public String getMinLiquidityAsText() {
        return QueueUtils.getString(minLiquidity, LIQUIDITY_OPTIONS);
    }

    public String getMinLiquidityState() {
        return GlobalUtil.getElement(minLiquidity + 1, PostOfficeAvailability.LiquidityState.values(), PostOfficeAvailability.LiquidityState.LIQUIDITY_STATE_UNKNOWN).name();
    }

    public void setMinLiquidity(int minLiquidity) {
        this.minLiquidity = minLiquidity;
    }

    public int getMaxQueueLength() {
        return maxQueueLength;
    }

    public void setMaxQueueLength(int maxQueueLength) {
        this.maxQueueLength = maxQueueLength;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isVibrate() {
        return vibrate;
    }

    public void setVibrate(boolean vibrate) {
        this.vibrate = vibrate;
    }

    public int getPriority() {
        return priority;
    }

    public String getPriorityAsText() {
        return QueueUtils.getString(priority, PRIORITY_OPTIONS);
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getRingtone() {
        return ringtone;
    }

    public static int alarmPriority(AppConfig appConfig, Integer priority) {
        return priority != null ? priority : appConfig.getAsInt(Settings.DEFAULT_PRIORITY_VALUE);
    }

    public static int notificationPriority(int priority) {
        return GlobalUtil.bound(priority - 2, NotificationCompat.PRIORITY_MIN, NotificationCompat.PRIORITY_HIGH);
    }

    public static Integer ringtoneRes(Integer ringtone, int priority) {
        if (ringtone != null && notificationPriority(priority) > NotificationCompat.PRIORITY_LOW)
            switch (ringtone) {
                case 1:
                    return R.raw.sound_bell;
                case 2:
                    return R.raw.sound_bubble_pop_up;
                case 3:
                    return R.raw.sound_musical_reveal;
                case 4:
                    return R.raw.sound_poke;
                case 5:
                    return R.raw.sound_positive;
                case 6:
                    return R.raw.sound_reveal;
                case 7:
                    return R.raw.sound_start;
                case 8:
                    return R.raw.sound_uplifting;
            }

        return null;
    }

    public String getRingtoneAsText() {
        return QueueUtils.getString(ringtone, RINGTONE_OPTIONS);
    }

    public void setRingtone(int ringtone) {
        this.ringtone = ringtone;
    }

    public int getSnooze() {
        return snooze;
    }

    public void setSnooze(int snooze) {
        this.snooze = snooze;
    }

    public String getSnoozeAsText() {
        return QueueUtils.formatAsDurationOfSecMin((long) snooze);
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer visibleWhenPhone() {
        return StringUtil.isBlank(phone) ? ContextUtils.VIEW_GONE : ContextUtils.VIEW_VISIBLE;
    }

    public Integer visibleWhenNotPhone() {
        return StringUtil.isBlank(phone) ? ContextUtils.VIEW_VISIBLE : ContextUtils.VIEW_GONE;
    }

    @Override
    @NonNull
    public String toString() {
        return "TurnAlarm{" +
                "id=" + id +
                ", beforehandTime=" + beforehandDuration +
                ", enabled=" + enabled +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        ensurePersisted();
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TurnAlarm entity = (TurnAlarm) o;

        return id == entity.id;
    }

    @Override
    public int hashCode() {
        return idHashcode();
    }

    public enum Settings implements LongPreference {
        MAX_BEFOREHAND_INPUT(90),
        MIN_BEFOREHAND_INPUT(5),
        DEFAULT_BEFOREHAND_INPUT(30),

        MAX_QUEUE_LENGTH_INPUT(300),
        MIN_QUEUE_LENGTH_INPUT(3),
        DEFAULT_QUEUE_LENGTH_PER_MINUTE(3),

        MAX_SNOOZE_INPUT(60),
        MIN_SNOOZE_INPUT(5),
        DEFAULT_SNOOZE_VALUE(5 * TimeUtils.ONE_MINUTE_MILLIS),

        DEFAULT_PRIORITY_VALUE(3),
        DEFAULT_MIN_LIQUIDITY_VALUE(2);

        private final long defaultLong;

        Settings(long defaultLong) {
            this.defaultLong = defaultLong;
        }

        @Override
        public long getDefaultLong() {
            return defaultLong;
        }

        @Override
        public int getDefaultInteger() {
            return (int) defaultLong;
        }
    }

    public String getNamespace() {
        return "turn_alarm_element";
    }

}
