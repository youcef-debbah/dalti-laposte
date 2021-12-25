package dz.jsoftware95.silverbox.android.backend;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class DatabaseUtils {

    private static final String TAG = DatabaseUtils.class.getSimpleName();

    private DatabaseUtils() {
        throw new IllegalAccessError();
    }

    public static <E extends Item> List<E> filterValid(Collection<E> entities) {
        if (entities == null || entities.isEmpty())
            return new LinkedList<>();
        else {
            ArrayList<E> result = new ArrayList<>(entities.size());

            for (E entity : entities)
                if (entity.isValid())
                    result.add(entity);
                else
                    Log.w(TAG, "invalid entity dropped: " + entity);

            return result;
        }
    }

    public static <E extends Item> E findItem(long id, Collection<E> items) {
        if (items != null)
            for (E item : items)
                if (item != null && item.getId() == id)
                    return item;
        return null;
    }
}
