package com.dalti.laposte.core.repositories;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.dalti.laposte.R;
import com.dalti.laposte.core.util.BuildConfiguration;
import com.dalti.laposte.core.util.QueueUtils;
import com.dalti.laposte.core.util.RepositoryUtil;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Lazy;
import dz.jsoftware95.silverbox.android.backend.DataEvent;
import dz.jsoftware95.silverbox.android.backend.DatabaseUtils;
import dz.jsoftware95.silverbox.android.backend.Item;
import dz.jsoftware95.silverbox.android.backend.LiveListRepository;
import dz.jsoftware95.silverbox.android.concurrent.AppWorker;
import dz.jsoftware95.silverbox.android.concurrent.Job;
import dz.jsoftware95.silverbox.android.concurrent.UnDatabaseJob;
import dz.jsoftware95.silverbox.android.concurrent.UnJob;
import retrofit2.Call;
import retrofit2.Response;

@Singleton
@AnyThread
public class ServicesListRepository extends LiveListRepository<Service, ServiceDAO> {

    @NonNull
    private final Lazy<CoreAPI> coreAPI;
    @NonNull
    private final ExtraRepository extraRepository;
    @NonNull
    private final BuildConfiguration buildConf;
    @NonNull
    private final RepositoryUtil repositoryUtil;

    @Inject
    @AnyThread
    public ServicesListRepository(@NonNull final Lazy<ServiceDAO> serviceDAO,
                                  @NonNull final Lazy<CoreAPI> coreAPI,
                                  @NonNull final BuildConfiguration buildConf,
                                  @NonNull final RepositoryUtil repositoryUtil,
                                  @NonNull ExtraRepository extraRepository) {
        super(serviceDAO);
        this.coreAPI = coreAPI;
        this.buildConf = buildConf;
        this.extraRepository = extraRepository;
        this.repositoryUtil = repositoryUtil;
//        initializeIfNeeded();
    }

    @WorkerThread
    protected void onAutoRefresh() {
        ServiceDAO dao = requireDAO();
        if (dao.count() == 0)
            refresh(true);
        else {
            Long lastUpdate = dao.getLastServicesUpdate();
            if (lastUpdate == null)
                dao.clearProgresses();

            long maxCache = AppConfig.getInstance().getRemoteLong(LongSetting.MAX_SERVICE_CACHE);
            if (lastUpdate == null || (System.currentTimeMillis() - lastUpdate) > maxCache)
                refresh(true);
        }
    }

    @Override
    public void invalidate() {
        fetchData(true);
    }

    public void refresh(boolean feedback) {
        postPublish(DataEvent.FETCHING_DATA);
        fetchData(feedback);
    }

    private void fetchData(boolean feedback) {
        execute(new FetchServicesJob(this, buildConf.getServerTarget(), feedback));
    }

    private static final class FetchServicesJob extends UnJob<ServicesListRepository> {
        private static final String NAME = "fetch_services_job";

        private final int serverTarget;
        private final boolean feedback;

        public FetchServicesJob(ServicesListRepository repository, int serverTarget, boolean feedback) {
            super(AppWorker.NETWORK, repository);
            this.serverTarget = serverTarget;
            this.feedback = feedback;
        }

        @Override
        protected void doFromBackground(@NonNull ServicesListRepository repository) throws InterruptedException {
            String url = null;
            try {
                String username = repository.buildConf.isAdmin() ? repository.extraRepository.getAndWait(InputProperty.PRINCIPAL_NAME) : null;
                Call<ServicesInfo> servicesCall = repository.coreAPI.get().getServicesAsGuest(serverTarget, username);
                url = QueueUtils.getUrl(servicesCall);
                Response<ServicesInfo> response = servicesCall.execute();

                if (response.isSuccessful()) {
                    ServicesInfo responseBody = response.body();
                    if (responseBody != null) {
                        List<Service> services = responseBody.getServices();
                        List<Service> newServicesList = DatabaseUtils.filterValid(services);
                        try {
                            repository.waitForDAO(getClass().getSimpleName()).replaceAll(newServicesList, responseBody.getSchema());
                            Teller.info("services fetched successfully, count: " + newServicesList.size());
                            QueueUtils.toast(R.string.services_updated, feedback);
                        } catch (Exception e) {
                            Teller.warn("failed to save fetched services to the database", e);
                            QueueUtils.toast(R.string.could_not_persist_data, feedback);
                        }
                    } else
                        Teller.logMissingInfo("null body", feedback);

                    repository.repositoryUtil.handleResponse(responseBody, NAME);
                } else
                    repository.repositoryUtil.handleUnsuccessfulResponse(response, "unsuccessful services list fetch: " + url, null, feedback, NAME);
            } catch (Exception e) {
                repository.repositoryUtil.handleRequestException(e, "failed to fetch services: " + url, feedback);
            } finally {
                repository.postPublish(DataEvent.DATA_FETCHED);
            }
        }
    }

    public void setCurrentService(Long serviceID) {
        if (serviceID != null && serviceID > Item.AUTO_ID)
            execute(newSelectServiceJob(this, serviceID));
        else
            execute(newSelectServiceJob(this, null));
    }

    private static Job newSelectServiceJob(ServicesListRepository repository, @Nullable Long serviceID) {
        return new UnDatabaseJob<ServicesListRepository>(repository) {
            @Override
            protected void doFromBackground(@NonNull ServicesListRepository repository) {
                repository.requireDAO().selectService(serviceID);
            }
        };
    }
}
