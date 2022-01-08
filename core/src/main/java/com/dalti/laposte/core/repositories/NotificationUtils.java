package com.dalti.laposte.core.repositories;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.hilt.work.HiltWorker;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.dalti.laposte.R;
import com.dalti.laposte.core.entity.Progress;
import com.dalti.laposte.core.entity.TurnAlarm;
import com.dalti.laposte.core.model.BasicActionReceiver;
import com.dalti.laposte.core.ui.AbstractQueueApplication;
import com.dalti.laposte.core.ui.Request;
import com.dalti.laposte.core.util.QueueConfig;
import com.dalti.laposte.core.util.QueueUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import dz.jsoftware95.queue.common.Estimator;
import dz.jsoftware95.queue.common.GlobalConf;
import dz.jsoftware95.queue.common.GlobalUtil;
import dz.jsoftware95.queue.common.IdentityManager;
import dz.jsoftware95.queue.common.Predicate;
import dz.jsoftware95.silverbox.android.common.StringUtil;
import dz.jsoftware95.silverbox.android.middleware.ContextUtils;
import dz.jsoftware95.silverbox.android.middleware.TimeUtils;

public class NotificationUtils {

    public static final String NOTIFICATION_TAG_TURN_ALARM = "turn-alarm";
    public static final String NOTIFICATION_TAG_TURN_ALARM_TEST = "turn-alarm-test";
    public static final String NOTIFICATION_TAG_IDLE_ADMIN = "idle-admin";
    public static final String NOTIFICATION_TAG_UNKNOWN_AVAILABILITY = "unknown-availability";
    public static final int NOTIFICATION_ID_DEFAULT = StringUtil.hash(IdentityManager.getProgressID(1, 0));
    public static final String KEY_NOTIFICATION_ID = "notification_id";
    public static final String KEY_NOTIFICATION_TAG = "notification_tag";
    public static final String KEY_NOTIFICATION_SMS_TOKEN = "notification_sms_token";

    public static final long ALARM_NOTIFICATION_TTL = TimeUnit.MINUTES.toMillis(10);

    @WorkerThread
    public static void cancelLaunchedAlarms(Predicate<LaunchedTurnAlarm> predicate) {
        AppConfig appConfig = AppConfig.getInstance();
        Set<String> launchedAlarmsData = appConfig.get(SetSetting.LAUNCHED_TURN_ALARMS);
        if (GlobalUtil.notEmpty(launchedAlarmsData)) {
            Set<String> canceledAlarms = new HashSet<>(2);
            for (String alarmData : launchedAlarmsData) {
                LaunchedTurnAlarm launchedTurnAlarm = LaunchedTurnAlarm.decode(alarmData);
                if (launchedTurnAlarm != null && predicate.test(launchedTurnAlarm)) {
                    NotificationUtils.cancelTurnAlarmNotification(launchedTurnAlarm.getProgressID());
                    canceledAlarms.add(alarmData);
                }
            }
            appConfig.remove(SetSetting.LAUNCHED_TURN_ALARMS, canceledAlarms);
        }
    }

    @WorkerThread
    public static void handleTurnAlarmNotification(@Nullable Map<String, String> data) {
        if (data != null) {
            Long alarmProgressID = StringUtil.parseAsLong(data.get(GlobalConf.TURN_ALARM_PROGRESS_ID));
            if (alarmProgressID != null) {
                startTurnAlarmNotification(StringUtil.hash(alarmProgressID), NOTIFICATION_TAG_TURN_ALARM, alarmProgressID,
                        StringUtil.getString(data, GlobalConf.TURN_ALARM_SMS_TOKEN), StringUtil.parseAsLong(data.get(GlobalConf.TURN_ALARM_REMAINING_TIME)),
                        StringUtil.parseAsInteger(data.get(GlobalConf.TURN_ALARM_QUEUE_LENGTH)), StringUtil.getString(data, GlobalConf.TURN_ALARM_SERVICE_DESCRIPTION),
                        StringUtil.parseBoolean(data, GlobalConf.TURN_ALARM_VIBRATE), StringUtil.parseAsInteger(data.get(GlobalConf.TURN_ALARM_RINGTONE)),
                        StringUtil.parseAsInteger(data.get(GlobalConf.TURN_ALARM_PRIORITY)), StringUtil.parseAsLong(data.get(GlobalConf.TURN_ALARM_TARGET_TIME)));
            }
        }
    }

    public static void startTurnAlarmNotification(int notificationID, String notificationTag, Long progressID, String smsToken,
                                                  Long remainingTime, Integer queue, String serviceDescription,
                                                  Boolean vibrate, Integer ringtone, Integer priority, Long alarmTargetTime) {
        AbstractQueueApplication context = AbstractQueueApplication.getInstance();
        if (context != null) {
            Notification notification = NotificationUtils.newTurnAlarmNotification(context, progressID, smsToken,
                    notificationTag, notificationID, remainingTime, queue, serviceDescription,
                    vibrate, ringtone, priority);

            if (notification != null) {
                NotificationManagerCompat.from(context).notify(notificationTag, notificationID, notification);
                if (NOTIFICATION_TAG_TURN_ALARM_TEST.equals(notificationTag))
                    NotificationUtils.cancelNotificationAfter(notificationTag, notificationID, 1, TimeUnit.MINUTES);
                else {
                    AppConfig appConfig = AppConfig.getInstance();
                    long targetTime = alarmTargetTime != null ? alarmTargetTime :
                            TimeUnit.MINUTES.toMillis(appConfig.getLong(TurnAlarm.Settings.MAX_BEFOREHAND_INPUT));
                    appConfig.add(SetSetting.LAUNCHED_TURN_ALARMS, LaunchedTurnAlarm.encodeString(progressID, targetTime));

                    AbstractQueueApplication.getWorkManager().enqueueUniquePeriodicWork(PeriodicNotificationCleaner.NAME, ExistingPeriodicWorkPolicy.KEEP,
                            new PeriodicWorkRequest.Builder(PeriodicNotificationCleaner.class, 15, TimeUnit.MINUTES)
                                    .setInputData(Teller.logWorkerRequest(PeriodicNotificationCleaner.NAME).build())
                                    .setInitialDelay(ALARM_NOTIFICATION_TTL + TimeUtils.ONE_SECOND_MILLIS, TimeUnit.MILLISECONDS)
                                    .build()
                    );
                }
            }
        }
    }

    public static void startUnknownAvailabilityAlert(long serviceID, String serviceDescription) {
        AbstractQueueApplication context = AbstractQueueApplication.getInstance();
        if (context != null) {
            Notification notification = newStaticAlarmNotification(context, true,
                    context.getString(R.string.unknown_office_state),
                    context.getString(R.string.unknown_office_state_content, serviceDescription),
                    context.getString(R.string.unknown_office_state_details, serviceDescription),
                    R.drawable.ic_logo_client_app_24);
            if (notification != null) {
                NotificationManagerCompat.from(context).notify(NOTIFICATION_TAG_UNKNOWN_AVAILABILITY, StringUtil.hash(serviceID), notification);
                AppConfig.getInstance().getAndSet(BooleanSetting.UNKNOWN_AVAILABILITY_NOTIFICATION_SHOWN, true);
            } else
                Teller.logUnexpectedCondition();
        }
    }

    public static void startIdleAdminNotification(long serviceID, String serviceDescription) {
        AbstractQueueApplication context = AbstractQueueApplication.getInstance();
        if (context != null) {
            Notification notification = newStaticAlarmNotification(context, false,
                    context.getString(R.string.idle_admin_warning),
                    context.getString(R.string.idle_admin_content, serviceDescription),
                    context.getString(R.string.idle_admin_details, serviceDescription),
                    R.drawable.ic_logo_admin_app_24);
            if (notification != null) {
                NotificationManagerCompat.from(context).notify(NOTIFICATION_TAG_IDLE_ADMIN, StringUtil.hash(serviceID), notification);
                AppConfig.getInstance().getAndSet(BooleanSetting.IDLE_ADMIN_NOTIFICATION_SHOWN, true);
            } else
                Teller.logUnexpectedCondition();
        }
    }

    @Nullable
    private static Notification newStaticAlarmNotification(AbstractQueueApplication context, boolean once, String title, String content, String details, int icon) {
        try {
            return new NotificationCompat.Builder(context, context.getString(R.string.alarms_channel_id))
                    .setSmallIcon(icon)
                    .setColor(context.getResources().getColor(R.color.brand_color))
                    .setContentTitle(title)
                    .setContentText(content)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(details))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setOnlyAlertOnce(once)
                    .setAutoCancel(true)
                    .setShowWhen(true)
                    .setVibrate(QueueConfig.ALARM_VIBRATION_PATTERN)
                    .setLights(context.getResources().getColor(R.color.brand_color), QueueConfig.ON_DURATION, QueueConfig.OFF_DURATION)
                    .setContentIntent(ContextUtils.newOpenActivityIntent(context, context.getMainActivity()))
                    .build();
        } catch (RuntimeException e) {
            Teller.warn("could not get static notification", e);
            return null;
        }
    }

    public static void cancelIdleAdminAlert(long serviceID) {
        boolean notificationShown = AppConfig.getInstance().getAndSet(BooleanSetting.IDLE_ADMIN_NOTIFICATION_SHOWN, false);
        if (notificationShown)
            cancelNotification(AbstractQueueApplication.getInstance(), NOTIFICATION_TAG_IDLE_ADMIN, StringUtil.hash(serviceID));
    }

    public static void cancelUnknownAvailabilityAlert(long serviceID) {
        boolean notificationShown = AppConfig.getInstance().getAndSet(BooleanSetting.UNKNOWN_AVAILABILITY_NOTIFICATION_SHOWN, false);
        if (notificationShown)
            cancelNotification(AbstractQueueApplication.getInstance(), NOTIFICATION_TAG_UNKNOWN_AVAILABILITY, StringUtil.hash(serviceID));
    }

    public static void cancelNotification(Context context, String tag, int id) {
        if (context != null)
            NotificationManagerCompat.from(context).cancel(tag, id);
    }

    public static void cancelTurnAlarmNotification(long progressID) {
        AbstractQueueApplication context = AbstractQueueApplication.getInstance();
        if (context != null)
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_TAG_TURN_ALARM, StringUtil.hash(progressID));
    }

    @Nullable
    public static Notification newTurnAlarmNotification(AbstractQueueApplication context, Long progressID,
                                                        String smsToken, String notificationTag, int notificationID,
                                                        Long remainingTime, Integer queue, String serviceDescription,
                                                        Boolean vibrate, Integer ringtone, Integer priority) {
        String alarmTitle = getAlarmTitle(context, remainingTime);
        if (alarmTitle == null)
            return null;

        AppConfig appConfig = AppConfig.getInstance();
        int alarmPriority = TurnAlarm.alarmPriority(appConfig, priority);
        Intent openIntent = newNotificationIntent(context, context.getMainActivity(), notificationID, notificationTag, smsToken);

        try {
            return new NotificationCompat.Builder(context, context.getString(R.string.alarms_channel_id))
                    .setSmallIcon(R.drawable.ic_logo_client_app_24)
                    .setColor(context.getResources().getColor(R.color.brand_color))
                    .setContentTitle(alarmTitle)
                    .setContentText(getAlarmContent(context, queue, serviceDescription))
                    .setPriority(alarmPriority)
                    .setOnlyAlertOnce(false)
                    .setAutoCancel(true)
                    .setShowWhen(true)
                    .setVibrate(StringUtil.isTrue(vibrate) ? QueueConfig.ALARM_VIBRATION_PATTERN : null)
                    .setSound(ContextUtils.getUri(context, TurnAlarm.ringtoneRes(ringtone, alarmPriority)))
                    .setLights(context.getResources().getColor(R.color.brand_color), QueueConfig.ON_DURATION, QueueConfig.OFF_DURATION)
                    .setContentIntent(ContextUtils.newOpenActivityIntent(context, openIntent))
                    .setDeleteIntent(newAlarmClearedIntent(context, notificationID, notificationTag, smsToken, progressID))
                    .addAction(R.drawable.ic_baseline_alarm_off_24, context.getString(R.string.dismiss_alarm), newAlarmDismissIntent(context, notificationID, notificationTag, smsToken, progressID))
                    .addAction(R.drawable.ic_baseline_snooze_24, context.getString(R.string.snooze), newAlarmSnoozeIntent(context, notificationID, notificationTag, smsToken))
                    .build();
        } catch (RuntimeException e) {
            Teller.warn("could not get turn alarm notification", e);
            return null;
        }
    }

    private static PendingIntent newAlarmClearedIntent(AbstractQueueApplication context, int notificationID, String notificationTag, String smsToken, Long progressID) {
        Intent intent = newNotificationIntent(context, context.getMainActionReceiver(), notificationID, notificationTag, smsToken);
        intent.putExtra(Progress.ID, progressID);
        return getAlarmPendingIntent(context, Request.ALARM_CLEARED, intent);
    }

    private static PendingIntent newAlarmDismissIntent(AbstractQueueApplication context, int notificationID, String notificationTag, String smsToken, Long progressID) {
        Intent intent = newNotificationIntent(context, context.getMainActionReceiver(), notificationID, notificationTag, smsToken);
        intent.putExtra(Progress.ID, progressID);
        return getAlarmPendingIntent(context, Request.DISMISS_ALARM, intent);
    }

    private static PendingIntent newAlarmSnoozeIntent(AbstractQueueApplication context, int notificationID, String notificationTag, String smsToken) {
        Intent intent = newNotificationIntent(context, context.getMainActionReceiver(), notificationID, notificationTag, smsToken);
        return getAlarmPendingIntent(context, Request.SNOOZE_ALARM, intent);
    }

    private static PendingIntent getAlarmPendingIntent(AbstractQueueApplication context, Request request, Intent intent) {
        intent.putExtra(BasicActionReceiver.ACTION_KEY, request.name());
        return ContextUtils.getBroadcastIntent(context, request.ordinal(), intent);
    }

    @NotNull
    private static Intent newNotificationIntent(AbstractQueueApplication context, Class<?> target,
                                                int notificationID, String notificationTag, String smsToken) {
        Intent intent = new Intent(context, target);
        intent.putExtra(KEY_NOTIFICATION_ID, notificationID);
        intent.putExtra(KEY_NOTIFICATION_TAG, notificationTag);
        intent.putExtra(KEY_NOTIFICATION_SMS_TOKEN, smsToken);
        return intent;
    }

    private static String getAlarmTitle(AbstractQueueApplication context, Long remainingTime) {
        if (remainingTime == null || remainingTime < Estimator.TICKET_TURN_PASSED || remainingTime > Estimator.MAX_ESTIMATION_VALUE)
            return null;
        else if (remainingTime == Estimator.TICKET_TURN_PASSED)
            return context.getString(R.string.turn_alarm_title_turn_passed);
        else if (remainingTime < TimeUtils.ONE_MINUTE_MILLIS)
            return context.getString(R.string.turn_alarm_title_turn_now);
        else
            return context.getString(R.string.turn_alarm_title_turn_estimation, QueueUtils.formatAsDurationOfMinutesAndHours(remainingTime));
    }

    private static CharSequence getAlarmContent(AbstractQueueApplication context, Integer waiting, String desc) {
        if (waiting == null || waiting < 1)
            return desc != null ? desc : context.getString(R.string.turn_alarm_content_empty);
        else {
            final String personsWaitingBeforeYou = context.getResources().getQuantityString(R.plurals.persons_waiting_before_you, waiting, waiting);
            return desc != null ? context.getString(R.string.concat, personsWaitingBeforeYou, desc) : personsWaitingBeforeYou;
        }
    }

    public static void cancelNotificationAfter(String notificationTag, int notificationID, long duration, TimeUnit timeUnit) {
        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(CancelNotificationWork.class)
                .setInitialDelay(duration, timeUnit)
                .setInputData(Teller.logWorkerRequest(CancelNotificationWork.NAME)
                        .putInt(KEY_NOTIFICATION_ID, notificationID)
                        .putString(KEY_NOTIFICATION_TAG, notificationTag)
                        .build())
                .build();
        AbstractQueueApplication.getWorkManager().enqueue(work);
    }

    public static final class CancelNotificationWork extends Worker {

        public static final String NAME = "cancel_notification_work";

        public CancelNotificationWork(@NonNull Context context,
                                      @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
        }

        @NonNull
        @NotNull
        @Override
        public Result doWork() {
            Data data = getInputData();
            Teller.logWorkerSession(data);
            NotificationManagerCompat.from(getApplicationContext()).cancel(data.getString(KEY_NOTIFICATION_TAG), data.getInt(KEY_NOTIFICATION_ID, NOTIFICATION_ID_DEFAULT));
            return Result.success();
        }
    }

    @HiltWorker
    public static class PeriodicNotificationCleaner extends Worker {

        public static final String NAME = "periodic_notes_cleaner";
        private final ProgressRepository progressRepository;

        @AssistedInject
        public PeriodicNotificationCleaner(@Assisted @NotNull Context context,
                                           @Assisted @NotNull WorkerParameters workerParams,
                                           ProgressRepository progressRepository) {
            super(context, workerParams);
            this.progressRepository = progressRepository;
        }

        @NotNull
        @Override
        public Result doWork() {
            try {
                Teller.logWorkerSession(getInputData());
                progressRepository.clearOldLaunchedTurnAlarms(getId());
            } catch (InterruptedException e) {
                Teller.logInterruption(e);
            } catch (RuntimeException e) {
                Teller.warn("error while clearing old notifications", e);
            }
            return Result.success();
        }
    }
}
