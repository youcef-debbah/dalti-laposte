/*
 * Copyright (c) 2018 Youcef DEBBAH (youcef-debbah@hotmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the Software) to deal in the Software without restriction
 * but under the following conditions:
 *
 * - This notice shall be included in all copies and portions of the Software.
 * - The software is provided "AS IS", WITHOUT WARRANTY OF ANY KIND (Implicit or Explicit).
 *
 */

package dz.jsoftware95.silverbox.android.middleware;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.UiThread;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModelProvider;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;

import dz.jsoftware95.silverbox.android.common.Assert;
import dz.jsoftware95.silverbox.android.frontend.ForegroundService;

@UiThread
public abstract class BasicActivity extends StatefulActivity {

    private static final String ACTIVITY_NOT_INJECTED = "Activity not injected: ";
    public static volatile BasicActivity CURRENT_STARTED_ACTIVITY = null;

    private final Collection<MutableStateOwner> stateOwners = new ArrayList<>(1);
    private final Bundle stateToBeSaved = new Bundle();

    @Nullable
    private ViewDataBinding binding = null;

    private boolean injected = false;

    @Override
    protected void onCreate(final Bundle savedState) {
        super.onCreate(savedState);

        if (savedState == null)
            importState(getIntent());
        else
            loadState(savedState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        CURRENT_STARTED_ACTIVITY = this;
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        importState(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stateToBeSaved.clear();
        saveState(stateToBeSaved);
    }

    @Override
    protected void onSaveInstanceState(final @NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putAll(stateToBeSaved);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (this == CURRENT_STARTED_ACTIVITY)
            CURRENT_STARTED_ACTIVITY = null;
    }

    @Override
    protected void onDestroy() {
        stateOwners.clear();
        setBinding(null);
        super.onDestroy();
    }

    @Override
    @NonNull
    public final <T extends StatefulModel> T getViewModel(Class<T> viewModelClass) {
        return linkState(new ViewModelProvider(this).get(viewModelClass));
    }

    @Override
    protected final <T extends MutableStateOwner> T linkState(@NonNull final T stateOwner) {
        stateOwners.add(Assert.nonNull(stateOwner));
        return stateOwner;
    }

    @Override
    protected final <T extends MutableStateOwner> T unlinkState(@NonNull final T stateOwner) {
        stateOwners.remove(Assert.nonNull(stateOwner));
        return stateOwner;
    }

    @Override
    protected final void saveState(@NonNull final Bundle state) {
        for (final MutableStateOwner stateOwner : stateOwners)
            stateOwner.saveState(state);
    }

    @Override
    protected final void loadState(@Nullable final Bundle state) {
        for (final MutableStateOwner stateOwner : stateOwners)
            stateOwner.loadState(state);
    }

    @Override
    public void exportState(@NonNull final Intent intent) {
        for (final MutableStateOwner stateOwner : stateOwners)
            stateOwner.exportState(intent);
    }

    @Override
    public void exportState(@NonNull final Intent intent,
                            @NonNull final Class<? extends StateOwner> receiver) {
        for (final MutableStateOwner stateOwner : stateOwners)
            stateOwner.exportState(intent, receiver);
    }

    @Override
    public void importState(@Nullable final Intent intent) {
        for (final MutableStateOwner stateOwner : stateOwners)
            stateOwner.importState(intent);
    }

    @Override
    public void importState(@Nullable final Intent intent,
                            @NonNull final Class<? extends StateOwner> receiver) {
        for (final MutableStateOwner stateOwner : stateOwners)
            stateOwner.importState(intent, receiver);
    }

    @Override
    @CallSuper
    protected void setBinding(@Nullable final ViewDataBinding binding) {
        if (this.binding != binding) {
            if (this.binding != null)
                this.binding.setLifecycleOwner(null);

            if (binding != null)
                binding.setLifecycleOwner(this);

            this.binding = binding;
        }
    }

    @Nullable
    @Override
    public ViewDataBinding getBinding() {
        return binding;
    }

    @Inject
    public void markAsInjected() {
        Assert.not(injected);
        this.injected = true;
    }

    @Nullable
    public <T extends View> T findViewById(@IdRes Integer id) {
        return id == null ? null : super.findViewById(id);
    }

    @Override
    public boolean startServiceThenUpgradeToForeground(Class<? extends ForegroundService> serviceClass) {
        Intent intent = new Intent(this, serviceClass);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            startService(intent);
            return true;
        } else
            return bindServiceThenUpgradeToForeground(intent);
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.O)
    public boolean bindServiceThenUpgradeToForeground(Intent intent) {
        try {
            return bindService(intent, new ForegroundService.UpgradeToForegroundConnection(this), Service.BIND_AUTO_CREATE);
        } catch (RuntimeException e) {
            Log.e(TAG, "could not bind to CompactDashboardService", e);
            return false;
        }
    }
}
