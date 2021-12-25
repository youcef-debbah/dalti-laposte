package dz.jsoftware95.silverbox.android.common;

import androidx.annotation.MainThread;

import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import dz.jsoftware95.queue.common.Function;

@MainThread
public class Cache<K, V> {
    private final Map<K, WeakReference<V>> data;

    public Cache() {
        this(8);
    }

    public Cache(int initialCapacity) {
        this.data = new HashMap<>(initialCapacity);
    }

    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        Check.isMainThread();
        Objects.requireNonNull(mappingFunction);
        V oldValue, newValue;
        return ((oldValue = get(key)) == null
                && (newValue = mappingFunction.apply(key)) != null
                && (oldValue = putIfAbsent(key, newValue)) == null)
                ? newValue
                : oldValue;
    }

    public V get(K key) {
        return valueOf(data.get(key));
    }

    public V put(K key, V value) {
        return valueOf(data.put(key, new WeakReference<>(value)));
    }

    public V putIfAbsent(K key, V value) {
        V v = get(key);
        if (v == null)
            v = put(key, value);
        return v;
    }

    @Nullable
    private static <T> T valueOf(WeakReference<T> reference) {
        return reference != null ? reference.get() : null;
    }
}
