package org.checkerframework.checker.tainting;

import com.sun.source.tree.*;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;
import org.checkerframework.checker.tainting.qual.Untainted;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;
import org.checkerframework.javacutil.TreeUtils;

/** Visitor for the {@link TaintingChecker}. */
public class TaintingVisitor extends BaseTypeVisitor<BaseAnnotatedTypeFactory> {

    /**
     * Creates a {@link TaintingVisitor}.
     *
     * @param checker the checker that uses this visitor
     */
    public TaintingVisitor(BaseTypeChecker checker) {
        super(checker);
    }

    /**
     * Don't check that the constructor result is top. Checking that the super() or this() call is a
     * subtype of the constructor result is sufficient.
     */
    @Override
    protected void checkConstructorResult(
            AnnotatedExecutableType constructorType, ExecutableElement constructorElement) {}

    /**
     * Checks whether indirect information flow cannot take place in given condition check.
     *
     * @param tree of condition statement that needs to be checked
     */
    private void checkCondition(ExpressionTree tree) {
        if (tree.getKind().asInterface().equals(BinaryTree.class)) {
            AnnotatedTypeMirror lhs =
                    atypeFactory.getAnnotatedType(((BinaryTree) tree).getLeftOperand());
            AnnotatedTypeMirror rhs =
                    atypeFactory.getAnnotatedType(((BinaryTree) tree).getRightOperand());
            if ((lhs.hasAnnotation(Untainted.class)) ^ (rhs.hasAnnotation(Untainted.class))
                    && (!lhs.getKind().equals(TypeKind.NULL))
                    && (!rhs.getKind().equals(TypeKind.NULL))) {
                checker.reportError(tree, "condition.flow.unsafe", lhs, rhs);
            }
        }
    }

    @Override
    public Void visitIf(IfTree node, Void p) {
        checkCondition(TreeUtils.withoutParens(node.getCondition()));
        return super.visitIf(node, p);
    }

    @Override
    public Void visitSwitch(SwitchTree node, Void p) {
        checkCondition(TreeUtils.withoutParens(node.getExpression()));
        return super.visitSwitch(node, p);
    }

    @Override
    public Void visitCase(CaseTree node, Void p) {
        if (node.getExpression() != null)
            checkCondition(TreeUtils.withoutParens(node.getExpression()));
        return super.visitCase(node, p);
    }

    @Override
    public Void visitDoWhileLoop(DoWhileLoopTree node, Void p) {
        checkCondition(TreeUtils.withoutParens(node.getCondition()));
        return super.visitDoWhileLoop(node, p);
    }

    @Override
    public Void visitWhileLoop(WhileLoopTree node, Void p) {
        checkCondition(TreeUtils.withoutParens(node.getCondition()));
        return super.visitWhileLoop(node, p);
    }

    @Override
    public Void visitForLoop(ForLoopTree node, Void p) {
        if (node.getCondition() != null) {
            checkCondition(TreeUtils.withoutParens(node.getCondition()));
        }
        return super.visitForLoop(node, p);
    }
}
