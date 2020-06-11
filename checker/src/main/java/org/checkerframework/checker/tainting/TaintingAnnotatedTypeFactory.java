package org.checkerframework.checker.tainting;

import javax.lang.model.element.AnnotationMirror;
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
import org.checkerframework.javacutil.TreeUtils;

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

    /** The method that returns the value element of a {@code @Untainted} annotation. */
    protected final ExecutableElement untaintedValueElement =
            TreeUtils.getMethod(
                    org.checkerframework.checker.tainting.qual.Untainted.class.getName(),
                    "value",
                    0,
                    processingEnv);

    /** The method that returns the value element of a {@code @Tainted} annotation. */
    protected final ExecutableElement taintedValueElement =
            TreeUtils.getMethod(
                    org.checkerframework.checker.tainting.qual.Tainted.class.getName(),
                    "value",
                    0,
                    processingEnv);

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

        @Override
        public boolean isSubtype(AnnotationMirror subAnno, AnnotationMirror superAnno) {
            if (AnnotationUtils.areSameByName(subAnno, UNTAINTED)
                    && AnnotationUtils.areSameByName(superAnno, UNTAINTED)) {
                String rhsValue = getUntaintedValue(subAnno);
                return rhsValue.equals("") || AnnotationUtils.areSame(superAnno, subAnno);
            }
            if (AnnotationUtils.areSameByName(subAnno, TAINTED)
                    && AnnotationUtils.areSameByName(superAnno, TAINTED)) {
                String lhsValue = getTaintedValue(superAnno);
                return lhsValue.equals("") || AnnotationUtils.areSame(superAnno, subAnno);
            }
            // Ignore annotation values to ensure that annotation is in supertype map.
            if (AnnotationUtils.areSameByName(superAnno, UNTAINTED)
                    && AnnotationUtils.areSameByName(subAnno, TAINTED)) {
                return false;
            }
            if (AnnotationUtils.areSameByName(subAnno, UNTAINTED)
                    && AnnotationUtils.areSameByName(superAnno, TAINTED)) {
                String subVal = getUntaintedValue(subAnno);
                String superVal = getTaintedValue(superAnno);
                if (subVal.isEmpty() || superVal.isEmpty() || subVal.equals(superVal)) {
                    return true;
                } else {
                    return false;
                }
            }
            return super.isSubtype(subAnno, superAnno);
        }

        /**
         * Gets the value out of a untainted annotation.
         *
         * @param anno annotation mirror whose value needs to be found out
         * @return the String parameter of the untainted annotation
         */
        private String getUntaintedValue(AnnotationMirror anno) {
            return (String)
                    AnnotationUtils.getElementValuesWithDefaults(anno)
                            .get(untaintedValueElement)
                            .getValue();
        }

        /**
         * Gets the value out of a tainted annotation.
         *
         * @param anno annotation mirror whose value needs to be found out
         * @return the String parameter of the tainted annotation
         */
        private String getTaintedValue(AnnotationMirror anno) {
            return (String)
                    AnnotationUtils.getElementValuesWithDefaults(anno)
                            .get(taintedValueElement)
                            .getValue();
        }
    }
}
