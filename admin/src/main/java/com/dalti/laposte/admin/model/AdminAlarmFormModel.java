package com.dalti.laposte.admin.model;

import android.app.Application;

import com.dalti.laposte.admin.R;
import com.dalti.laposte.admin.repositories.AdminAlarmsListRepository;
import com.dalti.laposte.core.repositories.AppConfig;
import com.dalti.laposte.core.repositories.BooleanSetting;
import com.dalti.laposte.core.repositories.LongSetting;
import com.dalti.laposte.core.entity.TurnAlarm;
import com.dalti.laposte.core.util.QueueUtils;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dz.jsoftware95.queue.api.AlarmInfo;
import dz.jsoftware95.queue.common.GlobalConf;
import dz.jsoftware95.silverbox.android.backend.Item;
import dz.jsoftware95.silverbox.android.common.Check;
import dz.jsoftware95.silverbox.android.common.StringUtil;
import dz.jsoftware95.silverbox.android.middleware.RepositoryModel;

@HiltViewModel
public class AdminAlarmFormModel extends RepositoryModel<AdminAlarmsListRepository> {

    private long progressID;
    private Long alarmID;

    // required
    private String phone;
    private int ticket;
    private int duration;

    // optional
    private int queue;
    private int liquidity;
    private boolean confirm;
    private boolean enabled;
    private boolean editMode;

    @Inject
    public AdminAlarmFormModel(Application application, AdminAlarmsListRepository repository) {
        super(application, repository);
        AppConfig appConfig = AppConfig.getInstance();
        progressID = Item.AUTO_ID;
        alarmID = null;
        phone = "";
        ticket = 0;
        editMode = false;

        liquidity = appConfig.getAsInt(LongSetting.SMS_ALARM_MIN_LIQUIDITY_DEFAULT_INPUT);
        confirm = appConfig.get(BooleanSetting.ENABLE_SMS_ALARM_CONFIRMATION);
        enabled = appConfig.get(BooleanSetting.ENABLE_SMS_ALARM);
        duration = appConfig.getAsInt(LongSetting.SMS_ALARM_DURATION_DEFAULT_INPUT);
        queue = getAutoQueueValue(duration);
    }

    private void updateDefaultValues() {
        AppConfig appConfig = AppConfig.getInstance();
        appConfig.put(LongSetting.SMS_ALARM_MIN_LIQUIDITY_DEFAULT_INPUT, liquidity);
        appConfig.put(BooleanSetting.ENABLE_SMS_ALARM_CONFIRMATION, confirm);
        appConfig.put(BooleanSetting.ENABLE_SMS_ALARM, enabled);
        appConfig.put(LongSetting.SMS_ALARM_DURATION_DEFAULT_INPUT, duration);
    }

    public static int getAutoQueueValue(int duration) {
        AppConfig appConfig = AppConfig.getInstance();
        long theoreticalQueueLength = duration * appConfig.getAsInt(TurnAlarm.Settings.DEFAULT_QUEUE_LENGTH_PER_MINUTE);
        return (int) Math.min(theoreticalQueueLength, appConfig.getLong(TurnAlarm.Settings.MAX_QUEUE_LENGTH_INPUT));
    }

    public long getProgressID() {
        return progressID;
    }

    public void setProgressID(long progressID) {
        this.progressID = progressID;
    }

    public Long getAlarmID() {
        return alarmID;
    }

    public void setAlarmID(Long alarmID) {
        this.alarmID = alarmID;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getTicket() {
        return ticket;
    }

    public void setTicket(int ticket) {
        this.ticket = ticket;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Integer getQueue() {
        return queue;
    }

    public void setQueue(Integer queue) {
        this.queue = queue;
    }

    public int getLiquidity() {
        return liquidity;
    }

    public void setLiquidity(int liquidity) {
        this.liquidity = liquidity;
    }

    public boolean isConfirm() {
        return confirm;
    }

    public void setConfirm(boolean confirm) {
        this.confirm = confirm;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public boolean submit() {
        if (progressID <= Item.AUTO_ID) {
            QueueUtils.handleServiceMissing();
            return true;
        } else if (ticket <= 0) {
            QueueUtils.toast(R.string.client_ticket_needed);
            return true;
        } else if (StringUtil.isBlank(phone)) {
            QueueUtils.toast(R.string.client_phone_needed);
            return true;
        } else {
            String phoneNumber = QueueUtils.getString(R.string.phone_prefix) + phone;
            if (Check.matches(GlobalConf.PHONE_PATTERN, phoneNumber)) {
                updateDefaultValues();
                getRepository().saveAdminAlarm(getAlarmInfo(phoneNumber), confirm, isEditMode() ? alarmID : null);
                return false;
            } else {
                QueueUtils.toast(R.string.invalid_phone);
                return true;
            }
        }
    }

    @NotNull
    public AlarmInfo getAlarmInfo(String phoneNumber) {
        AlarmInfo info = new AlarmInfo();
        info.setPhone(phoneNumber);
        info.setProgress(progressID);
        info.setTicket(ticket);
        info.setDuration(TimeUnit.MINUTES.toMillis(duration));

        info.setLastUpdate(System.currentTimeMillis());
        info.setQueue(queue);
        info.setLiquidity(liquidity);
        info.setEnabled(enabled);
        return info;
    }
}
