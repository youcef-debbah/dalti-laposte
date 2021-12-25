package dz.jsoftware95.silverbox.android.middleware;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import javax.inject.Provider;

import dz.jsoftware95.silverbox.android.common.Check;

public final class StatefulModelFactory implements ViewModelProvider.Factory {

    private final Provider<? extends ViewModel> provider;

    public StatefulModelFactory(final Provider<? extends ViewModel> provider) {
        this.provider = provider;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull final Class<T> modelClass) {
        return Check.nonNull(modelClass.cast(provider.get()));
    }
}
