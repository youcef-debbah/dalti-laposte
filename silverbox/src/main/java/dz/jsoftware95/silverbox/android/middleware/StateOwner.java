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

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface StateOwner {

    /**
     * Saves the current exportable state of this instance into the given {@code Intent} as an
     * {@linkplain Intent#putExtra(String, Bundle) extra bundle} using the runtime class name of this instance as a
     * bundle name
     *
     * @param intent the intent to save current exportable state into it's extra
     */
    void exportState(@NonNull final Intent intent);

    /**
     * Saves the current exportable state of this instance into the given {@code Intent} as an
     * {@linkplain Intent#putExtra(String, Bundle) extra bundle} using the runtime name of {@code receiver} as a
     * bundle name
     *
     * @param intent the intent to save current exportable state into it's extra
     */
    void exportState(@NonNull final Intent intent, @NonNull final Class<? extends StateOwner> receiver);

    /**
     * Loads state from the extra bundle (that has identical name to the runtime class name of this instance) in the
     * given intent to the current exportable state
     *
     * @param intent the intent that contains new exportable state in it's extra
     */
    void importState(@Nullable final Intent intent);

    /**
     * Loads state from the extra bundle (that has identical name to the runtime class name of {@code receiver}) in the
     * given intent to the current exportable state
     *
     * @param intent the intent that contains new exportable state in it's extra
     * @throws NullPointerException if {@code receiver} is {@code null}
     */
    void importState(@Nullable final Intent intent, @NonNull final Class<? extends StateOwner> receiver);
}
