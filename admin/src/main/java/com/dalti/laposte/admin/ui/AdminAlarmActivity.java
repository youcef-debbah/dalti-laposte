package com.dalti.laposte.admin.ui;

import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LiveData;

import com.dalti.laposte.admin.BR;
import com.dalti.laposte.admin.R;
import com.dalti.laposte.admin.repositories.AdminAlarmsListRepository;
import com.dalti.laposte.admin.repositories.AdminAlarmsRepository;
import com.dalti.laposte.core.entity.AdminAlarm;
import com.dalti.laposte.core.repositories.LoadedProgress;
import com.dalti.laposte.core.entity.Progress;
import com.dalti.laposte.core.repositories.ProgressRepository;
import com.dalti.laposte.core.ui.AbstractQueueActivity;
import com.dalti.laposte.core.util.QueueUtils;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import dz.jsoftware95.queue.common.Estimator;
import dz.jsoftware95.queue.common.GlobalUtil;
import dz.jsoftware95.silverbox.android.backend.Item;
import dz.jsoftware95.silverbox.android.common.Assert;
import dz.jsoftware95.silverbox.android.middleware.BindingUtil;
import dz.jsoftware95.silverbox.android.middleware.ContextUtils;

@AndroidEntryPoint
public class AdminAlarmActivity extends AbstractQueueActivity {

    @Inject
    AdminAlarmsListRepository listRepository;

    @Inject
    AdminAlarmsRepository repository;

    @Inject
    ClipboardManager clipboardManager;

    @Inject
    ProgressRepository progressRepository;

    private long alarmID = Item.AUTO_ID;
    private long progressID = Item.AUTO_ID;
    private LoadedProgress currentLoadedProgress;
    private LiveData<LoadedProgress> progressData;
    private LiveData<AdminAlarm> alarm;

    @Override
    protected Integer getActionBarID() {
        return R.id.action_bar;
    }

    @Override
    protected void setupLayout(Bundle savedState) {
        setProgress(getLongExtra(Progress.ID, Item.AUTO_ID));
        setAlarm(getLongExtra(AdminAlarm.ID, Item.AUTO_ID));
        ViewDataBinding binding = BindingUtil.setContentView(this, R.layout.activity_admin_alarm);
        binding.setVariable(BR.activity, this);
    }

    private void setAlarm(long alarm) {
        Assert.that(alarm > Item.AUTO_ID);
        alarmID = alarm;
        this.alarm = repository.getLoadedData(alarm);
    }

    private void setProgress(long progress) {
        Assert.that(progress > Item.AUTO_ID);
        progressID = progress;
        progressData = progressRepository.getLoadedData(progress);
        progressData.observe(this, this::setLoadedProgress);
    }

    public LiveData<LoadedProgress> getProgressData() {
        return progressData;
    }

    public long getProgressID() {
        return progressID;
    }

    public long getAlarmID() {
        return alarmID;
    }

    public LiveData<AdminAlarm> getAlarm() {
        return alarm;
    }

    public void deleteAlarm(View v) {
        listRepository.deleteAdminAlarm(alarmID);
        finish();
    }

    public void cloneAlarm(View v) {
        startActivity(newAlarmFormIntent());
    }

    public void editAlarm(View v) {
        Intent intent = newAlarmFormIntent();
        intent.putExtra(AdminAlarmFormActivity.KEY_EDIT_MODE, true);
        startActivity(intent);
    }

    @NotNull
    public Intent newAlarmFormIntent() {
        Intent intent = new Intent(this, AdminAlarmFormActivity.class);
        intent.putExtra(AdminAlarm.ID, alarmID);
        intent.putExtra(Progress.ID, progressID);
        return intent;
    }

    public void sendSms(AdminAlarm alarm) {
        if (alarm != null) {
            LoadedProgress loadedProgress = currentLoadedProgress;
            int current = loadedProgress != null && loadedProgress.getCurrentToken() != null ? loadedProgress.getCurrentToken() : alarm.getTicket();//check
            Long avg = loadedProgress != null ? loadedProgress.getAverageServingTime() : null;
            String description = loadedProgress != null ? loadedProgress.getServiceDescription() : null;
            String text = GlobalUtil.getSmsNotificationText(description, current, alarm.getTicket(), Estimator.calcRemainingTime(avg, current, alarm.getTicket()));//check
            startActivity(ContextUtils.getSmsIntent(alarm.getPhone(), text));
        }
    }

    public void copyPhone(String phone) {
        QueueUtils.copyToClipboard(clipboardManager, phone, R.string.phone_number);
    }

    private void setLoadedProgress(LoadedProgress loadedProgress) {
        currentLoadedProgress = loadedProgress;
    }

    public void setEnabled(boolean enabled) {
        listRepository.updateAlarm(alarmID, alarm -> {
            if (!Objects.equals(alarm.getEnabled(), enabled)) {
                alarm.setEnabled(enabled);
                return true;
            } else
                return false;
        });
    }

    public String getNamespace() {
        return "admin_alarm_activity";
    }
}
