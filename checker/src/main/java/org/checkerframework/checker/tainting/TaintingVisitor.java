package org.checkerframework.checker.tainting;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.WhileLoopTree;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;
import org.checkerframework.checker.tainting.qual.Tainted;
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
        if (!checker.hasOption("flow") || !checker.getBooleanOption("flow", true)) {
            return;
        }
        if (tree.getKind().asInterface() == MethodInvocationTree.class) {
            ExpressionTree MethodSelect = ((MethodInvocationTree) tree).getMethodSelect();
            ExpressionTree object = TreeUtils.getReceiverTree(MethodSelect);
            AnnotatedTypeMirror type = atypeFactory.getAnnotatedType(object);
            List<? extends ExpressionTree> arguments = ((MethodInvocationTree) tree).getArguments();
            if (type.hasAnnotation(Untainted.class)) {
                for (ExpressionTree ExpTree : arguments) {
                    AnnotatedTypeMirror arg = atypeFactory.getAnnotatedType(ExpTree);
                    if (arg.hasAnnotation(Tainted.class)) {
                        checker.reportError(tree, "method.invocation.flow.unsafe", ExpTree, arg);
                        break;
                    }
                }
            } else {
                for (ExpressionTree ExpTree : arguments) {
                    AnnotatedTypeMirror arg = atypeFactory.getAnnotatedType(ExpTree);
                    if (arg.hasAnnotation(Untainted.class)) {
                        checker.reportError(tree, "method.invocation.flow.unsafe", ExpTree, arg);
                        break;
                    }
                }
            }
        }
        if (tree.getKind().asInterface() == BinaryTree.class) {
            AnnotatedTypeMirror lhs =
                    atypeFactory.getAnnotatedType(((BinaryTree) tree).getLeftOperand());
            AnnotatedTypeMirror rhs =
                    atypeFactory.getAnnotatedType(((BinaryTree) tree).getRightOperand());
            if ((lhs.hasAnnotation(Untainted.class) ^ rhs.hasAnnotation(Untainted.class))
                    && (lhs.getKind() != TypeKind.NULL)
                    && (rhs.getKind() != TypeKind.NULL)) {
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
    public Void visitInstanceOf(InstanceOfTree node, Void p) {
        if (node.getType().getKind() == Tree.Kind.ANNOTATED_TYPE) {
            AnnotatedTypeMirror type = atypeFactory.getAnnotatedType(node.getType());
            AnnotatedTypeMirror exp = atypeFactory.getAnnotatedType(node.getExpression());
            if (atypeFactory.getTypeHierarchy().isSubtype(type, exp)
                    && !type.getAnnotations().equals(exp.getAnnotations())) {
                checker.reportWarning(node, "operand.instanceof.subtype", exp, type);
            }
        }
        return super.visitInstanceOf(node, p);
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
