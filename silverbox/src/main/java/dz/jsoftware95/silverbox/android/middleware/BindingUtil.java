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

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

import dz.jsoftware95.silverbox.android.common.Check;

public final class BindingUtil {

    private BindingUtil() {
        throw new UnsupportedOperationException("bad boy! no instance for you");
    }

    @NonNull
    public static <T extends ViewDataBinding> T setContentView(@NonNull final StatefulActivity activity,
                                                 final int layoutId) {
        final T binding = DataBindingUtil.setContentView(activity, layoutId);
        Check.nonNull(binding, "the activity layout is not a data binding layout");

        activity.setBinding(binding);

        return binding;
    }

    @NonNull
    public static <T extends ViewDataBinding> T inflate(@NonNull final LayoutInflater inflater,
                                          final int layoutId,
                                          @Nullable final ViewGroup container,
                                          final boolean attach) {
        final T binding = DataBindingUtil.inflate(inflater, layoutId, container, attach);
        Check.nonNull(binding, "the layout is not a data binding layout");
        return binding;
    }

    @NonNull
    public static <T extends ViewDataBinding> T inflate(@NonNull final LayoutInflater inflater,
                                          final int layoutId,
                                          @Nullable final ViewGroup container) {
        return inflate(inflater, layoutId, container, false);
    }

    @NonNull
    public static <T extends ViewDataBinding> T inflate(@NonNull StatefulFragment fragment,
                                          @NonNull final LayoutInflater inflater,
                                          final int layoutId,
                                          @Nullable final ViewGroup container) {
        final T binding = inflate(inflater, layoutId, container, false);
        fragment.setBinding(binding);
        return binding;
    }

    @NonNull
    public static <T extends ViewDataBinding> T inflate(@NonNull final LayoutInflater inflater,
                                          final int layoutId) {
        return inflate(inflater, layoutId, null, false);
    }
}
