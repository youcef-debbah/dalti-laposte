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

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;

public abstract class StatefulFragment extends Fragment implements
        StateOwner,
        StatefulContext {

    protected final String TAG = getClass().getSimpleName();

    @Override
    public void onAttach(@SuppressWarnings("NullableProblems") Context context) {
        super.onAttach(context);
    }

    protected abstract void linkState(@NonNull MutableStateOwner stateOwner);

    protected abstract void unlinkState(@NonNull MutableStateOwner stateOwner);

    protected abstract void saveState(@NonNull Bundle state);

    protected abstract void loadState(@Nullable Bundle state);

    protected abstract void setBinding(@NonNull ViewDataBinding binding);

    @NonNull
    public abstract StatefulActivity getStatefulActivity();

    @NonNull
    public abstract <T extends StatefulModel> T getViewModel(Class<T> modelClass);
}
