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

public interface ReadableStateOwner {

    /**
     * Returns the value associated with the given {@code key}, or (byte) 0 if
     * no mapping of the desired type exists for the given {@code key}.
     *
     * @param key the {@code key} whose associated value is to be returned
     * @return a {@code byte} to which the specified {@code key} is mapped
     * @throws NullPointerException if {@code key} is {@code null}
     */
    byte getByte(@NonNull final String key);

    /**
     * Returns the value associated with the given {@code key}, or {@code defaultValue} if
     * no mapping of the desired type exists for the given {@code key}.
     *
     * @param key          the {@code key} whose associated value is to be returned
     * @param defaultValue Value to return if {@code key} does not exist
     * @return a {@code byte} to which the specified {@code key} is mapped
     * @throws NullPointerException if {@code key} is {@code null}
     */
    byte getByte(@NonNull final String key, final byte defaultValue);

    /**
     * Returns the value associated with the given {@code key}, or false if
     * no mapping of the desired type exists for the given {@code key}.
     *
     * @param key the {@code key} whose associated value is to be returned
     * @return a {@code boolean} to which the specified {@code key} is mapped
     * @throws NullPointerException if {@code key} is {@code null}
     */
    boolean getBoolean(@NonNull final String key);

    /**
     * Returns the value associated with the given {@code key}, or {@code defaultValue} if
     * no mapping of the desired type exists for the given {@code key}.
     *
     * @param key          the {@code key} whose associated value is to be returned
     * @param defaultValue Value to return if {@code key} does not exist
     * @return a {@code boolean} to which the specified {@code key} is mapped
     * @throws NullPointerException if {@code key} is {@code null}
     */
    boolean getBoolean(@NonNull final String key, final boolean defaultValue);

    /**
     * Returns the value associated with the given {@code key}, or (char) 0 if
     * no mapping of the desired type exists for the given {@code key}.
     *
     * @param key the {@code key} whose associated value is to be returned
     * @return a {@code char} to which the specified {@code key} is mapped
     * @throws NullPointerException if {@code key} is {@code null}
     */
    char getChar(@NonNull final String key);

    /**
     * Returns the value associated with the given {@code key}, or {@code defaultValue} if
     * no mapping of the desired type exists for the given {@code key}.
     *
     * @param key          the {@code key} whose associated value is to be returned
     * @param defaultValue Value to return if {@code key} does not exist
     * @return a {@code char} to which the specified {@code key} is mapped
     * @throws NullPointerException if {@code key} is {@code null}
     */
    char getChar(@NonNull final String key, final char defaultValue);

    /**
     * Returns the value associated with the given {@code key}, or (short) 0 if
     * no mapping of the desired type exists for the given {@code key}.
     *
     * @param key the {@code key} whose associated value is to be returned
     * @return a {@code short} to which the specified {@code key} is mapped
     * @throws NullPointerException if {@code key} is {@code null}
     */
    short getShort(@NonNull final String key);

    /**
     * Returns the value associated with the given {@code key}, or {@code defaultValue} if
     * no mapping of the desired type exists for the given {@code key}.
     *
     * @param key          the {@code key} whose associated value is to be returned
     * @param defaultValue Value to return if {@code key} does not exist
     * @return a {@code short} to which the specified {@code key} is mapped
     * @throws NullPointerException if {@code key} is {@code null}
     */
    short getShort(@NonNull final String key, final short defaultValue);

    /**
     * Returns the value associated with the given {@code key}, or 0.0f if
     * no mapping of the desired type exists for the given {@code key}.
     *
     * @param key the {@code key} whose associated value is to be returned
     * @return a {@code float} to which the specified {@code key} is mapped
     * @throws NullPointerException if {@code key} is {@code null}
     */
    float getFloat(@NonNull final String key);

    /**
     * Returns the value associated with the given {@code key}, or {@code defaultValue} if
     * no mapping of the desired type exists for the given {@code key}.
     *
     * @param key          the {@code key} whose associated value is to be returned
     * @param defaultValue Value to return if {@code key} does not exist
     * @return a {@code float} to which the specified {@code key} is mapped
     * @throws NullPointerException if {@code key} is {@code null}
     */
    float getFloat(@NonNull final String key, final float defaultValue);

    /**
     * Returns the value associated with the given {@code key}, or {@code null} if
     * no mapping of the desired type exists for the given {@code key} or a {@code null}
     * value is explicitly associated with the {@code key}.
     *
     * @param key the {@code key} whose associated value is to be returned
     * @return a {@code CharSequence} to which the specified {@code key} is mapped, or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    @Nullable
    CharSequence getCharSequence(@NonNull final String key);

    /**
     * Returns the value associated with the given {@code key}, or {@code defaultValue} if
     * no mapping of the desired type exists for the given {@code key} or if a {@code null}
     * value is explicitly associated with the given {@code key}.
     *
     * @param key          the {@code key} whose associated value is to be returned
     * @param defaultValue Value to return if {@code key} does not exist or if a {@code null}
     *                     value is associated with the given {@code key}.
     * @return the {@code CharSequence} value associated with the given {@code key}, or {@code defaultValue}
     * if no valid {@code CharSequence} object is currently mapped to that {@code key}.
     * @throws NullPointerException if {@code key} or {@code defaultValue} is {@code null}
     */
    @NonNull
    CharSequence getCharSequence(@NonNull final String key, @NonNull final CharSequence defaultValue);

    /**
     * Returns the value associated with the given {@code key}, or {@code null} if
     * no mapping of the desired type exists for the given {@code key} or a {@code null}
     * value is explicitly associated with the {@code key}.
     *
     * @param key the {@code key} whose associated value is to be returned
     * @return a {@code String} to which the specified {@code key} is mapped, or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    @Nullable
    String getString(@NonNull final String key);

    /**
     * Returns the value associated with the given {@code key}, or {@code defaultValue} if
     * no mapping of the desired type exists for the given {@code key} or if a {@code null}
     * value is explicitly associated with the given {@code key}.
     *
     * @param key          the {@code key} whose associated value is to be returned
     * @param defaultValue Value to return if {@code key} does not exist or if a {@code null}
     *                     value is associated with the given {@code key}.
     * @return the {@code String} value associated with the given {@code key}, or {@code defaultValue}
     * if no valid {@code String} object is currently mapped to that {@code key}.
     * @throws NullPointerException if {@code key} is {@code null}
     */
    @NonNull
    String getString(@NonNull final String key, @NonNull final String defaultValue);

    /**
     * Returns the value associated with the given {@code key}, or {@code null} if
     * no mapping of the desired type exists for the given {@code key} or a {@code null}
     * value is explicitly associated with the {@code key}.
     *
     * @param key the {@code key} whose associated value is to be returned
     * @return a {@code Bundle} to which the specified {@code key} is mapped, or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    @Nullable
    Bundle getBundle(@NonNull final String key);

    /**
     * Returns the value associated with the given {@code key}, or {@code null} if
     * no mapping of the desired type exists for the given {@code key} or a {@code null}
     * value is explicitly associated with the {@code key}.
     *
     * @param key the {@code key} whose associated value is to be returned
     * @return a {@code Parcelable} to which the specified {@code key} is mapped, or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    @Nullable
    <T extends Parcelable> T getParcelable(@NonNull final String key);

    /**
     * Returns the value associated with the given {@code key}, or {@code null} if
     * no mapping of the desired type exists for the given {@code key} or a {@code null}
     * value is explicitly associated with the {@code key}.
     *
     * @param key the {@code key} whose associated value is to be returned
     * @return a {@code Parcelable[]} to which the specified {@code key} is mapped, or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    @Nullable
    Parcelable[] getParcelableArray(@NonNull final String key);

    /**
     * Returns the value associated with the given {@code key}, or {@code null} if
     * no mapping of the desired type exists for the given {@code key} or a {@code null}
     * value is explicitly associated with the {@code key}.
     *
     * @param key the {@code key} whose associated value is to be returned
     * @return an {@code ArrayList<T>} to which the specified {@code key} is mapped, or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    @Nullable
    <T extends Parcelable> ArrayList<T> getParcelableArrayList(@NonNull final String key);

    /**
     * Returns the value associated with the given {@code key}, or {@code null} if
     * no mapping of the desired type exists for the given {@code key} or a {@code null}
     * value is explicitly associated with the {@code key}.
     *
     * @param key the {@code key} whose associated value is to be returned
     * @return a {@code SparseArray<T>} to which the specified {@code key} is mapped, or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    @Nullable
    <T extends Parcelable> SparseArray<T> getSparseParcelableArray(@NonNull final String key);

    /**
     * Returns the value associated with the given {@code key}, or {@code null} if
     * no mapping of the desired type exists for the given {@code key} or a {@code null}
     * value is explicitly associated with the {@code key}.
     *
     * @param key the {@code key} whose associated value is to be returned
     * @return a {@code Serializable} to which the specified {@code key} is mapped, or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    @Nullable
    Serializable getSerializable(@NonNull final String key);

    /**
     * Returns the value associated with the given {@code key}, or {@code null} if
     * no mapping of the desired type exists for the given {@code key} or a {@code null}
     * value is explicitly associated with the {@code key}.
     *
     * @param key the {@code key} whose associated value is to be returned
     * @return an {@code ArrayList<String>} to which the specified {@code key} is mapped, or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    @Nullable
    ArrayList<Integer> getIntegerArrayList(@NonNull final String key);

    /**
     * Returns the value associated with the given {@code key}, or {@code null} if
     * no mapping of the desired type exists for the given {@code key} or a {@code null}
     * value is explicitly associated with the {@code key}.
     *
     * @param key the {@code key} whose associated value is to be returned
     * @return an {@code ArrayList<String>} to which the specified {@code key} is mapped, or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    @Nullable
    ArrayList<String> getStringArrayList(@NonNull final String key);

    /**
     * Returns the value associated with the given {@code key}, or {@code null} if
     * no mapping of the desired type exists for the given {@code key} or a {@code null}
     * value is explicitly associated with the {@code key}.
     *
     * @param key the {@code key} whose associated value is to be returned
     * @return an {@code ArrayList<CharSequence>} to which the specified {@code key} is mapped, or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    @Nullable
    ArrayList<CharSequence> getCharSequenceArrayList(@NonNull final String key);

    /**
     * Returns the value associated with the given {@code key}, or {@code null} if
     * no mapping of the desired type exists for the given {@code key} or a {@code null}
     * value is explicitly associated with the {@code key}.
     *
     * @param key the {@code key} whose associated value is to be returned
     * @return a {@code byte[]} to which the specified {@code key} is mapped, or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    @Nullable
    byte[] getByteArray(@NonNull final String key);

    /**
     * Returns the value associated with the given {@code key}, or {@code null} if
     * no mapping of the desired type exists for the given {@code key} or a {@code null}
     * value is explicitly associated with the {@code key}.
     *
     * @param key the {@code key} whose associated value is to be returned
     * @return a {@code short[]} to which the specified {@code key} is mapped, or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    @Nullable
    short[] getShortArray(@NonNull final String key);

    /**
     * Returns the value associated with the given {@code key}, or {@code null} if
     * no mapping of the desired type exists for the given {@code key} or a {@code null}
     * value is explicitly associated with the {@code key}.
     *
     * @param key the {@code key} whose associated value is to be returned
     * @return a {@code char[]} to which the specified {@code key} is mapped, or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    @Nullable
    char[] getCharArray(@NonNull final String key);

    /**
     * Returns the value associated with the given {@code key}, or {@code null} if
     * no mapping of the desired type exists for the given {@code key} or a {@code null}
     * value is explicitly associated with the {@code key}.
     *
     * @param key the {@code key} whose associated value is to be returned
     * @return a {@code float[]} to which the specified {@code key} is mapped, or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    @Nullable
    float[] getFloatArray(@NonNull final String key);

    /**
     * Returns the value associated with the given {@code key}, or {@code null} if
     * no mapping of the desired type exists for the given {@code key} or a {@code null}
     * value is explicitly associated with the {@code key}.
     *
     * @param key the {@code key} whose associated value is to be returned
     * @return a {@code CharSequence[]} to which the specified {@code key} is mapped, or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    @Nullable
    CharSequence[] getCharSequenceArray(@NonNull final String key);
}