package dz.jsoftware95.silverbox.android.common;

import androidx.annotation.MainThread;
import androidx.annotation.WorkerThread;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * A Utility class that contains general-purpose static assertions that delegates to {@link Check}
 * assertions if {@link Config#ASSERTION_ENABLED} is {@code true}, does nothing otherwise.
 * <p>
 * Since {@link Config#ASSERTION_ENABLED} is a compile time constant which mean that the compiler
 * can easily optimise each assertion in this class to either a simple delegation to {@link Check}
 * assertion (without the 'if' statement) or an empty void method depending on the value of
 * {@link Check}, such empty calls can be optimised away by further transformations
 * (such as R8 or ProGuard).
 * Note the expressions used as argument to these assertions may not be as easy to optimise as
 * the assertions themselves.
 * </p>
 */
@NotThreadSafe
public final class Assert {

    private Assert() {
        throw new UnsupportedOperationException("bad boy! no instance for you");
    }

    /**
     * Checks that the specified object reference is not {@code null} (by throwing
     * a NullPointerException if it is {@code null}) in case assertions are
     * {@linkplain Config#ASSERTION_ENABLED enabled} otherwise this method simply will return
     * the given <var>reference</var>.
     *
     * @param reference the object reference to be checked
     * @param <T>       the type of the reference
     * @return the given <var>reference</var> is it is not {@code null}
     * @throws NullPointerException if <var>reference</var> is {@code null}
     */
    @NotNull
    @Contract("!null -> param1")
    public static <T> T nonNull(@NotNull final T reference) {
        if (Config.ASSERTION_ENABLED)
            return Check.nonNull(reference);
        else
            return reference;
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
        if (Config.ASSERTION_ENABLED)
            Check.isNull(reference);
    }

    /**
     * Checks that the specified object reference is not {@code null} (by throwing
     * a NullPointerException if it is {@code null}) in case assertions are
     * {@linkplain Config#ASSERTION_ENABLED enabled} otherwise this method simply will return
     * the given <var>reference</var>.
     *
     * @param reference the object reference to be checked
     * @param message   the exception detail message. The detail message is saved for
     *                  later retrieval by the {@link Exception#getMessage()} method.
     * @param <T>       the type of the reference
     * @return the given {@code reference} if it is not {@code null}
     * @throws NullPointerException if {@code reference} is {@code null}
     */
    @Contract("!null, _ -> param1")
    public static @NotNull <T> T nonNull(@NotNull final T reference,
                                         @Nullable final String message) {
        if (Config.ASSERTION_ENABLED)
            return Check.nonNull(reference, message);
        else
            return reference;
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
        if (Config.ASSERTION_ENABLED)
            Check.isNull(reference, message);
    }

    /**
     * Checks that the specified object reference is not {@code null} (by throwing
     * a NullPointerException if it is {@code null}) in case assertions are
     * {@linkplain Config#ASSERTION_ENABLED enabled} otherwise this method simply will return
     * the given <var>reference</var>.
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
    @Contract("!null, _ -> param1")
    public static <T> T nonNull(@NotNull final T reference,
                                @Nullable final Class<? extends RuntimeException> exception) {
        if (Config.ASSERTION_ENABLED)
            return Check.nonNull(reference, exception);
        else
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
        if (Config.ASSERTION_ENABLED)
            Check.isNull(reference, exception);
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
        if (Config.ASSERTION_ENABLED)
            Check.that(isTrue);
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
        if (Config.ASSERTION_ENABLED)
            Check.not(isFalse);
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
        if (Config.ASSERTION_ENABLED)
            Check.that(isTrue, message);
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
        if (Config.ASSERTION_ENABLED)
            Check.not(isFalse, message);
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
        if (Config.ASSERTION_ENABLED)
            Check.that(isTrue, exception);
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
        if (Config.ASSERTION_ENABLED)
            Check.not(isFalse, exception);
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
        if (Config.ASSERTION_ENABLED)
            Check.arg(isTrue);
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
        if (Config.ASSERTION_ENABLED)
            Check.argNot(isFalse);
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
        if (Config.ASSERTION_ENABLED)
            Check.arg(isTrue, message);
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
        if (Config.ASSERTION_ENABLED)
            Check.argNot(isFalse, message);
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
        if (Config.ASSERTION_ENABLED)
            Check.that(isTrue);
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
        if (Config.ASSERTION_ENABLED)
            Check.not(isFalse);
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
        if (Config.ASSERTION_ENABLED)
            Check.that(isTrue, message);
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
        if (Config.ASSERTION_ENABLED)
            Check.not(isFalse, message);
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
        if (Config.ASSERTION_ENABLED)
            Check.that(isTrue, exception);
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
        if (Config.ASSERTION_ENABLED)
            Check.not(isFalse, exception);
    }

    /**
     * Checks that the given {@code array} is not empty or {@code null} and throws an unchecked
     * exception if it is.
     *
     * @param array the {@code array} to be checked
     * @throws RuntimeException if the given {@code array} is {@code null} or empty.
     */
    @Contract("null -> fail")
    public static void notEmpty(@Nullable final Object[] array) {
        if (Config.ASSERTION_ENABLED)
            Check.notEmpty(array);
    }

    /**
     * Checks that the given {@code collection} is not empty or {@code null} and throws an unchecked
     * exception if it is.
     *
     * @param collection the {@code collection} to be checked
     * @throws RuntimeException if the given {@code collection} is {@code null} or empty.
     */
    @Contract("null -> fail")
    public static void notEmpty(@Nullable final Collection<?> collection) {
        if (Config.ASSERTION_ENABLED)
            Check.notEmpty(collection);
    }

    /**
     * Checks that the given {@code map} is not empty or {@code null} and throws an unchecked
     * exception if it is.
     *
     * @param map the {@code map} to be checked
     * @throws RuntimeException if the given {@code map} is {@code null} or empty.
     */
    @Contract("null -> fail")
    public static void notEmpty(@Nullable final Map<?, ?> map) {
        if (Config.ASSERTION_ENABLED)
            Check.notEmpty(map);
    }

    /**
     * Checks that the given {@code array} is not empty or {@code null} and throws an unchecked
     * exception if it is.
     *
     * @param array   the {@code array} to be checked
     * @param message the exception detail message. The detail message is saved for
     *                later retrieval by the {@link Exception#getMessage()} method.
     * @throws RuntimeException if the given {@code array} is {@code null} or empty.
     */
    @Contract("null, _ -> fail")
    public static void notEmpty(@Nullable final Object[] array,
                                @Nullable final String message) {
        if (Config.ASSERTION_ENABLED)
            Check.notEmpty(array, message);
    }

    /**
     * Checks that the given {@code collection} is not empty or {@code null} and throws an unchecked
     * exception if it is.
     *
     * @param collection the collection to be checked
     * @param message    the exception detail message. The detail message is saved for
     *                   later retrieval by the {@link Exception#getMessage()} method.
     * @throws RuntimeException if the given {@code collection} is {@code null} or empty.
     */
    @Contract("null, _ -> fail")
    public static void notEmpty(@Nullable final Collection<?> collection,
                                @Nullable final String message) {
        if (Config.ASSERTION_ENABLED)
            Check.notEmpty(collection, message);
    }

    /**
     * Checks that the given {@code map} is not empty or {@code null} and throws an unchecked
     * exception if it is.
     *
     * @param map     the {@code map} to be checked
     * @param message the exception detail message. The detail message is saved for
     *                later retrieval by the {@link Exception#getMessage()} method.
     * @throws RuntimeException if the given {@code map} is {@code null} or empty.
     */
    @Contract("null, _ -> fail")
    public static void notEmpty(@Nullable final Map<?, ?> map,
                                @Nullable final String message) {
        if (Config.ASSERTION_ENABLED)
            Check.notEmpty(map, message);
    }

    /**
     * Checks that the given {@code array} is not empty or {@code null}
     * and throws a RuntimeException if it is.
     *
     * @param array     the {@code array} to be checked
     * @param exception The runtime class of the exception instance that will be thrown, default
     *                  to {@code RuntimeException.class} if parameter is {@code null}
     * @throws RuntimeException if the given {@code array} is not empty or {@code null},
     *                          the exact runtime class of the thrown exception depends on the
     *                          {@code exception} parameter
     */
    @Contract("null, _ -> fail")
    public static void notEmpty(@Nullable final Object[] array,
                                @Nullable final Class<? extends RuntimeException> exception) {
        if (Config.ASSERTION_ENABLED)
            Check.notEmpty(array, exception);
    }

    /**
     * Checks that the given {@code collection} is not empty or {@code null}
     * and throws a RuntimeException if it is.
     *
     * @param collection the collection to be checked
     * @param exception  The runtime class of the exception instance that will be thrown, default
     *                   to {@code RuntimeException.class} if parameter is {@code null}
     * @throws RuntimeException if the given {@code collection} is not empty or {@code null},
     *                          the exact runtime class of the thrown exception depends on the
     *                          {@code exception} parameter
     */
    @Contract("null, _ -> fail")
    public static void notEmpty(@Nullable final Collection<?> collection,
                                @Nullable final Class<? extends RuntimeException> exception) {
        if (Config.ASSERTION_ENABLED)
            Check.notEmpty(collection, exception);
    }

    /**
     * Checks that the given {@code map} is not empty or {@code null}
     * and throws a RuntimeException if it is.
     *
     * @param map       the {@code map} to be checked
     * @param exception The runtime class of the exception instance that will be thrown, default
     *                  to {@code RuntimeException.class} if parameter is {@code null}
     * @throws RuntimeException if the given {@code map} is not empty or {@code null},
     *                          the exact runtime class of the thrown exception depends on the
     *                          {@code exception} parameter
     */
    @Contract("null, _ -> fail")
    public static void notEmpty(@Nullable final Map<?, ?> map,
                                @Nullable final Class<? extends RuntimeException> exception) {
        if (Config.ASSERTION_ENABLED)
            Check.notEmpty(map, exception);
    }

    /**
     * Checks that the given {@code array} is {@linkplain #notEmpty(Object[]) not empty}
     * and all of it's elements are non-null.
     *
     * @param objects the {@code array} to be checked
     * @throws RuntimeException if the {@code objects} {@code array} is {@code null}, empty or
     *                          contains a {@code null} element
     */
    @Contract("null -> fail")
    public static void noNullElements(@Nullable final Object[] objects) {
        if (Config.ASSERTION_ENABLED)
            Check.noNullElements(objects);
    }

    /**
     * Checks that the given iterable is not {@code null} and all of it's elements are non-null.
     *
     * @param objects the iterable to be checked
     * @throws RuntimeException if the {@code objects} iterable is {@code null}
     *                          or contains a {@code null} element
     */
    @Contract("null -> fail")
    public static void noNullElements(@Nullable final Iterable<?> objects) {
        if (Config.ASSERTION_ENABLED)
            Check.noNullElements(objects);
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
        if (Config.ASSERTION_ENABLED)
            Check.noNullKey(map);
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
        if (Config.ASSERTION_ENABLED)
            Check.noNullValues(map);
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
     * @throws IllegalArgumentException if one of the above conditions is violated
     */
    @Contract("null -> fail")
    public static void bijective(@Nullable final Map<?, ?> map) {
        if (Config.ASSERTION_ENABLED)
            Check.bijective(map);
    }

    /**
     * Fails immediately by throwing an unchecked exception with the given {@code message}
     *
     * @throws RuntimeException always
     */
    @Contract("-> fail")
    public static void notReached() {
        if (Config.ASSERTION_ENABLED)
            Check.notReached();
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
        if (Config.ASSERTION_ENABLED)
            Check.notReached(message);
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
        if (Config.ASSERTION_ENABLED)
            Check.illegalState(message);
    }

    /**
     * Checks if the current thread is the Android Main thread and throw an unchecked exception
     * if it is not.
     * The name of the current thread is included in the message of the thrown exception.
     *
     * @throws RuntimeException if the current thread is not the Main thread
     */
    @MainThread
    public static void isMainThread() {
        if (Config.ASSERTION_ENABLED)
            Check.isMainThread();
    }

    /**
     * Checks if the current thread is the Android Main thread and throw an unchecked exception
     * if it is not.
     *
     * @param message the exception detail message. The detail message is saved for
     *                later retrieval by the {@link Exception#getMessage()} method.
     * @throws RuntimeException if the current thread is not the Main thread
     */
    @MainThread
    public static void isMainThread(@Nullable final String message) {
        if (Config.ASSERTION_ENABLED)
            Check.isMainThread(message);
    }

    /**
     * Checks if the current thread <em>is not</em> the Android Main thread
     * and throw an unchecked exception if it is.
     *
     * @throws RuntimeException if the current thread is the Main thread
     */
    @WorkerThread
    public static void isNotMainThread() {
        if (Config.ASSERTION_ENABLED)
            Check.isNotMainThread();
    }

    /**
     * Checks if the current thread <em>is not</em> the Android Main thread
     * and throw an unchecked exception if it is.
     *
     * @param message the exception detail message. The detail message is saved for
     *                later retrieval by the {@link Exception#getMessage()} method.
     * @throws RuntimeException if the current thread is the Main thread
     */
    @WorkerThread
    public static void isNotMainThread(@Nullable final String message) {
        if (Config.ASSERTION_ENABLED)
            Check.isNotMainThread(message);
    }
}
