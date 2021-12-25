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

import android.os.BaseBundle;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.google.common.math.DoubleMath;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * A Utility class for representing different types of objects as strings or iterators,
 * in a cross-type and consistence way, while encapsulating the details.
 *
 * <h2><a id='main-doc'>Container vs Non-Container Types</a></h2>
 *
 * <h3>Terminology</h3>
 * Any type that can be converted to an {@code Iterator} is called a "Container", any other type is
 * considered "Non-Container"
 *
 * <h3>Type resolving logic</h3>
 * Common containers types are supported out of the box, for other types custom
 * {@linkplain ContainerAdapter adapters} are needed.
 * Given an instance implement may types as an input the priority between these types is as follow:
 * <ul>
 * <li>If the instance is an {@linkplain Entry Entry} then it's not a <a href='#main-doc'>container</a></li>
 * <li>otherwise the first {@linkplain ContainerAdapter Adapter} that match the instance (in the
 * order of {@link Builder#addAdapter(ContainerAdapter, Class) adding}) will be used to
 * convert it to an Iterator</li>
 * <li>if no adapter match the instance then the default strategy will be used</li>
 * </ul>
 * <p>
 * The default strategy is to test whether the instance is an:
 * <ol>
 * <li>Array</li>
 * <li>Iterator</li>
 * <li>Iterable</li>
 * <li>Enumeration</li>
 * <li>Map</li>
 * <li>BaseBundle (Android SDK)</li>
 * </ol>
 * <p>
 * Notes:
 * <ul>
 * <li>Arrays of primitives are lazily wrapped</li>
 * <li>Iterators instances are used themselves without any conversion</li>
 * <li>Maps and Bundles are converted to Iterators of {@linkplain Entry entries}</li>
 * </ul>
 * <p>
 * If the instance is:
 * <em>not</em> an Entry <em>and</em> could <em>not</em> be converted
 * to an Iterator using the above logic then it is considered to be a
 * <a href='#main-doc'>non-container</a> type
 *
 * <h2>String Representation templates</h2>
 *
 * <h3><a id='template'>Representation Templates</a></h3>
 * Syntax:
 * <ul>
 * <li>{@code ?} denotes that there is at most one occurrence</li>
 * <li>{@code +} denotes that there is at least one occurrence</li>
 * </ul>
 * The general form of a string representation is:
 * <p>
 * {@code {BEFORE_ALL}{BEFORE_CONTAINER}headers?{HEADERS_ELEMENTS_SEPARATOR}?elements?{AFTER_CONTAINER}{AFTER_ALL}}
 * <ul>
 * <li>
 * <strong>If headers are present</strong> (has been {@linkplain Builder#addHeader(Class, String, Header) added})
 * hen they will get represented according to this template:
 * <br>
 * {@code {BEFORE_HEADERS}({header label}{HEADER_EQUALS}{header text}{HEADER_SEPARATOR}?)+{AFTER_HEADERS}}
 * <br>
 * Note that HEADER_SEPARATOR is only present in between headers (not at the end)
 * </li>
 * <li>
 * <strong>If BOTH headers and elements are present</strong> then HEADERS_ELEMENTS_SEPARATOR
 * will be added between them otherwise the representation will only contain the headers OR the elements
 * </li>
 * <li>
 * <strong>If elements are present</strong> (the <a href='#main-doc'>container</a> is not empty):
 * <br>
 * - In case the interval mode is {@linkplain Builder#enableIntervalMode() enabled} AND the count
 * of elements in the <a href='#main-doc'>container</a> is absolutely bigger than the
 * {@linkplain Builder#setMaxSize(int) max size} then this template is used:
 * <br>
 * {@code {INTERVAL_OPENING}{first element}{ETC}{INTERVAL_CLOSING}{last element}}
 * <br>
 * - And in case the interval mode is not enabled but the count of elements in the
 * <a href='#main-doc'>container</a> is absolutely bigger than the max size then this template is used:
 * <br>
 * {@code ({element}{ELEMENT_SEPARATOR})+{ETC}}
 * <br>
 * - Otherwise this template is used:
 * {@code ({element}{ELEMENT_SEPARATOR}?)+}
 * <br>
 * Note that ELEMENT_SEPARATOR is only present in between elements (and not at the end)
 * </li>
 * </ul>
 * <h3>Non Containers</h3>
 * In case the object being represented is not a <a href='#main-doc'>container</a> then:
 * <ul>
 * <li>If it is an Entry this template will be used:
 * <br>
 * {@code {entry key}{KEY_EQUALS}{entry value}}
 * </li>
 * <li>
 * Otherwise this template will get used:
 * <br>
 * {@code {BEFORE_ALL}{object}{AFTER_ALL}}
 * <br>
 * - Generally the instance's {@link Object#toString() toString()} method will determine it's String representation
 * <br>
 * - Any {@link Number number} other than: Byte, Short, Integer, Long, Float, Double, BigInteger
 * and BigDecimal is converted into Double or Long depending on whether it's value is integral or no
 * (this may cause some custom decimal number implementations to get represented as integers)
 * <br>
 * - Decimal numbers doesn't follow the general rule, they are formatted using the format:
 * "{@link #FORMAT %.5g}" and {@link #LOCALE US} Local
 * </li>
 * </ul>
 *
 * <h2><a id='examples'>String Representation Examples</a></h2>
 * To obtain an instance of a Represent you must use the builder:
 * <pre>{@code
 *     Represent represent = Represent.builder()
 *         // some configuration goes here
 *         .build();
 * }</pre>
 * In case no configuration is needed then there is no need to create an instance in the first place
 * simply use the constant {@link #SIMPLY} or the shortcut method {@link #the(Object)}
 * <p>
 * Example 1:
 * <pre>{@code
 *     final int[] instance = {1, 2, 3};
 *     final String output = "[1, 2, 3]";
 *
 *     assertThat(Represent.SIMPLY.asString(instance), is(output));
 *     assertThat(Represent.the(instance), is("Numbers: [1, 2, 3]"));
 * }</pre>
 * Other pre-configured instances are also available
 * <p>
 * Example 2:
 * <pre>{@code
 *     final Map<String, Integer> theData = new LinkedHashMap<>();
 *     theData.put("one", 1);
 *     theData.put("two", 2);
 *     theData.put("three", 3);
 *     theData.put("four", 4);
 *     theData.put("five", 5);
 *
 *     final String simpleOutput = Represent.SIMPLY.asString(theData); // max size = 10
 *     assertThat(simpleOutput, is("[one = 1, two = 2, three = 3, four = 4, five = 5]"));
 *
 *     final String compactOutput = Represent.COMPACTLY.asString(theData); // max size = 3
 *     assertThat(compactOutput, is("[one=1,two=2,three=3,...]"));
 *
 *     final String briefOutput = Represent.BRIEFLY.asString(theData); // max size = 2
 *     assertThat(briefOutput, is("[size: 5, non null: 5; from: one = 1 ... to: five = 5]"));
 *
 *     final String lengthOutput = Represent.LENGTHILY.asString(theData); // max size = 100
 *     assertThat(lengthOutput, is("[one -> 1; two -> 2; three -> 3; four -> 4; five -> 5]"));
 * }</pre>
 * Building a custom instance gives a lot of flexibility but require a proper configuration:
 * <br>
 * It is possible to {@linkplain Builder#addHeader(String, Header) add headers},
 * either by using the pre defined headers or by implementing other custom {@linkplain Header headers}.
 * <br>
 * For example the header can be used to produce an XML like output
 * <br>
 * Note that there is many libraries which are dedicated to generating xml output, and this class
 * in not intended to do so by any means, but this example shows how flexible the {@code Represent}
 * util is by generating an output with a syntax similar to XML
 * <br>
 * Example 3:
 * <pre>{@code
 *     final Represent representBottlesAsXML = Represent.builder()
 *             .addHeader("total", Represent.SIZE_HEADER)
 *             .addHeader("empty", Represent.NULL_COUNT_HEADER)
 *             .set(Token.BEFORE_CONTAINER, "<bottles ")
 *             .set(Token.HEADER_EQUALS, "=")
 *             .set(Token.HEADER_SEPARATOR, " ")
 *             .set(Token.HEADERS_ELEMENTS_SEPARATOR, ">")
 *             .set(Token.NO_ELEMENTS_PLACEHOLDER, " />")
 *             .set(Token.AFTER_ELEMENTS, "</bottles>")
 *             .set(Token.AFTER_CONTAINER, "")
 *             .build();
 *
 *     final Integer[] bottles = {8, 6, null, 9, 2};
 *     final String xml = representBottlesAsXML.asString(bottles);
 *     assertThat(xml, is("<bottles total=5 empty=1>8, 6, null, 9, 2</bottles>"));
 *
 *     final String emptyTag = representBottlesAsXML.asString(Collections.emptyList());
 *     assertThat(emptyTag, is("<bottles total=0 empty=0 />"));
 * }</pre>
 *
 * <h2>Style hierarchy</h2>
 *
 * <h3>Terminology</h3>
 *
 * <strong><a id='super'>the "super" function</a></strong>
 * <p>
 * {@code super(Class: C, Integer: N)} is a function that
 * returns the N-th super class of C or null if
 * C represent the Object class, an Interface, a Primitive or void
 * <p>
 * <strong><a id='interfaces'>the "interfaces" function</a></strong>
 * <p>
 * {@code interfaces(Class: C, Integer: N)} is a function that
 * returns the N-th interface implemented by the class C
 * in the order used is the order of the interface names in the {@code implements}
 * clause of the declaration of the class represented by this instance
 * <p>
 * <strong>Examples of the above functions</strong>
 * <p>
 * Let C be a {@code Class} instance that represent the C class in the code bellow:
 * <pre>{@code
 *     interface I1 {}
 *     interface I2 {}
 *     class A {}
 *     class B extends A {}
 *     class C extends B implements I1, I2 {}
 * }</pre>
 * then:
 * <ul>
 * <li>{@code super(C, -1) is undefined}</li>
 * <li>{@code super(C, 0) is C}</li>
 * <li>{@code super(C, 1) is B }(In other cases it may be {@code null})</li>
 * <li>{@code super(C, 2) is A }(In Other cases it may be {@code null} or
 * {@code undefined} if the super class of C is {@code null})</li>
 * <li>{@code super(C, 3) is undefined}</li>
 * <li>{@code interfaces(C, -1) is undefined}</li>
 * <li>{@code interfaces(C, 0) is I1}</li>
 * <li>{@code interfaces(C, 1) is I2}</li>
 * <li>{@code interfaces(C, 2) is undefined}</li>
 * </ul>
 * <strong><a id='array-comp'>the "array-comp" function</a></strong>
 * <p>
 * {@code array-comp(Array: A)} is a function that returns
 * the {@link Class#getComponentType() component} of the array A
 * <p>
 * <strong><a id='closest'>the "hierarchy" of an instance</a></strong>
 * <p>
 * The "hierarchy" of a given instance is an ordered list of {@link Class} objects that start with
 * the object that represent the runtime class of the given instance {@code (instance.getClass())}
 * followed by objects that represent the interfaces that are implemented by it (in the order they
 * show up in the {@code implements} clause), followed by the object that represent the super class
 * of the given instance and then the objects that represent the interfaces of the super class ...
 * and finally {@code null}
 * <p>
 * In case the instance is given instance is an instance of {@link Class} then it is considered the
 * first element in the hierarchy and the rest of elements are found using the above logic
 * <p>
 * For example the hierarchy of a "abc" is:
 * <p>
 * {@code {String.class, Serializable.class, Comparable.class, CharSequence.class, Object.class, null}}
 * <p>
 * and the hierarchy of Object.class is:
 * <p>
 * {@code {Object.class, null}}
 * <p>
 * while the hierarchy of Serializable.class is:
 * {@code {Serializable.class, null}}
 * <p>
 * <strong><a id='subtype'>the "subtype" relation</a></strong>
 * <p>
 * Note that all of <a href='#super'>super</a>, <a href='#array-comp'>array-comp</a> and
 * <a href='#interfaces'>interfaces</a> are used in this definition
 * <p>
 * Let C1 and C2 be references of {@link Class} such that C1 is nullable and
 * C2 is non-null, in the context of this documentation it is said that:
 * <p>
 * C2 is a "{@code subtype}" of C1 only and only if one of these four conditions are met:
 * <ol>
 * <li>A finite positive integer {@code N} exists such that {@code super(C2, N) == C1}</li>
 * <li>A finite positive integers {@code N, M} exists such that {@code interfaces(super(C2, N), M) == C1}</li>
 * <li>if C1 and C2 both represent array types and
 * A finite positive integer {@code N} exists such that {@code super(array-comp(C2), N) == array-comp(C1)}</li>
 * <li>if C1 and C2 both represent array types and
 * A finite positive integers {@code N, M} exists such that {@code interfaces(super(array-comp(C2), N), M) == array-comp(C1)}</li>
 * </ol>
 * - A short form of the subtype relation:
 * <p>
 * Let X be a non-null reference of an arbitrary instance, and C a nullable reference of {@link Class},
 * in the context of this documentation it is said that:
 * <p>
 * X is a subtype of C if and only if: {@code X.getClass() is a subtype of C}
 * <p>
 * The above definitions imply many relations, including but not limited to:
 * <ul>
 * <li>{@code String.class is a subtype of String.class}</li>
 * <li>{@code String.class is a subtype of Object.class}</li>
 * <li>{@code String.class is a subtype of null}</li>
 * <li>{@code "abc" is a subtype of String.class}</li>
 * <li>{@code "abc" is a subtype of Object.class}</li>
 * <li>{@code "abc" is a subtype of null}</li>
 * <li>{@code String[].class is a subtype of Object[].class}</li>
 * <li>{@code String[].class is a subtype of String[].class}</li>
 * <li>{@code String[].class is a subtype of Object.class}</li>
 * <li>{@code String[].class is a subtype of null}</li>
 * <li>{@code int.class is a subtype of null}</li>
 * <li>{@code Integer.class is a subtype of null}</li>
 * <li>{@code Serializable.class is a subtype of null}</li>
 * <li>{@code Void.class is a subtype of null}</li>
 * <li>{@code String is a subtype of Serializable.class}</li>
 * <li>{@code String is a subtype of CharSequence.class}</li>
 * </ul>
 *
 * <h3><a id='root'>Simple Typed Styles</a></h3>
 * EVERY style customization is attached to a distinct type called a "root", a given instance is represented
 * using the customization attached to the first root in the <a href='#hierarchy'>hierarchy</a>
 * of this instance
 * <p>
 * Example 4:
 * <pre>{@code
 *     final Represent represent = Represent.builder()
 *             .set(Token.BEFORE_CONTAINER, "(")
 *             // the above call is the same as: set(Token.BEFORE_CONTAINER, "(", null)
 *             .set(Token.AFTER_CONTAINER, ")")
 *             // the above call is the same as: set(Token.AFTER_CONTAINER, ")", null)
 *             .withRoot(Set.class)
 *             .set(Token.BEFORE_CONTAINER, "{")
 *             // now the above call is the same as: set(Token.BEFORE_CONTAINER, "{", Set.class)
 *             .set(Token.AFTER_CONTAINER, "}")
 *             // now the above call is the same as: set(Token.AFTER_CONTAINER, "}", Set.class)
 *             .build();
 *
 *     assertThat(represent.asString(Collections.emptySet()), is("{}"));
 *     assertThat(represent.asString(Collections.emptyMap()), is("()"));
 * }</pre>
 * In the sample above, any instance that is a <a href='#subtype'>subtype</a> of Set.class will use
 * the "{" and "}" tokens, other instances (which are technically subtypes of {@code null}) will use
 * the "(" and ")" tokens
 *
 * <h3>extending typed styles</h3>
 * Initially the builder instantiate only the style attached to the {@code null} <a href='#root'>root</a>
 * <p>
 * New roots are configured when they are configured for the first time,
 * new created roots implicitly get attached to a mutable copy of the style of the first root
 * (in the <a href='#hierarchy'>hierarchy</a> of this new root) that already exist
 * <p>
 * Example 5:
 * <pre>{@code
 *     final Represent representWithInheritance = Represent.builder()
 *             .withRoot(Object.class)
 *             .set(Token.BEFORE_ALL, "@")
 *             .withRoot(AbstractSet.class)
 *             // note that the first root in the hierarchy of the class AbstractSet.class is Object.class
 *             .set(Token.BEFORE_CONTAINER, "{")
 *             .set(Token.AFTER_CONTAINER, "}")
 *             .build();
 *
 *     final Represent representWithoutInheritance = Represent.builder()
 *             .withRoot(Object.class)
 *             .set(Token.BEFORE_ALL, "@")
 *             .withRoot(Set.class)
 *             // note that the first (and the only) root in the hierarchy of the interface Set.class is null
 *             .set(Token.BEFORE_CONTAINER, "{")
 *             .set(Token.AFTER_CONTAINER, "}")
 *             .build();
 *
 *     assertThat(representWithInheritance.asString(Collections.emptySet()), is("@{}"));
 *     assertThat(representWithoutInheritance.asString(Collections.emptySet()), is("{}"));
 * }</pre>
 */
@Immutable
public final class Represent {

    private static final String TAG = "Representer";

    /**
     * The local that is used to {@linkplain java.util.Formatter format} decimal numbers
     */
    public static final Locale LOCALE = Locale.US;

    /**
     * The format that is used to {@linkplain java.util.Formatter format} decimal numbers
     */
    public static final String FORMAT = "%.5g";

    /**
     * A header that print the {@linkplain Class#getSimpleName() name} of the class the input instance
     */
    public static Header CLASS_HEADER = elements ->
            elements.getContainerType().getSimpleName();

    /**
     * A header that print the size of the input <a href='#main-doc'>container</a>
     * (or "0" if the instance is a non-container)
     */
    public static Header SIZE_HEADER = elements ->
            elements.isEmpty() ? "0" : String.valueOf(elements.size() + elements.getSurplusElements().size());

    /**
     * A header that loop the whole <a href='#main-doc'>container</a> and count {@code null}
     * elements, then print the count (or "0" in case the instance is non-container)
     */
    public static Header NULL_COUNT_HEADER = new CountHeader() {

        @Override
        @Contract(value = "null -> true; !null -> false", pure = true)
        protected boolean isElementCounted(final Object element) {
            return element == null;
        }
    };

    /**
     * A header that loop the whole <a href='#main-doc'>container</a> and count elements that are
     * not {@code null}, then print the count (or "0" in case the instance is non-container)
     */
    public static Header NON_NULL_COUNT_HEADER = new CountHeader() {

        @Override
        @Contract(pure = true)
        protected boolean isElementCounted(final Object element) {
            return element != null;
        }
    };

    /**
     * An instance of {@code Represent} that has been initialized with no custom configuration
     * (default max size is {@code 10}),
     * <pre>{@code
     *     final Map<String, Integer> theData = new LinkedHashMap<>();
     *     theData.put("one", 1);
     *     theData.put("two", 2);
     *     theData.put("three", 3);
     *     theData.put("four", 4);
     *     theData.put("five", 5);
     *
     *     final String simpleOutput = Represent.SIMPLY.asString(theData);
     *     assertThat(simpleOutput, is("[one = 1, two = 2, three = 3, four = 4, five = 5]"));
     * }</pre>
     */
    public static final Represent SIMPLY = builder().build();

    /**
     * An instance of {@code Represent} that is configured with a max size of {@code 3},
     * and no empty spaces in between elements
     * <pre>{@code
     *     final Map<String, Integer> theData = new LinkedHashMap<>();
     *     theData.put("one", 1);
     *     theData.put("two", 2);
     *     theData.put("three", 3);
     *     theData.put("four", 4);
     *     theData.put("five", 5);
     *
     *     final String compactOutput = Represent.COMPACTLY.asString(theData);
     *     assertThat(compactOutput, is("[one=1,two=2,three=3,...]"));
     * }</pre>
     */
    public static final Represent COMPACTLY = builder()
            .setMaxSize(3)
            .set(Token.KEY_EQUALS, "=")
            .set(Token.ELEMENT_SEPARATOR, ",")
            .build();

    /**
     * An instance of {@code Represent} that is configured to avoid printing a long output by
     * enabling the {@linkplain Builder#enableIntervalMode() interval move} and setting
     * the max size to {@code 2}, also the {@linkplain #SIZE_HEADER size header} and
     * {@linkplain #NON_NULL_COUNT_HEADER non null header} are included to make up for the lake of
     * information caused by the brief output
     * <pre>{@code
     *     final Map<String, Integer> theData = new LinkedHashMap<>();
     *     theData.put("one", 1);
     *     theData.put("two", 2);
     *     theData.put("three", 3);
     *     theData.put("four", 4);
     *     theData.put("five", 5);
     *
     *     final String briefOutput = Represent.BRIEFLY.asString(theData);
     *     assertThat(briefOutput, is("[size: 5, non null: 5; from: one = 1 ... to: five = 5]"));
     * }</pre>
     */
    public static final Represent BRIEFLY = Represent.builder()
            .enableIntervalMode()
            .setMaxSize(2)
            .addHeader("size", Represent.SIZE_HEADER)
            .addHeader("non null", Represent.NON_NULL_COUNT_HEADER)
            .set(Token.ETC, " ... ")
            .build();

    /**
     * An instance of {@code Represent} with a max size of {@code 100} and a bit more expressive
     * {@linkplain Token#ELEMENT_SEPARATOR elements} separator and
     * {@linkplain Token#KEY_EQUALS key-value} separator
     * <pre>{@code
     *     final Map<String, Integer> theData = new LinkedHashMap<>();
     *     theData.put("one", 1);
     *     theData.put("two", 2);
     *     theData.put("three", 3);
     *     theData.put("four", 4);
     *     theData.put("five", 5);
     *
     *     final String lengthOutput = Represent.LENGTHILY.asString(theData); // max size = 100
     *     assertThat(lengthOutput, is("[one -> 1; two -> 2; three -> 3; four -> 4; five -> 5]"));
     * }</pre>
     */
    public static final Represent LENGTHILY = builder()
            .setMaxSize(100)
            .set(Token.ELEMENT_SEPARATOR, "; ")
            .set(Token.KEY_EQUALS, " -> ")
            .build();

    private static final int HASH_BIAS = 31;

    private final Map<Class<?>, ImmutableTokens> objectsTokens;
    private final Map<Class<?>, ImmutableTokens> arraysTokens;
    private final Map<Class<?>, ContainerAdapter> adapters;

    private Represent(@NonNull final Map<Class<?>, MutableTokens> objectsTokens,
                      @NonNull final Map<Class<?>, MutableTokens> arraysTokens,
                      @NonNull final Map<Class<?>, ContainerAdapter> adapters) {
        this.objectsTokens = copyTokens(objectsTokens);
        this.arraysTokens = copyTokens(arraysTokens);
        this.adapters = Collections.unmodifiableMap(adapters);
    }

    private Map<Class<?>, ImmutableTokens> copyTokens(@NonNull final Map<Class<?>, MutableTokens> tokens) {
        Check.nonNull(tokens.get(null));
        final Map<Class<?>, ImmutableTokens> immutableTokens = new HashMap<>(tokens.size(), 1);

        for (final Entry<Class<?>, MutableTokens> tokensEntry : tokens.entrySet())
            immutableTokens.put(tokensEntry.getKey(), new ImmutableTokens(tokensEntry.getValue()));

        return Collections.unmodifiableMap(immutableTokens);
    }

    /**
     * Converts the given {@code container} to an iterator in case it is a <a href='#main-doc'>container</a>
     * otherwise return {@code null}.
     * <p>
     * The logic used to convert the {@code container} is the same as described in the
     * <a href='#main-doc'>main documentation</a>
     * <p>
     * Due to the flexibility of {@linkplain ContainerAdapter container adapter} any
     * instance can be considered a <a href='#main-doc'>container</a> including {@code Strings}
     * <p>
     * Note that maps are considered as a collection of {@linkplain Entry entries}
     *
     * @param container the container to be converted
     * @return an {@code Iterator} that contains the elements of {@code container} or {@code null}
     * if the {@code container} is not a <a href='#main-doc'>container</a>
     */
    @Nullable
    @Contract(pure = true)
    public Iterator<?> asIterator(@NonNull final Object container) {
        Check.nonNull(container);
        final InstanceInspector instance = new InstanceInspector(container, adapters);
        return instance.isContainer() ? instance.getAsIterator() : null;
    }

    /**
     * Converts the given {@code container} to an iterator and cast it to the suitable generic type.
     * <p>
     * The logic used to convert the {@code container} is the same as described in the
     * <a href='#main-doc'>main documentation</a>
     * <p>
     * Due to the flexibility of {@linkplain ContainerAdapter container adapter} any
     * instance can be considered a <a href='#main-doc'>container</a> including {@code Strings}
     * <p>
     * Note that maps are considered as a collection of {@linkplain Entry entries}
     *
     * @param container the container to be converted
     * @param <T>       the generic parameter that the resulting iterator will be cast to
     * @return an {@code Iterator} that contains the elements of {@code container} or {@code null}
     * if the {@code container} is not a <a href='#main-doc'>container</a>
     * @throws IllegalArgumentException if the {@code container} is not a <a href='#main-doc'>container</a>
     */
    @NonNull
    @Contract(pure = true)
    @SuppressWarnings("unchecked")
    public <T> Iterator<T> asIteratorOf(@NonNull final Object container) {
        Check.nonNull(container);
        final InstanceInspector instance = new InstanceInspector(container, adapters);

        Check.arg(instance.isContainer());
        return (Iterator<T>) instance.getAsIterator();
    }

    /**
     * Returns the hash of the given {@code object} in the hexadecimal system.
     * <p>
     * Note that if the {@code object} is a <a href='#main-doc'>container</a>
     * then the hash of each element will be used to calculate the hash the container itself,
     * and the hash of {@code null} value is considered {@code 0}
     * </p>
     *
     * @param object the object to calculate it's hash
     * @return the hash of the {@code object} in the hexadecimal system
     */
    @NonNull
    @Contract(pure = true)
    public String asHexHash(@Nullable final Object object) {
        return "0x" + Integer.toHexString(asHash(object));
    }

    /**
     * Returns the hash of the given {@code object}.
     * <p>
     * Note that if the {@code object} is a <a href='#main-doc'>container</a>
     * then the hash of each element will be used to calculate the hash the container itself
     *
     * @param object the object to calculate it's hash
     * @return the hash of the {@code object} in the hexadecimal system
     */
    @Contract(pure = true)
    public int asHash(@Nullable final Object object) {
        return object == null ? 0 : calcHash(object);
    }

    @Contract(pure = true)
    private int calcHash(@NonNull final Object object) {
        final InstanceInspector instance = new InstanceInspector(object, adapters);
        if (instance.isContainer())
            return hashIterator(instance.getAsIterator());
        else
            return instance.getAsObject().hashCode();
    }

    @Contract(pure = true)
    private int hashIterator(@NonNull final Iterator<?> iterator) {
        int hash = 1;

        while (iterator.hasNext())
            hash = HASH_BIAS * hash + asHash(iterator.next());

        return hash;
    }

    /**
     * Represents this {@code object} as a string, see the <a href='#main-doc'>main</a> documentation
     * for more details.
     *
     * @param object the object to be represented
     * @return a String that represent the given {@code object}
     */
    @NonNull
    @Contract(pure = true)
    public String asString(@Nullable final Object object) {
        return object == null ? "null" : buildStringRepresentation(object);
    }

    @NonNull
    @Contract(pure = true)
    private String buildStringRepresentation(@NonNull final Object object) {
        final InstanceInspector instance = new InstanceInspector(object, adapters);
        final Tokens tokens = getTokens(instance);
        final StringBuilder output = new StringBuilder();

        if (instance.isEntry())
            output.append(instance.getEntryKey())
                    .append(tokens.get(Token.KEY_EQUALS))
                    .append(instance.getEntryValue());
        else
            appendInstance(instance, tokens, output);

        instance.close();
        return output.toString();
    }

    private void appendInstance(@NonNull final InstanceInspector instance,
                                @NonNull final Tokens tokens,
                                @NonNull final StringBuilder output) {
        output.append(tokens.get(Token.BEFORE_ALL));

        if (instance.isContainer())
            appendContainer(instance, tokens, output);
        else
            appendObject(instance, output);

        output.append(tokens.get(Token.AFTER_ALL));
    }

    private void appendContainer(@NonNull final InstanceInspector instance,
                                 @NonNull final Tokens tokens,
                                 @NonNull final StringBuilder output) {

        output.append(tokens.get(Token.BEFORE_CONTAINER));
        final LinkedElements elements = new LinkedElements(instance, tokens.getMaxSize());

        if (tokens.hasHeaders())
            appendHeaders(elements, tokens, output);

        if (elements.notEmpty()) {
            if (tokens.hasHeaders())
                output.append(tokens.get(Token.HEADERS_ELEMENTS_SEPARATOR));

            output.append(tokens.get(Token.BEFORE_ELEMENTS));

            if (tokens.isIntervalMode())
                appendInterval(elements, tokens, output);
            else
                appendElements(elements, tokens, output);

            output.append(tokens.get(Token.AFTER_ELEMENTS));
        } else
            output.append(tokens.get(Token.NO_ELEMENTS_PLACEHOLDER));

        elements.clear();
        output.append(tokens.get(Token.AFTER_CONTAINER));
    }

    private void appendHeaders(@NonNull final Elements elements,
                               @NonNull final Tokens tokens,
                               @NonNull final StringBuilder output) {
        output.append(tokens.get(Token.BEFORE_HEADERS));

        boolean firstHeaderAppended = false;
        for (final Entry<String, Header> headerEntry : tokens.getHeaders().entrySet()) {
            if (firstHeaderAppended)
                output.append(tokens.get(Token.HEADER_SEPARATOR));

            output.append(headerEntry.getKey())
                    .append(tokens.get(Token.HEADER_EQUALS))
                    .append(headerEntry.getValue().getHeaderText(elements));

            firstHeaderAppended = true;
        }

        output.append(tokens.get(Token.AFTER_HEADERS));
    }

    private void appendInterval(@NonNull final LinkedElements elements,
                                @NonNull final Tokens tokens,
                                @NonNull final StringBuilder output) {

        if (elements.hasSurplus())
            output.append(tokens.get(Token.INTERVAL_OPENING))
                    .append(asString(elements.getFirst()))
                    .append(tokens.get(Token.ETC))
                    .append(tokens.get(Token.INTERVAL_CLOSING))
                    .append(asString(elements.getSurplusElements().getLast()));
        else
            appendElements(elements, tokens, output);
    }

    private void appendElements(@NonNull final LinkedElements elements,
                                @NonNull final Tokens tokens,
                                @NonNull final StringBuilder output) {

        output.append(asString(elements.popFirst()));

        while (elements.notEmpty())
            output.append(tokens.get(Token.ELEMENT_SEPARATOR))
                    .append(asString(elements.popFirst()));

        if (elements.hasSurplus())
            output.append(tokens.get(Token.ELEMENT_SEPARATOR))
                    .append(tokens.get(Token.ETC));
    }

    private void appendObject(@NonNull final InstanceInspector instance,
                              @NonNull final StringBuilder output) {

        if (instance.isDecimalNumber())
            //noinspection MalformedFormatString since the object is known to be float at this point
            output.append(String.format(LOCALE, FORMAT, instance.getAsObject()));

        else
            output.append(instance.getRawString());
    }

    @NonNull
    @Contract(pure = true)
    private Tokens getTokens(@NonNull final InstanceInspector instance) {
        if (instance.isArray())
            return getTokensFrom(arraysTokens, instance.getArrayComponent());
        else
            return getTokensFrom(objectsTokens, instance.getInstanceRawClass());
    }

    @NonNull
    private Tokens getTokensFrom(@NonNull final Map<Class<?>, ImmutableTokens> typesTokens,
                                 @NonNull final Class<?> root) {
        final Tokens typedTokens = findToken(typesTokens, root);
        if (typedTokens == null) {
            final Tokens defaultTokens = typesTokens.get(null);
            Check.nonNull(defaultTokens);
            return defaultTokens;
        } else
            return typedTokens;
    }

    @Nullable
    @Contract(pure = true)
    private static <T extends Tokens> T findToken(@NonNull final Map<Class<?>, T> typesTokens,
                                                  @NonNull final Class<?> root) {

        Assert.nonNull(typesTokens);
        Assert.nonNull(root);

        Class<?> objectClass = root;
        do {
            final T tokens = getTokensOfThisClass(typesTokens, objectClass);
            if (tokens != null)
                return tokens;

            objectClass = objectClass.getSuperclass();
        } while (objectClass != null);

        return null;
    }

    @Nullable
    @Contract(pure = true)
    private static <T extends Tokens> T getTokensOfThisClass(@NonNull final Map<Class<?>, T> tokens,
                                                             @NonNull final Class<?> objectClass) {
        if (tokens.containsKey(objectClass))
            return tokens.get(objectClass);
        else
            for (final Class<?> currentInterface : objectClass.getInterfaces())
                if (tokens.containsKey(currentInterface))
                    return tokens.get(currentInterface);

        return null;
    }

    /**
     * Represents this {@code object} as a string using the {@linkplain #SIMPLY default}
     * representation, see the <a href='#main-doc'>main</a> documentation for more details.
     *
     * @param object the object to be represented
     * @return a String that represent the given {@code object}
     */
    @NonNull
    @Contract(pure = true)
    public static String the(@Nullable final Object object) {
        return SIMPLY.asString(object);
    }

    /**
     * Returns a new builder that can instantiate and configure a {@code Represent} instance
     *
     * @return a new represent builder
     */
    @NonNull
    @Contract(pure = true)
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns a new {@link FieldsAdapter} with a fields mapping that map each field to it's own name.
     *
     * @param fields the fields that the returned adapter should consider as elements
     * @return an adapter that adapt instance fields who are named exactly as the given
     * {@code fields} to an {@code Iterator} elements
     * @see FieldsAdapter
     */
    @NonNull
    @Contract(pure = true)
    public static ContainerAdapter fields(@NonNull final String... fields) {
        final LinkedHashMap<String, String> fieldsMapping = new LinkedHashMap<>();

        for (final String field : fields)
            fieldsMapping.put(field, field);

        return new FieldsAdapter(fieldsMapping);
    }

    /**
     * A builder that can instantiate and configure a new instance of {@code Represent},
     * see <a href='#main-doc'>container</a> for more details.
     * <p>
     * Note that each builder instance can only build a single {@code Represent} instance
     * <p>
     * <strong>Important:</strong>
     * <p>
     * This builder class (and in contrary to {@link Represent}) is <strong>NOT THREAD SAFE</strong>
     */
    @NotThreadSafe
    public static final class Builder {

        @NonNull
        private final AbstractMap<Class<?>, MutableTokens> objectTokens;

        @NonNull
        private final AbstractMap<Class<?>, MutableTokens> arrayTokens;

        @NonNull
        private final AbstractMap<Class<?>, ContainerAdapter> adapters;

        @NonNull
        private final MutableTokens defaultTokens;

        @Nullable
        private Class<?> currentRoot = null;

        private boolean active;

        private Builder() {
            defaultTokens = new MutableTokens();
            objectTokens = new HashMap<>(4);
            arrayTokens = new HashMap<>(4);
            adapters = new HashMap<>(4);
            active = true;
        }

        private Builder(@NonNull final Builder template) {
            if (!template.active)
                throw new IllegalStateException("you can not copy a closed builder");

            this.objectTokens = copyTokens(template.objectTokens);
            this.arrayTokens = copyTokens(template.arrayTokens);

            this.adapters = new HashMap<>(template.adapters.size());
            this.adapters.putAll(template.adapters);

            this.defaultTokens = new MutableTokens(template.defaultTokens);

            this.currentRoot = null;
            this.active = true;
        }

        @NonNull
        private AbstractMap<Class<?>, MutableTokens> copyTokens(@NonNull final AbstractMap<Class<?>, MutableTokens> tokens) {
            final AbstractMap<Class<?>, MutableTokens> tokensCopy = new HashMap<>(tokens.size());

            for (final Entry<Class<?>, MutableTokens> entry : tokens.entrySet())
                tokensCopy.put(entry.getKey(), new MutableTokens(entry.getValue()));

            return tokensCopy;
        }

        /**
         * Returns a deep copy of this builder instance, except
         * {@linkplain #addAdapter(ContainerAdapter, Class) the added adapters} as they are
         * simply referenced from the copy and not actually copied
         * (that's why container adapters should be stateless).
         * <p>
         * Note that the returned instance will always have it's
         * {@linkplain #withRoot(Class) current root} set to {@code null}
         *
         * @return a deep copy of this builder instance
         * @throws IllegalStateException if this instance was already closed ({@link #build()} has
         *                               been called) when calling this method
         */
        @NonNull
        @Contract(pure = true)
        public Builder copy() {
            return new Builder(this);
        }

        /**
         * Sets a new current root for this builder, witch means that further calls to
         * {@link #set(Token, String)} will use this new {@code currentRoot} instead of the old root
         * (the default root is {@code null}).
         * <p>
         * Note that copying this builder instance will not copy it's current root, instead the new
         * copy will have the default root {@code null}
         *
         * @param currentRoot the new current root of this builder
         * @return this instance
         */
        @NonNull
        public Builder withRoot(@Nullable final Class<?> currentRoot) {
            this.currentRoot = currentRoot;
            return this;
        }

        @NonNull
        private MutableTokens getTokensOf(@Nullable final Class<?> root) {
            if (root == null)
                return defaultTokens;
            else if (root.isArray())
                //noinspection ConstantConditions
                return getTokensFrom(arrayTokens, root.getComponentType());
            else
                return getTokensFrom(objectTokens, root);
        }

        @NonNull
        private MutableTokens getTokensFrom(@NonNull final Map<Class<?>, MutableTokens> typesTokens,
                                            @NonNull final Class<?> root) {
            MutableTokens tokens = findToken(typesTokens, root);
            if (tokens == null)
                tokens = new MutableTokens(defaultTokens);

            if (typesTokens.containsKey(root))
                return tokens;
            else {
                final MutableTokens newTokens = new MutableTokens(tokens);
                typesTokens.put(root, newTokens);
                return newTokens;
            }
        }

        /**
         * Sets a {@code token} of the {@linkplain #withRoot(Class) current root}
         * to the given {@code representation}
         *
         * @param token          the token (part) of the representation (of instances of the
         *                       current root) that this configuration will set
         * @param representation the new representation of the given {@code token} for instances
         *                       of the current root
         * @return this instance
         * @see #set(Class, Token, String)
         */
        @NonNull
        public Builder set(@NonNull final Token token,
                           @NonNull final String representation) {
            return set(currentRoot, token, representation);
        }

        /**
         * Sets a {@code token} of the {@code root} to the given {@code representation}
         *
         * @param root           the root that this configuration will apply to
         * @param token          the token (part) of the representation (of instances of the root)
         *                       that this configuration will set
         * @param representation the new representation of the given {@code token} for instances
         *                       of the root
         * @return this instance
         * @see #set(Token, String)
         */
        @NonNull
        public Builder set(@Nullable final Class<?> root,
                           @NonNull final Token token,
                           @NonNull final String representation) {
            getTokensOf(root).set(token, representation);
            return this;
        }

        /**
         * Sets a {@code token} of the {@linkplain #withRoot(Class) current root}
         * to {@code ""} (empty string)
         *
         * @param token the token (part) of the representation (of instances of the root)
         *              that this configuration will set
         * @return this instance
         * @see #clear(Class, Token)
         */
        @NonNull
        public Builder clear(@NonNull final Token token) {
            return clear(currentRoot, token);
        }

        /**
         * Sets a {@code token} of the {@linkplain #withRoot(Class) current root}
         * to {@code ""} (empty string)
         *
         * @param root  the root that this configuration will apply to
         * @param token the token (part) of the representation (of instances of the root)
         *              that this configuration will set
         * @return this instance
         * @see #clear(Token)
         */
        @NonNull
        public Builder clear(@Nullable final Class<?> root,
                             @NonNull final Token token) {
            getTokensOf(root).clear(token);
            return this;
        }

        /**
         * Enables the interval mode for the {@linkplain #withRoot(Class) current root}.
         * See the <a href='template'>docs</a> for more details
         *
         * @return this instance
         */
        @NonNull
        public Builder enableIntervalMode() {
            return enableIntervalMode(currentRoot);
        }

        /**
         * Enables the interval mode for the given {@code root}.
         * See the <a href='template'>docs</a> for more details
         *
         * @param root the root that this configuration will apply to
         * @return this instance
         */
        @NonNull
        public Builder enableIntervalMode(@Nullable final Class<?> root) {
            getTokensOf(root).setIntervalMode(true);
            return this;
        }

        /**
         * Disables the interval mode for the {@linkplain #withRoot(Class) current root}.
         * See the <a href='template'>docs</a> for more details
         *
         * @return this instance
         * @see #disableIntervalMode(Class)
         */
        @NonNull
        public Builder disableIntervalMode() {
            return disableIntervalMode(currentRoot);
        }

        /**
         * Disables the interval mode for the given {@code root}.
         * See the <a href='template'>docs</a> for more details
         *
         * @param root the root that this configuration will apply to
         * @return this instance
         * @see #disableIntervalMode()
         */
        @NonNull
        public Builder disableIntervalMode(@Nullable final Class<?> root) {
            getTokensOf(root).setIntervalMode(false);
            return this;
        }

        /**
         * Sets the {@code max size } for the {@linkplain #withRoot(Class) current root}.
         * See the <a href='template'>docs</a> for more details
         *
         * @param size the new {@code max size} (default is 10)
         * @return this instance
         * @see #setMaxSize(Class, int)
         */
        @NonNull
        public Builder setMaxSize(final int size) {
            return setMaxSize(currentRoot, size);
        }

        /**
         * Sets the {@code max size } for the given {@code root}.
         * See the <a href='template'>docs</a> for more details
         *
         * @param root the root that this configuration will apply to
         * @param size the new {@code max size} (default is 10)
         * @return this instance
         * @see #setMaxSize(int)
         */
        @NonNull
        public Builder setMaxSize(@Nullable final Class<?> root,
                                  final int size) {
            getTokensOf(root).setMaxSize(size);
            return this;
        }

        /**
         * Adds the {@link Header#getHeaderText(Elements) text} of the given {@code header}
         * with {@code label} as it's label to the configuration
         * of the {@linkplain #withRoot(Class) current root}.
         * See the <a href='template'>docs</a> for more details
         *
         * @param label  the text that will be printed right before the header value
         * @param header a function that takes the elements of a container and return the header text
         * @return this instance
         * @see #addHeader(Class, String, Header)
         */
        @NonNull
        public Builder addHeader(@NonNull final String label,
                                 @NonNull final Header header) {
            return addHeader(currentRoot, label, header);
        }

        /**
         * Adds the {@link Header#getHeaderText(Elements) text} of the given {@code header}
         * with {@code label} as it's label to the configuration of the given {@code root}.
         * See the <a href='template'>docs</a> for more details
         *
         * @param root   the root that this configuration will apply to
         * @param label  the text that will be printed right before the header value
         * @param header a function that takes the elements of a container and return the header text
         * @return this instance
         * @see #addHeader(String, Header)
         */
        @NonNull
        public Builder addHeader(@Nullable final Class<?> root,
                                 @NonNull final String label,
                                 @NonNull final Header header) {
            getTokensOf(root).addHeader(label, header);
            return this;
        }

        /**
         * Associates an {@linkplain ContainerAdapter container adapter} with the given {@code type}
         * which will effectively make this {@code type} a <a href='#main-doc'>container</a>
         * (sub classes of this {@code type} are not associated).
         * <p>
         * Note that {@code type} can not be:
         * <ul>
         * <li>{@code null}</li>
         * <li>a Map Entry (any class that implements {@link Entry})</li>
         * <li>a primitive</li>
         * <li>an interface</li>
         * <li>{@code Object.class}</li>
         * <li>{@code Void.class}</li>
         * </ul>
         * Note that it is possible to associate this adapter multiple times,
         * each time with a different type
         *
         * @param type    the type that will be associated with the given {@code adapter}
         * @param adapter the adapter that will be used to convert an instance (that it's runtime class
         *                type is represented by {@code type}) to an {@code Iterator}
         * @return this instance
         * @throws IllegalArgumentException if the adapter is {@code null} or
         *                                  if the given {@code type} is one values listed above or
         *                                  if an adapter has been already associated with this {@code type}
         */
        @NonNull
        public Builder addAdapter(@NonNull final ContainerAdapter adapter,
                                  @NonNull final Class<?> type) {
            Check.nonNull(adapter);
            Check.nonNull(type);

            Check.arg(type.getSuperclass() != null, "adapters can not adapt: " + type);
            Check.argNot(Entry.class.isAssignableFrom(type), "adapters can not adapt Map Entries");

            if (adapters.containsKey(type))
                throw new IllegalArgumentException("you can't add: " + adapter
                        + " because it conflict with: " + adapters.get(type));

            adapters.put(type, adapter);
            return this;
        }

        /**
         * Creates a new instance of {@code Represent} while passing all the configuration to
         * the new instance, please consider using a <a href='#examples'>constant</a> before
         * building an new instance.
         *
         * @return a new instance of {@code Represent}
         */
        @NonNull
        @Contract(pure = true)
        public Represent build() {
            if (!active)
                throw new IllegalStateException("this builder has been already closed");

            active = false;
            objectTokens.put(null, defaultTokens);
            arrayTokens.put(null, defaultTokens);
            return new Represent(objectTokens, arrayTokens, adapters);
        }
    }

    /**
     * A constant that represent a small token in the general <a href='#template'>template</a>
     * of string representations
     */
    public enum Token {
        /**
         * A prefix that will be added at the beginning of every representation
         * (be it a <a href='#main-doc'>container or a non-container</a>).
         * <p>
         * The default representation is {@code ""}
         * <p>
         * See the general <a href='#template'>template</a> for more details about the syntax
         */
        BEFORE_ALL(""),

        /**
         * A prefix that will be added before the headers if the <a href='#main-doc'>container</a> being
         * represented has at least one header.
         * <p>
         * The default representation is {@code ""}
         * <p>
         * See the general <a href='#template'>template</a> for more details about the syntax
         */
        BEFORE_HEADERS(""),

        /**
         * A separator that will be added between the label and
         * {@link Header#getHeaderText(Elements) text} of each header.
         * <p>
         * The default representation is {@code ": "}
         * <p>
         * See the general <a href='#template'>template</a> for more details about the syntax
         */
        HEADER_EQUALS(": "),

        /**
         * A separator that will be added between headers (note that header syntax is:
         * '{header label}{HEADER_EQUALS}{header text}').
         * <p>
         * The default representation is {@code ", "}
         * <p>
         * See the general <a href='#template'>template</a> for more details about the syntax
         */
        HEADER_SEPARATOR(", "),

        /**
         * A suffix that will be added after the headers, if the <a href='#main-doc'>container</a> being
         * represented has at least one header.
         * <p>
         * The default representation is {@code ""}
         * <p>
         * See the general <a href='#template'>template</a> for more details about the syntax
         */
        AFTER_HEADERS(""),

        /**
         * A separator that separates the headers and the elements of a <a href='#main-doc'>container</a>,
         * if the container has at least one header <strong>and</strong> it contains at least one element.
         * <p>
         * The default representation is {@code "; "}
         * <p>
         * See the general <a href='#template'>template</a> for more details about the syntax
         */
        HEADERS_ELEMENTS_SEPARATOR("; "),

        /**
         * A prefix that is added before the contents of the <a href='#main-doc'>container</a> being
         * represented.
         * <p>
         * Note that the contents may be: headers and elements, only elements, only headers
         * or nothing at all
         * <p>
         * The default representation is {@code "["}
         * <p>
         * See the general <a href='#template'>template</a> for more details about the syntax
         */
        BEFORE_CONTAINER("["),

        /**
         * A prefix that is added before adding the elements of the <a href='#main-doc'>container</a>
         * being represented, if the container is not empty (has at least one element).
         * <p>
         * The default representation is {@code ""}
         * <p>
         * See the general <a href='#template'>template</a> for more details about the syntax
         */
        BEFORE_ELEMENTS(""),

        /**
         * A separator that is added between the elements of the container being represented.
         * <p>
         * The default representation is {@code ", "}
         * <p>
         * See the general <a href='#template'>template</a> for more details about the syntax
         */
        ELEMENT_SEPARATOR(", "),

        /**
         * A placeholder that is added instead of the elements of the <a href='#main-doc'>container</a>
         * being represented if it is empty (has no elements).
         * <p>
         * The default representation is {@code ""}
         * <p>
         * See the general <a href='#template'>template</a> for more details about the syntax
         */
        NO_ELEMENTS_PLACEHOLDER(""),

        /**
         * A suffix that is added after the elements of the <a href='#main-doc'>container</a> being
         * represented, if the container is not empty (has at least one element).
         * <p>
         * The default representation is {@code ""}
         * <p>
         * See the general <a href='#template'>template</a> for more details about the syntax
         */
        AFTER_ELEMENTS(""),

        /**
         * A suffix that is added after the contents of the <a href='#main-doc'>container</a> being
         * represented.
         * <p>
         * Note that the contents may be: headers and elements, only elements, only headers
         * or nothing at all
         * <p>
         * The default representation is {@code "]"}
         * <p>
         * See the general <a href='#template'>template</a> for more details about the syntax
         */
        AFTER_CONTAINER("]"),

        /**
         * A suffix that will be added to the end of every representation
         * (be it a <a href='#main-doc'>container or a non-container</a>).
         * <p>
         * The default representation is {@code ""}
         * <p>
         * See the general <a href='#template'>template</a> for more details about the syntax
         */
        AFTER_ALL(""),

        /**
         * A separator that is added between the the key and value of a map
         * {@linkplain Entry entry}.
         * <p>
         * The default representation is {@code " = "}
         * <p>
         * See the general <a href='#template'>template</a> for more details about the syntax
         */
        KEY_EQUALS(" = "),

        /**
         * A placeholder that is added instead of the element number {@code max size + 1}
         * (the first element is number {@code 1}) of the <a href='#main-doc'>container</a> being
         * represented to indicated that some elements could not be shown because the max size has
         * been reached.
         * <p>
         * The default representation is {@code "..."}
         * <p>
         * See the general <a href='#template'>template</a> for more details about the syntax
         */
        ETC("..."),

        /**
         * A prefix that is added before the first element in interval mode.
         * <p>
         * The default representation is {@code "from: "}
         * <p>
         * See the general <a href='#template'>template</a> for more details about the syntax
         */
        INTERVAL_OPENING("from: "),

        /**
         * A prefix that is added before the last element in interval mode.
         * <p>
         * The default representation is {@code "to: "}
         * <p>
         * See the general <a href='#template'>template</a> for more details about the syntax
         */
        INTERVAL_CLOSING("to: ");

        @NonNull
        private final String defaultRepresentation;

        Token(@NonNull final String defaultRepresentation) {
            Assert.nonNull(defaultRepresentation);
            this.defaultRepresentation = defaultRepresentation;
        }

        /**
         * Returns the default representation of this token
         *
         * @return the default representation of this token
         */
        @NonNull
        @Contract(pure = true)
        public String defaultRepresentation() {
            return defaultRepresentation;
        }
    }

    /**
     * A function that takes the elements of a <a href='#main-doc'>container</a> and return a header that
     * will be printed before the actual elements.
     * <p>
     * Note that implementations of this interface are <em>must be side-effects-free and stateless</em>,
     * these requirements needed to guarantee the immutability and thread-safety of {@link Represent} instances
     * </p>
     */
    public interface Header {

        /**
         * Returns the header that corresponds to the given {@code elements} (of a container)
         *
         * @param elements the elements of the container that is being represented
         * @return a header that should be printed before the elements of the container that is being
         * represented
         */
        String getHeaderText(@NonNull final Elements elements);
    }

    /**
     * A helper basic implementation of a header that loop all the elements of a container and count
     * the number of elements that satisfy a certain condition
     */
    public static abstract class CountHeader implements Header {

        @NonNull
        @Override
        @Contract(pure = true)
        public final String getHeaderText(@NonNull final Elements elements) {
            return elements.isEmpty() ? "0" : countAllElements(elements);
        }

        @NonNull
        private String countAllElements(@NonNull final Elements elements) {
            int count = 0;
            for (final Object element : elements)
                if (isElementCounted(element))
                    count++;

            if (elements.hasSurplus())
                for (final Object element : elements.getSurplusElements())
                    if (isElementCounted(element))
                        count++;

            return String.valueOf(count);
        }

        /**
         * Returns whether the given element should be counted or no
         *
         * @param element an element of a container that this header is currently looping
         * @return {@code true} to count this element or {@code false} otherwise
         */
        protected abstract boolean isElementCounted(Object element);
    }

    /**
     * A function that takes an {@code Object} and return it's {@code Iterator} representation,
     * that can be used to represent the {@code Object} as a <a href='#main-doc'>container</a>.
     * <p>
     * Note that implementations of this interface are <em>must be side-effects-free and stateless</em>,
     * these requirements needed to guarantee the immutability and thread-safety of {@link Represent} instances
     * </p>
     */
    public interface ContainerAdapter {

        /**
         * Returns a custom {@code Iterator} representation of the given
         * {@code instance} that can be used to represent it as a <a href='#main-doc'>container</a>.
         *
         * @param instance an instance of a type that has been
         *                 {@linkplain Builder#addAdapter(ContainerAdapter, Class)}  associated}
         *                 with this adapter
         * @return the {@code Iterator} representation of the given {@code instance}
         */
        @Nullable
        Iterator<?> getIterator(@NonNull final Object instance);
    }

    /**
     * An adapter that convert a given instance to an Iterator of map entries, each entry contain
     * a string representation of a field as the entry key and it's value as the entry value, a map
     * of fields-names/fields-representations is needed to instantiate an instance of this adapter.
     */
    public static final class FieldsAdapter implements ContainerAdapter {

        private final Map<String, String> fieldsMapping;

        /**
         * Creates a new instance of this adapter using the given {@code fieldsMapping}, such that
         * each key in this map represent a name of a field (as returned by {@link Field#getName()})
         * that is supposed to get represented as an element by the returned
         * {@linkplain #getIterator(Object) iterator} and each entry value represent how this field
         * should be represented
         *
         * @param fieldsMapping a fields-names/fields-representations mapping for each field that is
         *                      supposed to get added as an element in the returned
         *                      {@linkplain #getIterator(Object) iterator}
         */
        public FieldsAdapter(@NonNull final Map<String, String> fieldsMapping) {
            Check.notEmpty(fieldsMapping);
            this.fieldsMapping = Check.bijective(fieldsMapping);
        }

        /**
         * Returns an iterator that contains entries that corresponds to the
         * {@code fields mapping} that was passed to the constructor of this adapter,
         * such that each entry key (in the returned iterator) represent field
         * (a value in the fields mapping of this adapter) and each entry value has
         * the actual value of that field in the given {@code instance}.
         * <p>
         * Note that it is not necessary to use all (or any) of the fields mapping, this mean that
         * for example if the given {@code instance} contains no fields that corresponds to any
         * mapping then an empty iterator is returned
         *
         * @param instance the instance that fields values will be extracted from
         * @return an iterator that contains entries that corresponds to the fields (that has been specified in
         * the {@code fields mapping} of this adapter) and their values
         */
        @NonNull
        @Override
        public Iterator<?> getIterator(@NonNull final Object instance) {
            final Map<String, Object> output = new LinkedHashMap<>(fieldsMapping.size());
            final Field[] declaredFields = instance.getClass().getDeclaredFields();

            for (final Field declaredField : declaredFields)
                if (fieldsMapping.containsKey(declaredField.getName()))
                    addField(instance, declaredField, output);

            return output.entrySet().iterator();
        }

        private void addField(@NonNull final Object instance,
                              @NonNull final Field declaredField,
                              @NonNull final Map<String, Object> output) {
            try {
                declaredField.setAccessible(true);
                final String mappedFieldName = fieldsMapping.get(declaredField.getName());
                Check.nonNull(mappedFieldName);
                output.put(mappedFieldName, declaredField.get(instance));
            } catch (Exception e) {
                Log.e(TAG, "could not get the value of field:" + declaredField.getName()
                        + " in class: " + instance.getClass().getName(), e);
            }
        }
    }

    /**
     * A collection of elements extracted from a <a href='#main-doc'>container</a>
     */
    @NotThreadSafe
    public interface Elements extends Iterable<Object> {

        /**
         * Returns an instance that represent the runtime class of the original container that it is
         * currently being represented.
         *
         * @return the type of the container
         */
        @NonNull
        Class<?> getContainerType();

        /**
         * Returns the first element in the container that it is currently being represented
         *
         * @return the first element in the container
         */
        @Nullable
        Object getFirst();

        /**
         * Returns the count of the <strong>main</strong> elements in the container that it is
         * currently being represented. If the total number of elements is bigger than the
         * {@linkplain Builder#setMaxSize(Class, int) max size} then the max size is returned,
         * since the rest of elements will be considered as {@linkplain #getSurplusElements() surplus}
         * elements
         *
         * @return the size of container elements or the max size allowed in case there is a surplus
         * @see #hasSurplus()
         * @see #getSurplusElements()
         */
        int size();

        /**
         * Returns whether the container (that it is currently being represented) is empty.
         *
         * @return {@code true} if the container has no elements and {@code false} otherwise
         */
        boolean isEmpty();

        /**
         * Returns whether the container (that it is currently being represented) is not empty.
         *
         * @return {@code true} if the container has at least one elements and {@code false} otherwise
         */
        boolean notEmpty();

        /**
         * Returns whether there is more elements than the
         * {@linkplain Builder#setMaxSize(Class, int) max size}
         * allowed, in that case the rest of elements can be abscessed via {@link #getSurplusElements()}.
         *
         * @return {@code true} it there is a more elements than the max size
         */
        boolean hasSurplus();

        /**
         * Returns the additional elements (from the <a href='#main-doc'>container</a> being represented)
         * that exceed the {@linkplain Builder#setMaxSize(Class, int) max size}
         * allowed, in case no such elements exist then an empty collection is returned
         *
         * @return the additional elements that exceed the max size allowed
         */
        @NonNull
        Deque<Object> getSurplusElements();

        /**
         * <p>
         * Note that the returned iterator contains the same number of elements as returned by
         * {@link #size()}, which may (or may not) be the total number of elements in the container,
         * use {@link #hasSurplus()} to know it there is extra elements and {@link #getSurplusElements()}
         * to get those elements
         *
         * @return the main elements of this container
         */
        @NonNull
        @Override
        Iterator<Object> iterator();
    }

    private static final class LinkedElements implements Elements {

        private static final Deque<Object> EMPTY_ELEMENTS = new LinkedList<>();

        private final Deque<Object> elements;

        private final Class<?> classOfInstance;

        @Nullable
        private final InstanceInspector containerWithSurplus;

        @Nullable
        private Deque<Object> surplusElements = null;

        private LinkedElements(@NonNull final InstanceInspector instance,
                               final int maxSize) {
            classOfInstance = instance.getInstanceRawClass();

            if (instance.hasNext()) {
                elements = extract(instance, maxSize);
                containerWithSurplus = instance.hasNext() ? instance : null;
            } else {
                elements = EMPTY_ELEMENTS;
                containerWithSurplus = null;
            }
        }

        @NonNull
        private Deque<Object> extract(@NonNull final InstanceInspector instance,
                                      final int maxSize) {

            final Deque<Object> elements = new LinkedList<>();
            elements.addFirst(instance.next());
            int size = 1;

            while (size < maxSize && instance.hasNext()) {
                elements.addLast(instance.next());
                size++;
            }

            return elements;
        }

        @NonNull
        @Override
        @Contract(pure = true)
        public Class<?> getContainerType() {
            return classOfInstance;
        }

        @Nullable
        @Override
        @Contract(pure = true)
        public Object getFirst() {
            return elements.getFirst();
        }

        @Override
        @Contract(pure = true)
        public int size() {
            return elements.size();
        }

        @Override
        @Contract(pure = true)
        public boolean isEmpty() {
            return elements.size() == 0;
        }

        @Override
        @Contract(pure = true)
        public boolean notEmpty() {
            return elements.size() > 0;
        }

        @Override
        @Contract(pure = true)
        public boolean hasSurplus() {
            return containerWithSurplus != null;
        }

        @NonNull
        @Override
        public Deque<Object> getSurplusElements() {
            if (containerWithSurplus == null)
                return EMPTY_ELEMENTS;

            if (surplusElements != null)
                return surplusElements;
            else
                return surplusElements = extract(containerWithSurplus, Integer.MAX_VALUE);
        }

        @NonNull
        @Override
        public Iterator<Object> iterator() {
            return elements.iterator();
        }

        @Nullable
        public Object popFirst() {
            return elements.removeFirst();
        }

        public void clear() {
            if (elements != EMPTY_ELEMENTS)
                elements.clear();

            if (surplusElements != null)
                surplusElements.clear();
        }
    }

    interface Tokens {
        @NonNull
        @Contract(pure = true)
        String get(@NonNull Token tokenName);

        @Contract(pure = true)
        boolean isIntervalMode();

        @Contract(pure = true)
        int getMaxSize();

        @Contract(pure = true)
        boolean hasHeaders();

        @NonNull
        @Contract(pure = true)
        Map<String, Header> getHeaders();
    }

    private static final class MutableTokens implements Tokens {

        private final String[] tokenStrings;
        private final Map<String, Header> headers;

        private boolean intervalMode;
        private int maxSize;

        private MutableTokens() {
            final String[] tokensCopy = new String[Token.values().length];
            for (final Token token : Token.values())
                tokensCopy[token.ordinal()] = token.defaultRepresentation();

            tokenStrings = tokensCopy;
            headers = new LinkedHashMap<>(4);
            intervalMode = false;
            maxSize = 10;
        }

        public MutableTokens(final MutableTokens tokens) {
            tokenStrings = tokens.tokenStrings.clone();
            headers = new LinkedHashMap<>(tokens.headers);
            intervalMode = tokens.intervalMode;
            maxSize = tokens.maxSize;
        }

        @Override
        @NonNull
        @Contract(pure = true)
        public String get(@NonNull final Token tokenName) {
            final String token = tokenStrings[tokenName.ordinal()];
            Assert.nonNull(token);
            return token;
        }

        private void set(@NonNull final Token token,
                         @NonNull final String value) {
            tokenStrings[token.ordinal()] = value;
        }

        private void clear(@NonNull final Token token) {
            tokenStrings[token.ordinal()] = "";
        }

        @Override
        @Contract(pure = true)
        public boolean isIntervalMode() {
            return intervalMode;
        }

        private void setIntervalMode(final boolean intervalMode) {
            this.intervalMode = intervalMode;
        }

        @Override
        @Contract(pure = true)
        public int getMaxSize() {
            return maxSize;
        }

        private void setMaxSize(final int maxSize) {
            if (maxSize < 0 || maxSize == Integer.MAX_VALUE)
                throw new IllegalArgumentException("max size must be in [0, Integer.MAX_VALUE - 1]");

            this.maxSize = maxSize;
        }

        @Override
        @Contract(pure = true)
        public boolean hasHeaders() {
            return !headers.isEmpty();
        }

        @Override
        @NonNull
        @Contract(pure = true)
        public Map<String, Header> getHeaders() {
            return headers;
        }

        private void addHeader(@NonNull final String label,
                               @NonNull final Header header) {
            Check.nonNull(label);
            Check.nonNull(header);

            headers.put(label, header);
        }
    }

    private static final class ImmutableTokens implements Tokens {
        final String[] tokenStrings;
        final Map<String, Header> headers;

        final boolean intervalMode;
        final int maxSize;

        public ImmutableTokens(@NonNull final MutableTokens tokens) {
            tokenStrings = tokens.tokenStrings.clone();
            headers = Collections.unmodifiableMap(tokens.headers);
            intervalMode = tokens.intervalMode;
            maxSize = tokens.maxSize;
        }

        @NonNull
        @Override
        public String get(@NonNull final Token token) {
            final String tokenString = tokenStrings[token.ordinal()];
            Assert.nonNull(tokenString);
            return tokenString;
        }

        @Override
        public boolean isIntervalMode() {
            return intervalMode;
        }

        @Override
        public int getMaxSize() {
            return maxSize;
        }

        @Override
        public boolean hasHeaders() {
            return !headers.isEmpty();
        }

        @NonNull
        @Override
        public Map<String, Header> getHeaders() {
            return headers;
        }
    }

    private static final class InstanceInspector {

        private static final Set<Class<?>> JDK_DECIMALS;
        private static final Set<Class<?>> JDK_NUMBERS;

        static {
            final Set<Class<?>> decimals = new HashSet<>(3);
            decimals.add(Float.class);
            decimals.add(Double.class);
            decimals.add(BigDecimal.class);

            final Set<Class<?>> numbers = new HashSet<>(8);
            numbers.addAll(decimals);
            numbers.add(Byte.class);
            numbers.add(Short.class);
            numbers.add(Integer.class);
            numbers.add(Long.class);
            numbers.add(BigInteger.class);

            JDK_DECIMALS = decimals;
            JDK_NUMBERS = numbers;
        }

        @NonNull
        private Object instanceAsObject;
        @NonNull
        private final Map<Class<?>, ContainerAdapter> adapters;
        @NonNull
        private Class<?> instanceRawClass;

        @Nullable
        private Entry<?, ?> instanceAsEntry = null;
        @Nullable
        private Iterator<?> instanceAsIterator = null;
        @Nullable
        private Class<?> arrayComponent = null;

        private int iteratorIndex = -1;

        private boolean isDecimalNumber;

        private final Map<Object, Integer> dejaVue = new IdentityHashMap<>();

        public InstanceInspector(@NonNull final Object instance,
                                 @NonNull final Map<Class<?>, ContainerAdapter> adapters) {

            this.adapters = Check.nonNull(adapters);

            instanceRawClass = instance.getClass();
            instanceAsObject = convertAndroidTypesToJDK(instance);

            if (instance instanceof Entry)
                instanceAsEntry = (Entry<?, ?>) instance;
            else {
                instanceAsIterator = instanceAsIterator(instance);
                if (instanceAsIterator == null)
                    isDecimalNumber = JDK_DECIMALS.contains(instanceAsObject.getClass());
            }
        }

        @Nullable
        @Contract(pure = true)
        private Iterator<?> instanceAsIterator(@NonNull final Object instance) {
            final Class<?> instanceClass = instance.getClass();

            if (adapters.containsKey(instanceClass))
                //noinspection ConstantConditions, the Builder will garantee that no null adapters are added
                return adapters.get(instanceClass).getIterator(instance);

            if (instanceClass == String.class || instanceClass.isPrimitive())
                return null;

            if (isArray(instance))
                return ArrayIterator.of(instance);

            else if (instance instanceof Iterator)
                return (Iterator<?>) instance;

            else if (instance instanceof Iterable)
                return ((Iterable<?>) instance).iterator();

            else if (instance instanceof Enumeration)
                return EnumerationIterator.of(instance);

            else if (instance instanceof Map)
                return ((Map<?, ?>) instance).entrySet().iterator();

            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && instance instanceof BaseBundle)
                return convertAndroidBaseBundle((BaseBundle) instance);

            else if (instance instanceof Bundle)
                return convertAndroidBundle((Bundle) instance);

            else if (instance instanceof LiveData)
                return new ArrayIterator.ObjectArrayIterator(new Object[]{((LiveData<?>) instance).getValue()});

            else
                return null;
        }

        private boolean isArray(@NonNull final Object instance) {
            arrayComponent = instance.getClass().getComponentType();
            return arrayComponent != null;
        }

        @NonNull
        @Contract(pure = true)
        //        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        private Iterator<?> convertAndroidBaseBundle(@NonNull final BaseBundle bundle) {
            final Set<String> keySet = bundle.keySet();

            if (keySet.isEmpty())
                return EmptyIterator.INSTANCE;
            else {
                final AbstractMap<String, Object> bundleElements = new LinkedHashMap<>();

                for (final String key : keySet)
                    bundleElements.put(key, bundle.get(key));

                return bundleElements.entrySet().iterator();
            }
        }

        @NonNull
        @Contract(pure = true)
        private Iterator<?> convertAndroidBundle(@NonNull final Bundle bundle) {
            final Set<String> keySet = bundle.keySet();

            if (keySet.isEmpty())
                return EmptyIterator.INSTANCE;
            else {
                final AbstractMap<String, Object> bundleElements = new LinkedHashMap<>();

                for (final String key : keySet)
                    bundleElements.put(key, bundle.get(key));

                return bundleElements.entrySet().iterator();
            }
        }

        @NonNull
        @Contract(pure = true)
        private Object convertAndroidTypesToJDK(@NonNull final Object rawInstance) {
            Assert.nonNull(rawInstance);

            if (rawInstance instanceof Number)
                if (JDK_NUMBERS.contains(rawInstance.getClass()))
                    return rawInstance;
                else
                    return convertNumber((Number) rawInstance);

            return rawInstance;
        }

        @NonNull
        private Number convertNumber(@NonNull final Number rawInstance) {
            final double number = rawInstance.doubleValue();
            if (DoubleMath.isMathematicalInteger(number))
                return (long) number;
            else
                return number;
        }

        @NonNull
        @Contract(pure = true)
        public Class<?> getInstanceRawClass() {
            return instanceRawClass;
        }

        @NonNull
        @Contract(pure = true)
        public Object getAsObject() {
            return instanceAsObject;
        }

        @NonNull
        @Contract(pure = true)
        public Iterator<?> getAsIterator() {
            return Check.nonNull(instanceAsIterator);
        }

        @NonNull
        public String getRawString() {
            return instanceAsObject.toString();
        }

        @Contract(pure = true)
        public boolean isEntry() {
            return instanceAsEntry != null;
        }

        @Nullable
        @Contract(pure = true)
        public Object getEntryKey() {
            Check.nonNull(instanceAsEntry, "you can call #getEntryKey only on an instance that #isEntry");
            return instanceAsEntry.getKey();
        }

        @Nullable
        @Contract(pure = true)
        public Object getEntryValue() {
            Check.nonNull(instanceAsEntry, "you can call #getEntryValue only on an instance that #isEntry");
            return instanceAsEntry.getValue();
        }

        @Contract(pure = true)
        public boolean isArray() {
            return arrayComponent != null;
        }

        @NonNull
        @Contract(pure = true)
        public Class<?> getArrayComponent() {
            Check.nonNull(arrayComponent, "you can call #getArrayComponent only on an instance that #isArray");
            return arrayComponent;
        }

        @Contract(pure = true)
        public boolean isContainer() {
            return instanceAsIterator != null;
        }

        public boolean hasNext() {
            Check.nonNull(instanceAsIterator, "you can call #hasNext only on an instance that #isContainer");
            return instanceAsIterator.hasNext();
        }

        @Nullable
        public Object next() {
            Check.nonNull(instanceAsIterator, "you can call #next only on an instance that #isContainer");
            final Object nextObject = instanceAsIterator.next();
            iteratorIndex++;

            if (nextObject != null)
                return getNext(nextObject);
            else
                return null;
        }

        @NotNull
        private Object getNext(@NonNull final Object nextObject) {
            final Integer dejaVuePosition = dejaVue.get(nextObject);
            if (dejaVuePosition != null) {
                final InstanceInspector nextObjectInspector = new InstanceInspector(nextObject, adapters);
                return nextObjectInspector.isContainer() ? "dejaVue@" + dejaVuePosition : nextObject;
            } else {
                dejaVue.put(nextObject, iteratorIndex);
                return nextObject;
            }
        }

        @Contract(pure = true)
        public boolean isDecimalNumber() {
            return isDecimalNumber;
        }

        public void close() {
            dejaVue.clear();

            //noinspection ConstantConditions
            instanceAsObject = null;
            //noinspection ConstantConditions
            instanceRawClass = null;

            instanceAsEntry = null;
            instanceAsIterator = null;

            arrayComponent = null;
            isDecimalNumber = false;
        }
    }

    private enum EmptyIterator implements Iterator<Object> {
        INSTANCE;

        @Override
        @Contract(pure = true)
        public boolean hasNext() {
            return false;
        }

        @Override
        @Contract("-> fail")
        public Object next() {
            throw new NoSuchElementException("iterator is empty");
        }
    }

    private static abstract class ArrayIterator implements Iterator<Object> {

        private int currentIndex = -1;

        @Override
        @Nullable
        public final Object next() {
            if (arrayAvailable() && hasMoreElements()) {
                currentIndex++;
                return getElementAt(currentIndex);
            } else
                throw new NoSuchElementException("iterator is empty");
        }

        @Override
        @Contract(pure = true)
        public final boolean hasNext() {
            return arrayAvailable() && hasMoreElements();
        }

        @Contract(pure = true)
        private boolean hasMoreElements() {
            final boolean hasNext = currentIndex < arrayLength() - 1;
            if (!hasNext)
                clearArray();
            return hasNext;
        }

        protected abstract boolean arrayAvailable();

        protected abstract int arrayLength();

        protected abstract Object getElementAt(final int index);

        protected abstract void clearArray();

        @NonNull
        @Contract(pure = true)
        public static Iterator<?> of(@NonNull final Object instance) {
            final Class<?> type = instance.getClass().getComponentType();
            Check.nonNull(type);

            if (type.isPrimitive())
                if (type == boolean.class)
                    return new BooleanArrayIterator((boolean[]) instance);

                else if (type == int.class)
                    return new IntArrayIterator((int[]) instance);

                else if (type == long.class)
                    return new LongArrayIterator((long[]) instance);

                else if (type == float.class)
                    return new FloatArrayIterator((float[]) instance);

                else if (type == double.class)
                    return new DoubleArrayIterator((double[]) instance);

                else if (type == char.class)
                    return new CharArrayIterator((char[]) instance);

                else if (type == byte.class)
                    return new ByteArrayIterator((byte[]) instance);

                else if (type == short.class)
                    return new ShortArrayIterator((short[]) instance);

            return new ObjectArrayIterator((Object[]) instance);
        }

        private static final class BooleanArrayIterator extends ArrayIterator {

            private boolean[] array;

            public BooleanArrayIterator(final boolean[] array) {
                Assert.nonNull(array);
                this.array = array;
            }

            @Override
            @Contract(pure = true)
            protected boolean arrayAvailable() {
                return array != null;
            }

            @Override
            @Contract(pure = true)
            protected int arrayLength() {
                return array.length;
            }

            @Override
            @Contract(pure = true)
            protected Object getElementAt(final int index) {
                return array[index];
            }

            @Override
            protected void clearArray() {
                array = null;
            }
        }

        private static final class IntArrayIterator extends ArrayIterator {

            private int[] array;

            public IntArrayIterator(final int[] array) {
                Assert.nonNull(array);
                this.array = array;
            }

            @Override
            @Contract(pure = true)
            protected boolean arrayAvailable() {
                return array != null;
            }

            @Override
            @Contract(pure = true)
            protected int arrayLength() {
                return array.length;
            }

            @Override
            @Contract(pure = true)
            protected Object getElementAt(final int index) {
                return array[index];
            }

            @Override
            protected void clearArray() {
                array = null;
            }
        }

        private static final class LongArrayIterator extends ArrayIterator {

            private long[] array;

            public LongArrayIterator(final long[] array) {
                Assert.nonNull(array);
                this.array = array;
            }

            @Override
            @Contract(pure = true)
            protected boolean arrayAvailable() {
                return array != null;
            }

            @Override
            @Contract(pure = true)
            protected int arrayLength() {
                return array.length;
            }

            @Override
            @Contract(pure = true)
            protected Object getElementAt(final int index) {
                return array[index];
            }

            @Override
            protected void clearArray() {
                array = null;
            }
        }

        private static final class FloatArrayIterator extends ArrayIterator {

            private float[] array;

            public FloatArrayIterator(final float[] array) {
                Assert.nonNull(array);
                this.array = array;
            }

            @Override
            @Contract(pure = true)
            protected boolean arrayAvailable() {
                return array != null;
            }

            @Override
            @Contract(pure = true)
            protected int arrayLength() {
                return array.length;
            }

            @Override
            @Contract(pure = true)
            protected Object getElementAt(final int index) {
                return array[index];
            }

            @Override
            protected void clearArray() {
                array = null;
            }
        }

        private static final class DoubleArrayIterator extends ArrayIterator {

            private double[] array;

            public DoubleArrayIterator(final double[] array) {
                Assert.nonNull(array);
                this.array = array;
            }

            @Override
            @Contract(pure = true)
            protected boolean arrayAvailable() {
                return array != null;
            }

            @Override
            @Contract(pure = true)
            protected int arrayLength() {
                return array.length;
            }

            @Override
            @Contract(pure = true)
            protected Object getElementAt(final int index) {
                return array[index];
            }

            @Override
            protected void clearArray() {
                array = null;
            }
        }

        private static final class CharArrayIterator extends ArrayIterator {

            private char[] array;

            public CharArrayIterator(final char[] array) {
                Assert.nonNull(array);
                this.array = array;
            }

            @Override
            @Contract(pure = true)
            protected boolean arrayAvailable() {
                return array != null;
            }

            @Override
            @Contract(pure = true)
            protected int arrayLength() {
                return array.length;
            }

            @Override
            @Contract(pure = true)
            protected Object getElementAt(final int index) {
                return array[index];
            }

            @Override
            protected void clearArray() {
                array = null;
            }
        }

        private static final class ByteArrayIterator extends ArrayIterator {

            private byte[] array;

            public ByteArrayIterator(final byte[] array) {
                Assert.nonNull(array);
                this.array = array;
            }

            @Override
            @Contract(pure = true)
            protected boolean arrayAvailable() {
                return array != null;
            }

            @Override
            @Contract(pure = true)
            protected int arrayLength() {
                return array.length;
            }

            @Override
            @Contract(pure = true)
            protected Object getElementAt(final int index) {
                return array[index];
            }

            @Override
            protected void clearArray() {
                array = null;
            }
        }

        private static final class ShortArrayIterator extends ArrayIterator {

            private short[] array;

            public ShortArrayIterator(final short[] array) {
                Assert.nonNull(array);
                this.array = array;
            }

            @Override
            @Contract(pure = true)
            protected boolean arrayAvailable() {
                return array != null;
            }

            @Override
            @Contract(pure = true)
            protected int arrayLength() {
                return array.length;
            }

            @Override
            @Contract(pure = true)
            protected Object getElementAt(final int index) {
                return array[index];
            }

            @Override
            protected void clearArray() {
                array = null;
            }
        }

        private static final class ObjectArrayIterator extends ArrayIterator {

            private Object[] array;

            public ObjectArrayIterator(final Object[] array) {
                Assert.nonNull(array);
                this.array = array;
            }

            @Override
            @Contract(pure = true)
            protected boolean arrayAvailable() {
                return array != null;
            }

            @Override
            @Contract(pure = true)
            protected int arrayLength() {
                return array.length;
            }

            @Override
            @Contract(pure = true)
            protected Object getElementAt(final int index) {
                return array[index];
            }

            @Override
            protected void clearArray() {
                array = null;
            }
        }
    }

    private static final class EnumerationIterator implements Iterator<Object> {

        @Nullable
        private Enumeration<?> enumeration;

        public EnumerationIterator(@NonNull final Enumeration<?> enumeration) {
            Assert.nonNull(enumeration);
            this.enumeration = enumeration;
        }

        @Override
        public boolean hasNext() {
            return enumeration != null && hasNext(enumeration);
        }

        private boolean hasNext(@NonNull final Enumeration<?> enumeration) {
            final boolean hasNext = enumeration.hasMoreElements();
            if (!hasNext)
                this.enumeration = null;
            return hasNext;
        }

        @Override
        public Object next() {
            if (enumeration != null)
                return enumeration.nextElement();
            else
                throw new NoSuchElementException("iterator is empty");
        }

        @NonNull
        @Contract(pure = true)
        public static Iterator<?> of(final Object instance) {
            return new EnumerationIterator((Enumeration<?>) instance);
        }

    }
}