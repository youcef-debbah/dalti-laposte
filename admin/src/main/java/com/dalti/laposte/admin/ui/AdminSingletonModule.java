package com.dalti.laposte.admin.ui;

import android.app.Application;

import androidx.room.Room;

import com.dalti.laposte.admin.repositories.AdminAPI;
import com.dalti.laposte.admin.repositories.AdminActivationRepository;
import com.dalti.laposte.admin.repositories.AdminBuildConfiguration;
import com.dalti.laposte.admin.repositories.AdminUpdateHandler;
import com.dalti.laposte.core.repositories.AbstractActivationRepository;
import com.dalti.laposte.core.repositories.AbstractUpdateHandler;
import com.dalti.laposte.core.repositories.ActivationDAO;
import com.dalti.laposte.core.repositories.AdminAlarmDAO;
import com.dalti.laposte.core.repositories.CoreAPI;
import com.dalti.laposte.core.repositories.ExtraDAO;
import com.dalti.laposte.core.repositories.LogDAO;
import com.dalti.laposte.core.repositories.ProgressDAO;
import com.dalti.laposte.core.repositories.QueueDatabase;
import com.dalti.laposte.core.repositories.ServiceDAO;
import com.dalti.laposte.core.repositories.ShortMessageDAO;
import com.dalti.laposte.core.repositories.StateDAO;
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
public interface AdminSingletonModule {

    @Provides
    @Singleton
    static QueueDatabase getDatabase(final Application context) {
        return Room.databaseBuilder(context, QueueDatabase.class, QueueDatabase.NAME).build();
    }

    @Provides
    static ActivationDAO activationDAO(QueueDatabase database) {
        return database.activationDAO();
    }

    @Provides
    static AdminAlarmDAO adminAlarmDAO(QueueDatabase database) {
        return database.adminAlarmDAO();
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
    static LogDAO logDao(QueueDatabase database) {
        return database.logDAO();
    }

    @Provides
    static WebPageDAO webPageDAO(QueueDatabase database) {
        return database.webPageDAO();
    }

    @Provides
    static ShortMessageDAO shortMessageDao(QueueDatabase database) {
        return database.shortMessageDAO();
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
    static AdminAPI getAdminAPI(Retrofit retrofit) {
        return retrofit.create(AdminAPI.class);
    }

    @Binds
    BuildConfiguration getBuildConf(AdminBuildConfiguration configuration);

    @Binds
    QueueActivitySupport getActivitySupport(AdminActivitySupport activitySupport);

    @Binds
    AbstractActivationRepository getAbstractActivationRepository(AdminActivationRepository repository);

    @Binds
    AbstractUpdateHandler getUpdateHandler(AdminUpdateHandler updateHandler);
}
