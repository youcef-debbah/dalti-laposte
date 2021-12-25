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

package dz.jsoftware95.silverbox.android.common;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import javax.annotation.concurrent.NotThreadSafe;

import dz.jsoftware95.silverbox.android.backend.Item;
import dz.jsoftware95.silverbox.android.concurrent.AppWorker;
import dz.jsoftware95.silverbox.android.concurrent.SystemWorker;

/**
 * A Utility class that contains general-purpose static assertions.
 */
@SuppressWarnings({"ConstantConditions", "unused", "UnusedReturnValue"})
@NotThreadSafe
public final class Check {

    private Check() {
        throw new UnsupportedOperationException("bad boy! no instance for you");
    }

    @SuppressWarnings("Contract")
    @Contract(value = "null -> fail; !null -> new", pure = true)
    private static RuntimeException newInstanceOf(final Class<? extends RuntimeException> type) {
        try {
            return type.getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            final RuntimeException runtimeException = new RuntimeException(type.getName());
            runtimeException.addSuppressed(e);
            return runtimeException;
        }
    }

    /**
     * Checks that the specified object reference is not {@code null} and throws
     * a NullPointerException if it is.
     *
     * @param reference the object reference to be checked
     * @param <T>       the type of the reference
     * @return the given {@code reference} if it is not {@code null}
     * @throws NullPointerException if {@code reference} is {@code null}
     */
    @NotNull
    @Contract("null -> fail; !null -> param1")
    public static <T> T nonNull(@Nullable final T reference) {
        return Objects.requireNonNull(reference);
    }

    /**
     * Checks that given Object reference is {@code null} and throws an unchecked exception if
     * it is not.
     *
     * @param reference the Object reference to be checked
     * @throws RuntimeException is the given {@code reference} is not {@code null}
     */
    @Contract("!null -> fail")
    public static void isNull(@Nullable final Object reference) {
        if (reference != null)
            throw new RuntimeException();
    }

    /**
     * Checks that the specified object reference is not {@code null} and throws
     * a NullPointerException if it is.
     *
     * @param reference the object reference to be checked
     * @param message   the exception detail message. The detail message is saved for
     *                  later retrieval by the {@link Exception#getMessage()} method.
     * @param <T>       the type of the reference
     * @return the given {@code reference} if it is not {@code null}
     * @throws NullPointerException if {@code reference} is {@code null}
     */
    @NotNull
    @Contract("null, _ -> fail; !null, _ -> param1")
    public static <T> T nonNull(@Nullable final T reference,
                                @Nullable final String message) {
        return Objects.requireNonNull(reference, message);
    }

    /**
     * Checks that given Object reference is {@code null} and throws an unchecked exception if
     * it is not.
     *
     * @param reference the Object reference to be checked
     * @param message   the exception detail message. The detail message is saved for
     *                  later retrieval by the {@link Exception#getMessage()} method.
     * @throws RuntimeException is the given {@code reference} is not {@code null}
     */
    @Contract("!null, _ -> fail")
    public static void isNull(@Nullable final Object reference,
                              @Nullable final String message) {
        if (reference != null)
            throw new RuntimeException(message);
    }

    /**
     * Checks that the specified object reference is not {@code null} and throws
     * a RuntimeException if it is.
     *
     * @param reference the object reference to be checked
     * @param exception The runtime class of the exception instance that will be thrown, default
     *                  to {@code RuntimeException.class} if parameter is {@code null}
     * @param <T>       the type of the reference
     * @return the given {@code reference} if it is not {@code null}
     * @throws RuntimeException if the given {@code reference} is {@code null}, the exact
     *                          runtime class of the thrown exception depends on the
     *                          {@code exception} parameter
     */
    @NotNull
    @Contract("null, _ -> fail; !null, _ -> param1")
    public static <T> T nonNull(@Nullable final T reference,
                                @Nullable final Class<? extends RuntimeException> exception) {
        if (reference == null)
            if (exception != null)
                throw newInstanceOf(exception);
            else
                throw new RuntimeException();

        return reference;
    }

    /**
     * Checks that given Object reference is {@code null} and throws a RuntimeException if it is not.
     *
     * @param reference the Object reference be checked
     * @param exception The runtime class of the exception instance that will be thrown, default
     *                  to {@code RuntimeException.class} if parameter is {@code null}
     * @throws RuntimeException if the given {@code reference} is not {@code null}, the exact
     *                          runtime class of the thrown exception depends on the
     *                          {@code exception} parameter
     */
    @Contract("!null, _ -> fail")
    public static void isNull(@Nullable final Object reference,
                              @Nullable final Class<? extends RuntimeException> exception) {
        if (reference != null)
            if (exception != null)
                throw newInstanceOf(exception);
            else
                throw new RuntimeException();
    }

    /**
     * Checks that the given boolean value {@code isTrue} is {@code true} and throws an
     * unchecked exception if it is not.
     *
     * @param isTrue the boolean value to be checked
     * @throws RuntimeException if the given value {@code isTrue} is not {@code true}
     */
    @Contract("false -> fail")
    public static void that(final boolean isTrue) {
        if (!isTrue)
            throw new RuntimeException();
    }

    /**
     * Checks that the given boolean value {@code isFalse} is {@code false} and throws an
     * unchecked exception if it is not.
     *
     * @param isFalse the boolean value to be checked
     * @throws RuntimeException if the given value {@code isTrue} is not {@code true}
     */
    @Contract("true -> fail")
    public static void not(final boolean isFalse) {
        if (isFalse)
            throw new RuntimeException();
    }

    /**
     * Checks that the given boolean value {@code isTrue} is {@code true} and throws an
     * unchecked exception if it is not.
     *
     * @param isTrue  the boolean value to be checked
     * @param message the exception detail message. The detail message is saved for
     *                later retrieval by the {@link Exception#getMessage()} method.
     * @throws RuntimeException if the given value {@code isTrue} is not {@code true}
     */
    @Contract("false, _ -> fail")
    public static void that(final boolean isTrue, @Nullable final String message) {
        if (!isTrue)
            throw new RuntimeException(message);
    }

    /**
     * Checks that the given boolean value {@code isFalse} is {@code false} and throws an
     * unchecked exception if it is not.
     *
     * @param isFalse the boolean value to be checked
     * @param message the exception detail message. The detail message is saved for
     *                later retrieval by the {@link Exception#getMessage()} method.
     * @throws RuntimeException if the given value {@code isTrue} is not {@code true}
     */
    @Contract("true, _ -> fail")
    public static void not(final boolean isFalse, final String message) {
        if (isFalse)
            throw new RuntimeException(message);
    }

    /**
     * Checks that the given boolean value {@code isTrue} is {@code true}
     * and throws a RuntimeException if it is not.
     *
     * @param isTrue    the boolean value to be checked
     * @param exception The runtime class of the exception instance that will be thrown, default
     *                  to {@code RuntimeException.class} if parameter is {@code null}
     * @throws RuntimeException if the given value {@code isTrue} is not {@code true}, the exact
     *                          runtime class of the thrown exception depends on the
     *                          {@code exception} parameter
     */
    @Contract("false, _ -> fail")
    public static void that(final boolean isTrue,
                            @Nullable final Class<? extends RuntimeException> exception) {
        if (!isTrue)
            if (exception != null)
                throw newInstanceOf(exception);
            else
                throw new RuntimeException();
    }

    /**
     * Checks that the given boolean value {@code isFalse} is {@code false}
     * and throws a RuntimeException if it is not.
     *
     * @param isFalse   the boolean value to be checked
     * @param exception The runtime class of the exception instance that will be thrown, default
     *                  to {@code RuntimeException.class} if parameter is {@code null}
     * @throws RuntimeException if the given value {@code isFalse} is not {@code false}, the exact
     *                          runtime class of the thrown exception depends on the
     *                          {@code exception} parameter
     */
    @Contract("true, _ -> fail")
    public static void not(final boolean isFalse,
                           @Nullable final Class<? extends RuntimeException> exception) {
        if (isFalse)
            if (exception != null)
                throw newInstanceOf(exception);
            else
                throw new RuntimeException();
    }

    /**
     * Checks that boolean value wrapped by the given AtomicBoolean {@code isTrue}
     * is {@code true} and throws an unchecked exception if it is not.
     *
     * @param isTrue the boolean value to be checked
     * @throws RuntimeException if the given value {@code isTrue} is {@code null} or it's
     *                          {@linkplain AtomicBoolean#get() value} is {@code false}
     */
    @Contract("null -> fail")
    public static void that(@Nullable final AtomicBoolean isTrue) {
        if (isTrue == null || !isTrue.get())
            throw new RuntimeException();
    }

    /**
     * Checks that boolean value wrapped by the given AtomicBoolean {@code isFalse}
     * is {@code false} and throws an unchecked exception if it is not.
     *
     * @param isFalse the boolean value to be checked
     * @throws RuntimeException if the given value {@code isTrue} is {@code null} or it's
     *                          {@linkplain AtomicBoolean#get() value} is {@code true}
     */
    @Contract("null -> fail")
    public static void not(@Nullable final AtomicBoolean isFalse) {
        if (isFalse == null || isFalse.get())
            throw new RuntimeException();
    }

    /**
     * Checks that boolean value wrapped by the given AtomicBoolean {@code isTrue}
     * is {@code true} and throws an unchecked exception if it is not.
     *
     * @param isTrue  the boolean value to be checked
     * @param message the exception detail message. The detail message is saved for
     *                later retrieval by the {@link Exception#getMessage()} method.
     * @throws RuntimeException if the given value {@code isTrue} is {@code null} or it's
     *                          {@linkplain AtomicBoolean#get() value} is {@code false}
     */
    @Contract("null, _ -> fail")
    public static void that(@Nullable final AtomicBoolean isTrue,
                            @Nullable final String message) {
        if (isTrue == null || !isTrue.get())
            throw new RuntimeException(message);
    }

    /**
     * Checks that boolean value wrapped by the given AtomicBoolean {@code isFalse}
     * is {@code false} and throws an unchecked exception if it is not.
     *
     * @param isFalse the boolean value to be checked
     * @param message the exception detail message. The detail message is saved for
     *                later retrieval by the {@link Exception#getMessage()} method.
     * @throws RuntimeException if the given value {@code isTrue} is {@code null} or it's
     *                          {@linkplain AtomicBoolean#get() value} is {@code true}
     */
    @Contract("null, _ -> fail")
    public static void not(@Nullable final AtomicBoolean isFalse,
                           @Nullable final String message) {
        if (isFalse == null || isFalse.get())
            throw new RuntimeException(message);
    }

    /**
     * Checks that boolean value wrapped by the given AtomicBoolean {@code isTrue}
     * is {@code true} and throws a RuntimeException if it is not.
     *
     * @param isTrue    the boolean value to be checked
     * @param exception The runtime class of the exception instance that will be thrown, default
     *                  to {@code RuntimeException.class} if parameter is {@code null}
     * @throws RuntimeException if the given value {@code isTrue} is not {@code true}, the exact
     *                          runtime class of the thrown exception depends on the
     *                          {@code exception} parameter
     */
    @Contract("null, _ -> fail")
    public static void that(@Nullable final AtomicBoolean isTrue,
                            @Nullable final Class<? extends RuntimeException> exception) {
        if (isTrue == null || !isTrue.get())
            if (exception != null)
                throw newInstanceOf(exception);
            else
                throw new RuntimeException();
    }

    /**
     * Checks that boolean value wrapped by the given AtomicBoolean {@code isFalse}
     * is {@code false} and throws a RuntimeException if it is not.
     *
     * @param isFalse   the boolean value to be checked
     * @param exception The runtime class of the exception instance that will be thrown, default
     *                  to {@code RuntimeException.class} if parameter is {@code null}
     * @throws RuntimeException if the given value {@code isFalse} is not {@code false}, the exact
     *                          runtime class of the thrown exception depends on the
     *                          {@code exception} parameter
     */
    @Contract("null, _ -> fail")
    public static void not(@Nullable final AtomicBoolean isFalse,
                           @Nullable final Class<? extends RuntimeException> exception) {
        if (isFalse == null || isFalse.get())
            if (exception != null)
                throw newInstanceOf(exception);
            else
                throw new RuntimeException();
    }

    /**
     * Checks that the given boolean value {@code isTrue} is {@code true} and throws an
     * IllegalArgumentException if it is not.
     *
     * @param isTrue the boolean value to be checked
     * @throws IllegalArgumentException if the given value {@code isTrue} is not {@code true}
     */
    @Contract("false -> fail")
    public static void arg(final boolean isTrue) {
        if (!isTrue)
            throw new IllegalArgumentException();
    }

    /**
     * Checks that the given boolean value {@code isTrue} is {@code true} and throws an
     * IllegalStateException if it is not.
     *
     * @param isTrue the boolean value to be checked
     * @throws IllegalStateException if the given value {@code isTrue} is not {@code true}
     */
    @Contract("false -> fail")
    public static void state(final boolean isTrue) {
        if (!isTrue)
            throw new IllegalStateException();
    }

    /**
     * Checks that the given boolean value {@code isFalse} is {@code false} and throws an
     * IllegalArgumentException if it is not.
     *
     * @param isFalse the boolean value to be checked
     * @throws IllegalArgumentException if the given value {@code isFalse} is not {@code false}
     */
    @Contract("true -> fail")
    public static void argNot(final boolean isFalse) {
        if (isFalse)
            throw new IllegalArgumentException();
    }

    /**
     * Checks that the given boolean value {@code isFalse} is {@code false} and throws an
     * IllegalStateException if it is not.
     *
     * @param isFalse the boolean value to be checked
     * @throws IllegalStateException if the given value {@code isFalse} is not {@code false}
     */
    @Contract("true -> fail")
    public static void stateNot(final boolean isFalse) {
        if (isFalse)
            throw new IllegalStateException();
    }

    /**
     * Checks that the given boolean value {@code isTrue} is {@code true} and throws an
     * IllegalArgumentException if it is not.
     *
     * @param isTrue  the boolean value to be checked
     * @param message the exception detail message. The detail message is saved for
     *                later retrieval by the {@link Exception#getMessage()} method.
     * @throws IllegalArgumentException if the given value {@code isTrue} is not {@code true}
     */
    @Contract("false, _ -> fail")
    public static void arg(final boolean isTrue, final String message) {
        if (!isTrue)
            throw new IllegalArgumentException(message);
    }

    /**
     * Checks that the given boolean value {@code isTrue} is {@code true} and throws an
     * IllegalStateException if it is not.
     *
     * @param isTrue  the boolean value to be checked
     * @param message the exception detail message. The detail message is saved for
     *                later retrieval by the {@link Exception#getMessage()} method.
     * @throws IllegalStateException if the given value {@code isTrue} is not {@code true}
     */
    @Contract("false, _ -> fail")
    public static void state(final boolean isTrue, final String message) {
        if (!isTrue)
            throw new IllegalStateException(message);
    }

    /**
     * Checks that the given boolean value {@code isFalse} is {@code false} and throws an
     * IllegalArgumentException if it is not.
     *
     * @param isFalse the boolean value to be checked
     * @param message the exception detail message. The detail message is saved for
     *                later retrieval by the {@link Exception#getMessage()} method.
     * @throws IllegalArgumentException if the given value {@code isFalse} is not {@code false}
     */
    @Contract("true, _ -> fail")
    public static void argNot(final boolean isFalse, final String message) {
        if (isFalse)
            throw new IllegalArgumentException(message);
    }

    /**
     * Checks that the given boolean value {@code isFalse} is {@code false} and throws an
     * IllegalStateException if it is not.
     *
     * @param isFalse the boolean value to be checked
     * @param message the exception detail message. The detail message is saved for
     *                later retrieval by the {@link Exception#getMessage()} method.
     * @throws IllegalStateException if the given value {@code isFalse} is not {@code false}
     */
    @Contract("true, _ -> fail")
    public static void stateNot(final boolean isFalse, final String message) {
        if (isFalse)
            throw new IllegalStateException(message);
    }

    /**
     * Checks that the given {@code array} is not empty or {@code null} and throws an unchecked
     * exception if it is.
     *
     * @param array the {@code array} to be checked
     * @param <T>   the component type of the checked array
     * @return the given {@code array} if it is not {@code null} or empty
     * @throws RuntimeException if the given {@code array} is {@code null} or empty.
     */
    @Contract("null -> fail; !null -> param1")
    public static <T> T[] notEmpty(@Nullable final T[] array) {
        if (array == null || array.length == 0)
            throw new RuntimeException();
        return array;
    }

    /**
     * Checks that the given {@code String} is not empty or {@code null} and throws an unchecked
     * exception if it is.
     *
     * @param string the {@code String} to be checked
     * @return the given {@code String} if it is not {@code null} or empty
     * @throws RuntimeException if the given {@code String} is {@code null} or empty.
     */
    @Contract("null -> fail; !null -> param1")
    public static String notEmpty(String string) {
        if (string == null || string.isEmpty())
            throw new RuntimeException();
        return string;
    }

    /**
     * Checks that the given {@code collection} is not empty or {@code null} and throws an unchecked
     * exception if it is.
     *
     * @param collection the {@code collection} to be checked
     * @param <T>        the type parameter of the collection
     * @return the given collection parameter if it is not {@code null} or empty
     * @throws RuntimeException if the given {@code collection} is {@code null} or empty.
     */
    @Contract("null -> fail; !null -> param1")
    public static <T extends Collection<?>> T notEmpty(@Nullable final T collection) {
        if (collection == null || collection.isEmpty())
            throw new RuntimeException();
        return collection;
    }

    /**
     * Checks that the given {@code map} is not empty or {@code null} and throws an unchecked
     * exception if it is.
     *
     * @param map the {@code map} to be checked
     * @param <K> the type of keys maintained by the given {@code map}
     * @param <V> the type of values of the given {@code map}
     * @return the given {@code map} parameter if it is not {@code null} or empty
     * @throws RuntimeException if the given {@code map} is {@code null} or empty.
     */
    @Contract("null -> fail; !null -> param1")
    public static <K, V> Map<K, V> notEmpty(@Nullable final Map<K, V> map) {
        if (map == null || map.isEmpty())
            throw new RuntimeException();
        return map;
    }

    /**
     * Checks that the given {@code array} is not empty or {@code null} and throws an unchecked
     * exception if it is.
     *
     * @param array   the {@code array} to be checked
     * @param message the exception detail message. The detail message is saved for
     *                later retrieval by the {@link Exception#getMessage()} method.
     * @param <T>     the component type of the checked array
     * @return the given {@code array} parameter if it is not {@code null} or empty
     * @throws RuntimeException if the given {@code array} is {@code null} or empty.
     */
    @Contract("null, _ -> fail; !null, _ -> param1")
    public static <T> T[] notEmpty(@Nullable final T[] array,
                                   @Nullable final String message) {
        if (array == null || array.length == 0)
            throw new RuntimeException(message);
        return array;
    }

    /**
     * Checks that the given {@code collection} is not empty or {@code null} and throws an unchecked
     * exception if it is.
     *
     * @param collection the collection to be checked
     * @param message    the exception detail message. The detail message is saved for
     *                   later retrieval by the {@link Exception#getMessage()} method.
     * @param <T>        the type parameter of the collection
     * @return the given {@code collection} parameter if it is not {@code null} or empty
     * @throws RuntimeException if the given {@code collection} is {@code null} or empty.
     */
    @Contract("null, _ -> fail; !null, _ -> param1")
    public static <T extends Collection<?>> T notEmpty(@Nullable final T collection,
                                                       @Nullable final String message) {
        if (collection == null || collection.isEmpty())
            throw new RuntimeException(message);
        return collection;
    }

    /**
     * Checks that the given {@code map} is not empty or {@code null} and throws an unchecked
     * exception if it is.
     *
     * @param map     the {@code map} to be checked
     * @param message the exception detail message. The detail message is saved for
     *                later retrieval by the {@link Exception#getMessage()} method.
     * @param <K>     the type of keys maintained by the given {@code map}
     * @param <V>     the type of values of the given {@code map}
     * @return the given {@code map} parameter if it is not {@code null} or empty
     * @throws RuntimeException if the given {@code map} is {@code null} or empty.
     */
    @Contract("null, _ -> fail; !null, _ -> param1")
    public static <K, V> Map<K, V> notEmpty(@Nullable final Map<K, V> map,
                                            @Nullable final String message) {
        if (map == null || map.isEmpty())
            throw new RuntimeException(message);
        return map;
    }

    /**
     * Checks that the given {@code array} is not empty or {@code null}
     * and throws a RuntimeException if it is.
     *
     * @param array     the {@code array} to be checked
     * @param exception The runtime class of the exception instance that will be thrown, default
     *                  to {@code RuntimeException.class} if parameter is {@code null}
     * @param <T>       the component type of the checked array
     * @return the given {@code array} parameter if it is not {@code null} or empty
     * @throws RuntimeException if the given {@code array} is not empty or {@code null},
     *                          the exact runtime class of the thrown exception depends on the
     *                          {@code exception} parameter
     */
    @Contract("null, _ -> fail; !null, _ -> param1")
    public static <T> T[] notEmpty(@Nullable final T[] array,
                                   @Nullable final Class<? extends RuntimeException> exception) {
        if (array == null || array.length == 0)
            if (exception != null)
                throw newInstanceOf(exception);
            else
                throw new RuntimeException();

        return array;
    }

    /**
     * Checks that the given {@code collection} is not empty or {@code null}
     * and throws a RuntimeException if it is.
     *
     * @param collection the collection to be checked
     * @param exception  The runtime class of the exception instance that will be thrown, default
     *                   to {@code RuntimeException.class} if parameter is {@code null}
     * @param <T>        the type parameter of the collection
     * @return the given {@code collection} parameter if it is not {@code null} or empty
     * @throws RuntimeException if the given {@code collection} is not empty or {@code null},
     *                          the exact runtime class of the thrown exception depends on the
     *                          {@code exception} parameter
     */
    @Contract("null, _ -> fail; !null, _ -> param1")
    public static <T extends Collection<?>> T notEmpty(@Nullable final T collection,
                                                       @Nullable final Class<? extends RuntimeException> exception) {
        if (collection == null || collection.isEmpty())
            if (exception != null)
                throw newInstanceOf(exception);
            else
                throw new RuntimeException();

        return collection;
    }

    /**
     * Checks that the given {@code map} is not empty or {@code null}
     * and throws a RuntimeException if it is.
     *
     * @param map       the {@code map} to be checked
     * @param exception The runtime class of the exception instance that will be thrown, default
     *                  to {@code RuntimeException.class} if parameter is {@code null}
     * @param <T>       the type of the map
     * @return the given {@code map} parameter if it is not {@code null} or empty
     * @throws RuntimeException if the given {@code map} is not empty or {@code null},
     *                          the exact runtime class of the thrown exception depends on the
     *                          {@code exception} parameter
     */
    @Contract("null, _ -> fail; !null, _ -> param1")
    public static <T extends Map<?, ?>> T notEmpty(@Nullable final T map,
                                                   @Nullable final Class<? extends RuntimeException> exception) {
        if (map == null || map.isEmpty())
            if (exception != null)
                throw newInstanceOf(exception);
            else
                throw new RuntimeException();

        return map;
    }

    /**
     * Checks that the given {@code array} is {@linkplain #notEmpty(Object[]) not empty}
     * and all of it's elements are non-null.
     *
     * @param objects the {@code array} to be checked
     * @param <T>     the component type of the checked array
     * @return the given {@code array} parameter if it is not {@code null} or empty
     * @throws RuntimeException if the {@code objects} {@code array} is {@code null}, empty or
     *                          contains a {@code null} element
     */
    @NotNull
    @Contract("null -> fail; !null -> param1")
    @SafeVarargs
    public static <T> T[] noNullElements(@Nullable final T... objects) {
        for (final T object : notEmpty(objects))
            if (object == null)
                throw new RuntimeException();

        //noinspection NullableProblems since the loop will fail if any element if null
        return objects;
    }

    /**
     * Checks that the given iterable is not {@code null} and all of it's elements are non-null.
     *
     * @param objects the iterable to be checked
     * @param <T>     the type parameter of the given iterable
     * @return the given {@code iterable} parameter if it is not {@code null} and does not contain
     * any {@code null} elements
     * @throws RuntimeException if the {@code objects} iterable is {@code null}
     *                          or contains a {@code null} element
     */
    @NotNull
    @Contract("null -> fail; !null -> param1")
    public static <T> Iterable<T> noNullElements(@Nullable final Iterable<T> objects) {
        if (objects == null)
            throw new RuntimeException();

        for (final Object element : objects)
            if (element == null)
                throw new RuntimeException();

        return objects;
    }

    /**
     * Checks that the given {@code map} is not {@code null} and all of it's keys are non-null.
     *
     * @param map the {@code map} to be checked
     * @throws IllegalArgumentException if the given {@code map} is {@code null}
     *                                  or contains a {@code null} key
     */
    @Contract(value = "null -> fail")
    public static void noNullKey(@Nullable final Map<?, ?> map) {
        if (map == null)
            throw new IllegalArgumentException();

        if (map.containsKey(null))
            throw new IllegalArgumentException("The given map should not contains a null key: "
                    + Represent.the(map));
    }

    /**
     * Checks that the given {@code map} is not {@code null} and all of it's values are non-null.
     *
     * @param map the {@code map} to be checked
     * @throws IllegalArgumentException if the given {@code map} is {@code null}
     *                                  or contains a {@code null} value
     */
    @Contract("null -> fail")
    public static void noNullValues(@Nullable final Map<?, ?> map) {
        if (map == null)
            throw new IllegalArgumentException();

        if (map.containsValue(null))
            throw new IllegalArgumentException("The given map should not contains a null value: "
                    + Represent.the(map));
    }

    /**
     * Checks that the given {@code map} forms a bijective mapping.
     * For the given {@code map} to from a mapping bijective it must be not {@code null}
     * and satisfy the following conditions:
     * <ul>
     * <li>all the keys are non null</li>
     * <li>all the values are non null</li>
     * <li>no two values are {@linkplain Object#equals(Object) equals}</li>
     * </ul>
     *
     * @param map the {@code map} to be checked
     * @param <K> the type of keys maintained by the given {@code map}
     * @param <V> the type of values of the given {@code map}
     * @return the given {@code map} if it satisfy all the conditions stated above
     * @throws IllegalArgumentException if one of the above conditions is violated
     */
    @Contract("null -> fail")
    public static <K, V> Map<K, V> bijective(@Nullable final Map<K, V> map) {
        noNullKey(map);
        noNullValues(map);

        final Collection<Object> valuesSet = new TreeSet<>();
        for (final Object element : map.values())
            if (!valuesSet.add(element))
                throw new IllegalArgumentException("The given map have a duplicated value: "
                        + element + " in: " + Represent.the(map));

        return map;
    }

    /**
     * Fails immediately by throwing an unchecked exception with the given {@code message}
     *
     * @throws RuntimeException always
     */
    @Contract("-> fail")
    public static void notReached() {
        throw new RuntimeException();
    }

    /**
     * Fails immediately by throwing an unchecked exception with the given {@code message}
     *
     * @param message the exception detail message. The detail message is saved for
     *                later retrieval by the {@link Exception#getMessage()} method.
     * @throws RuntimeException always
     */
    @Contract("_ -> fail")
    public static void notReached(@Nullable final String message) {
        throw new RuntimeException(message);
    }

    /**
     * Fails immediately by throwing an IllegalStateException with the given {@code message}
     *
     * @param message the exception detail message. The detail message is saved for
     *                later retrieval by the {@link Exception#getMessage()} method.
     * @throws IllegalStateException always
     */
    @Contract("_ -> fail")
    public static void illegalState(@Nullable final String message) {
        throw new IllegalStateException(message);
    }

    /**
     * Checks if the current thread <em>is</em> the Android Main thread
     * and throw an unchecked exception if it is not.
     * The name of the current thread is included in the message of the thrown exception.
     *
     * @throws RuntimeException if the current thread is not the Main thread
     */
    public static void isMainThread() {
        isMainThread("The main thread is required not: " + Thread.currentThread().getName());
    }

    /**
     * Checks if the current thread <em>is</em> the Android Main thread
     * and throw an unchecked exception if it is not.
     *
     * @param message the exception detail message. The detail message is saved for
     *                later retrieval by the {@link Exception#getMessage()} method.
     * @throws RuntimeException if the current thread is not the Main thread
     */
    public static void isMainThread(@Nullable final String message) {
        if (!SystemWorker.MAIN.isCurrentThread())
            throw new RuntimeException(message);
    }

    /**
     * Checks if the current thread <em>is not</em> the Android Main thread
     * and throw an unchecked exception if it is.
     *
     * @throws RuntimeException if the current thread is the Main thread
     */
    public static void isNotMainThread() {
        isNotMainThread("A worker thread is required not the main thread");
    }

    /**
     * Checks if the current thread <em>is not</em> the Android Main thread
     * and throw an unchecked exception if it is.
     *
     * @param message the exception detail message. The detail message is saved for
     *                later retrieval by the {@link Exception#getMessage()} method.
     * @throws RuntimeException if the current thread is the Main thread
     */
    public static void isNotMainThread(@Nullable final String message) {
        if (SystemWorker.MAIN.isCurrentThread())
            throw new RuntimeException(message);
    }

    /**
     * Attempts to match the entire region against the pattern.
     *
     * @return <tt>true</tt> if, and only if, the entire region sequence
     * is not null and matches this matcher's pattern
     */
    public static boolean matches(@NonNull Pattern pattern, @Nullable String text) {
        return text != null && pattern.matcher(text).matches();
    }

    @Contract(value = "_ -> param1", pure = true)
    public static int positiveInt(@Range(from = 0, to = Integer.MAX_VALUE) int value) {
        that(value > -1);
        return value;
    }

    @Contract(value = "_ -> param1", pure = true)
    public static int positiveIntNoZero(@Range(from = 1, to = Integer.MAX_VALUE) int value) {
        that(value > 0);
        return value;
    }

    @Contract(value = "null -> fail; !null -> param1", pure = true)
    public static int positiveInteger(@Range(from = 0, to = Integer.MAX_VALUE) Integer value) {
        nonNull(value);
        that(value > -1);
        return value;
    }

    @Contract(value = "_ -> param1", pure = true)
    public static long positiveLong(@Range(from = 0, to = Long.MAX_VALUE) long value) {
        that(value > -1);
        return value;
    }

    public static void isValid(@NonNull Item item) {
        if (!item.isValid())
            throw new RuntimeException("item not valid: " + item);
    }

    public static boolean sameNullability(Object obj1, Object obj2) {
        return (obj1 == null && obj2 == null) || (obj1 != null && obj2 != null);
    }

    public static void thatCurrentThreadIn(AppWorker... workers) {
        for (AppWorker worker : workers)
            if (worker.isCurrentThread())
                return;

        throw new RuntimeException("Wrong thread, worker required in: " + Arrays.toString(workers) + ". found: " + Thread.currentThread().getName());
    }

    public static void thatCurrentThreadNotIn(AppWorker... workers) {
        for (AppWorker worker : workers)
            if (worker.isCurrentThread())
                throw new RuntimeException("Wrong thread, " + worker + " is not suitable");
    }
}