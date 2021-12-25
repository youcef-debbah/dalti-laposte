package dz.jsoftware95.silverbox.android.common;

import java.util.Map;
import java.util.Objects;

import dz.jsoftware95.queue.common.BiFunction;
import dz.jsoftware95.queue.common.Function;

public class CollectionUtil {

    public static <K, V> V computeIfAbsent(Map<K, V> map, K key,
                                           Function<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(map);
        Objects.requireNonNull(mappingFunction);
        V oldValue, newValue;
        return ((oldValue = map.get(key)) == null
                && (newValue = mappingFunction.apply(key)) != null
                && (oldValue = putIfAbsent(map, key, newValue)) == null)
                ? newValue
                : oldValue;
    }

    public static <K, V> V putIfAbsent(Map<K, V> map, K key, V value) {
        V v = map.get(key);
        if (v == null) {
            v = map.put(key, value);
        }

        return v;
    }

    public static <K, V> V compute(Map<K, V> map, K key,
                                   BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        retry:
        for (; ; ) {
            V oldValue = map.get(key);
            // if putIfAbsent fails, opportunistically use its return value
            haveOldValue:
            for (; ; ) {
                V newValue = remappingFunction.apply(key, oldValue);
                if (newValue != null) {
                    if (oldValue != null) {
                        if (replace(map, key, oldValue, newValue))
                            return newValue;
                    } else if ((oldValue = putIfAbsent(map, key, newValue)) == null)
                        return newValue;
                    else continue haveOldValue;
                } else if (oldValue == null || remove(map, key, oldValue)) {
                    return null;
                }
                continue retry;
            }
        }
    }

    public static <K, V> boolean replace(Map<K, V> map, K key, V oldValue, V newValue) {
        Object curValue = map.get(key);
        if (!Objects.equals(curValue, oldValue) ||
                (curValue == null && !map.containsKey(key))) {
            return false;
        }
        map.put(key, newValue);
        return true;
    }

    public static <K, V> boolean remove(Map<K, V> map, K key, Object value) {
        Object curValue = map.get(key);
        if (!Objects.equals(curValue, value) ||
                (curValue == null && !map.containsKey(key))) {
            return false;
        }
        map.remove(key);
        return true;
    }

}
