package org.checkerframework.checker.tainting;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.source.SupportedOptions;
import org.checkerframework.framework.source.SuppressWarningsPrefix;

/**
 * A type-checker plug-in for the Tainting type system qualifier that finds (and verifies the
 * absence of) trust bugs.
 *
 * <p>It verifies that only verified values are trusted and that user-input is sanitized before use.
 *
 * @checker_framework.manual #tainting-checker Tainting Checker
 */
@SuppressWarningsPrefix({"untainted", "tainting"})
@SupportedOptions({

    // Checks for instances where indirect information flow can take place
    "indirectInfoFlow",

    // Tells the qual files of the annotations which the Tainting Checker has to check
    "quals",

    // Tells the directory path of the annotations which the Tainting Checker has to check
    "qualDirs"
})
public class TaintingChecker extends BaseTypeChecker {}
