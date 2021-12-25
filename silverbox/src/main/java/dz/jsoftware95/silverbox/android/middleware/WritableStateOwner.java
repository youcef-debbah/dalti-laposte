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

import android.os.Bundle;
import android.os.Parcelable;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;

public interface WritableStateOwner {

    /**
     * Inserts a {@code byte} value into the mapping of this instance, replacing
     * any existing value for the given {@code key}.
     *
     * @param key   {@code key} with which the specified value is to be associated
     * @param value a {@code byte} to be associated with the specified {@code key}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    void putByte(@NonNull final String key, final byte value);

    /**
     * Inserts a {@code boolean} value into the mapping of this instance, replacing
     * any existing value for the given {@code key}.
     *
     * @param key   {@code key} with which the specified value is to be associated
     * @param value a {@code boolean} to be associated with the specified {@code key}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    void putBoolean(@NonNull final String key, final boolean value);

    /**
     * Inserts a {@code char} value into the mapping of this instance, replacing
     * any existing value for the given {@code key}.
     *
     * @param key   {@code key} with which the specified value is to be associated
     * @param value a {@code char} to be associated with the specified {@code key}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    void putChar(@NonNull final String key, final char value);

    /**
     * Inserts a {@code short} value into the mapping of this instance, replacing
     * any existing value for the given {@code key}.
     *
     * @param key   {@code key} with which the specified value is to be associated
     * @param value a {@code short} to be associated with the specified {@code key}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    void putShort(@NonNull final String key, final short value);

    /**
     * Inserts a {@code float} value into the mapping of this instance, replacing
     * any existing value for the given {@code key}.
     *
     * @param key   {@code key} with which the specified value is to be associated
     * @param value a {@code float} to be associated with the specified {@code key}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    void putFloat(@NonNull final String key, final float value);

    /**
     * Inserts a {@code CharSequence} value into the mapping of this instance, replacing
     * any existing value for the given {@code key}.  Either {@code key} or value may be {@code null}.
     *
     * @param key   {@code key} with which the specified value is to be associated
     * @param value a {@code CharSequence} to be associated with the specified {@code key}, or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    void putCharSequence(@NonNull final String key, @Nullable final CharSequence value);

    /**
     * Inserts a {@code String} value into the mapping of this instance, replacing
     * any existing value for the given {@code key}.  Either {@code key} or value may be {@code null}.
     *
     * @param key   {@code key} with which the specified value is to be associated
     * @param value a {@code String} to be associated with the specified {@code key}, or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    void putString(@NonNull final String key, @Nullable final String value);

    /**
     * Inserts a {@code Parcelable} value into the mapping of this instance, replacing
     * any existing value for the given {@code key}.  Either {@code key} or value may be {@code null}.
     *
     * @param key   {@code key} with which the specified value is to be associated
     * @param value a {@code Parcelable} instance to be associated with the specified {@code key}, or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    void putParcelable(@NonNull final String key, @Nullable final Parcelable value);

    /**
     * Inserts an {@code array} of {@code Parcelable} values into the mapping of this instance,
     * replacing any existing value for the given {@code key}.  Either {@code key} or value may
     * be {@code null}.
     *
     * @param key   {@code key} with which the specified value is to be associated
     * @param value an {@code array} of {@code Parcelable} instances to be associated with the
     *              specified {@code key}, or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    void putParcelableArray(@NonNull final String key, @Nullable final Parcelable[] value);

    /**
     * Inserts a {@code List} of {@code Parcelable} values into the mapping of this instance,
     * replacing any existing value for the given {@code key}.  Either {@code key} or value may
     * be {@code null}.
     *
     * @param key   {@code key} with which the specified value is to be associated
     * @param value an {@code ArrayList} of {@code Parcelable} instances to be associated with the
     *              specified {@code key}, or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    void putParcelableArrayList(@NonNull final String key,
                                @Nullable final ArrayList<? extends Parcelable> value);

    /**
     * Inserts a {@code SparseArray} of {@code Parcelable} values into the mapping of this instance,
     * replacing any existing value for the given {@code key}.  Either {@code key} or value may be {@code null}.
     *
     * @param key   {@code key} with which the specified value is to be associated
     * @param value a {@code SparseArray} of {@code Parcelable} instances to be associated with the
     *              specified {@code key}, or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    void putSparseParcelableArray(@NonNull final String key,
                                  @Nullable final SparseArray<? extends Parcelable> value);

    /**
     * Inserts an {@code ArrayList<Integer>} value into the mapping of this instance, replacing
     * any existing value for the given {@code key}.  Either {@code key} or value may be {@code null}.
     *
     * @param key   {@code key} with which the specified value is to be associated
     * @param value an {@code ArrayList} of {@code Integer} instances to be associated with the specified {@code key},
     *              or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    void putIntegerArrayList(@NonNull final String key, @Nullable final ArrayList<Integer> value);

    /**
     * Inserts an {@code ArrayList<String>} value into the mapping of this instance, replacing
     * any existing value for the given {@code key}.  Either {@code key} or value may be {@code null}.
     *
     * @param key   {@code key} with which the specified value is to be associated
     * @param value an {@code ArrayList} of {@code String} instances to be associated with the specified {@code key},
     *              or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    void putStringArrayList(@NonNull final String key, @Nullable final ArrayList<String> value);

    /**
     * Inserts an {@code ArrayList<CharSequence>} value into the mapping of this instance, replacing
     * any existing value for the given {@code key}.  Either {@code key} or value may be {@code null}.
     *
     * @param key   {@code key} with which the specified value is to be associated
     * @param value an {@code ArrayList} of CharSequence instances to be associated with the specified {@code key},
     *              or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    void putCharSequenceArrayList(@NonNull final String key,
                                  @Nullable final ArrayList<CharSequence> value);

    /**
     * Inserts a {@code Serializable} value into the mapping of this Bundle, replacing
     * any existing value for the given {@code key}.  Either {@code key} or value may be {@code null}.
     *
     * @param key   {@code key} with which the specified value is to be associated
     * @param value a {@code Serializable} instance to be associated with the specified {@code key},
     *              or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    void putSerializable(@NonNull final String key, @Nullable final Serializable value);

    /**
     * Inserts a {@code byte array} value into the mapping of this instance, replacing
     * any existing value for the given {@code key}.  Either {@code key} or value may be {@code null}.
     *
     * @param key   {@code key} with which the specified value is to be associated
     * @param value a {@code byte array} instance to be associated with the specified {@code key},
     *              or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    void putByteArray(@NonNull final String key, @Nullable final byte[] value);

    /**
     * Inserts a {@code short array} value into the mapping of this instance, replacing
     * any existing value for the given {@code key}.  Either {@code key} or value may be {@code null}.
     *
     * @param key   {@code key} with which the specified value is to be associated
     * @param value a {@code short array} instance to be associated with the specified {@code key},
     *              or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    void putShortArray(@NonNull final String key, @Nullable final short[] value);

    /**
     * Inserts a {@code char array} value into the mapping of this instance, replacing
     * any existing value for the given {@code key}.  Either {@code key} or value may be {@code null}.
     *
     * @param key   {@code key} with which the specified value is to be associated
     * @param value a {@code char array} instance to be associated with the specified {@code key},
     *              or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    void putCharArray(@NonNull final String key, @Nullable final char[] value);

    /**
     * Inserts a {@code float array} value into the mapping of this instance, replacing
     * any existing value for the given {@code key}.  Either {@code key} or value may be {@code null}.
     *
     * @param key   {@code key} with which the specified value is to be associated
     * @param value a {@code float array} instance to be associated with the specified {@code key},
     *              or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    void putFloatArray(@NonNull final String key, @Nullable final float[] value);

    /**
     * Inserts a {@code CharSequence array} value into the mapping of this instance, replacing
     * any existing value for the given {@code key}.  Either {@code key} or value may be {@code null}.
     *
     * @param key   {@code key} with which the specified value is to be associated
     * @param value a {@code CharSequence array} instance to be associated with the specified {@code key},
     *              or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    void putCharSequenceArray(@NonNull final String key, @Nullable final CharSequence[] value);

    /**
     * Inserts a {@code Bundle} value into the mapping of this instance, replacing
     * any existing value for the given {@code key}.  Either {@code key} or value may be {@code null}.
     *
     * @param key   {@code key} with which the specified value is to be associated
     * @param value a {@code Bundle} instance to be associated with the specified {@code key},
     *              or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    void putBundle(@NonNull final String key, @Nullable final Bundle value);
}