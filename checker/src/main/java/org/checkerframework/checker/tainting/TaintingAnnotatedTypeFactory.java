package org.checkerframework.checker.tainting;

import org.checkerframework.checker.tainting.qual.Tainted;
import org.checkerframework.checker.tainting.qual.TaintedBottom;
import org.checkerframework.checker.tainting.qual.Untainted;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.framework.util.GraphQualifierHierarchy;
import org.checkerframework.framework.util.MultiGraphQualifierHierarchy;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;

import javax.lang.model.element.AnnotationMirror;

public class TaintingAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {
    protected AnnotationMirror TAINTED;
    protected AnnotationMirror UNTAINTED, TAINTED_BOTTOM;

    public TaintingAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker);

        TAINTED_BOTTOM = AnnotationBuilder.fromClass(elements, TaintedBottom.class);
        UNTAINTED = AnnotationBuilder.fromClass(elements, Untainted.class);
        TAINTED = AnnotationBuilder.fromClass(elements, Tainted.class);

        this.postInit();
    }

    @Override
    public QualifierHierarchy createQualifierHierarchy(MultiGraphQualifierHierarchy.MultiGraphFactory factory) {
        return new TaintingAnnotatedTypeFactory.TaintingQualifierHierarchy(factory);
    }

    protected class TaintingQualifierHierarchy extends GraphQualifierHierarchy {

        public TaintingQualifierHierarchy(MultiGraphFactory factory) {
            super(factory, TAINTED_BOTTOM);
        }

        @Override
        public boolean isSubtype(AnnotationMirror subAnno, AnnotationMirror superAnno) {
            if (AnnotationUtils.areSameByName(superAnno, UNTAINTED)
                    && AnnotationUtils.areSameByName(subAnno, UNTAINTED)) {
                return AnnotationUtils.areSame(superAnno, subAnno);
            }
            // Ignore annotation values to ensure that annotation is in supertype map.
            if (AnnotationUtils.areSameByName(superAnno, UNTAINTED)) {
                superAnno = UNTAINTED;
            }
            if (AnnotationUtils.areSameByName(subAnno, UNTAINTED)) {
                subAnno = UNTAINTED;
            }
            return super.isSubtype(subAnno, superAnno);
        }
    }
}
