package dz.jsoftware95.silverbox.android.backend;

import androidx.lifecycle.LiveData;

public interface DataLoader<V> {

    LiveData<V> load(long id);
}
