package dz.jsoftware95.silverbox.android.common;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Indicates that the annotated method is provided only to be overridden: it should not be
 * <i>invoked</i> from outside its declaring source file (as if it is {@code private}), and
 * overriding methods should not be directly invoked at all. Such a method represents a contract
 * between a class and its <i>subclasses</i> only, and is not to be considered part of the
 * <i>caller</i>-facing API of either class.
 *
 * <p>The annotated method must have protected or package-private visibility, and must not be {@code
 * static}, {@code final} or declared in a {@code final} class. Overriding methods must have either
 * protected or package-private visibility, although their effective visibility is actually "none".
 */
@Documented
@Retention(CLASS) // Parent source might not be available while compiling subclass
@Target(METHOD)
public @interface ForOverride {}