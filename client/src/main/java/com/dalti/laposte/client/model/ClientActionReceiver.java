package com.dalti.laposte.client.model;

import android.content.Context;
import android.content.Intent;

import com.dalti.laposte.core.model.BasicActionReceiver;
import com.dalti.laposte.core.repositories.DashboardRepository;
import com.dalti.laposte.core.repositories.Event;
import com.dalti.laposte.core.repositories.NotificationUtils;
import com.dalti.laposte.core.repositories.Progress;
import com.dalti.laposte.core.repositories.SmsRepository;
import com.dalti.laposte.core.repositories.Teller;
import com.dalti.laposte.core.repositories.TurnAlarm;
import com.dalti.laposte.core.ui.Request;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import dz.jsoftware95.silverbox.android.backend.Item;

@AndroidEntryPoint
public class ClientActionReceiver extends BasicActionReceiver {

    @Inject
    DashboardRepository dashboardRepository;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (context != null && intent != null) {
            String action = intent.getStringExtra(BasicActionReceiver.ACTION_KEY);
            long alarmID = intent.getLongExtra(TurnAlarm.ID, Item.AUTO_ID);
            int notificationID = intent.getIntExtra(NotificationUtils.KEY_NOTIFICATION_ID, NotificationUtils.NOTIFICATION_ID_DEFAULT);
            String notificationTag = intent.getStringExtra(NotificationUtils.KEY_NOTIFICATION_TAG);

            boolean alarmCleared = Request.ALARM_CLEARED.name().equals(action);
            boolean alarmDismissed = Request.DISMISS_ALARM.name().equals(action);
            boolean alarmSnoozed = Request.SNOOZE_ALARM.name().equals(action);

            if (alarmCleared || alarmDismissed) {
                long progressID = intent.getLongExtra(Progress.ID, Item.AUTO_ID);
                if (progressID > Item.AUTO_ID) {
                    dashboardRepository.setTicket(null, progressID);
                    Teller.logClearTicket(progressID, alarmCleared ? Event.Trigger.CLEAR_NOTIFICATION : Event.Trigger.CLICK_NOTIFICATION_ACTION);
                }
            }

            if (alarmDismissed || alarmSnoozed) {
                NotificationUtils.cancelNotification(context, notificationTag, notificationID);
                alarmCleared = true;
            }

            if (alarmCleared)
                SmsRepository.cancelByClientConfirmation(intent.getStringExtra(NotificationUtils.KEY_NOTIFICATION_SMS_TOKEN));

            if (alarmSnoozed)
                Teller.log(Event.SnoozeAlarm.NAME);
        }
    }
}
