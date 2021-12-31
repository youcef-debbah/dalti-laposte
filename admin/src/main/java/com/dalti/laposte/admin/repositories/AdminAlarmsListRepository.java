package com.dalti.laposte.admin.repositories;

import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;

import com.dalti.laposte.admin.R;
import com.dalti.laposte.admin.entity.AdminAPI;
import com.dalti.laposte.core.entity.AdminAlarm;
import com.dalti.laposte.core.repositories.AdminAlarmDAO;
import com.dalti.laposte.core.entity.AlarmsInfo;
import com.dalti.laposte.core.repositories.AppConfig;
import com.dalti.laposte.core.repositories.ExtraRepository;
import com.dalti.laposte.core.repositories.InputProperty;
import com.dalti.laposte.core.repositories.LongSetting;
import com.dalti.laposte.core.repositories.Teller;
import com.dalti.laposte.core.util.BuildConfiguration;
import com.dalti.laposte.core.util.QueueUtils;
import com.dalti.laposte.core.util.RepositoryUtil;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Lazy;
import dz.jsoftware95.queue.api.AlarmInfo;
import dz.jsoftware95.queue.common.Function;
import dz.jsoftware95.queue.common.ResponseConfig;
import dz.jsoftware95.silverbox.android.backend.DataEvent;
import dz.jsoftware95.silverbox.android.backend.DatabaseUtils;
import dz.jsoftware95.silverbox.android.backend.Item;
import dz.jsoftware95.silverbox.android.backend.LiveDataWrapper;
import dz.jsoftware95.silverbox.android.backend.LiveListRepository;
import dz.jsoftware95.silverbox.android.backend.hasLiveDataCache;
import dz.jsoftware95.silverbox.android.common.StringUtil;
import dz.jsoftware95.silverbox.android.concurrent.AppWorker;
import dz.jsoftware95.silverbox.android.concurrent.DuoDatabaseJob;
import dz.jsoftware95.silverbox.android.concurrent.Job;
import dz.jsoftware95.silverbox.android.concurrent.UnJob;
import retrofit2.Call;
import retrofit2.Response;

@Singleton
@AnyThread
public class AdminAlarmsListRepository extends LiveListRepository<AdminAlarm, AdminAlarmDAO> implements hasLiveDataCache<Long, AdminAlarm> {

    @NonNull
    private final Lazy<AdminAPI> adminAPI;
    @NonNull
    private final ExtraRepository extraRepository;
    @NonNull
    private final BuildConfiguration buildConf;
    @NonNull
    private final RepositoryUtil repositoryUtil;

    private final ConcurrentMap<Long, LiveDataWrapper<AdminAlarm>> cache = new ConcurrentHashMap<>(4);

    @Inject
    @AnyThread
    public AdminAlarmsListRepository(@NonNull final Lazy<AdminAlarmDAO> adminAlarmDAO,
                                     @NonNull final Lazy<AdminAPI> adminAPI,
                                     @NonNull final ExtraRepository extraRepository,
                                     @NonNull final BuildConfiguration buildConf,
                                     @NonNull final RepositoryUtil repositoryUtil) {
        super(adminAlarmDAO);
        this.adminAPI = adminAPI;
        this.extraRepository = extraRepository;
        this.buildConf = buildConf;
        this.repositoryUtil = repositoryUtil;
//        initializeIfNeeded();
    }

    @WorkerThread
    protected void onAutoRefresh() {
        AdminAlarmDAO dao = requireDAO();
        if (dao.count() == 0)
            refresh(true);
        else {
            Long lastUpdate = dao.getLastAdminAlarmsUpdate();
            long maxCache = AppConfig.getInstance().getRemoteLong(LongSetting.MAX_ADMIN_ALARM_CACHE);
            if (lastUpdate == null || (System.currentTimeMillis() - lastUpdate) > maxCache)
                refresh(true);
        }
    }

    @Override
    public void invalidate() {
        fetchAdminAlarms(true);
    }

    public void refresh(boolean feedback) {
        postPublish(DataEvent.FETCHING_DATA);
        fetchAdminAlarms(feedback);
    }

    @MainThread
    public LiveData<AdminAlarm> getAdminAlarm(long id) {
        return getFromCache(id);
    }

    @Override
    public ConcurrentMap<Long, LiveDataWrapper<AdminAlarm>> getCache() {
        return cache;
    }

    @Override
    public void initCacheEntry(Long key, LiveDataWrapper<AdminAlarm> output) {
        execute(newPostValueJob(this, output, key));
    }

    private static Job newPostValueJob(AdminAlarmsListRepository repository, LiveDataWrapper<AdminAlarm> output, Long key) {
        return new DuoDatabaseJob<LiveDataWrapper<AdminAlarm>, AdminAlarmsListRepository>(output, repository) {
            @Override
            protected void doFromBackground(@NonNull LiveDataWrapper<AdminAlarm> output,
                                            @NonNull AdminAlarmsListRepository repository) {
                output.postValue(repository.requireDAO().getAdminAlarmValue(key));
            }
        };
    }

    private void fetchAdminAlarms(boolean feedback) {
        execute(new FetchAdminAlarmsJob(this, buildConf.getServerTarget(), feedback));
    }

    private static class FetchAdminAlarmsJob extends UnJob<AdminAlarmsListRepository> {
        private static final String NAME = "fetch_admin_alarms_job";

        protected final int serverTarget;
        protected volatile boolean feedback;

        public FetchAdminAlarmsJob(AdminAlarmsListRepository repository, int serverTarget, boolean feedback) {
            super(AppWorker.NETWORK, repository);
            this.serverTarget = serverTarget;
            this.feedback = feedback;
        }

        @Override
        protected void doFromBackground(@NonNull AdminAlarmsListRepository repository) throws InterruptedException {
            Call<AlarmsInfo> alarmsCall = null;
            try {
                String username = repository.extraRepository.getAndWait(InputProperty.PRINCIPAL_NAME);
                String password = repository.extraRepository.getAndWait(InputProperty.PRINCIPAL_PASSWORD);
                alarmsCall = getAdminAlarms(username, password, repository);
                if (alarmsCall != null) {
                    String url = QueueUtils.getUrl(alarmsCall);
                    Response<AlarmsInfo> response = alarmsCall.execute();

                    if (response.isSuccessful()) {
                        AlarmsInfo responseBody = response.body();
                        if (responseBody != null) {
                            List<AdminAlarm> newAlarmsList = DatabaseUtils.filterValid(responseBody.getAlarms());
                            try {
                                repository.waitForDAO(getClass().getSimpleName()).replaceAll(newAlarmsList);
                                Teller.info("alarms fetched successfully, count: " + newAlarmsList.size());
                                if (StringUtil.isTrue(responseBody.getData(), ResponseConfig.ENTRY_CREATED))
                                    QueueUtils.toast(R.string.alarm_added, feedback);
                                else if (StringUtil.isTrue(responseBody.getData(), ResponseConfig.ENTRY_SAVED))
                                    QueueUtils.toast(R.string.alarm_updated, feedback);
                                else if (StringUtil.isTrue(responseBody.getData(), ResponseConfig.ENTRY_DELETED))
                                    QueueUtils.toast(R.string.alarm_deleted, feedback);
                                else if (StringUtil.isTrue(responseBody.getData(), ResponseConfig.ENTRY_EXIST))
                                    QueueUtils.toast(R.string.alarm_exist, feedback);
                                else if (StringUtil.isTrue(responseBody.getData(), ResponseConfig.UPDATE_ERROR))
                                    QueueUtils.toast(R.string.update_failed, feedback);
                                else
                                    QueueUtils.toast(R.string.alarms_updated, feedback);
                            } catch (Exception e) {
                                Teller.warn("failed to save fetched alarms to the database", e);
                                QueueUtils.toast(R.string.could_not_persist_data, feedback);
                            }
                        } else
                            Teller.logMissingInfo("null body", feedback);

                        repository.repositoryUtil.handleResponse(responseBody, NAME);
                    } else
                        repository.repositoryUtil.handleUnsuccessfulResponse(response, "could not update alarms list: " + QueueUtils.getUrl(alarmsCall), null, feedback, NAME);
                } else if (username == null || password == null)
                    QueueUtils.toast(R.string.credentials_needed, feedback);
            } catch (Exception e) {
                repository.repositoryUtil.handleRequestException(e, "error while updating alarms list: " + QueueUtils.getUrl(alarmsCall), feedback);
            } finally {
                repository.postPublish(DataEvent.DATA_FETCHED);
            }
        }

        @WorkerThread
        protected Call<AlarmsInfo> getAdminAlarms(@Nullable String username, @Nullable String password, @NonNull AdminAlarmsListRepository repository) throws InterruptedException {
            if (username == null && password == null)
                return null;
            else
                return repository.adminAPI.get().getAdminAlarms(username, password, serverTarget);
        }
    }

    public void saveAdminAlarm(AlarmInfo info, boolean confirm, Long id) {
        postPublish(DataEvent.FETCHING_DATA);
        execute(new PutAdminAlarmJob(this, buildConf.getServerTarget(), info, confirm, id));
    }

    private static class PutAdminAlarmJob extends FetchAdminAlarmsJob {

        protected final AlarmInfo info;
        protected final boolean confirm;
        protected final Long id;

        public PutAdminAlarmJob(AdminAlarmsListRepository repository, int serverTarget, AlarmInfo info, boolean confirm, Long id) {
            super(repository, serverTarget, true);
            this.info = info;
            this.confirm = confirm;
            this.id = id;
        }

        @Override
        protected Call<AlarmsInfo> getAdminAlarms(String username, String password, @NotNull AdminAlarmsListRepository repository) {
            if (username == null && password == null)
                return null;
            else
                return repository.adminAPI.get().putAdminAlarm(username, password, serverTarget, confirm, id, info);
        }
    }

    public void deleteAdminAlarm(Long id) {
        if (id != null && id > Item.AUTO_ID) {
            postPublish(DataEvent.FETCHING_DATA);
            execute(new DeleteAdminAlarmJob(this, buildConf.getServerTarget(), id));
        }
    }

    private static class DeleteAdminAlarmJob extends FetchAdminAlarmsJob {

        protected final long id;

        public DeleteAdminAlarmJob(AdminAlarmsListRepository repository, int serverTarget, long id) {
            super(repository, serverTarget, true);
            this.id = id;
        }

        @Override
        @WorkerThread
        protected Call<AlarmsInfo> getAdminAlarms(String username, String password, @NotNull AdminAlarmsListRepository repository) {
            if (username == null && password == null)
                return null;
            else
                return repository.adminAPI.get().deleteAdminAlarm(username, password, serverTarget, id);
        }
    }

    public void updateAlarm(long id, Function<AdminAlarm, Boolean> update) {
        if (id > Item.AUTO_ID && update != null) {
            postPublish(DataEvent.FETCHING_DATA);
            execute(new UpdateAdminAlarmJob(this, buildConf.getServerTarget(), update, id));
        }
    }

    private static class UpdateAdminAlarmJob extends FetchAdminAlarmsJob {

        protected final Function<AdminAlarm, Boolean> updater;
        protected final long id;

        public UpdateAdminAlarmJob(AdminAlarmsListRepository repository,
                                   int serverTarget,
                                   Function<AdminAlarm, Boolean> updater,
                                   long id) {
            super(repository, serverTarget, true);
            this.updater = Objects.requireNonNull(updater);
            this.id = id;
        }

        @Override
        protected Call<AlarmsInfo> getAdminAlarms(String username, String password, @NotNull AdminAlarmsListRepository repository) throws InterruptedException {
            if (username != null && password != null) {
                AdminAlarmDAO adminAlarmDAO = repository.waitForDAO(getClass().getSimpleName());
                AdminAlarm alarm = adminAlarmDAO.getAdminAlarmValue(id);
                if (alarm != null && StringUtil.isTrue(updater.apply(alarm)))
                    return repository.adminAPI.get().putAdminAlarm(username, password, serverTarget, true, id, alarm.getInfo());
            }
            return null;
        }
    }
}
