package com.dalti.laposte.core.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.dalti.laposte.R;
import com.dalti.laposte.core.repositories.Teller;
import com.dalti.laposte.core.util.QueueUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

import dz.jsoftware95.queue.common.GlobalConf;
import dz.jsoftware95.silverbox.android.backend.Item;
import dz.jsoftware95.silverbox.android.backend.VisualItem;
import dz.jsoftware95.silverbox.android.common.Check;
import dz.jsoftware95.silverbox.android.common.StringUtil;
import dz.jsoftware95.silverbox.android.middleware.TimeUtils;

@Entity(tableName = Activation.TABLE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Activation implements VisualItem, Serializable {
    public static final int PAGE_SIZE = 64;
    public static final String TABLE_NAME = "activation";
    public static final String ID = "activation_id";
    public static final String CODE = "code";

    @PrimaryKey
    @ColumnInfo(name = ID)
    private long id;

    @ColumnInfo(name = CODE, index = true)
    private String code;

    private long duration;

    private Long activationDate;
    private Long expirationDate;

    private Integer applicationVersion;
    private Integer androidVersion;
    private Long googleVersion;

    public Activation() {
    }

    @Ignore
    public Activation(long id, String code, long duration) {
        this.id = id;
        this.code = Objects.requireNonNull(code);
        this.duration = duration;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public Long getActivationDate() {
        return activationDate;
    }

    public void setActivationDate(Long activationDate) {
        this.activationDate = activationDate;
    }

    public Long getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Long expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Integer getApplicationVersion() {
        return applicationVersion;
    }

    public void setApplicationVersion(Integer applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    public Integer getAndroidVersion() {
        return androidVersion;
    }

    public void setAndroidVersion(Integer androidVersion) {
        this.androidVersion = androidVersion;
    }

    public Long getGoogleVersion() {
        return googleVersion;
    }

    public void setGoogleVersion(Long googleVersion) {
        this.googleVersion = googleVersion;
    }

    @Override
    public boolean isValid() {
        return Check.matches(GlobalConf.ACTIVATION_CODE_PATTERN, code)
                && Check.sameNullability(activationDate, expirationDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Activation that = (Activation) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return idHashcode();
    }

    @Override
    @NotNull
    public String toString() {
        return "Activation{" +
                "id=" + id +
                ", code='" + code + '\'' +
                '}';
    }

    @Override
    public boolean areContentsTheSame(@NonNull VisualItem o) {
        if (this == o) return true;
        if (getClass() != o.getClass()) return false;
        Activation that = (Activation) o;
        return id == that.id &&
                duration == that.duration &&
                Objects.equals(code, that.code) &&
                Objects.equals(activationDate, that.activationDate) &&
                Objects.equals(expirationDate, that.expirationDate) &&
                Objects.equals(applicationVersion, that.applicationVersion) &&
                Objects.equals(androidVersion, that.androidVersion) &&
                Objects.equals(googleVersion, that.googleVersion);
    }

    public static Activation parse(Map<String, String> data) {
        Long id = StringUtil.parseLong(data.get(GlobalConf.ACTIVATION_ID));
        String code = data.get(GlobalConf.ACTIVATION_CODE);
        Long duration = StringUtil.parseLong(data.get(GlobalConf.ACTIVATION_DURATION));
        if (id != null && code != null && duration != null) {
            Activation activation = new Activation(id, code, duration);
            activation.setActivationDate(StringUtil.parseLong(data.get(GlobalConf.ACTIVATION_DATE)));
            activation.setExpirationDate(StringUtil.parseLong(data.get(GlobalConf.EXPIRATION_DATE)));
            activation.setApplicationVersion(StringUtil.parseInteger(data.get(GlobalConf.APPLICATION_VERSION)));
            activation.setAndroidVersion(StringUtil.parseInteger(data.get(GlobalConf.ANDROID_VERSION)));
            activation.setGoogleVersion(StringUtil.parseLong(data.get(GlobalConf.GOOGLE_VERSION)));
            return activation;
        } else {
            Teller.logUnexpectedCondition("could not parse activation with incomplete info: " +
                    "id=" + id + ", code='" + code + "', duration=" + duration);
            return null;
        }
    }

    public Integer getActivationDateIcon() {
        if (activationDate == null)
            return null;
        else
            return Objects.equals(TimeUtils.getDays(activationDate), TimeUtils.getDays(System.currentTimeMillis())) ?
                    R.drawable.ic_baseline_calendar_today_24 : R.drawable.ic_baseline_date_range_24;
    }

    public String getCodeLabel() {
        if (id == Item.AUTO_ID || StringUtil.isBlank(code))
            return null;
        else {
            Long days = TimeUtils.getDays(duration);
            if (days != null && days > 1)
                return QueueUtils.getString(R.string.activation_code_label, code, days);
            else
                return code;
        }
    }

    public String getApplicationVersionAsText() {
        if (applicationVersion == null)
            return null;
        else {
            String version = String.valueOf(Math.abs(applicationVersion));
            if (applicationVersion < 0)
                return version + " (test version)";
            else
                return version;
        }
    }

    public String getAndroidVersionAsText() {
        String version = StringUtil.androidVersion(androidVersion);
        return version != null ? version + " (API " + androidVersion + ")" : "API " + androidVersion;
    }

    public String getNamespace() {
        return "activation_element";
    }
}