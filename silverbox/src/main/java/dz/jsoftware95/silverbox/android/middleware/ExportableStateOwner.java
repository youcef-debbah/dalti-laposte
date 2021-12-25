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

public interface ExportableStateOwner {

    /**
     * Exports (puts) a {@code byte} value into the exportable state of this instance, replacing
     * any existing value for the given {@code key}.
     *
     * @param key   {@code key} with which the specified value is to be associated
     * @param value a {@code byte} to be associated with the specified {@code key}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    void exportByte(@NonNull final String key, final byte value);

    /**
     * Exports (puts) a {@code boolean} value into the exportable state of this instance, replacing
     * any existing value for the given {@code key}.
     *
     * @param key   {@code key} with which the specified value is to be associated
     * @param value a {@code boolean} to be associated with the specified {@code key}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    void exportBoolean(@NonNull final String key, final boolean value);

    /**
     * Exports (puts) a {@code char} value into the exportable state of this instance, replacing
     * any existing value for the given {@code key}.
     *
     * @param key   {@code key} with which the specified value is to be associated
     * @param value a {@code char} to be associated with the specified {@code key}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    void exportChar(@NonNull final String key, final char value);

    /**
     * Exports (puts) a {@code short} value into the exportable state of this instance, replacing
     * any existing value for the given {@code key}.
     *
     * @param key   {@code key} with which the specified value is to be associated
     * @param value a {@code short} to be associated with the specified {@code key}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    void exportShort(@NonNull final String key, final short value);

    /**
     * Exports (puts) a {@code float} value into the exportable state of this instance, replacing
     * any existing value for the given {@code key}.
     *
     * @param key   {@code key} with which the specified value is to be associated
     * @param value a {@code float} to be associated with the specified {@code key}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    void exportFloat(@NonNull final String key, final float value);

    /**
     * Exports (puts) a {@code CharSequence} value into the exportable state of this instance, replacing
     * any existing value for the given {@code key}.  Either {@code key} or value may be {@code null}.
     *
     * @param key   {@code key} with which the specified value is to be associated
     * @param value a {@code CharSequence} to be associated with the specified {@code key}, or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    void exportCharSequence(@NonNull final String key, @Nullable final CharSequence value);

    /**
     * Exports (puts) a {@code String} value into the exportable state of this instance, replacing
     * any existing value for the given {@code key}.  Either {@code key} or value may be {@code null}.
     *
     * @param key   {@code key} with which the specified value is to be associated
     * @param value a {@code String} to be associated with the specified {@code key}, or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    void exportString(@NonNull final String key, @Nullable final String value);

    /**
     * Exports (puts) a {@code Parcelable} value into the exportable state of this instance, replacing
     * any existing value for the given {@code key}.  Either {@code key} or value may be {@code null}.
     *
     * @param key   {@code key} with which the specified value is to be associated
     * @param value a {@code Parcelable} instance to be associated with the specified {@code key}, or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    void exportParcelable(@NonNull final String key, @Nullable final Parcelable value);

    /**
     * Exports (puts) an {@code array} of {@code Parcelable} values into the exportable state of this instance,
     * replacing any existing value for the given {@code key}.  Either {@code key} or value may
     * be {@code null}.
     *
     * @param key   {@code key} with which the specified value is to be associated
     * @param value an {@code array} of {@code Parcelable} instances to be associated with the
     *              specified {@code key}, or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    void exportParcelableArray(@NonNull final String key, @Nullable final Parcelable[] value);

    /**
     * Exports (puts) a {@code List} of {@code Parcelable} values into the exportable state of this instance,
     * replacing any existing value for the given {@code key}.  Either {@code key} or value may
     * be {@code null}.
     *
     * @param key   {@code key} with which the specified value is to be associated
     * @param value an {@code ArrayList} of {@code Parcelable} instances to be associated with the
     *              specified {@code key}, or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    void exportParcelableArrayList(@NonNull final String key,
                                   @Nullable final ArrayList<? extends Parcelable> value);

    /**
     * Exports (puts) a {@code SparseArray} of {@code Parcelable} values into the exportable state of this instance,
     * replacing any existing value for the given {@code key}.  Either {@code key} or value may be {@code null}.
     *
     * @param key   {@code key} with which the specified value is to be associated
     * @param value a {@code SparseArray} of {@code Parcelable} instances to be associated with the
     *              specified {@code key}, or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    void exportSparseParcelableArray(@NonNull final String key,
                                     @Nullable final SparseArray<? extends Parcelable> value);

    /**
     * Exports (puts) an {@code ArrayList<Integer>} value into the exportable state of this instance, replacing
     * any existing value for the given {@code key}.  Either {@code key} or value may be {@code null}.
     *
     * @param key   {@code key} with which the specified value is to be associated
     * @param value an {@code ArrayList} of {@code Integer} instances to be associated with the specified {@code key},
     *              or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    void exportIntegerArrayList(@NonNull final String key, @Nullable final ArrayList<Integer> value);

    /**
     * Exports (puts) an {@code ArrayList<String>} value into the exportable state of this instance, replacing
     * any existing value for the given {@code key}.  Either {@code key} or value may be {@code null}.
     *
     * @param key   {@code key} with which the specified value is to be associated
     * @param value an {@code ArrayList} of {@code String} instances to be associated with the specified {@code key},
     *              or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    void exportStringArrayList(@NonNull final String key, @Nullable final ArrayList<String> value);

    /**
     * Exports (puts) an {@code ArrayList<CharSequence>} value into the exportable state of this instance, replacing
     * any existing value for the given {@code key}.  Either {@code key} or value may be {@code null}.
     *
     * @param key   {@code key} with which the specified value is to be associated
     * @param value an {@code ArrayList} of CharSequence instances to be associated with the specified {@code key},
     *              or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    void exportCharSequenceArrayList(@NonNull final String key,
                                     @Nullable final ArrayList<CharSequence> value);

    /**
     * Exports (puts) a {@code Serializable} value into the mapping of this Bundle, replacing
     * any existing value for the given {@code key}.  Either {@code key} or value may be {@code null}.
     *
     * @param key   {@code key} with which the specified value is to be associated
     * @param value a {@code Serializable} instance to be associated with the specified {@code key},
     *              or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    void exportSerializable(@NonNull final String key, @Nullable final Serializable value);

    /**
     * Exports (puts) a {@code byte array} value into the exportable state of this instance, replacing
     * any existing value for the given {@code key}.  Either {@code key} or value may be {@code null}.
     *
     * @param key   {@code key} with which the specified value is to be associated
     * @param value a {@code byte array} instance to be associated with the specified {@code key},
     *              or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    void exportByteArray(@NonNull final String key, @Nullable final byte[] value);

    /**
     * Exports (puts) a {@code short array} value into the exportable state of this instance, replacing
     * any existing value for the given {@code key}.  Either {@code key} or value may be {@code null}.
     *
     * @param key   {@code key} with which the specified value is to be associated
     * @param value a {@code short array} instance to be associated with the specified {@code key},
     *              or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    void exportShortArray(@NonNull final String key, @Nullable final short[] value);

    /**
     * Exports (puts) a {@code char array} value into the exportable state of this instance, replacing
     * any existing value for the given {@code key}.  Either {@code key} or value may be {@code null}.
     *
     * @param key   {@code key} with which the specified value is to be associated
     * @param value a {@code char array} instance to be associated with the specified {@code key},
     *              or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    void exportCharArray(@NonNull final String key, @Nullable final char[] value);

    /**
     * Exports (puts) a {@code float array} value into the exportable state of this instance, replacing
     * any existing value for the given {@code key}.  Either {@code key} or value may be {@code null}.
     *
     * @param key   {@code key} with which the specified value is to be associated
     * @param value a {@code float array} instance to be associated with the specified {@code key},
     *              or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    void exportFloatArray(@NonNull final String key, @Nullable final float[] value);

    /**
     * Exports (puts) a {@code CharSequence array} value into the exportable state of this instance, replacing
     * any existing value for the given {@code key}.  Either {@code key} or value may be {@code null}.
     *
     * @param key   {@code key} with which the specified value is to be associated
     * @param value a {@code CharSequence array} instance to be associated with the specified {@code key},
     *              or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    void exportCharSequenceArray(@NonNull final String key, @Nullable final CharSequence[] value);

    /**
     * Exports (puts) a {@code Bundle} value into the exportable state of this instance, replacing
     * any existing value for the given {@code key}.  Either {@code key} or value may be {@code null}.
     *
     * @param key   {@code key} with which the specified value is to be associated
     * @param value a {@code Bundle} instance to be associated with the specified {@code key},
     *              or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    void exportBundle(@NonNull final String key, @Nullable final Bundle value);
}
