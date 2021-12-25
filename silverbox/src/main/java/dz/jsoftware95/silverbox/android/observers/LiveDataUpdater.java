package dz.jsoftware95.silverbox.android.observers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import org.jetbrains.annotations.NotNull;

public class LiveDataUpdater<T> extends UnConcurrentObserver<MutableLiveData<T>, T> {

    private volatile Long timestamp;

    public LiveDataUpdater(@NotNull MutableLiveData<T> liveData) {
        super(liveData);
    }

    @Override
    protected void onUpdate(@NonNull MutableLiveData<T> liveData, @Nullable T data) {
        if (data instanceof Chronological)
            updateChronologicalData(liveData, data);
        else
            updateData(liveData, data);
    }

    private void updateChronologicalData(@NonNull MutableLiveData<T> liveData, @NonNull T data) {
        Long dataTimestamp = ((Chronological) data).getTimestamp();
        if (dataTimestamp == null)
            updateData(liveData, data);
        else {
            Long timestamp = this.timestamp;
            if (timestamp == null || dataTimestamp >= timestamp) {
                this.timestamp = dataTimestamp;
                updateData(liveData, data);
            }
        }
    }

    private void updateData(@NonNull MutableLiveData<T> liveData, @Nullable T data) {
        liveData.postValue(data);
    }
}
