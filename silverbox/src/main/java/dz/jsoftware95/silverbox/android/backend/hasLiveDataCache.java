package dz.jsoftware95.silverbox.android.backend;

import androidx.annotation.MainThread;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

public interface hasLiveDataCache<K, V> {

    ConcurrentMap<K, LiveDataWrapper<V>> getCache();

    @MainThread
    void initCacheEntry(K key, LiveDataWrapper<V> newValue);

    default void putInCache(K key, @Nullable V value) {
        Objects.requireNonNull(key);
        ConcurrentMap<K, LiveDataWrapper<V>> cache = getCache();
        LiveDataWrapper<V> oldData = cache.get(key);
        if (oldData != null)
            oldData.postValue(value);
        else {
            LiveDataWrapper<V> newData = new LiveDataWrapper<>(value);
            oldData = cache.putIfAbsent(key, newData);
            if (oldData != null)
                oldData.postValue(value);
        }
    }

    @MainThread
    default LiveData<V> getFromCache(K key) {
        Objects.requireNonNull(key);
        ConcurrentMap<K, LiveDataWrapper<V>> cache = getCache();
        LiveDataWrapper<V> oldValue = cache.get(key);
        if (oldValue != null)
            return oldValue.getLiveData();
        else {
            LiveDataWrapper<V> newValue = new LiveDataWrapper<>();
            oldValue = cache.putIfAbsent(key, newValue);
            if (oldValue != null)
                return oldValue.getLiveData();
            else {
                initCacheEntry(key, newValue);
                return newValue.getLiveData();
            }
        }
    }
}
