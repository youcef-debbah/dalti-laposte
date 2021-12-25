package com.dalti.laposte.core.ui;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.dalti.laposte.core.repositories.AbstractUpdateHandler;
import com.dalti.laposte.core.repositories.AppConfig;
import com.dalti.laposte.core.repositories.DashboardRepository;
import com.dalti.laposte.core.repositories.Teller;
import com.dalti.laposte.core.util.BuildConfiguration;
import com.dalti.laposte.core.util.QueueUtils;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

public abstract class AbstractQueueService extends FirebaseMessagingService {

    @Inject
    AbstractUpdateHandler updateHandler;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        updateHandler.handleUpdate(remoteMessage.getData());
    }

    @Override
    public void onNewToken(@NonNull String token) {
        long now = System.currentTimeMillis();
        Teller.info("new FCM token: " + token);
        AbstractQueueApplication.getWorkManager().enqueue(new OneTimeWorkRequest.Builder(TokenUpdateWorker.class)
                .setInputData(Teller.logWorkerRequest(TokenUpdateWorker.KEY_INPUT_TOKEN)
                        .putString(TokenUpdateWorker.KEY_INPUT_TOKEN, token)
                        .build())
                .build());
    }

    @Override
    public void onDeletedMessages() {
        Teller.info("FCM messages deleted");
        QueueUtils.requestCacheInvalidation();
    }

    @HiltWorker
    public static class TokenUpdateWorker extends Worker {

        public static final String NAME = "token_update_worker";
        public static final String KEY_INPUT_TOKEN = "KEY_INPUT_TOKEN";

        private final DashboardRepository dashboardRepository;
        private final BuildConfiguration buildConfiguration;

        @AssistedInject
        public TokenUpdateWorker(@Assisted @NotNull Context context,
                                 @Assisted @NotNull WorkerParameters workerParams,
                                 DashboardRepository dashboardRepository,
                                 BuildConfiguration buildConfiguration) {
            super(context, workerParams);
            this.dashboardRepository = dashboardRepository;
            this.buildConfiguration = buildConfiguration;
        }

        @NonNull
        @NotNull
        @Override
        public Result doWork() {
            Data data = getInputData();
            Teller.logWorkerSession(data);
            String token = data.getString(KEY_INPUT_TOKEN);
            if (token != null)
                try {
                    AppConfig.getInstance().setApplicationID(token, buildConfiguration);
                    dashboardRepository.refreshAndWait();
                } catch (InterruptedException e) {
                    Teller.logInterruption(e);
                }
            return Result.success();
        }
    }
}
