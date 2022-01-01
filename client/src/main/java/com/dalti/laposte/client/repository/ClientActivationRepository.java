package com.dalti.laposte.client.repository;

import android.content.Intent;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.dalti.laposte.client.R;
import com.dalti.laposte.client.ui.ActivationCodeFormActivity;
import com.dalti.laposte.core.repositories.AbstractActivationRepository;
import com.dalti.laposte.core.repositories.ActivationState;
import com.dalti.laposte.core.repositories.AppConfig;
import com.dalti.laposte.core.repositories.DashboardRepository;
import com.dalti.laposte.core.repositories.ExtraRepository;
import com.dalti.laposte.core.repositories.InputProperty;
import com.dalti.laposte.core.repositories.LongSetting;
import com.dalti.laposte.core.repositories.SimpleProperty;
import com.dalti.laposte.core.repositories.Teller;
import com.dalti.laposte.core.ui.AbstractQueueApplication;
import com.dalti.laposte.core.ui.scanner.ScannerActivity;
import com.dalti.laposte.core.util.BuildConfiguration;
import com.dalti.laposte.core.util.QueueUtils;
import com.dalti.laposte.core.util.RepositoryUtil;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Lazy;
import dz.jsoftware95.queue.common.Executable;
import dz.jsoftware95.queue.common.GlobalUtil;
import dz.jsoftware95.queue.common.Pair;
import dz.jsoftware95.queue.api.ServerResponse;
import dz.jsoftware95.silverbox.android.middleware.BasicActivity;
import retrofit2.Call;

@Singleton
@AnyThread
public class ClientActivationRepository extends AbstractActivationRepository {

    private static final String SOURCE = "activation_hint_handler";
    @NonNull
    private final ExtraRepository extraRepository;
    @NonNull
    private final Lazy<DashboardRepository> dashboardRepository;
    @NonNull
    private final Lazy<ClientAPI> clientAPI;

    @Inject
    @AnyThread
    public ClientActivationRepository(@NotNull final BuildConfiguration buildConf,
                                      @NotNull final RepositoryUtil repositoryUtil,
                                      @NonNull final ExtraRepository extraRepository,
                                      @NonNull final Lazy<DashboardRepository> dashboardRepository,
                                      @NonNull final Lazy<ClientAPI> clientAPI) {
        super(buildConf, repositoryUtil);
        this.extraRepository = extraRepository;
        this.dashboardRepository = dashboardRepository;
        this.clientAPI = clientAPI;
    }

    @Override
    @NonNull
    @WorkerThread
    protected Pair<Call<ServerResponse>, Executable> callActivationAPI(String applicationID,
                                                                       int applicationVersion,
                                                                       int androidVersion,
                                                                       Long googleServicesVersion,
                                                                       int targetServer,
                                                                       boolean feedbackEnabled) throws InterruptedException {
        String code = extraRepository.getAndWait(InputProperty.ACTIVATION_CODE);
        if (code != null) {
            Call<ServerResponse> activationCall = clientAPI.get().activateClient(code, applicationID, applicationVersion, androidVersion, googleServicesVersion, targetServer, QueueUtils.getAppCheckToken());
            Executable postActivation = () -> {
                dashboardRepository.get().invalidate();
                extraRepository.putAndWait(SimpleProperty.CURRENT_ACTIVATED_CODE, code);
            };
            return new Pair<>(activationCall, postActivation);
        } else {
            QueueUtils.toast(R.string.enter_activation_code_first, feedbackEnabled);
            return Pair.empty();
        }
    }

    @Override
    public void handleActivationNeeded(ActivationState activationState, boolean feedback, String operation) {
        if (feedback) {
            if (GlobalUtil.isBlankToken(activationState.getApplicationID())) {
                QueueUtils.toast(R.string.google_services_connection_needed, !AppConfig.getInstance().isWaitingForAppID());
            } else if (!activationState.isActive()) {
                if (activationState.isExpired())
                    QueueUtils.toastActivationOutdated(activationState.getExpirationDate());
                else
                    QueueUtils.toastActivationNeeded(operation);

                considerActivationPopUp();
            }
        }
    }

    @Override
    public void toastActivationNeeded(String operation) {
        QueueUtils.toastActivationNeeded(operation);
        considerActivationPopUp();
    }

    public void considerActivationPopUp() {
        AppConfig appConfig = AppConfig.getInstance();
        AtomicInteger activationNeededEncounters = AbstractQueueApplication.getActivationNeededEncounters();
        int activationNeededCount = activationNeededEncounters.incrementAndGet();

        BasicActivity currentActivity = BasicActivity.CURRENT_STARTED_ACTIVITY;
        if (currentActivity != null && activationNeededCount > appConfig.getRemoteLong(LongSetting.ACTIVATION_HINTS_COUNT)) {
            activationNeededEncounters.set(appConfig.getRemoteInt(LongSetting.ACTIVATION_HINTS_RESET));
            if (!(currentActivity instanceof ActivationCodeFormActivity) && !(currentActivity instanceof ScannerActivity)) {
                Intent intent = new Intent(currentActivity, ActivationCodeFormActivity.class);
                intent.putExtra(Teller.ACTIVATION_SOURCE, SOURCE);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                currentActivity.startActivity(intent);
            }
        }
    }
}
