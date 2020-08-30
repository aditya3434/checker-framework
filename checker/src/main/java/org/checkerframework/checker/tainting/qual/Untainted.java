package org.checkerframework.checker.tainting.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.DefaultFor;
import org.checkerframework.framework.qual.LiteralKind;
import org.checkerframework.framework.qual.QualifierForLiterals;
import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.TypeUseLocation;

/**
 * Denotes a reference that is untainted, i.e. can be trusted.
 *
 * @checker_framework.manual #tainting-checker Tainting Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@QualifierForLiterals(LiteralKind.STRING)
@SubtypeOf(Tainted.class)
@DefaultFor(TypeUseLocation.LOWER_BOUND)
public @interface Untainted {
    /**
     * Method that returns the string array argument of the {@code @Untainted} annotation. An empty
     * string array argument signifies the universal set of all strings, which is the default value
     * in this case.
     *
     * @return string array argument of the {@code @Untainted} annotation
     */
    String[] value() default "";
}
