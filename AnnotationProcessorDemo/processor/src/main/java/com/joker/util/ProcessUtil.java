package com.joker.util;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

import javax.lang.model.element.Modifier;
import java.util.Set;

public class ProcessUtil {

    public static final String CONSTRUCTOR_NAME = "<init>";
    public static final String THIS = "this";

    public static boolean hasNoArgsConstructor(JCTree.JCClassDecl jcClass) {
        for (JCTree jcTree: jcClass.defs) {
            if (jcTree.getKind().equals(JCTree.Kind.METHOD)) {
                JCTree.JCMethodDecl jcMethod = (JCTree.JCMethodDecl) jcTree;
                if (CONSTRUCTOR_NAME.equals(jcMethod.name.toString())) {
                    if (jcMethod.params.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean hasAllArgsConstructor(List<JCTree.JCVariableDecl> jcVariables, JCTree.JCClassDecl jcClass) {
        for (JCTree jcTree: jcClass.defs) {
            if (jcTree.getKind().equals(Tree.Kind.METHOD)) {
                JCTree.JCMethodDecl jcMethod = (JCTree.JCMethodDecl) jcTree;
                if (CONSTRUCTOR_NAME.equals(jcMethod.name.toString())) {
                    if (jcVariables.size() == jcMethod.params.size()) {
                        boolean isEqual = true;
                        for (int i = 0; i < jcVariables.size(); ++i) {
                            // 这里是不是，如果顺序错误也不行呢？
                            if (!jcVariables.get(i).vartype.type.equals(jcMethod.params.get(i).vartype.type)) {
                                isEqual = false;
                                break;
                            }
                        }
                        if (isEqual) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static JCTree.JCVariableDecl cloneJCVariableAsParam(TreeMaker treeMaker, JCTree.JCVariableDecl prototypeJCVariable) {
        return treeMaker.VarDef(
                treeMaker.Modifiers(Flags.PARAMETER),
                prototypeJCVariable.name,
                prototypeJCVariable.vartype,
                null
        );
    }

    public static List<JCTree.JCVariableDecl> cloneJCVariablesAsParams(TreeMaker treeMaker, List<JCTree.JCVariableDecl> prototypeJCVariables) {
        ListBuffer<JCTree.JCVariableDecl> jcVariables = new ListBuffer<>();
        for (JCTree.JCVariableDecl jcVariable : prototypeJCVariables) {
            jcVariables.append(cloneJCVariableAsParam(treeMaker, jcVariable));
        }
        return jcVariables.toList();
    }

    public static boolean hasFromEntityConstructor(List<JCTree.JCVariableDecl> jcVariables, String pkg, JCTree.JCClassDecl jcClass) {
        if (jcVariables.size() != 1 || !jcVariables.get(0).vartype.type.toString().equals(pkg)) {
            return false;
        }
        for (JCTree jcTree: jcClass.defs) {
            if (jcTree.getKind().equals(Tree.Kind.METHOD)) {
                JCTree.JCMethodDecl jcMethod = (JCTree.JCMethodDecl) jcTree;
                if (CONSTRUCTOR_NAME.equals(jcMethod.name.toString())) {
                    if (jcMethod.params.size() == 1 && jcMethod.params.get(0).vartype.type.toString().equals(pkg)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static List<JCTree.JCVariableDecl> getJCVariables(JCTree.JCClassDecl jcClass) {
        ListBuffer<JCTree.JCVariableDecl> jcVariables = new ListBuffer<>();
        for (JCTree jcTree: jcClass.defs) {
            if (isValidField(jcTree)) {
                jcVariables.append((JCTree.JCVariableDecl) jcTree);
            }
        }
        return jcVariables.toList();
    }

    private static boolean isValidField(JCTree jcTree) {
        if (jcTree.getKind().equals(Tree.Kind.VARIABLE)) {
            JCTree.JCVariableDecl jcVariable = (JCTree.JCVariableDecl) jcTree;
            Set<Modifier> flagSets = jcVariable.mods.getFlags();
            return (!flagSets.contains(Modifier.STATIC) && !flagSets.contains(Modifier.FINAL));
        }
        return false;
    }
}
