package org.checkerframework.checker.tainting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import org.checkerframework.checker.tainting.qual.Tainted;
import org.checkerframework.checker.tainting.qual.Untainted;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.framework.util.GraphQualifierHierarchy;
import org.checkerframework.framework.util.MultiGraphQualifierHierarchy;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;

/** The type factory for the Tainting Checker. */
public class TaintingAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {

    /** The @{@link Untainted} annotation. */
    protected AnnotationMirror TAINTED;
    /** The @{@link Tainted} annotation. */
    protected AnnotationMirror UNTAINTED;

    /**
     * Constructor function and building TAINTED, UNTAINTED annotation mirrors from classes.
     *
     * @param checker the associated {@link TaintingChecker}
     */
    public TaintingAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker);

        UNTAINTED = AnnotationBuilder.fromClass(elements, Untainted.class);
        TAINTED = AnnotationBuilder.fromClass(elements, Tainted.class);

        this.postInit();
    }

    @Override
    public QualifierHierarchy createQualifierHierarchy(
            MultiGraphQualifierHierarchy.MultiGraphFactory factory) {
        return new TaintingAnnotatedTypeFactory.TaintingQualifierHierarchy(factory);
    }

    /**
     * A custom qualifier hierarchy for the Tainting Checker. This makes the @Tainted annotation as
     * the top default and the @Untainted annotation as the bottom annotation. {@code @Tainted} with
     * a string argument will become a subtype of {@code @Tainted} whereas {@code @Untainted}
     * becomes a subtype of {@code @Untainted} with a string argument. For example,
     * {@code @Tainted("SQL")} is a subtype of {@code @Tainted} and {@code @Untainted} is a subtype
     * of {@code @Untainted("SQL")}.
     */
    protected class TaintingQualifierHierarchy extends GraphQualifierHierarchy {

        /**
         * Constructor function to create hierarchy from the bottom class.
         *
         * @param factory of checker whose hierarchy must be created
         */
        public TaintingQualifierHierarchy(MultiGraphFactory factory) {
            super(factory, UNTAINTED);
        }

        /**
         * Extracts the string array argument from the tainted/untainted annotation.
         *
         * @param anno of checker whose values need to be returned
         * @return string list made from the array elements given as argument
         */
        private List<String> extractValues(AnnotationMirror anno) {
            Map<? extends ExecutableElement, ? extends AnnotationValue> valMap =
                    anno.getElementValues();

            List<String> res;
            if (valMap.isEmpty()) {
                res = new ArrayList<>();
            } else {
                res = AnnotationUtils.getElementValueArray(anno, "value", String.class, true);
            }
            return res;
        }

        @Override
        public boolean isSubtype(AnnotationMirror subAnno, AnnotationMirror superAnno) {
            if (AnnotationUtils.areSameByName(subAnno, UNTAINTED)
                    && AnnotationUtils.areSameByName(superAnno, UNTAINTED)) {

                List<String> lhsValues = extractValues(superAnno);
                List<String> rhsValues = extractValues(subAnno);

                return rhsValues.isEmpty()
                        || (!lhsValues.isEmpty() && rhsValues.containsAll(lhsValues));
            }
            if (AnnotationUtils.areSameByName(subAnno, TAINTED)
                    && AnnotationUtils.areSameByName(superAnno, TAINTED)) {

                List<String> lhsValues = extractValues(superAnno);
                List<String> rhsValues = extractValues(subAnno);

                return lhsValues.isEmpty() || rhsValues.containsAll(lhsValues);
            }
            // Ignore annotation values to ensure that annotation is in supertype map.
            if (AnnotationUtils.areSameByName(superAnno, UNTAINTED)
                    && AnnotationUtils.areSameByName(subAnno, TAINTED)) {
                return false;
            }
            if (AnnotationUtils.areSameByName(subAnno, UNTAINTED)
                    && AnnotationUtils.areSameByName(superAnno, TAINTED)) {

                List<String> lhsValues = extractValues(superAnno);
                List<String> rhsValues = extractValues(subAnno);

                return lhsValues.isEmpty()
                        || rhsValues.isEmpty()
                        || rhsValues.containsAll(lhsValues);
            }
            return super.isSubtype(subAnno, superAnno);
        }
    }
}
