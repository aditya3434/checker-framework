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

public class TaintingAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {
    protected AnnotationMirror TAINTED;
    protected AnnotationMirror UNTAINTED;

    public TaintingAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker);

        UNTAINTED = AnnotationBuilder.fromClass(elements, Untainted.class);
        TAINTED = AnnotationBuilder.fromClass(elements, Tainted.class);

        this.postInit();
    }

    protected final ExecutableElement untaintedValueElement =
            TreeUtils.getMethod(
                    org.checkerframework.checker.tainting.qual.Untainted.class.getName(),
                    "value",
                    0,
                    processingEnv);

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

    protected class TaintingQualifierHierarchy extends GraphQualifierHierarchy {

        public TaintingQualifierHierarchy(MultiGraphFactory factory) {
            super(factory, UNTAINTED);
        }

        @Override
        public boolean isSubtype(AnnotationMirror subAnno, AnnotationMirror superAnno) {
            if (AnnotationUtils.areSameByName(subAnno, UNTAINTED)
                    && AnnotationUtils.areSameByName(superAnno, UNTAINTED)) {
                String rhsValue = getUntaintedValue(subAnno);
                String lhsValue = getUntaintedValue(superAnno);
                return rhsValue.equals("") || AnnotationUtils.areSame(superAnno, subAnno);
            }
            if (AnnotationUtils.areSameByName(subAnno, TAINTED)
                    && AnnotationUtils.areSameByName(superAnno, TAINTED)) {
                String rhsValue = getTaintedValue(subAnno);
                String lhsValue = getTaintedValue(superAnno);
                return lhsValue.equals("") || AnnotationUtils.areSame(superAnno, subAnno);
            }
            // Ignore annotation values to ensure that annotation is in supertype map.
            if (AnnotationUtils.areSameByName(superAnno, UNTAINTED)) {
                superAnno = UNTAINTED;
            }
            if (AnnotationUtils.areSameByName(subAnno, UNTAINTED)) {
                subAnno = UNTAINTED;
            }
            if (AnnotationUtils.areSameByName(superAnno, TAINTED)) {
                superAnno = TAINTED;
            }
            if (AnnotationUtils.areSameByName(subAnno, TAINTED)) {
                subAnno = TAINTED;
            }
            return super.isSubtype(subAnno, superAnno);
        }

        private String getUntaintedValue(AnnotationMirror anno) {
            return (String)
                    AnnotationUtils.getElementValuesWithDefaults(anno)
                            .get(untaintedValueElement)
                            .getValue();
        }

        private String getTaintedValue(AnnotationMirror anno) {
            return (String)
                    AnnotationUtils.getElementValuesWithDefaults(anno)
                            .get(taintedValueElement)
                            .getValue();
        }
    }
}
