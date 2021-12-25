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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;

import dz.jsoftware95.silverbox.android.common.Assert;
import dz.jsoftware95.silverbox.android.common.Check;
import dz.jsoftware95.silverbox.android.frontend.ForegroundService;

@UiThread
public abstract class BasicFragment extends StatefulFragment {

    private static final String FRAGMENT_NOT_INJECTED = "Fragment not injected: ";
    private static final String BINDING_ACTIVITIES_NEEDED_NOT = "Binding Fragments can only be attached to "
            + "Binding Activities not: ";

    private final Collection<MutableStateOwner> stateOwners = new ArrayList<>(1);
    private final Bundle stateToBeSaved = new Bundle();

    @Nullable
    private StatefulActivity statefulActivity = null;
    @Nullable
    private ViewDataBinding binding = null;

    private boolean injected = false;

    @Override
    public void onAttach(final Context context) {
        if (context instanceof StatefulActivity) {
            super.onAttach(context);
            statefulActivity = (StatefulActivity) context;
        } else
            throw new IllegalArgumentException(BINDING_ACTIVITIES_NEEDED_NOT + context);
    }

    @Override
    @NonNull
    public <T extends StatefulModel> T getViewModel(Class<T> modelClass) {
        return getStatefulActivity().getViewModel(modelClass);
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedState) {
        super.onActivityCreated(savedState);
        loadState(savedState);
    }

    @Override
    public void onPause() {
        super.onPause();
        stateToBeSaved.clear();
        saveState(stateToBeSaved);
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putAll(stateToBeSaved);
    }

    @Override
    public void onDestroyView() {
        setBinding(null);
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        stateOwners.clear();
        statefulActivity = null;
        super.onDetach();
    }

    @Override
    protected final void linkState(@NonNull final MutableStateOwner stateOwner) {
        stateOwners.add(Assert.nonNull(stateOwner));
    }

    @Override
    protected final void unlinkState(@NonNull final MutableStateOwner stateOwner) {
        stateOwners.remove(Assert.nonNull(stateOwner));
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

    @Inject
    public void markAsInjected() {
        Check.not(injected);
        this.injected = true;
    }

    @Override
    protected final void setBinding(@Nullable final ViewDataBinding binding) {
        if (this.binding != binding) {
            if (this.binding != null)
                this.binding.setLifecycleOwner(null);

            if (binding != null)
                binding.setLifecycleOwner(getViewLifecycleOwner());

            this.binding = binding;
        }
    }

    @Nullable
    @Override
    public ViewDataBinding getBinding() {
        return binding;
    }

    @NonNull
    @Override
    public StatefulActivity getStatefulActivity() {
        return Check.nonNull(statefulActivity);
    }

    @NonNull
    public Lifecycle getViewLifecycle() {
        return getViewLifecycleOwner().getLifecycle();
    }

    @NonNull
    @NotNull
    @Override
    // a workaround until I figure why the super method cause bugs
    public LifecycleOwner getViewLifecycleOwner() {
        return requireActivity();
    }

    public boolean startServiceThenUpgradeToForeground(Class<? extends ForegroundService> serviceClass) {
        if (statefulActivity != null)
            return statefulActivity.startServiceThenUpgradeToForeground(serviceClass);
        else
            return false;
    }

    public void startActivity(Class<? extends Activity> activityClass) {
        startActivity(new Intent(requireContext(), activityClass));
    }

    public void openActivity(Class<? extends Activity> activityClass) {
        Intent intent = new Intent(requireContext(), activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public boolean openSystemActivity(String action) {
        try {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(action);
            startActivity(intent);
            return true;
        } catch (RuntimeException e) {
            Log.w(TAG, "could not start system activity using action: " + action);
            return false;
        }
    }
}
