package com.dalti.laposte.core.repositories;

import androidx.annotation.AnyThread;
import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dalti.laposte.core.util.QueueUtils;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Lazy;
import dz.jsoftware95.silverbox.android.backend.LazyRepository;
import dz.jsoftware95.silverbox.android.common.CollectionUtil;
import dz.jsoftware95.silverbox.android.common.StringUtil;
import dz.jsoftware95.silverbox.android.concurrent.DuoDatabaseJob;
import dz.jsoftware95.silverbox.android.concurrent.Job;
import dz.jsoftware95.silverbox.android.concurrent.UnDatabaseJob;

@Singleton
@AnyThread
public class ExtraRepository extends LazyRepository<ExtraDAO> {

    private final ConcurrentMap<Long, MutableLiveData<String>> cache = new ConcurrentHashMap<>(InputProperty.values().length);
    private final ConcurrentMap<String, Long> longStore = new ConcurrentHashMap<>(2);
    private final ConcurrentMap<StringPreference, LiveStringProperty> properties = new ConcurrentHashMap<>(4);

    @Inject
    @AnyThread
    public ExtraRepository(@NonNull final Lazy<ExtraDAO> extraDAO) {
        super(extraDAO);
//        initializeIfNeeded();LAST_SMS_VERIFICATION_TIME
    }

    @Override
    @CallSuper
    protected void ontInitialize() {
        if (QueueUtils.isTesting()) {
            ExtraDAO extraDAO = requireDAO();
            setString(StringSetting.CONTACT_PHONE, "+213664319751");
            extraDAO.putIfAbsent(new Extra(InputProperty.PRINCIPAL_NAME, "test-user"));
            extraDAO.putIfAbsent(new Extra(InputProperty.PRINCIPAL_PASSWORD, "test-password"));
        }
    }

    public void setString(StringPreference preference, String value) {
        getLiveStringProperty(preference).set(value);
    }

    public LiveData<String> getString(StringPreference preference) {
        return getLiveStringProperty(preference);
    }

    public void resetString(StringPreference preference) {
        getLiveStringProperty(preference).reset();
    }

    private LiveStringProperty getLiveStringProperty(StringPreference preference) {
        return CollectionUtil.computeIfAbsent(properties, preference, LiveStringProperty::new);
    }

    public ConcurrentMap<String, Long> getLongStore() {
        return longStore;
    }

    public void remove(@NonNull Property property) {
        execute(newRemoveJob(this, property.key()));
    }

    @WorkerThread
    public void removeAndWait(@NonNull Property property) throws InterruptedException {
        executeAndWait(newRemoveJob(this, property.key()));
    }

    private static Job newRemoveJob(ExtraRepository repository, long key) {
        return new UnDatabaseJob<ExtraRepository>(repository) {
            @Override
            protected void doFromBackground(@NonNull ExtraRepository context) {
                context.requireDAO().remove(key);
                context.putInCache(key, null);
            }
        };
    }

    public void put(@NonNull Property property, @Nullable Object value) {
        put(property, StringUtil.toString(value));
    }

    public void put(@NonNull Property property, @Nullable String value) {
        if (property.isNull(value))
            remove(property);
        else
            execute(newPutJob(this, new Extra(property.key(), value)));
    }

    @WorkerThread
    public void putAndWait(@NonNull Property property, @Nullable String value) throws InterruptedException {
        if (property.isNull(value))
            removeAndWait(property);
        else
            executeAndWait(newPutJob(this, new Extra(property.key(), value)));
    }

    private static Job newPutJob(ExtraRepository repository, Extra extra) {
        return new DuoDatabaseJob<ExtraRepository, Extra>(repository, extra) {
            @Override
            protected void doFromBackground(@NonNull ExtraRepository context, @NonNull Extra entity) {
                context.requireDAO().put(entity);
                context.putInCache(entity.getId(), entity.getValue());
            }
        };
    }

    private void putInCache(long id, @Nullable String value) {
        MutableLiveData<String> oldData = cache.get(id);
        if (oldData != null)
            oldData.postValue(value);
        else {
            MutableLiveData<String> newData = new MutableLiveData<>(value);
            oldData = cache.putIfAbsent(id, newData);
            if (oldData != null)
                oldData.postValue(value);
        }
    }

    @MainThread
    public LiveData<String> get(@NonNull Property property) {
        return getFromCache(property.key());
    }

    @WorkerThread
    public String getAndWait(@NonNull Property property) throws InterruptedException {
        long key = property.key();
        String value = waitForDAO("reading property: " + property).get(key);
        putInCache(key, value);
        return value;
    }

    @WorkerThread
    public Map<Property, String> getAndWait(@NonNull Collection<Property> properties) throws InterruptedException {
        Map<Property, String> result = new LinkedHashMap<>(properties.size());
        ExtraDAO dao = waitForDAO("reading properties in: " + properties);

        for (Property property : properties) {
            long key = property.key();
            String value = dao.get(key);
            result.put(property, value);
            putInCache(key, value);
        }

        return result;
    }

    @MainThread
    private LiveData<String> getFromCache(long key) {
        MutableLiveData<String> oldValue = cache.get(key);
        if (oldValue != null)
            return oldValue;
        else {
            MutableLiveData<String> newValue = new MutableLiveData<>();
            oldValue = cache.putIfAbsent(key, newValue);
            if (oldValue != null)
                return oldValue;
            else {
                execute(newPostValueJob(this, newValue, key));
                return newValue;
            }
        }
    }

    private static Job newPostValueJob(ExtraRepository repository, MutableLiveData<String> newValue, long key) {
        return new DuoDatabaseJob<MutableLiveData<String>, ExtraRepository>(newValue, repository) {
            @Override
            protected void doFromBackground(@NonNull MutableLiveData<String> liveData, @NonNull ExtraRepository repository) {
                liveData.postValue(repository.requireDAO().get(key));
            }
        };
    }

    //    private LiveData<String> get(long key) {
    //        MutableLiveData<String> oldValue, newValue;
    //        return ((oldValue = values.get(key)) == null
    //                && (newValue = getNewLiveData(key)) != null
    //                && (oldValue = values.putIfAbsent(key, newValue)) == null)
    //                ? newValue : oldValue;
    //    }
}
