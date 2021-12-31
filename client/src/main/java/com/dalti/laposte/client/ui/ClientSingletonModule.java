package com.dalti.laposte.client.ui;

import android.app.Application;

import androidx.room.Room;

import com.dalti.laposte.client.repository.ClientAPI;
import com.dalti.laposte.client.repository.ClientActivationRepository;
import com.dalti.laposte.client.repository.ClientBuildConfiguration;
import com.dalti.laposte.client.repository.ClientUpdateHandler;
import com.dalti.laposte.core.repositories.AbstractActivationRepository;
import com.dalti.laposte.core.repositories.AbstractUpdateHandler;
import com.dalti.laposte.core.entity.CoreAPI;
import com.dalti.laposte.core.repositories.ExtraDAO;
import com.dalti.laposte.core.repositories.LogDAO;
import com.dalti.laposte.core.repositories.ProgressDAO;
import com.dalti.laposte.core.repositories.QueueDatabase;
import com.dalti.laposte.core.repositories.ServiceDAO;
import com.dalti.laposte.core.repositories.ShortMessageDAO;
import com.dalti.laposte.core.repositories.StateDAO;
import com.dalti.laposte.core.repositories.TurnAlarmDAO;
import com.dalti.laposte.core.repositories.WebPageDAO;
import com.dalti.laposte.core.ui.AbstractQueueApplication;
import com.dalti.laposte.core.ui.QueueActivitySupport;
import com.dalti.laposte.core.util.BuildConfiguration;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import retrofit2.Retrofit;

@Module
@InstallIn(SingletonComponent.class)
public interface ClientSingletonModule {

    @Provides
    @Singleton
    static QueueDatabase getDatabase(final Application context) {
        return Room.databaseBuilder(context, QueueDatabase.class, QueueDatabase.NAME).build();
    }

    @Provides
    static ServiceDAO serviceDAO(QueueDatabase database) {
        return database.serviceDAO();
    }

    @Provides
    static ProgressDAO progressDAO(QueueDatabase database) {
        return database.progressDAO();
    }

    @Provides
    static ExtraDAO extraDAO(QueueDatabase database) {
        return database.extraDAO();
    }

    @Provides
    static StateDAO stateDAO(QueueDatabase database) {
        return database.stateDAO();
    }

    @Provides
    static TurnAlarmDAO turnAlarmDao(QueueDatabase database) {
        return database.turnAlarmDAO();
    }

    @Provides
    static ShortMessageDAO shortMessageDao(QueueDatabase database) {
        return database.shortMessageDAO();
    }

    @Provides
    static LogDAO logDao(QueueDatabase database) {
        return database.logDAO();
    }

    @Provides
    static WebPageDAO webPageDAO(QueueDatabase database) {
        return database.webPageDAO();
    }

    @Provides
    @Singleton
    static Retrofit getRetrofitClient() {
        return AbstractQueueApplication.buildRetrofitClient();
    }

    @Provides
    @Singleton
    static CoreAPI getCoreAPI(Retrofit retrofit) {
        return retrofit.create(CoreAPI.class);
    }

    @Provides
    @Singleton
    static ClientAPI getClientAPI(Retrofit retrofit) {
        return retrofit.create(ClientAPI.class);
    }

    @Binds
    BuildConfiguration getBuildConf(ClientBuildConfiguration configuration);

    @Binds
    QueueActivitySupport getActivitySupport(ClientActivitySupport activitySupport);

    @Binds
    AbstractActivationRepository getAbstractActivationRepository(ClientActivationRepository repository);

    @Binds
    AbstractUpdateHandler getUpdateHandler(ClientUpdateHandler updateHandler);


//    @Provides
//    @Singleton
//    static ExtraRepository getExtraRepository(Lazy<ExtraDAO> extraDAO) {
//        return new ExtraRepository(extraDAO);
//    }
//
//    @Provides
//    @Singleton
//    static DashboardRepository getDashboardRepository(Lazy<ProgressDAO> progressDAO, Lazy<CoreAPI> coreAPI, Lazy<ServicesListRepository> servicesListRepository, Lazy<TimeCalculator> timeCalculator, StateRepository stateRepository, BuildConfiguration buildConfiguration, RepositoryUtil repositoryUtil) {
//        return new DashboardRepository(progressDAO, coreAPI, servicesListRepository, timeCalculator, stateRepository, buildConfiguration, repositoryUtil);
//    }
}
