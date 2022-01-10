package com.dalti.laposte.admin.repositories;

import android.util.Log;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.dalti.laposte.admin.R;
import com.dalti.laposte.admin.entity.AdminAPI;
import com.dalti.laposte.core.repositories.AbstractActivationRepository;
import com.dalti.laposte.core.repositories.ActivationState;
import com.dalti.laposte.core.repositories.AppConfig;
import com.dalti.laposte.core.repositories.ExtraRepository;
import com.dalti.laposte.core.repositories.InputProperty;
import com.dalti.laposte.core.repositories.LongSetting;
import com.dalti.laposte.core.repositories.StateRepository;
import com.dalti.laposte.core.repositories.Teller;
import com.dalti.laposte.core.util.BuildConfiguration;
import com.dalti.laposte.core.util.QueueUtils;
import com.dalti.laposte.core.util.RepositoryUtil;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.appcheck.AppCheckToken;
import com.google.firebase.appcheck.FirebaseAppCheck;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Lazy;
import dz.jsoftware95.queue.common.Executable;
import dz.jsoftware95.queue.api.Pair;
import dz.jsoftware95.queue.api.ServerResponse;
import retrofit2.Call;

@Singleton
@AnyThread
public class AdminActivationRepository extends AbstractActivationRepository {

    @NonNull
    private final ExtraRepository extraRepository;
    @NonNull
    private final StateRepository stateRepository;
    @NonNull
    private final Lazy<ActivationsListRepository> activationsListRepository;
    @NonNull
    private final Lazy<AdminAlarmsListRepository> adminAlarmsListRepository;
    @NonNull
    private final Lazy<AdminAPI> adminAPI;

    @Inject
    @AnyThread
    public AdminActivationRepository(@NonNull final BuildConfiguration buildConf,
                                     @NonNull final RepositoryUtil repositoryUtil,
                                     @NonNull final ExtraRepository extraRepository,
                                     @NonNull final StateRepository stateRepository,
                                     @NonNull final Lazy<ActivationsListRepository> activationsListRepository,
                                     @NonNull final Lazy<AdminAlarmsListRepository> adminAlarmsListRepository,
                                     @NonNull final Lazy<AdminAPI> adminAPI) {
        super(buildConf, repositoryUtil);
        this.extraRepository = extraRepository;
        this.stateRepository = stateRepository;
        this.activationsListRepository = activationsListRepository;
        this.adminAlarmsListRepository = adminAlarmsListRepository;
        this.adminAPI = adminAPI;
    }

    @Override
    @NonNull
    @WorkerThread
    protected Pair<Call<ServerResponse>, Executable> callActivationAPI(String applicationID, int applicationVersion, int androidVersion, Long googleServicesVersion, int targetServer, boolean feedback) throws InterruptedException {
        String username = extraRepository.getAndWait(InputProperty.PRINCIPAL_NAME);
        String password = extraRepository.getAndWait(InputProperty.PRINCIPAL_PASSWORD);
        if (username == null || password == null) {
            QueueUtils.toast(R.string.credentials_needed, feedback);
            return Pair.empty();
        }

        return new Pair<>(adminAPI.get().activateAdmin(
                applicationID, applicationVersion, androidVersion, googleServicesVersion,
                username, password, targetServer, QueueUtils.getAppCheckToken()), () -> {
            stateRepository.invalidateCacheThenFetch();
            activeUsername.set(username);
        });
    }

    @Override
    public void refreshCacheableData(boolean feedback) {
        ActivationsListRepository activationsListRepository = this.activationsListRepository.get();
        if (activationsListRepository.isDataAvailable())
            activationsListRepository.refresh(feedback);
        AdminAlarmsListRepository adminAlarmsListRepository = this.adminAlarmsListRepository.get();
        if (adminAlarmsListRepository.isDataAvailable())
            adminAlarmsListRepository.refresh(feedback);
    }

    @Override
    public void handleActivationNeeded(ActivationState activationState, boolean feedback, String operation) {
        if (feedback && !activationState.isActive()) {
            if (activationState.isExpired())
                QueueUtils.toastActivationOutdated(activationState.getExpirationDate());
            else
                QueueUtils.toastActivationNeeded(operation);
        }
    }

    @Override
    public void toastActivationNeeded(String operation) {
        QueueUtils.toastActivationNeeded(operation);
    }
}
