package com.dalti.laposte.core.repositories;

import androidx.annotation.CallSuper;

import com.dalti.laposte.core.util.QueueUtils;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.Map;

import dz.jsoftware95.queue.common.GlobalConf;
import dz.jsoftware95.silverbox.android.common.LDT;
import dz.jsoftware95.silverbox.android.common.StringUtil;

public abstract class AbstractUpdateHandler {

    private static final String EXCLUDE_KEY = QueueUtils.isTesting() ? GlobalConf.EXCLUDE_TEST_CLIENTS
            : GlobalConf.EXCLUDE_PRODUCTION_CLIENTS;

    public AbstractUpdateHandler() {
    }

    public void handleUpdate(Map<String, String> data) {
        if (!StringUtil.isTrue(data, EXCLUDE_KEY)) {
            LDT.i("handling update: " + data);
            onData(data);
        }
    }

    @CallSuper
    protected void onData(Map<String, String> data) {
        FirebaseCrashlytics crashlytics = Teller.getCrashlytics();
        Teller.info("FCM data received: " + data.get(GlobalConf.WAITING_PREFIX + "0"));
        if (crashlytics != null) {
            String queueDelay = StringUtil.getString(data, GlobalConf.KEY_QUEUE_DELAY);
            crashlytics.log("Data received (queue-delay=" + queueDelay + " ms): " + data.toString());
        }

        final AppConfig appConfig = AppConfig.findInstance();
        if (appConfig != null) {
            final String lastReceivedAlarm = StringUtil.getString(data, GlobalConf.LAST_RECEIVED_ALARM);
            if (lastReceivedAlarm != null)
                appConfig.put(StringSetting.LAST_RECEIVED_ALARM, lastReceivedAlarm);
        }
    }
}
