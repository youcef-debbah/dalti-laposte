package dz.jsoftware95.silverbox.android.middleware;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import java.util.Map;

import dz.jsoftware95.silverbox.android.common.Check;

@MainThread
public final class ModelProvider {

    private final Map<Class<?>, StatefulModelFactory> factories;

    //    @Inject
    public ModelProvider(final Map<Class<?>, StatefulModelFactory> factories) {
        this.factories = factories;
    }

    public <T extends StatefulModel> T get(@NonNull final StatefulContext context,
                                           @NonNull final Class<T> modelClass) {
        final StatefulModelFactory factory = Check.nonNull(factories.get(modelClass));
        return new ViewModelProvider(context.getViewModelStore(), factory).get(modelClass);
    }
}
