package org.checkerframework.checker.tainting.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.DefaultQualifierInHierarchy;
import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.TargetLocations;
import org.checkerframework.framework.qual.TypeUseLocation;

/**
 * Denotes a possibly-tainted value: at run time, the value might be tainted or might be untainted.
 *
 * <p>This is the top qualifier of the tainting type system. This annotation is associated with the
 * {@link org.checkerframework.checker.tainting.TaintingChecker}.
 *
 * @see Untainted
 * @see org.checkerframework.checker.tainting.TaintingChecker
 * @checker_framework.manual #tainting-checker Tainting Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@TargetLocations({TypeUseLocation.EXPLICIT_LOWER_BOUND, TypeUseLocation.EXPLICIT_UPPER_BOUND})
@DefaultQualifierInHierarchy
@SubtypeOf({})
public @interface Tainted {
    /** Data types for which the object is Tainted. */
    String[] value() default "";
}
